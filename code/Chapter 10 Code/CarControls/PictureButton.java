
// PictureButton.java
// Andrew Davison, August 2009, ad@fivedots.coe.psu.ac.th

/* A field that displays either an 'active' image when pressed, or
   an 'inactive' image the rest of the time.

   While the button is being pressed, it plays a sound clip.

   Both a button press and release trigger calls to dirStatus() in 
   the top-level controller, which sends messages to the Bluetooth server.
*/


import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;



public class PictureButton extends Field
{
  private final static String IM_DIR = "images/";

  private CarControls top;
  private ClipsPlayer player;
  private String buttonName;  // name of the button and its sound clip
  
  private boolean isPressed = false;
  private Bitmap inActiveIm, activeIm;



  public PictureButton(CarControls pb, ClipsPlayer p, String nm)
  {
    super(Field.FOCUSABLE);    // combination of field style bits to specify display
    
    top = pb;
    player = p;
    buttonName = nm;
    
    activeIm = Bitmap.getBitmapResource(IM_DIR + buttonName + "On.png");
    inActiveIm = Bitmap.getBitmapResource(IM_DIR + buttonName + "Off.png");
    player.load(buttonName);
  }  // end of PictureButton()


  public int getPreferredHeight()
  {  return activeIm.getHeight();  }

  public int getPreferredWidth()
  {  return activeIm.getWidth();  }


  protected void layout(int width, int height)
  // Say how big we would like this field to be
  { setExtent( Math.min(width, getPreferredWidth()),
               Math.min(height, getPreferredHeight()) );
  }


  protected void paint(Graphics graphics)
  // repaint the field 
  {
    int w = getWidth();
    int h = getHeight();

    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, w, h);   // use a white background

    if (isPressed)
      graphics.drawBitmap(0, 0, w, h, activeIm, 0, 0);    // show active image
    else   // not pressed
      graphics.drawBitmap(0, 0, w, h, inActiveIm, 0, 0);  // show inactive image

    graphics.setColor(Color.BLUE);
    graphics.drawRect(0, 0, w, h);  // outline the image
    
    top.dirStatus(buttonName, isPressed);  // tell the top-level
  }  // end of paint()


  // ------------ methods for navigation clicks ----------------


  protected boolean navigationClick(int status, int time)
  { isPressed = true;
    player.loop(buttonName);
    invalidate();
    return true;
  }

  protected boolean navigationUnclick(int status, int time)
  { isPressed = false;
    player.stop(buttonName);
    invalidate();
    return true;
  }

}  // end of PictureButton class
