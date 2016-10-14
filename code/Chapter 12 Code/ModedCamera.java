
// ModedCamera.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, December 2009

/* Use key presses/the trackball to position/rotate the camera.
   ModedCamera is an adapter for the Camera class

   The camera has 3 modes: TRANSLATE (for translations),
   ROTATE (for rotations), and FLOAT_GLIDE (for floating and gliding).
   They are selected via menu items accessible from the BoxTrixScreen.

   There is also a RESET menu item, which returns the camera to its starting 
   position and forward direction.

   The keys used are translated into direction constants
   LEFT, UP, RIGHT, and DOWN before the ModedCamera methods are called.

*/

import net.rim.device.api.system.Characters;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;


public class ModedCamera
{
  // key modes -- public so they can be used in BoxTrixScreen
  public static final int TRANSLATE = 0;
  public static final int ROTATE = 1;
  public static final int FLOAT_GLIDE = 2;
  
  // directions -- public so they can be used in BoxTrixScreen
  public static final int NONE = 0;
  public static final int LEFT = 1;
  public static final int UP = 2;
  public static final int RIGHT = 3;
  public static final int DOWN = 4;

  // initial camera position
  private static final double X_POS = 0.0; 
  private static final double Y_POS = 1.0; 
  private static final double Z_POS = 6.1;

  // translation and rotation increments
  private static final double MOVE_INCR = 0.1;
  private static final double ANGLE_INCR = 5.0;   // in degrees

  // navigation related
  private Camera camera;
  private int keyMode;


  public ModedCamera()
  {  
    keyMode = TRANSLATE;
    camera = new Camera(X_POS, Y_POS, Z_POS);  
  }


  // ------------------ update the camera state ---------------

  public void setMode(int mode)
  {
    if ((mode == TRANSLATE) || (mode == ROTATE) ||
        (mode == FLOAT_GLIDE))
      keyMode = mode;
  }  // end of setMode()


  public void reset()
  /* return the camera to its starting 
     position and forward direction. */
  { keyMode = TRANSLATE;
    camera.reset(); 
  }



  public void update(int dir)
  /* Update the camera's position and orientation.
     The three modes (TRANSLATE, ROTATE, and FLOAT_GLIDE) 
     cause the key directions to be interpreted differently.
  */
  {
    if (keyMode == TRANSLATE) { 
      if (dir == UP)
        camera.transFwd(MOVE_INCR);     // translate forward
      if (dir == DOWN)
        camera.transFwd(-MOVE_INCR);    // translate backward
      if (dir == LEFT)
        camera.glideRight(-MOVE_INCR);  // move left parallel to XZ plane
      if (dir == RIGHT)
        camera.glideRight(MOVE_INCR);   // move right parallel to XZ plane
    }
    else if (keyMode == ROTATE) {
      if (dir == UP)
        camera.rotateX(ANGLE_INCR);    // rotate camera up around x-axis
      if (dir == DOWN)
        camera.rotateX(-ANGLE_INCR);   // rotate camera down
      if (dir == LEFT)
        camera.rotateY(ANGLE_INCR);    // rotate camera left around y-axis
      if (dir == RIGHT)
        camera.rotateY(-ANGLE_INCR);   // rotate camera right
    }
    else if (keyMode == FLOAT_GLIDE) {
      if (dir == UP)
        camera.moveUp(MOVE_INCR);     // move camera up
      if (dir == DOWN)
        camera.moveUp(-MOVE_INCR);    // move camera down
      if (dir == LEFT) 
        camera.glideFwd(MOVE_INCR);   // move forward parallel to XZ plane
      if (dir == RIGHT)
        camera.glideFwd(-MOVE_INCR);  // move backward parallel to XZ plane
    }
    else  // should not happen
      System.out.println("Unknown key mode");
  }  // end of update()



  public void move(GL10 gl)
  // update the camera's position/orientation
  { camera.move(gl);  }
  


  // --------------------- get camera state ------------------

  public String getMode()
  // return the camera mode
  { 
    if (keyMode == TRANSLATE) 
      return "Translate";
    else if (keyMode == ROTATE)
      return "Rotate";
    else if (keyMode == FLOAT_GLIDE)
      return "Float/glide";
    else
      return "Mode ??";
  }  // end of getMode()


  public String getPos()
  // return the current camera position
  {  return camera.getPos(); }

  public String getRots()
  // return the current camera rotations
  {  return camera.getRots();  }
  
  public double getRotY()
  {  return camera.getRotY();  }


}  // end of ModedCamera class
