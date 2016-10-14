package net.frogparrot.tweetspace;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.io.HttpConnection;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import com.versatilemonkey.net.*;

/**
 * This class reads tweets from Twitter and uses the data to 
 * place the remote players.
 * 
 * @author Carol Hamer
 */
public class TweetReader extends TimerTask {
    
//---------------------------------------------------------
//  instance fields

  /**
   * The handle to the game board.
   */
  SpaceLayer mySpaceLayer;
  
  /**
   * The timer that runs this task periodically.
   */
  Timer myTimer;
  
  /**
   * The Twitter id of the most recent message that has 
   * been read by this class.  This is used in the request
   * in order not to get the same messages again.
   */
  long mySinceId = 0;
  
//---------------------------------------------------------
//  data initialization and accessors

  /**
   * Schedule the task.
   * @param layer a handle to the game board for callbacks
   */
  public TweetReader(SpaceLayer layer) {
    mySpaceLayer = layer;
    myTimer = new Timer();
    // check for messages every ten seconds:
    myTimer.schedule(this, 10000, 10000);
  }
    
//---------------------------------------------------------
//  generic connection utilities

  /**
   * A simple utility to send a BlackBerry HTTP request.
   */
  public static String httpRequest(String url, String credentials, String postData) {
    String retString = null;
    HttpConnection connection = null;
    try {
      HttpConnectionFactory factory = new HttpConnectionFactory(url,
          HttpConnectionFactory.TRANSPORTS_ANY);
      while(true) {
        try {
          connection = factory.getNextConnection();
          //Main.setMessage("attempting connection: " + connection);
          try {
            if(credentials != null) {
              connection.setRequestProperty("Authorization", "Basic " + credentials);
              OutputStream os = connection.openOutputStream();
              os.write(postData.getBytes());
              os.close();
            }
            if(postData != null) {
              connection.setRequestMethod("POST");
            } else {
              connection.setRequestMethod("GET");
            }
            int responseCode = connection.getResponseCode();
            //Main.setMessage("response code: " + responseCode);
            int length = (int)(connection.getLength());
            //Main.setMessage("length: " + length);
            InputStream is = connection.openInputStream();
            byte[] data = new byte[length];
            int bytesRead = is.read(data);
            //Main.setMessage("bytesRead: " + bytesRead);
            retString = new String(data);
            is.close();
            //Main.setMessage(retString);
            if(responseCode == 200) {
              break;
            }
          } catch(IOException ioe) {
            //Log the error:
            Main.postException(ioe);
          }
        } catch(NoMoreTransportsException e) {
          //Log the error:
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
    return retString;
  }

//---------------------------------------------------------
//  business methods

  /**
   * Get the list of remote player tweets to parse and display.
   */
  public void run() {
    //Main.setMessage("* in TweetReader.run * ");
    try {
      // all of the game tweets are read from one list on the Twitter server:
      StringBuffer buff = new StringBuffer("http://api.twitter.com/1/bbspaceexp/lists/");
      buff.append(Main.theLabels.getString(Main.TWEETSPACE_LISTNAME));
      buff.append("/statuses.json");
      if(mySinceId > 0) {
        buff.append("?since_id=");
        buff.append(mySinceId);
      }
      String url = buff.toString();
      // read the recent tweets from the internet:
      String responseData = httpRequest(url, null, null);
      // parse the tweet data and update the opponent ships accordingly:
      parseJson(responseData);
    } catch(Exception e) {
      Main.postException(e);
    }
    //Main.setMessage("* done: TweetReader.run * ");
  }
    
  /**
   * Parse and store the remote players' data and messages.
   * @param jsonStr The data block returned by Twitter
   */
  void parseJson(String jsonStr) {
    try {
      JSONArray results = new JSONArray(jsonStr);
      //Main.setMessage("PARSED results: " + results);
      //Main.setMessage("* results length * " + results.length());
      int length = results.length();
      // parse them in reverse order to show the most recent 
      // message from each opponent:
      for(int i = 1; i < length + 1; i++) {
        //Main.setMessage("JSON Array element #" + (length - i) + ": " + results.getJSONObject(length - i));
        JSONObject tweet = results.getJSONObject(length - i);
        mySinceId = tweet.getLong("id");
        //Main.setMessage("id: " + mySinceId);
        String text = tweet.getString("text");
        //Main.setMessage("text: " + text);
        JSONObject user = tweet.getJSONObject("user");
        String name = user.getString("screen_name");
        //Main.setMessage("calling setAlienShip: " + (length - i));
        if(!name.equals(Main.getInstance().getLoginScreen().getUsername())) {
          mySpaceLayer.setAlienShip(name, text);
          //Main.setMessage("returned from setAlienShip: " + (length - i));
        }
      }
    } catch(Exception e) {
      //Main.setMessage("exception caught by parseJson");
      Main.postException(e);
    }
  }
   
} 
