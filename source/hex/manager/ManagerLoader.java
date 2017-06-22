package hex.manager;

public class ManagerLoader extends Thread {

	public void run() {
		/* Lade Einstellungen */
		Thread settings = new Thread() {
			public void run() {
				SettingManager.init();
			}
		};
		
		/* Lade LanguageManager */
		Thread language = new Thread() {
			public void run() {
				LanguageManager.init();
			}
		};
		
		/* Lade GFXManager */
		Thread gfx = new Thread() {
			public void run() {
				GFXManager.init();
			}
		};
		
		/* Lade SoundManager */
		Thread sound = new Thread() {
			public void run() {
				SoundManager.init();
			}
		};
		
		try {
			settings.start();
			settings.join();
			language.start();
			gfx.start();
			sound.start();
			language.join();
			gfx.join();
			sound.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
