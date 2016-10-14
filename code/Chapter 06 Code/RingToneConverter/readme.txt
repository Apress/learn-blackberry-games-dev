
Chapter 6. Swingin' Light Saber

From the book:

  Learn BlackBerry Games Development with JavaME Platform
  Carol Hamer and Andrew Davison
  Apress, 2010
  Website: ??


Author responsible for this chapter:
    Andrew Davison
    Dept. of Computer Engineering
    Prince of Songkla University
    Hat yai, Songkhla 90112, Thailand
    E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention the book's title and authors,
and include a link to the website.

Thanks,
  Carol and Andrew


============================
Directory contents:

   * RingToneConverter.java
    - converter of RTTTL text files into MMAPI JTS format
    - uses JavaSE

   * test.rtx
      - a text file containing a short RTTTL tone sequence


----------------------------
What does RingToneConverter do?

It parses a ringtone sequence in RTTTL (Ringing Tones text transfer language) format.
converting it to the MMAPI tone player format (.jts). Output is text into a .txt
file and JTS binary to a .jts file.

Closely based on the RingToneConverter.java example in the WTK MobileMediaAPI
example, but ported to run on JavaSE, not Java ME or on the BlackBerry.


----------------------------
Compilation and Execution

Note: RingToneConverter uses JavaSE, not JavaME or BlackBerry tools.

> javac RingToneConverter.java

> java RingToneConverter <RTTTL text file>

e.g.

> java RingToneConverter test.rtx

     - generates test.txt and test.jts
        * the text file contain a text version of the hexadecimal
          info in the JTS file
     - the JTS file can be loaded and played by MMAPI


----------------------------
Background

A short RTTTL Example:
     Entertainer:d=4, o=5, b=140:8d, 8d#, 8e, c6, 8e, c6, 8e,
     2c.6, 8c6, 8d6, 8d#6, 8e6, 8c6, 8d6, e6, 8b, d6, 2c6, p,
     8d, 8d#, 8e, c6, 8e, c6, 8e, 2c.6, 8p, 8a, 8g, 8f#, 8a,
     8c6, e6, 8d6, 8c6, 8a, 2d6

Info on the RTTTL format:
      * http://en.wikipedia.org/wiki/Ring_Tone_Transfer_Language
      * http://www.srtware.com/index.php?/ringtones/rtttlformat.php
      * http://www.activexperts.com/xmstoolkit/sms/rtttl/

RTTTL ringtone sites:
      * http://merwin.bespin.org/db/rts/
      * http://www.zedge.net/ringtones/

Info on MMAPI JTS tone player format
     - see the ToneControl documentation in the JavaME API docs

----------------------------
Useful audio tools:

    *  Audacity (free)
          - a great tool for manipulating WAV files
         (http://audacity.sourceforge.net/)

    *  Anvil Studio (free)
         - supports the capture, editing, and direct composing of MIDI.
           It also handles WAV files.
       (http://www.anvilstudio.com/)

     * MidiPiano (free)
         - a simple piano software keyboard for creating MIDI sequences
       (http://midipiano.googlepages.com/)

     * Quick Ringtone (commercial; 15 day trial)
          - converts MIDI to RTTTL (and other formats)
       (http://www.Lightthoughts.com/)

     * Ringtone Converter (shareware; free download but some features disabled)
       (http://www.codingworkshop.com/ringtones/)
          - piano keyboard for creating RTTTL
          - converts MIDI to RTTTL (and other formats)

---------
Last updated: 15th December 2009
