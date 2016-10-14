package net.frogparrot.smscheckers;

import javax.microedition.lcdui.*;

/**
 * This class displays the information for the user.
 */
public class MainScreen extends TextBox {
    
//---------------------------------------------------------
//   fields

 /**
  * The disclaimer message.
  */
 public static final String WARNING 
         = "\n\n* WARNING *\nThis game sends each move in an SMS message. "
         + "Your operator will charge you for one text message for each move.";

  /**
   * The singleton instance.
   */
  static MainScreen theInstance;

//-----------------------------------------------------
//    initialization and accessors
  
  /**
   * Set this as the singleton instance.
   */
  public MainScreen() {
    super("Checkers", "please wait" + WARNING, 500, TextField.UNEDITABLE);
    theInstance = this;
  }
  
  /**
   * Set the message to display.
   */
  public static void setMessage(String message) {
    theInstance.setString(message + WARNING);
  }

}


