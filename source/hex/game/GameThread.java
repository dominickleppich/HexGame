package hex.game;

import hex.board.Board;
import hex.board.Chain;
import hex.board.CrazyBoard;
import hex.exception.IllegalGameSequenceException;
import hex.graphic.GUI;
import hex.manager.GFXManager;
import hex.manager.LanguageManager;
import hex.manager.SettingManager;
import hex.player.AbstractPlayer;
import hex.preset.Move;
import hex.preset.Player;
import hex.preset.Status;

import java.rmi.RemoteException;
import java.util.Iterator;

public class GameThread extends Thread {
	private Player red, blue;
	private Status currentStatus;

	/*
	 * Variablen zum erkennen des zweiten Zuges, damit der zweite Spieler die
	 * Moeglichkeit hat den ersten Stein zu uebernehmen
	 */
	private boolean firstMove = true;
	private boolean secondMove = false;

	private Board board;

	// ---------------------------------------------------------------

	public GameThread(Board board, Player red, Player blue) {
		this.board = board;
		this.red = red;
		this.blue = blue;
	}

	// ---------------------------------------------------------------

	/* Hilfsmethoden */

	public void board(Board board) {
		this.board = board;

		Iterator<GUI> iterator = GFXManager.guis();
		if (iterator != null)
			while (iterator.hasNext())
				iterator.next().board(board);
	}

	/**
	 * Wechsle zum naechsten Spieler
	 */
	private void nextPlayer() {
		Game.currentPlayer = !Game.currentPlayer;
	}

	/**
	 * Tausche die beiden Spieler aus
	 */
	private void switchPlayers() {
		/* Tausche Spielernamen */
		String tmpString = SettingManager.getValue("PLAYER_RED");
		SettingManager.setValue("PLAYER_RED", SettingManager.getValue("PLAYER_BLUE"));
		SettingManager.setValue("PLAYER_BLUE", tmpString);

		/* Tausche Spieler */
		Player tmpPlayer = red;
		red = blue;
		blue = tmpPlayer;

		/* Passe Spielerfarben an */
		if (red instanceof AbstractPlayer)
			((AbstractPlayer) red).switchColor();
		if (blue instanceof AbstractPlayer)
			((AbstractPlayer) blue).switchColor();

		/* Passe Spielsequenz-Reihenfolge an */

		if (red instanceof AbstractPlayer)
			((AbstractPlayer) red).repairGameSequence();

		if (blue instanceof AbstractPlayer)
			((AbstractPlayer) blue).repairGameSequence();

		/*
		 * Es soll nicht gewechselt werden, daher muss ein zweiter
		 * Spielerwechsel durchgefuehrt werden
		 */
		nextPlayer();

		Output.debug("debug_player_switched", SettingManager.getValue("PLAYER_RED"), SettingManager.getValue("PLAYER_BLUE"));
	}

	/**
	 * Wechsle den Spieler
	 * 
	 * @param redgui
	 *            GUI des roten Spielers
	 * @param bluegui
	 *            GUI des blauen Spielers
	 */
	public void switchPlayer(GUI redgui, GUI bluegui) {
		GUI currentGUI = (Game.currentPlayer == Player.RED ? redgui : bluegui);
		if (currentGUI != null)
			currentGUI.makeMove(null);
	}

	// ---------------------------------------------------------------

	/**
	 * Gib eine aktuelle Kopie des Game-Boards
	 * 
	 * @return Board
	 */
	public Board getCurrentBoard() {
		return board.clone();
	}

	// ---------------------------------------------------------------

	/**
	 * In dieser Methode laueft das eigentliche Spiel ab. Es werden abwechselnd
	 * von beiden Spielern Zuege angefordert und dann auf dem Spieleigenen Board
	 * ausgefuehrt und dann bestaetigt und beim Gegner geupdated.
	 */
	@Override
	public void run() {
		int guiPrintTextDelay = Integer.parseInt(SettingManager.getValue("GUI_PRINT_TEXT_DELAY"));
		if (Game.firstGame) {
			/* Welcome-Nachrichten */
			try {
				int count = Integer.parseInt(LanguageManager.getText("welcome_message_count"));
				for (int i = 0; i < count; i++) {
					Output.guiPrint("welcome_message_" + Integer.toString(i + 1));
					Thread.sleep(guiPrintTextDelay);
				}
				if (count > 0)
					Thread.sleep(guiPrintTextDelay);
			} catch (NumberFormatException e) {
			} catch (InterruptedException e) {
				Output.error("error_interrupted_exception", e.toString());
			}
		}

		try {
			if (Game.firstGame) {
				Output.print("game_started");
				Output.guiPrint("game_started");
				Thread.sleep(guiPrintTextDelay);
			} else {
				Thread.sleep(Integer.parseInt(SettingManager.getValue("GAME_PAUSE")));
				Output.print("game_restarted");
				Output.guiPrint("game_restarted");
				Thread.sleep(guiPrintTextDelay);
			}

			do {
				/* Board-Ausgabe */
				if (!Boolean.parseBoolean(SettingManager.getValue("GUI_ENABLED")))
					Output.print("custom_text", board.toString());
				else
					/* Debug-Ausgabe des Boards */
					Output.debug("debug_print_board", "\n" + board.toString());

				/* Naechster Spieler */
				if (!firstMove)
					nextPlayer();

				Move move;
				/*
				 * Fordere je nach Spieler einen Zug an, fuehre ihn auf dem
				 * eigenen Board aus, dann confirm und update beim Gegner
				 */
				if (Game.currentPlayer == Player.RED) {
					do {
						Output.print("game_player_has_to_make_move", SettingManager.getValue("PLAYER_RED"));
						move = red.request();
						/* Wechsle Spieler */
						if (move == null && secondMove)
							switchPlayers();
						else {
							if (move == null)
								currentStatus = new Status(Status.BLUEWIN);
							else {
								currentStatus = board.makeMove(move);
								red.confirm(currentStatus);
							}
						}
					} while (currentStatus.isILLEGAL());
					if (move != null)
						blue.update(move, currentStatus);
				} else {
					do {
						Output.print("game_player_has_to_make_move", SettingManager.getValue("PLAYER_BLUE"));
						move = blue.request();
						/* Wechsle Spieler */
						if (move == null && secondMove)
							switchPlayers();
						else {
							if (move == null)
								currentStatus = new Status(Status.REDWIN);
							else {
								currentStatus = board.makeMove(move);
								blue.confirm(currentStatus);
							}
						}
					} while (currentStatus.isILLEGAL());
					if (move != null)
						red.update(move, currentStatus);
				}

				if (firstMove) {
					firstMove = false;
					secondMove = true;
				} else if (secondMove)
					secondMove = false;

				if (move != null) {
					/* GUIs aktualisieren */
					Iterator<GUI> iterator = GFXManager.guis();
					if (CrazyBoard.allUpdate) {
						if (iterator != null)
							while (iterator.hasNext())
								iterator.next().update();
					} else if (iterator != null)
						while (iterator.hasNext())
							iterator.next().update(Game.currentPlayer, move);
				}

				/*
				 * Das Spiel laueft solange der Status nicht Error ist oder
				 * einer der beiden Spieler gewonnen hat
				 */
			} while (!currentStatus.isERROR() && !currentStatus.isREDWIN() && !currentStatus.isBLUEWIN());
		} catch (IllegalGameSequenceException e) {
			Output.error("error_illegal_game_sequence_exception", e.toString());
		} catch (RemoteException e) {
			Output.error("error_remote_exception", e.toString());
		} catch (NullPointerException e) {
			Output.error("error_nullpointer_exception", e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			Output.error("error_exception", e.toString());
		}
		if (currentStatus.isREDWIN() || currentStatus.isBLUEWIN()) {
			/* Gewinnverwaltung */
			Game.setWinner(currentStatus.isREDWIN() ? Game.redWinner : Game.blueWinner);

			Chain winningChain = board.getWinningChain();
			String winner, loser;
			winner = (currentStatus.isREDWIN() ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE"));
			loser = (currentStatus.isREDWIN() ? SettingManager.getValue("PLAYER_BLUE") : SettingManager.getValue("PLAYER_RED"));
			if (winningChain != null) {
				Output.debug("debug_winning_chain", winningChain.toString());

				Iterator<GUI> iterator = GFXManager.guis();
				if (iterator != null)
					while (iterator.hasNext())
						iterator.next().update();

				Output.print("game_won_normal", winner, loser);
				Output.guiPrint("game_won_normal", winner, loser);
			} else {
				Output.print("game_won_surrender", winner, loser);
				Output.guiPrint("game_won_surrender", winner, loser);

				/* Bei Aufgabe folgen keine weiteren Spiele mehr */
				Game.running = false;
			}
			try {
				Thread.sleep(guiPrintTextDelay);
				Thread.sleep(Integer.parseInt(SettingManager.getValue("GAME_TIMEOUT")));
			} catch (InterruptedException e) {
				Output.error("error_interrupted_exception", e.toString());
			}
		}

		Output.debug("debug_game_status", currentStatus.toString());

		if (!Boolean.parseBoolean(SettingManager.getValue("GAME_MULTI_GAMES"))) {
			/* Goodbye-Nachrichten */
			try {
				for (int i = 0; i < Integer.parseInt(LanguageManager.getText("goodbye_message_count")); i++) {
					Output.guiPrint("goodbye_message_" + Integer.toString(i + 1));
					Thread.sleep(guiPrintTextDelay);
				}

				Thread.sleep(guiPrintTextDelay);

				Output.print("game_end");
				Output.guiPrint("game_end");
				Thread.sleep(guiPrintTextDelay);
				System.exit(0);
			} catch (InterruptedException e) {
			}
		}
	}
}
