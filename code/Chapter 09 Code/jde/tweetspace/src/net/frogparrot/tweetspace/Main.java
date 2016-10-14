//#preprocess
package net.frogparrot.tweetspace;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.i18n.ResourceBundle;

import net.frogparrot.login.*;
import net.frogparrot.ui.*;

//#ifdef RIM_4.6.0
import net.frogparrot.svgmovie.SvgFieldAnimator;
//#endif

/**
 * The main lifecycle class of the Tweet Space game.
 * Initializes all of the data classes and directs their interactions.
 * 
 * @author Carol Hamer
 */
public class Main extends UiApplication implements TweetSpaceResResource
//#ifdef RIM_4.6.0
   , Runnable
//#endif
{
    
//---------------------------------------------------------
//  static fields

  /**
   * This bundle contains all of the texts, translated by locale.
   */
  public static ResourceBundle theLabels = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

  /**
   * The singleton instance of this class
   */
  static Main theInstance;

//---------------------------------------------------------
//  instance fields

  /**
   * The screen that allows the user to enter login credentials.
   */
  LoginScreen myLoginScreen;
  
  /**
   * The class that periodically downloads the latest tweets.
   */
  TweetReader myTweetReader;
  
  /**
   * The game playing screen data.
   */
  SpaceLayer mySpaceLayer;
  
//---------------------------------------------------------
//  static methods

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    //System.out.println("*** Main!!! ***");
    // each BlackBerry application that wants to log 
    // messages must register with a unique ID:
    EventLogger.register(0xe9d1942bbd3cd08cL, "tweetspace", 
          EventLogger.VIEWER_STRING);
    theInstance = new Main();
//#ifdef RIM_4.6.0
    theInstance.showOpeningMovie();
//#else
    theInstance.init();
//#endif
    // Set the current thread to notify this application
    // of events such as user input.
    //System.out.println("about to enter event dispatcher");
    theInstance.enterEventDispatcher();
  }
  
  /**
   * Gets the current instance of Main.
   */
  public static Main getInstance() {
    return theInstance;
  }
  
//---------------------------------------------------------
//  special code to run the opening movie
// for version 4.6.0+ only

//#ifdef RIM_4.6.0
  /**
   * Movie-related data fields
   */
  SvgFieldAnimator myMovie;
  int myInvokeLaterId;
  MainScreen myMovieScreen;

  /**
   * Launch the movie.
   */
  void showOpeningMovie() {
    myMovieScreen = new MainScreen() {
      public boolean onClose() {
        // if the user escapes the movie,
        // close it up and start the game:
        cancelInvokeLater(myInvokeLaterId);
        run();
        return true;
      }
    };
    myMovie = new SvgFieldAnimator(
        Graphics.getScreenWidth(), Graphics.getScreenHeight());
    myMovieScreen.add(myMovie.getField());
    pushScreen(myMovieScreen);
    myMovie.play();
    myInvokeLaterId = invokeLater(this, 16000, false);
  }

  /**
   * Launch the game at the end of the movie.
   */
  public void run() {
    myMovie.stop();
    popScreen(myMovieScreen);
    myMovie = null;
    myMovieScreen = null;
    init();
  }
//#endif

//---------------------------------------------------------
//  data initialization, access, and lifecycle

  /**
   * Initializes the data like a constructor.
   */
  public void init() {
    try {
      //System.out.println("Main.init");
      TweetField tweetField = new TweetField(theLabels.getString(TWEETSPACE_TWEETSPACE));
      //System.out.println("Main: created tweet field: " + tweetField);
      Spaceship.setTweetField(tweetField);
      TwoFieldManager twoFieldManager = new TwoFieldManager(tweetField, this);
      //System.out.println("Main: created twoFieldManager: " + twoFieldManager);
      SimpleScreen simpleScreen = new SimpleScreen(twoFieldManager);
      //System.out.println("Main: SimpleScreen: " + simpleScreen);
      tweetField.setSpaceLayer(mySpaceLayer);
      // push the game to the top of the screen stack:
      pushScreen(simpleScreen);
      //System.out.println("done pushing screen");
      // create a login screen for later use:
      myLoginScreen = new LoginScreen(tweetField);
      //System.out.println("Main: myLoginScreen: " + myLoginScreen);
      // create and launch the class that reads the tweets:
      myTweetReader = new TweetReader(mySpaceLayer);
      //System.out.println("Main: myTweetReader: " + myTweetReader);
    } catch(Exception e) {
      // if initialization failed, log the exception and quit:
      postException(e);
      quit();
    }
  }
  
  /**
   * Creates the animated game screen based on the size of the window.
   */
  public FieldAnimator createFieldAnimator(int viewportWidth, int viewportHeight) {
    mySpaceLayer = new SpaceLayer(viewportWidth, viewportHeight);
    //System.out.println("createFieldAnimator: " + mySpaceLayer);
    return mySpaceLayer;
  }
  
  /**
   * Show the login screen to the user.
   */
  public void pushLoginScreen() {
    //System.out.println("pushLoginScreen: " + myLoginScreen);
    pushScreen(myLoginScreen);
  }
  
  /**
   * Get a handle to the login screen for data access.
   */
  public LoginScreen getLoginScreen() {
    //System.out.println("getLoginScreen: " + myLoginScreen);
    return myLoginScreen;
  }
  
  /**
   * Clean up and exit.
   */
  public void quit() {
    try {
      if(myTweetReader != null) {
        myTweetReader.cancel();
        myTweetReader = null;
      }
      myTweetReader = null;
      if(mySpaceLayer != null) {
        mySpaceLayer.stop();
        mySpaceLayer = null;
      }
      myLoginScreen = null;
      Spaceship.clear();
    } finally {
      theInstance = null;
      System.exit(0);
    }
  }
  
  /**
   * Handle a backgrounding event, such as the red key.
   */
  public void deactivate() {
    //System.out.println("Deactivate!!!");
    quit();
  }

//----------------------------------------------------------------
//  debug logging utilities
  
  /**
   * A utility to log debug messages.
   */
  public static void setMessage(String message) {
    EventLogger.logEvent(0xe9d1942bbd3cd08cL, message.getBytes());
    System.out.println(message);
  }

  /**
   * A utility to log exceptions.
   */
  public static void postException(Exception e) {
    System.out.println(e);
    e.printStackTrace();
    String exceptionName = e.getClass().getName();
    EventLogger.logEvent(0xe9d1942bbd3cd08cL, exceptionName.getBytes());
    if(e.getMessage() != null) {
      EventLogger.logEvent(0xe9d1942bbd3cd08cL, e.getMessage().getBytes());
    }
  }


}

