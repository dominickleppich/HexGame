package hex.player.ai;

import hex.board.Board;
import hex.player.AbstractPlayer;
import hex.preset.Move;

public abstract class ComputerPlayer extends AbstractPlayer {

	public ComputerPlayer(Board board, boolean color) {
		super(board, color);
	}

	@Override
	public abstract Move move();

}
