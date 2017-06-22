package hex.player.ai;

import hex.board.Board;
import hex.exception.IllegalGameSequenceException;
import hex.exception.StatusMismatchException;
import hex.game.Output;
import hex.preset.Move;
import hex.preset.Status;

import java.rmi.RemoteException;
import java.util.Random;

public class EasyAI extends ComputerPlayer {
	Move lastMove; /* Letzter Zug des Gegners */
	Move m;
	Random r = new Random();
	Move MoveChain[] = new Move[(board.dimX * board.dimY) / 2];
	int i = 0;
	int Traveler = 0;
	boolean noFiller = true;

	public EasyAI(Board board, boolean color) {
		super(board, color);
	}

	@Override
	public Move move() {
		if (lastMove == null) { /* Stelle fest ob Erster */
			m = Starter();
		} else {
			m = Folger();
		}
		return m;
	}

	private Move Starter() { /* Einfacher Eroeffnungszug, abhaengig von Brettgroesse */
		Move newMove = null;
		newMove = new Move(1, (r.nextInt(board.dimY - 2) + 1));
		MoveChain[i++] = newMove;
		return newMove;
	}

	private Move Folger() {
		Move newMove = null;
		if (noFiller && requestedMove.getX() + 2 < board.dimX
				&& requestedMove.getY() + 2 < board.dimY) {
			if (board.getField(requestedMove.getX() + 2,
					requestedMove.getY() + 1) == Board.EMPTY) { /*
																 * Diagonal
																 * rechts oben
																 * Feld frei?
																 */
				newMove = new Move(requestedMove.getX() + 2,
						requestedMove.getY() + 1);
				System.out.println("Weiche aus: Rechts Oben!");
			} else if (requestedMove.getY() >= (int) (Math
					.floor(board.dimY / 2))
					&& board.getField(requestedMove.getX() + 1,
							requestedMove.getY() - 1) == Board.EMPTY) { /*
																		 * Feld
																		 * links
																		 * frei?
																		 */
				newMove = new Move(requestedMove.getX() + 1,
						requestedMove.getY() - 1);
				System.out.println("Weiche aus: Links!");
			} else if (requestedMove.getY() <= (int) (Math
					.floor(board.dimY / 2))
					&& board.getField(requestedMove.getX() + 1,
							requestedMove.getY() + 2) == Board.EMPTY) { /*
																		 * Feld
																		 * Diagonal
																		 * links
																		 * oben
																		 * frei?
																		 */
				newMove = new Move(requestedMove.getX() + 1,
						requestedMove.getY() + 2);
				System.out.println("Weiche aus: Links Oben!");
			} else {
				System.out.println("Weiche aus: Random!");
				newMove = new Move(r.nextInt(11), r.nextInt(11));
			}
		} else if (requestedMove.getX() + 1 < board.dimX
				|| requestedMove.getY() + 1 < board.dimY) {
			noFiller = false;
			newMove = Filler();
		}
		System.out.println(newMove);
		if(noFiller)MoveChain[i++] = newMove;
		return newMove;

	}

	private Move Filler() {
		System.out.println("Filler gestartet!");
		Move newMove = null;
		if (board.getField(MoveChain[Traveler].getX() - 1,
				MoveChain[Traveler].getY()) == Board.EMPTY) {
			newMove = new Move(MoveChain[Traveler].getX() - 1,
					MoveChain[Traveler].getY());
		} else if (board.getField(MoveChain[Traveler].getX() - 1,
				MoveChain[Traveler].getY() - 1) == Board.EMPTY) {
			newMove = new Move(MoveChain[Traveler].getX() - 1,
					MoveChain[Traveler].getY() - 1);
		}
		if(Traveler == i){
			newMove = new Move(MoveChain[Traveler].getX() + 1,
					MoveChain[Traveler].getY() + 1);
		}
		Traveler++;
		return newMove;
	}

	public void update(Move opponentMove, Status boardStatus) throws Exception,
			RemoteException {
		/* Pruefe korrekte Ausfuehrungsreihenfolge */
		if (nextGameSequence != SEQUENCE_UPDATE)
			throw new IllegalGameSequenceException("A move update expected.");

		Output.debug("debug_opponent_move", opponentMove.toString());

		Status myStatus = board.makeMove(opponentMove);
		if (myStatus.getValue() != boardStatus.getValue())
			throw new StatusMismatchException();
		Output.debug("debug_opponent_move_updated");
		lastMove = opponentMove;
		/* als naechstes wird dann ein request erwartet */
		nextGameSequence = SEQUENCE_REQUEST;
	}

}
