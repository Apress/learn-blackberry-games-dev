package net.frogparrot.smscheckers;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import net.rim.device.api.system.EventLogger;
import java.util.Vector;

import net.frogparrot.net.*;

/**
 * This class controls the lifecycle of the MIDlet.
 */
public class Main extends MIDlet implements CommandListener, ContactListener {
    
//----------------------------------------------------------------
//  static fields

  /**
   * The sms port to listen on by default.
   */
  public static final String DEFAULT_PUSH_PORT = "16027";

  /**
   * Only one instance of this class should exist.
   */
  static Main theInstance;

  /**
   * The Screen displays messages to the user.
   */
  MainScreen myScreen;

  /**
   * The playing field.
   */
  CheckersCanvas myCanvas;

  /**
   * The text field where the player can add a message
   * for the opponent.
   */
  TextBox myTauntBox;

//----------------------------------------------------------------
//  instance fields

  /**
   * The menu item to close this application.
   */
  private Command myExitCommand = new Command("end game", Command.EXIT, 99);

  /**
   * The menu item to confirm the message.
   */
  private Command myOkCommand = new Command("OK", Command.OK, 1);

  /**
   * The menu item to add a message for the remote player.
   */
  private Command myTauntCommand = new Command("add message", Command.OK, 2);

  /**
   * The menu item for the taunt box, to remove it.
   */
  private Command myBackCommand = new Command("back to game", Command.OK, 2);

//----------------------------------------------------------------
//  initialization and accessors

  /**
   * Initialize the Screen and the commands.
   */
  public Main() {
    try {
      // each BlackBerry application that wants to log 
      // messages must register with a unique ID:
      EventLogger.register(0x62e74ebe294681cL, "smscheckers", 
          EventLogger.VIEWER_STRING);
      // initialize the instance data:
      theInstance = this;
      myScreen = new MainScreen();
      myScreen.addCommand(myExitCommand);
      myScreen.addCommand(myOkCommand);
      myScreen.setCommandListener(this);
      myCanvas = new CheckersCanvas();
      myCanvas.addCommand(myExitCommand);
      myCanvas.addCommand(myTauntCommand);
      myCanvas.setCommandListener(this);
      myTauntBox = new TextBox("message for opponent", 
          "", 50, TextField.ANY);
      myTauntBox.addCommand(myExitCommand);
      myTauntBox.addCommand(myBackCommand);
      myTauntBox.setCommandListener(this);
    } catch(Exception e) {
      postException(e);
    }
  }
  
/**
 * Return the associated game logic.
 */
 public static CheckersGame getGame() {
   return theInstance.myCanvas.getGame();
 }

//----------------------------------------------------------------
//  implementation of MIDlet

  /**
   * The AMS calls this method to start the application.
   */
  public void startApp() throws MIDletStateChangeException {
    try {
      // display my Screen on the screen:
      Display.getDisplay(this).setCurrent(myScreen);
      // this application starts by listening for a message:
      startSms();
    } catch(Exception e) {
      postException(e);
    }
  }
  
  /**
   * If the MIDlet was using resources, it should release 
   * them in this method.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    SMSManager.cleanup();
    System.gc();
  }
  
  /**
   * This closes the game.
   */
  public static void quit() {
    // The MIDlet calls these two methods to exit:
    try {
      theInstance.destroyApp(false);
      theInstance.notifyDestroyed();
    } catch (Exception e) {
    }
  }

  /**
   * If the game is sent to the background, just exit.
   */
  public void pauseApp() {
    quit();
  }
  
  /**
   * Place the game board on the screen.
   */
  public static void showGameBoard() {
    try {
      Display.getDisplay(theInstance).setCurrent(theInstance.myCanvas);
      theInstance.myCanvas.repaint();
    } catch(Exception e) {
      postException(e);
    }
  }

  /**
   * Place the message display on the screen.
   */
  public static void showMessageScreen() {
    try {
      Display.getDisplay(theInstance).setCurrent(theInstance.myScreen);
    } catch(Exception e) {
      postException(e);
    }
  }
  
  /**
   * display the message for the user.
   */
  public static void displayMessage(String message) {
    theInstance.myScreen.setMessage(message);
    showMessageScreen();
  }

//----------------------------------------------------------------
//  implementation of CommandListener

  /*
   * The AMS calls this method to notify the MIDlet of user 
   * command input.  This class is listening for command
   * input on all of its instance screens.
   */
  public void commandAction(Command c, Displayable s) {
    if(c == myOkCommand) {
      setMessage("command OK");
      myCanvas.getGame().ok();
    } else if(c == myTauntCommand) {
      Display.getDisplay(this).setCurrent(myTauntBox);
    } else if(c == myBackCommand) {
      Display.getDisplay(this).setCurrent(myCanvas);
    } else if(c == myExitCommand) {
      // if the game is not done, we first 
      // inform the other player before exiting:
      if(myCanvas.getGame().gameDone()) {
        quit();
      }
    }
  }
  
//----------------------------------------------------------------
//  debug logging utilities
  
  /**
   * A utility to log debug messages.
   */
  public static void setMessage(String message) {
    EventLogger.logEvent(0x62e74ebe294681cL, message.getBytes());
    System.out.println(message);
    //theInstance.myScreen.setMessage(message);
  }

  /**
   * A utility to log exceptions.
   */
  public static void postException(Exception e) {
    System.out.println(e);
    e.printStackTrace();
    String exceptionName = e.getClass().getName();
    EventLogger.logEvent(0x62e74ebe294681cL, exceptionName.getBytes());
    if(e.getMessage() != null) {
      EventLogger.logEvent(0x62e74ebe294681cL, e.getMessage().getBytes());
    }
    theInstance.myScreen.setMessage(exceptionName);
  }

//----------------------------------------------------------------
//  SMS initialization

  /**
   * This game starts by listening for incoming SMS messages.
   * The MIDlet checks which push-port is registered in the Jad
   * file, and passes this information to the SMSManager so that
   * the SMSManager will know to contact the opponent on the 
   * same port.
   */
  private void startSms() {
    try {
      String pushInfo = getAppProperty("MIDlet-Push-1").trim();
      if(pushInfo.startsWith("sms://:")) {
        int comma = pushInfo.indexOf(',');
        String portNum = pushInfo.substring(7, comma);
        setMessage("listening: " + portNum);
        // Send the SmsDataListener
        // to be notified when any messages arrive:
        SMSManager.startup(portNum, myCanvas.getGame());
      } else {
        // if there's no push port property in the jad,
        // then select a port to listen on:
        setMessage("attempting to use: " + DEFAULT_PUSH_PORT);
        SMSManager.startup(DEFAULT_PUSH_PORT, myCanvas.getGame());
      }
    } catch(Exception e) {
      setMessage("attempting to use: " + DEFAULT_PUSH_PORT);
      SMSManager.startup(DEFAULT_PUSH_PORT, myCanvas.getGame());
    }
  }
  
//----------------------------------------------------------------
//  PIM (contact list) methods

  /**
   * Launch the PIM functionality to read the user's address book
   * to populate the list of possible opponents.
   */
  static void startPim() {
    PIMRunner pr = new PIMRunner(theInstance);
    setMessage("starting pim runner");
    pr.start();
  }
  
  /**
   * This is the callback method that returns the PIM contact list.
   * The next step is to display the list to the user to allow the
   * user to select an opponent.
   */
  public void setContactList(Vector names, Vector phoneNumbers) {
    setMessage("got contacts");
    if((names == null) || (names.size() == 0)) {
      // no contacts were found, so the user will
      // enter the opponent's number manually:
      setMessage("no contacts");
      Display.getDisplay(this).setCurrent(new PhoneNumberScreen(this));
    } else {
      // select the opponent from the contact list:
      String[] choices = new String[names.size()];
      setMessage("name: " + choices[0]);
      names.copyInto(choices);
      Display.getDisplay(this).setCurrent(new PimScreen(choices, phoneNumbers, this));
    }
  }
  
  /**
   * The PimScreen or PhoneNumber screen calls this method
   * when the user is done selecting the opponent's phone number.
   */
  public void setOpponentPhoneNumber(String number) {
    //Display.getDisplay(this).setCurrent(myScreen);
    displayMessage("waiting for remote player");
    setMessage("got phone number: " + number);
    SMSManager.getInstance().checkPhoneNum(number);
    byte[] invitation = new byte[4];
    System.arraycopy(CheckersGame.INVITATION, 0, invitation, 0, 4);
    SMSManager.getInstance().sendMessage(invitation);
  }
  
//----------------------------------------------------------------
//  taunt methods

  /**
   * Return and clear the message in the taunt box.
   */
  public static String getTaunt() {
    String retString = theInstance.myTauntBox.getString();
    theInstance.myTauntBox.setString("");
    return retString;
  }

}



