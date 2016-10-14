
// ServiceFinder.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2009

/* Create a Bluetooth discovery agent, and carry out a devices
   search followed by services search.

   Each matching device must be a PC or phone.
   The devices are stored in deviceList.

   Each of the service searches are carried out sequentially, 
   one at a time.

   A matching service must have the same UUID as that specified in 
   UUIDStr, and the same service name as srchServiceName. It must 
   use the RFCOMM protocol.

   Each service record is stored along with its device name in the 
   serviceTable hashtable, using the device name as the 
   key, the service record as the value.

   At the end, the hashtable is passed to the top-level via the
   showServices() method.

   A very similar version of this class is explained in detail in:
      "An Echoing Client/Server Application Using Bluetooth", 
      http://fivedots.coe.psu.ac.th/~ad/jg/
          - it's chapter B1 near the bottom of the page
*/

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.*;



public class ServiceFinder implements DiscoveryListener
{
  private BlueCarControls client;
  private String UUIDStr;           // the UUID of the desired service
  private String srchServiceName;   // the name of the desired service

  private DiscoveryAgent agent;

  // stores the remote devices found during the device search
  private Vector<RemoteDevice> deviceList;

  /* table of matching services, stored as pairs of the form
     {device name, serviceRecord} */
  private Hashtable<String,ServiceRecord> serviceTable;
  private boolean searchDone;



  public ServiceFinder(BlueCarControls cli, String uuid, String nm)
  // create a discovery agent then perform device and services search.
  {
    client = cli;
    UUIDStr = uuid;
    srchServiceName = nm;

    try {   
      // get the discovery agent, by asking the local device
      LocalDevice local = LocalDevice.getLocalDevice();
      agent = local.getDiscoveryAgent();

      // initialize device search data structure
      deviceList = new Vector<RemoteDevice>();

      // start the searches: devices first, services later
      System.out.println("Searching for Devices...");
      agent.startInquiry(DiscoveryAgent.GIAC, this);   // non-blocking
    }
    catch (Exception e) {
      System.out.println(e);
    }
  } // end of ServiceFinder()


 // -------------------------- device search methods ---------------

  /* deviceDiscovered() and inquiryCompleted() are called 
     automatically during the device search initiated by the 
     DiscoveryAgent.startInquiry() call.
  */


  public void deviceDiscovered(RemoteDevice dev, DeviceClass cod)
  /* A matching device was found during the device search.
     Only store it if it's a PC or phone. */
  {
    System.out.println("Device Name: " +  getDeviceName(dev)); 

    int majorDC = cod.getMajorDeviceClass();
    int minorDC = cod.getMinorDeviceClass();   // not used in the code
    // System.out.println("Major Device Class: " + majorDC + 
    //                 "; Minor Device Class: " + minorDC);
    
    // restrict matching device to PC or Phone
    if ((majorDC == 0x0100) || (majorDC == 0x0200))
      deviceList.addElement(dev);
    else
      System.out.println("Device not PC or phone, so rejected");
  } // end of deviceDiscovered()


  private String getDeviceName(RemoteDevice dev)
  /* Return the 'friendly' name of the device being examined,
     or "Device ??" */
  {
    String devName;
    try {
      devName = dev.getFriendlyName(false);  // false to reduce connections
    }
    catch (IOException e) 
    { devName = "Device ??";  }
    return devName;
  }  // end of getDeviceName()


  public void inquiryCompleted(int inqType)
  // device search has finished; start the services search
  {
    showInquiryCode(inqType);
    System.out.println("No. of Matching Devices: " + deviceList.size() + "\n");

    // start the services search
    System.out.println("Searching for Services...");
    searchForServices(deviceList, UUIDStr);
  } // end of inquiryCompleted()


  private void showInquiryCode(int inqCode)
  {
    if(inqCode == INQUIRY_COMPLETED)
      System.out.println("Device Search Completed");
    else if(inqCode == INQUIRY_TERMINATED)
      System.out.println("Device Search Terminated");
    else if(inqCode == INQUIRY_ERROR)
      System.out.println("Device Search Error");
    else 
      System.out.println("Unknown Device Search Status: " + inqCode); 
  }  // end of showResponseCode()


  // --------------------- service search methods --------------------
  

  private void searchForServices(Vector<RemoteDevice> deviceList, String UUIDStr)
  /* Carry out service searches for all the matching devices, looking
     for the RFCOMM service with UUID == UUIDStr. Also check the
     service name.
  */
  {
    UUID[] uuids = new UUID[2];   // holds UUIDs used in the search

    /* Add the UUID for RFCOMM to make sure that the matching service
       support RFCOMM. */
    uuids[0] = new UUID(0x0003);

    // add the UUID for the service we're looking for
    uuids[1] = new UUID(UUIDStr, false);

    /* we want the search to retrieve the service name attribute,
       so we can check it against the service name we're looking for */
    int[] attrSet = {0x0100};

    // initialize service search data structure
    serviceTable = new Hashtable<String,ServiceRecord>();

    // carry out a service search for each of the devices
    RemoteDevice dev;
    for (int i = 0; i < deviceList.size(); i++) {
      dev = (RemoteDevice) deviceList.elementAt(i);
      searchForService(dev, attrSet, uuids);
    }

    // tell the top-level the result of the searches
    if (serviceTable.size() > 0)
      client.showServices(serviceTable);
    else {
      System.out.println("No Matching Services Found");
      client.showServices(null);
    }
  } // end of searchForServices()


  private void searchForService(RemoteDevice dev, int[] attrSet, 
                                                     UUID[] uuids)
  // search device for a service with the desired attribute and uuid values
  {
    System.out.println("Searching device: " + getDeviceName(dev));
    try {
      int trans = agent.searchServices(attrSet, uuids, dev, this); // non-blocking
      waitForSearchEnd(trans); 
    }
    catch (BluetoothStateException e) {
      System.out.println(e);
    }
  }  // end of searchForService()



  private void waitForSearchEnd(int trans)
  // wait for the current service search to finish
  {
    System.out.println("Waiting for trans ID " + trans + "...");
    searchDone = false;
    while (!searchDone) {
      synchronized (this) {
        try {
          this.wait();
        }
        catch (Exception e) {}
      }
    }
    System.out.println("Waiting finished");
  }  // end of waitForSearchEnd()


  /* servicesDiscovered() and serviceSearchCompleted() are called 
     automatically during a services search initiated by a
     DiscoveryAgent.searchServices() call. We call it from
     searchForService().
  */

  public void servicesDiscovered(int transID, ServiceRecord[] servRecords)
  /* Called when matching services are found on a device. 
     The service record is only stored if its name matches the one
     being searched for (srchServiceName).

     The service record is stored with the device name in the serviceTable
     hashtable, using the device name as the key, the service record as the
     value.
  */
  {
    for (int i=0; i < servRecords.length; i++) {
      if (servRecords[i] != null) {
        // get the service record's name
        DataElement servNameElem = servRecords[i].getAttributeValue(0x0100);
        String servName = (String)servNameElem.getValue();
        System.out.println("Name of Discovered Service: " + servName);

        if (servName.equals(srchServiceName)) {  // check the name
          RemoteDevice dev = servRecords[i].getHostDevice();
          serviceTable.put( getDeviceName(dev), servRecords[i]); // add to table
        }
      }
    }
  } // end of servicesDiscovered()



  public void serviceSearchCompleted(int transID, int respCode)
  // Called when the service search has finished
  {
    showResponseCode(transID, respCode);

    /* Wake up waitForSearchEnd() for this search, allowing the next
       services search to commence in searchForServices(). */
    searchDone = true;
    synchronized (this) {  
      this.notifyAll();  // wake up
    }
  } // end of serviceSearchCompleted()



  private void showResponseCode(int transID, int respCode)
  {
    System.out.print("Trans ID " + transID + ". ");

    if(respCode == SERVICE_SEARCH_ERROR)
      System.out.println("Service Search Error");
    else if(respCode == SERVICE_SEARCH_COMPLETED)
      System.out.println("Service Search Completed");
    else if(respCode == SERVICE_SEARCH_TERMINATED)
      System.out.println("Service Search Terminated");
    else if(respCode == SERVICE_SEARCH_NO_RECORDS)
      System.out.println("Service Search: No Records found");
    else if(respCode == SERVICE_SEARCH_DEVICE_NOT_REACHABLE)
      System.out.println("Service Search: Device Not Reachable");   
    else 
      System.out.println("Unknown Service Search Status: " + respCode); 
  }  // end of showResponseCode()


} // end of ServiceFinder class

