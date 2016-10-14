
// FHClient.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, November 2009


import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;


public class FHClient extends UiApplication
{
  private FHClient()
  {  pushScreen(new IDScreen());  }   
   

  // -----------------------------------------


  public static void main(String[] args)
  { FHClient theApp = new FHClient();
    theApp.enterEventDispatcher();
  }


}  // end of FHClient class

