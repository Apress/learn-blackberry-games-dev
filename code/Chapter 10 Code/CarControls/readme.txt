
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

   CarControls.java, PictureButton.java, ClipsPlayer.java
       - the BlackBerry Bluetooth client for the BaseStation server

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

   toyCar.png
      - the RIMlet icon

   CarControls.jdp, CarControls.rapc
       - sample BlackBerry JDE project files

----------------------------
Compilation and Execution:

Inside the JDE, create a new Project called CarControls, and
add:
  - the 3 Java files
  - all the images inside images\,
  - all the sounds in sounds\

 Make toyCar.png the resource icon file (via the projects properties)

 Build and Run inside the simulator.

----------------------------
Installation:

There's not much point running this on the RIM Device simulator
since it doesn't support Bluetooth emulation. Either install it on a
real device, or use the PC version of the clienr, BlueCarControls.

To run CarControls on a real device, you will need:
  * build the code
  * to create an ALX file using  Build > Generate ALX file
  * sign the code using Build > Request Signatures

---------
Last updated: 13th December 2009
