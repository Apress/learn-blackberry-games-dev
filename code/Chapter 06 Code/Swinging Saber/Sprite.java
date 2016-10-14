
// Sprite.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* A Sprite has a position, velocity (in terms of steps),
   an image, and can be deactivated.
*/

import java.util.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;


public class Sprite 
{
  protected static final String IMAGES_DIR = "images/";

  // default step sizes (how far to move in each update)
  private static final int STEP = 3; 

  // default dimensions when there is no image
  private static final int SIZE = 8;   


  private boolean isActive = true;      
  // a sprite is updated and drawn only when it is active

  // protected vars
  protected Bitmap image =  null;
  protected int pWidth, pHeight;   // screen dimensions
  protected int locx, locy;        // location of sprite
  protected int dx, dy;            // step to move in each update
  protected int width, height;     // sprite dimensions



  public Sprite(String fnm) 
  { 
    pWidth = Display.getWidth();
    pHeight = Display.getHeight();

    loadImage(fnm);

    // start in center of screen by default
    locx = pWidth/2; 
    locy = pHeight/2;

    dx = STEP; dy = STEP;
  } // end of Sprite()



  private void loadImage(String fnm)
  {
    image = Bitmap.getBitmapResource(IMAGES_DIR + fnm);
    if (image != null) {
      // System.out.println("Loaded " + fnm);
      width = image.getWidth();
      height = image.getHeight();
    }
    else {
      System.out.println("Could not load image from " + fnm);
      width = SIZE;  
      height = SIZE;
    }
  }  // end of loadImage()


  public void setImage(Bitmap im)
  {
    if (im != null) {
      // System.out.println("Loaded " + fnm);
      image = im;
      width = image.getWidth();
      height = image.getHeight();
    }
    else
      System.out.println("Image is empty");
  }  // end of setImage()



  public boolean isActive() 
  {  return isActive;  }

  public void setActive(boolean a) 
  {  isActive = a;  }


  public void setPosition(int x, int y)
  {  locx = x; locy = y;  }

  public int getX()
  {  return locx;  }

  public int getY()
  {  return locy;  }

  public void setStep(int x, int y)
  {  dx = x; dy = y;  }


  // -------------- collision detection methods ----------------------

  public boolean hasHit(Sprite sprite)
  {
    if (!isActive)
      return false;
    if (!sprite.isActive())
      return false;

    XYRect thisRect = getRect();
    XYRect spriteRect = sprite.getRect();
    if (thisRect.intersects(spriteRect))
      return true;
    return false;
  }  // end of hasHit()



  public boolean hasReachedFloor()
  {
    if (isActive() && ((locy + height) >= pHeight) )
      return true;
    return false;
  }  // end of hasReachedFloor()


  public XYRect getRect()
  {  return  new XYRect(locx, locy, width, height);  }



  // -------------- basic update and draw ----------------------

  public void update()
  // move the sprite
  { if (isActive()) {
      locx += dx;
      locy += dy;
    }
  } // end of update()


  public void draw(Graphics g) 
  {
    if (isActive()) {
      if (image == null) {   // the sprite has no image
        g.setColor(Color.YELLOW);   // draw a yellow circle instead
        g.fillArc(locx, locy, SIZE, SIZE, 0, 360);
        g.setColor(Color.BLACK);
      }
      else
        g.drawBitmap(locx, locy, width, height, image, 0, 0);
    }
  } // end of draw()

}  // end of Sprite class
