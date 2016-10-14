package net.frogparrot.net;

import java.io.*;
import javax.microedition.io.*;
import javax.wireless.messaging.*;

import net.frogparrot.smscheckers.Main;

/**
 * This class holds the data of a binary SMS message
 * and the functionality to send it.
 *
 * @author Carol Hamer
 */
public class SMSMessage extends Thread {

//--------------------------------------------------------
//  data fields

  /**
   * The string with the routing information to send an 
   * SMS to the right destination.
   */
  private String myAddress;

  /**
   * The data to send.
   */
  private byte[] myPayload;

  /**
   * The connection object that routes the message.
   */
  private MessageConnection myConnection;

  /**
   * The listener to notify when the message has been sent.
   */
  private SmsDataListener myListener;

//--------------------------------------------------------
//  initialization

  /**
   * Set the data and prepare the address string.
   */
  public SMSMessage(String address, MessageConnection conn, 
                   byte[] data, SmsDataListener listener) {
    myPayload = data;
    if(data == null) {
      Main.setMessage("empty message data");
    }
    myAddress = address;
    myConnection = conn;
    myListener = listener;
  }

//--------------------------------------------------------
//  business methods.

  /**
   * Sends the message.
   */
  public void run() {
    try {
      Main.setMessage("message->creating message");
      BinaryMessage msg = (BinaryMessage)myConnection.newMessage(
          MessageConnection.BINARY_MESSAGE);
      msg.setAddress(myAddress);
      Main.setMessage("to address: " + myAddress);
      msg.setPayloadData(myPayload);
      Main.setMessage("payload: " + myPayload.length);
      Main.setMessage("about to send");
      myConnection.send(msg);
      if(myListener != null) {
        myListener.doneSending();
      }
    } catch(Exception e) {
      Main.postException(e);
    }
  }

}
