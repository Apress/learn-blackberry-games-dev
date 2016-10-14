
// Camera.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, December 2009

/* Translate a camera forward/backward/left/right/up/down,
   and allow it to rotate around the x- and y- axes. 
   There's also a 'glide' feature that lets it move forward
   and backward parallel to the floor, even if it is facing down 
   or up.

   The reset() method returns the camera to its starting 
   position and forward direction.

   The code utilises a (x,y,z) camera position and
   forward direction. A rotation affects the direction, and a
   translation of the camera uses the direction to decide which
   way is forward/back/left/right.

   Based on C++ camera code at
   http://www.codecolony.de/opengl.htm#camera
   by Philipp Crocoll, philipp.crocoll@web.de, June 2000

   There's also an 'advanced' camera at Crocoll's website, which can
   rotate around all three axes.
*/


import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;


public class Camera
{
  private double xCamPos, yCamPos, zCamPos;    // (x,y,z) camera position
  private double xStart, yStart, zStart;       // starting position

  private double xFwdDir, yFwdDir, zFwdDir;    // forward direction 
  private double rotX, rotY;        // rotation in degrees around x- and y- axes



  public Camera(double x, double y, double z)
  {
    xStart = x; yStart = y; zStart = z;
    reset();
  } // end of Camera()



  public void reset()
  // place camera at starting position, facing along -z-axis
  {
    xCamPos = xStart; yCamPos = yStart; zCamPos = zStart;
    xFwdDir = 0; yFwdDir = 0; zFwdDir = -1.0;
       // forward direction is along -z-axis

    rotX = 0;   // no rotations initially
    rotY = 0;
  }  // end of reset()


  // ---------------- return camera details ------------------


  public String getPos()
  // return the current camera position
  {
    return "Pos: (" + round2dp(xCamPos) + ", " + 
                       round2dp(yCamPos) + ", " + 
                       round2dp(zCamPos) + ")";
  }  // end of getPos()

  private double round2dp(double x)
  // return a double which is x rounded to 2 dp
  {  return ((int)((x+0.005)*100.0))/100.0;  }

  
  public String getRots()
  // return the current camera rotations
  {  return "Rots: (" + rotX + ", " + rotY + ", 0)";  }
  
  
  public double getRotY()
  {  return rotY;  }



  // ---------------- rotations ------------------------------

  public void rotateY(double angle)
  {
    rotY = (rotY + angle) % 360;
    updateFwdDir();  // since the rotation has changed
  }  // end of rotateY()


  public void rotateX(double angle)
  {
    rotX = (rotX + angle) % 360;
    updateFwdDir();  // since the rotation has changed
  } // end of rotateX()


  private void updateFwdDir()
  // update the forward direction using the x- and y- axis rotations
  {
    /* Calculate x- and z- direction components when fwd dir is rotated
       around the y-axis. The angle is measured from the + x-axis. */
    double yRotRad = Math.toRadians(rotY + 90);
    double xDir = Math.cos(yRotRad);
    double zDir = -Math.sin(yRotRad);

    /* Calculate XZ plane component when fwd dir is
       rotated around the x-axis */
    double xRotRad = Math.toRadians(rotX);
    double xzProj = Math.cos(xRotRad);

    // combine the components to get the forward direction
    xFwdDir = xDir * xzProj;
    yFwdDir = Math.sin(xRotRad);
    zFwdDir = zDir * xzProj;
  }  // end of updateFwdDir()


  // --------------------- translations ------------------------

  public void transFwd(double dist)
  /* Move the camera forward by dist units.
     Update the camera position by multiplying 
     'dist' to the forward direction */
  {
    xCamPos += (xFwdDir * dist);
    yCamPos += (yFwdDir * dist);
    zCamPos += (zFwdDir * dist);
  } // end of transFwd()



  public void glideRight(double dist)
  // move right in the x- and z- dirs, without any y-axis move
  {
    xCamPos += (-zFwdDir * dist);
    // no change to yCamPos
    zCamPos += (xFwdDir * dist);
  } // end of glideRight()


  public void glideFwd(double dist)
  // move forward in the x- and z- dirs, without any y-axis move
  {  
    xCamPos += (xFwdDir * dist);
    // no change to yCamPos
    zCamPos += (zFwdDir * dist);
  }  // end of glideFwd()


  public void moveUp(double dist)
  {  yCamPos += dist;  }


  // -------------------- move the camera -------------------

  public void move(GL10 gl)
  /* The camera isn't really moved at all, it's the
     scene that's moved in the opposite direction. This
     is why the rotations and translations applied to the
     scene are negated. */
  {
    gl.glRotatef((float)-rotX, 1.0f, 0, 0);   // x-axis rotation
    gl.glRotatef((float)-rotY, 0, 1.0f, 0);   // y-axis rotation
    gl.glTranslatef((float)-xCamPos, (float)-yCamPos, (float)-zCamPos);
  } // end of position()

}  // end of Camera class
