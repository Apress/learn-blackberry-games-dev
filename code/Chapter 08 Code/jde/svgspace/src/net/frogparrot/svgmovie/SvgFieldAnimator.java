package net.frogparrot.svgmovie;

import java.io.InputStream;
import javax.microedition.m2g.*;

import net.rim.device.api.ui.Field;

import net.frogparrot.ui.*;

/**
 * This is a wrapper class for the SVGAnimator to make it 
 * behave like other animated fields (for modularity).
 * 
 * @author Carol Hamer
 */
public class SvgFieldAnimator implements FieldAnimator {
    
//---------------------------------------------------------
//   fields

  /**
   * The underlying SVGFieldAnimator.
   */
   SVGAnimator myAnimator;
    
//---------------------------------------------------------
//   data initialization and cleanup

  /**
   * Load the animations.
   * @param viewportWidth width in pixels of the visible area.
   * @param viewportHeight height in pixels of the visible area.
   */
  public SvgFieldAnimator(int viewportWidth, int viewportHeight) {
    try {
      InputStream inputStream = getClass().getResourceAsStream("/space-animation.svg");  
      //System.out.println("input stream: " + inputStream);
         
       // Load our svg image from the input stream:
      SVGImage animation = (SVGImage)SVGImage.createImage(inputStream, null); 
      //System.out.println("created image: " + animation);
      animation.setViewportWidth(viewportWidth);
      animation.setViewportHeight(viewportHeight);

      // The second argument tells the animator what type of object to return 
      // as its "targetComponent" to draw on.
      myAnimator = SVGAnimator.createAnimator(animation, "net.rim.device.api.ui.Field");
      //System.out.println("created animator: " + myAnimator);

      myAnimator.setTimeIncrement(0.05f);
    } catch(Exception e) {
      // for debug
      System.out.println(e);
    }
  }
 
//---------------------------------------------------------
//   implementation of FieldAnimator

  /**
   * @see FieldAnimator#getField()
   */
  public Field getField() {
    return (Field)(myAnimator.getTargetComponent());
  }
   
  /**
   * @see FieldAnimator#frameAdvance()
   */
  public boolean frameAdvance() {
    return false;
  }   
   
  /**
   * @see FieldAnimator#play()
   */
  public void play() {
    myAnimator.play();
  }

  /**
   * @see FieldAnimator#pause()
   */
  public void pause() {
    myAnimator.pause();
  }
 
  /**
   * @see FieldAnimator#stop()
   */
  public void stop() {
    myAnimator.stop();
  }
 
  /**
   * @see FieldAnimator#setTimeIncrementMillis(long)
   */
  public void setTimeIncrementMillis(long timeIncrement) {
    myAnimator.setTimeIncrement(timeIncrement/1000);
  }

  /**
   * @see FieldAnimator#keyChar(char, int, int)
   */
  public boolean keyChar(char key, int status, int time) {
    return false;
  }
   
  /**
   * @see FieldAnimator#navigationMovement(int, int, int, int)
   */
  public boolean navigationMovement(int dx, int dy, int status, int time) {
    return false;
  }
    
}


