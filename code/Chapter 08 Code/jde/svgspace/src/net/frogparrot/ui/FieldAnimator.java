package net.frogparrot.ui;

import net.rim.device.api.ui.Field;

/**
 * This is an interface to modularize the treatment of Field
 * objects that can be animated, such as a game canvas or a 
 * Scalable Vector Graphics movie.
 * @author Carol Hamer
 */
public interface FieldAnimator {
    
  /**
   * Gets the underlying Field object.
   * @return The Field that is being animated.
   */
   public Field getField();
   
   /**
    * Advance the animation by a single frame, if possible.
    * @return Whether an update actually took place.
    */
   public boolean frameAdvance();   
   
   /**
    * Start the animation.
    */
   public void play();
   
   /**
    * Pause the animation.
    */
   public void pause();
   
   /**
    * Stop the animation.
    */
   public void stop();
   
   /**
    * Set the amount of time between frames.
    * @param timeIncrement The time between 
    *        animation frames, in milliseconds.
    */
   public void setTimeIncrementMillis(long timeIncrement);
   
   /**
    * Pass user input to the animated field.
    * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
    */
   public boolean keyChar(char key, int status, int time);
   
   /**
    * Pass user input to the animated field.
    * @see net.rim.device.api.ui.Screen#navigationMovement(int, int, int, int)
    */
   public boolean navigationMovement(int dx, int dy, int status, int time);

}

