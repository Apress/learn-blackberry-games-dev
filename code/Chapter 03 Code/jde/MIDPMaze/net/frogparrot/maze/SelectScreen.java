package net.frogparrot.maze;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the screen that allows the user to modify the 
 * width of the maze walls.
 *
 * @author Carol Hamer
 */
public class SelectScreen extends Form 
  implements ItemStateListener, CommandListener  {

  //---------------------------------------------------------
  //   static fields

  /**
   * The single instance of this class.
   */
  private static SelectScreen theInstance;

  //----------------------------------------------------------------
  //  instance fields

  /**
   * The "Done" button to exit this screen and return to the maze.
   */
  private Command myExitCommand = new Command("Done", Command.EXIT, 1);

  /**
   * The gague that modifies the width of the maze walls.
   */
  private Gauge myWidthGauge;

  /**
   * The gague that displays the number of columns of the maze.
   */
  private Gauge myColumnsGauge;

  //-----------------------------------------------------
  //    gets / sets

  public static SelectScreen getInstance() {
    if(theInstance == null) {
      theInstance = new SelectScreen();
    }
    return theInstance;
  }

  static void clearInstance() {
    theInstance = null;
  }

  //----------------------------------------------------------------
  //  initialization

  /**
   * Create the gagues and place them on the screen.
   */
  public SelectScreen() {
    super("Size Preferences");
    addCommand(myExitCommand);
    setCommandListener(this);
    setItemStateListener(this);
    //myCanvas = canvas;
    MazeGame game = MazeGame.getInstance();
    myWidthGauge = new Gauge("Column Width", true, 
                             game.getMaxColWidth(), 
                             game.getColWidth());
    myColumnsGauge = new Gauge("Number of Columns", false,  
                               game.getMaxNumCols(), 
                               game.getNumCols());
    myWidthGauge.setLayout(Item.LAYOUT_CENTER);
    myColumnsGauge.setLayout(Item.LAYOUT_CENTER);
    append(myWidthGauge);
    append(myColumnsGauge);
  }

  //----------------------------------------------------------------
  //  implementation of ItemStateListener

  /**
   * Respond to the user changing the width.
   */
  public void itemStateChanged(Item item) {
    if(item == myWidthGauge) {
      MazeGame game = MazeGame.getInstance();
      int val = myWidthGauge.getValue();
      if(val < game.getMinColWidth()) {
        myWidthGauge.setValue(game.getMinColWidth());
      } else {
        int numCols = game.setColWidth(val);
        myColumnsGauge.setValue(numCols);
      }
    }
  }

  //----------------------------------------------------------------
  //  implementation of CommandListener

  /*
   * Respond to the exit command.
   */
  public void commandAction(Command c, Displayable s) {
    if(c == myExitCommand) {
      PrefsStorage.setSquareSize(myWidthGauge.getValue());
      MazeScreen.getInstance().paintMaze();
    }
  }
  
}
