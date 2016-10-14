
// Overlay.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, December 2009

/* Overlay is a transparent textured image with dimensions size*size,
   that always stays at the front,'stuck' to the screen. 

   The image acts as a drawing surface for displaying information
   (in this case, details about the camera's current position).
   The surface is updated by calling redrawTexture(), which 'cleans'
   the texture by drawing its background colour over the old info.

   I borrowed the 2D overlay technique (begin2D() and end2D())
   from ozak in his message at
     http://www.javagaming.org/forums/index.php?topic=8110.0

   ----
   I originally tried to implement overlays by switching to 2D drawing
   using the BlackBery Graphics API, after completing the 3D drawing in
   BoxTrixScreen. But I couldn't get the 2D and 3D elements to work
   together in 5.0 beta.
*/

import java.nio.*;

import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;
import net.rim.device.api.opengles.GLUtils;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;



public class Overlay 
{
  private final static String TEX_FNM = "noticeBd.png";
  private final static int BG_COLOR = 0x00ffdca8;    // color of plain texture

  private FloatBuffer vertsBuf;   // overlay vertices
  private ByteBuffer tcsBuf;     // tex coords
  private Bitmap texIm = null;
  private int texNames[];   // for the texture name
  
  private Font msgFont;


  public Overlay(GL10 gl, Font font, float size) 
  { 
    msgFont = font;
    createOverlay(size);

    texIm = Utils.loadImage(TEX_FNM);
    texNames = new int[1];       // generate a texture name
    gl.glGenTextures(1, texNames, 0); 
  }  // end of Overlay()



  private void createOverlay(float size)
  // create the vertices and tex coords buffers for the overlay of size-by-size
  {
    // create vertices buffer (a triangle strip defining a square)
    float[] verts = {0,size,0,  size,size,0,   0,0,0,  size,0,0};
             // the vertex ordering is top-down, to match the orthographic projection
    vertsBuf = ByteBuffer.allocateDirect(verts.length*4).asFloatBuffer();
    vertsBuf.put(verts).rewind();

    // create texture coordinates buffer
    byte[] tcs = {0,0,  1,0,  0,1,  1,1};
    tcsBuf = ByteBuffer.allocateDirect(tcs.length);
    tcsBuf.put(tcs).rewind();
  } // end of createOverlay()




  public void draw(GL10 gl, int width, int height, ModedCamera modedCamera, long frameDuration)
  // called from BoxTrixScreen to draw the overlay
  {
    begin2D(gl, width, height);  // switch to 2D viewing
    gl.glDisable(GL10.GL_LIGHTING);
    Utils.enableTexturing(gl, texNames, texIm, true);   // uses alphas
    
    redrawTexture(modedCamera, frameDuration);
    drawOverlay(gl);

    Utils.disableTexturing(gl, true);
    gl.glEnable(GL10.GL_LIGHTING);
    end2D(gl);  // switch back to 3D viewing
  }  // end of draw()



  private void begin2D(GL10 gl, int width, int height)
  // switch to 2D viewing (an orthographic projection)
  {
    gl.glMatrixMode(GL10.GL_PROJECTION); 
    gl.glPushMatrix();   // save projection settings
    gl.glLoadIdentity();
    
    GLUtils.gluOrtho2D(gl, 0.0f, width, height, 0.0f); 
                          // left, right, bottom, top
    /* The y-axis of the orthographic projection is reversed to be
       top-down, by switching the top and bottom values in glOrtho2D().
    */
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glPushMatrix();   // save model view settings
    gl.glLoadIdentity();    
    gl.glDisable(GL10.GL_DEPTH_TEST);
  } // end of begin2D()



  private void redrawTexture(ModedCamera modedCamera, long frameDuration)
  // change the information shown in the texture image
  {
    Graphics g = Graphics.create(texIm);

    g.setColor(BG_COLOR);
    g.fillRect(25, 25, 215, 115);    // wipe the text area clean
    
    if (modedCamera != null) {  // show the camera's position, rotations, and mode
      g.setColor(0xff0000);   // red
      g.setFont(msgFont);
      g.drawText( modedCamera.getPos(), 30, 25, DrawStyle.TOP|DrawStyle.LEFT);
      g.drawText( modedCamera.getRots(), 30, 48, DrawStyle.TOP|DrawStyle.LEFT);
      
      g.setColor(0x0000ff);   // blue
      g.drawText( modedCamera.getMode(), 30, 73, DrawStyle.TOP|DrawStyle.LEFT);
    }

    // show the current frame duration
    g.setColor(0x000000);   // black
    g.drawText( "" + frameDuration, 30, 98, DrawStyle.TOP|DrawStyle.LEFT);
  }  // end of redrawTexture()




  private void drawOverlay(GL10 gl)
  {
    // enable the use of vertex and tex coord arrays when rendering
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertsBuf);  // use verts
    gl.glTexCoordPointer(2, GL10.GL_BYTE, 0, tcsBuf);  // use tex coords

    gl.glTranslatef(10, 10, 0);   // move overlay left and down on the screen

    gl.glNormal3f( 0, 0, -1.0f);   // facing out
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    // disable the arrays at the end of rendering
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
  }  // end of drawOverlay()



  private void end2D(GL10 gl)
  // switch back to 3D viewing
  {
    gl.glEnable(GL10.GL_DEPTH_TEST);
    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glPopMatrix();   // restore previous projection settings
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glPopMatrix();   // restore previous model view settings
  } // end of end2D()



} // end of Overlay Class
