
// MapCoords.java
// Andrew Davison, November 2009, ad@fivedots.coe.psu.ac.th

/* A converter for changing latitude and longitude player positions
   into (x,y) coordinates on the map image. The translation uses Google 
   static maps info about the (lat,long) center of the image and
   its zoom factor, and Mercator functions since Google maps use a
   Mercator projection.
*/

import java.io.*;
import java.net.*;


public class MapCoords
{
  private static final double LAT_MAX = 85.05113;   // max latitude where Mercator works
      // see http://en.wikipedia.org/wiki/Mercator_projection

  // Google static map data
  private double lonCenter, latCenter;   // (lat,long) of image center
  private int zoom;

  private int imWidth, imHeight;

  private double lonConvert, latConvert;



  public MapCoords(String latStr, String lonStr, String zoomStr)
  {
    try {
      latCenter = Double.parseDouble(latStr);
    }
    catch (Exception ex){
      latCenter = 0;
    }

    try {
      lonCenter = Double.parseDouble(lonStr);
    }
    catch (Exception ex){
      lonCenter = 0;
    }

    try {
      zoom = Integer.parseInt(zoomStr);
    }
    catch (Exception ex){
      zoom = 0;
    }

    imWidth = 0; imHeight = 0;

    // calculate the conversion factors for the latitude and longitude
    lonConvert = (256.0 * Math.pow(2, zoom)) / 360.0;
    latConvert = (256.0 / (2*invGud(LAT_MAX))) * Math.pow(2, zoom);
  }  // end of MapCoords()



  public void setImageSize(int w, int h)
  // store the image dimensions
  {
    imWidth = w;
    imHeight = h;
  }  // end of setImageSize()



  // ------------------ latitude/longitude conversion -----------------------
  // See LatCalc.java for tests

  public int lon2x(double lon)
  // convert a longitude to a x-coordinate pixel in the image 
  {
    if ((lon < -180) || (lon > 180))
      return -1;
    if (imWidth <= 0)
      return -1;
    return (int) (imWidth/2 - (lonCenter - lon)*lonConvert);
  }  // end of lon2x()



  public int lat2y(double lat)
  // convert a latitude to a y-coordinate pixel in the image 
  {
    if ((lat < -LAT_MAX) || (lat > LAT_MAX))
      return -1;
    if (imHeight <= 0)
     return -1;

    double worldDist = invGud(lat) - invGud(latCenter);
    int yDist = (int)(worldDist * latConvert);
    return ( imHeight/2 - yDist );
  }  // end of lat2y()



  private double invGud(double latitude)
  /* Calculates the y-value for a latitude in degrees 
     An inverse Gudermannian function for a Mercator map. */
  { 
    double sign = Math.signum(latitude);
    double sin = Math.sin( Math.toRadians(latitude) * sign);
    return sign * (Math.log((1.0 + sin)/(1.0 - sin)) / 2.0);
  }


}  // end of MapCoords class

