package net.frogparrot.login;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.Base64InputStream;

import net.frogparrot.ui.MessageField;
import net.frogparrot.tweetspace.Main;

/**
 * The screen to prompt the user to log into Twitter.
 * 
 * @author Carol Hamer
 */
public class LoginScreen extends MainScreen {

//----------------------------------------------------------------
//  instance fields

  /**
   * A handle to the listner.
   */
  MessageField myLoginListener;
  
  /**
   * The B-64 encoded username and password.
   */
  String myCredentials;
  
  /**
   * The username to identify this user.
   */
  String myUsername;
          
  /**
   * The menu item that launches the login action to 
   * check and save the user's credentials.
   */
  MenuItem myOK = new MenuItem(Main.theLabels.getString(Main.TWEETSPACE_LOGIN), 0, 0) {
      // This is called when the user selects the OK menu item:
      public void run() {
        setCredentials();
      }
    };
  
  /**
   * The menu item that allows the user to leave this screen
   * without logging in.
   */
  MenuItem myCancel = new MenuItem(Main.theLabels.getString(Main.TWEETSPACE_CANCEL), 0, 0) {
      // This is called when the user selects the Cancel menu item:
      public void run() {
        cancel();
      }
    };
  
  /**
   * The input field where the user types in the username.
   */
  EditField myUsernameField = new EditField(Main.theLabels.getString(Main.TWEETSPACE_USERNAME), "", 30,
          EditField.JUMP_FOCUS_AT_END | EditField.NO_NEWLINE 
          | RichTextField.NO_LEARNING | RichTextField.NO_COMPLEX_INPUT
          | RichTextField.FOCUSABLE | RichTextField.EDITABLE);

  /**
   * The input field where the user types in the password.
   */
  PasswordEditField myPasswordField = new PasswordEditField(Main.theLabels.getString(Main.TWEETSPACE_PASSWORD), "", 30,
          EditField.JUMP_FOCUS_AT_END | EditField.NO_NEWLINE 
          | RichTextField.NO_LEARNING | RichTextField.NO_COMPLEX_INPUT
          | RichTextField.FOCUSABLE | RichTextField.EDITABLE);

//----------------------------------------------------------------
//  initialization and lifecycle

  /**
   * The constructor initializes the data.
   */
  public LoginScreen(MessageField ll) {
    myLoginListener = ll;
    // place the Twitter logo beside the login text:
    HorizontalFieldManager hfm = new HorizontalFieldManager();
    Bitmap twitterLogo = Bitmap.getBitmapResource("twitter.png");
    hfm.add(new BitmapField(twitterLogo));
    hfm.add(new RichTextField(Main.theLabels.getString(Main.TWEETSPACE_SIGNIN), 
          RichTextField.NON_FOCUSABLE));
    add(hfm);
    // then add the rest of the form
    add(myUsernameField);
    add(myPasswordField);
    add(new RichTextField(Main.theLabels.getString(Main.TWEETSPACE_WARNING), 
          RichTextField.NON_FOCUSABLE));
  }
  
  /**
   * Override the screen's menu creation method 
   * to add the custom commands.
   */
  protected void makeMenu(Menu menu, int instance) {
    menu.add(myOK);
    menu.add(myCancel);
  }

  /**
   * Pop this screen without updating the login credentials.
   */
  public void cancel() {
    UiApplication.getUiApplication().popScreen(this);
  }

//----------------------------------------------------------------
//  business methods

  /**
   * Called when the user selects "log in", this method
   * launches the action to check and save the user's credentials.
   */
  public void setCredentials() {
    try {
      myUsername = myUsernameField.getText();
      StringBuffer buff = new StringBuffer(myUsername);
      buff.append(":");
      buff.append(myPasswordField.getText());
      // encode the username and password for basic authentication:
      byte[] clearData = buff.toString().getBytes();
      byte[] codedData = Base64OutputStream.encode(clearData, 0, 
           clearData.length, false, false);
      // store the encoded username and password in memory:
      AccessStorage.setLicenseKey(codedData);
      myCredentials = new String(codedData);
      UiApplication.getUiApplication().popScreen(this);
      // Tell the message field to try the new credentials:
      myLoginListener.loginUpdated(myUsername);
    } catch(Exception e) {
      Main.postException(e);
    }
  }
  
  /**
   * Read the user's credentials from the record store.
   * @return The user's (b64) basic-authentication encoded 
   * username and password.
   */
  public String getCredentials() {
    if(myCredentials == null) {
      byte[] credentials = AccessStorage.getLicenseKey();
      if((credentials != null) && (credentials.length != 0)) {
        myCredentials = new String(credentials);
      }
    }
    return myCredentials;
  }
  
  /**
   * @return The current username or null if no valid username is found.
   */
  public String getUsername() {
    if(myUsername == null) {
      try {
        String credentials = getCredentials();
        String clearString = new String(Base64InputStream.decode(credentials));
        int index = clearString.indexOf(':');
        myUsername = clearString.substring(0, index);
        //System.out.println("my username: " + myUsername);
      } catch(Exception e) {
      }
    }
    return myUsername;
  }
  
}

