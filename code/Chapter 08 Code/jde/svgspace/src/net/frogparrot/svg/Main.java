package net.frogparrot.svg;

import net.rim.device.api.ui.UiApplication;

/**
 * The main lifecycle class of the Space game.
 * 
 * @author Carol Hamer
 */
public class Main extends UiApplication {

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    Main m = new Main();
    // Create a screen to write on and push it 
    // to the top of the screen stack.
    m.pushScreen(new SvgScreen());
    // Set the current thread to notify this application
    // of events such as user input.
    m.enterEventDispatcher();
  }
  
  /**
   * The exit point.
   */
  public static void quit() {
    System.exit(0);
  }

}

