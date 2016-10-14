package net.frogparrot.crypt;

import net.rim.device.api.crypto.*;
import java.io.*;

/**
 * A simple decryption example for BlackBerry.  This class takes
 * an int and uses a hard-coded DES key to encode it, for use
 * as a license.
 * 
 * @author Carol Hamer
 */
public class DesPinEncryptor {
    
//----------------------------------------------------------------
//  static fields

  /**
   * The singleton instance of this class.
   */
  private static DesPinEncryptor theInstance;
  
  /**
   * A character to separate the two ints that make up the 
   * license string.
   */
  public static String LICENSE_SEPARATOR = "z";

//----------------------------------------------------------------
//  instance fields

  /**
   * Hard-coded key data representing the DES key.
   * To create your own, just create a DESKey with the empty
   * constructor, then call getData() on it to get the 
   * key data.
   */
  private byte[] myKeyData = {
          (byte)-29, (byte)14, (byte)-22, (byte)35, 
          (byte)-53, (byte)4, (byte)-3, (byte)-48
      };
      
  /**
   * The object holding the DES key data.
   */
  private DESKey myKey;
  
//----------------------------------------------------------------
//  initialization and accessors

  /**
   * get the singleton instance.
   */
  static DesPinEncryptor getInstance() {
    if(theInstance == null) {
      theInstance = new DesPinEncryptor();
    }
    return theInstance;
  }
  
  /**
   * Constructor initializes data.
   */
  private DesPinEncryptor() {
    myKey = new DESKey(myKeyData);
  }
  
//----------------------------------------------------------------
//  decryption and formatting utilities

  /**
   * Uses an input stream to convert an array of bytes to an int.
   */
  public static int parseInt(byte[] data) throws IOException {
    DataInputStream stream 
      = new DataInputStream(new ByteArrayInputStream(data));
    int retVal = stream.readInt();
    stream.close();
    return(retVal);
  }

  /**
   * Takes a human-readable string and converts it to 
   * a byte array of eight bytes, to be decrypted with 
   * the DES key.
   */
  public byte[] licenseStringToBytes(String license) {
    byte[] retVal = null;
    try {
      // First split the string into two strings, 
      // each corresponding to one int:
      int index = license.indexOf(LICENSE_SEPARATOR);
      String sval1 = license.substring(0, index);
      int val1 = Integer.parseInt(sval1);
      //AccessControl.setMessage("val1: " + val1);
      String sval2 = license.substring(index + 1, license.length());
      int val2 = Integer.parseInt(sval2);
      //AccessControl.setMessage("val2: " + val2);
      // Convert the two ints to an array of eight bytes
      // using output streams:
      ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeInt(val1);
      dos.writeInt(val2);
      baos.close();
      dos.close();
      retVal = baos.toByteArray();
      //AccessControl.setMessage("licenseBytes.length: " + retVal.length);
    } catch(Exception e) {
      AccessControl.postException(e);
    }
    return retVal;
  }
  
  /**
   * Takes a human-readable string and decrypts it with the
   * DES key to determine whether it corresponds to the given PIN.
   */
  public boolean validateLicenseKey(String license, int pin) {
    boolean retVal = false;
    try {
      byte[] licenseBytes = licenseStringToBytes(license);
      //AccessControl.setMessage("licenseBytes.length: " + licenseBytes.length);
      retVal = validateLicenseBytes(licenseBytes, pin);
    } catch(Exception e) {
      AccessControl.postException(e);
    }
    return retVal;
  }

  /**
   * Takes an array of eight bytes and decrypts it with the
   * DES key to determine whether it corresponds to the given PIN.
   */
  public boolean validateLicenseBytes(byte[] edata, int pin) {
    boolean retVal = false;
    //AccessControl.setMessage("validateLicenseBytes-->pin: " + pin);
    try {
      // Set up the decryptor engine with the local key:
      DESDecryptorEngine de = new DESDecryptorEngine(myKey);
      //AccessControl.setMessage("validateLicenseBytes-->1");
      PKCS5UnformatterEngine ue = new PKCS5UnformatterEngine(de);
      //AccessControl.setMessage("validateLicenseBytes-->2");
      ByteArrayInputStream bais = new ByteArrayInputStream(edata);
      //AccessControl.setMessage("validateLicenseBytes-->3");
      BlockDecryptor decryptor = new BlockDecryptor(ue, bais);
      //AccessControl.setMessage("validateLicenseBytes-->4");
      
      // Decrypt the given byte array into a new byte array
      // of length 4:
      byte[] result = new byte[4];
      int bytesRead = decryptor.read(result);
      //AccessControl.setMessage("read " + bytesRead + " bytes");
      // Use input streams to convert the byte array to an int:
      int licenseInt = parseInt(result);
      //AccessControl.setMessage("result: " + retVal);
      //AccessControl.setMessage("pin: " + pin);
      // Check whether the resulting int equals the given PIN:
      retVal = (pin == licenseInt);
      //AccessControl.setMessage("no exception");
    } catch(Exception e) {
      AccessControl.postException(e);
    }
    //AccessControl.setMessage("returning: " + retVal);
    return retVal;
  }

//----------------------------------------------------------------
//  encryption utilities
//  These are not used by the access control functionality.
//  They are included only for convenience to illustrate how
//  to do an encryption parallel to the decryption code.

  /**
   * Takes a PIN and returns the corresponding string
   * encrypted with DES and the included key.
   */
  private byte[] byteCryptPin(int pin) {
    byte[] retVal = null;
    try {
      DESEncryptorEngine ee = new DESEncryptorEngine(myKey);
      PKCS5FormatterEngine fe = new PKCS5FormatterEngine(ee);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BlockEncryptor encryptor = new BlockEncryptor(fe, baos);
      encryptor.write(intToFourBytes(pin));
      encryptor.close();
      retVal = baos.toByteArray();
    } catch(Exception e) {
      AccessControl.postException(e);
    }
    return retVal;
  }
  
  /**
   * Takes a PIN and returns the corresponding string
   * encrypted with DES and the included key.
   */
  private String encryptPin(int pin) {
    String retString = "error";
    try {
      byte[] encryptedData = byteCryptPin(pin);
      //AccessControl.setMessage("encryptedData.length: " + encryptedData.length);
      DataInputStream stream 
          = new DataInputStream(new ByteArrayInputStream(encryptedData));
      int val1 = stream.readInt();
      //AccessControl.setMessage("val1: " + val1);
      int val2 = stream.readInt();
      //AccessControl.setMessage("val2: " + val2);
      stream.close();
      retString = (val1 + LICENSE_SEPARATOR) + val2;
      //AccessControl.setMessage("code: " + retString);
      boolean same = retString.equals("-717057553X2024320075");
      //AccessControl.setMessage("same: " + same);
    } catch(Exception e) {
      AccessControl.postException(e);
    }
    return retString;
  }
  
  /**
   * Uses an output stream to convert an int to four bytes.
   */
  public static byte[] intToFourBytes(int i) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(i);
    baos.close();
    dos.close();
    byte[] retArray = baos.toByteArray();
    return(retArray);
  }

}

