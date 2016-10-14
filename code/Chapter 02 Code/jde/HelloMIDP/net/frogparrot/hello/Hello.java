package net.frogparrot.hello;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the lifecycle and event-handling class of the 
 * "Hello World" MIDlet.
 * 
 * @author Carol Hamer
 */
public class Hello extends MIDlet implements CommandListener {

  /**
   * The canvas is the region of the screen that has been allotted 
   * to the game.
   */
  HelloCanvas myCanvas;

  /**
   * On BlackBerry, the Command objects appear as menu items.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  /**
   * On BlackBerry, the Command objects appear as menu items.
   */
  private Command myToggleCommand = new Command("Toggle Msg", Command.SCREEN, 1);

  /**
   * Initialize the canvas and the commands.
   */
  public Hello() {
    myCanvas = new HelloCanvas();
    myCanvas.addCommand(myExitCommand);
    myCanvas.addCommand(myToggleCommand);
    // we set one command listener to listen to all 
    // of the commands on the canvas:
    myCanvas.setCommandListener(this);
  }

  //----------------------------------------------------------------
  //  implementation of MIDlet

  /**
   * The AMS calls this method to start the application.
   */
  public void startApp() throws MIDletStateChangeException {
    // display my canvas on the screen:
    Display.getDisplay(this).setCurrent(myCanvas);
    myCanvas.repaint();
  }
  
  /**
   * If the MIDlet was using resources, it should release 
   * them in this method.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
  }

  /**
   * The AMS calls this method to notify the MIDlet to enter a paused 
   * state.  The MIDlet should use this opportunity to release 
   * shared resources.
   */
  public void pauseApp() {
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * The AMS calls this method to notify the CommandListener of user 
   * command input (either reset or exit).
   */
  public void commandAction(Command c, Displayable s) {
    if(c == myToggleCommand) {
      myCanvas.toggleHello();
    } else if(c == myExitCommand) {
      try {
        // The MIDlet calls these two methods to exit:
        destroyApp(false);
        notifyDestroyed();
      } catch (MIDletStateChangeException ex) {
      }
    }
  }
  
}


