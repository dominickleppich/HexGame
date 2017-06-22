package hex.graphic.simple;

import hex.board.Board;
import hex.game.Output;
import hex.manager.SettingManager;
import hex.preset.Move;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class DrawPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static Color empty = new Color(210, 210, 210);

	private SimpleGUI gui;

	private Move requestedMove;

	public DrawPanel(SimpleGUI gui) {
		this.gui = gui;
	}

	// ---------------------------------------------------------------

	public Dimension getPreferredSize() {
		return new Dimension(gui.width, gui.height);
	}

	// ---------------------------------------------------------------

	public void paintComponent(Graphics g) {
		
	}
	
	public void render() {
		Graphics2D g = (Graphics2D) this.getGraphics();
		
		/* Zeichne einen weissen Hintergrund */
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, gui.width, gui.height);

		drawBorders(g);
		drawHexagons(g);
		drawGrid(g);

		String text = gui.printText();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		/* Setze Schriftfarbe und Schrift */
		g.setColor(Color.decode(SettingManager.getValue("SIMPLE_GUI_PRINT_TEXT_COLOR")));
		float stringSize = (float) Double.parseDouble(SettingManager.getValue("SIMPLE_GUI_PRINT_TEXT_SIZE"));
		Font textFont = new Font(SettingManager.getValue("SIMPLE_GUI_PRINT_TEXT_FONT_NAME"), Integer.parseInt(SettingManager
				.getValue("SIMPLE_GUI_PRINT_TEXT_FONT_TYPE")), (int) stringSize);
		g.setFont(textFont);
		if (text != "") {
			g.drawString(text, Integer.parseInt(SettingManager.getValue("SIMPLE_GUI_PRINT_TEXT_LEFT")),
					Integer.parseInt(SettingManager.getValue("SIMPLE_GUI_PRINT_TEXT_TOP")));
		}
	}
	
	private void drawBorders(Graphics2D g) {
		Polygon[] borders = gui.borders();
		g.setColor(Color.RED);
		g.fillPolygon(borders[0]);
		g.fillPolygon(borders[2]);
		g.setColor(Color.BLUE);
		g.fillPolygon(borders[1]);
		g.fillPolygon(borders[3]);
	}

	private void drawGrid(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(3f));

		Polygon[][] hexagons = gui.hexagons();

		for (int x = 0; x < gui.board().dimX; x++) {
			for (int y = 0; y < gui.board().dimY; y++) {
				int[] xc = hexagons[x][y].xpoints;
				int[] yc = hexagons[x][y].ypoints;
				for (int j = 0; j < xc.length; j++)
					g.drawLine(xc[j], yc[j], xc[(j + 1) % xc.length], yc[(j + 1) % yc.length]);
			}
		}
	}

	private void drawHexagons(Graphics2D g) {
		Polygon[][] hexagons = gui.hexagons();

		for (int x = 0; x < gui.board().dimX; x++) {
			for (int y = 0; y < gui.board().dimY; y++) {
				switch (gui.board().getField(x, y)) {
				case Board.RED:
					g.setColor(Color.RED);
					g.fillPolygon(hexagons[x][y]);
					break;
				case Board.BLUE:
					g.setColor(Color.BLUE);
					g.fillPolygon(hexagons[x][y]);
					break;
				case Board.GREEN:
					g.setColor(Color.GREEN);
					g.fillPolygon(hexagons[x][y]);
					break;
				default:
					g.setColor(empty);
					g.fillPolygon(hexagons[x][y]);
				}
			}
		}
	}

	// ---------------------------------------------------------------

	/**
	 * Fordere einen Zug vom Spieler an
	 * 
	 * @param color
	 *            Spielerfarbe
	 * @return Zug
	 */
	public synchronized Move request() {
		try {
			wait();
		} catch (InterruptedException e) {
			Output.error("error_gui_input", e.toString());
		}
		return requestedMove;
	}

	/**
	 * Zug wurde gemacht, schlafender Thread wird geweckt
	 */
	private synchronized void moveDone() {
		notify();
	}

	public void makeMove(Move m) {
		requestedMove = m;
		moveDone();
	}

	/**
	 * Reagiert auf Mausklicks. Falls die grafische Eingabe aktiviert ist, wird
	 * der Board-Index bestimmt und daraus ein Zug generiert, welcher
	 * zurueckgegeben werden kann.
	 * 
	 * @param x
	 * @param y
	 */
	public void mouseClick(int x, int y) {
		Output.debug("debug_mouse_clicked", Integer.toString(x), Integer.toString(y));

		/* Tue nichts, falls die GUI inaktiv ist */
		if (!gui.inputEnabled()) {
			Output.debug("debug_gui_input_not_enabled");
			return;
		}

		if (x < 0 && y < 0) {
			requestedMove = null;
			moveDone();
			return;
		}

		Point p = gui.getBoardIndexFromCoordinates(new Point(x, y));
		if (p != null && gui.board().isEmptyField(p))
			requestedMove = new Move(p.x, p.y);

		if (requestedMove != null)
			moveDone();
	}
}
