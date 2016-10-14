
// BoxTrix.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, December 2009

/* A demo of features from the Java binding for OpenGL ES (JSR 239).
   See BoxTrixScreen for details.
*/

import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;


public class BoxTrix extends UiApplication
{
  public BoxTrix()
  {  pushScreen( new BoxTrixScreen() );   }

  // -----------------------------------------

  public static void main(String[] args)
  {  new BoxTrix().enterEventDispatcher();  }

}  // end of BoxTrix class


