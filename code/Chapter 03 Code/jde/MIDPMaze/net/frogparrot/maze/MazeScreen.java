package net.frogparrot.maze;

import javax.microedition.lcdui.*;

/**
 * This class represents the main display screen/canvas of the game.
 * 
 * @author Carol Hamer
 */
public class MazeScreen extends javax.microedition.lcdui.Canvas {

  //---------------------------------------------------------
  //   static fields

  /**
   * The single instance of this class.
   */
  private static MazeScreen theInstance;

  //---------------------------------------------------------
  //   instance fields

  /**
   * a handle to the display.
   */
  private Display myDisplay;

  //-----------------------------------------------------
  //    gets / sets

  /**
   * @return the singleton instance of this class.
   */
  public static MazeScreen getInstance() {
    return theInstance;
  }

  /**
   * Delete the singleton instance of this class.
   * (For end-of-game cleanup only)
   */
  static void clearInstance() {
    theInstance = null;
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Create the game and graphics data.
   */
  public MazeScreen(Display d) throws Exception {
    myDisplay = d;
    // prepare the game logic for this screen size:
    int width = getWidth();
    int height = getHeight();

    MazeGame.setInstance(new MazeGame(width, height));
    theInstance = this;
  }

  /**
   * discard the current maze and draw a new one.
   */
  void newMaze() {
    MazeGame.getInstance().newMaze();
    paintMaze();
  }
  //-------------------------------------------------------
  //  graphics and game actions
  
  /**
   * Set the canvas to be visible and paint it.
   */
  public void paintMaze() {
    myDisplay.setCurrent(this);
    repaint();
  }

  /**
   * This overrides Canvas#paint, and is called by 
   * the platform after a repaint() request.
   */
  protected void paint(Graphics g) {
    MazeGame.getInstance().drawMaze(g);
  }

  /**
   * Move the player.
   */
  public void keyPressed(int keyCode) {  
    int action = getGameAction(keyCode);
    boolean gameOver = MazeGame.getInstance().move(action);
    if(gameOver) {
       // create a new maze that will
       // appear once the alert is dismissed:
       MazeGame.getInstance().newMaze();
       // create the alert with the default 
       // alert behavior: a default dismiss
       // command with a displayable to display
       // after the alert is dismissed
       myDisplay.setCurrent(new Alert("Done", "Great Job!",
           null, AlertType.INFO), this);
    }
  }

}
