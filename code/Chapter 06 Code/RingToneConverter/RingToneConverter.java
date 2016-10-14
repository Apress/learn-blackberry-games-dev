
// RingToneConverter.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, Septemeber 2009

/* Parse a ringtone sequence in RTTTL (Ringing Tones text transfer language) format.
   converting it to the MMAPI tone player format (.jts). Output is text to a .txt
   file and binary to a .jts file.

   Based on the RingToneConverter.java example in the WTK MobileMediaAPI examples,

   Info on RTTTL format:
      * http://en.wikipedia.org/wiki/Ring_Tone_Transfer_Language
      * http://www.srtware.com/index.php?/ringtones/rtttlformat.php
      * http://www.activexperts.com/xmstoolkit/sms/rtttl/

   Info on MMAPI tone player format
     - see the ToneControl documentation in the JavaME API docs
*/


import java.util.*;
import java.io.*;


public class RingToneConverter
{
  // constants taken from MMAPI's ToneControl class
  private static final byte VERSION = -2;
  private static final byte TEMPO = -3;
  private static final byte C4 = 60;
  private static final byte SILENCE = -1;

  // note: strings must be sorted in descending order of their length
  private static final String[] durationStrings = {"16", "32", "1", "2", "4", "8" };
  private static final int[] durationValues = { 16, 32, 1, 2, 4, 8 };

  private static final String[] noteStrings = {
                                          "C#", "D#", "F#", "G#", "A#", "C", "D", 
                                          "E", "F", "G", "A", "H", "B" };
  private static final int[] noteValues = {1, 3, 6, 8, 10, 0, 2, 
                                           4, 5, 7, 9, 11, 11  }; // H (German) == B (English)

  private static final String[] scaleStrings = { "3", "4", "5", "6", "7", "8" };
  private static final int[] scaleValues = { 3, 4, 5, 6, 7, 8 };

  private static final String[] hexChars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", 
                                             "A", "B", "C", "D", "E", "F"};



  private static ArrayList<Integer> notes, lengths;
  private static int readPos;
  private static char lastSeparator;
  private static int tempo = 80; // in beats per second


  public static void main(String[] args) 
  {
   if (args.length != 1)
      System.out.println("Usage: java RingToneConverter <rtttl fnm>");
    else {
      try {
        DataInputStream in = new DataInputStream( new FileInputStream(args[0]) );
        byte[] inputData = readInputStream(in);
        in.close();

        byte[] sequence = buildSequence(inputData);

        String fnm = args[0].substring(0, args[0].indexOf("."));
        dumpSequence(sequence, fnm+".txt");
        saveSequence(sequence, fnm+".jts");
      } 
      catch (Exception e) {
        System.err.println(e);
      }
    }
  }  // end of main()



  private static byte[] readInputStream(InputStream is) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[128];

    while (true) {
      int read = is.read(buffer);
      if (read < 0)
        break;
      baos.write(buffer, 0, read);
    }
    is.close();
    return baos.toByteArray();
  }  // end of readInputStream()



  private static byte[] buildSequence(byte[] data) throws Exception
  {
    notes = new ArrayList<Integer>();
    lengths = new ArrayList<Integer>();

    if (!parseRTTTL(data))
      throw new Exception("Not a supported ringtone text file");

    if (tempo < 20 || tempo > 508)
      throw new Exception("tempo is out of range");

    byte[] sequence = new byte[notes.size()*2 + 4];
    sequence[0] = VERSION;
    sequence[1] = 1;
    sequence[2] = TEMPO;
    sequence[3] = (byte) ((tempo >> 2) & 0x7f);
    for (int i = 0; i < notes.size(); i++) {
      sequence[2*i + 4] = (byte) (((Integer) notes.get(i)).intValue() & 0xff);
      sequence[2*i + 5] = (byte) (((Integer) lengths.get(i)).intValue() & 0x7f);
    }

    return sequence;
  }  // end of buildSequence()




  private static boolean parseRTTTL(byte[] data)
  // Parse a ringtone sequence in the RTTTL format
  {
    boolean result = true;
    try {
      tempo = 63;             // default tempo is 63
      int defDuration = 4;    // default duration is a quarter note
      int defScale = 6;       // default octave is 6

	 // start with Name, followed by colon :
	 String songName = readString(data, ":");
	 if (songName.length() > 0)
	   System.out.println("Song name: " + songName);

      // read defaults
      do {
        String def = readString(data, ",:");
        if (def != "") {
          if (def.startsWith("D="))
            defDuration = Integer.parseInt(def.substring(2));
          else if (def.startsWith("O="))
            defScale = Integer.parseInt(def.substring(2));
          else if (def.startsWith("B="))
            tempo = Integer.parseInt(def.substring(2));
          else
            throw new Exception("Unknown default \"" + def + "\"");
        }
        else {
          if (lastSeparator != ':')
            throw new Exception("':' excepted");
          break;
        }
      } while (lastSeparator == ',');

      // read note commands
      StringBuffer noteCommand = new StringBuffer();
      while (lastSeparator != 'E') {
        noteCommand.setLength(0);
        noteCommand.append(readString(data, ","));
        if (noteCommand.length() == 0)
          break;
        int duration = tableLookup(noteCommand, durationStrings, durationValues, defDuration);
        int note = tableLookup(noteCommand, noteStrings, noteValues, -1);

        int dotCount = 0;    // dotted duration ?
        // dot may appear before or after scale
        if (noteCommand.length() > 0 && noteCommand.charAt(0) == '.') {
          dotCount = 1;
          noteCommand.deleteCharAt(0);
        }

        if (note >= 0) {    // octave
          int scale = tableLookup(noteCommand, scaleStrings, scaleValues, defScale);
          note = C4 + ((scale - 4) * 12) + note;
        }
        else {  // pause?
          if (noteCommand.charAt(0) == 'P') {
            note = SILENCE;
            noteCommand.deleteCharAt(0);
          }
          else
            throw new Exception("Unexpected note command: '" + noteCommand.toString() + "'");
        }

        // dot may appear before or after scale
        if (noteCommand.length() > 0 && noteCommand.charAt(0) == '.') {
          dotCount = 1;
          noteCommand.deleteCharAt(0);
        }

        if (noteCommand.length() > 0)
          throw new Exception("Unexpected note command: '" + noteCommand.toString() + "'");
        addNote(note, duration, dotCount);
      }

      System.out.println("RingToneConverter: read " + notes.size() + " notes successfully.");
    }
    catch (Exception e) {
      System.out.println(e);
      result = false;
    }
    return result;
  }  // end of parseRTTTL()



  private static String readString(byte[] data, String separators)
  {
    int start = readPos;
    lastSeparator = 'E'; // end of file
    boolean hasWhiteSpace = false;

    while (lastSeparator == 'E' && readPos < data.length) {
      char input = (char) data[readPos++];
      if (input <= 32)
        hasWhiteSpace = true;
      for (int i = 0; i < separators.length(); i++) {
        if (input == separators.charAt(i)) {
          // separator found
          lastSeparator = input;
          break;
        }
      }
    }
    int end = readPos - 1;
    if (lastSeparator != 'E')  // don't return separator
      end--;

    String result = "";
    if (start <= end) {
      result = new String(data, start, end - start + 1);
      if (hasWhiteSpace) {    // trim result
        StringBuffer sbResult = new StringBuffer(result);
        int i = 0;
        while (i < sbResult.length()) {
          if (sbResult.charAt(i) <= 32)
            sbResult.deleteCharAt(i);
          else
            i++;
        }
        result = sbResult.toString();
      }
      result = result.toUpperCase();
    }
    System.out.println("Returning '" + result + "'  with lastSep='" + lastSeparator + "'");
    return result;
  }  // end of readString()



  private static int tableLookup(StringBuffer command, String[] strings, int[] values, int defValue)
  {
    String sCmd = command.toString();
    int result = defValue;
    for (int i = 0; i < strings.length; i++) {
      if (sCmd.startsWith(strings[i])) {
        command.delete(0, strings[i].length());
        result = values[i];
        break;
      }
    }
    return result;
  }  // end of tableLookup()



  private static void addNote(int note, int duration, int dotCount)
  /* add a note to the notes and lengths lists.
        note - 0-128, as defined in ToneControl
        duration - the divider of a full note. E.g. 4 stands for a quarter note
        dotCount - if 1, then the duration is increased by half its length, 
                   if 2 by 3/4 of its length, etc.
   */
  {
    // int length = (60000 * 4) /(duration * tempo);
    int length = 64 / duration;
    int add = 0;
    int factor = 2;

    while(dotCount > 0) {
      add += length / factor;
      factor *= 2;
      dotCount--;
    }
    length += add;
    if (length > 127)
      length = 127;

    notes.add(note);
    lengths.add(length);
  }  // end of addNote()



  private static void dumpSequence(byte[] sequence, String outFnm)
  // Dump the sequence as hexadecimal numbers to outFnm txt file.
  {
    System.out.println("Writing sequence (as text) to " + outFnm);
    try {
      PrintStream ps = new PrintStream( new FileOutputStream(outFnm) );

      for (int i = 0; i < sequence.length; i++) {
        ps.print( hexChars[(sequence[i] & 0xF0) >> 4] + 
                  hexChars[sequence[i] & 0xF]  + " ");
        if (i%8 == 7)
          ps.println();
      }
      ps.println();
      ps.close();
    }
    catch (Exception e) {
      System.out.println(e);
    }
  }  // end of dumpSequence()



  private static void saveSequence(byte[] sequence, String outFnm)
  // Dump the sequence binary to outFnm jts file.
  {
    System.out.println("Writing sequence to " + outFnm);
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bos.write(sequence, 0, sequence.length);

      FileOutputStream fos = new FileOutputStream( new File(outFnm));
      bos.writeTo(fos);
      bos.close();
      fos.close();  
    } 
    catch (Exception e) {
      System.out.println(e);
    }
  }  // end of saveSequence()

}  // end of RingToneConverter class
