
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

This is a version of SwingSaber that has been tested on OS v4.2.

The main changes are the removal of all references to the accelerometer
and the touch screen.


   SwingSaber.java, IntroScreen.java, SaberScreen.java,
   LightSaber.java, SaberTrail.java,
   BlastSprite.java, ExplodingSprite.java, Sprite.java
   ClipsPlayer.java
       - the SwingSaber game (9 Java files)

   images\
      - title.png, knight.png, wallBlast.png, wallBlasts.png,
        blast.png, explosion.png

   sounds\
     - starWars.mid, hit0.wav, hit1.wav, hit2.wav,
       underestimate.wav, forcestrong.wav

   lightsaber.png
      - the RIMlet icon

   SwingSaber.jdp, SwingSaber.rapc
       - sample BlackBerry JDE project files

----------------------------
Compilation and Execution

Inside the JDE, create a new Project called SwingSaber, and
add:
  - the 9 Java files
  - all the images inside images\,
  - all the sounds in sounds\

 Make lightsaber.png the resource icon file (via the projects properties)

 Build and Run inside the simulator.

 Note: 9 warnings are generated, due to unused methods in the Sprite
       and ClipsPlayer classes; they can be ignored. 

       There is a deprecation warning if you compile this for OS v4.7,
       since it uses new Graphics() in ExplodingSprite.java rather than
       Graphics.create().

----------------------------
Audio Mixing 

I have commented out the test for audio mixing support in loadSounds()
in the SaberScreen class (at line 137, or thereabouts). 

If the test is included, the simulator reports "false", and only the 
MIDI background music is played.

----------------------------
Device Installation

To run SwingSaber on a real device, you will need:
  * build the code
  * to create an ALX file using  Build > Generate ALX file
  * sign the code using Build > Request Signatures

---------
Last updated: 15th December 2009
