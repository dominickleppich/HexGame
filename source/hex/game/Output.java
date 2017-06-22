package hex.game;

import hex.graphic.GUI;
import hex.graphic.extended.GraphicalGUI;
import hex.manager.GFXManager;
import hex.manager.LanguageManager;
import hex.manager.SettingManager;

import java.util.Iterator;

public class Output {
	private static final String errorPrefix = "[ERROR]";
	private static final String debugPrefix = "[DEBUG]";

	/* Default-Werte */
	public static boolean output = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_OUTPUT_ENABLED"));
	public static boolean error = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_ERROR_ENABLED"));
	public static boolean debug = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_DEBUG_ENABLED"));
	public static boolean guiDebug = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_GUIDEBUG_ENABLED"));
	public static boolean guiPrint = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_GUIPRINT_ENABLED"));

	// ---------------------------------------------------------------

	public static void error(String identifier) {
		error(identifier, "");
	}

	public static void error(String identifier, String... values) {
		if (output && error) {
			String s = LanguageManager.getText(identifier);
			for (int i = 1; i <= values.length; i++)
				s = s.replace("[" + Integer.toString(i) + "]", values[i - 1]);
			System.out.println(errorPrefix + " " + s + "!");
		}
	}

	public static void debug(String identifier) {
		debug(identifier, "");
	}

	public static void debug(String identifier, String... values) {
		if (output && debug) {
			String s = LanguageManager.getText(identifier);
			for (int i = 1; i <= values.length; i++)
				s = s.replace("[" + Integer.toString(i) + "]", values[i - 1]);
			System.out.println(debugPrefix + " " + s);
			Iterator<GUI> g = GFXManager.guis();
			if (g != null)
				while (g.hasNext()) {
					GUI gg = g.next();
					if (gg instanceof GraphicalGUI)
						((GraphicalGUI) gg).debug(debugPrefix + " " + s);
				}
		}
	}

	public static void guiPrint(String identifier) {
		guiPrint(identifier, "");
	}

	public static void guiPrint(String identifier, String... values) {
		String s = LanguageManager.getText(identifier);
		for (int i = 1; i <= values.length; i++)
			s = s.replace("[" + Integer.toString(i) + "]", values[i - 1]);
		Iterator<GUI> g = GFXManager.guis();
		if (g != null) 
			while (g.hasNext())
				g.next().printText(s);
	}

	public static void print(String identifier) {
		print(identifier, "");
	}

	public static void print(String identifier, String... values) {
		if (output) {
			String s = LanguageManager.getText(identifier);
			for (int i = 1; i <= values.length; i++)
				s = s.replace("[" + Integer.toString(i) + "]", values[i - 1]);
			System.out.println(s);
		}
	}
}
