package net.frogparrot.tweetspace;

import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.MenuItem;

import net.frogparrot.ui.TwoFieldManager;

/**
 * The Screen that the game is placed on.
 * 
 * @author Carol Hamer
 */
public class SimpleScreen extends MainScreen {
  
//---------------------------------------------------------
//   fields

  /**
   * The item to change your Twitter login.
   */
  private MenuItem myLoginItem = new MenuItem(Main.theLabels.getString(Main.TWEETSPACE_UPD_LOGIN), 0, 0) {
        public void run() {
          Main.getInstance().pushLoginScreen();
        }
      };
      
//---------------------------------------------------------
//   data initialization and cleanup

  /**
   * Initialize the screen.
   */
  SimpleScreen(TwoFieldManager tfm) {
    super();
    add(tfm);
  }
  
  /**
   * Override the screen's menu creation method 
   * to add the custom commands.
   */
  protected void makeMenu(Menu menu, int instance) {
    menu.add(myLoginItem);
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
    // end the program.
    Main.getInstance().quit();
    // confirm that the screen has been removed:
    return true;
  }
  
}

