package net.frogparrot.maze;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

/**
 * This is the screen that allows the user to modify the 
 * width of the maze walls.
 *
 * @author Carol Hamer
 */
public class SelectScreen extends MainScreen {

  //----------------------------------------------------------------
  //  fields

  /**
   * The gague that modifies the width of the maze walls.
   */
  private WidthGauge myWidthGauge;

  /**
   * The gague that displays the number of columns of the maze.
   */
  private ColumnsGauge myColumnsGauge;

  //----------------------------------------------------------------
  //  initialization

  /**
   * Create the gagues and place them on the screen.
   */
  SelectScreen() {
    MazeGame game = MazeGame.getInstance();

    // Since the number of columns depends on the cloumn width, the user 
    // isn't allowed to modify the column number value:
    myColumnsGauge = new ColumnsGauge(MazeScreen.getLabel(MazeScreen.MAZE_NUMCOLS), 
                               game.getMinNumCols(),  
                               game.getMaxNumCols(), 
                               game.getNumCols(),
                               Field.NON_FOCUSABLE);

    // create the gauge that allows the user to modify the width of the columns
    myWidthGauge = new WidthGauge(myColumnsGauge, 
                             MazeScreen.getLabel(MazeScreen.MAZE_COLWIDTH), 
                             game.getMinColWidthIndex(), 
                             game.getMaxColWidthIndex(), 
                             game.getColWidthIndex(),
                             Field.FOCUSABLE | Field.EDITABLE | Field.HIGHLIGHT_SELECT);

    // Use the custom manager to place the widgets:
    SelectSizeManager ssm = new SelectSizeManager();
    ssm.add(myWidthGauge);
    ssm.add(myColumnsGauge);
    add(ssm);
  }

}
