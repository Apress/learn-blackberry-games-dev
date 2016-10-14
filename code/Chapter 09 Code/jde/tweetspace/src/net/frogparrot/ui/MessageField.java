//#preprocess
package net.frogparrot.ui;

import net.rim.device.api.ui.component.*;

/**
 * An interface to wrap an EditField that has extra functionality.
 */
public interface MessageField {
  
  /**
   * @return The label (non-user text).
   */
  public String getLabel();
    
  /**
   * Prompt the message field to transmit its message data.
   */
  public void sendMessage();
  
  /**
   * Warn the message field that the user has updated the login data.
   * @param username the new username of the current user.
   */
  public void loginUpdated(String username);

  /**
   * Pass input information to the field.
   * This is a wrapper method to allow keyChar to be called from the Manager.
   * @see net.rim.device.api.ui.Field#keyChar(char, int, int)
   */
  public boolean keyChar(char key, int status, int time, boolean fromManager);

//#ifdef RIM_4.2.0
  /**
   * Pass input information to the field.
   * This is a wrapper method to allow navigationMovement to be called from the Manager.
   * @see net.rim.device.api.ui.Field#navigationMovement(int, int, int, int)
   */
  public boolean navigationMovement(int dx, int dy, int status, 
      int time, boolean fromManager);
//#endif
    
} 
