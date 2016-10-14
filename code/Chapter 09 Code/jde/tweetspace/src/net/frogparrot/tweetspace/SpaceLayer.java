//#preprocess
package net.frogparrot.tweetspace;

import java.util.Random;
import java.util.Hashtable;
import java.util.Enumeration;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.*;

import net.frogparrot.ui.*;
import net.frogparrot.game.RimSprite;

/**
 * The animated game board, including graphics and data to 
 * place the objects in space.
 * 
 * @author Carol Hamer
 */
public class SpaceLayer implements FieldAnimator {
    
//---------------------------------------------------------
//   constants

  /**
   * The length (and width) of the underlying space coordinate system.
   */
  public static final int SPACE_EXTENT = 10000;
  
  /**
   * The number of asteroids to place.
   */
  public static final int ASTEROID_COUNT = 10000;
  
  /**
   * Navigation constants.
   */
  public static final int UP = 0;
  public static final int DOWN = 1;
  public static final int LEFT = 2;
  public static final int RIGHT = 3;
  
//---------------------------------------------------------
//   fields

  /**
   * The size of the visible region in pixels.
   */
  int myViewportWidth;
  int myViewportHeight;
  
  /**
   * The data and objects to control the game animation.
   */
  int myInvokeLaterId = -1;
  long myTimeIncrement = 100;
  FrameRunner myFrameRunner;
  
  /**
   * The spaceship object and its data.
   */
  RimSprite mySprite;
  int mySpriteType;
  int myCornerX;
  int myCornerY;
  int myDirection;
  int mySpeed;

  /**
   * The graphics objects for the field.
   */
  Bitmap myCurrentBitmap;
  Bitmap myBitmap0;
  Bitmap myBitmap1;
  BitmapField myBitmapField;

  /**
   * The data that describes our area of space.
   * Each two consecutive ints in the array give the x and y
   * coordinates of an object in space.
   */
  int[] myStarField = new int[100];
  int[] myAsteroids = new int[2*ASTEROID_COUNT];
  Hashtable mySpaceships = new Hashtable();
  int mySpaceWidth = SPACE_EXTENT;
  int mySpaceHeight = SPACE_EXTENT;
  
  /**
   * A utility for randomly filling space with stars, etc.
   */
  private Random myRandom = new Random();
  
//---------------------------------------------------------
//   initialization and accessors

  /**
   * Create the underlying data.
   */
  public SpaceLayer(int viewportWidth, int viewportHeight) {
    try {
      // select the local player's spaceship image based on the
      // BlackBerry version:
//#ifdef RIM_4.1.0
      mySpriteType = 5;
//#endif
//#ifdef RIM_4.2.0
      mySpriteType = 2;
//#endif
//#ifdef RIM_4.3.0
      mySpriteType = 3;
//#endif
//#ifdef RIM_4.6.0
      mySpriteType = 4;
//#endif
      myViewportWidth = viewportWidth;
      myViewportHeight = viewportHeight;
      // the local player's ship is one of the cached ship sprites:
      mySprite = Spaceship.getSprite(mySpriteType);
      // place the spaceship in the middle of the viewscreen:
      mySprite.setRefPixelPosition(myViewportWidth/2, myViewportHeight/2);
      // fill the background with (fixed) randomly-placed stars:
      for(int i = 0; i < myStarField.length/2; i+=2) {
        myStarField[i] = getRandomInt(myViewportWidth);
        myStarField[i + 1] = getRandomInt(myViewportHeight);
      }
      // fill the virtual space grid with moveable asteroids:
      for(int i = 0; i < ASTEROID_COUNT; i+=2) {
        myAsteroids[i] = getRandomInt(mySpaceWidth);
        myAsteroids[i+1] = getRandomInt(mySpaceHeight);
      }
      // initialize the two bitmaps for image double-buffering:
      myBitmap0 = new Bitmap(myViewportWidth, myViewportHeight);
      myBitmap1 = new Bitmap(myViewportWidth, myViewportHeight);
      myCurrentBitmap = myBitmap0;
      // get the graphics object to draw on the first bitmap:
      Graphics g = new Graphics(myCurrentBitmap);
      // color the current bitmap black:
      g.pushRegion(new XYRect(0, 0, myViewportWidth, myViewportHeight));
      g.setColor(g.FULL_BLACK);
      g.fillRect(0, 0, myViewportWidth, myViewportHeight);
      // Set the bitmap into a bitmap field so that it can be displayed:
      myBitmapField = new BitmapField(myCurrentBitmap, Field.FOCUSABLE);
      // create the Runnable that is used to advance the game animation:
      myFrameRunner = new FrameRunner(this);
    } catch(Exception e) {
      // for debug:
      System.out.println(e);
    }
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
   * a randomization utility. 
   * @param upper the upper bound for the random int.
   * @return a random non-negative int less than the bound upper.
   */
  int getRandomInt(int upper) {
    int retVal = myRandom.nextInt() % upper;
    if(retVal < 0) {
      retVal += upper;
    }
    return(retVal);
  }
  
//---------------------------------------------------------
//   methods for communication with remote players:

  /**
   * Place the remote player's ship and store its message
   * (creating if necessary).
   * @param name the remote player's screen name
   * @param text the tweet containing the ship data and message
   */
  public void setAlienShip(String name, String text) {
    try {
      Spaceship existing = (Spaceship)(mySpaceships.get(name));
      if(existing != null) {
        existing.update(text);
      } else {
        Spaceship ship = new Spaceship(name, text);
        mySpaceships.put(name, ship);
      }
    } catch(IllegalArgumentException iae) {
      // if the tweet is malformed and/or not relevant to 
      // the game, it is logged and ignored:
      Main.postException(iae);
    }
  }
  
  /**
   * Creates a string that encodes the current local
   * player data, to send in the tweet.
   * @return the local player data to tweet
   */
  public String getPosition() {
    StringBuffer buff = new StringBuffer("*");
    buff.append(myCornerX + mySprite.getRefPixelX());
    buff.append(" ");
    buff.append(myCornerY + mySprite.getRefPixelY());
    buff.append(" ");
    switch(myDirection) {
      case RIGHT:
        buff.append("r ");
      break;
      case DOWN:
        buff.append("d ");
      break;
      case LEFT:
        buff.append("l ");
      break;
      default:
        buff.append("u ");
      break;
    }
    buff.append(mySpriteType);
    buff.append("*");
    return buff.toString();
  }

//---------------------------------------------------------
//   implementation of FieldAnimator

  /**
   * @see FieldAnimator#getField()
   */
  public Field getField() {
    return myBitmapField;
  }

  /**
   * @see FieldAnimator#frameAdvance()
   */
  public synchronized boolean frameAdvance() {
    //System.out.print(".");
    try {
      // decide how far to travel this frame, based on the speed:
      int distance = mySpeed*12;
      // decide where/how to place the spaceship, based on direction:
      switch(myDirection) {
        case UP:
          // rotate the sprite image to the correct direction:
          mySprite.setTransform(RimSprite.TRANS_NONE);
          myCornerY -= distance;
          // handle the wraparound:
          if(myCornerY < 0) {
            myCornerY += mySpaceHeight;
          }
          break;
        case DOWN:
          // rotate the sprite image to the correct direction:
          mySprite.setTransform(RimSprite.TRANS_ROT180);
          myCornerY += distance;
          // handle the wraparound:
          if(myCornerY > mySpaceHeight) {
            myCornerY -= mySpaceHeight;
          }
          break;
        case LEFT:
          // rotate the sprite image to the correct direction:
          mySprite.setTransform(RimSprite.TRANS_ROT90);
          myCornerX -= distance;
          // handle the wraparound:
          if(myCornerX < 0) {
            myCornerX += mySpaceWidth;
          }
          break;
        case RIGHT:
          // rotate the sprite image to the correct direction:
          mySprite.setTransform(RimSprite.TRANS_ROT270);
          myCornerX += distance;
          // handle the wraparound:
          if(myCornerX > mySpaceWidth) {
            myCornerX -= mySpaceWidth;
          }
          break;
      }
      // now draw all of the graphics onto the next bitmap:
      Bitmap currentBitmap = getNextBitmap();
      Graphics g = new Graphics(currentBitmap);
      // Start by painting the whole region black:
      g.pushRegion(new XYRect(0, 0, myViewportWidth, myViewportHeight));
      g.clear();
      g.setColor(g.FULL_BLACK);
      g.fillRect(0, 0, myViewportWidth, myViewportHeight);
      // Now add the fixed background stars:
      g.setColor(g.FULL_WHITE);
      for(int i = 0; i < myStarField.length/2; i++) {
        g.fillRect(myStarField[2*i], myStarField[2*i+1], 2, 2);
      }
      // Now add the asteriods that are visible nearby:
      int xCoord = 0;
      int yCoord = 0;
      for(int i = 0; i < ASTEROID_COUNT; i+=2) {
        xCoord = myAsteroids[i] - myCornerX;
        if((xCoord < myViewportWidth && xCoord > 0) 
             || (mySpaceWidth + xCoord < myViewportWidth)) {
          yCoord = myAsteroids[i+1] - myCornerY;
          if((yCoord < myViewportHeight && yCoord > 0) 
               || (mySpaceHeight + yCoord < myViewportHeight)) {
            if(xCoord < 0) {
              xCoord += mySpaceWidth;
            }
            if(yCoord < 0) {
              yCoord += mySpaceHeight;
            }
            g.fillArc(xCoord, yCoord, 8, 8, 0, 360);
          }
        }
      }
      //System.out.println("position: (" + myCornerX + ", " + myCornerY + ")");
      // now the alien ships that are visible nearby:
      Enumeration elements = mySpaceships.elements();
      while(elements.hasMoreElements()) {
        Spaceship ship = (Spaceship)(elements.nextElement());
        xCoord = ship.getX() - myCornerX;
        if((xCoord < myViewportWidth && xCoord > 0) 
             || (mySpaceWidth + xCoord < myViewportWidth)) {
          yCoord = ship.getY() - myCornerY;
          if((yCoord < myViewportHeight && yCoord > 0) 
               || (mySpaceHeight + yCoord < myViewportHeight)) {
            if(xCoord < 0) {
              xCoord += mySpaceWidth;
            }
            if(yCoord < 0) {
              yCoord += mySpaceHeight;
            }
            RimSprite alienSprite = ship.getSprite();
            alienSprite.setRefPixelPosition(xCoord, yCoord);
            alienSprite.paint(g);
            ship.postMessage();
          }
        }
      }
      // now paint the rocket on top:
      mySprite.paint(g);
      // since the region was pushed onto the context stack,
      // it must be popped off:
      g.popContext();
      // set the newly-painted bitmap to be visible:
      myBitmapField.setBitmap(currentBitmap);
    } catch(Exception e) {
      // if it doesn't set the bitmap, then the animation didn't advance:
      return false;
    }
    return true;
  }

  /**
   * @see FieldAnimator#play()
   */
  public synchronized void play() {
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
  public synchronized void stop() {
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
   * Handle the user commands.
   * @param direction The input direction (in terms of GameCanvas constants).
   */
  void move(int direction) {
    switch(direction) {
      case UP:
        if(mySpeed < 4) {
          mySpeed++;
        }    
        mySprite.setFrameSequence(Spaceship.FLAME_FRAME_SEQUENCE);
        break;
      case DOWN:
        mySpeed = 0;
        mySprite.setFrameSequence(Spaceship.FRAME_SEQUENCE);
        break;
      case LEFT:
        switch(myDirection) {
          case UP:
            myDirection = LEFT;
            break;
          case DOWN:
            myDirection = RIGHT;
            break;
          case LEFT:
            myDirection = DOWN;
            break;
          case RIGHT:
            myDirection = UP;
            break;
        }    
        break;
      case RIGHT:
        switch(myDirection) {
          case UP:
            myDirection = RIGHT;
            break;
          case DOWN:
            myDirection = LEFT;
            break;
          case LEFT:
            myDirection = UP;
            break;
          case RIGHT:
            myDirection = DOWN;
            break;
        }    
        break;
    }
  }
  
 /**
  * Pass user input to the animated field.
  * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
  */
  public boolean keyChar(char key, int status, int time) {
    if((key == 's') || (key == 'd')) { //left
      move(LEFT);
    } else if((key == 'f') || (key == 'j')) { // right
      move(RIGHT);
    } else if((key == 'e') || (key == 't')) { // up
      move(UP);
    } else if((key == 'x') || (key == 'b')) { // down
      move(DOWN);
    } else {
      // the keystroke was not relevant to this game
      return false;
    }
    // the keystroke was used by this game
    return true;
  }
  
 /**
  * Pass user input to the animated field.
  * @see net.rim.device.api.ui.Screen#navigationMovement(int, int, int, int)
  */
  public boolean navigationMovement(int dx, int dy, int status, int time) {
    if(dx < 0) { //left
      move(LEFT);
    } else if(dx > 0) { // right
      move(RIGHT);
    } else if(dy > 0) { // down
      move(DOWN);
    } else if(dy < 0) { // up
      move(UP);
    } else {
      // the motion was not relevant to this game
      return false;
    }
    // the motion was used by this game
    return true;
  }

}

