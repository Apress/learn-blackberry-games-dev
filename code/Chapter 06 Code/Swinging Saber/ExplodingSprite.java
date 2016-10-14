
// ExplodingSprite.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* A sprite which can be be 'blown up'. It's normal image can
   be changed to a series of explosion images, which are changed
   over a few updates. When the images have all been displayed,
   the sprite is made inactive (and so disappears).
   
   An explosions image explIms is read from a single file, and split
   into multiple images at load time. The number of images in
   the explIms are supplied with the filename.
*/


import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;


public class ExplodingSprite extends Sprite
{
  private boolean isExploding = false;
  private int explodingCounter;   // specifies which explosion image to display
  private Bitmap normalIm;    // the sprite's normal image
  private Bitmap[] explIms;   // stores the explosion images


  public ExplodingSprite(String fnm, String explFnm, int numImages) 
  { 
    super(fnm);  
    normalIm = image;  // backup initial sprite image
    loadImagesStrip(explFnm, numImages);

    setStep(0, 0); 
    setActive(false);
  }  // end of ExplodingSprite()


  private void loadImagesStrip(String fnm, int numImages) 
  /* Extract individual images from the explIms image file, <fnm>.
     I've assumed the images are stored in a single row, and that there
     are <numImages> of them. The images are stored in explIms[], an array of
     Bitmaps. Each image can have an alpha channel.
  */
  {
    System.out.println("Loading image strip from " + IMAGES_DIR+fnm);
    Bitmap stripIm = Bitmap.getBitmapResource(IMAGES_DIR + fnm);
    if (stripIm == null) {
      System.out.println("Image strip is empty");
      System.exit(1);
    }

    int imWidth = (int) stripIm.getWidth() / numImages;
    int imHeight = stripIm.getHeight();

    explIms = new Bitmap[numImages];
    int[] picData = new int[imWidth * imHeight];
    Bitmap newIm;
    Graphics stripG2D;
 
    // each Bitmap in the explIms is stored in explIms[]
    for (int i=0; i < numImages; i++) {
      newIm = new Bitmap(imWidth, imHeight);
      newIm.createAlpha(Bitmap.ALPHA_BITDEPTH_8BPP);   // allow image to have an alpha channel
      newIm.setARGB(picData, 0, imWidth, 0, 0, imWidth, imHeight);
      stripG2D = Graphics.create(newIm); 

      // draw image from explIms into offscreen buffer
      stripG2D.drawBitmap(0, 0, imWidth, imHeight, stripIm, i*imWidth, 0);            
      // stripG2D.dispose();
      explIms[i] = newIm;
    } 
  } // end of loadImagesStrip()



  public void shoot(int x, int y, int xStep, int yStep)
  // start the sprite moving
  {
    if (!isActive()) {
      setImage(normalIm);
      setPosition(x, y);
      setStep(xStep, yStep); 
      setActive(true);
    }
  }  // end of shoot()


  
  public void explodeAt(XYPoint pt)
  // make the sprite explode at the specified point
  {
    // calculate the sprite location so that pt is its center
    XYRect rect = getRect();
    int locx = pt.x - rect.width/2;
    int locy = pt.y - rect.height/2;
    // System.out.println("(locx,locy): (" + locx + ", " + locy + ")");

    setPosition(locx, locy);
    setActive(true);
    explode();
  }  // end of explodeAt()



  public void explode()
  // make the sprite explode
  {
    isExploding = true;
    explodingCounter = 0;   // start with the first exploson image
    setStep(0, 0);   // the sprite stops moving
  }  // end of explode()


  public boolean isExploding()
  {  return isExploding;  }



  public void update()
  {
    if (isExploding) {
      if (explodingCounter == explIms.length) {   // end of explosion
        isExploding = false;
        setActive(false);   // the sprite disappears
      }
      else {
        setImage(explIms[explodingCounter]);
        explodingCounter++;   // show the next explosion image next time
      }
    }
    super.update();
  }  // end of update()
  

}  // end of ExplodingSprite class

