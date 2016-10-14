
// IDScreen.java
// Andrew Davison, November 2009, ad@fivedots.coe.psu.ac.th

/* A screen for reading in a player's ID.
   It consists of an image header (from TITLE_FNM), an editfield,
   and "Ok" and "Cancel" buttons.

   If the user presses "Ok", then the ImageScreen is started, and the input ID is
   passed to it.
*/


import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.decor.*;

import java.util.*;


public final class IDScreen extends MainScreen
                            implements FieldChangeListener
{
  private static final String IMAGES_DIR = "images/";
  private static final String TITLE_FNM = "IDTitleBar.png";

  private int screenWidth, screenHeight;

  // GUI elements
  private ButtonField okButton;
  private ButtonField cancelButton;
  private BasicEditField idEditField;


  public IDScreen() 
  {    
    setTitle("ID Entry");
    
    screenWidth = Display.getWidth();
    screenHeight = Display.getHeight();

    FlowFieldManager ffm = new FlowFieldManager();

    // build the GUI
    ffm.add( new BitmapField(Bitmap.getBitmapResource(IMAGES_DIR+TITLE_FNM)) );   // image header
    ffm.add( new SeparatorField(SeparatorField.LINE_HORIZONTAL));

    idEditField = 
        new BasicEditField("  ID: ", "", 7, BasicEditField.NO_NEWLINE);     // input field
    ffm.add(idEditField);
    
    ffm.add(new SeparatorField(SeparatorField.LINE_HORIZONTAL));
    
    okButton = new ButtonField("Ok");    // ok button
    okButton.setChangeListener(this);
    ffm.add(okButton);

    cancelButton = new ButtonField("Cancel");    // cancel button
    cancelButton.setChangeListener(this);
    ffm.add(cancelButton);
    
    add(ffm);
    
    // set the background now, so all of the screen's background is updated
    Manager man = this.getMainManager();
    man.setBackground( BackgroundFactory.createSolidBackground(0x00ffff99));   // pale yellow
  }  // end of IDScreen()
  
  

  public void fieldChanged(Field field, int context)
  // deal with the user clicking the ok or cancel buton
  {
    if (field == okButton) {    // start the image screen, passing it the user's ID
      String id = idEditField.getText().trim();
      UiApplication.getUiApplication().pushScreen(
            new ImageScreen(screenWidth, screenHeight, id));
    }
    else if (field == cancelButton) 
      System.exit(0);
  }  // end of fieldChanged()



}  // end of IDScreen class

