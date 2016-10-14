
// PlayerLoc.java
// Andrew Davison, Nov. 2009, ad@fivedots.coe.psu.ac.th

/* Store information about a single player:
      the ID letter (H or F) , the current (x,y) location on the map, 
      and if alive (true or false);

   The images used for displaying the players are loaded by the class
*/

import java.util.*;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;


public class PlayerLoc
{
  private static final String IMAGES_DIR = "images/";

  // bitmaps used when drawing the players -- loaded by the class
  private static final Bitmap foxAlive = loadImage("foxAlive.png");
  private static final Bitmap foxDead = loadImage("foxDead.png");
  private static final Bitmap houndAlive = loadImage("dogAlive.png");
  private static final Bitmap houndDead = loadImage("dogDead.png");

  private static final int IM_WIDTH = 25;   // size of all the images
  private static final int IM_HEIGHT = 25;

  // player types
  private static final int FOX = 0;
  private static final int HOUND = 1;

  private int playerType;
  private int x = -1;
  private int y = -1;
  private boolean isAlive;


  public PlayerLoc(String playerInfo)
  { 
    Vector words = Utils.split(playerInfo, " ");    
      // split up the player info string based based on its spaces

    if (words.size() == 4) {    // ID-letter x y alive
      String typeStr = (String) words.elementAt(0);
      playerType = typeStr.trim().equals("F") ? FOX : HOUND;

      try {
        x = Integer.parseInt( (String)words.elementAt(1) );
        y = Integer.parseInt( (String)words.elementAt(2) );
      }
      catch (NumberFormatException e) {}

      String aliveStr = (String) words.elementAt(3);
      isAlive = aliveStr.trim().equals("true") ? true : false;
    }
  }  // end of PlayerLoc()

  

  public void draw(Graphics g, int screenWidth, int screenHeight,
                               int xDraw, int yDraw)
  // draw the player using one of the player images
  {  
    // System.out.println("PlayerLoc draw() 1: " + x + ", " + y);
    if ((x == -1) || (y == -1))   // error in location info
      return;

    // calculate drawing position on the screen of the top-left corner of the image
    int xPos = x + xDraw - IM_WIDTH/2;
    int yPos = y + yDraw - IM_HEIGHT/2;
    // System.out.println("PlayerLoc draw() 2: " + xPos + ", " + yPos);

    if ((xPos < -IM_WIDTH) || (xPos > screenWidth) ||
        (yPos < -IM_WIDTH) || (yPos > screenHeight))   // location not visible
      return;

    // select image for the player
    Bitmap im = null;
    if (playerType == FOX)
      im = isAlive ? foxAlive : foxDead;
    else if (playerType == HOUND)
      im = isAlive ? houndAlive : houndDead;

    if (im != null)
      g.drawBitmap(xPos, yPos, IM_WIDTH, IM_HEIGHT, im, 0, 0); 

  } // end of draw()



  private static Bitmap loadImage(String fnm)
  // static image loading carried out by the class
  {
    Bitmap im = null;
    System.out.println("Loading image in " + IMAGES_DIR+fnm);
    try {
      im = Bitmap.getBitmapResource(fnm);
    }
    catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
    if (im == null) {
      System.out.println("Image is empty");
      System.exit(1);
    }
    return im;
  }  // end of loadImage()

}  // end of PlayerLoc class
