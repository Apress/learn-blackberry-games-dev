
// IDCreator.java
// Andrew Davison, November 2009, ad@fivedots.coe.psu.ac.th

/* Generate a six-letter ID for the Fox and Hounds game.
   The first letter is "F" or "H" depending on the -f option
   supplied to the call to IDCreator.

   Write the ID into a PNG banner image with a yellow background,
   black border, and the Fox and Hounds logo.

   Usage:
      java IDCreator [ -f ]

   The image is saved to the ID_FNM file.
*/

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


public class IDCreator
{
  private static final int ID_LEN = 5;   
            // length of ID, excluding F or H at the beginning

  private static final int IM_HEIGHT = 300;   // size of resulting image
  private static final int IM_WIDTH = 800;

  private static final String ID_FNM = "foxHoundsID.png";   // where the PNG is saved
  private static final String LOGO_FNM = "fox.png";     // Fox and Hounds logo



  public static void main(String[] args)
  {
    boolean isFox = false;    // default ID is for hound
    if (args.length == 1) {
      if (args[0].toLowerCase().startsWith("-f"))
        isFox = true;
    }

    String idStr = generateID(isFox);
    System.out.println("ID: " + idStr);
    BufferedImage image = drawBanner(idStr);

    try {   // save the image to a file
      ImageIO.write(image, "png", new File(ID_FNM));
      System.out.println("Saved ID to " + ID_FNM);

    } 
    catch (IOException e) 
    {  System.out.println("Could not save ID to " + ID_FNM);  }
  }  // end of main()



  private static String generateID(boolean isFox)
  // generate a six-letter string, starting with "F" or "H"
  {
    if (isFox)
      System.out.println("Generating a Fox ID");
    else
      System.out.println("Generating a Hound ID");

    String lets = "ABCDEGJKLMNPQRSTUVWXYZ0123456789";   // no F, H, I, O
    Random rand = new Random();

    StringBuffer sb = (isFox) ? new StringBuffer("F") : new StringBuffer("H");
    for (int i=0; i < ID_LEN; i++)
      sb.append( lets.charAt( rand.nextInt(lets.length())) );

    return sb.toString();
  }  // end of generateID()



  private static BufferedImage drawBanner(String idStr)
  /* the resulting image contains the ID, a yellow background,
     black border, and the Fox and Hounds logo  */
  {
    BufferedImage image = new BufferedImage(IM_WIDTH, IM_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();

    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);    // smoothly draw the big letters

    g2d.setColor(Color.YELLOW);
    g2d.fillRect(0, 0, IM_WIDTH, IM_HEIGHT);       // yellow background

    g2d.setColor(Color.BLACK);
    g2d.drawRect(0, 0, IM_WIDTH-1, IM_HEIGHT-1);   // black border

    drawLogo(g2d);
    drawID(g2d, idStr);

    return image;
  }  // end of drawBanner()



  private static void drawLogo(Graphics2D g2d)
  {
    try{
      BufferedImage logoIm = ImageIO.read(new File(LOGO_FNM));
      g2d.drawImage(logoIm, (IM_WIDTH-logoIm.getWidth())/2, 0, null);  // horizontally centered 
    }
    catch(IOException e)
    {  System.out.println("Could not load logo from " + LOGO_FNM); }
  }  // end of drawLogo()




  private static void drawID(Graphics2D g2d, String idStr)
  // draw the ID in very large letters
  {
    Font font = new Font("SansSerif", Font.BOLD, 184);  //196
    g2d.setFont(font);

    FontMetrics metrics = g2d.getFontMetrics(font);
    int height = metrics.getHeight();
    int ascent = metrics.getAscent();    // where is font baseline
    int width = metrics.stringWidth(idStr);
   
    if (height > IM_HEIGHT)
      System.out.println("Height of ID is too large; some text lost");
    if (width > IM_WIDTH)
      System.out.println("Width of ID is too large; some text lost");

    g2d.drawString(idStr, (IM_WIDTH- width)/2, (IM_HEIGHT-height)/2 + ascent);  // centered text
  }  // end of drawID()


}  // end of IDCreator class

