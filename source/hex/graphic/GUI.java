package hex.graphic;

import hex.board.Board;
import hex.preset.Move;

/**
 * Interface fuer alle GUI's
 * @author nepster
 *
 */
public interface GUI {
	public Move request();
	public void makeMove(Move m);
	public void scaleFactor(double scaleFactor);
	public void board(Board b);
	public Board board();
	public void renderScreen();
	public void updateScreen();
	public void update();
	public void update(boolean color, Move move);
	public void printText(String s);
}
