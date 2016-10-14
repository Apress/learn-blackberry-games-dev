
// CarControls.java
// Andrew Davison, July 2009, ad@fivedots.coe.psu.ac.th

/* A Bluetooth client used to communicate with the BaseStation Bluetooth
    server which controls a Dream Cheeky car.

    This is a BlackBerry version of the PC application called BlueCarControls.
*/


import java.io.*;
import javax.microedition.io.*;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.*;

import net.rim.device.api.bluetooth.*;

import net.rim.device.api.system.*;
import net.rim.device.api.system.UnsupportedOperationException;
            // To disambiguate from java.lang.UnsupportedOperationException.



public class CarControls extends UiApplication 
{

  private StreamConnection blueConn;  // for the server
  private InputStream in;     // stream from server
  private OutputStream out;   // stream to server

  private String response;
  private EditField statusEF;
  private boolean isClosed = true;   
       // is the connection to the server closed?


  public CarControls()
  {
    ClipsPlayer player = new ClipsPlayer();  // for playing car sound clips
    player.load("ignition");

    MainScreen mainScreen = new MainScreen();
    mainScreen.setTitle( new LabelField("Car Controls",
                          LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH));

    makeGUI(mainScreen, player);
    pushScreen(mainScreen);

    player.play("ignition");   // 'start' the car

    makeConnection();   // to the Bluetooth server
  }  // end of CarControls()
  
  

  private void makeGUI(MainScreen mainScreen, ClipsPlayer player)
  {
    VerticalFieldManager vfm = new VerticalFieldManager();

    vfm.add( new RichTextField(Field.NON_FOCUSABLE) );   // a blank row
    
    // top row of buttons
    HorizontalFieldManager rowMan2 = new HorizontalFieldManager(Field.FIELD_HCENTER);
    rowMan2.add( new PictureButton(this, player, "left") );
    rowMan2.add( new PictureButton(this, player, "fwd") );
    rowMan2.add( new PictureButton(this, player, "right") );
    vfm.add(rowMan2);

    // second row of buttons
    HorizontalFieldManager rowMan3 = new HorizontalFieldManager(Field.FIELD_HCENTER);
    rowMan3.add( new PictureButton(this, player, "revLeft") );
    rowMan3.add( new PictureButton(this, player, "rev") );
    rowMan3.add( new PictureButton(this, player, "revRight") );
    vfm.add(rowMan3);

    // car horn button
    HorizontalFieldManager rowMan4 = new HorizontalFieldManager(Field.FIELD_HCENTER);
    rowMan4.add( new PictureButton(this, player, "carHorn") );
    vfm.add(rowMan4);

    statusEF = new EditField(Field.READONLY);    // status text field
    vfm.add(statusEF);

    mainScreen.add(vfm);
  }   // end of makeGUI()



  public void dirStatus(String buttonName, boolean isPressed)
  // called by the buttons
  { 
    // System.out.println(buttonName + " pressed:" + isPressed);  
    response = contactServer(buttonName + " " + isPressed);
    System.out.println("Response: " + response);
    invokeLater( new Runnable() {
      public void run() 
      {  statusEF.setText(response); }
    });
  } // end of dirStatus()


        
  
  // -------------------------IO to server ----------------------


  private void makeConnection()
  // Treat the connection to the server as IO streams.
  {
    try {
       // access the Bluetooth serial port connection
      BluetoothSerialPortInfo[] info = BluetoothSerialPort.getSerialPortInfo();
      if ((info == null) || (info.length == 0))
        closeConnection("No bluetooth serial ports available for connection");

      // connect to the server, and extract IO streams
      blueConn = (StreamConnection) Connector.open(
                             info[0].toString(), Connector.READ_WRITE);
      System.out.println("makeConnection() 3");

      // System.out.println("Opened a connection to server");
      out = blueConn.openOutputStream();         
      in = blueConn.openInputStream();

      System.out.println("Connected to service");
      isClosed = false;   // i.e. the connection is open
    }
    catch (IOException e) {
      closeConnection("Unable to open serial port");
    }
    catch (UnsupportedOperationException e) {
      closeConnection("This handheld or simulator does not support bluetooth");
    }
  }  // end of makeConnection()



  private void closeConnection(final String message)
  {
    invokeLater( new Runnable() {
      public void run()
      { 
        Dialog.alert(message);
        closeDown();
        // System.exit(1);
      }
    });
  }  // end of closeConnection()


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
    System.out.println("sendMessage: " + msg);
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

    // System.out.println("readData: " + new String(data));
    return new String(data);  // convert byte[] to String
  }   // end of readData()


  // ---------------------------------------------------------


  public static void main(String[] args)
  {
    CarControls theApp = new CarControls();
    theApp.enterEventDispatcher();
  }

}  // end of CarControls class
