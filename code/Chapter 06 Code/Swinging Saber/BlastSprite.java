
// BlastSprite.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* A blast sprite drops from a random point on the top of the screen.
   If it hits the light saber (actually a line running from the saber's
   pivot point to its tip) then the sprite 'explodes'. If the sprite
   reaches the floor without exploding then it is deactivated, and can be
   restarted.
   
   The sprite drops at a semi-random speed based on the YSTEP value
*/


import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.math.*;
import java.util.*;



public class BlastSprite extends ExplodingSprite
{
  private static final int YSTEP = 14;    // max vertical dropping speed

  private Random rand = new Random(); 
 

  public BlastSprite()
  {  super("blast.png", "explosion.png", 5);  } 


  public void drop()
  // start somewhere along top edge, at a semi-random vertical step speed 
  {
    int yStep = YSTEP/2 + rand.nextInt(YSTEP/2);  
    super.shoot( rand.nextInt(pWidth), 0, 0, yStep); 
  }  // end of drop()



  public void update()
  {
    if (!isExploding()) {
      if (locy > pHeight)   // if dropped through floor
        setActive(false);
    }
    super.update();
  }  // end of update()
  


  public boolean hasHitSaber(LightSaber saber)
  // has the blast hit the light saber?
  {
    XYRect r = getRect();    // rectangle for sprite
    XYPoint[] ln = saber.getLine();   // saber line
    
    /* check intersection of 4 line segments of rectangle against 
      saber line segment */
    if (VecMath.intersects(ln[0].x, ln[0].y, ln[1].x, ln[1].y,
                           r.x, r.y, (r.x + r.width), r.y) ||   // top
        VecMath.intersects(ln[0].x, ln[0].y, ln[1].x, ln[1].y,
                           r.x, r.y, r.x, (r.y + r.height)) ||  // left
        VecMath.intersects(ln[0].x, ln[0].y, ln[1].x, ln[1].y,
                 (r.x + r.width), r.y, (r.x + r.width), (r.y + r.height)) ||  // right
        VecMath.intersects(ln[0].x, ln[0].y, ln[1].x, ln[1].y,
                 r.x, (r.y + r.height), (r.x + r.width), (r.y + r.height)))  // bottom
       return true;
    return false;
  }  // end of hasHitSaber()

}  // end of BlastSprite class

