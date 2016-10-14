
// KillPopupScreen.java
// Andrew Davison, November 2009, ad@fivedots.coe.psu.ac.th

/* Send a 'kill' request to the server, using the format
           FoxHoundsServlet?cmd=kill&uid=??&kid=??

   The user ID (uid) comes from the ImageScreen, and the target player
   ID (kid) is obtained from the user via an edit field, "Ok" and 
   "Cancel" buttons.

   If the reply is a "game over" message, then the ImageScreen is informed, 
   otherwise the reply message is shown in a dialog box.
*/


import net.rim.device.api.system.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;



public class KillPopupScreen extends PopupScreen
                                  implements FieldChangeListener
{
  private static final String SERVER = "http://FOX_HOUNDS.COM/FoxHoundsServlet";
            /*   *** CHANGE THIS URL *** */
  
  private String uid;
  private ImageScreen imageScreen;
  
  // GUI elements
  private ButtonField okButton;
  private ButtonField cancelButton;
  private EditField idEditField;


  public KillPopupScreen(String id, ImageScreen imScr)
  {
    super( new FlowFieldManager() );

    uid = id;
    imageScreen = imScr;
    
    // build the GUI
    add( new RichTextField("Kill Player\n", NON_FOCUSABLE) );
    
    idEditField = new EditField("  ID: ", "", 7, EditField.NO_NEWLINE);
    add(idEditField);
    add( new LabelField("\n\n"));
    
    okButton = new ButtonField("Ok");
    okButton.setChangeListener(this);
    add(okButton);

    cancelButton = new ButtonField("Cancel");
    cancelButton.setChangeListener(this);
    add(cancelButton);
  }  // end of KillPopupScreen()



  public void fieldChanged(Field field, int context)
  // handle the pressing of the ok or cancel button
  {
    if (field == okButton) {    // send a "kill" request to the server
      String targetID = idEditField.getText();
      sayKillToServer(targetID);
      onClose();
    }
    if (field == cancelButton)
      onClose();
  }  // end of fieldChanged()



  public boolean onClose()
  { UiApplication.getUiApplication().popScreen(this);
    return true;
  }



  // --------------------- server communication --------------------------


  private void sayKillToServer(final String targetID) 
  // request a kill and deal with the server's response
  { 
    Thread t = new Thread( new Runnable() { 
      public void run() 
      { 
        String killReply = requestKill(targetID); 
        if (killReply.startsWith("GAME_OVER"))     // the kill caused the game to finish
          imageScreen.finishGame(killReply.substring(10));   // tell the ImageScreen
        else
          Utils.showMessage("Kill Reply", killReply);
      } 
    }); 
    t.start(); 
  }  // end of sayKillToServer()



  private String requestKill(String targetID)
  /* Create a FoxHoundsServlet?cmd=kill&uid=??&kid=?? URL and send it 
     directly to the server. Extract the answer from the text returned.
  */
  {
    String killReply = "Kill Request failed";
    String hiDirect = SERVER + "?cmd=kill&uid=" + uid + "&" +
                                  "kid=" + targetID + ";deviceside=true";

    HttpConnection conn = null; 
    InputStream inStream = null; 
    try { 
      conn = (HttpConnection) Connector.open(hiDirect, Connector.READ, true); 
      inStream = conn.openInputStream(); 
      if (conn.getResponseCode() == HttpConnection.HTTP_OK)
        killReply = downloadReply(inStream);  // extract reply from text returned by server
    } 
    catch (IOException ex)
    { System.out.println(ex); } 
    finally { 
      try { 
        inStream.close(); 
        inStream = null; 
        conn.close(); 
        conn = null; 
      } 
      catch (Exception e) {} 
    } 

    return killReply;
  }  // end of requestKill()

  

  private String downloadReply(InputStream inStream) throws IOException
  // extract a reply from the text sent along the stream from the server
  {
    byte[] buffer = new byte[256];
    StringBuffer sb = new StringBuffer();
    int len = 0;
    while ((len = inStream.read(buffer)) != -1)
      sb.append(new String(buffer, 0, len));

    return sb.toString();
  }  // end of downloadReply()


}  // end of KillPopupScreen class
