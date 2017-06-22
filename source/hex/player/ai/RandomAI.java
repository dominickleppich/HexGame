package hex.player.ai;

import hex.board.Board;
import hex.manager.SettingManager;
import hex.preset.Move;

import java.util.Random;

public class RandomAI extends ComputerPlayer {
	public RandomAI(Board board, boolean color) {
		super(board, color);
	}
	
	// ---------------------------------------------------------------
	
	@Override
	public Move move() {
		try {
			Thread.sleep(Integer.parseInt(SettingManager.getValue("COMPUTER_PLAYER_TIMEOUT")));
		} catch (InterruptedException e) {}
		Random r = new Random();
		Move m;
		do {
			m = new Move(r.nextInt(Board.DIMENSION_X), r.nextInt(Board.DIMENSION_Y));
		} while(board.getField(m.getX(), m.getY()) != Board.EMPTY);
		return m;
	}

}
