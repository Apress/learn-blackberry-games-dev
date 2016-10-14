package net.frogparrot.hello;

import net.rim.device.api.ui.*;

/**
 * A simple Hello World example for BlackBerry.
 */
public class HelloBlackBerry extends UiApplication {

  /**
   * The RIMlet starts with main, just like an
   * ordinary Java app!
   */
  public static void main(String[] args) {
    HelloBlackBerry helloBB = new HelloBlackBerry();
    // Create a screen to write on and push it 
    // to the top of the screen stack.
    helloBB.pushScreen(new HelloWorldScreen());
    // Set the current thread to notify this application
    // of events such as user input.
    helloBB.enterEventDispatcher();
  }

}

