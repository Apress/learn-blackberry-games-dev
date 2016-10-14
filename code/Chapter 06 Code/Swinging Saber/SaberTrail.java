
// SaberTrail.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/*  The 'trail' of the saber rotates a little behind the main
    light saber sprite. It's coordinates are based on the main saber
    but widened along the x-axis using widthFactor, and a different
    color.
*/

import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;
import net.rim.device.api.math.*;
import net.rim.device.api.util.MathUtilities;

import java.util.*;


public class SaberTrail
{
  private int[] xsSaber, ysSaber;     // coords of the saber
  private int[] xs, ys;       // coords of the rotated saber                             

  private XYPoint pivot;   // rotation point
  private int color;


  public SaberTrail(int[] xsCore, int[] ysCore, XYPoint pvt,
                                         double widthFactor, int col)
  {
    pivot = pvt;
    color = col;
        
    // trail's coordinates are based on the main saber's coordinates
    int numPts = xsCore.length;                     
    xsSaber = new int[numPts];
    ysSaber = new int[numPts];
    for (int i=0; i < numPts; i++) {
      xsSaber[i] = (int)(xsCore[i]*widthFactor); // widen x-dimensions
      ysSaber[i] = ysCore[i];    // y-dimensions are unchanged
    }

    // rotated saber coords are the saber coords initially
    xs = new int[numPts];
    ys = new int[numPts];
    for (int i=0; i < numPts; i++) {
      xs[i] = pivot.x + xsSaber[i];
      ys[i] = pivot.y - ysSaber[i];
    }
  }  // end of SaberTrail()



  public void setRotation(int angle)
  {  rotateCoords(angle, xsSaber, ysSaber, xs, ys);  } 



  private void rotateCoords(int angle, int[] xsOrig, int[] ysOrig, int[] xsNew, int[] ysNew)
  // rotate the original (x,y) coords to get the new (x,y) values
  {
    int angleFP = Fixed32.toFP(angle-90);   // since the saber is vertical at the start
    
    int dux = Fixed32.cosd(angleFP);
    int dvx = -Fixed32.sind(angleFP);
    int duy = Fixed32.sind(angleFP);         
    int dvy = Fixed32.cosd(angleFP);    

    for(int i=0; i < xsOrig.length; i++) {
      xsNew[i] = pivot.x + Fixed32.toInt( Fixed32.round( dux*xsOrig[i] + 
                                                         dvx*ysOrig[i]));       
      ysNew[i] = pivot.y - Fixed32.toInt( Fixed32.round( duy*xsOrig[i] + 
                                                         dvy*ysOrig[i]));
    }                                                     
  }  // end of rotateCoords()    


  public void draw(Graphics g)
  {
    g.setColor(color);
    g.drawFilledPath(xs, ys, null, null);
  } 

}  // end of SaberTrail class
