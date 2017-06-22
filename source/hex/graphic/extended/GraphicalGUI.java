package hex.graphic.extended;

import hex.board.Board;
import hex.cheat.KeyboardCheatChecker;
import hex.game.Game;
import hex.game.Output;
import hex.graphic.GUI;
import hex.graphic.GUIRefresherThread;
import hex.graphic.KeyboardKeyHunter;
import hex.graphic.WindowResizeListener;
import hex.manager.GFXManager;
import hex.manager.GraphicComponentManager;
import hex.manager.LanguageManager;
import hex.manager.SettingManager;
import hex.manager.SoundManager;
import hex.preset.Move;
import hex.preset.Player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JRootPane;

public class GraphicalGUI extends JFrame implements GUI {
	private static final long serialVersionUID = 1L;

	private GraphicComponentManager gcm;

	private Image backBuffer;

	private boolean fullscreen = false;
	private boolean inputEnabled = false;

	private Board board;

	private int oldLeft, oldTop;
	private double oldScaleFactor;

	private Move requestedMove;

	private int hexaXMouseHover = -2;
	private int hexaYMouseHover = -2;
	private int hexaXMouseHoverOld;
	private int hexaYMouseHoverOld;

	/* GUI Variablen */
	private boolean showHelp = false;
	private String printText = "";
	private String debugText = "";

	// ---------------------------------------------------------------

	public GraphicalGUI(Board board) {
		super(SettingManager.getValue("WINDOW_NAME"));
		this.board = board;

		this.setUndecorated(true);
		this.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		this.getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK));

		Output.debug("debug_gui_created");

		/* Maus-Listener fuer Mausklicks */
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				/* Reagiere nur auf Linksklick */
				if (e.getButton() == MouseEvent.BUTTON1)
					mouseClick(e.getX(), e.getY());
			}
		});
		/* Maus-Listener fuer den Maus-Hover Effekt */
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				GraphicalGUI.this.mouseMoved(e.getX(), e.getY());
			}
		});
		/* Listener fuer Tastatureingaben */
		addKeyListener(new KeyboardCheatChecker(this));

		/* Listener fuer Vollbildumschaltung nur bei einer GUI */
		addKeyListener(new KeyboardKeyHunter(this, !Boolean.parseBoolean(SettingManager.getValue("GUI_SEPERATED"))));

		this.requestFocus();

		/* Fuege den GCM ein */
		gcm = new GraphicComponentManager(this);

		/* Fuege die GUI dem GFXManager hinzu */
		GFXManager.addGUI(this);

		/* Passe die Groesse an */
		// correctSize();
		pack();

		/* Erzeuge den GUI-Refresher Thread, der die GUI aktualisiert */
		new GUIRefresherThread(this).start();

		/* Fenster-Listener fuer Groessenanpassung */
		this.addComponentListener(new WindowResizeListener(this, this.getWidth(), this.getHeight()));

		/* Setze GUI Einstellungen */
		/* Mauszeiger zu einer Hand aendern */
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		/* Fenster auf dem Bildschirm zentrieren */
		Dimension screenSize = GFXManager.screenDimension();
		oldLeft = (screenSize.width - this.getWidth()) / 2;
		oldTop = (screenSize.height - this.getHeight()) / 2;
		this.setLocation(oldLeft, oldTop);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// for (int i = 0; i < 50; i++)
		// gcm.add("mage" +i, new Mage(gcm, "mage", null, null));

		/* Mach die GUI sichtbar */
		setVisible(true);
		toFront();
	}

	public Dimension getPreferredSize() {
		Image background = gcm().image("boardBackground");
		if (background == null)
			return null;
		return new Dimension(background.getWidth(null), background.getHeight(null));
	}

	// ---------------------------------------------------------------

	/**
	 * Gib den GraphicComponentManager der GUI zurueck
	 * 
	 * @return
	 */
	public GraphicComponentManager gcm() {
		return gcm;
	}

	/**
	 * Setze das GUI Board
	 * 
	 * @param board
	 */
	public void board(Board board) {
		this.board = board;
		update();
	}

	/**
	 * Gib das Board zurueck, welches von der GUI verwendet wird
	 * 
	 * @return
	 */
	public Board board() {
		return board;
	}

	// ---------------------------------------------------------------

	/**
	 * Gebe Text grafisch auf der GUI aus
	 */
	public void printText(String s) {
		printText = s;
		Output.debug("debug_gui_printing_text", s);
		Thread t = new Thread() {
			@Override
			public void run() {
				String tmp = printText;
				try {
					Thread.sleep(Integer.parseInt(SettingManager.getValue("GUI_PRINT_TEXT_DELAY")));
				} catch (InterruptedException e) {
					Output.error("error_thread_sleep");
				}
				if (tmp == printText) {
					printText = "";
				}
			}
		};
		t.start();
		SoundManager.playSound("printText");
	}

	/**
	 * Gib den aktuellen GUI Text zurueck
	 * 
	 * @return
	 */
	public String printText() {
		return printText;
	}

	/**
	 * Schreibe den DebugText
	 * 
	 * @param text
	 */
	public void debug(String text) {
		debugText += "\n" + text;
		/* Bestimme die maximale Zeilenanzahl */
		int maxLines = gcm.image("boardBackground").getHeight(null) / (int) (Integer.parseInt(SettingManager.getValue("GUI_DEBUG_TEXT_SIZE"))) - 1;
		String[] s = debugText.split("\n");
		if (s.length > maxLines) {
			debugText = "";
			for (int i = s.length - maxLines; i < s.length; i++)
				debugText += s[i] + "\n";
		}
	}

	/**
	 * Gib den Debugtext zurueck
	 * 
	 * @return
	 */
	public String debug() {
		return debugText;
	}

	/**
	 * Fordere den Spieler zu einem Zug auf, es wird ein entsprechender Text
	 * angezeigt
	 */
	public void playerMakeMove() {
		Output.debug("debug_player_has_to_make_move",
				Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE"));
		printText(LanguageManager.getText("game_your_turn", (Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_RED")
				: SettingManager.getValue("PLAYER_BLUE")), (Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_BLUE")
				: SettingManager.getValue("PLAYER_RED"))));
	}

	/**
	 * Ist die Hilfe derzeit aktiv?
	 * 
	 * @return
	 */
	public boolean showHelp() {
		return showHelp;
	}

	/**
	 * Zeige Hilfe an oder blende sie wieder aus
	 * 
	 * @param value
	 */
	public void showHelp(boolean value) {
		showHelp = value;
	}

	// ---------------------------------------------------------------

	/**
	 * Die GUI fordert vom Spieler einen Zug an.
	 * 
	 * @return
	 */
	public synchronized Move request() {
		Output.debug("debug_gui_input_enabled");
		inputEnabled = true;
		playerMakeMove();
		if (gcm.cheatLightning())
			gcm.reloadComponents(GraphicComponentManager.SECOND_LEVEL);
		try {
			wait();
		} catch (InterruptedException e) {
			Output.error("error_gui_input", e.toString());
		}
		Output.debug("debug_gui_input_disabled");
		inputEnabled = false;
		SoundManager.playSound("mouseClick");
		return requestedMove;
	}

	/**
	 * Zug wurde gemacht, schlafender Thread wird geweckt
	 */
	private synchronized void moveDone() {
		notify();
	}

	/**
	 * Ist die grafische Eingabe aktiviert?
	 * 
	 * @return
	 */
	public boolean inputEnabled() {
		return inputEnabled;
	}

	/**
	 * Mache auf dieser GUI eine Eingabe
	 * 
	 * @param m
	 */
	public void makeMove(Move m) {
		requestedMove = m;
		moveDone();
	}

	// ---------------------------------------------------------------

	/**
	 * Wechsle zwischen Fenster- und Vollbildmodus
	 */
	public void switchFullscreen() {
		if (!fullscreen) {
			Point p = this.getLocation();
			if (p.x != 0 || p.y != 0) {
				oldLeft = p.x;
				oldTop = p.y;
				oldScaleFactor = gcm.scaleFactor();
			}
			this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
			Dimension screenDimension = GFXManager.screenDimension();
			gcm.scaleFactor((double) screenDimension.getWidth() / GFXManager.backgroundOriginalWidth());
			fullscreen = true;
			setLocation(0, 0);
		} else {
			this.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
			gcm.scaleFactor(oldScaleFactor);
			fullscreen = false;
			setLocation(oldLeft, oldTop);
		}
		pack();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		toFront();
	}

	@Override
	public void scaleFactor(double scaleFactor) {
		gcm.scaleFactor(scaleFactor);
	}

	public void update(boolean color, Move move) {
		if (move == null)
			return;

		Point p = gcm.getCoordinatesFromBoardIndex(move.getX(), move.getY());
		GraphicComponent gc = gcm.component(GraphicComponentManager.SECOND_LEVEL, "hexagon", p);
		if (gc == null)
			return;

		if (gcm.cheatMatrix())
			gc.frames(gcm.images("cheatMatrixHex" + (color == Player.RED ? "Red" : "Blue")));
		else if (gcm.cheatColorConfusion())
			gc.frames(gcm.images("cheatConfusionHexRainbow"));
		else
			gc.frames(gcm.images("hexagonHex" + (color == Player.RED ? "Red" : "Blue")));
	}

	/**
	 * Aktualisiere alle Komponenten
	 */
	public void update() {
		gcm.reloadComponents(GraphicComponentManager.SECOND_LEVEL);
	}
	
	public void pack() {
		super.pack();
		
		createBackBuffer();
	}

	// ---------------------------------------------------------------

	/* Cheats */
	public void cheatLightning(boolean value) {
		gcm.cheatLightning(value);
	}

	public boolean cheatLightning() {
		return gcm.cheatLightning();
	}

	public void cheatColorConfusion(boolean value) {
		gcm.cheatColorConfusion(value);
	}

	public boolean cheatColorConfusion() {
		return gcm.cheatColorConfusion();
	}

	public void cheatMatrix(boolean value) {
		gcm.cheatMatrix(value);
	}

	public boolean cheatMatrix() {
		return gcm.cheatMatrix();
	}

	// ---------------------------------------------------------------

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

		/* Falls Hilfetext angezeigt wird, schalte diesen ab */
		if (showHelp) {
			showHelp = false;
			return;
		}

		/* Tue nichts, falls die GUI inaktiv ist */
		if (!inputEnabled) {
			Output.debug("debug_gui_input_not_enabled");
			return;
		}

		if (x < 0 && y < 0) {
			requestedMove = null;
			moveDone();
			return;
		}

		Point p = gcm.getBoardIndexFromCoordinates(x, y);
		if (p != null && board.isEmptyField(p)) {
			requestedMove = new Move(p.x, p.y);
			hexaXMouseHover = -2;
			hexaYMouseHover = -2;
		}

		if (requestedMove != null)
			moveDone();
	}

	/**
	 * Regiere auf Mausbewegung, indem das aktive Feld in der aktuellen Farbe
	 * hervorgehoben wird
	 * 
	 * @param x
	 * @param y
	 */
	public void mouseMoved(int x, int y) {
		if (showHelp)
			return;

		Point p = gcm.getBoardIndexFromCoordinates(x, y);
		if (p != null && board.isEmptyField(p)) {
			hexaXMouseHover = p.x;
			hexaYMouseHover = p.y;
		} else {
			hexaXMouseHover = -2;
			hexaYMouseHover = -2;
		}
	}

	// ---------------------------------------------------------------

	private void createBackBuffer() {
		backBuffer = createImage(getWidth(), getHeight());
	}

	public void paintComponent(Graphics g) {
		updateScreen();
	}

	public void updateScreen() {
		Graphics g = getGraphics();
		if (g != null)
		// component already visible?
		{
			// is there a backBuffer to draw?
			if (backBuffer != null)
				g.drawImage(backBuffer, 0, 0, null);
			else {
				// if not, create one and render on it
				createBackBuffer();
				renderScreen();
			}
		}
	}

	public void renderScreen() {
		if (backBuffer == null)
			createBackBuffer();

		/* Hole ein Graphics2D Objekt */
		Graphics2D g = (Graphics2D) backBuffer.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		/*
		 * Falls der Matrix Cheat aktiv ist, zeichne zufaellige 0 und 1 in gruen
		 * auf schwarzen Hintergrund
		 */
		if (gcm.cheatMatrix()) {
			drawMatrix(g);
		} else
			drawFull(g, "boardBackground");

		drawScores(g);

		drawComponents(g);

		/* Mouse-Hover Effekt */
		if (inputEnabled && hexaXMouseHover >= 0 && hexaYMouseHover >= 0)
			drawMouseHover(g);

		/* Mache Text-Ausgabe, falls erforderlich */
		if (printText != "") {
			printText(g);
		}

		/* Bei Inaktivitaet im MultiGUI-Modus abdunkeln */
		if (inputEnabled && Boolean.parseBoolean(SettingManager.getValue("GUI_SEPERATED")))
			drawFull(g, "dark");

		/* Eventuelle Debug-Ausgabe */
		if (Output.guiDebug)
			printDebug(g);

		/* Hilfe anzeigen */
		if (showHelp)
			printHelp(g);
	}

	/**
	 * Zeichne ein Bild in der gesamten GUI
	 * 
	 * @param g
	 * @param identifier
	 */
	private void drawFull(Graphics2D g, String identifier) {
		Image img = gcm.image(identifier);
		if (img != null)
			g.drawImage(img, 0, 0, this);
	}

	/**
	 * Zeichnet die bunten Teppiche mit den Punktestaenden
	 * 
	 * @param g
	 */
	private void drawScores(Graphics2D g) {
		int redScore = Game.score(Player.RED);
		int blueScore = Game.score(Player.BLUE);

		if (redScore > 0 || blueScore > 0) {
			Image redScoreLayer = gcm.image("redScore");
			Image blueScoreLayer = gcm.image("blueScore");

			float stringSize = (float) redScoreLayer.getWidth(null) / 2;

			if (gcm.cheatMatrix()) {
				/* Setze Schriftfarbe und Schrift */
				g.setColor(Color.GREEN);
				Font textFont = new Font(Font.MONOSPACED, Font.PLAIN, (int) stringSize);
				g.setFont(textFont);

				int width = gcm.image("boardBackground").getWidth(null);

				g.setStroke(new BasicStroke(6f));

				if (redScoreLayer != null) {
					String s = Integer.toString(redScore);
					g.drawRect(width / 6 - redScoreLayer.getWidth(null) / 2, 0, redScoreLayer.getWidth(null), redScoreLayer.getHeight(null));
					g.drawString(s, width / 6 - g.getFontMetrics().stringWidth(s) / 2,
							(int) (redScoreLayer.getHeight(null) * Double.parseDouble(SettingManager.getValue("GUI_SCORE_POSITION_FROM_TOP"))));
				}
				if (blueScoreLayer != null) {
					String s = Integer.toString(blueScore);
					g.drawRect((width * 5) / 6 - blueScoreLayer.getWidth(null) / 2, 0, blueScoreLayer.getWidth(null), blueScoreLayer.getHeight(null));
					g.drawString(s, (width * 5) / 6 - g.getFontMetrics().stringWidth(s) / 2,
							(int) (redScoreLayer.getHeight(null) * Double.parseDouble(SettingManager.getValue("GUI_SCORE_POSITION_FROM_TOP"))));
				}
			} else {
				/* Setze Schriftfarbe und Schrift */
				g.setColor(Color.decode(SettingManager.getValue("GUI_SCORE_COLOR")));
				Font textFont = new Font(SettingManager.getValue("GUI_SCORE_FONT_NAME"), Integer.parseInt(SettingManager
						.getValue("GUI_SCORE_FONT_TYPE")), (int) stringSize);
				g.setFont(textFont);

				int width = gcm.image("boardBackground").getWidth(null);

				if (redScoreLayer != null) {
					String s = Integer.toString(redScore);
					g.drawImage(redScoreLayer, width / 6 - redScoreLayer.getWidth(null) / 2, 0, this);
					g.drawString(s, width / 6 - g.getFontMetrics().stringWidth(s) / 2,
							(int) (redScoreLayer.getHeight(null) * Double.parseDouble(SettingManager.getValue("GUI_SCORE_POSITION_FROM_TOP"))));
				}
				if (blueScoreLayer != null) {
					String s = Integer.toString(blueScore);
					g.drawImage(blueScoreLayer, (width * 5) / 6 - blueScoreLayer.getWidth(null) / 2, 0, this);
					g.drawString(s, (width * 5) / 6 - g.getFontMetrics().stringWidth(s) / 2,
							(int) (redScoreLayer.getHeight(null) * Double.parseDouble(SettingManager.getValue("GUI_SCORE_POSITION_FROM_TOP"))));
				}
			}
		}
	}

	/**
	 * Zeichne den Maus-Hover Effekt
	 * 
	 * @param g
	 */
	private void drawMouseHover(Graphics2D g) {
		/* Nicht im MatrixModus */
		if (gcm.cheatMatrix())
			return;

		if (hexaXMouseHover != hexaXMouseHoverOld || hexaYMouseHover != hexaYMouseHoverOld) {
			hexaXMouseHoverOld = hexaXMouseHover;
			hexaYMouseHoverOld = hexaYMouseHover;
			SoundManager.playSound("mouseHover");
		}
		Point p = gcm.getCoordinatesFromBoardIndex(hexaXMouseHover, hexaYMouseHover);
		Image img = gcm.image((Game.currentPlayer == Player.RED ? "hexagonHexRedHover" : "hexagonHexBlueHover"));
		if (img != null)
			g.drawImage(img, p.x, p.y, this);
	}

	/**
	 * Zeichne alle Komponenten aus dem GraphicComponentManager auf die GUI
	 * 
	 * @param g
	 */
	private void drawComponents(Graphics g) {
		Iterator<GraphicComponent> iterator = gcm.components(GraphicComponentManager.SECOND_LEVEL);
		while (iterator.hasNext()) {
			GraphicComponent current = iterator.next();
			if (current == null)
				continue;

			Point p = current.point();
			Image image = current.frame();
			if (p == null || image == null)
				continue;
			g.drawImage(image, p.x, p.y, this);
		}

		iterator = gcm.components(GraphicComponentManager.FIRST_LEVEL);
		while (iterator.hasNext()) {
			GraphicComponent current = iterator.next();
			if (current == null)
				continue;

			Point p = current.point();
			Image image = current.frame();
			if (p == null || image == null)
				continue;
			g.drawImage(image, p.x, p.y, this);
		}
	}

	/**
	 * Zeichne den Matrix-Hintergrund
	 * 
	 * @param g
	 */
	private void drawMatrix(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.GREEN);
		g.setFont(g.getFont().deriveFont(Float.parseFloat(SettingManager.getValue("MATRIX_CHAR_SIZE"))));
		Random r = new Random();
		for (int i = 0; i < Integer.parseInt(SettingManager.getValue("MATRIX_ROWS")); i++) {
			String s = "";
			for (int j = 0; j < Integer.parseInt(SettingManager.getValue("MATRIX_COLS")); j++)
				s += (r.nextBoolean() ? "1" : "0");
			g.drawString(s, -2, (int) (i * Integer.parseInt(SettingManager.getValue("MATRIX_CHAR_SIZE"))) - 1);
		}
	}

	private void printText(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (Output.guiPrint) {
			/* Zeichne schwarze Schattierung */
			Image boardBar = gcm.image("boardBar");
			if (boardBar != null)
				g.drawImage(boardBar, 0, (int) (this.getHeight() * Double.parseDouble(SettingManager.getValue("GUI_PRINT_TEXT_POSITION_FROM_TOP"))),
						this);

			float stringSize = (float) (boardBar.getHeight(null) * Double.parseDouble(SettingManager
					.getValue("GUI_PRINT_TEXT_FONT_NORMAL_SIZE_FACTOR")));

			/* Setze Schriftfarbe und Schrift */
			g.setColor(Color.decode(SettingManager.getValue("GUI_PRINT_TEXT_COLOR")));
			Font textFont = new Font(SettingManager.getValue("GUI_PRINT_TEXT_FONT_NAME"), Integer.parseInt(SettingManager
					.getValue("GUI_PRINT_TEXT_FONT_TYPE")), (int) stringSize);
			g.setFont(textFont);

			/* Berechne ideale Schriftgroesse */
			/*
			 * Es wird ausgehend von der "normalen" Schriftgroesse verkleinert
			 * falls noetig
			 */
			while (g.getFontMetrics().stringWidth(printText) > (int) (Double.parseDouble(SettingManager.getValue("GUI_PRINT_TEXT_WIDTH_PERCENTAGE")) * this
					.getWidth())) {
				stringSize = (float) (g.getFont().getSize() * Double
						.parseDouble(SettingManager.getValue("GUI_PRINT_TEXT_FONT_SMALLER_SIZING_FACTOR")));
				g.setFont(g.getFont().deriveFont(stringSize));

			}

			/* Berechne Text-Koordinaten */
			int stringX, stringY;
			stringX = this.getWidth() / 2 - g.getFontMetrics().stringWidth(printText) / 2;
			stringY = (int) (this.getHeight() * Double.parseDouble(SettingManager.getValue("GUI_PRINT_TEXT_POSITION_FROM_TOP"))
					+ boardBar.getHeight(null) / 2 + stringSize / 2);

			/* Schreibe Text */
			g.drawString(printText, stringX, stringY);
		} else {
			Image consoleLine = gcm.image("consoleline");
			/* Setze Schriftfarbe und Schrift */
			g.setColor(Color.decode(SettingManager.getValue("GUI_PRINT_TEXT_COLOR")));
			Font textFont = new Font(SettingManager.getValue("GUI_PRINT_TEXT_FONT_NAME"), Integer.parseInt(SettingManager
					.getValue("GUI_PRINT_TEXT_FONT_TYPE")), (int) (consoleLine.getHeight(null) * 0.666));
			g.setFont(textFont);

			int stringX, stringY;
			stringX = this.getWidth() / 2 - g.getFontMetrics().stringWidth(printText) / 2;
			stringY = this.getHeight() - (int) (consoleLine.getHeight(null) * 0.333);

			g.drawImage(consoleLine, 0, this.getHeight() - consoleLine.getHeight(null), this);
			g.drawString(printText, stringX, stringY);
		}
	}

	private void printDebug(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.decode(SettingManager.getValue("GUI_DEBUG_TEXT_COLOR")));
		Font debugFont = new Font(SettingManager.getValue("GUI_DEBUG_TEXT_FONT_NAME"), Integer.parseInt(SettingManager
				.getValue("GUI_DEBUG_TEXT_FONT_TYPE")), Integer.parseInt(SettingManager.getValue("GUI_DEBUG_TEXT_SIZE")));
		g.setFont(debugFont);

		/* Gib den Debug-Text aus */
		String[] s = debugText.split("\n");
		int y = Integer.parseInt(SettingManager.getValue("GUI_DEBUG_TOP_SPACE"));
		for (int i = 0; i < s.length; i++)
			g.drawString(s[i], Integer.parseInt(SettingManager.getValue("GUI_DEBUG_LEFT_SPACE")),
					y + (int) (i * Integer.parseInt(SettingManager.getValue("GUI_DEBUG_TEXT_SIZE"))));
	}

	private void printHelp(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		/* Abdunkeln */
		drawFull(g, "dark");

		/* Hilfetext holen */
		String helpText = "";
		try {
			for (int i = 0; i < Integer.parseInt(LanguageManager.getText("help_text_count")); i++)
				helpText += LanguageManager.getText("help_text_" + Integer.toString(i + 1)) + "\n";
		} catch (NumberFormatException e) {
		}

		/* Text anzeigen */
		g.setColor(Color.decode(SettingManager.getValue("GUI_HELP_TEXT_COLOR")));
		Font debugFont = new Font(SettingManager.getValue("GUI_HELP_TEXT_FONT_NAME"), Integer.parseInt(SettingManager
				.getValue("GUI_HELP_TEXT_FONT_TYPE")), Integer.parseInt(SettingManager.getValue("GUI_HELP_TEXT_SIZE")));
		g.setFont(debugFont);

		String[] s = helpText.split("\n");

		int y = Integer.parseInt(SettingManager.getValue("GUI_HELP_TOP_SPACE"));
		for (int i = 0; i < s.length; i++)
			g.drawString(s[i], Integer.parseInt(SettingManager.getValue("GUI_HELP_LEFT_SPACE")),
					y + (int) (i * Integer.parseInt(SettingManager.getValue("GUI_HELP_TEXT_SIZE"))));
		g.drawString("Programming: Dominick Leppich     Graphic: Philip Langer", Integer.parseInt(SettingManager.getValue("GUI_HELP_LEFT_SPACE")), y
				+ (int) ((s.length + 1) * Integer.parseInt(SettingManager.getValue("GUI_HELP_TEXT_SIZE"))));
	}
}
