
// ClipsPlayer.java
// Andrew Davison, August 2009, ad@fivedots.coe.psu.ac.th

/* ClipsPlayer stores a collection of AudioClip objects
   in a HashMap whose keys are their filenames. 

   The clip files are assumed to be in SOUND_DIR.

   ClipsPlayer allows a specified WAV clip to be loaded and played.

   It is possible for many clips to play at the same time, since
   each AudioClip object is responsible for playing its clip.
*/


import java.util.*;
import java.applet.*;



public class ClipsPlayer
{
  private final static String SOUND_DIR = "sounds/";

  private HashMap<String,AudioClip> clipsMap; 



  public ClipsPlayer()
  {  clipsMap = new HashMap<String,AudioClip>();  } 



  public boolean load(String fnm)
  // create a AudioClip object for fnm and store it
  {
    if (clipsMap.containsKey(fnm)) {
      System.out.println(fnm + "already stored");
      return true;
    }
    AudioClip clip = loadClip(fnm);
    if (clip != null) {
      clipsMap.put(fnm, clip);
      // System.out.println("Loaded " + fnm);
      return true;
    }
    return false;
  }  // end of load()



  private AudioClip loadClip(String fnm)
  {
    AudioClip clip = null;
    try {
	  clip = Applet.newAudioClip( getClass().getResource(SOUND_DIR + fnm + ".wav") );
    }
    catch (Exception e) {
      System.out.println("Could not load " + fnm);
    }
    return clip;
  }  // end of loadClip()



  public boolean play(String fnm)
  // play a sound once
  {  
    AudioClip clip = clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    clip.play();
    return true;
  }  // end of play()



  public boolean loop(String fnm)
  // keep repeating the sound
  {  
    AudioClip clip = clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    clip.loop();
    return true;
  }  // end of loop()


  public boolean stop(String fnm)
  // stop playing the sound
  {  
    AudioClip clip = clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    clip.stop();
    return true;
  }  // end of stop()


}  // end of ClipsPlayer class
