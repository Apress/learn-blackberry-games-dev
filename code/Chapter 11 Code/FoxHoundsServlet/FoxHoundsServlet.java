
// FoxHoundsServlet.java
// Andrew Davison, November 2009, ad@fivedots.coe.psu.ac.th

/* A server for the fox and hounds game in servlet form.
   It accepts messages from the player as arguments to its
   URL (i.e. as GET method arguments).

  Player Messages:

    * FoxHoundsServlet?cmd=hi&uid=??
        - return map or a game-over message

    * FoxHoundsServlet?cmd=loc&uid=??&lat=??&long=??
        - receive player's current GPS (latitude, longitude) location
        - send back all players details:
               id x y player-state
                   :

    * FoxHoundsServlet?cmd=kill&uid=??&kid=??
        - kill request
        - send back response


  A map is added to the server by the organizer before the game starts,
  located in the MAP_FNM file.

  A UID is allocated to a player by the organizer before the game starts,
  and the ID info and map details are read in from the PLAYERS_FNM file.


  Server-side infomation includes details about each player
  (uid, alive/dead).
*/

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;



public class FoxHoundsServlet extends HttpServlet
{
  private static final int MAX_PLAYERS = 5;

  private static final int MIN_REQUEST_PERIOD = 2*60;    // 2 minutes in seconds
  private static final long GAME_TIME_LENGTH = 30*60;    // 30 minutes in seconds

  private static final String PLAYERS_FNM = "players.txt";
        /*  format:   map center latitude
                      map center longitude
                      map zoom
                      player id1     // if id starts with 'H' than is hound; 'F' means fox
                      player id2     // one fox, 4 hounds
                        :
        */

  private static final String MAP_FNM = "map.jpg";



  private Player[] players;   // for storing player information

  private BufferedImage mapIm = null;
  private String imMimeType = null;   // mime type of the image

  private MapCoords mapCoords;     // for converting (lat,long) info into image coords
 
  private long startTime = -1;     // in seconds; reset whenever fox says hi
  private boolean isGameOver = false;
  private String gameOverMessage;

  // private Logger log = null;



  public void init() // throws ServletException
  {  
    ServletContext sc = getServletContext();

    //load player info and the map
    String playersFnm = sc.getRealPath(PLAYERS_FNM);
    loadPlayersInfo(playersFnm);

    String mapFnm = sc.getRealPath(MAP_FNM);
    loadMap(mapFnm);
    imMimeType = sc.getMimeType(mapFnm);
  }  // end of init()



  private void loadPlayersInfo(String playersFnm)
  {
    try {
      BufferedReader in = new BufferedReader( new FileReader( playersFnm ));

      // load map details
      String latStr = in.readLine();    // latitude
      String lonStr = in.readLine();    // longitude
      String zoomStr = in.readLine();   // zoom factor
      mapCoords = new MapCoords(latStr, lonStr, zoomStr);

      // store player IDs
      players = new Player[MAX_PLAYERS];  
      String line;
      int i = 0;
      while (((line = in.readLine()) != null) && (i < MAX_PLAYERS)) {
        players[i] = new Player(line.trim());
        i++;
      }
      in.close();
    }
    catch (IOException e) 
    {  System.out.println("Problem reading " + playersFnm);  }
  }  // end of loadPlayersInfo()



  private void loadMap(String mapFnm)
  {
    try {
      mapIm = ImageIO.read( new File(mapFnm) );
    }
    catch(IOException e)
    {  System.out.println("Could not read map from " + mapFnm);  }

    if (mapIm != null) 
       mapCoords.setImageSize( mapIm.getWidth(), mapIm.getHeight() );
  }  // end of loadMap()



  // ------------------------ processing GET requests ----------------------------


  public void doGet( HttpServletRequest request,
                      HttpServletResponse response ) throws IOException
  // look at the cmd parameter to decide which message the player sent
  {
    if (isGameOver) {
      PrintWriter output = response.getWriter();
      output.println("GAME_OVER " + gameOverMessage);
      output.close();
    }
    else {   // game isn't over
      // check if game playing time has expired
      long currTime = System.currentTimeMillis()/1000;   // in seconds
      if ((startTime > -1) && ((currTime - startTime) > GAME_TIME_LENGTH)) { 
        isGameOver = true;
        gameOverMessage = "Playing time ended:\nfox wins";
        PrintWriter output = response.getWriter();
        output.println("GAME_OVER " + gameOverMessage);
        output.close();
      }
      else   // still time to play
        processCmd(request, response);
    }
  }  // end of doGet()


  private void processCmd(HttpServletRequest request,
                         HttpServletResponse response) throws IOException
  /*  Cmd formats:
        FoxHoundsServlet?cmd=hi&uid=??
        FoxHoundsServlet?cmd=loc&uid=??&lat=??&long=??
        FoxHoundsServlet?cmd=kill&uid=??&kid=??
  */
  {
    // check the player ID first, which is used by all the commands
    String uid = request.getParameter("uid");
    Player p = findPlayer(uid);
    if (p == null) {
      PrintWriter output = response.getWriter();
      output.println("Player ID not found");  // request rejected
      output.close();
    }
    else {   // player was found, now process command
      String command = request.getParameter("cmd");
      if (command.equals("hi"))
        processHi(p, response);
      else if (command.equals("loc"))
        processLoc(p, request, response);
      else if (command.equals("kill"))
        processKill(p, request, response);
      else {
        PrintWriter output = response.getWriter();
        output.println("Command not understood: " + command);  // request rejected
        output.close();
      }
    }
  }  // end of processCmd()



  private Player findPlayer(String uid)
  // reurn the player with ID == uid, or null
  {
    for (Player p : players)
      if (p.hasUID(uid))
        return p;
    return null;
  }  // end of findPlayer()


  // ---------------------------- hi command ----------------------------------


  private void processHi(Player p, HttpServletResponse response) throws IOException
  /* Message format: FoxHoundsServlet?cmd=hi&uid=??
       The server returns a map
  */
  {
    sendMap(response);
    if (p.isFox() && (startTime == -1))
      startTime =  System.currentTimeMillis()/1000;   // set when fox first says hi
  }  // end of processHi()




  private void sendMap(HttpServletResponse resp) throws IOException
  {
    if ((mapIm == null) || (imMimeType == null)) {
      PrintWriter output = resp.getWriter();
      output.println("Map not found");  // request rejected
      output.close();
    }
    else {
      resp.setContentType(imMimeType);

      // image ==> byte array
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(mapIm, "jpg", baos);
      byte[] buf = baos.toByteArray();

      resp.setContentLength((int) buf.length);  // set content size

      OutputStream out = resp.getOutputStream();
      out.write(buf);
      out.flush();
      out.close();
    }
  }  // end of sendMap()



  // ---------------------------- loc command ----------------------------------


  private void processLoc(Player p, HttpServletRequest request,
                         HttpServletResponse response) throws IOException
  /* Message format: FoxHoundsServlet?cmd=loc&uid=??&lat=??&long=??
        - receive player's current GPS (latitude, longitude) location
        - send back all players details:
               id x y player-state
                   :
  */
  { // convert (lat,long) to map image coordinates
    double lat = getGPSCoord( request.getParameter("lat"));
    int yCoord = mapCoords.lat2y(lat);

    double lng = getGPSCoord( request.getParameter("long"));
    int xCoord = mapCoords.lon2x(lng);

    PrintWriter output = response.getWriter();
    if ((xCoord < 0) || (xCoord >= mapIm.getWidth()) ||
        (yCoord < 0) || (yCoord >= mapIm.getHeight()) ) {
      output.println("You've dropped off the map"); 
      output.println("lat: " + lat + " --> " + yCoord); 
      output.println("long: " + lng + " --> " + xCoord); 
    }
    else {
      p.storeCoord(xCoord, yCoord);  // store new coordinates for this player
      sendLocations(p, output);      // send back all players details
    }
    output.close();
  }  // end of processLoc()



  private double getGPSCoord(String coordStr)
  // convert string to double
  {
    if (coordStr == null)
      return -1.0;
     double gpsCoord = -1.0;
     try {
       gpsCoord = Double.parseDouble(coordStr);
     }
     catch (NumberFormatException ex){}
     return gpsCoord;
  }  // end of getGPSCoord()
  


  private void sendLocations(Player p, PrintWriter output)
  /* a fox can get player locations at any time, but a hound must 
     wait MIN_REQUEST_PERIOD seconds between location requests */
  {
    if (p.isFox()) {
      output.println("LOCS"); 
      for (Player pl : players)    // always send info to the fox
        output.println( pl.toString() ); 
    }
    else {   // p is a hound, so check request interval
      long requestTime = System.currentTimeMillis()/1000;   // in seconds
      if ((requestTime - p.getLocRequestTime()) > MIN_REQUEST_PERIOD) {
        output.println("LOCS"); 
        for (Player pl : players)
          output.println( pl.toString() ); 
        p.setLocRequestTime(requestTime);
      }
      else
        output.println("TOO SOON");   // loc request is too soon
    }
  }  // end of sendLocations()



  // ---------------------------- kill command ----------------------------------


  private void processKill(Player p, HttpServletRequest request,
                           HttpServletResponse response) throws IOException
  /* Message format: FoxHoundsServlet?cmd=kill&uid=??&kid=??
        - process kill request
  */
  { Player target = findPlayer( request.getParameter("kid") );  
                   // get the player who is meant to be killed

    PrintWriter output = response.getWriter();
    if (target == null)
      output.println("Target not found");
    else if (!p.isAlive())
      output.println("Zombie player not allowed");
    else if (!target.isAlive())
      output.println("Target already dead"); 
    else  
      killPlayer(p, target, output);
    output.close();
  }  // end of processKill()



  private void killPlayer(Player p, Player target, PrintWriter output)
  // try to have player p kill the traget player; report the result
  {
    synchronized(target) {  // target can only be killed by one thread at a time
      if (p.isFox() && target.isHound()) {  // fox can kill hound
        target.setAlive(false);   // kill hound
        if (allHoundsDead()) {   
          isGameOver = true;
          gameOverMessage = "Fox wins";
          output.println("GAME_OVER " + gameOverMessage);
        }
        else 
          output.println("Hound killed"); 
      }
      else if (p.isHound() && target.isFox()) {   // hound can kill fox
        target.setAlive(false);   // kill fox
        isGameOver = true;
        gameOverMessage = "Hound " + p.getID() + " wins";
        output.println("GAME_OVER " + gameOverMessage);
      }
      else
        output.println("Kill rejected"); 
    }
  }  // end of killPlayer()



  private boolean allHoundsDead()
  // are all the hounds dead?
  {
    for (Player p : players) {
      if (p.isHound() && p.isAlive())
        return false;  // since a hound is still alive
    }
    return true;
  }  // end of allHoundsDead()


} // end of FoxHoundsServlet class

