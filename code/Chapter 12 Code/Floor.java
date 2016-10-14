
// Floor.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, December 2009

/* The Floor is a square of dimensions size*size, centered at the
   origin on the XZ plane. Wrapped over the top of it is
   the image loaded from floorFnm.

   The floor geometry is a triangle strip defining a square.
*/

import java.nio.*;

import net.rim.device.api.system.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;



public class Floor 
{
  private FloatBuffer vertsBuf;   // floor vertices
  private ByteBuffer tcsBuf;     // tex coords
  private Bitmap texIm = null;
  private int texNames[];   // for the texture name



  public Floor(GL10 gl, String floorFnm, float size) 
  { 
    createFloor(size);

    texIm = Utils.loadImage(floorFnm);
    texNames = new int[1];    // generate a texture name
    gl.glGenTextures(1, texNames, 0); 
  }  // end of Floor()


  private void createFloor(float size)
  // create the vertices and tex coords buffers for the floor of size-by-size
  {
    // create vertices buffer  (a triangle strip defining a square)
    float[] verts = { -size/2,0,size/2,   size/2,0,size/2,  
                      -size/2,0,-size/2,  size/2,0,-size/2 };
    vertsBuf = ByteBuffer.allocateDirect(verts.length*4).asFloatBuffer();
    vertsBuf.put(verts).rewind();

    // create texture coordinates buffer
    byte[] tcs = {0,0,  1,0,  0,1,  1,1};
    tcsBuf = ByteBuffer.allocateDirect(tcs.length);
    tcsBuf.put(tcs).rewind();
  } // end of createFloor()



  public void draw(GL10 gl)
  {
    Utils.enableTexturing(gl, texNames, texIm, false);   // no alphas
    drawFloor(gl);
    gl.glDisable(GL10.GL_TEXTURE_2D);   // switch off texturing
  }  // end of draw()



  private void drawFloor(GL10 gl)
  {
    // enable the use of vertex and tex coord arrays when rendering
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertsBuf);  // use floor verts
    gl.glTexCoordPointer(2, GL10.GL_BYTE, 0, tcsBuf);  // use tex coords

    gl.glNormal3f( 0, 1.0f, 0);   // facing up
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    // disable the arrays at the end of rendering
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
  }  // end of drawFloor()

} // end of Floor Class
