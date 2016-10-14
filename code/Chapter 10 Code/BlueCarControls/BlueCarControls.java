
// BlueCarControls.java
// Andrew Davison, August 2009, ad@fivedots.coe.psu.ac.th

/*  A Bluetooth client used to communicate with the BaseStation Bluetooth
    server which controls a DreamCheeky car.

    This is a PC version of the BlackBerry application called CarControls.
*/


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;

import javax.microedition.io.*;
import javax.bluetooth.*;



public class BlueCarControls extends JFrame 
{
  // grid size ofot the GUI layout
  private static final int NUM_ROWS = 2;
  private static final int NUM_COLS = 3;

  // UUID and name of the BaseStation service
  private static final String UUID_STRING = "11111111111111111111111111111111";
         // 32 hex digit string (will become 128 bit ID)

  private static final String SERVICE_NAME = "basestation";   // use lowercase
  private static final int MAX_TRIES = 5;

  private ServiceFinder serviceFinder;

  private StreamConnection blueConn;  // for the server
  private InputStream in;     // stream from server
  private OutputStream out;   // stream to server

  private String response;
  private JTextField statusTF;
  private boolean isClosed = true;   
       // is the connection to the server closed?


  public BlueCarControls()
  {
    super("BlueCarControls");

    ClipsPlayer player = new ClipsPlayer();   // for playing car sound clips
    player.load("ignition");
    makeGUI(player);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) 
      {  closeDown();
         System.exit(0);
	  }
    });

    pack();  
    setResizable(false);
    setLocationRelativeTo(null);  // center the window 
    setVisible(true);

    player.play("ignition");  // 'start' the car

    // start looking for the Bluetooth server
    SwingUtilities.invokeLater( new Runnable() {
      public void run() 
      {  statusTF.setText("Looking for base station"); }
    });
    serviceFinder = new ServiceFinder(this, UUID_STRING, SERVICE_NAME);     
  }  // end of BlueCarControls()



  private void makeGUI(ClipsPlayer player)
  {
    Container c = getContentPane();
    c.setLayout( new BoxLayout(c, BoxLayout.Y_AXIS));  // vertical boxes

    JPanel dirPanel = new JPanel();
    dirPanel.setLayout(new GridLayout(NUM_ROWS, NUM_COLS));

    // top row of buttons
    dirPanel.add( new ButtonPanel(this, player, "left"));
    dirPanel.add( new ButtonPanel(this, player, "fwd"));
    dirPanel.add( new ButtonPanel(this, player, "right"));

    // second row of buttons
    dirPanel.add( new ButtonPanel(this, player, "revLeft"));
    dirPanel.add( new ButtonPanel(this, player, "rev"));
    dirPanel.add( new ButtonPanel(this, player, "revRight"));

    c.add(dirPanel);

    // car horn button
    JPanel hornPane = new JPanel();
    hornPane.setBackground(Color.white);
    hornPane.add( new ButtonPanel(this, player, "carHorn"));
    c.add(hornPane);

    // status text field
    JPanel statusPane = new JPanel();
    statusPane.setBackground(Color.white);
    statusTF = new JTextField(20);
    statusTF.setEditable(false);
    statusPane.add(statusTF);
    c.add(statusPane);
  }  // end of makeGUI()




  public void dirStatus(String buttonName, boolean isPressed)
  // called by the button panels
  {  
    // System.out.println(buttonName + " pressed:" + isPressed);  
    response = contactServer(buttonName + " " + isPressed);
    System.out.println("Response: " + response);

    SwingUtilities.invokeLater( new Runnable() {
      public void run() 
      {  statusTF.setText(response); }
    });
  }  // end of dirStatus()



  // ----------------- reporting from ServiceFinder --------------


  public void showServices(Hashtable<String,ServiceRecord> searchTable)
  /* Show the services list, and have the user choose a service if there's more
     than one */
  { 
    if (searchTable == null)
      System.exit(1);

    // list the table of matching servers
    int keysCount = 0;
    String firstKey = null;
    System.out.println("\nDevices:");
    for (Map.Entry e : searchTable.entrySet()) {
      System.out.println("  " + e.getKey()); // + " Value=" + e.getValue());  
      if (keysCount == 0)
        firstKey = (String) e.getKey();
      keysCount++;
    }

    ServiceRecord servRec;
    if (keysCount == 1) {   // only one record so use that
      System.out.println("Using " + firstKey);
      servRec = (ServiceRecord) searchTable.get(firstKey);
    }
    else   // many records, so let the user choose one
      servRec = selectRecord(searchTable);

    if (servRec == null) {
      System.out.println("No matching service found");
      System.exit(1);
    }
    else
      makeConnection(servRec);
  }  // end of showServices() 


  private ServiceRecord selectRecord(Hashtable<String,ServiceRecord> searchTable)
  // have the user select a record; give the user MAX_TRIES before giving up
  {
    try {
      Scanner in = new Scanner(System.in);  
      String key;
      System.out.print("Enter a key: ");
      key = in.nextLine().trim();
      ServiceRecord servRec = getRecord(searchTable, key);
      int numTries = 1;
      while ((servRec == null) && (numTries < MAX_TRIES)) {
        System.out.print("Try again: ");
        key = in.nextLine().trim();
        servRec = getRecord(searchTable, key);
        numTries++;
      }
      return servRec;
    }
    catch(Exception e)
    { return null; }
  }  // end of selectRecord()



  private ServiceRecord getRecord(Hashtable<String,ServiceRecord> searchTable, String key)
  // find the service record with the specified key inside the table
  {
    if ((key == null) || (key.length() == 0)) {
      System.out.println("No key entered");
      return null;
    }

    Enumeration e = searchTable.keys();
    String recKey;
    while(e.hasMoreElements()){
      recKey = (String) e.nextElement();
      if (recKey.contains(key)) {
        System.out.println(key + " matched against key: " + recKey);
        return (ServiceRecord) searchTable.get(recKey);
      }
    }
    System.out.println("No match found for: " + key);
    return null;
  }  // end of getRecord()


  // -------------------------IO to server ----------------------


  private void makeConnection(ServiceRecord servRecord)
  // Treat the connection to the server as IO streams.
  {
    // get a URL for the service
    String servURL = servRecord.getConnectionURL(
                            ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
    // System.out.println("Service URL: " + servURL);

    if (servURL != null) {
      System.out.println("Found service");
      try {
        // connect to the server, and extract IO streams
        blueConn = (StreamConnection) Connector.open(servURL);
        // System.out.println("Opened a connection to server");
        out = blueConn.openOutputStream();         
        in = blueConn.openInputStream();

        System.out.println("Connected to service");
        isClosed = false;   // i.e. the connection is open
        SwingUtilities.invokeLater( new Runnable() {
          public void run() 
          {  statusTF.setText("Connected to service"); }
        });
      } 
      catch (Exception ex) 
      {  System.out.println(ex);  } 
    }
    else
      System.out.println("No service found");
  }  // end of makeConnection()



  // --------------------- IO methods ------------------


  private void closeDown()
  // send "bye$$", then close the link with the service
  {
    if (!isClosed) {
      sendMessage("bye$$");  // tell the server that the client is leaving
      try {
        if (blueConn != null) {
          in.close();
          out.close();
          blueConn.close();
        }
      }
      catch (IOException e) 
      {  System.out.println(e);  }
      isClosed = true;
    }
  }  // end of closeDown();



  private String contactServer(String msg)
  /* This method is used to send a message (msg) to 
     the server, and wait for an answer. The response is returned. 
  */
  {
    if (isClosed)
      return "Error: No Connection to Server";

    if ((msg == null) || (msg.trim().equals("")))
      return "Empty input message";
    else {
      if (sendMessage(msg)) {    // message sent ok
        String response = readData();  // wait for response
        if (response == null) {
          isClosed = true;
          return "Error: Server Terminated Link";
        }
        else  // there was a response
          return response;
      }
      else {   // unable to send message
        isClosed = true;
        return "Error: Connection Lost";
      }
    }
  }  // end of contactServer()



  private boolean sendMessage(String msg)
  // the message format is "<length> msg" in byte form
  {
    // System.out.println("sendMessage: " + msg);
    try {
      out.write(msg.length());
      out.write(msg.getBytes());
      return true;
    } 
    catch (Exception e) 
    {  System.out.println("sendMessage(): " + e);  
       return false;
    } 
  }  // end of sendMessage()


  private String readData()
  /* Read a message in the form "<length> msg".
     The length allows us to know exactly how many bytes to read
     to get the complete message. Only the message part (msg) is
     returned, or null if there's been a problem.
  */
  { byte[] data = null;   
    try {       	      	
      int len = in.read();    // get the message length
      if (len <= 0) {
        System.out.println("Message Length Error");
        return null;
      }
   
      data = new byte[len];
      len = 0;
      // read the message, perhaps requiring several read() calls 
      while (len != data.length) {     
        int ch = in.read(data, len, data.length - len);
        if (ch == -1) {
          System.out.println("Message Read Error");
          return null;
        }
        len += ch;
      }      
    } 
    catch (IOException e) 
    {  System.out.println("readData(): " + e); 
       return null;
    } 

    return new String(data);  // convert byte[] to String
  }   // end of readData()


  // ---------------------------------------------------

  public static void main(String args[])
  {  new BlueCarControls(); }

} // end of BlueCarControls

