
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
     
    FoxHoundsServlet.java, MapCoords.java, Player.java
       - the Fox and Hounds server, implemented as a servlet (3 Java files)
       - note: this is a Java EE program, not a BlackBerry RIMlet


    map.png, players.txt
       - configuration file examples
       - THESE MUST BE CHANGED by the game organizer 

   compile.bat
      - used to compile the servlet;
      - it uses Apache Tomcat's servlet-api.jar which
        is assumed to be in d:\tomcat\lib\;
      - MODIFY THIS batch file to use the location of your servlet-api.jar


----------------------------
Compilation:

$ compile *.java

The compilation of servlet code requires
extra classpath information pointing to JARs for the 
servlet packages used by your Java EE container.

I used Apache Tomcat v6, which is available from
http://tomcat.apache.org/

MODIFY compile.bat to use the location of your servlet-api.jar

----------------------------
Installation:

1. Add the compiled servlet and its 2 support classes to Tomcat.
   The directory will probably be something like:
       <USER_HOME_DIR>/webapps/WEB-INF/classes

2. Add map.png and players.txt to the 'home' directory for
   FoxHoundsServlet. This will probably be something like:
       <USER_HOME_DIR>/webapps/

3. You will probably need to modify the web.xml file to 'register'
   FoxHoundsServlet with Tomcat. This usually requires the addition of
   the following XML:

  <servlet>
    <servlet-name>FoxHoundsServlet</servlet-name>
    <servlet-class>FoxHoundsServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>FoxHoundsServlet</servlet-name>
    <url-pattern>/FoxHoundsServlet</url-pattern>
  </servlet-mapping>


4. Ask Tomcat to (re)load FoxHoundsServlet.
   This might be done via a Tomcat configuration window or with a special URL.


----------------------------
Test Execution:


You can test the FoxHoundsServlet by typing URLs into a Web browser 

Assuming FoxHoundsServlet is located at:
    http://FOX_HOUNDS.COM/FoxHoundsServlet
and that you are using the map.jpg and players.txt 
supplied in this download.


1. Login as the fox with ID F11111:
http://FOX_HOUNDS.COM/FoxHoundsServlet?cmd=hi&uid=F11111


2. Fox is located at (lat,long) = (7.001156,100.491943)
http://FOX_HOUNDS.COM/FoxHoundsServlet?cmd=loc&uid=F11111&lat=7.001156&long=100.491943
  

3. Login as the hound with ID H22222:
http://FOX_HOUNDS.COM/FoxHoundsServlet?cmd=hi&uid=H22222


4. Hound is located at (lat,long) = (7.000556,100.491943)
http://FOX_HOUNDS.COM/FoxHoundsServlet?cmd=loc&uid=H22222&lat=7.000556&long=100.491943


5. Fox tries to kill hound H22224, but the ID is wrong
http://FOX_HOUNDS.COM/FoxHoundsServlet?cmd=kill&uid=F11111&kid=H22224


6. Hound kills the fox, so the game is over
http://FOX_HOUNDS.COM/FoxHoundsServlet?cmd=kill&uid=H22222&kid=F11111


----------------------------
Real Execution:

Before you can test the server from a BlackBerry, the game organizer must
create a map and player.txt file for your locality. Otherwise when the FHClient
contacts the server with its latitude and longitude, the server will reject it
since the location does not match the map's coordinates.


---------
Last updated: 23rd November 2009
