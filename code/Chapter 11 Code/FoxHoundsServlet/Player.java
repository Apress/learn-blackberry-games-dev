
// Player.java
// Andrew Davison, Nov. 2009, ad@fivedots.coe.psu.ac.th

/* Store information about a single player:
      the ID, the current (x,y) location on the map, and if alive;
      a hound ID starts with 'H', a fox with 'F'
*/

public class Player
{
  private String id;   // a hound ID starts with 'H', a fox with 'F'
  private int x, y;
  private boolean isAlive = true;
  private long locRequestTime;    // seconds
     // time when locations were last requested



  public Player(String uid)
  { id = uid;
    x = -1;     // location unknown
    y = -1;
    locRequestTime = -1;
  }


  public String getID()
  {  return id;  }


  public boolean hasUID(String uid)
  { return id.equals(uid);  }


  public void storeCoord(int xCoord, int yCoord)
  {  x = xCoord;
     y = yCoord;
  }


  public boolean isHound()
  {  return (id.charAt(0) == 'H');  }

  public boolean isFox()
  {  return (id.charAt(0) == 'F');  }


  public boolean isAlive()
  {  return isAlive;  }


  public void setAlive(boolean b)
  {  isAlive = b;  }

  
  public long getLocRequestTime()
  {  return locRequestTime;  }


  public void setLocRequestTime(long t)
  {  locRequestTime = t;  }


  public String toString()
  // only returns first letter (F or H) of ID
  {  return (id.charAt(0) + " " + x + " " + y + " " + isAlive);  } 

}  // end of Player class
