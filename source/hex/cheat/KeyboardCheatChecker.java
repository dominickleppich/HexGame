package hex.cheat;

import hex.board.CrazyBoard;
import hex.exception.TheMostRageExceptionWhichIsSoAngryThatItWouldEatThreeCamelsWithoutKnifeAndFork;
import hex.game.Game;
import hex.game.Output;
import hex.graphic.extended.GraphicalGUI;
import hex.manager.LanguageManager;
import hex.manager.SettingManager;
import hex.manager.SoundManager;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class KeyboardCheatChecker extends KeyAdapter {
	private String enteredText = "";
	private GraphicalGUI gui;

	private static Thread insultingThread;

	// ---------------------------------------------------------------

	public KeyboardCheatChecker(GraphicalGUI gui) {
		this.gui = gui;
	}

	// ---------------------------------------------------------------

	@Override
	public void keyTyped(KeyEvent e) {
		enteredText += e.getKeyChar();

		/* Bereinige falls noetig den String */
		if (enteredText.length() > Integer.parseInt(SettingManager.getValue("MAX_KEYBOARD_TEMP"))) {
			enteredText = enteredText.substring(Integer.parseInt(SettingManager.getValue("MAX_KEYBOARD_TEMP"))
					- Integer.parseInt(SettingManager.getValue("KEYBOARD_REMEMBER_SIZE")));
			return;
		}

		/* Cheat-Abfrage */
		boolean value = false; // Cheat an oder ausgeschaltet
		String cheat = ""; // CheatName

		if (enteredText.contains("exit")) {
			System.exit(0);
		} else if (enteredText.contains("whois")) {
			gui.playerMakeMove();
			enteredText = "";
			return;
		} else if (enteredText.contains("nodebug")) {
			value = false;
			cheat = "Debug";
			Output.debug = false;
			Output.guiDebug = false;
			enteredText = "";
			return;
		} else if (enteredText.contains("debug")) {
			value = true;
			cheat = "Debug";
			Output.debug = true;
			Output.guiDebug = true;
			enteredText = "";
			return;
		} else if (enteredText.contains("help")) {
			gui.showHelp(true);
			enteredText = "";
			return;
		}

		if (Boolean.parseBoolean(SettingManager.getValue("CHEATS_ENABLED"))) {
			if (enteredText.contains("rage"))
				throw new TheMostRageExceptionWhichIsSoAngryThatItWouldEatThreeCamelsWithoutKnifeAndFork();
			else if (enteredText.contains("lightning")) {
				value = !gui.cheatLightning();
				cheat = "Lightning";
				gui.cheatLightning(value);
			} else if (enteredText.contains("insult")) {
				value = true;
				cheat = "Insult";
				cheatStartInsultingOpponent(false);
			} else if (enteredText.contains("yourmama")) {
				value = true;
				cheat = "YourMama";
				cheatStartInsultingOpponent(true);
			} else if (enteredText.contains("peace")) {
				value = false;
				cheat = "Insult";
				cheatStopInsultingOpponent();
			} else if (enteredText.contains("ecstasy")) {
				value = true;
				cheat = "ColorConfusion";
				gui.cheatColorConfusion(true);
			} else if (enteredText.contains("betterbeclean")) {
				value = false;
				cheat = "ColorConfusion";
				gui.cheatColorConfusion(false);
			} else if (enteredText.contains("matrix")) {
				value = true;
				cheat = "Matrix";
				gui.cheatMatrix(true);
			} else if (enteredText.contains("backtoreality")) {
				value = false;
				cheat = "Matrix";
				gui.cheatMatrix(false);
			} else if (enteredText.contains("bonus")) {
				if (gui.board() instanceof CrazyBoard) {
					value = true;
					cheat = "Bonus";
					Point p = null;
					int counter = 10;
					do {
						Point tmp = new Point(new Random().nextInt(gui.board().dimX), new Random().nextInt(gui.board().dimY));
						if (gui.board().isEmptyField(tmp)) {
							Point[] neighbors = gui.board().getNeighbors(tmp);
							if (neighbors == null || neighbors.length == 0)
								p = tmp;
						}
						counter--;
					} while (p == null && counter > 0);
					if (p != null) {
						((CrazyBoard) gui.board()).makeBonus(p.x, p.y);
						Game.board(gui.board());
					} else
						value = false;
				}
			} else if (enteredText.contains("blackhole")) {
				if (gui.board() instanceof CrazyBoard) {
					value = true;
					cheat = "BlackHole";
					Point p;
					do {
						p = new Point(new Random().nextInt(gui.board().dimX), new Random().nextInt(gui.board().dimY));
					} while (!gui.board().isEmptyField(p));
					((CrazyBoard) gui.board()).makeBlackHole(p.x, p.y);
					Game.board(gui.board());
				}
			} else if (enteredText.contains("hole")) {
				if (gui.board() instanceof CrazyBoard) {
					value = true;
					cheat = "Hole";
					Point p;
					do {
						p = new Point(new Random().nextInt(gui.board().dimX), new Random().nextInt(gui.board().dimY));
					} while (!gui.board().isEmptyField(p));
					((CrazyBoard) gui.board()).makeHole(p.x, p.y);
					Game.board(gui.board());
				}
			} else if (enteredText.contains("fart")) {
				SoundManager.playSound("fart");
				enteredText = "";
				return;
			} else if (enteredText.matches(".*txt.*;")) {
				Output.guiPrint("custom_text", enteredText.substring(enteredText.indexOf("txt") + 3, enteredText.length() - 1));
				enteredText = "";
				return;
			} else
				return;
			/* Cheat-String leeren */
			enteredText = "";

			/* Ausgaben machen und Cheat-Ton abspielen */
			Output.debug(value ? "cheat_activated" : "cheat_deactivated", cheat);
			gui.printText(LanguageManager.getText((value ? "cheat_activated" : "cheat_deactivated"), cheat));
			SoundManager.playSound("cheatConfirmed");
		}
	}

	// ---------------------------------------------------------------

	public void cheatStartInsultingOpponent(final boolean yourMotherSpecial) {
		if (insultingThread == null) {
			insultingThread = new InsultingThread(yourMotherSpecial ? "cheat_insult_your_mama" : "cheat_insult");
			insultingThread.start();
		}
	}

	public void cheatStopInsultingOpponent() {
		if (insultingThread != null) {
			insultingThread.interrupt();
			insultingThread = null;
		}
	}

}
