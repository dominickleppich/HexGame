package hex.manager;

import hex.exception.GFXManagerException;
import hex.exception.IllegalFileNameException;
import hex.game.Output;
import hex.graphic.GUI;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

public class GFXManager {
	private static Object monitor = new Object();

	public static final int SCREEN_4_TO_3 = 0;
	public static final int SCREEN_16_TO_9 = 1;

	private static int screenType;

	private static HashMap<String, LinkedList<Image>> database;

	private static LinkedList<GUI> guis;

	private static int backgroundOriginalWidth;
	private static int backgroundOriginalHeight;

	// ---------------------------------------------------------------

	static {
		synchronized (monitor) {
			Output.debug("debug_gfx_init");

			/* Lege Datenbank an */
			database = new HashMap<String, LinkedList<Image>>();

			/* Bestimme das Wurzelverzeichnis aller Grafiken */
			File folder = new File(SettingManager.getValue("GFX_FOLDER"));

			addDirectoryToDatabase("", folder);

			// backgroundOriginalHeight = backGround.getHeight(null);

			Dimension screenSize = screenDimension();
			if ((double) screenSize.width / screenSize.height == (double) 4 / 3)
				screenType(GFXManager.SCREEN_4_TO_3);
			else
				screenType(GFXManager.SCREEN_16_TO_9);
		}
	}

	/**
	 * Leere Methode zum initialisieren der Klasse, es muss der
	 * static-Konstruktor aufgerufen werden.
	 */
	public static void init() {
		synchronized (monitor) {
			;
		}
	}

	// ---------------------------------------------------------------

	public static void addGUI(GUI gui) {
		synchronized (monitor) {
			if (guis == null)
				guis = new LinkedList<GUI>();
			guis.add(gui);
		}
	}

	public static Iterator<GUI> guis() {
		synchronized (monitor) {
			if (guis == null)
				return null;
			return guis.iterator();
		}
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
			LinkedList<Image> list;
			String[] frames = s.split("#");
			String frame = frames[0];
			if (f.getName().substring(f.getName().length() - 3).compareTo("gif") == 0) {
				list = new LinkedList<Image>();
				Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("GIF");
				ImageReader reader = (ImageReader)readers.next();
				reader.setInput(ImageIO.createImageInputStream(f));
				for (int i = 0; i < reader.getNumImages(true); i++) 
					list.add(reader.read(i));
				database.put(frame, list);
			} else {
				if (!database.containsKey(frame)) {
					list = new LinkedList<Image>();
					database.put(frame, list);
				} else
					list = database.get(frame);
				list.add(ImageIO.read(f));
			}
			
			Output.debug("debug_gfx_file_loaded", s);
		} catch (IOException e) {
			Output.error("error_gfx_file_loading", currentFile);
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

	public static int backgroundOriginalWidth() {
		synchronized (monitor) {
			return backgroundOriginalWidth;
		}
	}

	public static int backgroundOriginalHeight() {
		synchronized (monitor) {
			return backgroundOriginalHeight; // +
												// Integer.parseInt(SettingManager.getValue("WINDOW_TOP_BAR_HEIGHT"))
		}
	}

	// ---------------------------------------------------------------

	/**
	 * Besorgt die Bildschirmaufloesung
	 * 
	 * @return
	 */
	public static Dimension screenDimension() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = env.getDefaultScreenDevice();
		DisplayMode dm = gd.getDisplayMode();
		return new Dimension(dm.getWidth(), dm.getHeight());
	}

	/**
	 * Setze das Bildschirmformat
	 * 
	 * @param screenType
	 */
	private static void screenType(int screenType) {
		if (screenType != SCREEN_16_TO_9 && screenType != SCREEN_4_TO_3)
			return;
		GFXManager.screenType = screenType;
		backgroundOriginalWidth = image("boardBackground").get(0).getWidth(null);
		backgroundOriginalHeight = image("boardBackground").get(0).getHeight(null);
	}

	// ---------------------------------------------------------------

	public static LinkedList<Image> image(String identifier) {
		synchronized (monitor) {
			/* Pruefe ob Datenbank existiert */
			if (database == null)
				throw new GFXManagerException(LanguageManager.getText("error_gfx_manager_no_database"));

			/*
			 * Falls das Hintergrundbild angefordert wird, entscheide zwischen
			 * 16zu9 und 4zu3
			 */
			if (identifier.compareTo("boardBackground") == 0)
				identifier = (screenType == SCREEN_4_TO_3 ? "boardBackground4to3" : "boardBackground16to9");
			
			if (!database.containsKey(identifier))
				throw new GFXManagerException(LanguageManager.getText("error_gfx_manager_no_file", identifier));

			return database.get(identifier);
		}
	}

	public static Iterator<Entry<String, LinkedList<Image>>> images() {
		synchronized (monitor) {
			if (database == null)
				return null;
			return database.entrySet().iterator();
		}
	}
}
