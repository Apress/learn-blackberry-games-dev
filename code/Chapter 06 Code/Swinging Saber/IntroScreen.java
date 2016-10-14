
// IntroScreen.java
// Andrew Davison, September 2009, ad@fivedots.coe.psu.ac.th

/* An introductory screen for the game using a non-scrollable black
   background field manager. In the forground is an image and some
   text explaining the game. The text uses various fonts and colours
   in an ActiveRichTextField instance.
   
   The game can be started by:
      * selecting the "New Game" menu item
      * touching the screen
      * clicking with the trackball
      * typing <space> or <enter>
*/


import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;
// import net.rim.device.api.ui.decor.*;

import java.util.*;


public final class IntroScreen extends MainScreen
{
  private static final String SPLASH_FNM = "images/title.png";
  
  private BlackVFMan manager = new BlackVFMan();  // non-scrollable, black background
  private SaberScreen gameScreen;   // reference to game-playing screen


  public IntroScreen(SaberScreen gScreen) 
  {    
    super.add(manager);
    
    gameScreen = gScreen;
    setMenu();
    positionSplash(SPLASH_FNM);
    positionInfo();
  }  // end of IntroScreen()
  
  
  
  private void setMenu()
  // set up the menu for starting the game
  {
    MenuItem startGame = new MenuItem("New Game", 10, 10) {
      public void run()  
      { startGame(); }
    };
    addMenuItem(startGame);
  }  // end of setMenu()
    
  
  private void startGame()
  { UiApplication.getUiApplication().pushScreen(gameScreen);
    gameScreen.startGame();
  }


  private void positionSplash(String fnm)
  // place an image near the top of the screen, centered along the x-axis
  {
    BitmapField splash = new BitmapField(Bitmap.getBitmapResource(fnm));
    
    HorizontalFieldManager hfm = new HorizontalFieldManager();      
    hfm.add(splash);
        
    // calculate empty spaces
    int leftEmptySpace = (Display.getWidth() - hfm.getPreferredWidth())/2;
    int topEmptySpace = (Display.getHeight() - hfm.getPreferredHeight())/8;

    // make sure the picture fits on the screen
    if ((topEmptySpace >= 0) && (leftEmptySpace >= 0))
      hfm.setMargin(topEmptySpace, 0, 0, leftEmptySpace);
        
    add(hfm);  
  }  // end of positionSplash()
      


  private void positionInfo()
  // display text using a variety of fonts and colours
  {
    String info = "\nSwing your Light Saber\n" +
                  "to destroy laser blasts.\n" +
                  "Or use the arrow keys\n" +
                  "or click on the screen.\n" +
                  "\nMay the Force be with You...\n";

    // positions where the font changes: changes occur three time
    int orOffset = info.indexOf("Or use");
    int mayOffset = info.indexOf("May the");

    // list of all the change positions
    int offsets[] = new int[]{0, orOffset, mayOffset, info.length()};
    
    // load the casual font (an italic,font), or use the default font
    Font casFont;
    try {
      FontFamily ff = FontFamily.forName("BBCasual"); 
        // list at http://docs.blackberry.com/en/developers/deliverables/6625/Fonts_2_0_514368_11.jsp
      casFont = ff.getFont(FontFamily.SCALABLE_FONT, 24);
    }
    catch (ClassNotFoundException ex) {
      casFont = Font.getDefault();
    }
    
    // list of font changes
    Font[] fonts = new Font[]{Font.getDefault().derive(Font.BOLD), 
                             Font.getDefault(), 
                             casFont.derive(Font.BOLD) };
                           
    // lists of the background and foreground colors of the text            
    int bg[] = new int[]{Color.BLACK, Color.BLACK, Color.BLACK};  //always black
    int fg[] = new int[]{Color.YELLOW, Color.WHITE, Color.LIGHTBLUE};
    byte attributes[] = new byte[]{0, 1, 2};

    // add the user's text, varying the font and color,
    add( new ActiveRichTextField(info, offsets, attributes, fonts, fg, bg, 
                                 RichTextField.TEXT_ALIGN_HCENTER));
  }  // end of positionInfo()


  public void add( Field field ) 
  {  manager.add( field );  }


  public boolean onClose()
  { System.exit(0);
    return true;
  }


  // ----------------------------- user input ---------------------------------
  // handles touch, trackball click, and keypresses

  protected boolean touchEvent(TouchEvent message)
  {
    if (message.getEvent() == TouchEvent.CLICK) {
      startGame();
      return true;
    }    
    return super.touchEvent(message);
  }  // end of touchEvent()
  

  protected boolean navigationClick(int status, int time)  
  { startGame();
    return true;
  }


  protected boolean keyChar(char key, int status, int time)
  {
    if (key == Characters.ENTER || key == Characters.SPACE) {
      startGame();
      return true;
    }
    return false;
  }  // end of keyChar()


}  // end of IntroScreen class



// --------------------------------------------------------------

class BlackVFMan extends VerticalFieldManager 
// a non-scrollable vertical field manager with a completely black background
{
    
  public BlackVFMan() 
  {  super(Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR );  }
    
        
  protected void paintBackground(Graphics g)
  {
    g.setBackgroundColor(Color.BLACK);
    // Clears the entire graphic area to the current background
    g.clear();
  }

  protected void sublayout(int width, int height) 
  { super.sublayout(width, height);
    setExtent(width, height);
  }

}  // end of BlackVFMan class
