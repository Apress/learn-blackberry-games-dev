//#preprocess
package net.frogparrot.hello;

import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.i18n.ResourceBundle;

/**
 * The screen to draw the message on.
 */
public class HelloWorldScreen extends MainScreen implements HelloBBResResource {

  /**
   * This bundle contains all of the texts, translated by locale.
   */
  ResourceBundle myLabels = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

  /**
   * Write "Hello World!" on the screen.
   */
  HelloWorldScreen() {
    super();
    add(new RichTextField(myLabels.getString(HELLOBB_SAYHELLO), 
        RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
    //#ifdef RIM_4.1.0
    add(new RichTextField("version 4.1.0",
        RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
    //#else
    add(new RichTextField("not version 4.1.0",
        RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
    //#endif
    //#ifdef RIM_4.6.1
    add(new RichTextField("version 4.6.1",
        RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
    //#else
    add(new RichTextField("not version 4.6.1",
        RichTextField.NON_FOCUSABLE | RichTextField.READONLY));
    //#endif
    // Let's try adding an image!
    Bitmap imgBitmap = Bitmap.getBitmapResource("HelloPlus_icon.png");
    add(new BitmapField(imgBitmap));
  }

  /**
   * This is called by the BlackBerry platform when
   * the screen is popped off the screen stack.
   */
  public boolean onClose() {
    // end the program.
    System.exit(0);
    return true;
  }

}

