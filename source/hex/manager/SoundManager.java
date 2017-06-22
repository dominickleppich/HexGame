package hex.manager;

import hex.exception.IllegalFileNameException;
import hex.exception.SoundManagerException;
import hex.game.Output;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SoundManager {
	private static Object monitor = new Object();
	
	private static HashMap<String, AudioClip> database;

	/* Sound */
	private static boolean soundEnabled = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_SOUND_ENABLED"));
	private static boolean voiceEnabled = Boolean.parseBoolean(SettingManager.getValue("DEFAULT_VOICE_ENABLED"));

	static {
		synchronized (monitor) {
			Output.debug("debug_sound_init");
			
			/* Lege Datenbank an */
			database = new HashMap<String, AudioClip>();

			/* Bestimme das Wurzelverzeichnis aller Grafiken */
			File folder = new File(SettingManager.getValue("SOUND_FOLDER"));

			addDirectoryToDatabase("", folder);
		}
	}

	/**
	 * Leere Methode zum initialisieren der Klasse, es muss der
	 * static-Konstruktor aufgerufen werden.
	 */
	public static void init() {
	}

	// ---------------------------------------------------------------

	private static void addDirectoryToDatabase(String rootPrefix, File folder) {
		for (File f : folder.listFiles()) {
			if (f.isFile())
				addFilesToDatabase(rootPrefix, f);
			else
				addDirectoryToDatabase(rootPrefix + f.getName(), f);
		}
	}

	private static void addFilesToDatabase(String rootPrefix, File f) {
		String s = cutFileExtension(rootPrefix + f.getName());
		String currentFile = "";
		try {
			currentFile = f.getCanonicalPath();
			database.put(s, Applet.newAudioClip(f.toURI().toURL()));
			Output.debug("debug_sound_file_loaded", s);
		} catch (IOException e) {
			Output.error("error_sound_file_loading", currentFile);
			System.exit(1);
		}

	}

	private static String cutFileExtension(String s) {
		int dotIndex = s.indexOf('.');
		if (s.indexOf('.', dotIndex + 1) > -1)
			throw new IllegalFileNameException();

		return s.substring(0, dotIndex);
	}

	// ---------------------------------------------------------------

	public static void setSoundEnabled(boolean value) {
		soundEnabled = value;
	}
	
	public static void setVoiceEnabled(boolean value) {
		voiceEnabled = value;
	}
	
	public static void playSound(String identifier) {
		/* Falls Ton ausgeschaltet, nichts tun */
		if (!soundEnabled)
			return;
		
		/* Pruefe ob Datenbank existiert */
		if (database == null)
			throw new SoundManagerException(LanguageManager.getText("error_sound_manager_no_database"));

		if (!database.containsKey(identifier))
			throw new SoundManagerException(LanguageManager.getText("error_sound_manager_no_file", identifier));

		/* Falls Sprache deaktiviert, nicht sprechen */
		if (!voiceEnabled && identifier.contains("voice"))
			return;
		
		synchronized (monitor) {
			database.get(identifier).play();
		}
	}
}
