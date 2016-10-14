
// SaberScreen.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* The game screen, which uses an update-draw-sleep animation loop, implemented
   in a separate thread (see the startGame() method).
   
   A range of user input types are supported: the accelerometer, touch screen, 
   track ball movement, and key presses.
   
   --------------
   4.7 --> 4.6 changes
     - removed acceleormeter, touch screen
*/

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;
import net.rim.device.api.math.*;

import java.util.*;


public class SaberScreen extends FullScreen
{
  private static int FPS = 20;   // frames/sec

  private static final String IMAGES_DIR = "images/";
  private static final String BG_IM = "knight.png";    // the background image

  private static final int MAX_SCORE = 30;
  private static final int NUM_BLAST_SPRITES = 7;
  private static final int NUM_HIT_SOUNDS = 3;

  // keycodes for the green and red keys
  private static final int GREEN_KEY = 1114112;   // Send key
  private static final int RED_KEY = 1179648;     // End/Power key

  private final int screenWidth = Display.getWidth();
  private final int screenHeight = Display.getHeight();

  private Thread animator;        // the thread that performs the animation
  private volatile boolean isRunning = false;   // used to stop the animation thread
  private boolean gameOver = false;
  private boolean playerHasWon = true;
  
  private int score = MAX_SCORE/2;      // current score
  private int hitCounter = 0;

  // timing
  private long period;      // period between drawing in ms
  private long startTime;   // in ms
  private int timeSpent;    // user time spent playing the game (in secs)

  // background image
  private Bitmap backIm;
  private int bgWidth, bgHeight;
  
  // text and fonts
  private String gameOverMessage;
  private Font msgFont, endFont;   // the fonts for the status messages and the end
  
  // accelerometer info
  // private Channel accelChannel = null;
  // private short[] xyzAccels = new short[3];
  
  private ClipsPlayer clipsPlayer;
  
  // sprites
  private LightSaber saber;
  private BlastSprite[] blasts;
  private ExplodingSprite wallExplSprite;



  public SaberScreen()
  {  
    super();
    period = (int) 1000.0/FPS;
    // System.out.println("period: " + period + " ms");

    // load images, sounds, accelerometer channel, message fonts
    backIm = loadImage(BG_IM);
    bgWidth = backIm.getWidth(); 
    bgHeight = backIm.getHeight();
    
    clipsPlayer = new ClipsPlayer();
    loadSounds();

/*
    if (AccelerometerSensor.isSupported())
      accelChannel = AccelerometerSensor.openRawDataChannel(
                                               Application.getApplication() );
*/                                       
    msgFont = Font.getDefault().derive(Font.BOLD, 24); 
    endFont = Font.getDefault().derive(Font.BOLD, 48);   // bigger

    // create sprites
    saber = new LightSaber();
    
    blasts = new BlastSprite[NUM_BLAST_SPRITES];
    for (int i=0; i < NUM_BLAST_SPRITES; i++)
      blasts[i] = new BlastSprite();
      
    wallExplSprite = new ExplodingSprite("wallBlast.png", "wallBlasts.png", 7);  

    invalidate();        // draw the screen in its initial state.
  }  // end of SaberScreen()



  private Bitmap loadImage(String fnm)
  {
    Bitmap im = null;
    System.out.println("Loading image in " + IMAGES_DIR+fnm);
    try {
      im = Bitmap.getBitmapResource(IMAGES_DIR +fnm);
    }
    catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
    if (im == null) {
      System.out.println("Image is empty");
      System.exit(1);
    }
    return im;
  }  // end of loadImage()



  private void loadSounds()
  {
    // start looping through the background music
    clipsPlayer.loadLoop("starWars.mid");

//    if (System.getProperty("supports.mixing").equals("false"))   // always false on simulator
//      System.out.println("mixing not supported on this device");
//    else {    // load WAV files, and others
      // load hit clips
      for (int i=0; i < NUM_HIT_SOUNDS; i++)
        clipsPlayer.load("hit" + i + ".wav");
//     }
  }  // end of loadSounds()



  public void cancelGame()
  // undo the initialization carried out in the constructor
  // called from IntroScreen class
  {   clipsPlayer.close();  }
   
  

  public void startGame()
  // initialise and start the thread which drives the game
  { 
    if (animator != null || isRunning)
      return;   // thread already exists

    animator = new Thread (new Runnable() {  
      public void run()  
      // an update-draw-sleep cycle
      {
        long beforeTime, sleepTime;
        startTime = System.currentTimeMillis();
        isRunning = true;
        while(isRunning) { 
          beforeTime = System.currentTimeMillis();
        
          // processShakes();   // poll for accelerometer input
          update();
        
          // wait for the screen to be drawn
          UiApplication.getUiApplication().invokeAndWait(new Runnable() {
            public void run()
            {  invalidate(); }
          });

          sleepTime = period - (System.currentTimeMillis() - beforeTime);
          try {
            if (sleepTime > 0)    // animation is running too fast
              Thread.sleep(sleepTime);  // sleep for some ms
          }
          catch(InterruptedException ex){}
        }

        clipsPlayer.close();  
        System.exit(0);   // make sure app. disappears
      }  
    });  
    animator.start(); 
  } // end of startGame()



  // ---------------------- update the screen --------------


  private void update()
  {
    if (!gameOver) {
      gameOver = isGameFinished();
      if (gameOver)
        finalUpdates();
      else
        gameUpdate();
    }
  } // end of update()



  private boolean isGameFinished()
  // this game finishes basd on the current score
  {
    if (score <= 0) {
      gameOverMessage = "You lose";
      playerHasWon = false;
      return true;    // game is over
    }
    else if (score >= MAX_SCORE) {
      gameOverMessage = "Victory!";
      return true;
    }
    return false;
  }  // end of isGameFinished()



  private void finalUpdates()
  // things to update when the game finishes
  {
    clipsPlayer.close("starWars.mid");   // stop and close background music, avoiding lack of mixer support
    // accelChannel.close();
    if (playerHasWon)
      clipsPlayer.loadPlay("forcestrong.wav");    // player has won
    else
      clipsPlayer.loadPlay("underestimate.wav");  // player has lost
  }  // end of finalUpdates()



  private void gameUpdate()
  // check sprite conditions, then update all the sprites
  {
    // check interaction conditions
    checkBlasts();
    
    // check for saber hitting a wall
    if (!wallExplSprite.isExploding()) {
      if (saber.isTouchingWall())
        wallExplSprite.explodeAt( saber.getTip() );
    }
    
    // update sprites
    saber.update();
    for(int i=0; i < blasts.length; i++)
      blasts[i].update();
    wallExplSprite.update();
  }  // end of gameUpdate()
  
  
  
  private void checkBlasts()
  /* * reactivate inactive blast sprites 
     * make a blast explode if it's touching the saber
     * deactivate sprites that have descended below floor level
  */
  {
    BlastSprite blast;
    for(int i=0; i < blasts.length; i++) {
      blast = blasts[i];
      if(!blast.isActive())    // send out blasts that are currently inactive
        blast.drop();

      // check for blast intersection with saber
      if (!blast.isExploding()) {
        if (blast.hasHitSaber(saber)) {
          blast.explode();
          clipsPlayer.play("hit" + hitCounter + ".wav");
          Alert.startVibrate(300);    // vibrate for 0.3 second
          hitCounter = (hitCounter+1)%NUM_HIT_SOUNDS;
          score++;
        }
      }

      // check for blast intersection with floor
      if (blast.hasReachedFloor()) {
        score--;
        blast.setActive(false);  // now inactive, so can be reused next time
      }
    }
  }  // end of checkBlasts()



  // ------------------ render the screen -------------------------


  protected void paint(Graphics g)
  {
    // clear the background by painting it black
    g.setColor(Color.BLACK);
    g.fillRect (0, 0, screenWidth, screenHeight);

    // draw background image
    g.drawBitmap((screenWidth-bgWidth)/2, 0, bgWidth, bgHeight, backIm, 0, 0);

    // draw the sprites
    saber.draw(g);
    for(int i=0; i < blasts.length; i++)
      blasts[i].draw(g);
    wallExplSprite.draw(g);

    showStatus(g);
    
    if (gameOver)
      showGameOver(g);
  }  // end of paint()




  private void showStatus(Graphics g)
  // draw status information on bottom left and right of the screen
  {
    g.setColor(Color.YELLOW);
    g.setFont(msgFont);

    if (!gameOver)
      timeSpent = (int) (System.currentTimeMillis() - startTime)/1000;  // in secs

    int y = screenHeight - 10; // near bottom
    g.drawText( timeSpent+"s", 10, y,  DrawStyle.BOTTOM|DrawStyle.LEFT);  // text on left side
                        
    int x = screenWidth - msgFont.getAdvance("Score: XXX");    // text on right side
    g.drawText( "Score: "+score, x, y, DrawStyle.BOTTOM|DrawStyle.LEFT); 
  }  // end of showStatus()



  private void showGameOver(Graphics g)
  // draw game over message below center of panel
  {
    if (gameOverMessage == null)
      gameOverMessage = "Game Over!!";

    int x = (screenWidth - endFont.getAdvance(gameOverMessage))/2; 
    int y = (screenHeight - endFont.getHeight())/2;

    g.setColor(Color.RED);
    g.setFont(endFont);
    
    g.drawText(gameOverMessage, x, y, DrawStyle.BOTTOM|DrawStyle.LEFT);
  }  // end of showGameOver()



  // ----------------------------- user input ---------------------------------
  // handles the accelerometer, touch screen, track ball movement, and key presses;
  // the input is used to rotate the light saber

/*
  private void processShakes()
  // pass accelerometer info to the saber
  {
    if (accelChannel == null)
      return;
    if (gameOver)
      return;
      
    accelChannel.getLastAccelerationData( xyzAccels );
    saber.shake(xyzAccels[0]);  // pass x-axis accel to saber
  } // end of processShakes()
*/

/*
  protected boolean touchEvent(TouchEvent message)
  // pass touch screen click info to the saber
  {
    if (message.getEvent() == TouchEvent.CLICK){
      if (!gameOver)    // use the (x,y) touch coordinate                
        saber.setClickAngle(message.getX(1), message.getY(1));
    }          
    return true;       
  }  // end of touchEvent()
*/


  protected boolean navigationMovement(int dx,int dy, int status, int time)  
  // pass trackball movement info to the saber
  {
    if (!gameOver)
      saber.move(dx);  // negative for a move left, positive for a move right
    return true;
  }  // end of navigationMovement()



  protected boolean keyChar(char key, int status, int time)
  // left/right movement, and terminate the game by typing <ESC>
  {
    if (key == Characters.CONTROL_LEFT && !gameOver) {
      saber.move(-1);  // negative for a move left
      return true;
    }
    else if (key == Characters.CONTROL_RIGHT && !gameOver) {
      saber.move(1);  // positive for a move right
      return true;
    }
    else if (key == Characters.ESCAPE) {
      isRunning = false;
      return true;
    }
    return false;
  }  // end of keyChar()


  protected boolean keyDown(int keycode, int time)
  // terminate the game if the green or red keys are pressed
  {
    // System.out.println("keyDown: " + keycode);
    if ((keycode == GREEN_KEY) || (keycode == RED_KEY)) {
      isRunning = false;
      return true;
    }
    return false;
  }  // end of keyDown()
  
  
}  // end of SaberScreen class
