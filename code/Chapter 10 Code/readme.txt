
Chapter 10. Remotely Drive a (toy) Sports Car

From the book:

  Learn BlackBerry Games Development
  Carol Hamer and Andrew Davison
  Apress, 2010
  Website: http://frogparrot.net/blackberry/


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

This chapter describes how a BlackBerry can control the Dream Cheeky USB
Remote Control Mini Car by commuicating with it using Bluetooth and USB.

In ASCII-art details:


     BlackBerry ----------> PC ----------------------> garage -----------> car
  (CarController)      (BaseStation)      USB cable            infrared
 a Bluetooth client   a Bluetooth server
                      and sender of USB
                      control transfers

For testing purposes, I also wrote a PC version of CarController, so
I could test things without the BlackBerry. It is called BlueCarController.


============================
Subdirectory Contents

* CarTester/
    - a simple test-rig for controlling the Dream Cheeky car on a PC
    - uses JavaSE, LibusbJava, and libusb-win32
    - it moves the car forwards, waits, then puts into reverse

* BaseStation/
    - the Blutooth server and USB message sender on a PC
    - uses JavaSE and BlueCove
    - make sure CarTester/ is working before trying this code

* CarControls/
    - the BlackBerry Bluetooth client 

* BlueCarControls/
    - a version of CarControls for a PC
    - uses JavaSE and BlueCove


For more details, read the readme.txt files in the subdirectories.


============================
Hardware Requirements:

* the Dream Cheeky USB Remote Control Mini Car
  http://www.dreamcheeky.com/

* Bluetooth dongle on the PC, and one on the Netbook

============================
Software Requirements:

* LibusbJava
  http://libusbjava.sourceforge.net/wp/
     - see below

* libusb-win32
    - libusb-win32-filter-bin-0.1.12.1.exe
    - libusb-win32-device-bin-0.1.12.1.tar.gz
  http://libusb-win32.sourceforge.net
    - see below

* BlueCove
  http://bluecove.org/
    - see below

* USBDeview  
    - optional, but useful
  http://www.nirsoft.net/utils/usb_devices_view.html

* SourceUSB
    - not free, but a 30-day trial is available
    - optional, but useful
  http://www.sourcequest.com/


============================
Installing LibusbJava

1. Get libusb-win32 (http://libusb-win32.sourceforge.net) 
   Download and install: 
     - libusb-win32-filter-bin-0.1.12.1.exe   (execute this)
     - libusb-win32-device-bin-0.1.12.1.tar.gz  (unzip this)

2. Get libusbjava (http://libusbjava.sourceforge.net/wp)
   Download and install: 
     - ch.ntb.usb-0.5.9.jar          (store somewhere convenient)
     - LibusbJava_dll_0.2.4.0.zip    (unzip this in the same place)

3. Test libusbjava

The libusbjava JAR includes a number of test applications, 
the simplest being a viewer for all the USB devices 
connected to the machine:
 
java -Djava.library.path="d:\libusbjava" 
     -cp "d:\libusbjava\ch.ntb.usb-0.5.9.jar;."  ch.ntb.usb.usbView.UsbView

The paths to the DLL and JAR files need to match their location on your machine.


============================
Installing BlueCove

1. Get BlueCove (http://bluecove.org/). 
   Download bluecove-2.1.0.jar        (store somewhere convenient)

It seems to work best if your Bluetooth device uses Window XP's 
built-in Bluetooth stack.

---------
Last updated: 7 March 2010
