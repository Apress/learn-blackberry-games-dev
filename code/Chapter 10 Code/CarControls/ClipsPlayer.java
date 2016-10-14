
// ClipsPlayer.java
// Andrew Davison, August 2009, ad@fivedots.coe.psu.ac.th

/* ClipsPlayer stores a collection of Player objects
   in a Hashtable whose keys are their filenames. 

   The clip files are assumed to be in SOUND_DIR.

   ClipsPlayer allows a specified WAV clip to be loaded and played.

   It is possible for many clips to play at the same time, since
   each Player object is responsible for playing its clip.
*/


import javax.microedition.media.*;
import javax.microedition.media.control.*;

import java.util.*;



public class ClipsPlayer
{
  private final static String SOUND_DIR = "/sounds/";

  private Hashtable clipsMap; 
      // the key is the filename string, the value is a Player object



  public ClipsPlayer()
  {  clipsMap = new Hashtable();  } 



  public boolean load(String fnm)
  // create a Player object for fnm and store it
  {
    if (clipsMap.containsKey(fnm)) {
      System.out.println(fnm + "already stored");
      return true;
    }
    Player clip = loadClip(fnm);
    if (clip != null) {
      clipsMap.put(fnm, clip);
      System.out.println("Loaded " + fnm);
      return true;
    }
    return false;
  }  // end of load()



  private Player loadClip(String fnm)
  {
    Player clip = null;
    try {
      clip = Manager.createPlayer(
                  getClass().getResourceAsStream(SOUND_DIR + fnm + ".wav"), 
                  "audio/x-wav");
      clip.prefetch();     // prepare audio resources needed by player
    }
    catch (Exception e) {
      System.out.println("Could not load " + fnm);
    }
    return clip;
  }  // end of loadClip()



  public boolean play(String fnm)
  // play a sound once
  {  
    Player clip = (Player) clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    try {
      clip.start();
      return true;
    }
    catch(Exception e)
    { System.out.println("Could not play " + fnm);
      return false;
    }
  }  // end of play()



  public boolean loop(String fnm)
  // keep repeating the sound
  {  
    Player clip = (Player) clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    try {
      clip.setLoopCount(-1);  // play indefinitely
      clip.start();
      return true;
    }
    catch(Exception e)
    { System.out.println("Could not loop " + fnm);
      return false;
    }
  }  // end of loop()


  public boolean stop(String fnm)
  // stop playing the sound
  {  
    Player clip = (Player) clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    try {
      clip.stop();
      return true;
    }
    catch(Exception e)
    { System.out.println("Could not stop " + fnm);
      return false;
    }
  }  // end of stop()


}  // end of ClipsPlayer class
