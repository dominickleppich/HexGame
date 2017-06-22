package hex.board;

import hex.exception.IndexOutOfBoardException;
import hex.game.Output;
import hex.manager.SettingManager;
import hex.preset.Move;
import hex.preset.Player;
import hex.preset.Status;

import java.awt.Point;
import java.util.Iterator;
import java.util.Stack;

public class Board {
	public static final int HOLE = -1;
	public static final int EMPTY = 0;
	public static final int RED = 1;
	public static final int BLUE = 2;
	public static final int BORDER_RED = 3;
	public static final int BORDER_BLUE = 4;
	public static final int BORDER_BOTH_TOP = 5;
	public static final int BORDER_BOTH_BOTTOM = 6;
	public static final int GREEN = 7;
	public static final int BONUS = 8;
	public static final int BLACKHOLE = 9;
	public static final int BLACKHOLE_RED = 10;
	public static final int BLACKHOLE_BLUE = 11;

	public static final int[] neighborsX = { 0, 1, 1, 0, -1, -1 };
	public static final int[] neighborsY = { 1, 1, 0, -1, -1, 0 };

	public static final int[] neighborsXRed = { 0, 1, -1, 1, -1, 0 };
	public static final int[] neighborsYRed = { 1, 1, 0, 0, -1, -1 };
	public static final int[] neighborsXBlue = { 1, 1, 0, 0, -1, -1 };
	public static final int[] neighborsYBlue = { 0, 1, 1, -1, 0, -1 };

	public static final String FIELD_HOLE = " ";
	public static final String FIELD_EMPTY = ".";
	public static final String FIELD_RED = "X";
	public static final String FIELD_BLUE = "O";

	public static int DIMENSION_X = Integer.parseInt(SettingManager
			.getValue("DEFAULT_FIELD_DIMENSION"));
	public static int DIMENSION_Y = Integer.parseInt(SettingManager
			.getValue("DEFAULT_FIELD_DIMENSION"));

	public final int dimX, dimY;
	protected int[][] field;

	/*
	 * Stack zum "Merken" von Zuegen, um diese eventuell wieder rueckgaengig zu
	 * machen
	 */
	protected Stack<Move> doneMoves;

	protected boolean currentPlayer;

	protected Chain winningChain = null;

	// ---------------------------------------------------------------

	public Board(boolean player) {
		this(DIMENSION_X, DIMENSION_Y, player);
	}

	private Board(int dimX, int dimY, boolean player) {
		if (dimX < 0)
			this.dimX = 1;
		else if (dimX > Integer.parseInt(SettingManager.getValue("DIMENSION_LIMIT")))
			this.dimX = Integer.parseInt(SettingManager.getValue("DIMENSION_LIMIT"));
		else
			this.dimX = dimX;
		if (dimY < 0)
			this.dimY = 1;
		else if (dimY > Integer.parseInt(SettingManager.getValue("DIMENSION_LIMIT")))
			this.dimY = Integer.parseInt(SettingManager.getValue("DIMENSION_LIMIT"));
		else
			this.dimY = dimY;

		field = new int[dimX][dimY];
		doneMoves = new Stack<Move>();

		currentPlayer = player;
	}

	// ---------------------------------------------------------------

	/* Getter und Setter */

	public synchronized int getField(Point p) {
		if (p != null)
			return getField(p.x, p.y);
		return getField(-1, -1);
	}

	public synchronized int getField(int x, int y) {
		if (x == -1 && y == -1)
			return Board.BORDER_BOTH_BOTTOM;
		if (x == dimX && y == dimY)
			return Board.BORDER_BOTH_TOP;
		if ((x == -1 && y == dimY) || (x == dimX && y == -1))
			throw new IndexOutOfBoardException();
		if (x == -1 || x == dimX)
			return Board.BORDER_BLUE;
		if (y == -1 || y == dimY)
			return Board.BORDER_RED;
		if (x < 0 || y < 0 || x > dimX || y > dimY)
			throw new IndexOutOfBoardException();

		return field[x][y];
	}

	public synchronized Point[] getAllNeighbors(Point p) {
		return getNeighbors(true, neighborsX, neighborsY, p.x, p.y);
	}
	
	public synchronized Point[] getNeighbors(Point p, boolean player) {
		if (p == null)
			return null;

		if (player == Player.RED)
			return getNeighbors(false, neighborsXRed, neighborsYRed, p.x, p.y);
		else
			return getNeighbors(false, neighborsXBlue, neighborsYBlue, p.x, p.y);
	}

	/**
	 * Berechnet alle Nachbar Steine des Steins am Punkt p
	 * 
	 * @param p
	 *            Punkt
	 * @return Nachbarn
	 */
	public synchronized Point[] getNeighbors(Point p) {
		if (p != null)
			return getNeighbors(false, neighborsX, neighborsY, p.x, p.y);
		return null;
	}

	/**
	 * Berechnet alle Nachbar Steine des Steins an den Koordinate x, y
	 * 
	 * @param x
	 * @param y
	 * @return Nachbarn
	 */
	private synchronized Point[] getNeighbors(boolean all, int[] nX, int[] nY, int x, int y) {
		/* Falls dieses Feld ungueltig ist, liefere null zurueck */
		if (!isField(x, y))
			return null;

		Point[] p = new Point[6];
		int j = 0;
		for (int i = 0; i < nX.length; i++) {
			Point tmp = new Point(x + nX[i], y + nY[i]);
			try {
				int neighbor = getField(tmp.x, tmp.y);
				if (all) {
					if (neighbor != Board.HOLE && neighbor != Board.BORDER_RED && neighbor != Board.BORDER_BLUE && neighbor != Board.BORDER_BOTH_TOP && neighbor != Board.BORDER_BOTH_BOTTOM)
						p[j++] = tmp;
				} else if (neighbor == Board.RED || neighbor == Board.BLUE)
					p[j++] = tmp;
			} catch (IndexOutOfBoardException e) {
			}
		}
		Point[] neighbors = new Point[j];
		for (int c = 0; c < j; c++)
			neighbors[c] = p[c];
		return neighbors;
	}

	public static synchronized boolean areNeighbors(Point p1, Point p2) {
		return (Math.abs(p1.x - p2.x) <= 1 && Math.abs(p1.y - p2.y) <= 1);
	}

	public synchronized boolean getCurrentPlayer() {
		return currentPlayer;
	}

	public Chain getWinningChain() {
		return winningChain;
	}

	// ---------------------------------------------------------------

	/* Hilfsmethoden */

	protected boolean isField(int x, int y) {
		if (field == null)
			return false;
		if (x < 0 || y < 0 || x >= dimX || y >= dimY)
			return false;
		return true;
	}

	public boolean isEmptyField(Point p) {
		return isEmptyField(p.x, p.y);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isEmptyField(int x, int y) {
		if (isField(x, y))
			return field[x][y] == EMPTY;
		return false;
	}

	/**
	 * Wechsle den Spieler
	 */
	protected void nextPlayer() {
		currentPlayer = !currentPlayer;
	}

	// ---------------------------------------------------------------

	/* Zuege */

	/**
	 * Fuehre den uebergebenen Zug aus und liefere den Status, den das Board
	 * erzeugt.
	 * 
	 * @param move
	 *            Zug
	 * @return Status nach dem Zug
	 */
	public synchronized Status makeMove(Move move) {
		if (move == null)
			return new Status(Status.ERROR);

		/* Teste ob dieses ein gueltiges leeres Feld ist */
		if (!isEmptyField(new Point(move.getX(), move.getY())))
			return new Status(Status.ILLEGAL);

		/*
		 * Setze den Spielstein in der Farbe des aktuellen Spielers auf das
		 * Spielbrett
		 */
		field[move.getX()][move.getY()] = (currentPlayer == Player.RED ? RED
				: BLUE);

		/* Speichere den Zug auf dem Stack */
		doneMoves.push(move);

		

		Status newStatus = isWinSituation();
		/* Falls keine Gewinnsituation */
		if (newStatus.isOK()) 
			/* Wechsle den Spieler */
			nextPlayer();

		/* Gib den Status zurueck */
		return newStatus;
	}

	/** 
	 * Pruefe ob eine Gewinnsituation vorliegt
	 * @return
	 */
	public Status isWinSituation() {
		/* Pruefe eine Gewinnsituation */
		Chain[] chains = getChains(currentPlayer);
		for (Chain c : chains) {
			if (c.isWinningChain()) {
				winningChain = c;
				Iterator<Point> points = c.points();
				while (points.hasNext()) {
					Point p = points.next();
					field[p.x][p.y] = GREEN;
				}
				return (currentPlayer == Player.RED ? new Status(Status.REDWIN) : new Status(Status.BLUEWIN));
			}
		}
		return new Status(Status.OK);
	}
	
	/**
	 * Mache den zuletzt durchgefuehrten Zug rueckgaengig. Der Spieler der
	 * diesen Zug ausgefuehrt hat ist wieder an der Reihe.
	 * 
	 * @return
	 */
	public synchronized Status undoMove() {
		if (doneMoves.isEmpty()) {
			Output.error("error_board_revoke");
			return new Status(Status.ERROR);
		}

		/* Hole zuletzt gemachten Zug vom Stack */
		Move lastMove = doneMoves.pop();

		/* Loesche das gesetzte Feld */
		field[lastMove.getX()][lastMove.getY()] = EMPTY;

		/* Spielerwechsel */
		nextPlayer();

		/* Status OK */
		return new Status(Status.OK);
	}

	/**
	 * Bildet Steinketten ausgehend von einer der unteren Spielfeldgrenzen
	 * @param player
	 * @return
	 */
	protected Chain[] getChains(boolean player) {
		Chain[] ch = new Chain[(doneMoves.size() + 1) / 2];

		int i = 0;
		/* Durchlaufe alle moeglichen Felder */
		if (player == Player.BLUE)
			for (int y = 0; y < dimY; y++) {
				int field = getField(0, y);
				/* Falls es der eigene Stein ist */
				if (field == Board.BLUE)
					ch[i++] = new Chain(this, player, new Point(0, y));

			}
		else
			for (int x = 0; x < dimX; x++) {
				int field = getField(x, 0);
				/* Falls es der eigene Stein ist */
				if (field == Board.RED)
					ch[i++] = new Chain(this, player, new Point(x, 0));
			}

		Chain[] result = new Chain[i];
		for (int j = 0; j < i; j++)
			result[j] = ch[j];
		return result;
	}

	// ---------------------------------------------------------------

	/**
	 * Liefert eine Kopie des Spielfeldes
	 */
	@Override
	public synchronized Board clone() {
		Board b = new Board(dimX, dimY, currentPlayer);
		for (int x = 0; x < dimX; x++) {
			for (int y = 0; y < dimY; y++)
				b.field[x][y] = field[x][y];
		}

		b.currentPlayer = currentPlayer;
		// TODO
		// Stack muss noch kopiert werden!!
		return b;
	}

	/**
	 * Liefert eine String-Repraesentation des Spielfeldes.
	 */
	@Override
	public synchronized String toString() {
		String s = "   ";
		for (int x = 0; x < dimX; x++)
			s += (char) (x + 'A') + " ";
		s += "\n";
		for (int y = dimY - 1; y >= 0; y--) {
			/* Einrueckung am Anfang der Zeile */
			for (int i = 0; i < dimY - y; i++)
				s += " ";

			if (y < 9)
				s += " ";
			s += (y + 1) + " ";

			for (int x = 0; x < dimX; x++) {
				switch (field[x][y]) {
				case HOLE:
					s += FIELD_HOLE + " ";
					break;
				case EMPTY:
					s += FIELD_EMPTY + " ";
					break;
				case RED:
					s += FIELD_RED + " ";
					break;
				case BLUE:
					s += FIELD_BLUE + " ";
					break;
				}
			}

			if (y < 9)
				s += " ";
			s += (y + 1);
			s += "\n";
		}

		s += "    ";
		for (int y = 0; y < dimY; y++)
			s += " ";

		for (int x = 0; x < dimX; x++)
			s += (char) (x + 'A') + " ";

		return s;
	}
}