
// ClipsPlayer.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* ClipsPlayer stores a collection of MMAPI Player objects
   in a Hashtable whose keys are their filenames. The clips can
   be MIDI, mp3, WAV, AU, and tone sequences (JTS files).

   The clip files are assumed to be in SOUND_DIR.

   ClipsPlayer allows a specified clip to be loaded and played
   (or looped).
   There is also a static method for playing a single note for 
   a specified duration. The notes are supplied using a simple
   music notation (e.g. "C4", "B5#").

   It is _potentially_ possible for many clips to play at the same time, since
   each Player object is responsible for playing its clip, but this
   depends on whether the device supports audio mixing.
   
   I've not implemented volume changing code to hack around the problem with
   audio playing in the 4.7.0.75 OS (see 
   http://supportforums.blackberry.com/rim/board/message?board.id=java_dev&thread.id=11963)
   ---
   A tone sequence file (.jts) can be created from a MIDI file or a ring tone
   file (.rtttl) with the help of shareware tools and the RingTimeConverter.java
   application.
*/


import javax.microedition.media.*;
import javax.microedition.media.control.*;
import net.rim.device.api.media.control.*;
import java.util.*;


public class ClipsPlayer implements PlayerListener
{
  private final static String SOUND_DIR = "/sounds/";

  /* The note offsets use the "C" major scale, which
     has the order "C D E F G A B", but the offsets are
     stored in the order "A B C D E F G" to simplify their 
     lookup. */
  private static final int[] cOffsets =  {9, 11, 0, 2, 4, 5, 7};
                                        // A  B  C  D  E  F  G
  private static final int C4_KEY = 60;    
        // C4 is the "C" in the 4th octave on a piano 
  private static final int OCTAVE = 12;    // note size of an octave


  private Hashtable clipsMap; 
      // the key is the filename string, the value is a Player object
  private boolean isDeviceAvailable = true;



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
    String contentType = extractContentType(fnm);

    Player clip = null;
    try {
      clip = Manager.createPlayer(
                  getClass().getResourceAsStream(SOUND_DIR + fnm), 
                  contentType);
      clip.addPlayerListener(this);
      clip.realize();
      clip.prefetch();     // prepare audio resources needed by player
      useSpeaker(clip);
    }
    catch (Exception e) {
      System.out.println("Could not load " + fnm);
    }
    return clip;
  }  // end of loadClip()
  
  
  private void useSpeaker(Player clip)
  // make sure the player uses the device's speaker
  {
    try {
      AudioPathControl apc = null;
      Control[] ctrls = clip.getControls();
      for(int i = ctrls.length-1; i >= 0; i--)
        if(ctrls[i] instanceof AudioPathControl) {
          apc = (AudioPathControl) ctrls[i];
          break;
        } 
      if(apc != null)
       apc.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSFREE);  // speaker
    }
    catch (Exception e) {
      System.out.println("Could not use speaker");
    }
}  // end of useSpeaker()



  private String extractContentType(String fnm)
  /* choose the content type used by a player based on the clip's
     file extension */
  {
    int lastDot = fnm.lastIndexOf('.');
    if (lastDot == -1)
      return "audio/x-wav";   // default content type
    String extStr = fnm.substring(lastDot+1).toLowerCase();

    if (extStr.endsWith("au"))
      return "audio/basic";
    else if (extStr.endsWith("mp3"))
      return "audio/mpeg";
    else if (extStr.endsWith("mid"))
      return "audio/midi";
    else if (extStr.endsWith("jts"))   // tone sequences
      return "audio/x-tone-seq";

    return "audio/x-wav";   // default content type
  }  // end of extractContentType()


  public boolean play(String fnm)
  // play a clip once
  {  return play(fnm, false);  }



  private boolean play(String fnm, boolean isRepeating)
  // play a clip once or multiple times
  {  
    if (!isDeviceAvailable) {
      System.out.println("Device not available");
      return false;
    }

    Player clip = (Player) clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    
    if (clip.getState() == Player.STARTED){
      System.out.println("Clip for " + fnm + " already playing");
      return true;
    }
    
    try {
      if (isRepeating)
        clip.setLoopCount(-1);  // play indefinitely
      clip.start();
      return true;
    }
    catch(Exception e)
    { System.out.println("Could not play " + fnm);
      return false;
    }
  }  // end of play()


  public boolean loadPlay(String fnm)
  // load and play a clip once
  {
    boolean isLoaded = load(fnm);
    if (!isLoaded)
      return false;
    else
      return play(fnm);
  }  // end of loadPlay()




  public boolean isPlaying(String fnm)
  // is the specified clip currently playing
  {  
    if (!isDeviceAvailable) {
      System.out.println("Device not available");
      return false;
    }

    Player clip = (Player) clipsMap.get(fnm);
    if (clip == null) {
      System.out.println("No loaded clip for " + fnm);
      return false;
    }
    
    return (clip.getState() == Player.STARTED);
  }  // end of isPlaying()
  
  
  
  public boolean loop(String fnm)
  // keep repeating the clip
  {  return play(fnm, true);  }


  public boolean loadLoop(String fnm)
  // load and play a clip repeatedly
  {
    boolean isLoaded = load(fnm);
    if (!isLoaded)
      return false;
    else
      return loop(fnm);
  }  // end of loadPlay()



  public boolean stop(String fnm)
  // stop playing a clip
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



  public void close()
  // close all the players
  {
    Enumeration keys = clipsMap.keys();
    while (keys.hasMoreElements())
      close( (String) keys.nextElement() );
  }      


  public void close(String fnm)
  // close a player
  {  
    Player clip = (Player) clipsMap.get(fnm);
    if (clip != null) {
      try {
        clip.stop();
        clip.deallocate();
        clip.close();
        clip = null;
      }
      catch(Exception e){}
    }  
  }  // end of closes()



  public void playerUpdate(Player clip, String event, Object eventData) 
  {
    try {
      if (event.equals(PlayerListener.DEVICE_UNAVAILABLE))      // incoming phone call
        isDeviceAvailable = false;
      else if (event.equals(PlayerListener.DEVICE_AVAILABLE))   // finished phone call
        isDeviceAvailable = true;
        
      System.out.println("playerUpdate() event: " + event);
    } 
    catch (Exception e) 
    {  System.out.println(e); }
  }  // end of playerUpdate()


  // --------------------------- tone playing ---------------------
  
  public static void playTone(String noteStr, int duration)
  {
    int note = convertToNote(noteStr);
    try {
      Manager.playTone(note, duration, 100);   // always play at max volume
    }
    catch(Exception e) {}
  }  // end of playTone()




  private static int convertToNote(String noteStr)
  /* Convert a note string (e.g. "C4", "B5#") into a note. */
  {
    char[] letters = noteStr.toCharArray();

    if (letters.length < 2) {
      System.out.println("Incorrect note syntax; using C4");
      return C4_KEY;
    }

    // look at note letter in letters[0]
    int c_offset = 0;
    if ((letters[0] >= 'A') && (letters[0] <= 'G'))
      c_offset = cOffsets[letters[0] - 'A'];
    else
      System.out.println("Incorrect letter: " + letters[0] + ", using C");

    // look at octave number in letters[1]
    int range = C4_KEY;
    if ((letters[1] >= '0') && (letters[1] <= '9'))
      range = OCTAVE * (letters[1] - '0' + 1);  // plus 1 for midi
    else
      System.out.println("Incorrect number: " + letters[1] + ", using 4");

    // look at optional sharp in letters[2]
    int sharp = 0;
    if ((letters.length > 2) && (letters[2] == '#'))
      sharp = 1;    // a sharp is 1 note higher 
                    // (represented by the black keys on a piano)
    int key = range + c_offset + sharp;
    // System.out.println("note: " + noteStr + "; key: " + key);

    return key;
  }  // end of convertToNote()

}  // end of ClipsPlayer class
