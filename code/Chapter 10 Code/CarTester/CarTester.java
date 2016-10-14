
// CarTester.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2009

/* Move the Dream Cheeky USB RC car forwards, wait, then moves it back

     - libusb-win32 and libusbjava must be installed

     - the Dream Cheeky Garage must be plugged into a USB port

     - a libusb-win32 device driver for the car must have been created and
       installed into Windows (use inf-wizard.exe, which can be found in
       libusb-win32-device-bin-0.1.12.1.tar.gz)

   Usage:
      > compile CarTester.java
      > run CarTester
*/


import ch.ntb.usb.*;


public class CarTester
{
  private long handle = 0;   // used to communicate with the USB device


  public CarTester(short vid, short pid)
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
  }  // end of CarTester()



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
    
   // iterate through all the buses and devices until the right one is found
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
      LibusbJava.usb_close(handle);
      handle = 0;
    }
  }  // end of close()
	

  public void forward(int period)
  // move forward for period ms
  { System.out.println("  forward");
    sendCommand(0x01, period);  
  }


  public void backward(int period)
  // move backwards for period ms
  { System.out.println("  backward");
    sendCommand(0x08, period);  
  }


  public void sendCommand(int opCode, int period)
  // execute the opCode operation for period ms
  {
    if (handle > 0) {
      sendControl(opCode);
      wait(period);
      sendControl(0xFF);    // stop the operation
    }
  }  // end of sendCommand()



  private void wait(int ms)
  { try {
      Thread.sleep(ms);
    }
    catch(InterruptedException e) {}
  }  // end of wait()




  private void sendControl(int opCode)
  // send a USB control transfer representing a SEND REPORT HID command
  {
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
      close();
      System.exit(1);
    }
  }  // end of sendControl()



  // ------------------------------------ main --------------------------


  public static void main(String[] args)
  {
    CarTester carDev = new CarTester((short)0x0a81, (short)0x0702);
          // the IDs were obtained by looking at the car using USBDeview

    carDev.forward(400);    // 200ms == period of movement

    System.out.println("Waiting 2 secs");
    try {
      Thread.sleep(2000);
    }
    catch(InterruptedException e) {}

    carDev.backward(400);

    carDev.close();
  }  // end of main()


}  // end of CarTester class
