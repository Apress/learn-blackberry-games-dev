
// Utils.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, November 2009

// Various useful utilities.


import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.system.*;

import java.util.Vector;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;


public class Utils
{

  public static Vector split(String s, String delimiter)
  // split s into a vector of strings based on he supplied delimiter
  /* from the article:
        "Create BlackBerry applications with open source tools, 
         Part 2: Building an RSS reader" by Frank Ableson, February 2009
         http://www.ibm.com/developerworks/opensource/tutorials/os-blackberry2/index.html
  */
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
      ui.pushGlobalScreen(dialog, 1, UiEngine.GLOBAL_QUEUE);
    }
  }  // end of showMessage()


}  // end of Utils class
