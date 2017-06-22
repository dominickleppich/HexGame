package hex.graphic.simple;

import hex.board.Board;
import hex.game.Game;
import hex.game.Output;
import hex.graphic.GUI;
import hex.graphic.GUIRefresherThread;
import hex.manager.GFXManager;
import hex.manager.LanguageManager;
import hex.manager.SettingManager;
import hex.manager.SoundManager;
import hex.preset.Move;
import hex.preset.Player;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class SimpleGUI extends JFrame implements GUI {
	private static final long serialVersionUID = 1L;

	int width;
	int height;

	private Board board;
	private double scaleFactor;
	private Polygon[][] hexagons;
	private Polygon[] borders;

	private String printText = "";

	private DrawPanel panel;

	private boolean inputEnabled = false;
	private Move requestedMove;

	public SimpleGUI(Board board) {
		super(SettingManager.getValue("WINDOW_NAME"));

		this.board = board;

		scaleFactor(Double.parseDouble(SettingManager.getValue("DEFAULT_SCALE_FACTOR")));

		panel = new DrawPanel(this);

		panel.setDoubleBuffered(true);

		/* Maus-Listener fuer Mausklicks */
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				/* Reagiere nur auf Linksklick */
				if (e.getButton() == MouseEvent.BUTTON1)
					panel.mouseClick(e.getX(), e.getY());
			}
		});

		/* Erzeuge den GUI-Refresher Thread, der die GUI aktualisiert */
		new GUIRefresherThread(this).start();

		add(panel);
		pack();

		/* Mauszeiger zu einer Hand aendern */
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setVisible(true);
	}

	// ---------------------------------------------------------------

	public double scaleFactor() {
		return scaleFactor;
	}

	@Override
	public void scaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;

		int hexaSize = (int) ((GFXManager.backgroundOriginalWidth() * scaleFactor) / (board.dimX + board.dimY + 1) / 0.75);

		if ((board.dimX + board.dimY + 4) / 2 * hexaSize > GFXManager.backgroundOriginalHeight() * scaleFactor)
			hexaSize = (int) (GFXManager.backgroundOriginalHeight() * scaleFactor) / ((board.dimX + board.dimY + 4) / 2);

		Output.debug("debug_setting_hexasize", Integer.toString(hexaSize));

		int hexaHorizontalSpace = (int) (hexaSize * 0.75);
		int hexaVerticalSpace = hexaSize / 2;

		width = (int) ((board.dimX + board.dimY + 1) * hexaHorizontalSpace);
		height = (int) ((board.dimX + board.dimY + 3) * hexaVerticalSpace);

		/* Berechne die Stelle des ersten Hexagons */
		int beginXCoordinate = width / 2 - (int) ((double) (board.dimX - board.dimY) / 2 * hexaHorizontalSpace) - (hexaSize / 2);
		int beginYCoordinate = height / 2 + (((board.dimX + board.dimY) / 2) - 2) * hexaVerticalSpace;

		hexagons = new Polygon[board.dimX][board.dimY];
		for (int x = 0; x < board.dimX; x++) {
			for (int y = 0; y < board.dimY; y++) {
				int bx = beginXCoordinate + (x - y) * hexaHorizontalSpace;
				int by = beginYCoordinate - (x + y) * hexaVerticalSpace;
				int[] xc = new int[6];
				int[] yc = new int[6];
				xc[0] = bx + hexaSize / 4;
				xc[1] = bx + (hexaSize * 3) / 4;
				xc[2] = bx + hexaSize;
				xc[3] = bx + (hexaSize * 3) / 4;
				xc[4] = bx + hexaSize / 4;
				xc[5] = bx;
				yc[0] = by;
				yc[1] = by;
				yc[2] = by + hexaSize / 2;
				yc[3] = by + hexaSize;
				yc[4] = by + hexaSize;
				yc[5] = by + hexaSize / 2;
				hexagons[x][y] = new Polygon(xc, yc, 6);
			}
		}

		borders = new Polygon[4];
		int[] x, y;
		x = new int[]{hexagons[0][0].xpoints[5] + hexaSize / 2,
				hexagons[0][0].xpoints[5] + hexaSize / 2,
				hexagons[board.dimX-1][0].xpoints[5] + (hexaSize * 3) / 2,
				hexagons[board.dimX-1][0].xpoints[5] + (hexaSize * 3) / 2,
				hexagons[board.dimX-1][0].xpoints[5] + hexaSize / 2};
		y = new int[]{hexagons[0][0].ypoints[5],
				hexagons[0][0].ypoints[5] + hexaSize,
				hexagons[board.dimX - 1][0].ypoints[5] + hexaSize / 2,
				hexagons[board.dimX - 1][0].ypoints[5],
				hexagons[board.dimX - 1][0].ypoints[5]};
		borders[0] = new Polygon(x, y, 5);
		
		x = new int[]{hexagons[board.dimX-1][0].xpoints[5] + hexaSize / 2,
				hexagons[board.dimX-1][0].xpoints[5] + (hexaSize * 3) / 2,
				hexagons[board.dimX-1][0].xpoints[5] + (hexaSize * 3) / 2,
				hexagons[board.dimX-1][board.dimY-1].xpoints[5] + hexaSize /2,
				hexagons[board.dimX-1][board.dimY-1].xpoints[5] + hexaSize /2};
		y = new int[]{hexagons[board.dimX-1][0].ypoints[5],
				hexagons[board.dimX-1][0].ypoints[5],
				hexagons[board.dimX-1][0].ypoints[5] - hexaSize / 2,
				hexagons[board.dimX-1][board.dimY-1].ypoints[5] - hexaSize,
				hexagons[board.dimX-1][board.dimY-1].ypoints[5]};
		borders[1] = new Polygon(x, y, 5);
		
		x = new int[]{hexagons[board.dimX-1][board.dimY-1].xpoints[5] + hexaSize /2,
				hexagons[board.dimX-1][board.dimY-1].xpoints[5] + hexaSize /2,
				hexagons[0][board.dimY-1].xpoints[5] - hexaSize / 2,
				hexagons[0][board.dimY-1].xpoints[5] - hexaSize / 2,
				hexagons[0][board.dimY-1].xpoints[5] + hexaSize / 2};
		y = new int[]{hexagons[board.dimX-1][board.dimY-1].ypoints[5],
				hexagons[board.dimX-1][board.dimY-1].ypoints[5] - hexaSize,
				hexagons[0][board.dimY-1].ypoints[5] - hexaSize / 2,
				hexagons[0][board.dimY-1].ypoints[5],
				hexagons[0][board.dimY-1].ypoints[5]};
		borders[2] = new Polygon(x, y, 5);
		
		x = new int[]{hexagons[0][board.dimY-1].xpoints[5] + hexaSize /2,
				hexagons[0][board.dimY-1].xpoints[5] - hexaSize /2,
				hexagons[0][board.dimY-1].xpoints[5] - hexaSize /2,
				hexagons[0][0].xpoints[5] + hexaSize / 2,
				hexagons[0][0].xpoints[5] + hexaSize / 2};
		y = new int[]{hexagons[0][board.dimY-1].ypoints[5],
				hexagons[0][board.dimY-1].ypoints[5],
				hexagons[0][board.dimY-1].ypoints[5] + hexaSize / 2,
				hexagons[0][0].ypoints[5] + hexaSize,
				hexagons[0][0].ypoints[5]};
		borders[3] = new Polygon(x, y, 5);
	}

	public Point getBoardIndexFromCoordinates(Point p) {
		for (int x = 0; x < board.dimX; x++)
			for (int y = 0; y < board.dimY; y++)
				if (hexagons[x][y].contains(p))
					return new Point(x, y);
		return null;
	}

	// ---------------------------------------------------------------

	@Override
	public Move request() {
		Output.debug("debug_gui_input_enabled");
		inputEnabled = true;
		playerMakeMove();
		requestedMove = panel.request();
		Output.debug("debug_gui_input_disabled");
		inputEnabled = false;
		SoundManager.playSound("mouseClick");
		return requestedMove;
	}

	public void playerMakeMove() {
		Output.debug("debug_player_has_to_make_move",
				Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE"));
		printText(LanguageManager.getText("game_your_turn", (Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_RED")
				: SettingManager.getValue("PLAYER_BLUE")), (Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_BLUE")
				: SettingManager.getValue("PLAYER_RED"))));
	}

	public boolean inputEnabled() {
		return inputEnabled;
	}

	@Override
	public void makeMove(Move m) {
		panel.makeMove(m);
	}

	public Polygon[][] hexagons() {
		return hexagons;
	}

	public Polygon[] borders() {
		return borders;
	}

	@Override
	public Board board() {
		return board;
	}

	@Override
	public void board(Board b) {
		this.board = b;
		repaint();
	}

	// ---------------------------------------------------------------

	@Override
	public void update() {

	}

	@Override
	public void update(boolean color, Move move) {

	}

	public void renderScreen() {
		panel.render();
	}
	
	@Override
	public void printText(String s) {
		printText = s;
	}

	public String printText() {
		return printText;
	}

	@Override
	public void updateScreen() {
		// TODO Auto-generated method stub
		
	}
}
