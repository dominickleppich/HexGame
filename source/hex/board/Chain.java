package hex.board;

import hex.preset.Player;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Diese Klasse bildet Ketten von benachbarten Steinen. Sie wird benoetigt um
 * Gewinnsituationen zu erkennen
 * 
 * @author nepster
 * 
 */
public class Chain {
	private Board board;
	private boolean player;
	private LinkedList<LinkedList<Point>> items;
	private LinkedList<Point> chain;

	boolean win = false;

	// ---------------------------------------------------------------

	public Chain(Board board, boolean player, Point p) {
		this.board = board;
		this.player = player;
		items = new LinkedList<LinkedList<Point>>();

		/* Fuege rekursiv alle Nachbarn hinzu */
		addRecursively(new LinkedList<Point>(), p);
	}

	// ---------------------------------------------------------------

	public boolean isWinningChain() {
		return win;
	}

	public Iterator<Point> points() {
		return chain.iterator();
	}

	// ---------------------------------------------------------------

	private void addRecursively(LinkedList<Point> c, Point p) {
		if (win)
			return;

		if (c.contains(p)) {
			return;
		}

		if (!c.isEmpty() && !Board.areNeighbors(p, c.getLast())) {
			return;
		}

		LinkedList<Point> clone = new LinkedList<Point>();
		for (Point p2 : c)
			clone.add(p2);
		clone.add(p);
		items.add(clone);

		/* Pruefe moegliche Gewinnzuege */
		if (player == Player.RED) {
			if (p.y == board.dimY - 1)
				win = true;

		} else if (p.x == board.dimX - 1)
			win = true;

		if (win) {
			chain = clone;
			return;
		}

		for (Point neighbor : board.getNeighbors(p, player)) {
			int field = board.getField(neighbor);
			if ((field == Board.RED && player == Player.RED)
					|| (field == Board.BLUE && player == Player.BLUE))
				addRecursively(clone, neighbor);
		}
	}

	public boolean contains(Point p) {
		return items.contains(p);
	}

	// ---------------------------------------------------------------

	public String toString() {
		if (chain == null)
			return "";
		String s = "";
		for (Point p : chain)
			s += "[" + p.x + "," + p.y + "] ";
		return s;
	}
}
