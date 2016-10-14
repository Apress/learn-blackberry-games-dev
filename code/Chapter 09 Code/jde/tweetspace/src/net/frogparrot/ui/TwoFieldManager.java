//#preprocess
package net.frogparrot.ui;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;

import net.frogparrot.tweetspace.Main;

/**
 * This class controls the overall layout of the screen,
 * including an animated window and a message window.
 * 
 * @author Carol Hamer
 */
public class TwoFieldManager extends Manager {
      
//---------------------------------------------------------
//   constants

  /**
   * Color constants for the frame around the window.
   */
  public static final int BLACK = 0;
  public static final int WHITE = 0xffffff;
  public static final int SCREEN_GRAY = 0x00777777;
  public static final int HIGHLIGHT_GRAY = 0x00cccccc;
  public static final int SHADOW_GRAY = 0x00303030;
  public static final int OUTLINE = 0x00101010;
  
  /**
   * The number of pixels around the animated window field
   * (including the frame).
   */
  public static final int BORDER_WIDTH = 9;
  
  /**
   * The number of pixels of the border are used for 
   * a frame that is drawn around the window.
   */
  public static final int FRAME_WIDTH = 4;
  
//---------------------------------------------------------
//   instance fields

  /**
   * The screen dimensions.
   */
  int myScreenWidth;
  int myScreenHeight;
  int myWindowWidth;
  int myWindowHeight;
  
  /**
   * Whether the two windows are placed side-by-side
   * or one above the other.
   */
  boolean myVerticalLayout;
  
  /**
   * The object that holds and animates the animated window.
   */
  FieldAnimator myFieldAnimator;
  
  /**
   * The editable text field.
   */
  MessageField myMessageField;
  
  /**
   * The text field has a separate layout Manager so that
   * it can scroll separately.
   */
  Manager myTextManager;
    
//---------------------------------------------------------
//  data initialization and cleanup

 /**
  * Build the two fields based on the screen dimensions.
  */
  public TwoFieldManager(MessageField mf, Main mmain) {
    super(USE_ALL_HEIGHT | USE_ALL_WIDTH);
    myMessageField = mf;
    try {
      // get the font to check its dimensions
      Font font = Font.getDefault();
      // The Graphics class has methods to get the 
      // screen dimensions and so does the Display class.
      // One pair is deprecated, the other requires a signature.
      // Take your pick ;^)
      myScreenWidth = Graphics.getScreenWidth();
      myScreenHeight = Graphics.getScreenHeight();
      //myScreenWidth = Display.getWidth();
      //myScreenHeight = Display.getHeight();
      
      // The text area needs to at least be big enough
      // to fit two lines of text vertically and the 
      // game title horizontally:
      boolean screenOK = true;
      if(myScreenWidth > myScreenHeight) {
        int textWidth = font.getAdvance(myMessageField.getLabel());
        myWindowHeight = myScreenHeight - 2*(BORDER_WIDTH);
        myWindowWidth = myScreenHeight - 2*(BORDER_WIDTH);
        if((myScreenWidth - myWindowWidth) < textWidth) {
          myWindowWidth = myScreenWidth - (textWidth + 2*BORDER_WIDTH);
          // handle the case where the screen isn't big enough:
          if(myWindowWidth < 5*BORDER_WIDTH) {
            screenOK = false;
          }
        }
        myVerticalLayout = false;
      } else {
        int textHeight = font.getHeight();
        myWindowWidth = myScreenWidth - 2*(BORDER_WIDTH);
        myWindowHeight = myWindowWidth - 2*(BORDER_WIDTH);
        if((myScreenHeight - myWindowHeight) < 2*textHeight) {
          myWindowHeight = myScreenHeight - 2*(textHeight + BORDER_WIDTH);
          // handle the case where the screen isn't big enough:
          if(myWindowHeight < 5*BORDER_WIDTH) {
            screenOK = false;
          }
        }
        myVerticalLayout = true;
      }
      // now that we have the dimensions, build the fields:
      if(screenOK) {
        // Any field animator can be placed in the animated window:
        myFieldAnimator = mmain.createFieldAnimator(myWindowWidth, myWindowHeight);
        add(myFieldAnimator.getField());
        myFieldAnimator.play();
      } else {
        throw new IllegalArgumentException("Screen too small");
      }
      // create and add the scrolling area for the text:
      myTextManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL 
             | Manager.VERTICAL_SCROLLBAR | Manager.USE_ALL_HEIGHT);
      myTextManager.add((Field)myMessageField);
      add(myTextManager);
    } catch(Exception e) {
      Main.postException(e);
    }
  }
  
  /**
   * Stop the animation and clear the data.
   */
  public void cleanUp() {
    if(myFieldAnimator != null) {
      myFieldAnimator.stop();
      myFieldAnimator = null;
    }
    myTextManager = null;
  }
  
  /**
   * @return The MessageField displayed by this Manager.
   */
  public MessageField getMessageField() {
    return myMessageField;
  }
  
//---------------------------------------------------------
//  implementation of Manager

 /**
  * This Manager prefers to fill the whole display.
  */
  public int getPreferredHeight() {
    return myScreenWidth;
  }

 /**
  * This Manager prefers to fill the whole display.
  */
  public int getPreferredWidth() {
    return myScreenHeight;
  }
    
 /**
  * Place the fields according to the screen dimensions.
  */
  protected void sublayout(int width, int height) {
    // start by positioning the animated window:
    Field animatedWindow = myFieldAnimator.getField();
    setPositionChild(animatedWindow, BORDER_WIDTH, BORDER_WIDTH);
    layoutChild(animatedWindow, myWindowWidth, myWindowHeight);
    // The dimensions and placement of the text field depend
    // on the screen dimensions and on the position of the
    // animated window:
    int textFieldWidth = 0;
    int textFieldHeight = 0;
    if(myVerticalLayout) {
      textFieldWidth = myScreenWidth;
      textFieldHeight = myScreenHeight - myWindowHeight;
      setPositionChild(myTextManager, 0, myWindowHeight + 2*(BORDER_WIDTH));
    } else {
      textFieldHeight = myScreenHeight;
      textFieldWidth = myScreenWidth - myWindowWidth;
      setPositionChild(myTextManager, myWindowWidth + 2*(BORDER_WIDTH), 0);
    }
    layoutChild(myTextManager, textFieldWidth, textFieldHeight);
    // be sure to tell the platform that this Manager fills the whole screen:
    setExtent(myScreenWidth, myScreenHeight);
  }

  /**
   * Paint the two fields with a frame around the animated window.
   */
  protected void subpaint(net.rim.device.api.ui.Graphics g) {
    // choose colors to indicate the focus:
    int background = HIGHLIGHT_GRAY;
    int outline = OUTLINE;
    if(myFieldAnimator.getField().isFocus()) {
      background = SCREEN_GRAY;
      outline = WHITE;
    }
    // clear the screen and paint the animated window:
    g.setColor(background);
    g.fillRect(0, 0, myScreenWidth, myScreenHeight);
    paintChild(g, myFieldAnimator.getField());
    // draw the frame around the animated window:
    int cornerX = BORDER_WIDTH;
    int cornerY = BORDER_WIDTH;
    // first the top:
    int[] xPts = { cornerX - FRAME_WIDTH, 
                   cornerX, 
                   cornerX + myWindowWidth, 
                   cornerX + myWindowWidth + FRAME_WIDTH };
    int[] yPts = { cornerY - FRAME_WIDTH, 
                   cornerY, 
                   cornerY, 
                   cornerY - FRAME_WIDTH };
    // set the colors to create a gradient, 
    // darker on the inside edge, lighter on the outside
    int[] colors = { HIGHLIGHT_GRAY,
                     SHADOW_GRAY,
                     SHADOW_GRAY,
                     HIGHLIGHT_GRAY };
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // now draw the left side:
    xPts[0] = cornerX - FRAME_WIDTH;
    xPts[1] = cornerX;
    xPts[2] = cornerX;
    xPts[3] = cornerX - FRAME_WIDTH;
    yPts[0] = cornerY - FRAME_WIDTH;
    yPts[1] = cornerY;
    yPts[2] = cornerY + myWindowHeight;
    yPts[3] = cornerY + myWindowHeight + FRAME_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // now the bottom:
    // reverse the colors to give more shading to the 
    // bottom/right sides for a 3-d effect:
    colors[0] = SHADOW_GRAY;
    colors[1] = HIGHLIGHT_GRAY;
    colors[2] = HIGHLIGHT_GRAY;
    colors[3] = SHADOW_GRAY;
    xPts[0] = cornerX - FRAME_WIDTH;
    xPts[1] = cornerX;
    xPts[2] = cornerX + myWindowWidth;
    xPts[3] = cornerX + myWindowWidth + FRAME_WIDTH;
    yPts[0] = cornerY + myWindowHeight + FRAME_WIDTH;
    yPts[1] = cornerY + myWindowHeight;
    yPts[2] = cornerY + myWindowHeight;
    yPts[3] = cornerY + myWindowHeight + FRAME_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // now the right side:
    xPts[0] = cornerX + myWindowWidth + FRAME_WIDTH;
    xPts[1] = cornerX + myWindowWidth;
    xPts[2] = cornerX + myWindowWidth;
    xPts[3] = cornerX + myWindowWidth + FRAME_WIDTH;
    yPts[0] = cornerY - FRAME_WIDTH;
    yPts[1] = cornerY;
    yPts[2] = cornerY + myWindowHeight;
    yPts[3] = cornerY + myWindowHeight + FRAME_WIDTH;
    g.drawShadedFilledPath(xPts, yPts, null, colors, null);
    // Draw a dark or highlighted outline around the window:
    g.setColor(outline);
    g.drawRect(cornerX, cornerY, 
               myWindowWidth, myWindowHeight);
    // Draw a dark outline around the frame:
    g.setColor(OUTLINE);
    g.drawRect(cornerX - FRAME_WIDTH - 1, cornerY - FRAME_WIDTH - 1, 
               myWindowWidth + 2*FRAME_WIDTH + 1, 
               myWindowHeight + 2*FRAME_WIDTH + 1);
    // end with the text field:
    paintChild(g, myTextManager);
  }
  
  /**
   * Override keyChar to direct key input to the active window,
   * and cause the escape key to switch the focus for ease of navigation.
   * @return Whether the key input was used.
   */
  public boolean keyChar(char key, int status, int time) {
    //System.out.println("TwoFieldManager.keyChar: " + key);
    if(myFieldAnimator.getField().isFocus()) {
      if(key == Characters.ESCAPE) {
        // the spaceship stops moving when the player is entering
        // a message:
        myFieldAnimator.keyChar('x', status, time);
        myTextManager.setFocus();
        // force repaint:
        invalidate();
        return true;
      } else {
        return myFieldAnimator.keyChar(key, status, time);
      }
    } else {
      if(key == Characters.ESCAPE) {
        // when the user switches the focus off of the
        // message field, prompt it to send the message:
        myMessageField.sendMessage();
        myFieldAnimator.getField().setFocus();
        // force repaint:
        invalidate();
        return true;
      } else {
        return myMessageField.keyChar(key, status, time, true);
      }
    }
  }  
  
 /**
  * Override navigationMovement to direct key input to the active window.
  * @see net.rim.device.api.ui.Screen#navigationMovement(int, int, int, int)
  */
//#ifdef RIM_4.2.0
  public boolean navigationMovement(int dx, int dy, int status, int time) {
    if(myFieldAnimator.getField().isFocus()) {
      return myFieldAnimator.navigationMovement(dx, dy, status, time);
    } else {
      return myMessageField.navigationMovement(dx, dy, status, time, true);
    }
  }
//#endif

} 

