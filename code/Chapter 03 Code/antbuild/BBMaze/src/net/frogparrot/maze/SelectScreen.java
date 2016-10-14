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
  private GaugeField myWidthGauge;

  /**
   * The gague that displays the number of columns of the maze.
   */
  private GaugeField myColumnsGauge;

  //----------------------------------------------------------------
  //  initialization

  /**
   * Create the gagues and place them on the screen.
   */
  SelectScreen() {
    MazeGame game = MazeGame.getInstance();

    LabelField title = new LabelField(MazeScreen.getLabel(MazeScreen.MAZE_SELECTSIZE),
        LabelField.HCENTER );                               
    setTitle(title);
    // create the gauge that allows the user to modify the width of the columns
    myWidthGauge = new GaugeField(MazeScreen.getLabel(MazeScreen.MAZE_COLWIDTH), 
                             game.getMinColWidth(), 
                             game.getMaxColWidth(), 
                             game.getColWidth(),
                             Field.FOCUSABLE | Field.EDITABLE | Field.HIGHLIGHT_SELECT){
      protected void fieldChangeNotify(int context) {
        int val = getValue();
        int numCols = MazeGame.getInstance().setColWidth(val);
        myColumnsGauge.setValue(numCols);
      }
    };

    // Since the number of columns depends on the cloumn width, the user 
    // isn't allowed to modify the column number value:
    myColumnsGauge = new GaugeField(MazeScreen.getLabel(MazeScreen.MAZE_NUMCOLS), 
                               game.getMinNumCols(),  
                               game.getMaxNumCols(), 
                               game.getNumCols(),
                               Field.NON_FOCUSABLE);

    // Let the platform decide how to place the gauges on the screen:
    FlowFieldManager ffm = new FlowFieldManager(Field.FIELD_HCENTER);
    ffm.add(myWidthGauge);
    ffm.add(myColumnsGauge);
    add(ffm);
  }

}
