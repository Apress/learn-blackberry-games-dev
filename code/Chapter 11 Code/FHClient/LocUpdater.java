
// LocUpdater.java
// Andrew Davison, November 2009, ad@fivedots.coe.psu.ac.th

/* A thread which periodically sends a "loc" command to the server:
       FoxHoundsServlet?cmd=loc&uid=??&lat=??&long=??
   This informs the server of the player's current (latitude, longitude).

   The latitude and logitude come from a GPSLocator object, which uses
   the BlackBerry's GPS support.

   The response from the server is a list of all the players details,
   in the format
               first-letter-of-id x y player-state
                   :

   But if the request comes from a hound, the data is only returned after
   a specified interval since the last "loc" result.
*/

import net.rim.device.api.ui.component.*;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.location.*;



public class LocUpdater extends Thread
{
  private static final int SLEEP_TIME = 35*1000;    // 35 secs between polling

  private static final String SERVER = "http://FOX_HOUNDS.COM/FoxHoundsServlet";
            /*   *** CHANGE THIS URL *** */

  private volatile boolean isRunning = true;   // used to stop the thread
  private ImageScreen imageScreen;
  private String uid;
  private GPSLocator gpsLocator;


  public LocUpdater(String id, ImageScreen imScr)
  {  
    uid = id;
    imageScreen = imScr;
    gpsLocator = new GPSLocator();   // GPS object
  }


  public void stopUpdating()
  {  isRunning = false;  }


  public void run()
  /* get the position, send a "loc" command, process the response, sleep,
     and then start again. */
  { 
    while(isRunning) {
      Coordinates coord = gpsLocator.getCoord();
      if (coord == null)
        System.out.println("No GPS coordinate found");
      else
        requestLoc(uid, coord);    // send a "loc" request
      try {
        Thread.sleep(SLEEP_TIME);  // sleep for some ms
      }
      catch(InterruptedException ex){}
    }
    gpsLocator.stop();
  } // end of run()




  private void requestLoc(String uid, Coordinates coord)
  /* send a "loc" request to the server:
       FoxHoundsServlet?cmd=loc&uid=??&lat=??&long=??
  */
  {
    // send the GPS-supplied lat and long
    String locDirect = SERVER + "?cmd=loc&uid=" + uid + "&" +
                       "lat=" + coord.getLatitude() + "&" +
                       "long=" + coord.getLongitude() +  ";deviceside=true";

    HttpConnection conn = null; 
    InputStream inStream = null; 
    try { 
      conn = (HttpConnection) Connector.open(locDirect, Connector.READ, true); 
      inStream = conn.openInputStream();
      if (conn.getResponseCode() == HttpConnection.HTTP_OK)
        downloadReply(inStream);    // extract answer
    } 
    catch (IOException ex)
    { Utils.showMessage("Error", "Location update failed"); } 
    finally { 
      try { 
        inStream.close(); 
        inStream = null; 
        conn.close(); 
        conn = null; 
      } 
      catch (Exception e) {} 
    } 
  }  // end of requestLoc()



  private void downloadReply(InputStream inStream) throws IOException
  // process "loc" command reply from the server
  {
    byte[] buffer = new byte[256];
    StringBuffer sb = new StringBuffer();
    int len = 0;
    while ((len = inStream.read(buffer)) != -1)
      sb.append(new String(buffer, 0, len));

    String reply = sb.toString();
    
    if (reply.startsWith("GAME_OVER")) {   // tell ImageScreen that the game is over
      imageScreen.finishGame(reply.substring(10));
      isRunning = false;
    }
    else if (reply.startsWith("LOCS"))  // pass location info to ImageScreen
      imageScreen.updateLocs(reply.substring(5));
    else if (reply.startsWith("TOO SOON")) {} // request was too soon, so do nothing
    else
      Utils.showMessage("Location Error", reply);
  }  // end of downloadReply()

}  // end of LocUpdater class
