
// BoxTrixScreen.java
// Andrew Davison, December 2009, ad@fivedots.coe.psu.ac.th

/* A simple 3D scene consisting of a textured floor, blue sky,
   ambient and diffuse light,
   two rotating textured cubes, two billboards (a tree and bozo),
   and a 2D overlay at the front of the screen.
   It is implemented using the Java binding for OpenGL ES (JSR 239).

   The camera can be moved around the scene via keyboard/trackball controls.

   The camera has 3 modes: TRANSLATE (for translations),
   ROTATE (for rotations), and FLOAT_GLIDE (for floating and gliding).
   They are selected via menu items accessible from the BoxTrixScreen.

   There is a RESET menu item, which returns the camera to its starting 
   position and forward direction.

   The main animation loop is in BoxTrixScreen's run() method.
   The 3D rendering is all done to an off-screen buffer, which is painted
   by paint().

   It is possible to pause/resume execution.
*/


import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.system.*;

import net.rim.device.api.opengles.*;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;




public class BoxTrixScreen extends MainScreen implements Runnable
{

  private static final int PERIOD = 200;    // frame rate (in ms)
  private static final float ROT_INCR = 4.0f;    
                               // rotation increment for the cubes
  private static final float[] LIGHT_DIR = { 1.0f, 1.0f, 1.0f, 0.0f };   
          // right, top, front directional light
          
  private static final String FONT_NAME = "Sceptre";   // used in the overlay     


  // keycodes for the green and red keys
  private static final int GREEN_KEY = 1114112;
  private static final int RED_KEY = 1179648;


  // graphics related 
  private Bitmap offScrBitmap;      // buffer used for the off-screen rendering
  private Graphics offGraphics;  // graphics context for the buffer

  private EGL11 egl;     // OpenGL ES link from Java
  private GL10 gl;       // for calling OpenGL ES

  private EGLDisplay eglDisplay;
  private EGLContext eglContext;   // OpenGL ES state
  private EGLSurface eglSurface;   // for rendering
  

  // rendering related
  private boolean isRunning = false;
  private boolean isPaused = false;
  private long frameDuration;  // in ms
      // how long one iteration of the animation loop takes
  private boolean focusRegained = false;


  // scene related
  private TexCube texCube1, texCube2;   // two textured cubes
  private float angle;       // rotation angle of the cubes
  private Floor floor;
  private ModedCamera modedCamera = null;
  private Overlay statsHud = null;    // a 2D panel at the front of the screen
  private Billboard treeBoard, bozoBoard;
     // a billboard is an image that always faces the camera


  public BoxTrixScreen()
  {
    super(FullScreen.DEFAULT_MENU | FullScreen.DEFAULT_CLOSE);  

    // set up the menu items for the three camera modes, reset, and help
    MenuItem transCamera = new MenuItem("Translate", 10, 10) {
      public void run()  
      { if (modedCamera != null)
          modedCamera.setMode(ModedCamera.TRANSLATE); 
      }
    };
    addMenuItem(transCamera);

    MenuItem rotCamera = new MenuItem("Rotate", 20, 10) {
      public void run()  
      { if (modedCamera != null)
          modedCamera.setMode(ModedCamera.ROTATE); 
      }
    };
    addMenuItem(rotCamera);

    MenuItem glideCamera = new MenuItem("Float/Glide", 30, 10) {
      public void run()  
      { if (modedCamera != null)
          modedCamera.setMode(ModedCamera.FLOAT_GLIDE); 
      }
    };
    addMenuItem(glideCamera);

    MenuItem resetCamera = new MenuItem("Reset", 40, 10) {
      public void run()  
      { if (modedCamera != null)
          modedCamera.reset();   // reset the camera position/orientation
      }
    };
    addMenuItem(resetCamera);

    MenuItem help = new MenuItem("Help", 50, 10) {
      public void run()  
      {  UiApplication.getUiApplication().pushScreen( new HelpScreen());  }
    };
    addMenuItem(help);
  }  // end of BoxTrixScreen()




  protected void onVisibilityChange(boolean isVisible)
  /* Called when the visibility of the screen changes, which
     may signal the animations start, resumption or pausing. */
  {
    if (isVisible) {   // visible means resumption or start
      if (isRunning)
        resumeRendering();
      else {   // start the render thread and loop
        isRunning = true;
        new Thread(this).start();
      }
    }
    else   // not visible means pause
      isPaused = true;
  }  // end of onVisibilityChange()



  private void resumeRendering()
  {
    isPaused = false;
    synchronized (this) {
      notifyAll();
    }
  }  // end of resumeRendering()



  protected void onFocusNotify(boolean hasFocus)
  /* pause/resume execution based on focus, and
     trigger a recreation of the off-screen buffer when focus is regained */
  {  
    if (!hasFocus)
      isPaused = true;
    else
      resumeRendering();
    focusRegained = hasFocus;  
  }  // end of onFocusNotify()



  // ------------------------ rendering loop ---------------------------------

  public void run()
  /* Contains three stages: initialization, animation loop, shutdown.
     The animation loop has three main parts: update, draw, maybe sleep.
  */
  {
    if (!initGraphics())
      return;   // give up if there's an error during OpenGL ES initialization

    initScene();

    long startTime;
    while (isRunning) {
      if (isPaused)
        pauseRendering();
      startTime = System.currentTimeMillis();

      angle = (angle + ROT_INCR) % 360.0f;  // update
      checkBackBuffer();
      drawScene();                          // draw
      maybeSleep(startTime);                // sleep
    }

    shutdown();
    // destroyEGL();
  }  // end of run()



  private void pauseRendering()
  {
    synchronized (this) {     // idle if we are in the background
      try {
        wait();
      }
      catch (InterruptedException x) {}
    }
  }  // end of pauseRendering()



  private void maybeSleep(long startTime)
  {
    frameDuration = System.currentTimeMillis() - startTime;
    try { // sleep a bit maybe, so one iteration takes PERIOD ms
      if (frameDuration < PERIOD)
        Thread.sleep(PERIOD - (int)frameDuration); 
    }
    catch (InterruptedException e){}
  }  // end of maybeSleep()



  // ---------------- initialization OpenGL ES ----------------------------


  private boolean initGraphics()
  // Set up OpenGL ES 3D graphics connection
  {
    if (!GLUtils.isSupported()) {
      Utils.showMessage("Graphics Error", "No OpenGL ES");
      return false;
    }

    // initialize OpenGL ES
    egl = (EGL11) EGLContext.getEGL();
    if (egl == null) {
      Utils.showMessage("Init Error", "No OpenGL ES");
      return false;
    }

    // initialize the OpenGL ES connection to the display
    eglDisplay = egl.eglGetDisplay(EGL11.EGL_DEFAULT_DISPLAY);
    if (eglDisplay == null) {
      Utils.showMessage("Init Error", "No display connection");
      return false;
    }

    int[] majorMinor = new int[2];
    if (!egl.eglInitialize(eglDisplay, majorMinor)) {
      Utils.showMessage("Init Error", "No OpenGL ES display");
      return false;
    }
    System.out.println("EGL version: " + majorMinor[0] + "." + majorMinor[1]);

    // determine the number of available configurations
    int[] numConfigs = new int[1];
    egl.eglGetConfigs(eglDisplay, null, 0, numConfigs);
    if (numConfigs[0] < 1) {
      Utils.showMessage("Init Error", "No config found");
      return false;
    }

    // specify an 5/6/5 RGB configuration
    int configAttributes[] = {
      EGL11.EGL_RED_SIZE, 5, EGL11.EGL_GREEN_SIZE, 6, EGL11.EGL_BLUE_SIZE, 5,  // RGB
      EGL11.EGL_ALPHA_SIZE, 0,        // no alpha necessary
      EGL11.EGL_DEPTH_SIZE, 16,       // use a 16-bit z-buffer
      // EGL11.EGL_STENCIL_SIZE, EGL11.EGL_DONT_CARE,   // don't care about stencils
      EGL11.EGL_SURFACE_TYPE, EGL11.EGL_WINDOW_BIT, // use a window buffer
      EGL11.EGL_NONE
    };

    // use the first matching configuration
    EGLConfig eglConfigs[] = new EGLConfig[numConfigs[0]];
    if (!egl.eglChooseConfig(eglDisplay, configAttributes, eglConfigs,
                        eglConfigs.length, numConfigs)) {
      Utils.showMessage("Init Error", "No suitable config");
      return false;
    }
    EGLConfig eglConfig = eglConfigs[0];

    /* initialize the OpenGL ES rendering state (the context) 
       with the display and configuration */
    eglContext =  egl.eglCreateContext(eglDisplay, eglConfig, 
                                                 EGL11.EGL_NO_CONTEXT, null);
    if (eglContext == null) {
      Utils.showMessage("Init Error", "No rendering state");
      return false;
    }

    // initialize 3D graphics: the API is called through gl
    gl = (GL10) eglContext.getGL();  
    if (gl == null) {
      Utils.showMessage("Init Error", "No 3D context");
      return false;
    }

    // set the drawing surface to be a window (i.e. use on-screen rendering)
    eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, this, null);
    if (eglSurface == null) {
      Utils.showMessage("Init Error", "No drawing surface");
      return false;
    }

    // bind the display, drawing surface, and context to this thread  
    if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)){
      Utils.showMessage("Init Error", "No current context");
      return false;
    }

    return true;   // everything worked!
  } // end of initGraphics()



  // -------------------- initialize scene -------------------------------

  private void initScene()
  /* initialize the view, camera, lighting, create the 3D scenery, 
     create the 2D overlay */
  {
    setView(60.0f, 0.1f, 50.0f);

    modedCamera = new ModedCamera();

    gl.glClearColor(0.17f, 0.65f, 0.92f, 1.0f);  // sky blue background

    // z- (depth) selectBuffer initialization for hidden surface removal
    gl.glEnable(GL10.GL_DEPTH_TEST); 

    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    gl.glShadeModel(GL10.GL_SMOOTH);    // use smooth shading

    gl.glEnable(GL10.GL_CULL_FACE);   
              // to cull backfaces (a useful speed optimization)

    gl.glEnable(GL10.GL_COLOR_MATERIAL);   // set material properties
          // use default material properties

    // vertically flip texturing                       
    gl.glMatrixMode(GL10.GL_TEXTURE);
    gl.glLoadIdentity();
    gl.glScalef(1.0f, -1.0f, 1.0f);

    addLight();
    createScenery();
    
    // Font msgFont = Utils.loadFont("BBCasual", Font.BOLD, 20); 
    // Font msgFont = Utils.loadFont("Marathon.ttf", "Marathon", Font.PLAIN, 30);
    Font msgFont = Utils.loadFont("SceptreRegular.ttf", FONT_NAME, Font.PLAIN, 26);
                      // remember to unload non-system fonts at end
    statsHud = new Overlay(gl, msgFont, 200.0f);
  }  // end of initScene()



  private void setView(float fovy, float near, float far)
  /* Set the perspective view: the arguments are the field-of-view,
     and the near & far clipping planes */
  {
    gl.glViewport(0, 0, getWidth(), getHeight());    
           // set size of drawing area to be the screen size
    // set the projection matrix
    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glLoadIdentity();
    GLUtils.gluPerspective(gl, fovy, (float)getWidth()/(float)getHeight(), near, far);
  } // end of setView()



  private void addLight()
  // set the lighting properties
  {
    gl.glEnable(GL10.GL_LIGHTING);
    gl.glEnable(GL10.GL_LIGHT0);
    gl.glEnable(GL10.GL_NORMALIZE);

    float[] ambientLight = { 0.125f, 0.125f, 0.125f, 1.0f };  // weak gray ambient
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLight, 0);

    float[] diffuseLight = { 0.9f, 0.9f, 0.9f, 1.0f };   // white diffuse
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseLight, 0);
  } // end of addLight()



  private void createScenery()
  // create the floor,  the textured cubes, and billboards
  {
    // floor = new Floor(gl, "bigGrid.png", 8);   // 8 by 8 size
    /* other possible floor textures include:
          floor.png, grass.png */
    floor = new Floor(gl, "grass.png", 9.5f);

    texCube1 = new TexCube(gl, "matrix.png", 0, 0.4f, 0, 0.8f);  // (x,y,z), scale
    texCube2 = new TexCube(gl, "metal.png", -2.0f, 2.1f, -1.3f, 0.5f);
    /* other cube textures include:
         matrix.png, metal.png, cloth.png, brick.png, rock.png, brick2.png */

    treeBoard = new Billboard(gl, 1.6f, 1.5f, 2f, "tree.png");  // (x,z), size
    bozoBoard = new Billboard(gl, -1.8f, -0.5f, 1.4f, "bozo.png");

  } // end of createScenery()



  // ----------------- draw the scene -----------------------------------------



  private void checkBackBuffer()
  /* keep the off-screen buffer in sync with the screen size,
     and regenerate it when necessary */
  {
    boolean needsUpdating = false;
    
    if (offScrBitmap == null)
      needsUpdating = true;
    else if ((offScrBitmap.getWidth() != getWidth()) ||
             (offScrBitmap.getHeight() != getHeight()))
      needsUpdating = true;
    else if (focusRegained)
      needsUpdating = true;

    if (needsUpdating) {
      offScrBitmap = new Bitmap(getWidth(), getHeight());
      offGraphics = Graphics.create(offScrBitmap);
    
      setView(60.0f, 0.1f, 50.0f);
    }
  }  // end of checkBackBuffer()




  private void drawScene()
  /* draw the scene: the camera, the light source, floor, 
     billboards, the rotating cubes, the 2D overlay
  */
  {
    // wait until OpenGL ES is available before starting to draw
    egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, offGraphics);

    // clear colour and depth buffers
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

    // set modeling and viewing transformations
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glLoadIdentity();

    modedCamera.move(gl);   // move the camera

    // set light direction
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, LIGHT_DIR, 0);

    floor.draw(gl);  // floor

    float rotY = (float) modedCamera.getRotY();   // billboards
    treeBoard.draw(gl, rotY);
    bozoBoard.draw(gl, rotY);

    gl.glPushMatrix();
      gl.glRotatef(angle, 0, 1.0f, 0);   // rotate cubes around y-axis
      texCube1.draw(gl);
      texCube2.draw(gl);
    gl.glPopMatrix();
    
    statsHud.draw(gl, getWidth(), getHeight(), modedCamera, frameDuration);  // 2D overlay

    // wait until all Open GL ES tasks are finished
    egl.eglWaitGL();

    // pass the EGL drawing surface to the native 'window'
    egl.eglSwapBuffers(eglDisplay, eglSurface);
    
    String swapRes = Utils.getEGLErrorString( egl.eglGetError() );
    if (!swapRes.equals("EGL_SUCCESS"))
      Utils.showMessage("Swap Buffer Error", swapRes);  // report any problem
  }  // end of drawScene()




  protected void paint(Graphics g)
  // called by the UI system to paint the screen.
  {
    if (offScrBitmap != null)
      g.drawBitmap(0, 0, offScrBitmap.getWidth(),
                         offScrBitmap.getHeight(), offScrBitmap, 0, 0);
  }  // end of paint()



  // --------------------- shut down ----------------------------------


  private void shutdown()
  // close down OpenGL ES
  {
    if ((egl == null) || (eglDisplay == null))
      return;

    // disconnect the display, drawing surface, and context from this thread  
    egl.eglMakeCurrent(eglDisplay, EGL11.EGL_NO_SURFACE, 
                         EGL11.EGL_NO_SURFACE, EGL11.EGL_NO_CONTEXT);

    // delete the context
    if (eglContext != null)
      egl.eglDestroyContext(eglDisplay, eglContext);

    // delete the drawing surface
    if (eglSurface != null)
      egl.eglDestroySurface(eglDisplay, eglSurface);

    // break the OpenGL ES connection to the screen
    egl.eglTerminate(eglDisplay);
    
    FontManager.getInstance().unload(FONT_NAME);
    
    System.exit(0);
  }  // end of shutdown()



  public void close()
  // called when the screen is closing.
  {
    isRunning = false;
    synchronized (this) {
      notifyAll();
    }
    super.close();
  }  // close()



  // ----------------------------- user input ---------------------------------
  // handles the track ball movement, and key presses;
  // the input is used to move the camera


  protected boolean navigationMovement(int dx,int dy, int status, int time)  
  // pass trackball movement info to the camera
  {
    if (modedCamera == null)
      return false;

    if (dx < 0)
      modedCamera.update(ModedCamera.LEFT); 
    else if (dx > 0)
      modedCamera.update(ModedCamera.RIGHT); 

    if (dy < 0)
      modedCamera.update(ModedCamera.DOWN); 
    else if (dy > 0)
      modedCamera.update(ModedCamera.UP); 

    return true;
  }  // end of navigationMovement()



  protected boolean keyChar(char key, int status, int time)
  // camera movement, and game terminate with <ESC>
  {
    if (key == Characters.ESCAPE) {
      isRunning = false;
      return false;
    }

    if (modedCamera == null)
      return false;

    /* The left character (e.g. 's') corresponds to a QWERTY keyboard, 
       the second (e.g. 'd') is for the models with just a number pad.  */
    if((key == 's') || (key == 'd'))
      modedCamera.update(ModedCamera.LEFT);
    else if((key == 'f') || (key == 'j'))
      modedCamera.update(ModedCamera.RIGHT);
    else if((key == 'e') || (key == 't'))
      modedCamera.update(ModedCamera.UP);
    else if((key == 'x') || (key == 'b')) 
      modedCamera.update(ModedCamera.DOWN);
    else  // keystroke not relevant
      return false;

    return true;
  }  // end of keyChar()



  protected boolean keyDown(int keycode, int time)
  // terminate the game if the green or red keys are pressed
  {
    if ((keycode == GREEN_KEY) || (keycode == RED_KEY)) {
      isRunning = false;
      return true;
    }
    return false;
  }  // end of keyDown()
  

}  // end of BoxTrixScreen class
