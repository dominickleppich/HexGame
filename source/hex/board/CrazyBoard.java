package hex.board;

import hex.game.Game;
import hex.game.Output;
import hex.preset.Move;
import hex.preset.Player;
import hex.preset.Status;

import java.awt.Point;

public class CrazyBoard extends Board {
	public static boolean allUpdate = false;

	public CrazyBoard(boolean player) {
		super(player);
	}

	// ---------------------------------------------------------------

	/**
	 * Erzeuge ein Loch im Spielfeld
	 * @param x
	 * @param y
	 */
	public void makeHole(int x, int y) {
		if (isEmptyField(x, y)) {
			field[x][y] = Board.HOLE;
			Output.debug("debug_hole_created", Integer.toString(x), Integer.toString(y));
		}
	}

	/**
	 * Erzeuge ein schwarzes Loch im Spielfeld
	 * @param x
	 * @param y
	 */
	public void makeBlackHole(int x, int y) {
		if (isEmptyField(x, y)) {
			field[x][y] = Board.BLACKHOLE;
			Point[] neighbors = getNeighbors(new Point(x, y));
			for (Point p : neighbors)
				field[p.x][p.y] = Board.EMPTY;
			Output.debug("debug_blackhole_created", Integer.toString(x), Integer.toString(y));
			allUpdate = true;
		}
	}

	/**
	 * Erzeuge ein Bonusfeld im Spiel
	 * @param x
	 * @param y
	 */
	public void makeBonus(int x, int y) {
		if (isEmptyField(x, y)) {
			field[x][y] = Board.BONUS;
			Output.debug("debug_bonus_created", Integer.toString(x), Integer.toString(y));
			allUpdate = true;
		}
	}

	// ---------------------------------------------------------------

	/**
	 * Setze ein bestimmtes Feld auf einen uebergebenen Wert
	 * @param x
	 * @param y
	 * @param value
	 */
	public void setField(int x, int y, int value) {
		if (!isField(x, y))
			return;

		field[x][y] = value;
		Game.board(this);
	}

	// ---------------------------------------------------------------

	public Status makeMove(Move move) {
		if (move == null)
			return new Status(Status.ERROR);

		/* Teste ob dieses ein gueltiges leeres Feld ist */
		if (!isEmptyField(new Point(move.getX(), move.getY())))
			return new Status(Status.ILLEGAL);

		int x = move.getX();
		int y = move.getY();

		/*
		 * Setze den Spielstein in der Farbe des aktuellen Spielers auf das
		 * Spielbrett
		 */
		field[x][y] = (currentPlayer == Player.RED ? RED : BLUE);

		/* Speichere den Zug auf dem Stack */
		doneMoves.push(move);

		Status newStatus = new Status(Status.OK);

		Point[] neighbors = getAllNeighbors(new Point(x, y));

		boolean search = true;
		for (Point p : neighbors) {
			if (search) {
				switch (getField(p)) {
				case Board.BLACKHOLE:
					if (field[x][y] == Board.RED)
						field[x][y] = Board.BLACKHOLE_RED;
					else if (field[x][y] == Board.BLUE)
						field[x][y] = Board.BLACKHOLE_BLUE;
					search = false;

					break;
				case Board.BONUS:
					Point[] newFields = getAllNeighbors(p);
					for (Point p2 : newFields)
						if (isEmptyField(p2))
							field[p2.x][p2.y] = field[x][y];
					field[p.x][p.y] = field[x][y];
					search = false;
				}
			}
		}

		newStatus = isWinSituation();
		/* Falls keine Gewinnsituation */
		if (newStatus.isOK())
			/* Wechsle den Spieler */
			nextPlayer();

		/* Gib den Status zurueck */
		return newStatus;
	}
}
