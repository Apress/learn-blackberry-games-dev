package net.frogparrot.smscheckers;

import java.util.Vector;

import net.frogparrot.net.*;

/**
 * This class takes care of the game state and logic, 
 * including where all of the pieces are on the board 
 * and where it is okay for them to move to.  
 *
 * @author Carol Hamer
 */
public class CheckersGame implements SmsDataListener {

//--------------------------------------------------------
//  game state fields

  /**
   * Possible game states.
   */
  public static final int NOT_STARTED = 0;
  public static final int LOCALLY_LAUNCHED = 1;
  public static final int SENT_INVITATION = 2;
  public static final int REMOTELY_LAUNCHED = 3;
  public static final int AWAITING_INVITATION = 4;
  public static final int LOCAL_TURN = 5;
  public static final int SENDING_LOCAL_MOVE = 6;
  public static final int REMOTE_TURN = 7;
  public static final int LOCAL_WINS = 8;
  public static final int REMOTE_WINS = 9;

  /**
   * A code to identify an invitation SMS (so that 
   * it won't be confused with some other type of message).
   */
  public static final byte[] INVITATION = { 8, 1, 9, 7 };

 /**
  * The code for the state the game is currently in.
  */
  private int myState = NOT_STARTED;
  
 /**
  * The current message from the local player 
  * to the remote player.
  */
  private String myTaunt;
  
 /**
  * The data array that is sent in the binary SMS message.
  */
  private byte[] myMoveData = new byte[140];
  
 /**
  * How many moves are contained in myMoveData.
  * Usually it will be 1, but it can be more if the player
  * is jumping multiple times.
  */
  private byte myNumberOfMoves = 0;

//--------------------------------------------------------
//  game logic fields

  /**
   * The length of the checkerboard in the x-direction.
   */
  public static final int X_LENGTH = 4;

  /**
   * The length of the checkerboard in the y-direction.
   */
  public static final int Y_LENGTH = 8;

  /**
   * This array represents the black squares of the 
   * checkerboard.  The two dimensions of the array 
   * represent the two dimensions of the checkerboard.
   * The value represents what type of piece is on 
   * the square.
   * 0 = empty
   * 1 = local player's piece
   * 2 = local player's king
   * -1 = remote player's piece
   * -2 = remote player's king
   */
  private byte[][] myGrid;

  /**
   * If the user has currently selected a piece to move, 
   * this is its X grid coordinate. (-1 if none selected)
   */
  private byte mySelectedX = -1;

  /**
   * If the user has currently selected a piece to move, 
   * this is its Y grid coordinate.(-1 if none selected)
   */
  private byte mySelectedY = -1;

  /**
   * If the user has currently selected a possible 
   * destination square for a move, this is its X coordinate..
   * (-1 if none selected)
   */
  private byte myDestinationX = -1;

  /**
   * If the user has currently selected a possible 
   * destination square for a move, this is its Y coordinate..
   * (-1 if none selected)
   */
  private byte myDestinationY = -1;

  /**
   * This Vector contains the coordinates of all of the 
   * squares that the player could currently move to.
   */
  private Vector myPossibleMoves = new Vector(4);

  /**
   * This is true if the player has just jumped and can 
   * jump again.
   */
  private boolean myIsJumping = false;

//-------------------------------------------------------
//  Update the state based on user commands

  /**
   * The user is waiting for an invitation from a remote player.
   */
  public void setWaiting() {
    myState = AWAITING_INVITATION;
  }

  /**
   * Main calls this method when the user selects "OK" on the main 
   * message screen.  Updates the screen and state accordingly.
   */
  public void ok() {
    Main.setMessage("received OK");
    switch(myState) {
      case NOT_STARTED:
      case LOCALLY_LAUNCHED:
      case AWAITING_INVITATION:
      case SENT_INVITATION:
        // ignore the OK command if we're not ready for it
      break;
      case REMOTELY_LAUNCHED:
        // start the game.
        Main.setMessage("setting game board");
        Main.showGameBoard();
        myState = LOCAL_TURN;
      break;
      default:
        Main.showGameBoard();
      break;
    }
  }
  
  /**
   * Main calls this method to handle the case when the local user quits.
   * If the game is not done, it attempts to notify the other player.
   */
  public boolean gameDone() {
    if((myState == LOCAL_TURN) || (myState == REMOTE_TURN)) {
      // the game is not done, so we end it by having the
      // local player lose:
      loseGame();
      return false;
    } else {
      return true;
    }
  }

//----------------------------------------------------------------
//  Update the state based on SMS information
//  (implementation of SmsDataListener)

  /**
   * Inform the user of the incoming invitation.
   * If the game was launched by an incoming SMS message,
   * this method will be called with the message data.
   */
  public void initialMessage(byte[] payload, String phoneNumber) {
    Main.setMessage("received initial message");
    Main.setMessage("my state: " + myState);
    if((payload[0] == INVITATION[0]) &&
       (payload[1] == INVITATION[1]) &&
       (payload[2] == INVITATION[2]) &&
       (payload[3] == INVITATION[3])) {
      String name = PIMRunner.findName(SMSManager.stripPhoneNum(phoneNumber));
      Main.displayMessage(name + " invites you to play checkers!\n");
      //Main.setMessage("invitation: " + payload[0] + "," + payload[1] + "," + payload[2] + "," + payload[3]);
      myState = REMOTELY_LAUNCHED;
    } else {
      // not an invitation to play
      //Main.setMessage("not an invitation: " + payload[0] + "," + payload[1] + "," + payload[2] + "," + payload[3]);
      Main.quit();
    }
    Main.setMessage("my new state: " + myState);
  }

  /**
   * The application was not launched by receiving a message, so 
   * the user launched it. The next step is to call the PIM
   * functionality to allow the user to select an opponent from 
   * the address book.
   */
  public void noInitialMessage() {
    Main.setMessage("no initial message");
    Main.setMessage("my state: " + myState);
    myState = LOCALLY_LAUNCHED;
    Main.setMessage("my new state: " + myState);
    Main.startPim();
  }
  
  /**
   * Receive a standard message from the opponent.
   * Handle the message and update the state accordingly.
   */
  public void message(byte[] payload, String phoneNumber) {
    Main.setMessage("received message");
    Main.setMessage("my state: " + myState);
    // ignore invitation messages unless
    // awaiting an invitation
    if((payload[0] == INVITATION[0]) &&
       (payload[1] == INVITATION[1]) &&
       (payload[2] == INVITATION[2]) &&
       (payload[3] == INVITATION[3])) {
      if(myState == AWAITING_INVITATION) {
        String name = PIMRunner.findName(SMSManager.stripPhoneNum(phoneNumber));
        Main.displayMessage(name + " invites you to play checkers!\n");
        myState = REMOTELY_LAUNCHED;
      }
    } else {
      switch(myState) {
        case SENT_INVITATION:
        case LOCALLY_LAUNCHED:
          Main.setMessage("received first response");
          handleRemoteMove(payload);
        break;
        case REMOTE_TURN:
        case SENDING_LOCAL_MOVE:
          Main.setMessage("remote move!");
          handleRemoteMove(payload);
        break;
        case LOCAL_TURN:
          // during the local turn, the other player
          // is allowed to quit and end the game:
          Main.displayMessage("You Win!");
          myState = LOCAL_WINS;
        break;
        case REMOTE_WINS:
          Main.quit();
        break;
        default:
          Main.setMessage("unexpected remote message during state: " + myState);
        break;
      }
    }
    Main.setMessage("my new state: " + myState);
  }
  
  /**
   * A message sending request has completed.
   * (This is the callback.)
   */
  public void doneSending() {
    switch(myState) {
      case REMOTE_WINS:
        // done informing the remote user
        // so the application can close
        Main.quit();
      break;
      case LOCALLY_LAUNCHED:
        myState = SENT_INVITATION;
      break;
      case SENDING_LOCAL_MOVE:
        myState = REMOTE_TURN;
      break;
      default:
        Main.setMessage("unexpected doneSending in state: " + myState);
      break;
    }
  }

//-------------------------------------------------------
//   internal game state methods

  /**
   * Interpret one move by the remote player.
   */
  private void handleRemoteMove(byte[] payload) {
    Main.setMessage("canvas remote move!");
    byte numMoves = payload[0];
    Main.setMessage("numMoves: " + numMoves);
    int tauntIndex = 1 + numMoves*4;
    Main.setMessage("taunt index: " + tauntIndex);
    byte tauntLength = payload[tauntIndex];
    Main.setMessage("taunt length: " + tauntLength);
    String taunt = new String(payload, tauntIndex + 1, tauntLength);
    Main.setMessage("taunt: " + taunt);
    if(numMoves == 0) {
      Main.displayMessage("You Win!\n" + taunt);
      myState = LOCAL_WINS;
      Main.setMessage("done local win");
    } else {
      for(byte i = 0; i < numMoves; i++) {
        int os = i*4;
        //Main.setMessage("os: " + os);
        //Main.setMessage("payload length: " + payload.length);
        moveOpponent(payload[os+1], payload[os+2], payload[os+3], payload[os+4]);
      }
      Main.setMessage("done opponent turn");
      prepareLocalTurn();
      if(taunt.length() == 0) {
        Main.showGameBoard();
      } else {
        Main.displayMessage(taunt);
      }
      myState = LOCAL_TURN;
    }
  }
  
  /**
   * The game logic of this class calls this method when 
   * the user has selected a move.
   */
  private void setMove(byte sourceX, byte sourceY, byte destinationX, 
                    byte destinationY) {
    Main.setMessage("move-->setting local move");
    int index = (myNumberOfMoves*4) + 1;
    myMoveData[index] = sourceX;
    index++;
    myMoveData[index] = sourceY;
    index++;
    myMoveData[index] = destinationX;
    index++;
    myMoveData[index] = destinationY;
    myNumberOfMoves++;
    myMoveData[0] = (byte)myNumberOfMoves;
    Main.setMessage("move-->done setting");
  }
  
  /**
   * Send the move data to the remote player and 
   * advance the internal game state.
   * The game logic of this class calls this method
   * when the local player's turn is over.
   */
  private void endTurn() {
    Main.setMessage("move-->turn over");
    myState = SENDING_LOCAL_MOVE;
    // add the taunt to the move data:
    String taunt = Main.getTaunt();
    byte[] tauntData = taunt.getBytes();
    int index = myNumberOfMoves*4 + 1;
    if(index + tauntData.length < 139) {
      // copy the message into the payload:
      myMoveData[index] = (byte)(tauntData.length);
      System.arraycopy(tauntData, 0, myMoveData, index+1, tauntData.length);
    } else {
      // if it's too long to fit into the SMS, just skip it:
      myMoveData[index] = (byte)0;
    }
    SMSManager.getInstance().sendMessage(myMoveData);
    myNumberOfMoves = 0;
  }

  /**
   * Stop the game entirely.  Notify the remote player that 
   * the user is exiting the game.
   */
  private void loseGame() {
    myState = REMOTE_WINS;
    Main.setMessage("sending remote win");
    Main.displayMessage("remote player wins!");
    // sending an empty move indicates 
    // that the game is over
    myMoveData[0] = 0;
    myNumberOfMoves = 0;
    String taunt = Main.getTaunt();
    byte[] tauntData = taunt.getBytes();
    if(tauntData.length < 138) {
      // copy the message into the payload:
      myMoveData[1] = (byte)(tauntData.length);
      System.arraycopy(tauntData, 0, myMoveData, 2, tauntData.length);
    } else {
      // if it's too long to fit into the SMS, just skip it:
      myMoveData[1] = (byte)0;
    }
    SMSManager.getInstance().sendMessage(myMoveData);
  }

//-------------------------------------------------------
//   initialization

  /**
   * Constructor puts the pieces in their initial positions:
   */
  CheckersGame() {
    Main.setMessage("in CheckersGame()");
    myGrid = new byte[X_LENGTH][];
    for(byte i = 0; i < myGrid.length; i++) {
      myGrid[i] = new byte[Y_LENGTH];
      for(byte j = 0; j < myGrid[i].length; j++) {
        if(j < 3) {
          // fill the top of the board with remote players
          myGrid[i][j] = -1;
        } else if(j > 4) {
          // fill the bottom of the board with local players
          myGrid[i][j] = 1;
        }
      }
    }
    Main.setMessage("starting checkers game");
    mySelectedX = 0;
    mySelectedY = 5;
    getMoves(mySelectedX, mySelectedY, myPossibleMoves, false);
  }

 //-------------------------------------------------------
 //   game logic accessors
  
  /**
   * get the piece on the given grid square.
   */
  byte getPiece(byte x, byte y) {
    return(myGrid[x][y]);
  }

  /**
   * This is called by CheckersCanvas to determine if 
   * the square is currently selected (as containing 
   * a piece to move or a destination square).
   */
  boolean isSelected(byte x, byte y) {
    boolean retVal = false;
    if((x == mySelectedX) && (y == mySelectedY)) {
      retVal = true;
    } else if((x == myDestinationX) && (y == myDestinationY)) {
      retVal = true;
    }
    return(retVal);
  }

  /**
   * This tells whether or not the keystrokes should currently
   * be taken into account.
   */
  boolean isMyTurn() {
    return(myState == LOCAL_TURN);
  }


//-------------------------------------------------------
//   internal game logic

  /**
   * Once the opponent's move data has been received,
   * this method interprets it.
   * @param ooix = opponent's initial X coordinate
   * @param ooiy = opponent's initial Y coordinate
   * @param oodx = opponent's destination X coordinate
   * @param oody = opponent's destination Y coordinate
   */
  private void moveOpponent(byte ooix, byte ooiy, byte oodx, byte oody) {
    // since both players appear on their own screens 
    // as the red side (bottom of the screen), we need 
    // to invert the opponent's move:
    Main.setMessage("recieved: (" + ooix + "," + ooiy + ") => (" + oodx + "," + oody + ")");
    int oix = X_LENGTH - ooix - 1;
    int odx = X_LENGTH - oodx - 1;
    int oiy = Y_LENGTH - ooiy - 1;
    int ody = Y_LENGTH - oody - 1;
    Main.setMessage("inverted: (" + oix + "," + oiy + ") => (" + odx + "," + ody + ")");
    myGrid[odx][ody] 
      = myGrid[oix][oiy];
    Main.setMessage("source contains: " + myGrid[oix][oiy]);
    Main.setMessage("destination contains: " + myGrid[odx][ody]);
    myGrid[oix][oiy] = 0;
    // deal with an opponent's jump:
    if((oiy - ody > 1) || 
       (ody - oiy > 1)) {
      int jumpedY = (oiy + ody)/2;
      int jumpedX = oix;
      int parity = oiy % 2;
      if((parity > 0) && (odx > oix)) {
        jumpedX++;
      } else if((parity == 0) && (oix > odx)) {
        jumpedX--;
      }
      myGrid[jumpedX][jumpedY] = 0;
    }
    // if the opponent reaches the far side, 
    // make him a king:
    if(ody == Y_LENGTH - 1) {
      myGrid[odx][ody] = -2;
    }
  }

  /**
   * Once the opponent's turn has been interpreted, 
   * this method prepares the data for the local turn.
   */
  private void prepareLocalTurn() {
    // Now begin the local player's turn: 
    // First select the first local piece that can be 
    // moved. (rightPressed will select an appropriate 
    // piece or end the game if the local player has 
    // no possible moves to make)
    mySelectedX = 0;
    mySelectedY = 0;
    myDestinationX = -1;
    myDestinationY = -1;
    rightPressed();
  }

//-------------------------------------------------------
//   handle user input on the game board

  /**
   * If the left button is pressed, this method takes 
   * the correct course of action depending on the situation.
   */
  void leftPressed() {
    // in the first case the user has not yet selected a 
    // piece to move:
    if(myDestinationX == -1) {
      // find the next possible piece (to the left) 
      // that can move:
      selectPrevious();
      // if selectPrevious fails to fill myPossibleMoves, that 
      // means that the local player cannot move, so the game
      // is over:
      if(myPossibleMoves.size() == 0) {
        loseGame();
      }
    } else {
      // if the user has already selected a piece to move, 
      // we give the options of where the piece can move to:
      for(byte i = 0; i < myPossibleMoves.size(); i++) {
        byte[] coordinates = (byte[])myPossibleMoves.elementAt(i);
        if((coordinates[0] == myDestinationX) && 
           (coordinates[1] == myDestinationY)) {
          i++;
          i = (new Integer(i % myPossibleMoves.size())).byteValue();
          coordinates = (byte[])myPossibleMoves.elementAt(i);
          myDestinationX = coordinates[0];
          myDestinationY = coordinates[1];
          break;
        }
      }
    }
  }

  /**
   * if the left button is pressed, this method takes 
   * the correct course of action depending on the situation.
   */
  void rightPressed() {
    // in the first case the user has not yet selected a 
    // piece to move:
    if(myDestinationX == -1) {
      // find the next possible piece that can 
      // move:
      selectNext();
      // if selectNext fails to fill myPossibleMoves, that 
      // means that the local player cannot move, so the game
      // is over:
      if(myPossibleMoves.size() == 0) {
        loseGame();
      }
    } else {
      // if the user has already selected a piece to move, 
      // we give the options of where the piece can move to:
      for(byte i = 0; i < myPossibleMoves.size(); i++) {
        byte[] coordinates = (byte[])myPossibleMoves.elementAt(i);
        if((coordinates[0] == myDestinationX) && 
           (coordinates[1] == myDestinationY)) {
          i++;
          i = (new Integer(i % myPossibleMoves.size())).byteValue();
          coordinates = (byte[])myPossibleMoves.elementAt(i);
          myDestinationX = coordinates[0];
          myDestinationY = coordinates[1];
          break;
        }
      }
    }
  }

  /**
   * If no piece is selected, we select one.  If a piece 
   * is selected, we move it.
   */
  void upPressed() {
    // in the first case the user has not yet selected a 
    // piece to move:
    if(myDestinationX == -1) {
      fixSelection();
    } else {
      // if the source square and destination square 
      // have been chosen, we move the piece:
      move();
    }
  }

  /**
   * If the user decided not to move the selected piece 
   * (and instead wants to select again), this undoes 
   * the selection. This corresponds to pressing the 
   * DOWN key.
   */
  void deselect() {
    // if the player has just completed a jump and 
    // could possibly jump again but decides not to 
    // (i.e. deselects), then the turn ends:
    if(myIsJumping) {
      mySelectedX = -1;
      mySelectedY = -1;
      myDestinationX = -1;
      myDestinationY = -1;
      myIsJumping = false;
      endTurn();
    } else {
      // setting the destination coordinates to -1 
      // is the signal that the the choice of which 
      // piece to move can be modified:
      myDestinationX = -1;
      myDestinationY = -1;
    }
  }

//-------------------------------------------------------
//   internal square selection methods

  /**
   * When the player has decided that the currently selected
   * square contains the piece he really wants to move, this 
   * is called. This method switches to the mode where 
   * the player selects the destination square of the move.
   */
  private void fixSelection() {
    byte[] destination = (byte[])myPossibleMoves.elementAt(0);
    // setting the destination coordinates to valid 
    // coordinates is the signal that the user is done 
    // selecting the piece to move and now is choosing 
    // the destination square:
    myDestinationX = destination[0];
    myDestinationY = destination[1];
  }

  /**
   * This method starts from the currently selected square 
   * and finds the next square that contains a piece that 
   * the player can move.
   */
  private void selectNext() {
    // Test the squares one by one (starting from the 
    // currently selected square) until we find a square 
    // that contains one of the local player's pieces 
    // that can move:
    byte testX = mySelectedX;
    byte testY = mySelectedY;
    while(true) {
      testX++;
      if(testX >= X_LENGTH) {
        testX = 0;
        testY++;
        testY = (new Integer(testY % Y_LENGTH)).byteValue();
      }
      getMoves(testX, testY, myPossibleMoves, false);
      if((myPossibleMoves.size() != 0) || 
           ((testX == mySelectedX) && (testY == mySelectedY))) {
        mySelectedX = testX;
        mySelectedY = testY;
        break;
      }
    }
  }

  /**
   * This method starts from the currently selected square 
   * and finds the next square (to the left) that contains 
   * a piece that the player can move.
   */
  private void selectPrevious() {
    // Test the squares one by one (starting from the 
    // currently selected square) until we find a square 
    // that contains one of the local player's pieces 
    // that can move:
    byte testX = mySelectedX;
    byte testY = mySelectedY;
    while(true) {
      testX--;
      if(testX < 0) {
        testX += X_LENGTH;
        testY--;
        if(testY < 0) {
          testY += Y_LENGTH;
        }
      }
      getMoves(testX, testY, myPossibleMoves, false);
      if((myPossibleMoves.size() != 0) || 
         ((testX == mySelectedX) && (testY == mySelectedY))) {
        mySelectedX = testX;
        mySelectedY = testY;
        break;
      }
    }
  }

  /**
   * Once the user has selected the move to make, this 
   * updates the data accordingly.
   */
  private void move() {
    // the piece that was on the source square is 
    // now on the destination square:
    myGrid[myDestinationX][myDestinationY] 
      = myGrid[mySelectedX][mySelectedY];
    // the source square is emptied:
    myGrid[mySelectedX][mySelectedY] = 0;
    if(myDestinationY == 0) {
      myGrid[myDestinationX][myDestinationY] = 2;
    }
    // Store the move data so that it can be sent
    // to the remote player.
    setMove(mySelectedX, mySelectedY, 
        myDestinationX, myDestinationY);
    // deal with the special rules for jumps::
    if((mySelectedY - myDestinationY > 1) || 
       (myDestinationY - mySelectedY > 1)) {
      int jumpedY = (mySelectedY + myDestinationY)/2;
      int jumpedX = mySelectedX;
      int parity = mySelectedY % 2;
      // the coordinates of the jumped square depend on 
      // what row we're in:
      if((parity > 0) && (myDestinationX > mySelectedX)) {
        jumpedX++;
      } else if((parity == 0) && (mySelectedX > myDestinationX)) {
        jumpedX--;
      }
      // remove the piece that was jumped over:
      myGrid[jumpedX][jumpedY] = 0;
      // now get ready to jump again if possible:
      mySelectedX = myDestinationX;
      mySelectedY = myDestinationY;
      myDestinationX = -1;
      myDestinationY = -1;
      // see if another jump is possible.
      // The "true" argument tells the program to return 
      // only jumps because the player can go again ONLY 
      // if there's a jump:
      getMoves(mySelectedX, mySelectedY, myPossibleMoves, true);
      // if there's another jump possible with the same piece, 
      // allow the player to continue jumping:
      if(myPossibleMoves.size() != 0) {
        myIsJumping = true;
        byte[] landing = (byte[])myPossibleMoves.elementAt(0);
        myDestinationX = landing[0];
        myDestinationY = landing[1];
      } else {
        // since there are no further jumps, we just end the turn 
        // by deselecting everything.
        mySelectedX = -1;
        mySelectedY = -1;
        myDestinationX = -1;
        myDestinationY = -1;
        myIsJumping = false;
        myPossibleMoves.removeAllElements();
        endTurn();
      }
    } else {
      // since it's not a jump, we just end the turn 
      // by deselecting everything.
      mySelectedX = -1;
      mySelectedY = -1;
      myDestinationX = -1;
      myDestinationY = -1;
      myPossibleMoves.removeAllElements();
      // tell the other player we're done:
      myIsJumping = false;
      endTurn();
    }
  }
  
  /**
   * Given a square on the grid, get the coordinates 
   * of one of the adjoining (diagonal) squares.
   * 0 = top left
   * 1 = top right
   * 2 = bottom left
   * 3 = bottom right.
   * @return the coordinates or null if the desired corner 
   * is off the board.
   */
  private byte[] getCornerCoordinates(byte x, byte y, byte corner) {
    byte[] retArray = null;
    if(corner < 2) {
      y--;
    } else {
      y++;
    }
    // Where the corner is on the grid depends on 
    // whether this is an odd row or an even row:
    if((corner % 2 == 0) && (y % 2 != 0)) {
      x--;
    } else if((corner % 2 != 0) && (y % 2 == 0)) {
      x++;
    }
    try {
      if(myGrid[x][y] > -15) {
        // we don't really care about the value, this
        // if statement is just there to get it to 
        // throw if the coordinates aren't on the board.
        retArray = new byte[2];
        retArray[0] = x;
        retArray[1] = y;
      }
    } catch(ArrayIndexOutOfBoundsException e) {
      // this throws if the coordinates do not correspond 
      // to a square on the board. It's not a problem, 
      // so we do nothing--we just return null instead 
      // of returning coordinates since no valid 
      // coordinates correspond to the desired corner.
    }
    return(retArray);
  }
  
  /**
   * Determines where the piece in the given 
   * grid location can move.  Clears the Vector
   * and fills it with the locations that 
   * the piece can move to.
   * @param jumpsOnly if we should return only moves that 
   *        are jumps.
   */
  private void getMoves(byte x, byte y, Vector toFill, boolean jumpsOnly) {
    toFill.removeAllElements();
    // if the square does not contain one of the local player's 
    // pieces, then there are no corresponding moves and we just
    // return an empty vector.
    if(myGrid[x][y] <= 0) {
      return;
    }
    // check each of the four corners to see if the 
    // piece can move there:
    for(byte i = 0; i < 4; i++) {
      byte[] coordinates = getCornerCoordinates(x, y, i);
      // if the coordinate array is null, then the corresponding 
      // corner is off the board and we don't deal with it.
      // The later two conditions in the following if statement
      // ensure that either the move is a forward move or the 
      // current piece is a king:
      if((coordinates != null) &&
         ((myGrid[x][y] > 1) || (i < 2))) {
        // if the corner is empty (and we're not looking 
        // for just jumps), then this is a possible move
        // so we add it to the vector of moves:
        if((myGrid[coordinates[0]][coordinates[1]] == 0) && (! jumpsOnly)) {
          toFill.addElement(coordinates);
          // if the space is occupied by an opponent, see if we can jump it:
        } else if(myGrid[coordinates[0]][coordinates[1]] < 0) {
          byte[] jumpLanding = getCornerCoordinates(coordinates[0], 
                                                 coordinates[1], i);
          // if the space on the far side of the opponent's piece
          // is on the board and is unoccupied, then a jump 
          // is possible, so we add it to the vector of moves:
          if((jumpLanding != null) && 
             (myGrid[jumpLanding[0]][jumpLanding[1]] == 0)) {
            toFill.addElement(jumpLanding);
          }
        }
      }
    } // end for loop
  }
    
}


