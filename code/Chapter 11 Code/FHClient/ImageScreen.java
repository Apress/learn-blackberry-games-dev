
// ImageScreen.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* Start by sending a "hi" command to the server:
       FoxHoundsServlet?cmd=hi&uid=??
   using the ID obtained from the IDScreen

   If the ID matches, a map will be sent back or an error message.

   Display the map across the entire screen, and allow the user to
   employ the arrow keys to move around. The code assumes the image is
   bigger than the screen.

   A LocUpdater thread is started which will periodically send GPS 
   info to the server, and receive back the locations of all the
   players. This information is passed to ImageScreen via the
   updateLocs() method and stored in a PlayerLoc[] array.

   When the map is drawn, the player locations are drawn on top of
   it.

   A game-over message is drawn on top of eveything else when the 
   game has finished.
*/

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;



public class ImageScreen extends MainScreen
{
  private static final String IMAGES_DIR = "images/";
  private static final String TITLE_FNM = "title.png";

  private static final String SERVER = "http://FOX_HOUNDS.COM/FoxHoundsServlet";
            /*   *** CHANGE THIS URL *** */
  
  private static final int MAX_PLAYERS = 5;     // one fox, four hounds

  private static int STEP = 8;    // step increment for moving the image


  // map image
  private Bitmap mapIm = null;
  private int imWidth, imHeight;
  private boolean mapLoaded = false;
  
  // drawing coords
  private int screenWidth, screenHeight;
  private int xDraw, yDraw;   // position of top-left corner of image on the screen

  private String uid;
  private PlayerLoc[] playerLocs;    // player location details

  private LocUpdater locUpdater = null;   // thread for obtaining location info

  private ImageScreen imScr;    // used to store a reference to this object

  private boolean isGameOver = false;
  private String gameOverMsg;
  private Font endFont;   // the font for the game over message


  public ImageScreen(int sw, int sh, String id)
  {  
    screenWidth = sw;
    screenHeight = sh;
    uid = id;
    imScr = this;
    playerLocs = new PlayerLoc[MAX_PLAYERS];
    
    endFont = Font.getDefault().derive(Font.BOLD, 30);

    mapIm = loadImage(TITLE_FNM);    // display title image while map is downloaded
    drawImage(mapIm);

    sayHiToServer();  // login to F&H server
  }  // end of ImageScreen()
  
  
  
  private Bitmap loadImage(String fnm)
  {
    Bitmap im = null;
    System.out.println("Loading image in " + IMAGES_DIR+fnm);
    try {
      im = Bitmap.getBitmapResource(fnm);
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




  public void updateLocs(String playerLocsStr)
  /* called by LocUpdater thread. The method splits the 
     location string into a substring for each player, 
     and updates the playerLocs[] array */
  {
    Vector lines = Utils.split(playerLocsStr, "\n");   // split according to new lines
    int numPlayers = 0;
    for(int i=0; i < lines.size(); i++) {
      if (numPlayers == MAX_PLAYERS)
        break;
      if (lines.elementAt(i) == null)     // ignore empty lines
        continue;
      String line = ((String) lines.elementAt(i)).trim();
      if (line.equals(""))                // ignore blank lines
        continue;
      synchronized(playerLocs) {      // prevent playerLocs[] being accessed while it is being changed
        playerLocs[numPlayers] = new PlayerLoc(line);
        numPlayers++;
      }
    }
    invalidate();  // redraw
  }  // end of updateLocs()


  
  
  public void finishGame(String msg)
  /* can be called from sayHiToServer(), from LocUpdater or KillPopupScreen 
     to signal the end of the game */
  {
    isGameOver = true;
    gameOverMsg = msg;
    invalidate();  // redraw
  }  // end of finishGame()
  
  
    

  // ------------------------- painting -----------------------------------

  private void drawImage(Bitmap im)
  {
    imWidth = im.getWidth(); 
    imHeight = im.getHeight();
     
    // center the image on the center of the screen
    xDraw = (screenWidth - imWidth)/2; 
    yDraw = (screenHeight - imHeight)/2;
       // (xDraw,yDraw) should be -ve or 0 since the image is bigger than the screen
    limitMovement();
    
    invalidate();        // draw the screen in its initial state
  }  // end of drawImage()



  private void limitMovement()
  /* adjust (xDraw,yDraw), so map's movement doesn't expose the screen
     underneath the map */
  {
    // prevent map's right edge moving left of the right screen edge
    if (xDraw < (screenWidth-imWidth))
      xDraw = screenWidth-imWidth;

    // prevent map's left edge moving right of the edge screen edge
    if (xDraw > 0)
      xDraw = 0;
          
    // prevent map's bottom edge moving above the bottom screen edge
    if (yDraw < (screenHeight-imHeight))
      yDraw = screenHeight-imHeight;
      
    // prevent map's top edge moving below the top screen edge
    if (yDraw > 0)
      yDraw = 0;
   }  // end of limitMovement()   




  protected void paint(Graphics g)
  // draw the map, player locations, and perhaps a game-over message
  {
    // a black background
    g.setColor(Color.BLACK);
    g.fillRect (0, 0, screenWidth, screenHeight);

    if (mapIm != null)    // draw the map
      g.drawBitmap(0, 0, screenWidth, screenHeight, mapIm, -xDraw, -yDraw);

    // draw player locations
    if (mapLoaded)
      synchronized(playerLocs) {
        for(int i=0; i < MAX_PLAYERS; i++)
          if (playerLocs[i] != null) {
            // System.out.println("paint(): painting player " + i);
            playerLocs[i].draw(g, screenWidth, screenHeight, xDraw, yDraw);
          }
      }
    
    if (isGameOver)
      showGameOver(g);
  }  // end of paint()



  private void showGameOver(Graphics g)
  // draw game-over message at center of panel, spread over two lines
  {
    g.setColor(Color.YELLOW);
    g.setFont(endFont);
    
    int y = (screenHeight - endFont.getHeight())/2;   // vertical centering
    drawCentered(g, "Game Over", y);    // first line

    y += endFont.getHeight() + 8;
    drawCentered(g, gameOverMsg, y);    // second line
  }  // end of showGameOver()
  
  
  
  private void drawCentered(Graphics g, String msg, int y)
  // draw msg, horizontally centered on the screen
  {
    int txtWidth = endFont.getAdvance(msg);
    int x = (screenWidth - txtWidth)/2; 
    g.drawText(msg, x, y, DrawStyle.BOTTOM|DrawStyle.HCENTER, txtWidth+1);   // +1 to avoid clipping
  } // end of drawCentered()


  // --------------------- server communication --------------------------


  private void sayHiToServer() 
  /* Send a "hi" message to the server, and perhaps get back 
    a map and text message. If we get a map then start
    sending periodic "loc" messages to update my position with the
    server, and receive players' locations in return.
  */
  { Thread t = new Thread( new Runnable() { 
      public void run() 
      { 
        String hiReply = requestHi(uid);  
        if (hiReply.equals("Map received")) {
          mapLoaded = true;
          drawImage(mapIm);
          
          // start polling for locations
          locUpdater = new LocUpdater(uid, imScr);
          locUpdater.start();
        }
        else if (hiReply.startsWith("GAME_OVER"))
          finishGame( hiReply.substring(10) );
        else  // report error
          Utils.showMessage("Error", hiReply);
      } 
    }); 
    t.start(); 
  }  // end of sayHiToServer()


  private String requestHi(String id)
  // send a "hi" message to the server, and process the reply
  {
    String hiReply = "Hi failed";
    String hiDirect = SERVER + "?cmd=hi&uid=" + id + ";deviceside=true";

    HttpConnection conn = null; 
    InputStream inStream = null; 
    try { 
      conn = (HttpConnection) Connector.open(hiDirect, Connector.READ, true); 
      inStream = conn.openInputStream();
      
      if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
        if (isJPG(conn)) {   // a map is returned (a JPG)
          mapIm = downloadImage(inStream);
          hiReply = "Map received";
        }
        else   // read text response
          hiReply = downloadReply(inStream);
      }
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
    return hiReply;
  }  // end of requestHi()



  private boolean isJPG(HttpConnection conn) throws IOException
  // is the content of the response a JPG?
  {
    String contentType = conn.getHeaderField("content-type");

    if (contentType == null)
      return false;
    if (contentType.startsWith("image/jpeg")) 
      return true;

    return false;
  }  // end of isJPG



  private Bitmap downloadImage(InputStream inStream) throws IOException
  // read the input stream bytes, converting them into a Bitmap
  {
    byte[] buffer = new byte[256];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (inStream.read(buffer) != -1)
      baos.write(buffer);
    baos.flush();
    baos.close();

    byte[] imageData = baos.toByteArray();
    return Bitmap.createBitmapFromBytes(imageData, 0, imageData.length, 1);
  }  // end of downloadImage()
  
  

  private String downloadReply(InputStream inStream) throws IOException
  // read the input stream bytes, converting them into text message
  {
    byte[] buffer = new byte[256];
    StringBuffer sb = new StringBuffer();
    int len = 0;
    while ((len = inStream.read(buffer)) != -1)
      sb.append(new String(buffer, 0, len));

    return sb.toString();
  }  // end of downloadReply()



  // ----------------------------- user input ---------------------------------


  protected void makeMenu(Menu menu, int instance)
  // add a "kill" menu item to the menu
  {
    menu.add( new MenuItem("Kill" , 100, 10)  {
      public void run()
      { UiApplication.getUiApplication().pushScreen( new KillPopupScreen(uid, imScr)); }
              // call KillPopupScreen to carry out the "kill" request
    });

    menu.addSeparator();
    super.makeMenu(menu, instance);
  }  // add makeMenu

                                  
  protected boolean navigationMovement(int dx,int dy, int status, int time)  
  // convert key presses (and thumb wheel movement) into image shifts
  {
    xDraw -= (STEP*dx);
    yDraw -= (STEP*dy);
    limitMovement();

    invalidate();        // redraw the screen
    return true;
  }  // end of navigationMovement()



  public void close()
  { 
    if (locUpdater != null)
      locUpdater.stopUpdating();
    System.exit(0);
  }   

}  // end of ImageScreen class
