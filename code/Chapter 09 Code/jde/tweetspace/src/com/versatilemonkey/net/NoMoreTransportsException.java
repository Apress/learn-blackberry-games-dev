package com.versatilemonkey.net;

/**
 * This exception indicates that all of the chosen transport
 * types have been tried, and none worked.
 */
public class NoMoreTransportsException extends Exception {
    
    NoMoreTransportsException() {
      super();
    }
    
    NoMoreTransportsException(String message) {
      super(message);
    }
}
