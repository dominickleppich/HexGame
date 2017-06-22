package hex.player;

import hex.board.Board;
import hex.graphic.GUI;
import hex.preset.Move;

public class HumanPlayer extends AbstractPlayer {

	public HumanPlayer(Board board, boolean color) {
		super(board, color);
	}

	public HumanPlayer(Board board, GUI gui, boolean color) {
		super(board, gui, color);
	}

	@Override
	public Move move() {
		if (gui == null)
			return CommandLineMoveInterpreter.request(board);
		else
			/* Fordere einen Zug von der GUI an */
			return gui.request();
	}

}
