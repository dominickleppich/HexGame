package hex.player;

import hex.board.Board;
import hex.exception.IllegalGameSequenceException;
import hex.exception.StatusMismatchException;
import hex.game.Game;
import hex.game.Output;
import hex.graphic.GUI;
import hex.manager.SettingManager;
import hex.preset.Move;
import hex.preset.Player;
import hex.preset.Status;

import java.rmi.RemoteException;

public abstract class AbstractPlayer implements Player {
	public static final int SEQUENCE_REQUEST = 0;
	public static final int SEQUENCE_CONFIRM = 1;
	public static final int SEQUENCE_UPDATE = 2;

	protected int nextGameSequence;
	protected Board board;
	protected GUI gui;

	protected boolean color;

	protected Move requestedMove;

	// ---------------------------------------------------------------

	public AbstractPlayer(Board board, boolean color) {
		this(board, null, color);
	}

	public AbstractPlayer(Board board, GUI gui, boolean color) {
		this.board = board;

		this.gui = gui;

		this.color = color;

		if (board.getCurrentPlayer() == color)
			nextGameSequence = SEQUENCE_REQUEST;
		else
			nextGameSequence = SEQUENCE_UPDATE;

		Output.debug("debug_new_player_created",
				(color == Player.RED ? SettingManager.getValue("PLAYER_RED")
						: SettingManager.getValue("PLAYER_BLUE")));
	}

	// ---------------------------------------------------------------

	public abstract Move move();

	// ---------------------------------------------------------------

	public void switchColor() {
		Output.debug("debug_player_switched_color");
		color = !color;
		renewBoard();
	}

	public void repairGameSequence() {
		System.out.println("from " + nextGameSequence);
		if (nextGameSequence == SEQUENCE_UPDATE)
			nextGameSequence = SEQUENCE_REQUEST;
		else
			nextGameSequence = SEQUENCE_UPDATE;
		System.out.println("to " + nextGameSequence);
		Output.debug("debug_player_repair_game_sequence");
	}

	public void renewBoard() {
		if (gui != null)
			gui.board(Game.getCurrentBoard());
	}

	// ---------------------------------------------------------------

	@Override
	public final Move request() throws Exception, RemoteException {
		/* Pruefe korrekte Ausfuehrungsreihenfolge */
		if (nextGameSequence != SEQUENCE_REQUEST)
			throw new IllegalGameSequenceException("A move request expected.");

		/* Fordere einen Zug an */
		Output.debug("debug_player_request", SettingManager
				.getValue(color == Player.RED ? "PLAYER_RED" : "PLAYER_BLUE"));
		requestedMove = move();
		if (requestedMove != null)
			Output.debug("debug_player_move_received", requestedMove.toString());

		/* als naechstes wird dann ein confirm erwartet */
		nextGameSequence = SEQUENCE_CONFIRM;

		return requestedMove;
	}

	@Override
	public void confirm(Status boardStatus) throws Exception, RemoteException {
		/* Pruefe korrekte Ausfuehrungsreihenfolge */
		if (nextGameSequence != SEQUENCE_CONFIRM)
			throw new IllegalGameSequenceException("A move confirm expected.");

		Output.debug("debug_player_confirm", SettingManager
				.getValue(color == Player.RED ? "PLAYER_RED" : "PLAYER_BLUE"));

		switch (boardStatus.getValue()) {
		/* Alles in Ordnung */
		case Status.OK:
			Output.debug("debug_move_confirmation", "OK");
			board.makeMove(requestedMove);
			break;

		/* Rot gewinnt */
		case Status.REDWIN:
			Output.debug("debug_move_confirmation", "RedWIN");
			board.makeMove(requestedMove);
			break;

		/* Blau gewinnt */
		case Status.BLUEWIN:
			Output.debug("debug_move_confirmation", "BlueWIN");
			board.makeMove(requestedMove);
			break;

		/* Illegaler Zug */
		case Status.ILLEGAL:
			Output.debug("debug_move_confirmation", "Illegal");
			/* Es muss als naechstes ein korrekter Zug angefordert werden */
			nextGameSequence = SEQUENCE_REQUEST;
			return;

			/* ERROR */
		case Status.ERROR:
			Output.debug("debug_move_confirmation", "ERROR");
			throw new IllegalArgumentException("STATUS = ERROR");

			/* default */
		default:
			throw new IllegalArgumentException("UNKNOWN STATUS");
		}

		/* als naechstes wird dann ein update erwartet */
		nextGameSequence = SEQUENCE_UPDATE;
	}

	@Override
	public void update(Move opponentMove, Status boardStatus) throws Exception,
			RemoteException {
		/* Pruefe korrekte Ausfuehrungsreihenfolge */
		if (nextGameSequence != SEQUENCE_UPDATE)
			throw new IllegalGameSequenceException("A move update expected.");

		Output.debug("debug_player_update", SettingManager
				.getValue(color == Player.RED ? "PLAYER_RED" : "PLAYER_BLUE"));

		Output.debug("debug_opponent_move", opponentMove.toString());

		Status myStatus = board.makeMove(opponentMove);
		
		/* Pruefe den Status nur, wenn keine Cheats aktiviert sind */
		if (myStatus.getValue() != boardStatus.getValue() && !Boolean.parseBoolean(SettingManager.getValue("CHEATS_ENABLED")))
			throw new StatusMismatchException();

		Output.debug("debug_opponent_move_updated");

		/* als naechstes wird dann ein request erwartet */
		nextGameSequence = SEQUENCE_REQUEST;
	}

}
