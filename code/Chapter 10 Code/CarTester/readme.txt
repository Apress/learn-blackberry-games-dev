
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

   CarTester.java
    - a simple test-rig for controlling the Dream Cheeky car on a PC
    - uses JavaSE, LibusbJava, and libusb-win32

  Two batch files:
     * compile.bat, run.bat
     - the batch files assume that the libusbJava software 
       (ch.ntb.usb-0.5.9.jar, LibusbJava.dll) are in d:\libusbjava\


----------------------------
Compilation and Execution:

  > compile CarTester.java

  > run CarTester
     - the Dream Cheeky Garage must be plugged into a USB port

     - a libusb-win32 device driver for the car must have been created and
       installed into Windows (use inf-wizard.exe, which can be found in
       libusb-win32-device-bin-0.1.12.1.tar.gz)

     - CarTester moves the car forwards, waits, then moves it back

---------
Last updated: 8th August 2009
