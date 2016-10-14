package net.frogparrot.maze;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the main class of the maze game.
 *
 * @author Carol Hamer
 */
public class Maze extends MIDlet implements CommandListener {

  //----------------------------------------------------------------
  //  command fields

  /**
   * The command to exit the game.  On BlackBerry, it's placed in a menu.
   */
  private Command myExitCommand = new Command("Exit", Command.EXIT, 99);

  /**
   * The command to create a new maze.  On BlackBerry, it's placed in a menu.
   */
  private Command myNewCommand = new Command("New Maze", Command.SCREEN, 1);

  /**
   * The command to go to the screen that allows the user 
   * to alter the size parameters.  On BlackBerry, it's placed in a menu.
   */
  private Command myPrefsCommand 
    = new Command("Size Preferences", Command.SCREEN, 1);

  //----------------------------------------------------------------
  //  implementation of MIDlet

  /**
   * Start the application.
   */
  public void startApp() throws MIDletStateChangeException {
    try {
      MazeScreen screen = new MazeScreen(Display.getDisplay(this));
      screen.addCommand(myExitCommand);
      screen.addCommand(myNewCommand);
      screen.addCommand(myPrefsCommand);
      screen.setCommandListener(this);
      screen.paintMaze();
    } catch(Exception e) {
      System.out.println("startApp caught: " + e);
      e.printStackTrace();
    }
  }
  
  /**
   * Clean up.
   */
  public void destroyApp(boolean unconditional) 
      throws MIDletStateChangeException {
    MazeGame.setInstance(null);
    MazeScreen.clearInstance();
    SelectScreen.clearInstance();
    System.gc();
  }

  /**
   * Does nothing since this program occupies no shared resources 
   * and little memory.
   */
  public void pauseApp() {
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to a command issued on the Canvas.
   * (reset, exit, or change size prefs).
   */
  public void commandAction(Command c, Displayable s) {
    MazeScreen screen = MazeScreen.getInstance();
    if(c == myNewCommand) {
      screen.newMaze();
    } else if(c == myPrefsCommand) {
      Display.getDisplay(this).setCurrent(SelectScreen.getInstance());
    } else if(c == myExitCommand) {
      try {
        destroyApp(false);
        notifyDestroyed();
      } catch (MIDletStateChangeException ex) {
      }
    }
  }
  
}
