package hex.manager;

import hex.exception.SettingsMissingException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class SettingManager {
	private static HashMap<String, String> settings;
	private static Object monitor = new Object();

	/* Verzeichnisnamen */
	public static final String CONFIGURATION_FOLDER = "cfg";
	private static final String CONFIG_FILE_NAME = "config.xml";

	// ---------------------------------------------------------------

	static {
		synchronized (monitor) {
			settings = new HashMap<String, String>();

			/* Versuche die Datei einzulesen */
			try {
				Document doc = new SAXBuilder().build(new File(
						CONFIGURATION_FOLDER + "/" + CONFIG_FILE_NAME));

				/* Wurzelelement bekommen */
				Element element = doc.getRootElement();

				/* Alle Einstellungen laden */
				for (Element setting : element.getChildren()) {
					for (Attribute a : setting.getAttributes())
						settings.put(setting.getName(), a.getValue());
				}

			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ---------------------------------------------------------------

	public static String getValue(String identifier) {
		synchronized (monitor) {
			if (settings == null)
				throw new SettingsMissingException(
						LanguageManager.getText("error_settings_missing"));

			if (settings.containsKey(identifier))
				return settings.get(identifier);

			return "";
		}
	}

	public static void setValue(String identifier, String value) {
		synchronized (monitor) {
			if (settings == null)
				throw new SettingsMissingException(
						LanguageManager.getText("error_settings_missing"));

			if (settings.containsKey(identifier))
				settings.remove(identifier);
			settings.put(identifier, value);
		}
	}
	
	public static void init() {
		synchronized(monitor) {
			;
		}
	}

}
