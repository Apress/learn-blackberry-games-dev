
Chapter 11. Fox and Hounds

From the book:

  Learn BlackBerry Games Development with Java ME Platform
  Carol Hamer and Andrew Davison
  Apress, 2010
  Website: ??


Author responsible for this chapter:
    Andrew Davison
    Dept. of Computer Engineering
    Prince of Songkla University
    Hat yai, Songkhla 90112, Thailand
    E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention the book's title and authors, 
and include a link to the website.

Thanks,
  Carol and Andrew


============================
Directory contents:
     
    FHClient.java, IDScreen.java, ImageScreen.java,
    PlayerLoc.java, LocUpdater.java, GPSLocator.java,
    KillPopupScreen.java, Utils.java
       - the Fox and Hounds client (8 Java files)

   images\
      - foxAlive.png, foxDead.png,
        dogAlive.png, dogDead.png
        IDTitleBar.png, title.png

   fox.png
      - the RIMlet icon

   FHClient.jdp, FHClient.rapc
       - sample BlackBerry JDE project files


----------------------------
Compilation and Execution

This application is a little tricky to get working.
Please read "Contacting FoxHoundsServlet" and 
"Using GPS" below before compiling/executing the code.


Inside the JDE, create a new Project called FHClient, and
add:
  - the 8 Java files
  - all the images inside images\,

 Make fox.png the resource icon file (via the projects properties)

 Build and Run inside the simulator.


----------------------------
Device Installation

To run FHClient on a real device, you will need to:
  * build the code
  * create an ALX file using  Build > Generate ALX file
  * sign the code using Build > Request Signatures


----------------------------
Contacting FoxHoundsServlet


1.  The client code assumes that the server is located at:
      http://FOX_HOUNDS.COM/FoxHoundsServlet

    This MUST BE CHANGED to match the real URL of your server.

    The changes must be made to the SERVER constant in 3 files:
      - ImageScreen.java,
      - KillPopupScreen.java
      - LocUpdater.java

-----
2.  Before you can test the server from a BlackBerry, the game organizer must
    create a map and player.txt file for your locality. Otherwise when FHClient
    contacts the server with its latitude and longitude, the server will reject it
    since your device's location does not match the map's coordinates.

-----
3.  FHClient sets up a HTTPConnection with the server using direct TCP. As explained
    in the chapter this may not be supported by your wireless carrier
    (see the section entitled "Creating a HTTP Connection"). 

    A reasonable alternative is to use WAP 2.0 instead.

-----
4.  If you're having trouble getting FHClient to communicate with the server, 
    then first try communicating with it more directly, through a browser. 
    See the "Test Execution" section in the readme.txt file in FoxHoundsServlet/
    for how to do that.

----------------------------
Using GPS


1.  You can 'fake' a GPS location in the simulator via the 
    Simulate >> GPS Location menu item. Enter coordinates which are on
    the server's map and click Update.

    For the map supplied in this download, suitable coordinates are:
       latitude = 7.001156
       longitude = 100.491943


-----
2.  The GPS-specific code for FHClient is in GPSLocator.java.

    GPSLocator uses GPS in autonomous mode, and tries to keep the GPS link
    active so that there is little delay in receiving position fixes.

    Even so, there will be a wait when your device first starts
    (perhaps 2 minutes) before the first position is sent to the server.

    This means that your player icon will not appear on the map for at
    least 2 minutes after you switch on the device's GPS receiver.


-----
3.  As explained in the chapter, autonomous mode may not be supported
    by your device and/or carrier (see the section entitled
    "GPS and the BlackBerry"). 

    For a quick check, look at:
       "What Is - The BlackBerry Smartphone Models and their Corresponding GPS Capabilities"
       http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800332/800703/What_Is_-_The_BlackBerry_smartphone_models_and_their_corresponding_GPS_capabilities.html?nodeid=1371352&vernum=0 
            (note: this page is best viewed in FireFox)

    You may need to use GPS assisted mode instead.


---------
Last updated: 23rd November 2009
