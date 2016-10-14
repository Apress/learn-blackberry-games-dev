package net.frogparrot.maze;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.*;

/**
 * The gauge to display how many columns wide the maze is.
 *
 * @author Carol Hamer
 */
class WidthGauge extends GaugeField {
    
  //---------------------------------------------------------
  //   instance fields

  /**
   * The Columns gauge to inform when the square size changes.
   */
  private ColumnsGauge myColumnsGauge;
  
  /**
   * The size of this gauge's view window.
   */
  private int myMaxSquareSize;
  
  //---------------------------------------------------------
  //   initialization and state changes

  /**
   * Constructor initializes the data.
   */
  WidthGauge(ColumnsGauge cg, String label, int min, int max, 
      int start, long style) {
    super(label, min, max, start, style);
    myMaxSquareSize = MazeGame.getInstance().getColWidth(
        MazeGame.getInstance().getMaxColWidthIndex());
    myColumnsGauge = cg;
  }
  
  /**
   * This is called by the platform (prompted by the
   * layer manager) to tell this field to lay out its
   * own contents.  This gauge has nothing to lay out,
   * but we do need to inform the platform of how much 
   * of the allotted space this widget will take.
   * @param width The width (in pixels) allotted to this gauge.
   * @param height The width (in pixels) allotted to this gauge.
   */
  protected void layout(int width, int height) {
    // tell the platform that this gauge uses all of its
    // allotted space:
    setExtent(width, height);
  }
   
  /**
   * This is called by the platform to inform the gauge
   * that the user has edited its contents.
   * @param context Information specifying the origin 
   *                of the change (not relevant here).
   */
  protected void fieldChangeNotify(int context) {
    // get the gauge's new value and pass it along
    // to the underlying game logic.
    int val = getValue();
    int numCols = MazeGame.getInstance().setColWidthIndex(val);
    // inform the columns gauge that its value has
    // changed accordingly:
    myColumnsGauge.setValue(numCols);
  }
  
  //---------------------------------------------------------
  //   graphics methods

  /**
   * This is called by the platform to tell the Field to 
   * draw itself in its focused (or unfocused) state.
   * Since only one field on this screen can be focused,
   * it is always focused, hence there's no need to draw
   * a focused and unfocused version.  We override the 
   * method to prevent the superclass from drawing its
   * (ugly) default focus indicator.
   */
  protected void drawFocus(Graphics g, boolean on) {}
      
  /**
   * Paint the gauge.  It appears as an indented window
   * showing the player at its current size.
   * @param g The Graphics instance to use for painting.
   */
  protected void paint(Graphics g) {
    // color the background:
    g.setColor(SelectSizeManager.LT_BLUE);
    g.fillRect(0, 0, getWidth(), getHeight());
    // calculate where to place the window:
    int x = (getWidth() - myMaxSquareSize)/2;
    int y = (getHeight() - myMaxSquareSize)/2;
    // clear the window:
    g.setColor(MazeGame.WHITE);
    g.fillRect(x, y, myMaxSquareSize, myMaxSquareSize);
    // draw a border around the window to make it appear indented:
    g.setColor(SelectSizeManager.OUTLINE);
    g.drawRect(x - 2, y - 2, myMaxSquareSize + 4, myMaxSquareSize + 4);
    g.setColor(SelectSizeManager.MED_BLUE);
    g.drawLine(x - 1, y - 1, x + myMaxSquareSize - 1, y - 1);
    g.drawLine(x - 1, y - 1, x - 1, y + myMaxSquareSize - 1);
    // draw the player as an image bitmap:
    MazeGame game = MazeGame.getInstance();
    int squareSize = game.getColWidth(game.getColWidthIndex());
    if(squareSize == myMaxSquareSize) {
      g.drawBitmap(x, y,
                 squareSize, squareSize,
                 game.getPlayerBitmap(), 0, 0);
    } else {
      // if the player size is smaller than the window size,
      // then center the small image in the window and draw
      // a light gray border around it:
      int innerX = x + (myMaxSquareSize - squareSize)/2;
      int innerY = y + (myMaxSquareSize - squareSize)/2;
      g.drawBitmap(innerX, innerY,
                 squareSize, squareSize,
                 game.getPlayerBitmap(), 0, 0);
      g.setColor(MazeGame.HIGHLIGHT_GRAY);
      g.drawRect(innerX, innerY, squareSize, squareSize);
    }
  }

} 
