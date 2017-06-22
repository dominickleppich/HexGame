package hex.manager;

import hex.board.Board;
import hex.cheat.BlackHoleNeighbor;
import hex.exception.IndexOutOfBoardException;
import hex.game.Game;
import hex.game.Output;
import hex.graphic.GUI;
import hex.graphic.extended.GraphicComponent;
import hex.graphic.extended.GraphicalGUI;
import hex.preset.Player;

import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

public class GraphicComponentManager {
	public static final boolean FIRST_LEVEL = true;
	public static final boolean SECOND_LEVEL = false;

	/* Alle Komponenten die ueber dem Spielfeld sind */
	private HashMap<String, GraphicComponent> componentsFirstLevel;
	/* Alle Komponenten die zum Spielfeld gehoeren */
	private HashMap<String, GraphicComponent> componentsSecondLevel;
	/* Skalierte Bilder */
	private HashMap<String, LinkedList<Image>> scaledImages;

	private GUI gui;

	/* Aktueller Skalierungsfaktor */
	private double scaleFactor;

	/* Board Positionen */
	private int[][] hexaXCoordinates;
	private int[][] hexaYCoordinates;
	private Polygon[][] hexagons;

	/* Vertikale und horizontale Abstaende zwischen Hexagons */
	private int hexaHorizontalSpace;
	private int hexaVerticalSpace;

	private int hexaSize;

	/* obere linke Ecke des ersten Hexagons (oben links) */
	private int beginXCoordinate;
	private int beginYCoordinate;

	/* CHEATS */
	private boolean cheatLightning = false;
	private boolean cheatColorConfusion = false;
	private boolean cheatMatrix = false;

	// ---------------------------------------------------------------

	public GraphicComponentManager(GraphicalGUI gui) {
		this.gui = gui;

		scaleFactor(Double.parseDouble(SettingManager.getValue("DEFAULT_SCALE_FACTOR")));
	}

	// ---------------------------------------------------------------
	/* Bilder */

	public synchronized Image image(String identifier) {
		if (scaledImages == null)
			return null;
		if (!scaledImages.containsKey(identifier))
			return null;

		return scaledImages.get(identifier).get(0);
	}

	public synchronized Image[] images(String identifier) {
		if (scaledImages == null)
			return null;
		if (!scaledImages.containsKey(identifier))
			return null;

		LinkedList<Image> list = scaledImages.get(identifier);
		Image[] images = new Image[list.size()];
		for (int i = 0; i < list.size(); i++)
			images[i] = list.get(i);
		return images;
	}

	// ---------------------------------------------------------------
	/* Komponenten */

	public synchronized Iterator<GraphicComponent> components(boolean level) {
		HashMap<String, GraphicComponent> components;
		if (level == FIRST_LEVEL)
			components = componentsFirstLevel;
		else
			components = componentsSecondLevel;

		if (components == null)
			return null;
		return components.values().iterator();
	}

	public synchronized GraphicComponent component(boolean level, String name, Point p) {
		HashMap<String, GraphicComponent> components;
		if (level == FIRST_LEVEL)
			components = componentsFirstLevel;
		else
			components = componentsSecondLevel;

		if (components.containsKey(name + "~" + p.x + "~" + p.y))
			return components.get(name + "~" + p.x + "~" + p.y);
		return null;
	}

	// public synchronized GraphicComponent add(boolean level, String
	// identifier, GraphicComponent gc) {
	// HashMap<String, GraphicComponent> components;
	// if (level == FIRST_LEVEL)
	// components = componentsFirstLevel;
	// else
	// components = componentsSecondLevel;
	//
	// if (components.containsKey(identifier))
	// return components.get(identifier);
	//
	// components.put(identifier, gc);
	// return gc;
	// }

	public synchronized GraphicComponent add(boolean level, String name, String identifier, Point p) {
		HashMap<String, GraphicComponent> components;
		if (level == FIRST_LEVEL)
			components = componentsFirstLevel;
		else
			components = componentsSecondLevel;

		if (components.containsKey(name + "~" + p.x + "~" + p.y))
			return components.get(name + "~" + p.x + "~" + p.y);
		if (!scaledImages.containsKey(identifier))
			return null;

		GraphicComponent newComponent = new GraphicComponent(this, name + "~" + p.x + "~" + p.y, p, scaledImages.get(identifier));
		components.put(name + "~" + p.x + "~" + p.y, newComponent);
		return newComponent;
	}

	public synchronized void remove(String componentName) {
		if (componentsFirstLevel.containsKey(componentName))
			componentsFirstLevel.remove(componentName);
		if (componentsSecondLevel.containsKey(componentName))
			componentsSecondLevel.remove(componentName);
	}

	// ---------------------------------------------------------------

	/**
	 * Setze den Skalierungsfaktor
	 * 
	 * @param scaleFactor
	 */
	public synchronized void scaleFactor(double scaleFactor) {
		Output.debug("debug_setting_scalefactor", Double.toString(scaleFactor));
		this.scaleFactor = scaleFactor;

		hexagonCalculations();

		generateScaledImages();

		reloadComponents(FIRST_LEVEL);
		reloadComponents(SECOND_LEVEL);
	}

	public synchronized double scaleFactor() {
		return scaleFactor;
	}

	public synchronized void reloadComponents(boolean level) {
		if (level == FIRST_LEVEL) {
			componentsFirstLevel = new HashMap<String, GraphicComponent>();
		} else {
			componentsSecondLevel = new HashMap<String, GraphicComponent>();
			loadBoardComponents();
		}
	}

	private synchronized void generateScaledImages() {
		scaledImages = new HashMap<String, LinkedList<Image>>();

		/* Fuege das Hintergrundbild ein */
		Image boardBackground = GFXManager.image("boardBackground").get(0);
		LinkedList<Image> l = new LinkedList<Image>();
		l.add(boardBackground.getScaledInstance((int) (boardBackground.getWidth(null) * scaleFactor),
				(int) (boardBackground.getHeight(null) * scaleFactor), Image.SCALE_SMOOTH));
		scaledImages.put("boardBackground", l);

		Iterator<Entry<String, LinkedList<Image>>> items = GFXManager.images();
		while (items.hasNext()) {
			Entry<String, LinkedList<Image>> item = items.next();

			LinkedList<Image> list = new LinkedList<Image>();

			boolean isBackground = false;
			for (Image img : item.getValue()) {
				Image tmp = null;

				if ((int) (img.getWidth(null) * scaleFactor) > 0 && (int) (img.getHeight(null) * scaleFactor) > 0) {
					if (item.getKey().contains("boardBackground")) {
						isBackground = true;
						continue;
					}
					/* Ist das Bild ein Hexagon? */
					else if (item.getKey().contains("Hex"))
						tmp = img.getScaledInstance(hexaSize, hexaSize, Image.SCALE_SMOOTH);
//					else if (item.getKey().contains("Unit"))
//						tmp = img
//								.getScaledInstance((hexaSize * img.getWidth(null)) / 158, (hexaSize * img.getHeight(null)) / 158, Image.SCALE_SMOOTH);
					else
						tmp = img.getScaledInstance((int) (img.getWidth(null) * scaleFactor), (int) (img.getHeight(null) * scaleFactor),
								Image.SCALE_SMOOTH);
				}

				if (tmp != null)
					list.add(tmp);
			}
			if (!isBackground) {
				scaledImages.put(item.getKey(), list);
				Output.debug("debug_gfx_file_scaled", item.getKey());
			}
		}

		Output.debug("debug_gfx_scaling_images_finished");
	}

	public synchronized int hexaSize() {
		return hexaSize;
	}

	public synchronized int beginXCoordinate() {
		return beginXCoordinate;
	}

	public synchronized int beginYCoordinate() {
		return beginYCoordinate;
	}

	public synchronized int hexaHorizontalSpace() {
		return hexaHorizontalSpace;
	}

	public synchronized int hexaVerticalSpace() {
		return hexaHorizontalSpace;
	}

	// ---------------------------------------------------------------
	/* Berechnungsfunktionen */

	/**
	 * Berechne die neuen Koordinaten
	 */
	public void hexagonCalculations() {
		Board board = gui.board();
		hexaSize = (int) ((Double.parseDouble(SettingManager.getValue("GUI_BOARD_SIZE_PERCENTAGE")) * GFXManager.backgroundOriginalWidth() * scaleFactor)
				/ (board.dimX + board.dimY + 1) / 0.75);

		if ((board.dimX + board.dimY + 4) / 2 * hexaSize > Double.parseDouble(SettingManager.getValue("GUI_BOARD_SIZE_PERCENTAGE"))
				* GFXManager.backgroundOriginalHeight() * scaleFactor)
			hexaSize = (int) ((Double.parseDouble(SettingManager.getValue("GUI_BOARD_SIZE_PERCENTAGE")) * GFXManager.backgroundOriginalHeight() * scaleFactor) / ((board.dimX
					+ board.dimY + 4) / 2));

		Output.debug("debug_setting_hexasize", Integer.toString(hexaSize));

		hexaHorizontalSpace = (int) (hexaSize * 0.75);
		hexaVerticalSpace = hexaSize / 2;

		/* Berechne die Stelle des ersten Hexagons */
		beginXCoordinate = (int) (GFXManager.backgroundOriginalWidth() * scaleFactor / 2)
				- (int) ((double) (board.dimX - board.dimY) / 2 * hexaHorizontalSpace) - (hexaSize / 2);
		beginYCoordinate = (int) (GFXManager.backgroundOriginalHeight() * scaleFactor / 2) + (((board.dimX + board.dimY) / 2) - 2)
				* hexaVerticalSpace;

		/* Erzeuge Koordinaten-Arrays */
		hexaXCoordinates = new int[board.dimX + 2][board.dimY + 2];
		hexaYCoordinates = new int[board.dimX + 2][board.dimY + 2];

		/* Berechne die Positionen der Hexagons */
		for (int x = -1; x <= board.dimX; x++) {
			for (int y = -1; y <= board.dimY; y++) {
				Point p = getCoordinatesFromBoardIndex(x, y);
				hexaXCoordinates[x + 1][y + 1] = p.x;
				hexaYCoordinates[x + 1][y + 1] = p.y;
			}
		}

		hexagons = new Polygon[board.dimX][board.dimY];
		for (int x = 0; x < board.dimX; x++) {
			for (int y = 0; y < board.dimY; y++) {
				int bx = hexaXCoordinates[x + 1][y + 1];
				int by = hexaYCoordinates[x + 1][y + 1];
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
	}

	/**
	 * Liefert die Koordinaten des Hexagons nach Board-Index.
	 * 
	 * @param x
	 *            X-Index
	 * @param y
	 *            Y-Index
	 * @return Koordinaten als Move
	 */
	public synchronized Point getCoordinatesFromBoardIndex(int x, int y) {
		int pX = beginXCoordinate + (x - y) * hexaHorizontalSpace;
		int pY = beginYCoordinate - (x + y) * hexaVerticalSpace;

		return new Point(pX, pY);
	}

	public synchronized Point getBoardIndexFromCoordinates(int x, int y) {
		return getBoardIndexFromCoordinates(new Point(x, y));
	}

	public synchronized Point getBoardIndexFromCoordinates(Point p) {
		for (int x = 0; x < gui.board().dimX; x++)
			for (int y = 0; y < gui.board().dimY; y++)
				if (hexagons[x][y].contains(p))
					return new Point(x, y);
		return null;
	}

	private void loadBoardComponents() {
		Board board = gui.board();

		/* Fuege den Rand hinzu */
		/* Roter Rand */
		for (int i = 1; i <= board.dimX; i++) {
			Point p = new Point(hexaXCoordinates[i][0], hexaYCoordinates[i][0]);
			componentsSecondLevel.put("hexagon~" + p.x + "~" + p.y,
					new GraphicComponent(this, "hexagon~" + p.x + "~" + p.y, p, scaledImages.get("hexagonHexBorderRed").get(0)));
			p = new Point(hexaXCoordinates[i][board.dimY + 1], hexaYCoordinates[i][board.dimY + 1]);
			componentsSecondLevel.put("hexagon~" + p.x + "~" + p.y,
					new GraphicComponent(this, "hexagon~" + p.x + "~" + p.y, p, scaledImages.get("hexagonHexBorderRed").get(0)));
		}
		/* Blauer Rand */
		for (int i = 1; i <= board.dimY; i++) {
			Point p = new Point(hexaXCoordinates[0][i], hexaYCoordinates[0][i]);
			componentsSecondLevel.put("hexagon~" + p.x + "~" + p.y,
					new GraphicComponent(this, "hexagon~" + p.x + "~" + p.y, p, scaledImages.get("hexagonHexBorderBlue").get(0)));
			p = new Point(hexaXCoordinates[board.dimX + 1][i], hexaYCoordinates[board.dimX + 1][i]);
			componentsSecondLevel.put("hexagon~" + p.x + "~" + p.y,
					new GraphicComponent(this, "hexago~" + p.x + "~" + p.y, p, scaledImages.get("hexagonHexBorderBlue").get(0)));
		}
		/* Spezialrandpunkte */
		Point p = new Point(hexaXCoordinates[0][0], hexaYCoordinates[0][0]);
		componentsSecondLevel.put("hexagon~" + p.x + "~" + p.y, new GraphicComponent(this, "hexagon~" + p.x + "~" + p.y, p,
				image("hexagonHexBorderBothBlueRed")));
		p = new Point(hexaXCoordinates[board.dimX + 1][board.dimY + 1], hexaYCoordinates[board.dimX + 1][board.dimY + 1]);
		componentsSecondLevel.put("hexagon~" + p.x + "~" + p.y, new GraphicComponent(this, "hexagon~" + p.x + "~" + p.y, p,
				image("hexagonHexBorderBothRedBlue")));

		/* Fuege die Felder hinzu */
		if (cheatMatrix) {
			for (int x = -1; x <= board.dimX; x++) {
				for (int y = -1; y <= board.dimY; y++) {
					try {
						Image[] imgs;
						switch (board.getField(x, y)) {
						case Board.RED:
							imgs = images("cheatMatrixHexRed");
							break;
						case Board.BLUE:
							imgs = images("cheatMatrixHexBlue");
							break;
						case Board.BORDER_RED:
							imgs = images("cheatMatrixHexBorderRed");
							break;
						case Board.BORDER_BLUE:
							imgs = images("cheatMatrixHexBorderBlue");
							break;
						case Board.BORDER_BOTH_TOP:
						case Board.BORDER_BOTH_BOTTOM:
						case Board.GREEN:
							imgs = images("cheatMatrixHexBorderBoth");
							break;
						default:
							imgs = images("cheatMatrixHexEmpty");
						}
						if (imgs != null) {
							Point p2 = getCoordinatesFromBoardIndex(x, y);
							componentsSecondLevel.put("hexagon~" + p2.x + "~" + p2.y, new GraphicComponent(this, "hexagon~" + p2.x + "~" + p2.y, p2,
									imgs));
						}
					} catch (IndexOutOfBoardException e) {

					}
				}
			}
		} else {
			for (int x = -1; x <= board.dimX; x++) {
				for (int y = -1; y <= board.dimY; y++) {
					try {
						Image[] imgs;
						switch (board.getField(x, y)) {
						case Board.RED:
							imgs = images(cheatColorConfusion ? "cheatConfusionHexRainbow" : "hexagonHexRed");
							break;
						case Board.BLUE:
							imgs = images(cheatColorConfusion ? "cheatConfusionHexRainbow" : "hexagonHexBlue");
							break;
						case Board.BORDER_RED:
							if (cheatColorConfusion) 
								imgs = images("cheatConfusionHexBorderRed");
							else if (cheatLightning)
								imgs = images(Game.currentPlayer == Player.RED ? "cheatLightningHexBorderRedShiny" : "hexagonHexBorderRed");
							else imgs = images("hexagonHexBorderRed");
							break;
						case Board.BORDER_BLUE:
							if (cheatColorConfusion) 
								imgs = images("cheatConfusionHexBorderBlue");
							else if (cheatLightning)
								imgs = images(Game.currentPlayer == Player.BLUE ? "cheatLightningHexBorderBlueShiny" : "hexagonHexBorderBlue");
							else imgs = images("hexagonHexBorderBlue");
							break;
						case Board.BORDER_BOTH_TOP:
							if (cheatColorConfusion) 
								imgs = images("cheatConfusionHexBorderBothRedBlue");
							else if (cheatLightning)
								imgs = images(Game.currentPlayer == Player.RED ? "cheatLightningHexBorderBothRedShinyBlue" : "cheatLightningHexBorderBothRedBlueShiny");
							else imgs = images("hexagonHexBorderBothRedBlue");
							break;
						case Board.BORDER_BOTH_BOTTOM:
							if (cheatColorConfusion) 
								imgs = images("cheatConfusionHexBorderBothBlueRed");
							else if (cheatLightning)
								imgs = images(Game.currentPlayer == Player.RED ? "cheatLightningHexBorderBothBlueRedShiny" : "cheatLightningHexBorderBothBlueShinyRed");
							else imgs = images("hexagonHexBorderBothBlueRed");
							break;
						case Board.GREEN:
							imgs = images("hexagonHexGreen");
							break;
						case Board.EMPTY:
							imgs = images("hexagonHexBlank");
							break;
						case Board.BONUS:
							imgs = images("cheatHexBonus");
							break;
						case Board.BLACKHOLE:
							imgs = images("cheatHexBlackhole");
							break;
						case Board.BLACKHOLE_RED:
							imgs = images("cheatHexBlackholeRed");
							if (imgs != null) {
								Point ptemp1 = getCoordinatesFromBoardIndex(x, y);
								componentsSecondLevel.put("hexagon~" + ptemp1.x + "~" + ptemp1.y, new BlackHoleNeighbor(this, "hexagon~" + ptemp1.x
										+ "~" + ptemp1.y, ptemp1, new Point(x, y), board, imgs));
								imgs = null;
							}
							break;
						case Board.BLACKHOLE_BLUE:
							imgs = images("cheatHexBlackholeBlue");
							if (imgs != null) {
								Point ptemp2 = getCoordinatesFromBoardIndex(x, y);
								componentsSecondLevel.put("hexagon~" + ptemp2.x + "~" + ptemp2.y, new BlackHoleNeighbor(this, "hexagon~" + ptemp2.x
										+ "~" + ptemp2.y, ptemp2, new Point(x, y), board, imgs));
								imgs = null;
							}
							break;
						default:
							imgs = images("hexagonHexEmpty");
						}
						if (imgs != null) {
							Point p2 = getCoordinatesFromBoardIndex(x, y);
							componentsSecondLevel.put("hexagon~" + p2.x + "~" + p2.y, new GraphicComponent(this, "hexagon~" + p2.x + "~" + p2.y, p2,
									imgs));
						}
					} catch (IndexOutOfBoardException e) {

					}
				}
			}
		}
	}

	// ---------------------------------------------------------------
	/* CHEATS */

	public void cheatLightning(boolean value) {
		cheatLightning = value;
		reloadComponents(SECOND_LEVEL);
	}

	public boolean cheatLightning() {
		return cheatLightning;
	}

	public void cheatColorConfusion(boolean value) {
		cheatColorConfusion = value;
		reloadComponents(SECOND_LEVEL);
	}

	public boolean cheatColorConfusion() {
		return cheatColorConfusion;
	}

	public void cheatMatrix(boolean value) {
		cheatMatrix = value;
		reloadComponents(SECOND_LEVEL);
	}

	public boolean cheatMatrix() {
		return cheatMatrix;
	}
}
