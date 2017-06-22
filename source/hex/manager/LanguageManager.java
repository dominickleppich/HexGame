package hex.manager;

import hex.game.Output;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class LanguageManager {


	private static HashMap<String, HashMap<String, StringList>> texts;
	private static String language;

	// ---------------------------------------------------------------

	/**
	 * Beim ersten Zugriff auf die Klasse Texts werden die Konfigurationsdaten
	 * gelesen.
	 */
	static {

			texts = new HashMap<String, HashMap<String, StringList>>();

			File folder = new File(SettingManager.CONFIGURATION_FOLDER);
			/* Einlesen der Konfigurationsdateien */
			for (File file : folder.listFiles()) {
				String fileName = file.getName();

				/* Versuche die Datei einzulesen */
				try {
					/* Sprachdatei? */
					if (fileName.matches("lang_.*")) {
						HashMap<String, StringList> currentLanguage = new HashMap<String, StringList>();

						Document doc = new SAXBuilder().build(file);

						/* Wurzelelement bekommen */
						Element element = doc.getRootElement();

						/* Alle Texte laden */
						List<Element> debugContainer = element.getChildren("text");
						/* Fuer jeden text-Container (es sollte nur einer sein) */
						for (Element debugContainerItem : debugContainer) {
							/* Attribute des Text-Containers */
							for (Element textItem : debugContainerItem.getChildren()) {

								/* Fuer jeden Text in dem TextItem */
								List<Attribute> a = textItem.getAttributes();
								if (a.size() > 0) {
									StringList s = new StringList();
									for (Attribute item : a)
										s.add(item.getValue());
									currentLanguage.put(textItem.getName(), s);
									continue;
								}

								List<Element> c = textItem.getChildren();
								if (c.size() > 0) {
									StringList s = new StringList();
									for (Element e : textItem.getChildren())
										s.add(e.getValue());
									currentLanguage.put(textItem.getName(), s);
								}
							}

						}

						/* Sprache bestimmen */
						String thisLanguage = element.getChild("name").getValue();

						/*
						 * Speichere die eingelesene Sprache in der internen
						 * HashMap
						 */
						texts.put(thisLanguage, currentLanguage);

						/*
						 * Falls noch keine Standardsprache eingestellt, waehle
						 * diese
						 */
						if (language == null || language == "")
							language = thisLanguage;
						
						if (thisLanguage.compareTo(SettingManager.getValue("DEFAULT_LANGUAGE")) == 0)
							language = thisLanguage;

						continue;
					}

				} catch (JDOMException e) {
					if (language == null || language == "")
						System.out.println("Error reading configuration file [JDOMException]!" + e.toString());
					else
						Output.error("error_jdom_exception", e.toString());
				} catch (IOException e) {
					if (language == null || language == "")
						System.out.println("Error reading file [IOException]!");
					else
						Output.error("error_io_exception", e.toString());
				}

			}

	}

	/**
	 * Sorgt dafuer, dass der static Konstruktor ausgefuehrt wird.
	 */
	public static void init() {

	}

	// ---------------------------------------------------------------

	public static String getText(String identifier) {
		if (language == null || language == "")
			return "[NO LANGUAGE SELECTED]";

		if (!texts.containsKey(language))
			return "[LANGUAGE NOT FOUND]";

		HashMap<String, StringList> lang = texts.get(language);
		if (!lang.containsKey(identifier))
			return "[IDENTIFIER " + identifier + " IS MISSING IN LANGUAGE FILE]";
		return lang.get(identifier).get();
	}

	public static String getText(String identifier, String... values) {
		String s = getText(identifier);
		for (int i = 1; i <= values.length; i++)
			s = s.replace("[" + Integer.toString(i) + "]", values[i - 1]);
		return s;
	}

	public static void setLanguage(String lang) {
		if (texts.containsKey(lang)) {
			language = lang;
			Output.debug("debug_language_changed", lang);
			SettingManager.setValue("PLAYER_RED", getText("player_red"));
			SettingManager.setValue("PLAYER_BLUE", getText("player_blue"));
		} else
			Output.error("error_language_not_changed", lang);
	}
}
