package net.frogparrot.tweetspace;

import net.rim.device.api.util.StringUtilities;

import net.frogparrot.game.RimSprite;

/**
 * This class represents the characters you can
 * meet in cyberspace.
 * 
 * @author Carol Hamer
 */
public class Spaceship {
  
//---------------------------------------------------------
//  static fields

/**
 * The names of the image files that can be used as spaceship sprites.
 */
 public static final String[] SPACESHIP_IMAGES = { 
      "rocket-indexed-48.png", 
      "leo-indexed-43.png", 
      "scooter-indexed-66.png", 
      "martian-indexed-60.png",
      "rocket-indexed-96.png", 
      "nico-indexed-51.png", 
      };
 
 /**
  * The frame sequences for the animations.
  */
  public static final int[] FRAME_SEQUENCE = { 3, 2, 1, 0 };
  public static final int[] FLAME_FRAME_SEQUENCE = { 7, 6, 5, 4 };

 /**
  * A set of Sprites (representing each possible spaceship image)
  * so that extra copies of each image aren't created when the images
  * are re-used for multiple ships.
  */
  static RimSprite[] theSprites = new RimSprite[SPACESHIP_IMAGES.length];
  
  /**
   * The message field that displays the remote players' messages to the user.
   */
  static TweetField theTweetField;

//---------------------------------------------------------
//  instance fields

  /**
   * The screen name of this spaceship's player.
   */
  String myName;
  
  /**
   * The Sprite.
   */
  RimSprite mySprite;
  
  /**
   * The most recent message sent by this spaceship.
   */
  String myMessage;
  
  /**
   * This ship's coordinates in space.
   */
  int myX;
  int myY;
    
//---------------------------------------------------------
//  static data methods

  /**
   * Sets the message screen that all of the ships write to.
   */
  public static void setTweetField(TweetField field) {
    theTweetField = field;
  }

  /**
   * Returns the cached Sprite corresponding to the id.
   */
  public static RimSprite getSprite(int id) {
    //Main.setMessage("getSprite: " + id);
    if(theSprites[id] == null) {
      //Main.setMessage("loading: " + SPACESHIP_IMAGES[id]);
      theSprites[id] = new RimSprite(SPACESHIP_IMAGES[id], 4, 2);
      //Main.setMessage("loaded");
      theSprites[id].centerReferencePixel();
      theSprites[id].setFrameSequence(FRAME_SEQUENCE);
    }
    //Main.setMessage("cloning: " + theSprites[id]);
    // this creates a clone that points to the existing Image in memory
    // (without creating a new copy of the underlying Image files)
    return new RimSprite(theSprites[id]);
  }

  /**
   * Returns the cached Sprite corresponding to the id String.
   */
  public static RimSprite getSprite(String idStr) {
    //Main.setMessage("getSprite: " + idStr);
    try {
      int id = Integer.parseInt(idStr);
      return getSprite(id);
    } catch(Exception e) {
      Main.postException(e);
    }
    // if there's anything wrong with the request,
    // just return the default sprite:
    return getSprite(0);
  }
  
  /**
   * Clear the cache.
   */
   public static void clear() {
     for(int i = 0; i < theSprites.length; i++) {
       theSprites[i] = null;
     }
     theTweetField = null;
   }

//---------------------------------------------------------
//  instance data initialization and access

  /**
   * Create a new Sprite based on a tweet from a remote user.
   * @param name the remote user's screen name
   * @param tweet the text containing the message and the coordinates.
   * @param id which sprite image to use.
   */
  Spaceship(String name, String tweet) {
    myName = name;
    // parse the message to interpret and set the ship's data:
    update(tweet);
    //Main.setMessage("updated");
  }
  
  /**
   * parse the message to interpret and set the ship's data.
   * @param tweet the text containing the message and the coordinates.
   */
  public void update(String tweet) {
    try {
      Main.setMessage("parsing: " + tweet);
      // first extract and interpret the position data:
      int startIndex = tweet.indexOf('*');
      int endIndex = tweet.indexOf('*', startIndex + 1);
      Main.setMessage("indices: " + startIndex + ", " + endIndex);
      String infoStr = tweet.substring(startIndex, endIndex);
      Main.setMessage("didn't throw->indices good!");
      // use RIM's string utilities to split the data into an array of Strings
      String[] dst = new String[4];
      StringUtilities.stringToWords(infoStr, dst, 0);
      Main.setMessage("parsing: " + infoStr);
      // Save the user's message for display:
      synchronized(this) {
        myMessage = tweet.substring(endIndex + 1);
      }
      myX = Integer.parseInt(dst[0]);
      Main.setMessage("X coord: " + myX);
      myY = Integer.parseInt(dst[1]);
      Main.setMessage("Y coord: " + myY);
      if(mySprite == null) {
        mySprite = getSprite(dst[3]);
      }
      Main.setMessage("got sprite: " + mySprite);
      if("d".equals(dst[2])) {
        mySprite.setTransform(RimSprite.TRANS_ROT180);
      } else if("l".equals(dst[2])) {
        mySprite.setTransform(RimSprite.TRANS_ROT90);
      } else if("r".equals(dst[2])) {
        mySprite.setTransform(RimSprite.TRANS_ROT270);
      } else {
        mySprite.setTransform(RimSprite.TRANS_NONE);
      }
      //Main.setMessage("orientation: " + dst[2]);
    } catch(Exception e) {
      // if parsing fails, throw the message out:
      Main.postException(e);
      throw new IllegalArgumentException();
    }
  }
  
  /**
   * @return The ship's X-coordinate in space.
   */
  public int getX() {
    return myX;
  }
  
  /**
   * @return The ship's Y-coordinate in space.
   */
  public int getY() {
    return myY;
  }
  
  /**
   * @return The ship's sprite.
   */
  public RimSprite getSprite() {
    return mySprite;
  }
  
  /**
   * Get (and clear) the most recent message to display.
   * @return The remote user's most recent message.
   */
  public synchronized String getMessage() {
    String retString = myMessage;
    myMessage = null;
    return retString;
  }
  
//---------------------------------------------------------
//  business methods

  /**
   * Tell the message field to display the remote user's message.
   */
  public void postMessage() {
    // clear the message when getting it so that it will display only once:
    String message = getMessage();
    if(message != null) {
      theTweetField.displayRemoteMessage(myName, message);
    }
  }
  
} 
