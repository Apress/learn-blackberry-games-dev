
// LightSaber.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* The light saber consists of two polygons and several 'trails'. There is a polygon
   representing the light part of the saber (xsSaber and ysSaber arrays) and a polygon
   for its handle (the xsHandle and ysHandle arrays). 
   
   The trails are copies of the light
   saber which rotate a bit behind the main light saber to give the impression
   of blur and speed :) The trails are represented by SaberTrail objects.
   
   The saber (light, handle, and trails) rotate around a pivot point, and also include 
   a 'tip' point which is used to detect collisions with the sides of the screen, and a
   line (two points: pivot -- tip) which is used for detecting hits by blast sprites.
*/

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;
// import net.rim.device.api.ui.decor.*;
import net.rim.device.api.math.*;
import net.rim.device.api.util.MathUtilities;

import java.util.*;


public class LightSaber
{
  private static final int SABER_COLOR = 0x0000FF;    // bright blue

  private static final double[] TRAIL_WIDTH_FACTORS = new double[] {1.1, 1.4, 1.7};
                     // the trails get gradually wider
  private static final int[] TRAIL_COLORS = new int[] {0x6363FF, 0xA5A5FF, 0xD8D8FF};
                     // the trails become lighter blue
                     
  private static final int ROTATE_STEP = 5;     // in degrees
  private static final int MIN_ROTATE = 30;     
  private static final int MAX_ROTATE = 150;

  private static final int SABER_WIDTH = 15;   // used when testing for wall collisions


  // dimensions of the screen (panel)
  private int pWidth = Display.getWidth();
  private int pHeight = Display.getHeight();

  // coordinates of the light saber beam (not including the handle)
  private int[] xsSaber = new int[] {0,   3,    7,  7,   -7, -7, -3};
  private int[] ysSaber = new int[] {267, 265, 260, 20,  20, 260, 265};
     /* the first point is the tip of the saber, and the other points define the 
        polygon in a clockwise order. (0,0) is the center of the saber's base. */
  
  // coordinates of the rotated saber                             
  private int[] xs, ys;
  
  // coordinates of the saber handle
  private int[] xsHandle = 
       new int[] {-10, 10, 10,  7,   7,  10,  10,  5,    0,  -5, -10, -10, -7,  -7, -10};
  private int[] ysHandle = 
       new int[] { 20, 20, 14, 14, -24, -24, -30, -35, -37, -35, -30, -24, -24, 14,  14};
          /* start with the top-left point of the handle, and the other points define
             the polygon in a clockwise order. The top of the handle touches the base of
             the light saber. */
  private int[] handleColors = 
     new int[] { 0x808080, 0xDCDCDC, 0xDCDCDC, 0xDCDCDC, 0xDCDCDC, 
                 0xDCDCDC, 0xDCDCDC, 0xDCDCDC, 0xDCDCDC, 0x808080, 
                 0x808080, 0x808080, 0x808080, 0x808080, 0x808080, };
                      // dark gray to light gray (going from left to right side)
                      
  // coordinates of the rotated saber handle                         
  private int[] xsH, ysH;


  // points and lines on the light saber
  private XYPoint pivot, saberTip, rotatedTip;
  private XYPoint[] saberLine;   // used to detect collisions from blast sprites

  // rotation information
  private volatile int clickAngle = 90;    // the saber starts by pointing straight up
  private int currRotAngle = 90;
  private int[] prevRotations;   // used by the saber trails

  private SaberTrail[] saberTrails;     



  public LightSaber()
  {
    // inialise the saber points
    pivot = new XYPoint(pWidth/2, pHeight-30);    // in the center, near the bottom of the screen
    saberTip = new XYPoint( xsSaber[0], ysSaber[0]-5);   // a little below the top of the saber
    rotatedTip = new XYPoint(pivot.x+saberTip.x, pivot.y-saberTip.y);
       // the tip coordinates, mapped to screen coordinates by using the pivot
    
    // line used for blast collision detection
    saberLine = new XYPoint[2];
    saberLine[0] = pivot;    // the line goes from from the pivot to the tip
    saberLine[1] = rotatedTip;
    
    
    // rotated saber coords are the saber coords initially
    int numPts = xsSaber.length;                     
    xs = new int[numPts];
    ys = new int[numPts];
    for (int i=0; i < numPts; i++) {
      xs[i] = pivot.x + xsSaber[i];
      ys[i] = pivot.y - ysSaber[i];
    }
  
    // rotated handle coords are the handle coords initially
    numPts = xsHandle.length;                     
    xsH = new int[numPts];
    ysH = new int[numPts];
    for (int i=0; i < numPts; i++) {
      xsH[i] = pivot.x + xsHandle[i];
      ysH[i] = pivot.y - ysHandle[i];
    }

    // initialize the saber trails
    saberTrails = new SaberTrail[TRAIL_WIDTH_FACTORS.length];
    for (int i=0; i < TRAIL_WIDTH_FACTORS.length; i++) 
      saberTrails[i] = new SaberTrail(xsSaber, ysSaber, pivot,
                            TRAIL_WIDTH_FACTORS[i], TRAIL_COLORS[i]);
          // use the saber coordinates, but widened and differently coloured
                                         
    prevRotations = new int[saberTrails.length];                      
  }  // end of LightSaber()



  public XYPoint getTip()
  {  return rotatedTip; }


  public XYPoint[] getLine()
  { return saberLine;   }


  public boolean isTouchingWall()
  // is the tip of the saber touching the left of right sides of the screen?
  {
    if ((rotatedTip.x <= SABER_WIDTH) || (rotatedTip.x >= pWidth-SABER_WIDTH))
      return true;
    return false;
  }  // end of isTouchingWall()



  // --------------------- movement methods --------------------------------------
  // various methods for changing the clickAngle variable
  

  public void setClickAngle(int xClick, int yClick)
  // convert a touch click into an angle relative to the pivot
  { 
    if (yClick >= pivot.y)  // ignore clicks below the pivot point
      return;

    int dy = pivot.y - yClick;
    int dx = xClick - pivot.x;
    clickAngle = (int) Math.toDegrees( MathUtilities.atan2(dy, dx));
    // System.out.println("setClickAngle(): clickangle:" + clickAngle);
  }  // end of setClickAngle()


  public void move(int dx)
  /* Convert trackball movement in the x-direction into a click angle;
     dx is negative for a move left and positive for a move right. */
  {
    if (dx < 0)  // move left
      clickAngle += (2*ROTATE_STEP);
    else if (dx > 0)  // move right
      clickAngle -= (2*ROTATE_STEP);
    limitAngle();
  }  // end of move()


  public void shake(short xAccel)
  /* Convert accelerometer x-axis information into a click angle.
     Relative to the ground, a x-axis negative value mean east, while 
     positive is west. See the AccelerometerSensor API doc. */
  {
    if (xAccel < 0)        // movement east
      clickAngle -= (2*ROTATE_STEP);
    else if (xAccel > 0)   // movement west
      clickAngle += (2*ROTATE_STEP);
    limitAngle();
  }  // end of shake()
  
  
  private void limitAngle()
  // dont let clickAngle go outside a specified min-max range
  {
    if (clickAngle < MIN_ROTATE)
      clickAngle = MIN_ROTATE;
    else if (clickAngle > MAX_ROTATE)
      clickAngle = MAX_ROTATE;
  }  // end of limitAngle()
    


  // -------------------------- updating -------------------------


  public void update()
  // update the saber rotation
  {
    updateTrails();

    if (!stopRotation()) {   // update the saber's rotation angle
      if (clickAngle < currRotAngle)
        currRotAngle -= ROTATE_STEP;   // move right
      else
        currRotAngle += ROTATE_STEP;   // move left
    }

    // rotate the saber and handle
    rotateCoords(xsSaber, ysSaber, xs, ys);
    rotateCoords(xsHandle, ysHandle, xsH, ysH);
    
    rotateTip();       // rotate the saber tip
  } // end of update()


  private void updateTrails()
  {
    /* shift rotations in prevRotations[] to the right, and store 
       the current rotation (which is about to change) into the 
       0th box. */
    for (int i=saberTrails.length-2; i >= 0; i--)
      prevRotations[i+1] = prevRotations[i];
    prevRotations[0] = currRotAngle;

    // update saber trails rotations with prevRotations[]
    for (int i=0; i < saberTrails.length; i++)
      saberTrails[i].setRotation(prevRotations[i]);
  }  // end of updateTrails()




  private boolean stopRotation()
  // test for conditions that stop the rotation
  {
    // don't rotate the saber beyond left or right sides
   
    if ((rotatedTip.x <= SABER_WIDTH) && (clickAngle > currRotAngle))
      return true;    // since on saber is on the left and moving left

    if ((rotatedTip.x >= pWidth-SABER_WIDTH) && (clickAngle < currRotAngle))
      return true;   // since saber is on the right and moving right

    // stop rotating the saber when it's angle is close enough to the click angle
    if ( Math.abs(currRotAngle-clickAngle)< ROTATE_STEP)
      return true;

    return false;   // don't stop rotating
  }  // end of stopRotation



  private void rotateCoords(int[] xsOrig, int[] ysOrig, int[] xsNew, int[] ysNew)
  // rotate the original (x,y) coords to get the new (x,y) values
  {
    int angleFP = Fixed32.toFP(currRotAngle-90);   // -90 since the saber is vertical at the start
    
    // calculate x and y change due to the angle change
    int dux = Fixed32.cosd(angleFP);
    int dvx = -Fixed32.sind(angleFP);
    int duy = Fixed32.sind(angleFP);         
    int dvy = Fixed32.cosd(angleFP);    

    // update the coordinates, and map them to screen coordinates by using the pivot
    for(int i=0; i < xsOrig.length; i++) {
      xsNew[i] = pivot.x + Fixed32.toInt( Fixed32.round( dux*xsOrig[i] + dvx*ysOrig[i]));         
      ysNew[i] = pivot.y - Fixed32.toInt( Fixed32.round( duy*xsOrig[i] + dvy*ysOrig[i]));
    }                                                     
  }  // end of rotateCoords()    



  private void rotateTip()
  {
    int angleFP = Fixed32.toFP(currRotAngle-90);   // since saber is vertical at start
    
    // calculate x and y change due to the angle change
    int dux = Fixed32.cosd(angleFP);
    int dvx = -Fixed32.sind(angleFP);
    int duy = Fixed32.sind(angleFP);         
    int dvy = Fixed32.cosd(angleFP);    

    // update the coordinates, and map them to screen coordinates by using the pivot
    int x = pivot.x + Fixed32.toInt( Fixed32.round( dux*saberTip.x +dvx*saberTip.y));
    int y = pivot.y - Fixed32.toInt( Fixed32.round( duy*saberTip.x +dvy*saberTip.y));

    rotatedTip.set(x,y);
  }  // end of rotateTip    



  // -------------------------------- drawing -----------------------------
  
  
  public void draw(Graphics g)
  {
    // draw saber trails
    for (int i = saberTrails.length-1; i >= 0; i--)  // draw in reverse order
      saberTrails[i].draw(g);

    // draw the rotated saber tip (useful when debugging)
    // g.setColor(0xFFFFFF);    // white
    // g.fillArc(rotatedTip.x, rotatedTip.y, 8, 8, 0, 360);

    // draw the rotated saber line
    g.setColor(SABER_COLOR);
    g.drawFilledPath(xs, ys, null, null);
    
    // draw the rotated shaded handle
    g.drawShadedFilledPath( xsH, ysH, null, handleColors, null);
  }  // end of draw()


}  // end of LightSaber class
