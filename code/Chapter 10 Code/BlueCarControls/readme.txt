
Chapter 10. Remotely Drive a (toy) Sports Car

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

   BlueCarControls.java, ButtonPanel.java
   ClipsPlayer.java, ServiceFinder.java
       - the PC Bluetooth client for the BaseStation server
       - uses JavaSE and BlueCove

   images\
     rightOff.png, rightOn.png
     fwdOff.png, fwdOn.png
     leftOff.png, leftOn.png
     revLeftOff.png, revLeftOn.png
     revOff.png, revOn.png
     revRightOff.png, revRightOn.png
     carHornOff.png, carHornOn.png
        - images used to display inactive (off) and active (on) buttons


   sounds\
     left.wav, rev.wav, revLeft.wav, revRight.wav
     revving.wav, right.wav, fwd.wav
     carHorn.wav
     ignition.wav
        - sound clips played when a button is pressed down


----------------------------
Compilation and Execution:

  > javac -cp "bluecove-2.1.0.jar;." *.java

  > java -cp "bluecove-2.1.0.jar;." BlueCarControls

     - the BaseStation server should be running

     - the PC must be able to send Bluetooth messages
       (e.g. have a Bluetooth dongle)

     - bluecove-2.1.0.jar must be in the current directory

---------
Last updated: 8th August 2009
