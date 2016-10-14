package net.frogparrot.ui;

/**
 * This is a very simple runnable to pass to a Timer
 * or to Application.invokeLater() in order to make the
 * call to advance the animation by one frame.
 * @author Carol Hamer
 */
public class FrameRunner implements Runnable {
    
  FieldAnimator myFieldAnimator;
    
  public FrameRunner(FieldAnimator fa) { 
    myFieldAnimator = fa;
  }
    
  public void run() {
    myFieldAnimator.frameAdvance();
  }
} 
