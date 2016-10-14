
// BaseStation.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2009

/* A Blutooth server and USB message sender to the Dream Cheeky car
    - uses JavaSE, libusbJava, and BlueCove

   Usage:
      > compile *.java
      > run BaseStation
            - use ctrl-C to stop the server

   The client for this server is eith BlueCarControls on a PC or
   CarControls on a BlackBerry.
*/

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.bluetooth.*;


public class BaseStation
{
  // UUID and name of the basestation service
  private static final String UUID_STRING = "11111111111111111111111111111111";
       // 32 hex digits which will become a 128 bit ID
  private static final String SERVICE_NAME = "basestation";   // use lowercase

  private static USBCar carDev;   // the USB interface to the car



  public static void main(String args[])
  {
    carDev = new USBCar((short)0x0a81, (short)0x0702);
          // vendor and product IDs obtained by looking at the car using USBDeview

    try {  // make the server's device discoverable
      System.out.println("Setting device to be discoverable...");
      LocalDevice local = LocalDevice.getLocalDevice();
      local.setDiscoverable(DiscoveryAgent.GIAC);

      /* Create a RFCOMM connection notifier for the server, with 
         the given UUID and name. This also creates a service record. */
      System.out.println("Start advertising basestation service...");
      StreamConnectionNotifier server = 
          (StreamConnectionNotifier) Connector.open(
                  "btspp://localhost:" + UUID_STRING + ";name=" + SERVICE_NAME);

      while (true) {
        System.out.println("Waiting for incoming connection...");
        StreamConnection conn = server.acceptAndOpen(); 
           // wait for a client connection
           /* acceptAndOpen() also adds the service record to the 
              device's SDDB, making the service visible to clients */
        System.out.println("Connection requested...");
        processClient(conn); 
      }
    }
    catch (Exception e) {
      System.out.println(e);
    }

    carDev.close();
  }  // end of main()



  // -------------- process client IO ------------------------

  private static void processClient(StreamConnection conn)
  // communicate with the Bluetooth client using IO streams
  {
    try {
      reportDeviceName(conn);

      /* Get an InputStream and OutputStream from the stream connection,
         and start processing client messages. */
      InputStream in = conn.openInputStream();
      OutputStream out = conn.openOutputStream();
      processMsgs(in, out);

      System.out.println("Close down connection");
      if (conn != null) {
        in.close();
        out.close();
        conn.close();
      }
    }
    catch (IOException e) 
    {  System.out.println(e);  }
  } // end of processClient()



  private static void reportDeviceName(StreamConnection conn)
  /* Return the 'friendly' name of the device being examined,
     or "Device ??" */
  {
    String devName;
    try {
      RemoteDevice rd = RemoteDevice.getRemoteDevice(conn);
      devName = rd.getFriendlyName(false);  // to reduce connections
    }
    catch (IOException e) 
    { devName = "Device ??";  }

    System.out.println("Connection made by device: " + devName);
  }  // end of reportDeviceName()




   private static void processMsgs(InputStream in, OutputStream out)
   /* When a client message comes in, pass it to processMsg().
      If the client sends "bye$$", or there's a problem, then
      terminate processing.
   */
   {
     boolean isRunning = true;
     String line;
     while (isRunning) {
       if((line = readData(in)) == null)   // there's a problem
         isRunning = false;
       else {  // there was some input
         // System.out.println("  --> sent: \"" + line + "\"");
         if (line.equals("bye$$")) {
           isRunning = false;
           sendMessage(out, "ok: exiting");
         }
         else
           processMsg(out, line);
       }
     }
   }  // end of processMsgs()



  private static void processMsg(OutputStream out, String line)
  /* Format of an input line:  "direction true\false"  
         2nd argument is whether the direction button is pressed or not
     There are 6 possible directions that the car can go.
     The direction may also be "carHorn", which is currently ignored.
  */
  {
    System.out.println("Processing client message: " + line);

    String[] tokens = line.split("\\s+");
    if (tokens.length != 2) {
      System.out.println("error: wrong no. of tokens");
      sendMessage(out, "error: wrong no. of tokens");
      return;
    }

    if (tokens[1].equals("false")) {    // button not pressed, i.e. released
      carDev.stop();
      sendMessage(out, "ok: stopped");
    }
    else {   // pressed a direction button
      if (tokens[0].equals("left")) {
        carDev.turnLeft();
        sendMessage(out, "ok: turned left");
      }
      else if (tokens[0].equals("fwd")) {
        carDev.forward();
        sendMessage(out, "ok: forward");
      }
      else if (tokens[0].equals("right")) {
        carDev.turnRight();
        sendMessage(out, "ok: turned right");
      }
      else if (tokens[0].equals("revLeft")) {
        carDev.revLeft();
        sendMessage(out, "ok: reversed left");
      }
      else if (tokens[0].equals("rev")) {
        carDev.reverse();
        sendMessage(out, "ok: reversed");
      }
      else if (tokens[0].equals("revRight")) {
        carDev.revRight();
        sendMessage(out, "ok: reversed right");
      }
      else if (tokens[0].equals("carHorn"))  {  // no direction action
        sendMessage(out, "ok: horn ignored");
      }  
      else {
        System.out.println("Did not recognize the direction; stopping");
        carDev.stop();
        sendMessage(out, "error: unknown direction");
      }
    }
  }  // end of processMsg()



  // --------------- IO methods ---------------------------


  private static String readData(InputStream in)
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
    return new String(data).trim();  // convert byte[] to trimmed String
  }   // end of readData()



  private static boolean sendMessage(OutputStream out, String msg)
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

} // end of BaseStation class
