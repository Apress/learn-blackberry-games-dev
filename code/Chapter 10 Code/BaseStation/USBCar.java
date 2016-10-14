
// USBCar.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2009

/* Control the Dream Cheeky USB RC car through the USB cable 
   linked to the car's garage.

     - libusb-win32 and libusbjava must be installed

     - the Dream Cheeky Garage must be plugged into a USB port

     - a libusb-win32 device driver for the car must have been created and
       installed into Windows (use inf-wizard.exe, which can be found in
       libusb-win32-device-bin-0.1.12.1.tar.gz)


   This code is based on CarTester.java in the CarTester/ directory
*/


import ch.ntb.usb.*;


public class USBCar
{
  private long handle = 0;


  public USBCar(short vid, short pid)
  { 
    LibusbJava.usb_init();

    Usb_Device dev = findDevice(vid, pid);
    if (dev == null) {
      System.out.println("Device not found");
      System.exit(1);
    }

    System.out.println("Found Device. Openning...");
    handle = LibusbJava.usb_open(dev);
    if (handle == 0) {
      System.out.println("Failed to Open");
      System.exit(1);
    }
  }  // end of USBCar()



  private Usb_Device findDevice(short vid, short pid)
  // return the USB device with the specified vendor and product IDs
  {
    System.out.println("Looking for device: (vendor: 0x" + Integer.toHexString(vid) + 
                                          "; product: 0x" + Integer.toHexString(pid) + ")");
    // LibusbJava.usb_set_debug(255);   // switch on debugging (0 == disabled)
    LibusbJava.usb_find_busses();
    LibusbJava.usb_find_devices();

    Usb_Bus bus = LibusbJava.usb_get_busses();
    // System.out.println("bus: " + bus);
    
    while (bus != null) {
      Usb_Device dev = bus.getDevices();
      while (dev != null) {
        Usb_Device_Descriptor desc = dev.getDescriptor();
        // System.out.println("  examining device: " + desc);
        if ((desc.getIdVendor() == vid) && (desc.getIdProduct() == pid))
          return dev;
        dev = dev.getNext();
      }
      bus = bus.getNext();
    }
    return null;
  }  // end of findDevice()
	

	
  public void close()
  {
    System.out.println("Closing");
    if (handle > 0) {
      stop();    // just to be sure
      LibusbJava.usb_close(handle);
      handle = 0;
    }
  }  // end of close()
	


  // --------------- car movement ops -------------------------

  public void forward()
  { // System.out.println("  forward");
    sendControl(0x01);  
  }

  public void turnRight()
  { // System.out.println("  turn right");
    sendControl(0x02);  
  }

  public void revRight()
  { // System.out.println("  reverse right");
    sendControl(0x04);  
  }

  public void reverse()
  { // System.out.println("  reverse");
    sendControl(0x08);  
  }

  public void revLeft()
  { // System.out.println("  reverse left");
    sendControl(0x10);  
  }

  public void turnLeft()
  { // System.out.println("  turn left");
    sendControl(0x20);  
  }

  public void stop()
  {  // System.out.println("  stop");
     sendControl(0xFF);  
  } 


  private void sendControl(int opCode)
  // send a USB control transfer representing a SEND REPORT HID command
  {
    if (handle == 0) {
      System.out.println("No handle for USB device");
      return;
    }

    // System.out.println("Sending opCode: " + opCode);
    byte[] bytes = { new Integer(opCode).byteValue() };

    int rval = LibusbJava.usb_control_msg(handle,
                            USB.REQ_TYPE_DIR_HOST_TO_DEVICE | 
                            USB.REQ_TYPE_TYPE_CLASS | 
                            USB.REQ_TYPE_RECIP_INTERFACE, 
                    0x09, 0x0200, 0,
                    bytes, bytes.length, 2000);
    // System.out.println("rval: " + rval);
    if (rval < 0) {
      System.err.println("Control Error (" + rval + "):\n  " + 
                                             LibusbJava.usb_strerror() );
      LibusbJava.usb_close(handle);
      handle = 0;
      System.exit(1);
    }
  }  // end of sendControl()



}  // end of USBCar class
