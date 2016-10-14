//#preprocess
package net.frogparrot.maze;

import net.rim.device.api.ui.*;

/**
 * This is a custom layout manager to draw the look and feel
 * exactly as desired.
 *
 * @author Carol Hamer
 */
class SelectSizeManager extends Manager {
    
  //----------------------------------------------------------------
  //  static fields
  
  /**
   * Dimension constants depend on the screen size:
   */
//#ifdef SCREEN_240x160
  public static final int SCREEN_WIDTH = 240;
  public static final int SCREEN_HEIGHT = 160;
  public static final int BORDER_WIDTH = 4;
  public static final int INNER_BORDER_WIDTH = 16;
  public static final int H_GAUGE_BUFFER_WIDTH = 16;
  public static final int V_GAUGE_BUFFER_WIDTH = 12;
  public static final int WIDGET_WIDTH = 200;
  public static final int WIDGET_HEIGHT = 40;
  public static final int WIDGET_X = 20;
  public static final int WIDGET_Y = 20;
//#endif
//#ifdef SCREEN_240x260
  public static final int SCREEN_WIDTH = 240;
  public static final int SCREEN_HEIGHT = 260;
  public static final int BORDER_WIDTH = 4;
  public static final int INNER_BORDER_WIDTH = 16;
  public static final int H_GAUGE_BUFFER_WIDTH = 16;
  public static final int V_GAUGE_BUFFER_WIDTH = 12;
  public static final int WIDGET_WIDTH = 200;
  public static final int WIDGET_HEIGHT = 40;
  public static final int WIDGET_X = 20;
  public static final int WIDGET_Y = 70;
//#endif
//#ifdef SCREEN_320x240
  public static final int SCREEN_WIDTH = 320;
  public static final int SCREEN_HEIGHT = 240;
  public static final int BORDER_WIDTH = 6;
  public static final int INNER_BORDER_WIDTH = 24;
  public static final int H_GAUGE_BUFFER_WIDTH = 24;
  public static final int V_GAUGE_BUFFER_WIDTH = 18;
  public static final int WIDGET_WIDTH = 260;
  public static final int WIDGET_HEIGHT = 60;
  public static final int WIDGET_X = 30;
  public static final int WIDGET_Y = 30;
//#endif
//#ifdef SCREEN_480x360
  public static final int SCREEN_WIDTH = 480;
  public static final int SCREEN_HEIGHT = 360;
  public static final int BORDER_WIDTH = 8;
  public static final int INNER_BORDER_WIDTH = 32;
  public static final int H_GAUGE_BUFFER_WIDTH = 32;
  public static final int V_GAUGE_BUFFER_WIDTH = 24;
  public static final int WIDGET_WIDTH = 400;
  public static final int WIDGET_HEIGHT = 80;
  public static final int WIDGET_X = 40;
  public static final int WIDGET_Y = 40;
//#endif

  /**
   * color constants.
   */
  public static final int OUTLINE = 0x00025cb8;
  public static final int DK_BLUE = 0x00037ffd;
  public static final int MED_BLUE = 0x0066b0fb;
  public static final int LT_BLUE = 0x00c7e2fd;
  public static final int BLACK = 0x00000000;
  
  /**
   * The screen title.
   */
  public static String myLabel;

  //----------------------------------------------------------------
  //  initialization and lifecycle
  
  /**
   * Constructor initializes the data.
   */
  SelectSizeManager() {
    super(USE_ALL_HEIGHT | USE_ALL_WIDTH);
    myLabel = MazeScreen.getLabel(MazeScreen.MAZE_SELECTSIZE);
  }
  
  //----------------------------------------------------------------
  //  graphics
  
  /**
   * This layout covers the entire screen.
   */
  public int getPreferredHeight() {
    return SCREEN_HEIGHT;
  }

  /**
   * This layout covers the entire screen.
   */
  public int getPreferredWidth() {
    return SCREEN_WIDTH;
  }
    
  /**
   * This is called by the platform to prompt the
   * manager to lay out its contents.
   * @param width The width of the area allotted to the manager
   * @param height The height of the area allotted to the manager
   */
  protected void sublayout(int width, int height) {
    // set the positions and sizes of the gauges using 
    // screen-specific constants:
    Field widthField = getField(0);
    setPositionChild(widthField, WIDGET_X, WIDGET_Y + WIDGET_HEIGHT);
    layoutChild(widthField, WIDGET_WIDTH, WIDGET_HEIGHT);
    Field columnsField = getField(1);
    setPositionChild(columnsField, WIDGET_X, WIDGET_Y + WIDGET_HEIGHT*2);
    layoutChild(columnsField, WIDGET_WIDTH, WIDGET_HEIGHT);
    // Tell the platform how much space this manager is taking:
    setExtent(SCREEN_WIDTH, SCREEN_HEIGHT);
  }

  /**
   * This is called by the platform to prompt the manager to 
   * paint its contents.  It is not necessary to override this method.
   * @param g The Graphics instance to use to paint the region.
   */
  protected void subpaint(Graphics g) {
    g.setColor(LT_BLUE);
    // clear the screen to background color
    g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    // draw the border frame, made of four trapezoids
    // (like a picture frame), with gradient shading
    // to make the central area appear raised.
    // first the top:
    int[] xPts = { BORDER_WIDTH, 
                   WIDGET_X, 
                   SCREEN_WIDTH - WIDGET_X, 
                   SCREEN_WIDTH - BORDER_WIDTH };
    int[] yPts = { BORDER_WIDTH, 
                   WIDGET_Y, 
                   WIDGET_Y, 
                   BORDER_WIDTH };
    // set the colors to create a gradient, 
    // darker on the outside edge, lighter on the inside
    int[] colors = { MED_BLUE,
                     LT_BLUE,
                     LT_BLUE,
                     MED_BLUE };
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // now draw the left side:
    xPts[0] = BORDER_WIDTH;
    xPts[1] = WIDGET_X;
    xPts[2] = WIDGET_X;
    xPts[3] = BORDER_WIDTH;
    yPts[0] = BORDER_WIDTH;
    yPts[1] = WIDGET_Y;
    yPts[2] = SCREEN_HEIGHT - WIDGET_Y;
    yPts[3] = SCREEN_HEIGHT - BORDER_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // now the bottom:
    // change the colors to give more shading to the 
    // bottom/right sides for a 3-d effect:
    colors[0] = DK_BLUE;
    colors[3] = DK_BLUE;
    xPts[0] = BORDER_WIDTH;
    xPts[1] = WIDGET_X;
    xPts[2] = SCREEN_WIDTH - WIDGET_X;
    xPts[3] = SCREEN_WIDTH - BORDER_WIDTH;
    yPts[0] = SCREEN_HEIGHT - BORDER_WIDTH;
    yPts[1] = SCREEN_HEIGHT - WIDGET_Y;
    yPts[2] = SCREEN_HEIGHT - WIDGET_Y;
    yPts[3] = SCREEN_HEIGHT - BORDER_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // now the right side:
    xPts[0] = SCREEN_WIDTH - BORDER_WIDTH;
    xPts[1] = SCREEN_WIDTH - WIDGET_X;
    xPts[2] = SCREEN_WIDTH - WIDGET_X;
    xPts[3] = SCREEN_WIDTH - BORDER_WIDTH;
    yPts[0] = BORDER_WIDTH;
    yPts[1] = WIDGET_Y;
    yPts[2] = SCREEN_HEIGHT - WIDGET_Y;
    yPts[3] = SCREEN_HEIGHT - BORDER_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // put a shadow along the right side of the 
    // raised area:
    xPts[0] = SCREEN_WIDTH - BORDER_WIDTH;
    xPts[1] = SCREEN_WIDTH;
    xPts[2] = SCREEN_WIDTH;
    xPts[3] = SCREEN_WIDTH - BORDER_WIDTH;
    yPts[0] = BORDER_WIDTH;
    yPts[1] = BORDER_WIDTH;
    yPts[2] = SCREEN_HEIGHT;
    yPts[3] = SCREEN_HEIGHT - BORDER_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // Then put a shadow below the raised area:
    xPts[0] = BORDER_WIDTH;
    xPts[1] = BORDER_WIDTH;
    xPts[2] = SCREEN_WIDTH;
    xPts[3] = SCREEN_WIDTH - BORDER_WIDTH;
    yPts[0] = SCREEN_HEIGHT - BORDER_WIDTH;
    yPts[1] = SCREEN_HEIGHT;
    yPts[2] = SCREEN_HEIGHT;
    yPts[3] = SCREEN_HEIGHT - BORDER_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // Draw a dark outline around the raised area to 
    // separate it from its shadow:
    g.setColor(OUTLINE);
    g.drawRect(BORDER_WIDTH, BORDER_WIDTH, 
               SCREEN_WIDTH - 2*BORDER_WIDTH, 
               SCREEN_HEIGHT - 2*BORDER_WIDTH);
    
    // draw the title
    g.setColor(DK_BLUE);
    Font font = g.getFont();
    int textWidth = font.getAdvance(myLabel);
    int textHeight = font.getHeight();
    g.drawText(myLabel, WIDGET_X + (WIDGET_WIDTH - textWidth)/2, 
        WIDGET_Y + (WIDGET_HEIGHT - textHeight)/2);
    
    // paint the fields
    Field widthField = getField(0);
    paintChild(g, widthField);
    Field columnsField = getField(1);
    paintChild(g, columnsField);
  }

} 
