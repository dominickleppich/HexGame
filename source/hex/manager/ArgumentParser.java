package hex.manager;

import hex.board.Board;
import hex.exception.ArgumentParserException;

import java.util.HashMap;
import hex.game.Output;

public class ArgumentParser {
	private static HashMap<String, String> arguments;

	// ---------------------------------------------------------------

	public static void parse(String[] args) {
		arguments = new HashMap<String, String>();

		for (int i = 0; i < args.length; i++) {
			if (args[i].charAt(0) == '-') {
				if (i < args.length - 1 && args[i + 1].charAt(0) != '-') {
					arguments.put(args[i].substring(1), args[i + 1]);
					i++;
				} else {
					if (args[i].substring(1, 3).compareTo("no") != 0)
						arguments.put(args[i].substring(1), "true");
					else
						arguments.put(args[i].substring(3), "false");
				}
			}
		}

		analyzeArguments();
	}

	// ---------------------------------------------------------------

	private static void analyzeArguments() {
		/* Ein- und Ausgaben */
		if (arguments.containsKey("output"))
			Output.output = Boolean.parseBoolean(arguments.get("output"));
		if (arguments.containsKey("error"))
			Output.error = Boolean.parseBoolean(arguments.get("error"));
		if (arguments.containsKey("debug"))
			Output.debug = Boolean.parseBoolean(arguments.get("debug"));
		if (arguments.containsKey("guidebug"))
			Output.guiDebug = Boolean.parseBoolean(arguments.get("guidebug"));
		if (arguments.containsKey("guitext"))
			Output.guiPrint = Boolean.parseBoolean(arguments.get("guitext"));

		/* GUI aktiviert? */
		if (arguments.containsKey("gui"))
			SettingManager.setValue("GUI_ENABLED", arguments.get("gui"));
		/* Einzelne GUI's? */
		if (arguments.containsKey("multigui"))
			SettingManager.setValue("GUI_SEPERATED", arguments.get("multigui"));
		/* Einfache GUI (ohne Bilder) */
		if (arguments.containsKey("simplegui"))
			SettingManager.setValue("GUI_SIMPLE", arguments.get("simplegui"));

		/* Mehrere Spiele? */
		if (arguments.containsKey("multigames"))
			SettingManager.setValue("GAME_MULTI_GAMES", arguments.get("multigames"));
		
		/* Cheats aktivieren? */
		if (arguments.containsKey("cheats"))
			SettingManager.setValue("CHEATS_ENABLED", arguments.get("cheats"));

		/* Sprache */
		if (arguments.containsKey("lang"))
			LanguageManager.setLanguage(arguments.get("lang"));
		if (arguments.containsKey("sound"))
			SoundManager.setSoundEnabled(Boolean.parseBoolean(arguments.get("sound")));
		if (arguments.containsKey("voice"))
			SoundManager.setVoiceEnabled(Boolean.parseBoolean(arguments.get("voice")));
		
		/* Spielernamen */
		if (arguments.containsKey("red"))
			SettingManager.setValue("PLAYER_RED", arguments.get("red"));
		if (arguments.containsKey("blue"))
			SettingManager.setValue("PLAYER_BLUE", arguments.get("blue"));

		/* Spielertypen */
		if (arguments.containsKey("redplayer"))
			SettingManager.setValue("PLAYER_RED_TYPE", arguments.get("redplayer"));
		if (arguments.containsKey("blueplayer"))
			SettingManager.setValue("PLAYER_BLUE_TYPE", arguments.get("blueplayer"));

		/* Beginnender Spieler */
		if (arguments.containsKey("begin"))
			SettingManager.setValue("PLAYER_DEFAULT_BEGINNER", (arguments
					.get("begin").compareTo("red") != 0 ? "false" : "true"));

		/* Spielfelddimension */
		if (arguments.containsKey("x"))
			Board.DIMENSION_X = Integer.parseInt(arguments.get("x"));
		if (arguments.containsKey("y"))
			Board.DIMENSION_Y = Integer.parseInt(arguments.get("y"));
	}

	public String getValue(String identifier) {
		if (arguments == null)
			throw new ArgumentParserException(
					LanguageManager.getText("error_argument_parser"));

		if (arguments.containsKey(identifier))
			return arguments.get(identifier);
		return SettingManager.getValue(identifier);
	}
}
