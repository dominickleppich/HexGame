package hex.cheat;

import hex.board.Board;
import hex.board.CrazyBoard;
import hex.graphic.extended.GraphicComponent;
import hex.manager.GraphicComponentManager;

import java.awt.Image;
import java.awt.Point;

public class BlackHoleNeighbor extends GraphicComponent {

	private Board board;
	private Point boardPoint;

	public BlackHoleNeighbor(GraphicComponentManager gcm, String componentName, Point p, Point boardPoint, Board board, Image[] images) {
		super(gcm, componentName, p, images);

		this.boardPoint = boardPoint;
		this.board = board;
	}

	// ---------------------------------------------------------------

	public Image frame() {
		if (frames == null)
			return null;

		if (frames.size() == 0)
			return null;


		if (currentFrame >= frames.size())
			return null;
		Image i = frames.get(currentFrame);
		currentFrame++;

		if (currentFrame == frames.size())
			((CrazyBoard) board).setField(boardPoint.x, boardPoint.y, Board.EMPTY);
		
		return i;
	}

}
