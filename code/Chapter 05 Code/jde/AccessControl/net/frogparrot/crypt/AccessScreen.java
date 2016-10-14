package net.frogparrot.crypt;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

/**
 * The screen to draw the message on, to inform the user whether or 
 * not the application has been validated with the key.
 * 
 * @author Carol Hamer
 */
public class AccessScreen extends MainScreen {

//----------------------------------------------------------------
//  instance fields

  /**
   * A handle to the main class of the application.
   */
  AccessControl myMain;
          
  /**
   * The "OK" menu item that checks whether the String that the 
   * user entered is a valid license key for this device.
   */
  MenuItem myOK = new MenuItem("OK", 0, 0) {
      // This is called when the user selects the OK menu item:
      public void run() {
        Thread t = new Thread(myMain);
        myMain.run();
      }
    };
  
  /**
   * The input field where the user types in the license key.
   */
  EditField myEnterLicenseField = new EditField( 
          EditField.JUMP_FOCUS_AT_END | EditField.NO_NEWLINE 
          | RichTextField.NO_LEARNING | RichTextField.NO_COMPLEX_INPUT
          | RichTextField.FOCUSABLE | RichTextField.EDITABLE);


//----------------------------------------------------------------
//  initialization and lifecycle

  /**
   * @return the text the user has entered.
   */
  String getUserText() {
    return myEnterLicenseField.getText();
  }

  /**
   * The constructor initializes the data.
   */
  AccessScreen(AccessControl ac) {
    super();
    myMain = ac;
    //AccessControl.setMessage("in AccessScreen");
  }
  
  /**
   * Once the application has decided whether the user has 
   * entered a valid license key, this method fills the screen
   * with the corresponding items: either a message that the 
   * key has been validated or an editable field that the
   * user can type the key into.
   */
  public void setup() {
    //AccessControl.setMessage("in setup");
    deleteAll();
    //AccessControl.setMessage("myMain.isValid(): " + myMain.isValid());
    if(myMain.isValid()) {
      add(new RichTextField("code valid", 
          RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
    } else {
      add(new RichTextField("enter code", 
          RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
      add(myEnterLicenseField);
    }
    //AccessControl.setMessage("AccessScreen-->done constructing");
  }
  
  /**
   * Override the screen's menu creation method 
   * to add the custom command.
   */
  protected void makeMenu(Menu menu, int instance) {
    AccessControl.setMessage("makeMenu");
    menu.add(myOK);
    // separate the custom menu items from 
    // the default menu items:
    menu.addSeparator();
    super.makeMenu(menu, instance);
  }

  /**
   * This is called by the BlackBerry platform when
   * the screen is popped off the screen stack.
   */
  public boolean onClose() {
    // end the program:
    System.exit(0);
    // confirm that the screen has been removed:
    return true;
  }

}

