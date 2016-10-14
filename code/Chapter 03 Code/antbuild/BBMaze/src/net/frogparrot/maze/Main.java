package net.frogparrot.maze;

import net.rim.device.api.ui.UiApplication;

/**
 * The lifecycle class.
 *
 * @author Carol Hamer
 */
public class Main extends UiApplication {
  
  //---------------------------------------------------------
  //   static fields
  
  /**
   * The single instance of this application.
   */
  static Main theInstance;
  
  //---------------------------------------------------------
  //   accessors
  
  /**
   * @return The single instance of this application.
   */
  public static Main getInstance() {
    return theInstance;
  }

  //---------------------------------------------------------
  //   lifecycle methods
  
  /**
   * The BlackBerry platform calls this when it launches
   * the application.
   */
  public static void main(String[] args) {
    try {
      theInstance = new Main();
      // create the application's main screen and 
      // push it onto the top of the screen stack:
      theInstance.pushScreen(MazeScreen.getInstance());
      // Set this thread to notify this application for
      // events such as user input.
      theInstance.enterEventDispatcher();  
    } catch(Exception e) {
      System.out.println("main caught: " + e);
      e.printStackTrace();
    }
  }
  
  /**
   * Exit the game.
   */
  void terminate() {
    // cleanup:
    MazeGame.setInstance(null);
    MazeScreen.clearInstance();
    System.gc();
    // You must actively end the program,
    // just popping the base screen off the
    // stack isn't sufficient.
    System.exit(0);
  }

}

