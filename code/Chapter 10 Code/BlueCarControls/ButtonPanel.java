
// ButtonPanel.java
// Andrew Davison, August 2009, ad@fivedots.coe.psu.ac.th

/* A panel that displays either an 'active' image when pressed, or
   an 'inactive' image the rest of the time.

   While the button is being pressed, it plays a sound clip.

   Both a button press and release trigger calls to dirStatus() in 
   the top-level controller, which sends messages to the Bluetooth server.
*/


import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


public class ButtonPanel extends JPanel implements MouseListener
{
  private static final String IM_DIR = "images/";

  private BlueCarControls top;
  private ClipsPlayer player;
  private String buttonName;   // name of the button and its sound clip

  private boolean isPressed = false;
  private Image activeIm, inActiveIm;


  public ButtonPanel(BlueCarControls cc, ClipsPlayer p, String nm)
  {
    top = cc;
    player = p;
    buttonName = nm;

    setBackground(Color.white);
    activeIm = new ImageIcon(IM_DIR + buttonName + "On.png").getImage();
    inActiveIm = new ImageIcon(IM_DIR + buttonName + "Off.png").getImage();
    player.load(buttonName);

    addMouseListener(this);
  } // end of ButtonPanel() constructor


  public Dimension getPreferredSize()
  // Say how big we would like this panel to be
  {   return new Dimension( inActiveIm.getWidth(null), 
                            inActiveIm.getHeight(null)); }


  public void paintComponent(Graphics g)
  // repaint the panel 
  { 
    super.paintComponent(g);   // repaint standard stuff first
    if (isPressed)
      g.drawImage(activeIm, 0, 0, null);   // show active image
    else
      g.drawImage(inActiveIm, 0, 0, null);   // show inactive image
    top.dirStatus(buttonName, isPressed);    // tell the top-level
  }  // end of paintComponent()


  // ------------ methods for MouseListener ----------------


  public void mousePressed( MouseEvent e)
  { isPressed = true;
    player.loop(buttonName);
    repaint();
  }  // end of mousePressed()


  public void mouseReleased( MouseEvent e)
  { isPressed = false;
    player.stop(buttonName);
    repaint();
  }  // end of mouseReleased()


  public void mouseClicked(MouseEvent e) {}  // not needed
  public void mouseEntered(MouseEvent e) {}  // not needed 
  public void mouseExited(MouseEvent e) {}   // not needed 

} // end of ButtonPanel class
