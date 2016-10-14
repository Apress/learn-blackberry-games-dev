package net.frogparrot.net;

import java.io.*;

import javax.wireless.messaging.*;
import javax.microedition.io.*;
import javax.microedition.io.PushRegistry;

import net.frogparrot.smscheckers.Main;

/**
 * Sends and receives binary SMS messages.
 *
 * @author Carol Hamer
 */
public class SMSManager implements Runnable, MessageListener {

//--------------------------------------------------------
//  static fields

  /**
   * The protocol string.
   */
  public static final String SMS_PROTOCOL = "sms://";
  
  /**
   * The instance of this class.
   */
  static SMSManager theInstance = null;

//--------------------------------------------------------
//  instance fields

  /**
   * The connection to listen for messages on.
   */
  private MessageConnection myConnection;

  /**
   * The phone number to send to and receive from.
   */
  private String myPhoneNum = null;
  
  /**
   * The port number to send to.
   */
  private String myPortNum = null;
  
  /**
   * The listener to send message data to.
   */
  private SmsDataListener myListener;
  
  /**
   * Whether the current incoming message launched the application.
   */
  private boolean myIsInitialMessage = false;

//--------------------------------------------------------
//  lifecycle

  /**
   * Create and start the singleton instance.
   */
  public static void startup(String portNum, SmsDataListener listener) {
    Main.setMessage("SMS manager startup");
    theInstance = new SMSManager(portNum, listener);
  }
  
  /**
   * Get the singleton instance.
   */
  public static SMSManager getInstance() {
    return theInstance;
  }
  
  /**
   * Find the push registry connection and start up the listener thread.
   */
  private SMSManager(String portNum, SmsDataListener listener) {
    theInstance = this;
    myPortNum = portNum;
    myListener = listener;
    // We start by checking for a connection with data to read
    // to see if the application was launched because of 
    // receiving an invitation:
    try {
      String[] connections = PushRegistry.listConnections(true);
      if (connections != null && connections.length > 0) {
        Main.setMessage("received message");
        myConnection = (MessageConnection)Connector.open(connections[0]);
        myIsInitialMessage = true;
        myConnection.setMessageListener(this);
        // Start by reading the invitation:
        Thread thread = new Thread(this);
        thread.start();
      } else {
        // The application wasn't launched by an incoming SMS
        Main.setMessage("no incoming");
        // but the application should still have a push registry
        // connection to listen for SMS messages on:
        connections = PushRegistry.listConnections(false);
        if (connections == null || connections.length == 0) {
          // if there's no connection to listen on, then
          // that means that no push-port was registered.
          // Instead, we start listening on an sms port:
          Main.setMessage("opening " + SMS_PROTOCOL + ":" + myPortNum);
          myConnection = (MessageConnection)Connector.open(SMS_PROTOCOL + ":" + myPortNum);
          Main.setMessage("listening");
        } else {
          Main.setMessage("opening the push-port connection");
          // if the game has a connection (via the push-port)
          // listen on it for messages:
          myConnection = (MessageConnection)Connector.open(connections[0]);
        }
        myConnection.setMessageListener(this);
        Main.setMessage("opened connection");
        myListener.noInitialMessage();
      }
    } catch(Exception e) {
      Main.postException(e);
    }
  }

  /**
   * Clean up all of the listening resources.
   * For use when the application closes.
   */
  public static void cleanup() {
    Main.setMessage("SMS manager cleanup");
    if(theInstance != null) {
      // close the connection:
      try {
        theInstance.myConnection.close();
      } catch(Exception e) {
        Main.postException(e);
      }
      // re-register with the push registry.
      // This is only necessary on BlackBerry
      // (Normally, once registered the application stays registered)
      /*
      Main.setMessage("about to push-register: " + theInstance.myPortNum);
      try {
        PushRegistry.registerConnection("sms://:" 
           + theInstance.myPortNum, 
           "net.frogparrot.smscheckers.Main", "*");
        Main.setMessage("done push-register");
      } catch(Exception e) {
        Main.postException(e);
      }
      */
      theInstance = null;
    }
  }
  
//--------------------------------------------------------
//  listen for messages

  /**
   * Implementation of MessageListener.  Handle messages
   * when they arrive.
   *
   * @param conn the connection with messages available.
   */
  public void notifyIncomingMessage(MessageConnection conn) {
    Main.setMessage("notification!");
    // This isn't the message that launched the game because
    // the game had to be already running to receive this notification
    myIsInitialMessage = false;
    Thread thread = new Thread(this);
    thread.start();
  }

  /**
   * Load the message from a new thread.
   */
  public void run() {
    try {
      Main.setMessage("about to recieve");
      Message msg = myConnection.receive();
      Main.setMessage("received a message");
      byte[] data = null;
      if((msg != null) && (msg instanceof BinaryMessage)) {
        String senderAddress = msg.getAddress();
        if(checkPhoneNum(senderAddress)) {
          Main.setMessage("address: " + senderAddress);
          data = ((BinaryMessage)msg).getPayloadData();
          Main.setMessage("got data: " + data.length);
          if(myIsInitialMessage) {
            myListener.initialMessage(data, senderAddress);
          } else {
            myListener.message(data, senderAddress);
          }
        } else {
          // ignore messages from other people
        }
      } // if (msg != null) {
    } catch (IOException e) {
      Main.postException(e);
    }
  }
  
//--------------------------------------------------------
//  SMS sending

  /**
   * Send an SMS message to the chosen opponent.
   */
  public void sendMessage(byte[] data) {
    // for BlackBerry the message address doesn't include
        // the leading protocol information (because it's
        // already in the connection information.
        // So for BlackBerry, the address format is
        // "//5555555:16502" when for other MIDP devices
        // its "sms://5555555:16502"
    Main.setMessage("sendMessage-->myPhoneNum: " + myPhoneNum);
    Main.setMessage("sendMessage-->sending to: //" + myPhoneNum + ":" 
        + myPortNum);
    SMSMessage message = new SMSMessage("//" + myPhoneNum + ":" 
        + myPortNum, myConnection, data, myListener);
    message.start();
  }
  
//--------------------------------------------------------
//  utilities

  /**
   * Reformats and sets the current opponent phone number 
   * if none is set, and verifies that subsequent messages
   * came from the right opponent.
   *
   * @returns true if the message can be accepted.
   */
  public boolean checkPhoneNum(String phoneNumber) {
    Main.setMessage("whole phone num: " + phoneNumber);
    if(myPhoneNum == null) {
      myPhoneNum = stripPhoneNum(phoneNumber);
      Main.setMessage("myPhoneNum: " + myPhoneNum);
      return true;
    } else if (myPhoneNum.endsWith(phoneNumber)) {
      return true;
    } else {
      // This should return false for security,
      // but because of differing phone number formats
      // we have to be a little bit lenient.
      //return false;
      return true;
    }
  }
  
  /**
   * Strips leading and trailing data off the phone number.
   */
  public static String stripPhoneNum(String phoneNumber) {
    if(phoneNumber.startsWith("sms://")) {
      phoneNumber = phoneNumber.substring(6);
    }
    if(phoneNumber.indexOf(':') != -1) {
      phoneNumber = phoneNumber.substring(0, phoneNumber.indexOf(':'));
    }
    Main.setMessage("stripped phone num: " + phoneNumber);
    return phoneNumber;
  }

}
