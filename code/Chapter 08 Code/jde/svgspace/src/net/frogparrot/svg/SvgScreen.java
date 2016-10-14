package net.frogparrot.svg;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.system.Display;

import net.frogparrot.svggame.SvgController;
import net.frogparrot.svgmovie.SvgFieldAnimator;

import net.frogparrot.ui.FieldAnimator;

/**
 * A Simple screen to paint the SVG opening animation and 
 * the SVG game on.
 * 
 * @author Carol Hamer
 */
public class SvgScreen extends FullScreen {
  
//---------------------------------------------------------
//   fields

  /**
   * The opening animation runner.
   */
  FieldAnimator myMovie;
  
  /**
   * The game runner.
   */
  FieldAnimator myGame;
  
  /**
   * Whichever animator is current.
   */
  FieldAnimator myFieldAnimator;
  
//---------------------------------------------------------
//   data initialization and cleanup

  /**
   * Initialize the screen.
   */
  SvgScreen() {
    super();
    try {
      // these are deprecated, but have the advantage that they 
      // don't require signatures...
      //int screenWidth = Graphics.getScreenWidth();
      //int screenHeight = Graphics.getScreenHeight();
      int screenWidth = Display.getWidth();
      int screenHeight = Display.getHeight();
      myMovie = new SvgFieldAnimator(screenWidth, screenHeight);
      myFieldAnimator = myMovie;
      add(myMovie.getField());
      myMovie.play();
    } catch(Exception e) {
      System.out.println(e);
    }
  }
  
  /**
   * This is called by the BlackBerry platform when
   * the screen is popped off the screen stack.
   */
  public boolean onClose() {
    if(myMovie != null) {
      myMovie.stop();
      myMovie = null;
      int screenWidth = Graphics.getScreenWidth();
      int screenHeight = Graphics.getScreenHeight();
      myGame = new SvgController(screenWidth, screenHeight);
      deleteAll();
      add(myGame.getField());
      myFieldAnimator = myGame;
      myGame.play();
      // the screen is not removed:
      return false;
    } else if(myGame != null) {
      myGame.stop();
      myGame = null;
      myFieldAnimator = null;
    }
    // end the program.
    Main.quit();
    // confirm that the screen has been removed:
    return true;
  }
  
  /**
   * This is to end the game if the game is sent to the 
   * background for any reason (such as red key or green key).
   * If the game has more than one screen, use keyDown()
   * instead, as illustrated in the Swinging Saber game.
   */
  protected void onObscured() {
    if(myMovie != null) {
      myMovie.stop();
      myMovie = null;
    }
    if(myGame != null) {
      myGame.stop();
      myGame = null;
    }
    myFieldAnimator = null;
    // end the program.
    Main.quit();
  }
  
//---------------------------------------------------------
//   user input

  /**
   * Override keyChar to direct key input to the animator.
   * @return Whether the key input was used.
   */
  public boolean keyChar(char key, int status, int time) {
    return myFieldAnimator.keyChar(key, status, time);
  }  
    
 /**
  * Override navigationMovement to direct key input to the animator.
  * @see net.rim.device.api.ui.Screen#navigationMovement(int, int, int, int)
  */
  public boolean navigationMovement(int dx, int dy, int status, int time) {
    return myFieldAnimator.navigationMovement(dx, dy, status, time);
  }

}

