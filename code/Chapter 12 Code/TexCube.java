
// TexCube.java
// Andrew Davison, December 2009, ad@fivedots.coe.psu.ac.th

/* A textured cube, centered at the (xPos, yPos, zPos) coordinate,
   with a default dimension of 1 unit. The scale argument can be used to
   change the size.

   The cube is defines as 6 square faces, with each 
   face defined by a triangle strip of two triangles
   (4 coordinates).

   The cube can be positioned and scaled, depending on its
   constructor arguments.

   -----
   
   drawCube() calls glDrawArrays() just once, but an alternative approach is to
   call it six times, once for each face. However, when I tried that in 5.0 beta
   only the first face woule be drawn.

   This is the same problem as reported in:
       http://supportforums.blackberry.com/t5/Java-Development/5-0-Simulator-supporting-OpenGL-ES/m-p/388300
*/

import java.nio.*;

import net.rim.device.api.system.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;


public class TexCube
{
  private FloatBuffer vertsBuf;  // vertices
  private ByteBuffer tcsBuf;     // tex coords
  private FloatBuffer normsBuf;  // normals
  private Bitmap texIm = null;
  private int texNames[];   // for the texture name

  private float xPos, yPos, zPos;  // position of cube's center
  private float scale;             // scale factor



  public TexCube(GL10 gl, String texFnm, 
                        float x, float y, float z, float sc)
  // (x,y,z) is the cube's position, and sc the scaling factor
  {
    xPos = x; yPos = y; zPos = z;
    scale = sc;

    createCube();
    texIm = Utils.loadImage(texFnm);
    texNames = new int[1];       // generate a texture name
    gl.glGenTextures(1, texNames, 0); 
  }  // end of TexCube()



  private void createCube()
  // create the vertices and tex coords buffers for the cube
  {
    // create vertices buffer for cube
    float verts[] = {  // organized into 6 faces
      -0.5f, -0.5f, 0.5f,   0.5f, -0.5f, 0.5f,  -0.5f, 0.5f, 0.5f,   0.5f, 0.5f, 0.5f,   // front
       0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f,  // back
       0.5f, -0.5f, 0.5f,   0.5f, -0.5f, -0.5f,  0.5f, 0.5f, 0.5f,   0.5f, 0.5f, -0.5f,  // right
      -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,  -0.5f, 0.5f,-0.5f,  -0.5f, 0.5f, 0.5f,   // left
      -0.5f, 0.5f, 0.5f,    0.5f, 0.5f, 0.5f,   -0.5f, 0.5f, -0.5f,  0.5f, 0.5f, -0.5f,  // top
      -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,  0.5f, -0.5f, 0.5f   // bottom
    };
    vertsBuf = ByteBuffer.allocateDirect(verts.length*4).asFloatBuffer();
    vertsBuf.put(verts).rewind();

    // 2D tex coords buffer for cube
    byte texCoords[] = {  // 4 tex coords for each face
      0,0, 1,0, 0,1, 1,1,   0,0, 1,0, 0,1, 1,1,   0,0, 1,0, 0,1, 1,1, 
      0,0, 1,0, 0,1, 1,1,   0,0, 1,0, 0,1, 1,1,   0,0, 1,0, 0,1, 1,1
    };
    tcsBuf = ByteBuffer.allocateDirect(texCoords.length);
    tcsBuf.put(texCoords).rewind();

    float normals[] = {   // each normal repeated 4 times, for each face
         0, 0, 1.0f,    0, 0, 1.0f,   0, 0, 1.0f,   0, 0, 1.0f,     // front
         0, 0, -1.0f,   0, 0, -1.0f,  0, 0, -1.0f,  0, 0, -1.0f,    // back
         1.0f, 0, 0,    1.0f, 0, 0,   1.0f, 0, 0,   1.0f, 0, 0,     // right
         -1.0f, 0, 0,  -1.0f, 0, 0,  -1.0f, 0, 0,  -1.0f, 0, 0,     // left
         0, 1.0f, 0,   0, 1.0f, 0,    0, 1.0f, 0,   0, 1.0f, 0,     // top
         0, -1.0f, 0,  0, -1.0f, 0,   0, -1.0f, 0,  0, -1.0f, 0     // bottom
    };
    normsBuf = ByteBuffer.allocateDirect(normals.length*4).asFloatBuffer();
    normsBuf.put(normals).rewind();
  } // end of createCube()




  public void draw(GL10 gl)
  {
    Utils.enableTexturing(gl, texNames, texIm, false);   // no alphas
    drawCube(gl);
    gl.glDisable(GL10.GL_TEXTURE_2D);   // switch off texturing
  }  // end of draw()



  private void drawCube(GL10 gl)
  {
    // enable the use of vertex and tex coord arrays when rendering
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertsBuf);  // use cube verts
    gl.glNormalPointer(GL10.GL_FLOAT, 0, normsBuf);     // use normals
    gl.glTexCoordPointer(2, GL10.GL_BYTE, 0, tcsBuf);  // use tex coords

    gl.glPushMatrix();

      gl.glTranslatef(xPos, yPos, zPos);   // move to the specified (x,y,z) pos
      if (scale != 1.0f)
        gl.glScalef(scale, scale, scale);  // uniform scaling
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 24);   // works, since only one glDrawArrays() call
    gl.glPopMatrix();

    // disable the arrays at the end of rendering
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
  }  // end of drawCube()

}  // end of TexCube class
