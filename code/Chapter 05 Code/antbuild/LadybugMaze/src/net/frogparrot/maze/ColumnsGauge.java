package net.frogparrot.maze;

import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.*;

/**
 * The gauge to display how many columns wide the maze is.
 *
 * @author Carol Hamer
 */
class ColumnsGauge extends GaugeField {
    
  //---------------------------------------------------------
  //   initialization and state changes

  /**
   * Constructor does nothing.
   */
  ColumnsGauge(String label, int min, int max, 
      int start, long style) {
    super(label, min, max, start, style);
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
   
  //---------------------------------------------------------
  //   graphics methods

  /**
   * Paint the gauge.  It appears as an indented window
   * showing how many columns the maze is divided into.
   * @param g The Graphics instance to use for painting.
   */
  protected void paint(Graphics g) {
    // color the background:
    g.setColor(SelectSizeManager.LT_BLUE);
    g.fillRect(0, 0, getWidth(), getHeight());
    // get the data on where to place the window
    // and how big to make it
    int x = SelectSizeManager.H_GAUGE_BUFFER_WIDTH;
    int y = SelectSizeManager.V_GAUGE_BUFFER_WIDTH;
    int width = getWidth() - 2*SelectSizeManager.H_GAUGE_BUFFER_WIDTH;
    int height = getHeight() - 2*SelectSizeManager.V_GAUGE_BUFFER_WIDTH;
    // color the window white:
    g.setColor(MazeGame.WHITE);
    g.fillRect(x + 2, y + 2, width - 4, height - 4);
    // draw a border around the window to make it look indented:
    g.setColor(SelectSizeManager.OUTLINE);
    g.drawRect(x, y, width, height);
    g.setColor(SelectSizeManager.MED_BLUE);
    g.drawLine(x + 1, y + 1, x + width - 2, y + 1);
    g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
    // now draw in the columns in the window:
    int columns = getValue();
    int colWidth = (width-4)/columns;
    int innerX = x + ((width-4) % columns)/2;
    boolean colored = true;
    for(int i = 0; i < columns; i++) {
      if(colored) {
        g.setColor(MazeGame.WALL_GRAY);
        int startX = innerX + i*colWidth;
        g.fillRect(startX, y + 2, colWidth, height - 4);
        g.setColor(MazeGame.HIGHLIGHT_GRAY);
        g.drawLine(startX, y + 2, startX, y + height - 3);
        g.setColor(MazeGame.SHADOW_GRAY);
        g.drawLine(startX + colWidth, y + 2, startX + colWidth, y + height - 3);
      }
      colored = !colored;
    }
  }


} 
