
// HelpScreen.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, December 2009

/* A simple help popup screen. Called via the "Help" menu item in
   BoxTrixScreen. It should be extended to explain the three modes.
*/


import net.rim.device.api.ui.*; 
import net.rim.device.api.ui.component.*; 
import net.rim.device.api.ui.container.*; 
 

public class HelpScreen extends PopupScreen implements FieldChangeListener
{ 

  public HelpScreen()
  { 
    super(new VerticalFieldManager(), Field.FOCUSABLE); 
 
    add(new LabelField("Help", Field.FIELD_HCENTER));
    add(new SeparatorField());

    VerticalFieldManager vfm = new VerticalFieldManager(VERTICAL_SCROLL);
    vfm.add( makeTF("Three modes + keys") );
    vfm.add( makeTF("\tleft: \t\ts (or d)") );
    vfm.add( makeTF("\tright: \tf (or j)") );
    vfm.add( makeTF("\tfwd: \t\te (or t)") );
    vfm.add( makeTF("\tback: \tx (or b)") );
    add(vfm);

    ButtonField okButton = new ButtonField("Ok", ButtonField.CONSUME_CLICK | Field.FIELD_HCENTER); 
    okButton.setChangeListener(this); 
    add(okButton); 
  } 
 
 
  public void fieldChanged(Field field, int context)
  {  close(); } 


  private TextField makeTF(String contents) 
  {
    TextField tf = new TextField(FIELD_LEFT|READONLY);
    tf.setText(contents);
    return tf;
  }  // end of makeTF()


} // end of HelpScreen class
