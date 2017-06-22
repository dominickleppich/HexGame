package hex.game;

import hex.board.Board;
import hex.board.CrazyBoard;
import hex.graphic.GUI;
import hex.graphic.extended.GraphicalGUI;
import hex.graphic.simple.SimpleGUI;
import hex.manager.SettingManager;
import hex.manager.SoundManager;
import hex.player.HumanPlayer;
import hex.player.NetworkPlayer;
import hex.player.ai.EasyAI;
import hex.player.ai.RandomAI;
import hex.preset.Player;

import java.util.Random;

/**
 * Diese Klasse wird zum Verwalten des Spiels verwendet. Die Spielerauswahl
 * sowie Dimensionsgroesse werden organisiert.
 * 
 * @author nepster
 * 
 */
public class Game {
	private static GameThread gameThread;

	public static final int noWinner = 0;
	public static final int redWinner = 1;
	public static final int blueWinner = 2;

	private static int redWon = 0;
	private static int blueWon = 0;

	private static int lastWinner = 0;
	private static int lastLastWinner = 0;
	private static int lastLastLastWinner = 0;
	
	public static boolean currentPlayer;

	public static boolean firstGame = true;
	public static boolean running = true;

	private static String redType, blueType;
	private static Player red, blue;
	private static GUI redgui, bluegui;
	private static boolean beginPlayer;
	private static Board board;
	private static GUI gui = null;

	// ---------------------------------------------------------------

	public static void createGame() {
		createGame(new Random().nextBoolean());
	}

	public static void createGame(boolean beginPlayer) {
		/* Falls schon eine Spielinstanz existiert, brich ab */
		if (gameThread != null)
			return;

		Game.beginPlayer = beginPlayer;

		if (Boolean.parseBoolean(SettingManager.getValue("CHEATS_ENABLED"))) 
			board = new CrazyBoard(beginPlayer);
		else 
			board = new Board(beginPlayer);
		
		if (Boolean.parseBoolean(SettingManager.getValue("GUI_ENABLED"))) {
			if (!Boolean.parseBoolean(SettingManager.getValue("GUI_SEPERATED"))) {
				if (Boolean.parseBoolean(SettingManager.getValue("GUI_SIMPLE")))
					gui = new SimpleGUI(board);
				else
					gui = new GraphicalGUI(board);
			}
		}

		Output.debug("debug_player_will_begin",
				(beginPlayer == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE")));
	}

	// ---------------------------------------------------------------

	/* Funktionen zum Erzeugen der Spieler */

	public static void addPlayer(boolean color, String type) {
		boolean seperatedGUIs = Boolean.parseBoolean(SettingManager.getValue("GUI_SEPERATED"));
		boolean guiEnabled = Boolean.parseBoolean(SettingManager.getValue("GUI_ENABLED"));

		/* Gibt es die GUI schon? */
		if (!seperatedGUIs && guiEnabled && gui == null) {
			Output.error("error_gui_missing");
			return;
		}

		/* Dieser Spieler existiert bereits */
		if (color == Player.RED && red != null || color == Player.BLUE && blue != null) {
			Output.error("error_player_already_exists");
			return;
		}

		if (type.compareTo("HUMAN") == 0) {
			GUI g = null;
			if (Boolean.parseBoolean(SettingManager.getValue("GUI_ENABLED"))) {
				if (Boolean.parseBoolean(SettingManager.getValue("GUI_SEPERATED"))) {
					if (Boolean.parseBoolean(SettingManager.getValue("GUI_SIMPLE")))
						g = new SimpleGUI(board);
					else
						g = new GraphicalGUI(board);
				} else
					g = gui;
			}
			if (color == Player.RED) {
				red = new HumanPlayer(board.clone(), g, color);
				redgui = g;
				redType = type;
			} else {
				blue = new HumanPlayer(board.clone(), g, color);
				bluegui = g;
				blueType = type;
			}
			Output.debug("debug_player_human_added",
					(color == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE")));
		} else if (type.compareTo("NETWORK") == 0) {
			try {
				if (color == Player.RED) {
					red = new NetworkPlayer(NetworkPlayer.RED);
					redType = type;
				} else {
					blue = new NetworkPlayer(NetworkPlayer.BLUE);
					blueType = type;
				}
				Output.debug("debug_player_computer_added",
						(color == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE")));
			} catch (hex.player.NetworkPlayer.NetworkPlayerException e) {
				Output.error("error_networkplayer_exception", e.toString());
			}
		} else if (type.compareTo("COMPUTER_RANDOMAI") == 0) {
			if (color == Player.RED) {
				red = new RandomAI(board.clone(), color);
				redType = type;
			} else {
				blue = new RandomAI(board.clone(), color);
				blueType = type;
			}
			Output.debug("debug_player_computer_added",
					(color == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE")));
		} else if (type.compareTo("COMPUTER_EASYAI") == 0) {
			if (color == Player.RED) {
				red = new EasyAI(board.clone(), color);
				redType = type;
			} else {
				blue = new EasyAI(board.clone(), color);
				blueType = type;
			}
			Output.debug("debug_player_computer_added",
					(color == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE")));
		} else {
			Output.error("error_unknown_player_type");
		}
	}

	// ---------------------------------------------------------------

	/* Hilfsmethoden */

	private static boolean ready() {
		if (red == null || blue == null)
			return false;

		return true;
	}

	public static void switchPlayers() {
		if (gameThread != null)
			gameThread.switchPlayer(redgui, bluegui);
	}

	public static Board getCurrentBoard() {
		if (gameThread == null)
			return null;

		return gameThread.getCurrentBoard();
	}
	
	public static void board(Board board) {
		Game.board = board;
		if (gameThread != null)
			gameThread.board(board);
	}
 
	public static int score(boolean color) {
		if (color == Player.RED)
			return redWon;
		else
			return blueWon;
	}
	
	public static void setWinner(int winner) {
		if (winner == redWinner)
			redWon++;
		else
			blueWon++;

		Output.print("game_player_won", (winner == redWinner ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE")),
				Integer.toString(winner == redWinner ? redWon : blueWon));

		if (winner == lastWinner && winner == lastLastWinner && winner == lastLastLastWinner)
			SoundManager.playSound("voiceUnstoppable");
		else if (winner == lastWinner && winner == lastLastWinner)
			SoundManager.playSound("voiceTriplekill");
		else if (winner == lastWinner)
			SoundManager.playSound("voiceDoublekill");

		/* FirstBlood Ansage */
		if (firstGame)
			SoundManager.playSound("voiceFirstBlood");

		/* Merke die Vergangenheit */
		lastLastLastWinner = lastLastWinner;
		lastLastWinner = lastWinner;
		lastWinner = winner;
	}

	// ---------------------------------------------------------------

	public static void start() {
		if (!ready()) {
			Output.error("Not ready to start game!");
			return;
		}

		currentPlayer = beginPlayer;
		
		if (Boolean.parseBoolean(SettingManager.getValue("GAME_MULTI_GAMES"))) {
			while (running) {
				if (board == null) {
					if (Boolean.parseBoolean(SettingManager.getValue("CHEATS_ENABLED"))) 
						board = new CrazyBoard(beginPlayer);
					else 
						board = new Board(beginPlayer);
					if (redgui != null)
						redgui.board(board);
					if (bluegui != null)
						bluegui.board(board);
				}
				if (red == null && redType != "")
					addPlayer(Player.RED, redType);
				if (blue == null && blueType != "")
					addPlayer(Player.BLUE, blueType);
				gameThread = new GameThread(board, red, blue);
				currentPlayer = beginPlayer;
				gameThread.start();
				try {
					gameThread.join();
					gameThread = null;
					board = null;
					red = null;
					blue = null;
					firstGame = false;
					beginPlayer = !beginPlayer; // Der andere Spieler beginnt
				} catch (InterruptedException e) {
				}
			}
		} else {
			gameThread = new GameThread(board, red, blue);
			gameThread.start();
		}
	}
}
