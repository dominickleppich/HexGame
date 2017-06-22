package hex.preset;

/**
 * Implementiert Zug-Objekte
 * 
 * @author Doktor Brosenne
 * 
 */
public class Move implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private int x;
	private int y;

	public Move(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Move(Move move) {
		x = move.x;
		y = move.y;
	}

	// ---------------------------------------------------------------

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	// ---------------------------------------------------------------

	/**
	 * Stringformat eines Move-Objekts.
	 */
	@Override
	public String toString() {
		return "(" + x + "/" + y + ")";
	}
}
