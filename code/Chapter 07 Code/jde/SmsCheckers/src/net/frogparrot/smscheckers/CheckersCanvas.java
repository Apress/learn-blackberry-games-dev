package net.frogparrot.smscheckers;

import javax.microedition.lcdui.*;
import java.io.IOException;

/**
 * This class is the display of the game.
 * 
 * @author Carol Hamer
 */
public class CheckersCanvas extends Canvas {

//---------------------------------------------------------
//   graphics constants

  /**
   * color constants.
   */
  public static final int BLACK = 0;
  public static final int WHITE = 0xffffff;
  public static final int RED = 0xf96868;
  public static final int GREY = 0xc6c6c6;
  public static final int LT_GREY = 0xe5e3e3;
  public static final int TAN = 0xf9b97b;

  /**
   * how many rows and columns the display is divided into.
   */
  public static final int GRID_WIDTH = 8;

  /**
   * checkers dimensions: dimensions for different sized 
   * checkerboards and their borders.
   */
  private static int SMALL_SQUARE_SIZE = 24;
  private static int SMALL_BORDER = 6;
  private static int MED_SQUARE_SIZE = 36;
  private static int MED_BORDER = 9;
  private static int LARGE_SQUARE_SIZE = 48;
  private static int LARGE_BORDER = 12;

//---------------------------------------------------------
//   instance fields

  /**
   * a handle to the object that stores the game logic
   * and game data.
   */
  private CheckersGame myGame;
  
  /**
   * The images of the checkers.
   */
  private Image myBlackChecker;
  private Image myRedChecker;
  private Image myBlackKing;
  private Image myRedKing;

  /**
   * Checkerboard dimensions and coordinates.
   */
  private int mySquareSize;
  private int myCornerX;
  private int myCornerY;
  private int myBorderWidth;
  private int myBoardWidth;

//-----------------------------------------------------
//    gets / sets

  /**
   * @return a handle to the class that holds the logic of the 
   * checkers game.
   */
  public CheckersGame getGame() {
    return(myGame);
  }

//-----------------------------------------------------
//    initialization and game state changes

  /**
   * Constructor performs size calculations.
   */
  CheckersCanvas() throws IOException {
    Main.setMessage("in CheckersCanvas()");
    // create the game logic class:
    myGame = new CheckersGame();
    Main.setMessage("in CheckersCanvas()-->game: " + myGame);
    // a few calculations to make the right checkerboard 
    // for the current display.
    int width = getWidth();
    int height = getHeight();
    int smBoardSize = SMALL_SQUARE_SIZE*8 + SMALL_BORDER*2;
    int medBoardSize = MED_SQUARE_SIZE*8 + MED_BORDER*2;
    int lgBoardSize = LARGE_SQUARE_SIZE*8 + LARGE_BORDER*2;
    if((width < smBoardSize) || (height < smBoardSize)) {
      Main.setMessage("screen too small");
      Main.quit();
    } else if((width < medBoardSize) || (height < medBoardSize)) {
      mySquareSize = SMALL_SQUARE_SIZE;
      myBorderWidth = SMALL_BORDER;
      myBoardWidth = smBoardSize;
      myBlackChecker = Image.createImage("/img/black_checker_24.png");
      myRedChecker = Image.createImage("/img/red_checker_24.png");
      myBlackKing = Image.createImage("/img/black_king_24.png");
      myRedKing = Image.createImage("/img/red_king_24.png");
    } else if((width < lgBoardSize) || (height < lgBoardSize)) {
      mySquareSize = MED_SQUARE_SIZE;
      myBorderWidth = MED_BORDER;
      myBoardWidth = medBoardSize;
      myBlackChecker = Image.createImage("/img/black_checker_36.png");
      myRedChecker = Image.createImage("/img/red_checker_36.png");
      myBlackKing = Image.createImage("/img/black_king_36.png");
      myRedKing = Image.createImage("/img/red_king_36.png");
    } else {
      mySquareSize = LARGE_SQUARE_SIZE;
      myBorderWidth = LARGE_BORDER;
      myBoardWidth = lgBoardSize;
      myBlackChecker = Image.createImage("/img/black_checker_48.png");
      myRedChecker = Image.createImage("/img/red_checker_48.png");
      myBlackKing = Image.createImage("/img/black_king_48.png");
      myRedKing = Image.createImage("/img/red_king_48.png");
    }
    myCornerX = (width - mySquareSize*8)/2;
    myCornerY = (height - mySquareSize*8)/2;
    Main.setMessage("loaded image: " + myBlackChecker);
    Main.setMessage("loaded image: " + myRedChecker);
    Main.setMessage("loaded image: " + myBlackKing);
    Main.setMessage("loaded image: " + myRedKing);
    if((myBlackChecker == null) || (myRedChecker == null) ||
       (myBlackKing == null) || (myRedKing == null)) {
      Main.setMessage("failed to load images");
      Main.quit();
    }
  }

//-------------------------------------------------------
//  graphics methods

  /**
   * Repaint the checkerboard.
   */
  protected void paint(Graphics g) {
    Main.setMessage("paint");
    int width = getWidth();
    int height = getHeight();
    // clear the board (including the region around
    // the board, which can get menu stuff and other 
    // garbage painted onto it...)
    g.setColor(TAN);
    g.fillRect(0, 0, width, height);
    g.setColor(WHITE);
    g.fillRect(myCornerX - myBorderWidth, myCornerY - myBorderWidth, 
               myBoardWidth, myBoardWidth);
    g.setColor(BLACK);
    g.drawRect(myCornerX - myBorderWidth, myCornerY - myBorderWidth, 
               myBoardWidth, myBoardWidth);
    // now draw the checkerboard:
    // first the dark squares:
    Main.setMessage("about to draw");
    byte offset = 0;
    for(byte i = 0; i < 4; i++) {
      for(byte j = 0; j < 8; j++) {
        // the offset is used to handle the fact that in every 
        // other row the dark squares are shifted one place 
        // to the right.
        if(j % 2 != 0) {
          offset = 1;
        } else {
          offset = 0;
        }
        // calculate the top, left corner of the current square:
        int xCoord = myCornerX + (2*i + offset)*mySquareSize;
        int yCoord = myCornerY + j*mySquareSize;
        // now if this is a selected square, we draw it lighter
        // and outline it:
        if(myGame.isSelected(i, j)) {
          g.setColor(LT_GREY);
          //g.fillRect(myCornerX + (2*i + offset)*mySquareSize, myCornerY + j*mySquareSize, 
          //           mySquareSize, mySquareSize);
          g.fillRect(xCoord, yCoord, 
                     mySquareSize, mySquareSize);
          g.setColor(RED);
          g.drawRect(xCoord, yCoord, 
                     mySquareSize - 1, mySquareSize - 1);
          //g.drawRect(myCornerX + (2*i + offset)*mySquareSize, myCornerY + j*mySquareSize, 
          //           mySquareSize - 1, mySquareSize - 1);
        } else {
          // if it's not selected, we draw it dark grey:
          g.setColor(GREY);
          //g.fillRect(myCornerX + (2*i + offset)*mySquareSize, myCornerY + j*mySquareSize, 
          //           mySquareSize, mySquareSize);
          g.fillRect(xCoord, myCornerY + j*mySquareSize, 
                     mySquareSize, mySquareSize);
        }
        // now put the pieces in their places:
        int piece = myGame.getPiece(i, j);
        int center = mySquareSize/2;
        if(piece == -1) {
          g.drawImage(myBlackChecker, 
                      xCoord + center, 
                      yCoord + center, 
                      Graphics.VCENTER|Graphics.HCENTER);
          //g.drawImage(myBlackChecker, 
          //            myCornerX + (2*i + offset)*mySquareSize + mySquareSize/2, 
          //            myCornerY + j*mySquareSize + 1 + mySquareSize/2, 
          //            Graphics.VCENTER|Graphics.HCENTER);
        } else if(piece == -2) {
          g.drawImage(myBlackKing, 
                      xCoord + center, 
                      yCoord + center, 
                      Graphics.VCENTER|Graphics.HCENTER);
          //g.drawImage(myBlackKing, 
          //            myCornerX + (2*i + offset)*mySquareSize + mySquareSize/2, 
          //            myCornerY + j*mySquareSize + 1 + mySquareSize/2, 
          //            Graphics.VCENTER|Graphics.HCENTER);
        } else if(piece == 1) {
          g.drawImage(myRedChecker, 
                      xCoord + center, 
                      yCoord + center, 
                      Graphics.VCENTER|Graphics.HCENTER);
          //g.drawImage(myRedChecker, 
          //            myCornerX + (2*i + offset)*mySquareSize + mySquareSize/2, 
          //            myCornerY + j*mySquareSize + 1 + mySquareSize/2, 
          //            Graphics.VCENTER|Graphics.HCENTER);
        } else if(piece == 2) {
          g.drawImage(myRedKing, 
                      xCoord + center, 
                      yCoord + center, 
                      Graphics.VCENTER|Graphics.HCENTER);
          //g.drawImage(myRedKing, 
          //            myCornerX + (2*i + offset)*mySquareSize + mySquareSize/2, 
          //            myCornerY + j*mySquareSize + 1 + mySquareSize/2, 
          //            Graphics.VCENTER|Graphics.HCENTER);
        }
      }
    }
    //g.setColor(BLACK);
    //g.drawRect(myCornerX, myCornerY, mySquareSize*8, mySquareSize*8);
    Main.setMessage("paint-->done");
  }

  //-------------------------------------------------------
  //  handle keystrokes

  /**
   * Move the player.
   */
  public void keyPressed(int keyCode) {
    Main.setMessage("key pressed");
    if(myGame.isMyTurn()) {
      int action = getGameAction(keyCode);   
      switch (action) {
      case LEFT:
        Main.setMessage("LEFT");
        myGame.leftPressed();
        break;
      case RIGHT:
        Main.setMessage("RIGHT");
        myGame.rightPressed();
        break;
      case FIRE:
      case UP:
        Main.setMessage("UP");
        myGame.upPressed();
        break;
      case DOWN:
        Main.setMessage("DOWN");
        myGame.deselect();
        break;
      }
      repaint();
      //serviceRepaints();
    }
  }

}
