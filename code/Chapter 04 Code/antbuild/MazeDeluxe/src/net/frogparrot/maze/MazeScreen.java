//#preprocess
package net.frogparrot.maze;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.i18n.ResourceBundle;

/**
 * This class represents the main display screen/canvas of the game.
 * 
 * @author Carol Hamer
 */
public class MazeScreen extends MainScreen implements MazeResource {

  //---------------------------------------------------------
  //   static fields

  /**
   * an input code (chosen to match the MIDP GameAction codes).
   */
  public static final int UP = 1;

  /**
   * an input code (chosen to match the MIDP GameAction codes).
   */
  public static final int DOWN = 6;

  /**
   * an input code (chosen to match the MIDP GameAction codes).
   */
  public static final int LEFT = 2;

  /**
   * an input code (chosen to match the MIDP GameAction codes).
   */
  public static final int RIGHT = 5;

  /**
   * This bundle contains all of the texts, translated by locale.
   */
  private static ResourceBundle myLabels = 
      ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

  /**
   * The single instance of this class.
   */
  private static MazeScreen theInstance;

  //---------------------------------------------------------
  //   instance fields
  
  /**
   * a graphics area.
   */
  private Bitmap myBitmap0;

  /**
   * alternate graphics area.
   */
  private Bitmap myBitmap1;
  
  /**
   * Which bitmap is currently displayed.
   */
  private Bitmap myCurrentBitmap;

  /**
   * The playing field.
   */
  private BitmapField myBitmapField;
  
  /**
   * The "new maze" menu item.
   */
  private MenuItem myNewMazeItem = new MenuItem(getLabel(MAZE_NEWMAZE), 0, 0) {
        public void run() {
           MazeGame.getInstance().newMaze();
        }
      };

  /**
   * The "size selection" menu item.
   */
  private MenuItem mySelectSizeItem = new MenuItem(getLabel(MAZE_SELECTSIZE), 0, 0) {
        public void run() {
            Main.getInstance().pushScreen(new SelectScreen());
        }
      };

  //-----------------------------------------------------
  //    gets / sets

  /**
   * @return the singleton instance of this class.
   */
  public static MazeScreen getInstance() {
    if(theInstance == null) {
      theInstance = new MazeScreen();
    }
    return theInstance;
  }

  /**
   * Delete the singleton instance of this class.
   * (For end-of-game cleanup only)
   */
  static void clearInstance() {
    theInstance = null;
  }

  /**
   * @return a label from the resource bundle resources.
   */
  public static String getLabel(int key){
    return myLabels.getString(key);
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Create the game and graphics data.
   */
  private MazeScreen() {
    super();

    // prepare the game logic for this screen size:
    // (the non-deprecated versions of these methods require permission)
    int screenWidth = Graphics.getScreenWidth();
    int screenHeight = Graphics.getScreenHeight();
    MazeGame.setInstance(new MazeGame(screenWidth, screenHeight));
    
    // Create two buffers to draw the graphics into:
    // While one is displayed, the other is painted in
    // the background before being flushed to the screen
    myBitmap0 = new Bitmap(screenWidth, screenHeight);
    myBitmap1 = new Bitmap(screenWidth, screenHeight);
    // Wrap the data buffer in a Field object that 
    // can be added to this Screen:
    myBitmapField = new BitmapField(myBitmap0);
    add(myBitmapField);
    // paint the initial maze onto the screen:
    paintMaze();
    // One more time to be sure that the second
    // bitmap is initialized:
    paintMaze();
  }
  
  /**
   * Override the screen's menu creation method 
   * to add the custom commands.
   */
  protected void makeMenu(Menu menu, int instance) {
    menu.add(myNewMazeItem);
    menu.add(mySelectSizeItem);
    // separate the custom menu items from 
    // the default menu items:
    menu.addSeparator();
    super.makeMenu(menu, instance);
  }

  /**
   * The platform calls this when the screen is popped
   * off the screen stack (triggered by commands that
   * are added by default).
   */
  public boolean onClose() {
    Dialog.alert(getLabel(MAZE_GOODBYE));
    Main.getInstance().terminate();
    return true;
  }

  /**
   * Draw a new maze when the size is changed.
   */
  protected void onExposed() {
    MazeGame.getInstance().doneResize();
  }

  //-------------------------------------------------------
  //  graphics and game actions

  /**
   * Paint the screen with the current playing board.
   */
  void paintMaze() {
    Bitmap boardBitmap = getNextBitmap();
    Graphics g = new Graphics(boardBitmap);
    // Here you could optimize by just pushing the region 
    // around the player:
    g.pushRegion(new XYRect(0, 0, g.getScreenWidth(), g.getScreenHeight()));
    g.clear();
    // The game logic takes over deciding precisely what to paint:
    MazeGame.getInstance().drawMaze(g);
    
    // since the region was pushed onto the context stack,
    // it must be popped off:
    g.popContext();
    // set the newly-painted bitmap to be visible:
    myBitmapField.setBitmap(boardBitmap);
  }
  
  /**
   * This method keeps track of which buffer is visible
   * and which is being painted.
   * @return the data buffer to paint into
   */
  private Bitmap getNextBitmap() {
    if(myCurrentBitmap == myBitmap1) {
      myCurrentBitmap = myBitmap0;
    } else {
      myCurrentBitmap = myBitmap1;
    }
    return myCurrentBitmap;
  }

  /**
   * The BB platform calls this method to notify
   * the screen of user keyboard input.
   * @return true if the application used the input
   *         false to pass it along
   */
  public boolean keyChar(char key, int status, int time) {
    boolean gameOver = false;
    MazeGame game = MazeGame.getInstance();
    // Map the characters to the MIDP game action codes
    // for compatibility.  The first character (eg. 's')
    // corresponds to a QWERTY keyboard, the second (eg. 'd')
    // is for the older models with just a number pad.
    if((key == 's') || (key == 'd')) { //left
      gameOver = game.move(LEFT);
    } else if((key == 'f') || (key == 'j')) { // right
      gameOver = game.move(RIGHT);
    } else if((key == 'e') || (key == 't')) { // up
      gameOver = game.move(UP);
    } else if((key == 'x') || (key == 'b')) { // down
      gameOver = game.move(DOWN);
    } else {
      // the keystroke was not relevant to this game
      return false;
    }
    // congratulate the player for winning:
    if(gameOver) {
      Dialog.alert(getLabel(MAZE_CONGRATS));
      game.newMaze();
    }
    // the keystroke was used by this game
    return true;
  }

//#ifndef RIM_4.1.0
  /**
   * The BB platform calls this method to notify
   * the screen of user keyboard input.
   * @return true if the application used the input
   *         false to pass it along
   */
  public boolean navigationMovement(int dx,
                                     int dy,
                                     int status,
                                     int time) {
    boolean gameOver = false;
    MazeGame game = MazeGame.getInstance();
    // Map the movement to the MIDP game action codes
    // for compatibility:
    if(dx < 0) { //left
      gameOver = game.move(LEFT);
    } else if(dx > 0) { // right
      gameOver = game.move(RIGHT);
    } else if(dy > 0) { // down
        gameOver = game.move(DOWN);
    } else if(dy < 0) { // up
        gameOver = game.move(UP);
    } else {
      // the motion was not relevant to this game
      return false;
    }
    // congratulate the player for winning:
    if(gameOver) {
      Dialog.alert(getLabel(MAZE_CONGRATS));
      game.newMaze();
    }
    // the motion was used by this game
    return true;
  }
//#endif

}
