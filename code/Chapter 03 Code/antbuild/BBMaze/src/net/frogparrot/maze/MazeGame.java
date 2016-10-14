package net.frogparrot.maze;

import net.rim.device.api.ui.Graphics;

/**
 * This class controls the game logic.
 * 
 * @author Carol Hamer
 */
public class MazeGame {

  //---------------------------------------------------------
  //   static fields

  /**
   * a color constant.
   */
  public static final int BLACK = 0;
  
  /**
   * a color constant.
   */
  public static final int WHITE = 0xffffff;
  
  /**
   * a color constant.
   */
  public static final int GRAY = 0x888888;
  
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
   * maze dimension: the width of the maze walls.
   */
  private int mySquareSize;

  /**
   * maze dimension: the maximum width possible for the maze walls.
   */
  private int myMaxSquareSize;

  /**
   * maze dimension: the minimum width possible for the maze walls.
   */
  private int myMinSquareSize;

  /**
   * top corner of the display: x-coordiate
   */
  private int myStartX = 0;

  /**
   * top corner of the display: y-coordinate
   */
  private int myStartY = 0;

  /**
   * how many rows the display is divided into.
   */
  private int myGridHeight;

  /**
   * how many columns the display is divided into.
   */
  private int myGridWidth;

  /**
   * the maximum number columns the display can be divided into.
   */
  private int myMaxGridWidth;

  /**
   * the minimum number columns the display can be divided into.
   */
  private int myMinGridWidth;

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
  public int setColWidth(int colWidth) {
    if(mySquareSize != colWidth) {
      clearGrid();
      mySquareSize = colWidth;
      PrefsStorage.setSquareSize(mySquareSize);
      myGridWidth = myScreenWidth / mySquareSize;
      // only odd values are valid for the 
      // number of rows and columns:
      if((myGridWidth & 0x1) == 0) {
        myGridWidth -= 1;
      }
      myGridHeight = myScreenHeight / mySquareSize;
      if((myGridHeight & 0x1) == 0) {
        myGridHeight -= 1;
      }
      // Center the maze in the screen:
      myStartX = (myScreenWidth - (myGridWidth*mySquareSize))/2;
      myStartY = (myScreenHeight - (myGridHeight*mySquareSize))/2;
    }
    return(myGridWidth);
  }

  /**
   * @return the minimum width possible for the maze walls.
   */
  public int getMinColWidth() {
    return(myMinSquareSize);
  }

  /**
   * @return the maximum width possible for the maze walls.
   */
  public int getMaxColWidth() {
    return(myMaxSquareSize);
  }

  /**
   * @return the minimum number of columns the display can be divided into.
   */
  public int getMinNumCols() {
    return(myMinGridWidth);
  }

  /**
   * @return the maximum number of columns the display can be divided into.
   */
  public int getMaxNumCols() {
    return(myMaxGridWidth);
  }

  /**
   * @return the width of the maze walls.
   */
  public int getColWidth() {
    return(mySquareSize);
  }

  /**
   * @return the number of maze columns the display is divided into.
   */
  public int getNumCols() {
    return(myGridWidth);
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
    mySquareSize = PrefsStorage.getSquareSize();
    myMinSquareSize = 5;
    myMaxGridWidth = myScreenWidth / myMinSquareSize;
    if((myMaxGridWidth & 0x1) == 0) {
      myMaxGridWidth -= 1;
    }
    myGridWidth = myScreenWidth / mySquareSize;
    if((myGridWidth & 0x1) == 0) {
      myGridWidth -= 1;
    }
    myGridHeight = myScreenHeight / mySquareSize;
    if((myGridHeight & 0x1) == 0) {
      myGridHeight -= 1;
    }
    myMinGridWidth = 15;
    myMaxSquareSize = myScreenWidth / myMinGridWidth;
    if(myMaxSquareSize > myScreenHeight / myMinGridWidth) {
      myMaxSquareSize = myScreenHeight / myMinGridWidth;
    }
    // Center the maze in the screen:
    myStartX = (myScreenWidth - (myGridWidth*mySquareSize))/2;
    myStartY = (myScreenHeight - (myGridHeight*mySquareSize))/2;
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
    if(myGrid == null) {
      // create the underlying data of the maze.
      myGrid = new Grid(myGridWidth, myGridHeight);
    }
    // draw the maze:
    // loop through the grid data and color each square the 
    // right color
    for(int i = 0; i < myGridWidth; i++) {
        for(int j = 0; j < myGridHeight; j++) {
          if(myGrid.isWall(i, j)) {
            g.setColor(BLACK);
          } else {
            g.setColor(WHITE);
          }
          // fill the square with the appropriate color
          g.fillRect(myStartX + (i*mySquareSize), 
                     myStartY + (j*mySquareSize), 
                     mySquareSize, mySquareSize);
        }
    }
    // fill the extra space outside of the maze
    g.setColor(BLACK);
    g.fillRect(myStartX + ((myGridWidth) * mySquareSize), 
                 myStartY, myScreenWidth, myScreenHeight);
    g.fillRect(myStartX, 
                 myStartY + ((myGridHeight) * mySquareSize), 
                 myScreenWidth, myScreenHeight);
    g.fillRect(0, 0, myStartX, myScreenHeight);
    g.fillRect(0, 0, myScreenWidth, myStartY);
    // erase the exit path: 
    g.setColor(WHITE);
    g.fillRect(myStartX + ((myGridWidth) * mySquareSize), 
                 myStartY + ((myGridHeight-2) * mySquareSize), 
                 mySquareSize + myStartX, mySquareSize);

    // draw the player in gray: 
    g.setColor(GRAY);
    g.fillRoundRect(myStartX + (mySquareSize)*myPlayerX, 
                    myStartY + (mySquareSize)*myPlayerY, 
                    mySquareSize, mySquareSize, 
                    mySquareSize, mySquareSize);
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
          if(!myGrid.isWall(myPlayerX-1, myPlayerY) && 
             (myPlayerX != 1)) {
            myPlayerX -= 2;
            screen.paintMaze();
          }
          break;
        case MazeScreen.RIGHT:
          if(!myGrid.isWall(myPlayerX+1, myPlayerY)) {
            myPlayerX += 2;
            if(myPlayerX == myGridWidth) {
              myGameOver = true;
            }
            screen.paintMaze();
          }
          break;
        case MazeScreen.UP:
          if(!myGrid.isWall(myPlayerX, myPlayerY-1)) {
            myPlayerY -= 2;
            screen.paintMaze();
          }
          break;
        case MazeScreen.DOWN:
          if(!myGrid.isWall(myPlayerX, myPlayerY+1)) {
            myPlayerY += 2;
            screen.paintMaze();
          }
          break;
        default:
          break;
      }
    }
    return myGameOver;
  }

} 
