package net.frogparrot.hello;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.i18n.ResourceBundle;

/**
 * The screen to draw the message on.
 */
public class HelloWorldScreen extends MainScreen implements HelloBBResResource {

//---------------------------------------------------------
//   instance fields
  
  /**
   * This bundle contains all of the texts, translated by locale.
   */
  ResourceBundle myLabels = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

  /**
   * Whether or not the screen should currently display the 
   * "hello world" message.
   */
  boolean mySayHello = false;

  /**
   * The user interface component to write hello on.
   */
  RichTextField myTextField;

  /**
   * The "toggle hello" menu item.
   */
  MenuItem myToggleHelloItem = new MenuItem(this.myLabels.getString(HELLOBB_TOGGLE), 0, 0) {
        public void run() {
           toggleHello();
        }
      };

//-----------------------------------------------------
//    initialization and state changes

  /**
   * Write "Hello World!" on the screen.
   */
  HelloWorldScreen() {
    super();
    myTextField = new RichTextField(myLabels.getString(HELLOBB_SAYHELLO), 
        RichTextField.NON_FOCUSABLE | RichTextField.READONLY);
    add(myTextField);
  }

  /**
   * Override the screen's menu creation method 
   * to add the custom commands.
   */
  protected void makeMenu(Menu menu, int instance) {
    menu.add(myToggleHelloItem);
    // separate the custom menu items from 
    // the default menu items:
    menu.addSeparator();
    super.makeMenu(menu, instance);
  }

  /**
   * Remove or replace the "Hello World!" message.
   */
  void toggleHello() {
    if(mySayHello) {
      myTextField.setText(myLabels.getString(HELLOBB_SAYHELLO));
      mySayHello = false;
    } else {
      myTextField.setText("");
      mySayHello = true;
    }
  }

  /**
   * This is called by the BlackBerry platform when
   * the screen is popped off the screen stack.
   */
  public boolean onClose() {
    // end the program.
    System.exit(0);
    // confirm that the screen is closing.
    return true;
  }

}

