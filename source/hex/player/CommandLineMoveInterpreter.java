package hex.player;

import hex.board.Board;
import hex.game.Game;
import hex.game.Output;
import hex.manager.LanguageManager;
import hex.manager.SettingManager;
import hex.preset.Move;
import hex.preset.Player;

import java.util.Scanner;

public class CommandLineMoveInterpreter {
	/**
	 * Fordere vom Spieler ein Zug auf der Kommandozeile an
	 * @param color Spielerfarbe
	 * @return
	 */
	public static Move request(Board board) {
		String playerName = (Game.currentPlayer == Player.RED ? SettingManager.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE"));
		
		boolean goodMove = true;
		
		@SuppressWarnings("resource")
		Scanner rd = new Scanner(System.in);
		
		do {
			Output.print("console_make_move", playerName);
			if (!goodMove) {
				for (int i = 1; i <= Integer.parseInt(LanguageManager.getText("console_help_count")); i++)
					Output.print("console_help_" + i);
			}
			
			String line = rd.nextLine();
			String t = delimiter(line);
			
			if (t == null) {
				goodMove = false;
				continue;
			}
			
			String[] split = line.split(t);
			try {
				int x, y;
				if (Character.isLetter(split[0].charAt(0))) {
					char c = Character.toLowerCase(split[0].charAt(0));
					x = c - 'a';
				} else
					x = Integer.parseInt(split[0]) - 1;
				if (Character.isLetter(split[1].charAt(0))) {
					char c = Character.toLowerCase(split[1].charAt(0));
					y = c - 'a';
				} else
					y = Integer.parseInt(split[1]) - 1;
				if (board.isEmptyField(x, y)) 
					return new Move(x, y);
			} catch(NumberFormatException e) {
				Output.error("error_wrong_input");
			} catch(IndexOutOfBoundsException e) {
				Output.error("error_wrong_input");
			}
			goodMove = false;
		} while(!goodMove);
		
		return null;
	}
	
	/**
	 * Bestimmt das Trennzeichen der Eingabe
	 * @param s
	 * @return
	 */
	private static String delimiter(String s) {
		boolean delimiterFound = false;
		String deli = "";
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetterOrDigit(s.charAt(i))) {
				delimiterFound = true;
				deli += s.charAt(i);
			} else if (delimiterFound)
				return deli;
		}
		return null;
	}
}
