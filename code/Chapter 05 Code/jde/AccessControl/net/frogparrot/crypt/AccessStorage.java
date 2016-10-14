package net.frogparrot.crypt;

import javax.microedition.rms.*;

/**
 * This class helps to store and retrieve the license key.
 *
 * This is a utility class that does not contain instance data, 
 * so to simplify acess all of the methods are static.
 * 
 * @author Carol Hamer
 */
public class AccessStorage {

  //---------------------------------------------------------
  //   static fields

  /**
   * The name of the datastore.
   */
  private static final String STORE = "Validation";

  //---------------------------------------------------------
  //   business methods

  /**
   * This gets the users license key, if one has been stored.
   */
  public static byte[] getLicenseKey() {
    byte[] retVal = null;
    RecordStore store = null;
    try {
      // if the record store does not yet exist, we 
      // send "false" so it won't bother to create it.
      store = RecordStore.openRecordStore(STORE, false);
      if((store != null) && (store.getNumRecords() > 0)) {
        // the first record has id number 1
        // (In fact this program stores only one record)
        retVal = store.getRecord(1);
        //AccessControl.setMessage("found record size: " + retVal.length);
      }
    } catch(Exception e) {
      AccessControl.postException(e);
    } finally {
      try {
        store.closeRecordStore();
      } catch(Exception e) {
        // if the record store is open this shouldn't throw.
      }
    }
    //AccessControl.setMessage("getLicenseKey returning: " + retVal);
    return(retVal);
  }

  /**
   * Sets the user's license key.
   */
  public static void setLicenseKey(byte[] licenseKey) {
    RecordStore store = null;
    try {
      // if the record store does not yet exist, the second 
      // arg "true" tells it to create.
      store = RecordStore.openRecordStore(STORE, true);
      int numRecords = store.getNumRecords();
      if(numRecords > 0) {
        store.setRecord(1, licenseKey, 0, licenseKey.length);
      } else {
        store.addRecord(licenseKey, 0, licenseKey.length);
      }
    } catch(Exception e) {
      AccessControl.postException(e);
    } finally {
      try {
        store.closeRecordStore();
      } catch(Exception e) {
        // if the record store is open this shouldn't throw.
      }
    }
  }

}
