package net.frogparrot.net;

/**
 * This is a small interface to provide a callback method 
 * for sending and receiving SMS messages.
 *
 * @author Carol Hamer
 */
public interface SmsDataListener {

  /**
   * Transfer the message data to the message listener.
   */
  public void message(byte[] payload, String phoneNumber);

  /**
   * Notify the message listener that this message launched
   * the application (and transfer the data).
   */
  public void initialMessage(byte[] payload, String phoneNumber);

  /**
   * Notify the message listener that the application was not
   * launched by receiving a message.
   */
  public void noInitialMessage();
  
  /**
   * This notifies the listener that a message send request has completed.
   */
  public void doneSending();

}


