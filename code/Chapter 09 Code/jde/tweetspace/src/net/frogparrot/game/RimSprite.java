package net.frogparrot.game;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;

/**
 * This reproduces some of the functionality of the 
 * lcdui Sprite class, for use with RIM Graphics objects.
 * 
 * @author Carol Hamer
 */
public class RimSprite {
    
//---------------------------------------------------------
//   Constants

  public static final int TRANS_NONE = 0;
  public static final int TRANS_ROT90 = 5;
  public static final int TRANS_ROT180 = 3;
  public static final int TRANS_ROT270 = 6;

//---------------------------------------------------------
//   fields

  /**
   * The underlying image and its rotated versions.
   */
  Bitmap myImage;
  Bitmap myImage90;
  Bitmap myImage180;
  Bitmap myImage270;
  
  /**
   * The dimensions describing how to cut the individual Sprite out of its image.
   */
  int myFrameWidth;
  int myFrameHeight;
  int myRowLength;
  int myColumnLength;
  
  /**
   * The reference pixel with respect to the Sprite's internal coordinates.
   */
  int myRefPixelX = 0;
  int myRefPixelY = 0;
  
  /**
   * Where the reference pixel is placed with respect to the external graphics object.
   */
  int myXcoordinate = 0;
  int myYcoordinate = 0;
  
  /**
   * Data to select the correct frame of the correct image to display.
   */
  int myOrientation;
  int[] myFrameSequence;
  int myFrameIndex;
  
//---------------------------------------------------------
//   data initialization and accessors

  /**
   * Calculate the image and frame data.
   * @param url The name of the Image resource.
   * @param rowLength the number of columns the image is divided into
   * @param columnLength the number of rows the image is divided into
   */
  public RimSprite(String url, int rowLength, int columnLength) {
    try {
      myImage = Bitmap.getBitmapResource(url);
      myImage90 = rotateBitmap(myImage);
      myImage180 = rotateBitmap(myImage90);
      myImage270 = rotateBitmap(myImage180);
      myFrameWidth = myImage.getWidth()/rowLength;
      myFrameHeight = myImage.getHeight()/columnLength;
      myRowLength = rowLength;
      myColumnLength = columnLength;
      int animationLength = myRowLength*myColumnLength;
      myFrameSequence = new int[animationLength];
      for(int i = 0; i < animationLength; i++) {
        myFrameSequence[i] = i;
      }
    } catch(Exception e) {
      System.out.println(e);
    }
  }
  
  /**
   * Creates a new Sprite with the same underlying data as an 
   * existing Sprite. This is a memory-saving option to allow
   * multiple Sprites to use the same underlying image files
   * in memory.
   */
  public RimSprite(RimSprite source) {
    myFrameWidth = source.myFrameWidth;
    myFrameHeight = source.myFrameHeight;
    myImage = source.myImage;
    myImage90 = source.myImage90;
    myImage180 = source.myImage180;
    myImage270 = source.myImage270;
    myRowLength = source.myRowLength;
    myColumnLength = source.myColumnLength;
    myFrameSequence = source.myFrameSequence;
    myRefPixelX = source.myRefPixelX;
    myRefPixelY = source.myRefPixelY;
  }
  
  /**
   * Set the order of the frames to create the animation.
   * @param frameSequence An array of indices into the array of frames,
   *   indexed in reading order (left-to-right rows, read from top to bottom).
   */
  public void setFrameSequence(int[] frameSequence) {
    myFrameSequence = frameSequence;
  }
  
  /**
   * Set where (in the Graphics object's coordinate system) the 
   * Sprite's reference pixel is to be painted.
   */
  public void setRefPixelPosition(int x, int y) {
    myXcoordinate = x;
    myYcoordinate = y;
  }
  
  /**
   * @return The x-coordinate of the Sprite's reference pixel with respect
   * to the painter's coordinate system.
   */
  public int getRefPixelX() {
    return myXcoordinate;
  }
  
  /**
   * @return The y-coordinate of the Sprite's reference pixel with respect
   * to the painter's coordinate system.
   */
  public int getRefPixelY() {
    return myYcoordinate;
  }
  
  /**
   * Set the pixel within the Sprite (with respect to the top 
   * left corner of the Sprite) that is used to calculate the 
   * placement of the Sprite for painting.
   */
  public void defineReferencePixel(int x, int y) {
    myRefPixelX = x;
    myRefPixelY = y;
  }
  
  /**
   * Set the center of the Sprite to be the pixel 
   * (within the Sprite, with respect to the top 
   * left corner of the Sprite) that is used to calculate the 
   * placement of the Sprite for painting.
   */
  public void centerReferencePixel() {
    myRefPixelX = myFrameWidth/2;
    myRefPixelY = myFrameHeight/2;
  }
  
  /**
   * Set the desired orientation of the Sprite image.
   */
  public void setTransform(int orientation) {
    myOrientation = orientation;
  }
  
//---------------------------------------------------------
//   business methods

/**
 * A simple utility to take a bitmap image and 
 * return a copy of the image, rotated by 90 degrees.
 * @param src <description>
 * @return <description>
 */
  public static Bitmap rotateBitmap(Bitmap src) {
    Bitmap retObj = null;
    try {
      int imageWidth = src.getWidth();
      int imageHeight = src.getHeight();
      //System.out.println("src image: " + src);
      int[] dataSrc = new int[imageWidth*imageHeight];
      src.getARGB(dataSrc, 0, imageWidth, 0, 0, imageWidth, imageHeight);
      int[] dataDst = new int[dataSrc.length];
      for(int i = 0; i < imageWidth; i++) {
        for(int j = 0; j < imageHeight; j++) {
          dataDst[(imageWidth - i - 1)*imageHeight + j] 
              = dataSrc[j*imageWidth + i];
        }
      }
      retObj = new Bitmap(imageHeight, imageWidth);
      retObj.setARGB(dataDst, 0, imageHeight, 0, 0, imageHeight, imageWidth);
    } catch(Exception e) {
      System.out.println("rotate bitmap: " + e);
    }
    //System.out.println("rotate bitmap returning: " + retObj);
    return retObj;
  }
  
  /**
   * Paint the Sprite onto the target that is bound to the Graphics object.
   */
  public void paint(Graphics g) {
    myFrameIndex++;
    myFrameIndex %= myFrameSequence.length;
    //System.out.println("myFrameIndex: " + myFrameIndex);
    // compute which part of the image to paint,
    // based on the orientation and the frame index:
    int frame = myFrameSequence[myFrameIndex];
    int gridX = frame % myRowLength;
    int gridY = frame / myRowLength;
    switch(myOrientation) {
      case TRANS_NONE:
        g.drawBitmap(myXcoordinate - myRefPixelX, 
                     myYcoordinate - myRefPixelY, 
                     myFrameWidth, myFrameHeight, 
                     myImage, gridX*myFrameWidth, gridY*myFrameHeight);
      break;
      case TRANS_ROT90:
        g.drawBitmap(myXcoordinate - myRefPixelY, 
                     myYcoordinate - (myFrameWidth - myRefPixelX), 
                     myFrameHeight, myFrameWidth, 
                     myImage90, 
                     gridY*myFrameHeight, 
                     (myRowLength - gridX - 1)*myFrameWidth);
      break;
      case TRANS_ROT180:
        g.drawBitmap(myXcoordinate - (myFrameWidth - myRefPixelX), 
                     myYcoordinate - (myFrameHeight - myRefPixelY), 
                     myFrameWidth, myFrameHeight, 
                     myImage180, 
                     (myRowLength - gridX - 1)*myFrameWidth, 
                     (myColumnLength - gridY - 1)*myFrameHeight);
      break;
      case TRANS_ROT270:
        g.drawBitmap(myXcoordinate - (myFrameHeight - myRefPixelY), 
                     myYcoordinate - myRefPixelX,
                     myFrameHeight, myFrameWidth, 
                     myImage270, 
                     (myColumnLength - gridY - 1)*myFrameHeight, 
                     gridX*myFrameWidth);
      break;
    }
  }

}

