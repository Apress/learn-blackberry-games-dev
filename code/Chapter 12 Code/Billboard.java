
// Billboard.java
// Andrew Davison, December 2009, ad@fivedots.coe.psu.ac.th

/* A billboard is a transparent quadrilateral of dimensions size*size
   which only shows the non-transparent parts of the texture stored
   in texFnm. Lighting is disabled.

   The board rests on the floor at (xc, zc), and rotates to always face the
   camera.
*/

import java.nio.*;

import net.rim.device.api.system.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;



public class Billboard
{
  private FloatBuffer vertsBuf;   // billboard vertices
  private ByteBuffer tcsBuf;     // tex coords
  private Bitmap texIm = null;
  private int texNames[];   // for the texture name

  private float xPos, zPos;  // position of board's base center



  public Billboard(GL10 gl, float xc, float zc, float size, String texFnm)
  { 
    xPos = xc;
    zPos = zc;
    createBoard(size);

    texIm = Utils.loadImage(texFnm);
    texNames = new int[1];    // generate a texture name
    gl.glGenTextures(1, texNames, 0); 
  } // end of Billboard()



  private void createBoard(float size)
  // create the vertices and tex coords buffers for the overlay of size-by-size
  {
    // create vertices buffer
    float[] verts = { -size/2, 0,  0,    size/2, 0, 0, 
                      -size/2, size, 0,  size/2, size, 0
                   };  // billboard coords
    vertsBuf = ByteBuffer.allocateDirect(verts.length*4).asFloatBuffer();
    vertsBuf.put(verts).rewind();

    // create texture coordinates buffer
    byte[] tcs = {0,0,  1,0,  0,1,  1,1};
    tcsBuf = ByteBuffer.allocateDirect(tcs.length);
    tcsBuf.put(tcs).rewind();
  } // end of createBoard()



  public void draw(GL10 gl, float rotY)
  /* Draw the billboard rotated around
     the y-axis by rotY to face the camera.  */
  {
    gl.glDisable(GL10.GL_LIGHTING);
    Utils.enableTexturing(gl, texNames, texIm, true);   // uses alphas

    drawBoard(gl, rotY);

    Utils.disableTexturing(gl, true);
    gl.glEnable(GL10.GL_LIGHTING);
  }  // end of draw()




  private void drawBoard(GL10 gl, float rotY)
  {
    // enable the use of vertex and tex coord arrays when rendering
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertsBuf);  // use verts
    gl.glTexCoordPointer(2, GL10.GL_BYTE, 0, tcsBuf);  // use tex coords
    gl.glNormal3f( 0, 0, -1.0f);   // facing out

    gl.glPushMatrix();
      gl.glTranslatef(xPos, 0, zPos);   // move to the specified (x,y,z) pos
      gl.glRotatef(rotY, 0, 1, 0);   
         // rotates towards camera, since facing toward it
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    gl.glPopMatrix();

    // disable the arrays at the end of rendering
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
  }  // end of drawBoard()


} // end of Billboard class

