
// Utils.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, November 2009

// Various useful utilities.


import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.system.*;

import java.util.Vector;

import net.rim.device.api.opengles.GLUtils;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;



public class Utils
{
  private static final String IMAGES_DIR = "images/";
  private static final String FONTS_DIR = "fonts/";


  public static Font loadFont(String ttfFnm, String fontName, int style, int size)
  /* Attempt to use the specified non-standard font, fontName, which 
     is stored in FONTS_DIR+ttfFnm. Otherwise use default, bold, 18pt 

     Non-standard fonts are limited to 60k in size, and must be stored in
     the ttf format (TrueType Unicode).
  */
  {
    Font font = null;
    boolean loadedFont = false;
    
    FontManager.getInstance().unload(fontName);    // remove any old version
   
    if ( FontManager.getInstance().load(FONTS_DIR+ttfFnm, fontName, 
                              FontManager.APPLICATION_FONT) == FontManager.SUCCESS) {
      try {
        FontFamily family = FontFamily.forName(fontName);
        font = family.getFont(style, size);
        loadedFont = true;
      } 
      catch (ClassNotFoundException e) {}
    }

    if (!loadedFont)
      font = Font.getDefault().derive(Font.BOLD, 18);

    return font;
  }  // end of loadFont()



  public static Font loadFont(String fontName, int style, int size)
  /* attempt to use the specified system font, fontName, otherwise use 
     default, bold, 18pt */
  {
    Font font;
    try {
      FontFamily family = FontFamily.forName(fontName);
      font = family.getFont(style, size);
    } 
    catch (ClassNotFoundException e) {  
      font = Font.getDefault().derive(Font.BOLD, 18);
    }
    return font;
  }  // end of loadFont()





  public static Bitmap loadImage(String fnm)
  // load the Bitmape stored in IMAGES_DIR+fnm
  {
    Bitmap im = null;
    System.out.println("Loading image in " + IMAGES_DIR+fnm);
    try {
      im = Bitmap.getBitmapResource(IMAGES_DIR +fnm);
    }
    catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
    if (im == null) {
      System.out.println("Image is empty");
      System.exit(1);
    }
    return im;
  }  // end of loadImage()




  public static void enableTexturing(GL10 gl, int texNames[], Bitmap texIm, boolean hasAlpha)
  // enable texturing for the specified texture (with/without alphas)
  {
    if (hasAlpha) {
      // do not draw transparent parts of the texture
      gl.glEnable(GL10.GL_BLEND);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
              // don't show source alpha parts in the destination

      // determine which areas of the polygon are to be rendered
      gl.glEnable(GL10.GL_ALPHA_TEST);
      gl.glAlphaFunc(GL10.GL_GREATER, 0);  // only render if alpha > 0
    }

    gl.glEnable(GL10.GL_TEXTURE_2D);   // use texturing

    gl.glBindTexture(GL10.GL_TEXTURE_2D, texNames[0]);  // use the tex name

    // specify the texture for the currently bound tex name
    int format =  (hasAlpha) ? GL10.GL_RGBA : GL10.GL_RGB;
    GLUtils.glTexImage2D(gl, 0, format, GL10.GL_UNSIGNED_BYTE, texIm, null);
                         // level, format,         type,           bitmap, region

    // set the minification/magnification techniques
    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
  } // end of enableTexturing()



  public static void disableTexturing(GL10 gl, boolean hasAlpha)
  {
    gl.glDisable(GL10.GL_TEXTURE_2D);   // switch off texturing

    if (hasAlpha) {
      // switch back to modulation of quad colours and textures
      gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
      gl.glDisable(GL10.GL_ALPHA);  // switch off transparency
      gl.glDisable(GL10.GL_BLEND);
    }
 } // end of disableTexturing()



  public static String getEGLErrorString(int error) 
  // convert an EGL error code (an integer) into a printable string
  {
    switch (error) {
      case EGL10.EGL_SUCCESS:
        return "EGL_SUCCESS";
      case EGL10.EGL_NOT_INITIALIZED:
        return "EGL_NOT_INITIALIZED";
      case EGL10.EGL_BAD_ACCESS:
        return "EGL_BAD_ACCESS";
      case EGL10.EGL_BAD_ALLOC:
        return "EGL_BAD_ALLOC";
      case EGL10.EGL_BAD_ATTRIBUTE:
        return "EGL_BAD_ATTRIBUTE";
      case EGL10.EGL_BAD_CONFIG:
        return "EGL_BAD_CONFIG";
      case EGL10.EGL_BAD_CONTEXT:
        return "EGL_BAD_CONTEXT";
      case EGL10.EGL_BAD_CURRENT_SURFACE:
        return "EGL_BAD_CURRENT_SURFACE";
      case EGL10.EGL_BAD_DISPLAY:
        return "EGL_BAD_DISPLAY";
      case EGL10.EGL_BAD_MATCH:
        return "EGL_BAD_MATCH";
      case EGL10.EGL_BAD_NATIVE_PIXMAP:
        return "EGL_BAD_NATIVE_PIXMAP";
      case EGL10.EGL_BAD_NATIVE_WINDOW:
        return "EGL_BAD_NATIVE_WINDOW";
      case EGL10.EGL_BAD_PARAMETER:
        return "EGL_BAD_PARAMETER";
      case EGL10.EGL_BAD_SURFACE:
        return "EGL_BAD_SURFACE";
      case EGL11.EGL_CONTEXT_LOST:
        return "EGL_CONTEXT_LOST";
      default:
        return "0x" + Integer.toHexString(error);
    }
  }  // end of getEGLErrorString()




  /* split() is taken from the article:
        "Create BlackBerry applications with open source tools, 
         Part 2: Building an RSS reader" by Frank Ableson, February 2009
         http://www.ibm.com/developerworks/opensource/tutorials/os-blackberry2/index.html
  */
  public static Vector split(String s, String delimiter)
  // split s into a vector of strings based on he supplied delimiter
  {
    try {
      Vector vec = new Vector();
      int i, j;
      boolean isFinished = false;

      i = 0;
      j = 0;
      while (!isFinished) {
        i = s.indexOf(delimiter, j);
        if (i == -1) {  // no more delimiters, grab rest of the field
          if (j < s.length())
            vec.addElement(s.substring(j));
          else
            vec.addElement("");
          isFinished = true;
        }
        else {
          String x = s.substring(j, i);
          // System.out.println("Found [" + x + "]");
          vec.addElement(x);
          j = i;
          if (j < s.length())
            j++;
        }
      }
      return vec;
    }
    catch (Exception e) {
      System.err.println("Error during split [" + e.getMessage() + "]");
      return null;
    }
  }  // end of split()





public static void showMessage(String title, String message)
/* Show a message dialog on the screen.
   Taken from 
     http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800332/800505/800608/
     How_To_-_Alert_a_user_from_a_Background_application.html?nodeid=820551&vernum=0  
*/
{
  synchronized(Application.getEventLock()) {
    UiEngine ui = Ui.getUiEngine();
    Dialog dialog = new Dialog( Dialog.D_OK, title + "\n\n" + message, Dialog.OK,
                             Bitmap.getPredefinedBitmap(Bitmap.INFORMATION),
                             Manager.VERTICAL_SCROLL);        
    // UiApplication.getUiApplication().pushScreen(dialog );
    ui.pushGlobalScreen(dialog, 1, UiEngine.GLOBAL_QUEUE);
  }
}  // end of showMessage()



}  // end of Utils class
