//#preprocess
package net.frogparrot.maze;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.system.Bitmap;

/**
 * This class controls the game logic.
 *
 * @author Carol Hamer
 */
public class MazeGame {

  //---------------------------------------------------------
  //   static fields

  /**
   * color constants.
   */
  public static final int BLACK = 0;
  
  public static final int WHITE = 0xffffff;
  
  public static final int WALL_GRAY = 0x00808080;
  
  public static final int HIGHLIGHT_GRAY = 0x00cccccc;
  
  public static final int SHADOW_GRAY = 0x00303030;
  
  public static final int WALL_BACKGROUND = 0x00efcc8a;
  
  public static final int HIGHLIGHT_BACKGROUND = 0x00efcc8a;
  
  public static final int SHADOW_BACKGROUND = 0x006f4d0b;
  
  /**
   * Image orientation constants.
   */
  public static final int IMAGE_RIGHT = 0;

  public static final int IMAGE_DOWN = 1;

  public static final int IMAGE_LEFT = 2;

  public static final int IMAGE_UP = 3;

  /**
   * maze dimension: the possible width values of the maze walls.
   */
//#ifdef SCREEN_240x160
  static int[] mySquareSizes = { 8, 12, 16 };
  public static final int NUM_SIZES = 3;
//#endif
//#ifdef SCREEN_240x260
  static int[] mySquareSizes = { 8, 12, 16 };
  public static final int NUM_SIZES = 3;
//#endif
//#ifdef SCREEN_320x240
  static int[] mySquareSizes = { 8, 12, 16 };
  public static final int NUM_SIZES = 3;
//#endif
//#ifdef SCREEN_480x360
  static int[] mySquareSizes = { 8, 12, 16, 24 };
  public static final int NUM_SIZES = 4;
//#endif

  /**
   * The single instance of this class.
   */
  private static MazeGame theInstance;

  //---------------------------------------------------------
  //   instance fields

  /**
   * The data object that describes the maze configuration.
   */
  private Grid myGrid;

  /**
   * Whether or not the currently displayed maze has 
   * been completed.
   */
  private boolean myGameOver = false;

  /**
   * maze dimension: the width of the screen.
   */
  private int myScreenWidth;

  /**
   * maze dimension: the height of the screen.
   */
  private int myScreenHeight;

  /**
   * top corner of the playing area: x-coordiate
   */
  private int myStartX = 0;

  /**
   * top corner of the playing area: y-coordinate
   */
  private int myStartY = 0;

  /**
   * maze dimension: the index to the current width of the maze walls.
   */
  int mySquareSizeIndex;

  /**
   * the possible numbers of columns the display can be divided into.
   */
  private int[] myGridWidths = new int[NUM_SIZES];

  /**
   * the possible numbers of rows the display can be divided into.
   */
  private int[] myGridHeights = new int[NUM_SIZES];

  /**
   * current location of the player in the maze: x-coordiate
   * (in terms of the coordinates of the maze grid, NOT in terms 
   * of the coordinate system of the Canvas/Screen.)
   */
  private int myPlayerX = 1;

  /**
   * current location of the player in the maze: y-coordinate
   * (in terms of the coordinates of the maze grid, NOT in terms 
   * of the coordinate system of the Canvas/Screen.)
   */
  private int myPlayerY = 1;
  
  /**
   * The most recently selected direction (for orienting the image).
   */
  private int myPlayerDirection;
  
  /**
   * The bitmap image to use for the player.
   */
  private Bitmap myPlayerBitmap;

  //-----------------------------------------------------
  //    gets / sets
  
  /**
   * @return the single instance of this class.
   */
  public static MazeGame getInstance() {
    return theInstance;
  }

  /**
   * Set the single instance of this class.
   */
  static void setInstance(MazeGame mazeGame) {
    theInstance = mazeGame;
  }

  /**
   * Changes the width of the maze walls and calculates how 
   * this change affects the number of rows and columns 
   * the maze can have.
   * @return the number of columns now that the the 
   *         width of the columns has been updated.
   */
  public int setColWidthIndex(int colWidthIndex) {
    if(mySquareSizeIndex != colWidthIndex) {
      clearGrid();
      mySquareSizeIndex = colWidthIndex;
      PrefsStorage.setSquareSizeIndex(mySquareSizeIndex);
      // get the new player image:
      myPlayerBitmap = getPlayerBitmap();
      // Center the maze in the screen:
      myStartX = (myScreenWidth 
          - (myGridWidths[mySquareSizeIndex]*mySquareSizes[mySquareSizeIndex]))/2;
      myStartY = (myScreenHeight 
          - (myGridHeights[mySquareSizeIndex]*mySquareSizes[mySquareSizeIndex]))/2;
    }
    return(myGridWidths[mySquareSizeIndex]);
  }

  /**
   * @return the minimum square size index.
   */
  public int getMinColWidthIndex() {
    return(0);
  }

  /**
   * @return the maximum square size index.
   */
  public int getMaxColWidthIndex() {
    return(mySquareSizes.length - 1);
  }

  /**
   * @return the current square size index.
   */
  public int getColWidthIndex() {
    return(mySquareSizeIndex);
  }

  /**
   * @return the actual square size in pixels corresponding to the index.
   */
  public int getColWidth(int index) {
    return(mySquareSizes[index]);
  }

  /**
   * @return the minimum number of columns the display can be divided into.
   */
  public int getMinNumCols() {
    return(myGridWidths[myGridWidths.length - 1]);
  }

  /**
   * @return the maximum number of columns the display can be divided into.
   */
  public int getMaxNumCols() {
    return(myGridWidths[0]);
  }

  /**
   * @return the current number of maze columns the display is divided into.
   */
  public int getNumCols() {
    return(myGridWidths[mySquareSizeIndex]);
  }

  /**
   * @return the current player bitmap image.
   */
  public Bitmap getPlayerBitmap() {
    return Bitmap.getBitmapResource("player_" + 
          mySquareSizes[mySquareSizeIndex] + ".png");
  }

  //-----------------------------------------------------
  //    initialization and game state changes

  /**
   * Constructor initializes the maze dimension data.
   * @param screenWidth the width of the play area in pixels
   * @param screenHeight the height of the play area in pixels
   */
  MazeGame(int screenWidth, int screenHeight) {
    myScreenWidth = screenWidth;
    myScreenHeight = screenHeight;
    // Calculate how many rows and columns of maze
    // can (and should) be placed on the screen.
    mySquareSizeIndex = PrefsStorage.getSquareSizeIndex();
    if((mySquareSizeIndex < 0) || (mySquareSizeIndex >= mySquareSizes.length)) {
      mySquareSizeIndex = 1;
    }
    
    myPlayerBitmap = getPlayerBitmap();

    // initialize the grid dimensions corresponding 
    // to the possible square sizes:
    for(int i = 0; i < mySquareSizes.length; i++) {
      myGridWidths[i] = myScreenWidth / mySquareSizes[i];
        if((myGridWidths[i] & 0x1) == 0) {
        myGridWidths[i] -= 1;
      }
      myGridHeights[i] = myScreenHeight / mySquareSizes[i];
        if((myGridHeights[i] & 0x1) == 0) {
        myGridHeights[i] -= 1;
      }
    }
    // Center the maze in the screen:
    myStartX = (myScreenWidth 
        - (myGridWidths[mySquareSizeIndex]*mySquareSizes[mySquareSizeIndex]))/2;
    myStartY = (myScreenHeight 
        - (myGridHeights[mySquareSizeIndex]*mySquareSizes[mySquareSizeIndex]))/2;
  }
  
  /**
   * discard the current maze.
   */
  void clearGrid() {
    myGameOver = false;
    // throw away the current maze.
    myGrid = null;
    // set the player back to the beginning of the maze.
    myPlayerX = 1;
    myPlayerY = 1;
    myPlayerDirection = IMAGE_RIGHT;
  }
  
  /**
   * discard the current maze and draw a new one.
   */
  void newMaze() {
    clearGrid();
    // paint the new maze
    MazeScreen.getInstance().paintMaze();
  }
  
  /**
   * Draw a new maze when the size is changed.
   * This is used only in the RIMlet version
   * of the game (not the MIDlet version), 
   * to initialize new maze data when the BB platform 
   * notifies the MazeScreen that it has been exposed, 
   * after the SelectScreen is popped off the screen stack.
   */
  void doneResize() {
    if(myGrid == null) {
      MazeScreen.getInstance().paintMaze();
    }
  }

  //-------------------------------------------------------
  //  graphics and game actions

  /**
   * Create and display a maze if necessary, otherwise just 
   * move the player.  For simplicity we repaint the whole 
   * maze each time, but this could be optimized by painting
   * just the player and erasing the square that the player 
   * just left.
   */
  void drawMaze(Graphics g) {
    // If there is no current maze, create one and draw it.
    int gridWidth = myGridWidths[mySquareSizeIndex];
    int gridHeight = myGridHeights[mySquareSizeIndex];
    int squareSize = mySquareSizes[mySquareSizeIndex];
    if(myGrid == null) {
      // create the underlying data of the maze.
      myGrid = new Grid(gridWidth, gridHeight);
    }
    // draw the maze:
    // loop through the grid data and color each square the 
    // right color, checking the surrounding squares in
    // order to determine where to draw highlights and shadows
    for(int i = 0; i < gridWidth; i++) {
      for(int j = 0; j < gridHeight; j++) {
        if(myGrid.isWall(i, j)) {
          // draw the wall block with shadows and highlights:
          g.setColor(WALL_GRAY);
          g.fillRect(myStartX + (i*squareSize), 
                   myStartY + (j*squareSize), 
                   squareSize, squareSize);
          // start with the shadow:
          g.setColor(SHADOW_GRAY);
          if(!myGrid.isWall(i+1, j)) {
            g.drawLine(myStartX + ((i+1)*squareSize) - 1, 
                   myStartY + (j*squareSize), 
                   myStartX + ((i+1)*squareSize) - 1, 
                   squareSize + ((j+1)*squareSize));
          }
          if(!myGrid.isWall(i, j+1)) {
            g.drawLine(myStartX + (i*squareSize), 
                   myStartY + ((j+1)*squareSize - 1), 
                   myStartX + ((i+1)*squareSize), 
                   myStartY + ((j+1)*squareSize - 1));
          }
          if(!myGrid.isWall(i+1, j+1)) {
            g.drawPoint(myStartX + ((i+1)*squareSize) - 1, 
                   myStartY + ((j+1)*squareSize - 1));
          }
          // then do the highlight:
          g.setColor(HIGHLIGHT_GRAY);
          if(!myGrid.isWall(i-1, j)) {
            g.drawLine(myStartX + (i*squareSize), 
                   myStartY + (j*squareSize), 
                   myStartX + (i*squareSize), 
                   squareSize + ((j+1)*squareSize));
          }
          if(!myGrid.isWall(i, j-1)) {
            g.drawLine(myStartX + (i*squareSize), 
                   myStartY + (j*squareSize), 
                   myStartX + ((i+1)*squareSize), 
                   myStartY + (j*squareSize));
          }
          if(!myGrid.isWall(i-1, j-1)) {
            g.drawPoint(myStartX + (i*squareSize), 
                     myStartY + (j*squareSize));
          }
        } else {
          g.setColor(WHITE);
          // clear the square as white:
          g.fillRect(myStartX + (i*squareSize), 
                     myStartY + (j*squareSize), 
                     squareSize, squareSize);
        }
      }
    }
    drawBorder(g);
    // erase the exit path: 
    g.setColor(WHITE);
    g.fillRect(myStartX + (gridWidth * squareSize), 
                 myStartY + ((gridHeight-2) * squareSize), 
                 squareSize + myStartX, squareSize);

    // draw the player as an image bitmap:
    g.drawBitmap(myStartX + (squareSize)*myPlayerX, 
                 myStartY + (squareSize)*myPlayerY,
                 squareSize, squareSize,
                 myPlayerBitmap, myPlayerDirection*squareSize, 0);
  }
  
  /**
   * Draw the border area around the maze.
   */
  void drawBorder(Graphics g) {
    int gridHeight = myGridHeights[mySquareSizeIndex];
    int squareSize = mySquareSizes[mySquareSizeIndex];
    // shade in the extra space outside of the maze
    int[] xPts = { 0, 
                   myScreenWidth, 
                   myScreenWidth, 
                   myScreenWidth - myStartX, 
                   myScreenWidth - myStartX, 
                   myStartX, 
                   myStartX, 
                   0 };
    int[] yPts = { 0, 
                   0, 
                   myStartY + ((gridHeight-2) * squareSize), 
                   myStartY + ((gridHeight-2) * squareSize), 
                   myStartY, 
                   myStartY, 
                   myStartY + squareSize, 
                   myStartY + squareSize };
    int[] colors = { HIGHLIGHT_BACKGROUND, 
                     WALL_BACKGROUND, 
                     WALL_BACKGROUND, 
                     SHADOW_BACKGROUND, 
                     SHADOW_BACKGROUND, 
                     HIGHLIGHT_BACKGROUND, 
                     HIGHLIGHT_BACKGROUND, 
                     HIGHLIGHT_BACKGROUND };
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    int[] xPts2 = { 0, 
                   myScreenWidth,
                   myScreenWidth,
                   myScreenWidth - myStartX,
                   myScreenWidth - myStartX,
                   myStartX,
                   myStartX,
                   0 };
    int[] yPts2 = { myScreenHeight,
                   myScreenHeight,
                   myScreenHeight - (myStartY + squareSize),
                   myScreenHeight - (myStartY + squareSize),
                   myScreenHeight - myStartY,
                   myScreenHeight - myStartY,
                   myStartY + 2*squareSize,
                   myStartY + 2*squareSize };
    int[] colors2 = { WALL_BACKGROUND, 
                     WALL_BACKGROUND, 
                     SHADOW_BACKGROUND, 
                     SHADOW_BACKGROUND, 
                     SHADOW_BACKGROUND, 
                     WALL_BACKGROUND, 
                     HIGHLIGHT_BACKGROUND, 
                     HIGHLIGHT_BACKGROUND };
    g.drawShadedFilledPath(xPts2, yPts2, null, colors2, null);
  }
  
  /**
   * Handle user input.
   * @param direction the code to tell which direction the user selected.
   * @return whether the user exits the maze on this move.
   */
  boolean move(int direction) {  
    MazeScreen screen = MazeScreen.getInstance();
    if(! myGameOver) {
      switch(direction) {
        case MazeScreen.LEFT:
          myPlayerDirection = IMAGE_LEFT;
          if(!myGrid.isWall(myPlayerX-1, myPlayerY)
             && (myPlayerX != 1)) {
            myPlayerX -= 2;
          }
          screen.paintMaze();
          break;
        case MazeScreen.RIGHT:
          myPlayerDirection = IMAGE_RIGHT;
          if(!myGrid.isWall(myPlayerX+1, myPlayerY)) {
            myPlayerX += 2;
            // handle the player exiting the maze:
            if(myPlayerX == myGridWidths[mySquareSizeIndex]) {
              myGameOver = true;
            }
          }
          screen.paintMaze();
          break;
        case MazeScreen.UP:
          myPlayerDirection = IMAGE_UP;
          if(!myGrid.isWall(myPlayerX, myPlayerY-1)) {
            myPlayerY -= 2;
          }
          screen.paintMaze();
          break;
        case MazeScreen.DOWN:
          myPlayerDirection = IMAGE_DOWN;
          if(!myGrid.isWall(myPlayerX, myPlayerY+1)) {
            myPlayerY += 2;
          }
          screen.paintMaze();
          break;
        default:
          break;
      }
    }
    return myGameOver;
  }

} 
