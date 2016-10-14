
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

* BaseStation.java, USBCar.java
    - the Blutooth server and USB message sender on a PC
    - uses JavaSE and BlueCove

* compile.bat, run.bat
     - the batch files assume that the libusbJava software 
       (ch.ntb.usb-0.5.9.jar, LibusbJava.dll) are in d:\libusbjava\
       *and*
       bluecove-2.1.0.jar is in the current directory


----------------------------
Compilation and Execution:

  > compile *.java

  > run BaseStation
     - the Dream Cheeky Garage must be plugged into a USB port

     - the PC must be able to receive Bluetooth messages
       (e.g. have a Bluetooth dongle)

     - a libusb-win32 device driver for the car must have been created and
       installed into Windows (use inf-wizard.exe, which can be found in
       libusb-win32-device-bin-0.1.12.1.tar.gz)

     - use ctrl-C to stop the server

The client for this server is eith BlueCarControls on a PC or
CarControls on a BlackBerry.

---------
Last updated: 8th August 2009
