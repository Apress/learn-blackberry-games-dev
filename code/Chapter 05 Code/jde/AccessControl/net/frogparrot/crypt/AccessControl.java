package net.frogparrot.crypt;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.DeviceInfo;

/**
 * A simple encryption/decryption example for BlackBerry.
 * This application reads in a license key that the user has 
 * purchased and checks whether it is a valid key for the 
 * current device by checking it against the device PIN.
 *
 * @author Carol Hamer
 */
public class AccessControl extends UiApplication implements Runnable {

//----------------------------------------------------------------
//  instance fields

  /**
   * The PIN number that uniquely identifies the device.
   */
  int myPin;
  
  /**
   * Whether a valid license has been provided (either this session
   * or from a previously entered license in memory).
   */
  boolean myIsValid;
  
  /**
   * The screen to communicate with the user.
   */
  AccessScreen myAccessScreen;
  
  /**
   * A special inner class that is used to update the display
   * on the event thread (for cases where the update was prompted
   * from another thread).
   */
  Runnable myUpdateScreen = new Thread() {
      public void run() {
         //AccessControl.setMessage("running update screen");
         myAccessScreen.setup();
         //AccessControl.setMessage("done running update screen");
      }
    };

//----------------------------------------------------------------
//  initialization and accessors

  /**
   * The application entry point
   */
  public static void main(String[] args) {
    // each BlackBerry application that wants to log 
    // messages must register with a unique ID:
    EventLogger.register(0x40b0f6f6c6052cdaL, "accesscontrol", 
        EventLogger.VIEWER_STRING);
    AccessControl ac = new AccessControl();
    // Create a thread to perform memory access and decryption
    // functionality that may take too long to be run on 
    // the event thread:
    Thread t = new Thread(ac);
    t.start();
    setMessage("done starting thread");
    // Set the current thread to notify this application
    // of events such as user input.
    ac.enterEventDispatcher();
  }
  
  /**
   * The constructor initializes the data.
   */
  private AccessControl() {
    myAccessScreen = new AccessScreen(this);
    pushScreen(myAccessScreen);
    myPin = DeviceInfo.getDeviceId();
    //AccessControl.setMessage("got pin: " + myPin);
 }
  
  /**
   * Tells whether a valid license has been provided 
   * (either this session or from a previously entered 
   * license in memory).
   */
  public boolean isValid() {
    return myIsValid;
  }
  
//----------------------------------------------------------------
//  Business methods to check the license key

  /**
   * A runnable method to perform time-consuming memory 
   * access and decryption functions that should not be performed
   * on the event thread.
   */
  public void run() {
    try {
      //setMessage("in run");
      DesPinEncryptor dpe = DesPinEncryptor.getInstance();
      //AccessControl.setMessage("got dpe: " + dpe);
      // check the input field for user text:
      String userText = myAccessScreen.getUserText();
      if((userText == null) || (userText.trim().length() == 0)) {
        // if no text has been entered, try the memory:
        byte[] licenseKey = AccessStorage.getLicenseKey();
        //AccessControl.setMessage("got license key: " + licenseKey);
        // Check whether the license data in memory is valid:
        if((licenseKey != null) && (dpe.validateLicenseBytes(licenseKey, myPin))) {
          myIsValid = true;
        }
        //AccessControl.setMessage("myIsValid: " + myIsValid);
      } else {
        byte[] licenseBytes = dpe.licenseStringToBytes(userText);
        if((licenseBytes != null) && (dpe.validateLicenseBytes(licenseBytes, myPin))) {
          //AccessControl.setMessage("checkLicenseKey-->1");
          myIsValid = true;
          //AccessControl.setMessage("checkLicenseKey-->2");
          // set the license data in memory:
          AccessStorage.setLicenseKey(licenseBytes);
        }
      }
      // The screen needs to be updated, but can't be updated from
      // this thread. So we call invokeLater to tell the platform
      // to call myUpdateScreen.run() from the event thread at 
      // the next opportunity:
      invokeLater(myUpdateScreen);
    } catch(Exception e) {
      postException(e);
    }
  }
  
//----------------------------------------------------------------
//  debug logging utilities
  
  /**
   * A utility to log debug messages.
   */
  public static void setMessage(String message) {
    EventLogger.logEvent(0x40b0f6f6c6052cdaL, message.getBytes());
    System.out.println(message);
  }

  /**
   * A utility to log exceptions.
   */
  public static void postException(Exception e) {
    System.out.println(e);
    e.printStackTrace();
    String exceptionName = e.getClass().getName();
    EventLogger.logEvent(0x40b0f6f6c6052cdaL, exceptionName.getBytes());
    if(e.getMessage() != null) {
      EventLogger.logEvent(0x40b0f6f6c6052cdaL, e.getMessage().getBytes());
    }
  }

}

