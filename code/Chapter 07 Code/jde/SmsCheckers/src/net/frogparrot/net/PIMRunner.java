package net.frogparrot.net;

import java.util.Enumeration;
import java.util.Vector;
//import net.frogparrot.smscheckers.Main;

import javax.microedition.pim.*;

/**
 * A simple PIM utility to load a list of contacts.
 */
public class PIMRunner extends Thread {

  /**
   * A callback listener for this 
   * class to call when the PIM list is filled.
   */
  ContactListener myListener;

  /**
   * The constructor just sets the callback listener for this 
   * class to call when the PIM list is filled.
   */
  public PIMRunner(ContactListener listener) {
    myListener = listener;
  }

  /**
   * The method that fills the data fields.
   */
  public void run() {
    ContactList addressbook = null;
    Contact contact = null;
    Enumeration items = null;
    Vector names = new Vector();
    Vector phoneNumbers = new Vector();
    try {
      addressbook = (ContactList)(PIM.getInstance(
          ).openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY));
      //Main.setMessage("addressbook: " + addressbook);
      items = addressbook.items();
    } catch(Exception e) {
      // if the addressbook can't be opened, then we're done.
      myListener.setContactList(names, phoneNumbers);
    }
    // Now load the contents of the addressbook:
    while(items.hasMoreElements()) {
      //Main.setMessage("items.hasMoreElements");
      try {
        contact = (Contact)(items.nextElement());
        // only continue if the contact has at least one 
        // phone number listed:
        int phoneNumCount = contact.countValues(Contact.TEL);
        //Main.setMessage("phoneNumCount: " + phoneNumCount);
        if(phoneNumCount > 0) {
          String phoneNum = null;
          for(int i = 0; i < phoneNumCount; i++) {
            int attr = contact.getAttributes(Contact.TEL, i);
            // look for a MOBILE number if there is one,
            // otherwise just take the first phone number:
            if(i == 0 || attr == Contact.ATTR_MOBILE) {
              //Main.setMessage("getting #: " + i);
              phoneNum = contact.getString(Contact.TEL, i);
            }
          }
          if(phoneNum != null) {
            // now try to find the name.
            int fieldIndex = Contact.NAME;
            // BlackBerry stores the name in an array of Strings.
            String[] nameArray = contact.getStringArray(fieldIndex, 0);
            //Main.setMessage("array length: " + nameArray.length);
            String formattedName = nameArray[1] + " " + nameArray[0];
            //Main.setMessage(formattedName);
            names.addElement(formattedName);
            phoneNumbers.addElement(phoneNum);
          }
        }
      } catch(Exception e) {
        //Main.postException(e);
        // if an individual contact provokes an exception, 
        // we skip it and move on.
      }
    } // while(items.hasMoreElements())
    myListener.setContactList(names, phoneNumbers);
  }

  /**
   * Finds the contact corresponding to the phone number.
   * This is called from the SMS listener thread, hence
   * doesn't need to be run from another thread.
   * If no name corresponds to the phone number, then 
   * just send back the number.
   */
  public static String findName(String phoneNumber) {
    ContactList addressbook = null;
    Contact contact = null;
    Enumeration items = null;
    try {
      addressbook = (ContactList)(PIM.getInstance(
          ).openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY));
      //Main.setMessage("addressbook: " + addressbook);
      items = addressbook.items();
    } catch(Exception e) {
      // if the addressbook can't be opened, then we're done.
      return phoneNumber;
    }
    // Now load the contents of the addressbook:
    while(items.hasMoreElements()) {
      //Main.setMessage("items.hasMoreElements");
      try {
        contact = (Contact)(items.nextElement());
        // only continue if the contact has at least one 
        // phone number listed:
        int phoneNumCount = contact.countValues(Contact.TEL);
        //Main.setMessage("phoneNumCount: " + phoneNumCount);
        for(int i = 0; i < phoneNumCount; i++) {
          //Main.setMessage("getting #: " + i);
          if(phoneNumber.endsWith(contact.getString(Contact.TEL, i))) {
            String[] nameArray = contact.getStringArray(Contact.NAME, 0);
            //Main.setMessage("array length: " + nameArray.length);
            return nameArray[1] + " " + nameArray[0];
          }
        }
      } catch(Exception e) {
        //Main.postException(e);
        // if an individual contact provokes an exception, 
        // we skip it and move on.
      }
    } // while(items.hasMoreElements())
    return phoneNumber;
  }

}
