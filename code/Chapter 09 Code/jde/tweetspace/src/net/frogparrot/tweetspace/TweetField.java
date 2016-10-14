//#preprocess
package net.frogparrot.tweetspace;

import java.io.*;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.ui.component.EditField;

import com.versatilemonkey.net.*;

import net.frogparrot.ui.*;

/**
 * An EditField that sends the input to Twitter as tweets.
 * 
 * @author Carol Hamer
 */
public class TweetField extends EditField implements MessageField, Runnable {
    
//---------------------------------------------------------
//  instance fields

  /**
   * Data to position the cursor.
   */
  int myCursorPosition = 0;
  int mySentPosition = 0;
  int myLabelLength = 0;
  
  /**
   * Whether the user has updated the login credentials since the last post.
   */
  boolean myNewLogin = false;
  
  /**
   * The string to post to Twitter.
   */
  String mySendString;
  
  /**
   * A message to display to the local user.
   */
  String myUpdateMessage;
  
  /**
   * The game board.
   */
  SpaceLayer mySpaceLayer;

  /**
   * A runnable inner class to pass to invokeLater()
   * to ensure that display updates are called from the event thread.
   */
  Runnable myUpdateDisplay = new Runnable() {
      public void run() {
        String displayMessage = myUpdateMessage;
        myUpdateMessage = null;
        if(displayMessage != null) {
          mySentPosition += displayMessage.length();
          myCursorPosition += displayMessage.length();
          insert(displayMessage);
          setCursorPosition(myCursorPosition);
        }
      }
  };

//---------------------------------------------------------
//  data initialization and access

  /**
   * Initialize the field.
   */
  public TweetField(String label) {
    super(label, "");
    myLabelLength = getLabelLength();
    mySentPosition = myLabelLength;
  }
  
  /**
   * Give this instance a handle to the game board.
   */
  public void setSpaceLayer(SpaceLayer layer) {
    mySpaceLayer = layer;
  }
    
//---------------------------------------------------------
//  handle user input

  /**
   * @see net.frogparrot.ui.MessageField#keyChar(char, int, int, boolean)
   */
  public boolean keyChar(char key, int status, int time, boolean fromManager) {
    return keyChar(key, status, time);
  }

//#ifdef RIM_4.2.0
  /**
   * @see net.frogparrot.ui.MessageField#navigationMovement
   */
  public boolean navigationMovement(int dx, int dy, int status, 
      int time, boolean fromManager) {
    return navigationMovement(dx, dy, status, time);
  }
//#endif
  
  /**
   * Ensure that the cursor is placed at the end of the field
   * when focus is gained.
   * @see net.rim.device.api.ui.component.EditField#onFocus
   */
  protected void onFocus(int direction) {
    setCursorPosition(myCursorPosition);
  }
  
  /**
   * Save the cursor location for later use.
   * @see net.rim.device.api.ui.component.EditField#onUnfocus
   */
  protected void onUnfocus() {
    myCursorPosition = getCursorPosition();
  }
  
  /**
   * @see net.frogparrot.ui.MessageField#loginUpdated
   */
  public void loginUpdated(String username) {
    Thread t = new Thread(this);
    myNewLogin = true;
    t.start();
  }
  
//---------------------------------------------------------
//  handle the remote player's data

  /**
   * Display the latest message from a remote user.
   * @param name the remote user's screen name
   * @param message the remote user's message
   */
  public void displayRemoteMessage(String name, String message) {
    String postString = "\n" + name + ": " + message + "\n";
    //Main.setMessage("posting: " + postString);
    //Main.setMessage("is event thread: " + UiApplication.getUiApplication().isEventThread());
    mySentPosition += postString.length();
    myCursorPosition += postString.length();
    insert(postString);
    setCursorPosition(myCursorPosition);
    // This tells the local spaceship to stop where it is
    mySpaceLayer.move(SpaceLayer.DOWN);
    //Main.setMessage("calling setFocus");
    // This scrolls the messages to the end and gives the 
    // local user the opportunity to respond.
    setFocus();
  }
  
//---------------------------------------------------------
//  sending methods
  
  /**
   * Post the local user's data and latest message to Twitter.
   */
  public void sendMessage() {
    // get the user's message input:
    int currentLength = getTextLength() + myLabelLength;
    StringBuffer buff = new StringBuffer("status=");
    buff.append(mySpaceLayer.getPosition());
    if(currentLength > mySentPosition) {
      // update the sent postion so that this data won't be sent twice:
      buff.append(getText(mySentPosition, currentLength - mySentPosition));
      mySentPosition = currentLength + 1;
      insert("\n");
      myCursorPosition++;
    }
    // cache the current message for sending (in case login is needed)
    mySendString = buff.toString();
    //Main.setMessage(mySendString);
    // if the user hasn't logged in, then prompt the user to login
    // (instead of just sending the message):
    if(Main.getInstance().getLoginScreen().getCredentials() == null) {
      Main.getInstance().pushLoginScreen();
    } else {
      Thread t = new Thread(this);
      t.start();
    }
  }
  
  /**
   * Perform the http POST operation
   */
  public void run() {
    //Main.setMessage("* in   TweetField.run * ");
    HttpConnection connection = null;
    try {
      HttpConnectionFactory factory = new HttpConnectionFactory(
          "http://twitter.com/statuses/update.json",
          HttpConnectionFactory.TRANSPORTS_ANY);
      String credentials = Main.getInstance().getLoginScreen().getCredentials();
      if(mySendString == null) {
        StringBuffer buff = new StringBuffer("status=");
        buff.append(mySpaceLayer.getPosition());
        buff.append(Main.theLabels.getString(Main.TWEETSPACE_TWEETSPACE));
        mySendString = buff.toString();
      }
      while(true) {
        try {
          connection = factory.getNextConnection();
          //Main.setMessage("attempting connection: " + connection);
          try {
            connection.setRequestMethod( "POST" );
            connection.setRequestProperty("Authorization", "Basic " + credentials);
            OutputStream os = connection.openOutputStream( );
            os.write(mySendString.getBytes());
            os.close();
            int responseCode = connection.getResponseCode();
            //Main.setMessage("response code: " + responseCode);
            int length = (int)(connection.getLength());
            //Main.setMessage("length: " + length);
            InputStream is = connection.openInputStream();
            byte[] data = new byte[length];
            int bytesRead = is.read(data);
            //Main.setMessage("bytesRead: " + bytesRead);
            String str = new String(data);
            //Main.setMessage(str);
            is.close();
            if(responseCode == 200) {
              // if the login succeeded, then send a message to the 
              // space explorer list admin to add this player:
              if(myNewLogin) {
                requestListAddition(credentials);
                myNewLogin = false;
              }
              break;
            } else if(responseCode == 401) {
              // login failed, prompt the user to give valid credentials:
              myUpdateMessage = Main.theLabels.getString(Main.TWEETSPACE_LOGIN_ERROR);
              Main.getUiApplication().invokeLater(myUpdateDisplay);
              break;
            }
          } catch(IOException ioe) {
            //Log the error:
            Main.postException(ioe);
          }
        } catch(NoMoreTransportsException e) {
          //There are no more transports to attempt
          Main.postException(e);
          break;
        } finally {
           try {
            connection.close();
          } catch(Exception e) {}
        }
      }
    } catch(Exception e) {
      Main.postException(e);
    }
    //Main.setMessage("* done: TweetField.run * ");
  }
  
  /**
   * Posts a request to add this user to the list players (so that
   * this player's tweets will be visible to other players).
   */
  public void requestListAddition(String credentials) {
    TweetReader.httpRequest("http://twitter.com/friendships/create/bbspaceexp.json",
        credentials, "");
    String reqStr = "screen_name=bbspaceexp&text=add me to " 
                + Main.theLabels.getString(Main.TWEETSPACE_LISTNAME);
    TweetReader.httpRequest("http://twitter.com/direct_messages/new.json", 
        credentials, reqStr);
  }
  
} 
