
// SwingSaber.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* Swing a light saber left and right across the screen to destroy 
   laser blasts dropping from above.

   Illustrates several useful game elements:
     * separate introduction and game-play screens

     * the introductory screen uses a splash image, ActiveRichTextField

     * game play using an update-draw-sleep animation loop

     * a variety of input processing
         - acceleormeter, touch, trackball, key presses

     * the use of reusable Sprite and ExplodingSprite classes

     * a sound clip playing class (ClipsPlayer) which can play
       MIDI, mp3, AU, tone sequences (JTS files), and individual tones
*/


import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;


public class SwingSaber extends UiApplication 
{

  public SwingSaber()
  {
    /* stop device tilting from affecting this app's orientation, so
       screen dimensions can't change and accelerometer processing is simplified
    */
    Ui.getUiEngineInstance().setAcceptableDirections(
                                   Display.DIRECTION_PORTRAIT);
    
    // create the game-play and introduction screens
    SaberScreen gameScreen = new SaberScreen();
    IntroScreen introScreen = new IntroScreen(gameScreen);
    
    pushScreen(introScreen);   // start with the introduction screen
  }  // end of SwingSaber()

  // ---------------------------------------------------

  public static void main(String args[])
  { SwingSaber theApp = new SwingSaber();
    theApp.enterEventDispatcher();
  }

} // end of SwingSaber

