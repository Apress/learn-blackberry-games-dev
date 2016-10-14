
// GPSLocator.java
// Andrew Davison, November 2009, ad@fivedots.ce.psu.ac.th

/* Define a criteria for GPS data and create a provider and listener.
   The LocUpdater thread can access the current latitude and longitude
   by calling GPSLocator.getCoord()


*/

import net.rim.device.api.gps.GPSInfo;
import javax.microedition.location.*;


public class GPSLocator implements LocationListener
{
  private static final int DIST_ACCURACY = 10;  // meters
  private static final int LOC_INTERVAL = 10;  // seconds between location requests

  private LocationProvider locProvider = null;

  private double lat, lon;
  private boolean hasCoord = false;


  public GPSLocator()
  {
    // specify a level of long/lat accuracy, with no cost (autonomous mode)
    Criteria cr = new Criteria();
    cr.setHorizontalAccuracy(DIST_ACCURACY);  // longitude
    cr.setVerticalAccuracy(DIST_ACCURACY);    // latutude
    cr.setCostAllowed(false);

    // start location provider and set up its listener
    try {
      locProvider = LocationProvider.getInstance(cr);
      if (locProvider != null) {
        locProvider.setLocationListener(this, LOC_INTERVAL, -1, -1);  // interval, timeout, maxAge
      }
      else
        Utils.showMessage("GPS Error", "GPS not supported");
    }
    catch (LocationException e) {
      Utils.showMessage("GPS Error", "Location Exception");
    }
  }  // end of GPSLocator()



  public void stop()
  { 
    if (locProvider != null) {
      locProvider.setLocationListener(null, 0, 0, 0);   // cancel earlier registration
      locProvider.reset();
      locProvider = null;
    }
  }  // end of stop()



  public void locationUpdated(LocationProvider lp, Location location)
  // listener for the location provider
  {
    if (location == null) {
      Utils.showMessage("Location Error", "Location is null");
      return;
    }

    if (!location.isValid()) {
      String errMsg = getErrorMsg( GPSInfo.getLastGPSError() );
      Utils.showMessage("Location Invalid", errMsg);
      return;
    }


    QualifiedCoordinates coord = location.getQualifiedCoordinates();
    if ((coord.getHorizontalAccuracy() > DIST_ACCURACY) ||   // too inaccurate to store
        (coord.getVerticalAccuracy() > DIST_ACCURACY))
      return;

    lat = coord.getLatitude();
    lon = coord.getLongitude();
    hasCoord = true;
  }  // end of locationUpdated()



  public Coordinates getCoord()
  // called by the LocUpdater thread
  {
    if (!hasCoord)
      return null;
    else
      return new Coordinates(lat, lon, 0);   // no altuitude or accuracy values
  }  // end of getCoord()



  public void providerStateChanged(LocationProvider lp, int state)
  // access provider state as a string
  {
    switch (state) {
    case LocationProvider.AVAILABLE:
      Utils.showMessage("Location Provider", "Available");
      break;

    case LocationProvider.TEMPORARILY_UNAVAILABLE:
      Utils.showMessage("Location Provider", "Temporarily unavailable");
      break;

    case LocationProvider.OUT_OF_SERVICE:
      Utils.showMessage("Location Provider", "Out of service");
      break;

    default:
      return;
    }
  }  // end of providerStateChanged()



  public String getErrorMsg(int err)
  // return a readable error message for a given gps error code
  {
    switch (err) {
    case (GPSInfo.GPS_ERROR_ALMANAC_OUTDATED):
      return "Almanac outdated";

    case (GPSInfo.GPS_ERROR_AUTHENTICATION_FAILURE):
      return "Network authentication failed";

    case (GPSInfo.GPS_ERROR_CHIPSET_DEAD):
      return "GPS chip is dead";

    case (GPSInfo.GPS_ERROR_DEGRADED_FIX_IN_ALLOTTED_TIME):
      return "Degraded location in allocated time";

    case (GPSInfo.GPS_ERROR_GPS_LOCKED):
      return "GPS service locked";

    case (GPSInfo.GPS_ERROR_INVALID_NETWORK_CREDENTIAL):
      return "Invalid network credential";

    case (GPSInfo.GPS_ERROR_INVALID_REQUEST):
      return "Request is invalid";

    case (GPSInfo.GPS_ERROR_LOW_BATTERY):
      return "Low battery";

    case (GPSInfo.GPS_ERROR_NETWORK_CONNECTION_FAILURE):
      return "Unable to connect to the network";

    case (GPSInfo.GPS_ERROR_NO_FIX_IN_ALLOTTED_TIME):
      return "No location found in alloted time";

    case (GPSInfo.GPS_ERROR_NO_SATELLITE_IN_VIEW):
      return "No Satellite in view";

    case (GPSInfo.GPS_ERROR_NONE):
      return "No GPS Error";

    case (GPSInfo.GPS_ERROR_PRIVACY_ACCESS_DENIED):
      return "Privacy setting means no location";

    case (GPSInfo.GPS_ERROR_SERVICE_UNAVAILABLE):
      return "GPS service is unavailable";

    case (GPSInfo.GPS_ERROR_TIMEOUT_DEGRADED_FIX_NO_ASSIST_DATA):
      return "Degraded location; poor accuracy";

    case (GPSInfo.GPS_ERROR_TIMEOUT_NO_FIX_NO_ASSIST_DATA):
      return "No location found in alloted time";

    default:
      return "Unknown error";
    }
  }  // end of getErrorMsg()

}  // end of GPSLocator class
