package net.frogparrot.smscheckers;

import javax.microedition.lcdui.*;

/**
 * This is a screen that allows the user to enter a phone number
 * in the case where no contacts are found in the address book.
 */
public class PhoneNumberScreen extends TextBox implements CommandListener {
    
  /**
   * Once the user has selected a number, the Main class is called back.
   */
   private Main myListener;

  /**
   * The menu command that indicates that the user is done entering the
   * opponent's number and is ready to play.
   */
   private Command mySelectCommand = new Command("Send invitation", Command.SCREEN, 1);
    
  /**
   * The menu command that indicates that the user is waiting for an invitation
   * from another opponent.
   */
   private Command myWaitCommand = new Command("Wait for invitation", Command.SCREEN, 1);
    
  /**
   * The menu item to close this application.
   */
   private Command myExitCommand = new Command("End Game", Command.EXIT, 99);

  /**
   * Build the screen.
   */
   PhoneNumberScreen(Main listener) {
     super("Enter number or wait", "", 50, TextField.PHONENUMBER);
     addCommand(mySelectCommand);
     addCommand(myWaitCommand);
     addCommand(myExitCommand);
     setCommandListener(this);
     myListener = listener;
   }
    
  /**
   * User is done, call back with the response.
   */
  public void commandAction(Command c, Displayable s) {
    if(c == mySelectCommand) {
      Main.setMessage("select command");
      myListener.setOpponentPhoneNumber(getString());
    } else if(c == myWaitCommand) {
      Main.displayMessage("Waiting for an opponent");
      Main.getGame().setWaiting();
    } else if(c == myExitCommand) {
      Main.quit();
    }
  }

}
