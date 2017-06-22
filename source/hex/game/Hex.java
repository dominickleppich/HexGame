package hex.game;

import hex.manager.ArgumentParser;
import hex.manager.ManagerLoader;
import hex.manager.SettingManager;
import hex.preset.Player;

public class Hex {

	/* Initialisiere alle Manager in Threads */
	static {
		/* Lade alle Manager */
		Thread initializer = new ManagerLoader();
		try {
			initializer.start();
			initializer.join();
		} catch(InterruptedException e) {
			System.out.println("Critical error occurred!!");
			e.printStackTrace();
		}
	}

	// ---------------------------------------------------------------

	public static void main(String[] args) {
		ArgumentParser.parse(args);

		if (SettingManager.getValue("PLAYER_DEFAULT_BEGINNER") != "")
			Game.createGame(Boolean.parseBoolean(SettingManager.getValue("PLAYER_DEFAULT_BEGINNER")));
		else
			Game.createGame();
		
		Game.addPlayer(Player.RED, SettingManager.getValue("PLAYER_RED_TYPE"));
		Game.addPlayer(Player.BLUE, SettingManager.getValue("PLAYER_BLUE_TYPE"));

		Game.start();
	}
}
