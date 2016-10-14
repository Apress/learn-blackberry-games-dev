package net.frogparrot.svggame;

import java.io.InputStream;
import javax.microedition.m2g.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;

import net.frogparrot.ui.*;

/**
 * This is an illustration of how an SVG animation
 * can be controlled to form a game.
 * 
 * @author Carol Hamer
 */
public class SvgController extends Field implements FieldAnimator {
    
//---------------------------------------------------------
//   fields

  /**
   * The SVG image built from the image data in the file.
   */
  SVGImage myImage;

  /**
   * The root element.
   */
  SVGSVGElement mySvgSvg;
   
  /**
   * Game objects that are described in the SVG file.
   */
  SVGLocatableElement mySpaceship;
  SVGLocatableElement myFlames1;
  SVGLocatableElement myFlames2;
  SVGLocatableElement myFlames3;
  SVGLocatableElement myFlames4;
   
  /**
   * The Graphics instance that is used for drawing operations.
   */
  ScalableGraphics myScalableGraphics;
   
 /**
  * The width (in screen pixels) of the visible region.
  */
  int myViewportWidth;
   
 /**
  * The height (in screen pixels) of the visible region.
  */
  int myViewportHeight;
   
 /**
  * The spaceship's current location and direction, in terms of SVG coordinates.
  */
  float mySpaceshipX;
  float mySpaceshipY;
  float mySpaceshipAngle;
   
 /**
  * The data and objects to control the game animation.
  */
  int myInvokeLaterId = -1;
  long myTimeIncrement = 100;
  FrameRunner myFrameRunner;

 /**
  * The user's latest input data.
  */
  char myInput = (char)-1;
   

//---------------------------------------------------------
//   data initialization and cleanup

  /**
   * Load the image data and modify it based on the viewport dimensions.
   * @param viewportWidth width in pixels of the visible area.
   * @param viewportHeight height in pixels of the visible area.
   */
  public SvgController(int viewportWidth, int viewportHeight) {
    super(Field.FOCUSABLE);
    try {
      myScalableGraphics = ScalableGraphics.createInstance();
      myViewportWidth = viewportWidth;
      myViewportHeight = viewportHeight;
      // get the resource from the file:  
      InputStream inputStream = getClass().getResourceAsStream("/space-components.svg");  
      myImage = (SVGImage)SVGImage.createImage(inputStream, null); 
      
      // The SVGSVGElement has some interesting functionality 
      // to control the whole image:
      Document doc = myImage.getDocument();
      mySvgSvg = (SVGSVGElement)doc.getDocumentElement();
      
      // Get handles to the elements that represent components of the image:
      mySpaceship = (SVGLocatableElement)doc.getElementById("spaceship");
      myFlames1 = (SVGLocatableElement)doc.getElementById("flames-1");
      myFlames2 = (SVGLocatableElement)doc.getElementById("flames-2");
      myFlames3 = (SVGLocatableElement)doc.getElementById("flames-3");
      myFlames4 = (SVGLocatableElement)doc.getElementById("flames-4");
      // the engine flames should be invisible except when the rocket is moving:
      myFlames1.setTrait("visibility", "hidden");
      myFlames2.setTrait("visibility", "hidden");
      myFlames3.setTrait("visibility", "hidden");
      myFlames4.setTrait("visibility", "hidden");
      
      // tell the SVG Image how big to scale itself:
      myImage.setViewportWidth(viewportWidth);
      myImage.setViewportHeight(viewportHeight);

      // move the animation forward very slightly because it's 
      // the animation the puts the engines in place:
      myImage.incrementTime(0.01f);

      // Create the runnable that advances the animation:
      myFrameRunner = new FrameRunner(this);
    } catch(Exception e) {
      // for debug
      System.out.println(e);
    }
  }
 
//---------------------------------------------------------
//   implementation of Field

  /**
   * When this field is laid out, it merely claims all of the
   * area that it is given.
   */
  protected void layout(int width, int height) {
    setExtent(width, height);
  }

  /**
   * Paint the SVG image onto the field.
   * @param g The Graphics object that draws into the Field.
   */
  protected void paint(Graphics g) {
    myScalableGraphics.bindTarget(g);
    myScalableGraphics.render(0, 0, myImage);
    myScalableGraphics.releaseTarget();
  }

//---------------------------------------------------------
//   implementation of FieldAnimator

  /**
   * @see FieldAnimator#getField()
   */
  public Field getField() {
    return this;
  }
   
  /**
   * @see FieldAnimator#frameAdvance()
   */
  public boolean frameAdvance() {
    try {
      // advance the animation:
      myImage.incrementTime(0.15f);
      // get the latest user input, and clear the input data:
      char key = (char)-1;
      synchronized(this) {
        key = myInput;
        myInput = (char)-1;
      }
      // move the spaceship accordingly:
      SVGMatrix spaceshipTransform = mySpaceship.getMatrixTrait("transform");
      if(key == (char)-1) {
        // just turn the flames off:
        myFlames1.setTrait("visibility", "hidden");
        myFlames2.setTrait("visibility", "hidden");
        myFlames3.setTrait("visibility", "hidden");
        myFlames4.setTrait("visibility", "hidden");
      } else if(key == 'd') {
        // scale up
        spaceshipTransform.mScale(1.25f);
      } else if(key == 'x') {
        // scale down
        spaceshipTransform.mScale(0.75f);
      } else if(key == 'f') {
        // rotate clockwise:
        mySpaceshipAngle += 45f;
        // apply the rotation to the current transformation:
        spaceshipTransform.mRotate(45f);
      } else if(key == 'e') {
        // turn the flames on when the ship moves forward:
        myFlames1.setTrait("visibility", "visible");
        myFlames2.setTrait("visibility", "visible");
        myFlames3.setTrait("visibility", "visible");
        myFlames4.setTrait("visibility", "visible");
        // Move the spaceship in the direction it is pointing:
        spaceshipTransform.mTranslate(0f, -100f);
      } else if(key == 's') {
        // rotate counter-clockwise:
        mySpaceshipAngle -= 45f;
        // apply the rotation to the current transformation:
        spaceshipTransform.mRotate(-45f);
      }
      // Once the matrix has been modified, it has to be set back
      // into the spaceship element in order to take effect:
      mySpaceship.setMatrixTrait("transform", spaceshipTransform);
      // The call to invalidate forces a repaint operation:
      invalidate(0, 0, myViewportWidth, myViewportHeight);
    } catch(Exception e) {
      System.out.println(e);
    }
    return true;
  }   
  
  /**
   * @see FieldAnimator#play()
   */
  public void play() {
    // Application#invokeLater has a built-in timer function
    // so it can be used to advance an animation from the
    // event thread.  Don't forget to save the ID so that
    // you can stop the animation later!
    myInvokeLaterId = UiApplication.getUiApplication().invokeLater(
        myFrameRunner, myTimeIncrement, true);
  }

  /**
   * @see FieldAnimator#pause()
   */
  public void pause() {
    stop();
  }
 
  /**
   * @see FieldAnimator#stop()
   */
  public void stop() {
    if(myInvokeLaterId != -1) {
      UiApplication.getUiApplication().cancelInvokeLater(myInvokeLaterId);
      myInvokeLaterId = -1;
    }
  }
 
  /**
   * @see FieldAnimator#setTimeIncrementMillis(long)
   */
  public void setTimeIncrementMillis(long timeIncrement) {
    if(timeIncrement <= 0) {
      throw new IllegalArgumentException("timeIncrement must be positive");
    }
    myTimeIncrement = timeIncrement;
  }

  /**
   * @see FieldAnimator#keyChar(char, int, int)
   */
  public synchronized boolean keyChar(char key, int status, int time) {
    if((key == 's') || (key == 'd') || (key == 'f') || 
       (key == 'e') || (key == 'x')) {
      //System.out.println("got input: " + key);
      myInput = key;
      return true;
    } else {
      // the keystroke was not relevant to this game
      return false;
    }
  }
   
  /**
   * @see FieldAnimator#navigationMovement(int, int, int, int)
   */
  public synchronized boolean navigationMovement(int dx, int dy, int status, int time) {
      // map navigation input to key input for simplicity:
    if(dx < 0) { //left
      myInput = 's';
    } else if(dx > 0) { // right
      myInput = 'f';
    } else if(dy < 0) { // up
      myInput = 'e';
    } else {
      // the motion was not relevant to this game
      return false;
    }
    //System.out.println("got input: " + myInput);
    // the motion was used by this game
    return true;
  }
  
}


