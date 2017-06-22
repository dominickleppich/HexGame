package hex.preset;

import java.rmi.*;

public interface Player extends Remote {
	public static final boolean RED = true;
	public static final boolean BLUE = false;
	
	public static final int HUMAN = 0;
	public static final int COMPUTER_RANDOMAI = 1;
	
	// ---------------------------------------------------------------
	
    Move request() throws Exception, RemoteException; 
    void confirm(Status boardStatus) throws Exception, RemoteException;
    void update(Move opponentMove, Status boardStatus) throws Exception, RemoteException;
}
