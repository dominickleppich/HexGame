package hex.cheat;

import hex.game.Game;
import hex.game.Output;
import hex.manager.SettingManager;
import hex.preset.Player;

import java.util.Random;

public class InsultingThread extends Thread {
	private String type;
	private boolean insultPlayer;

	public InsultingThread(String type) {
		this.type = type;
		insultPlayer = !Game.currentPlayer;
	}

	@Override
	public void run() {
		Random r = new Random();
		try {
			Thread.sleep(3000);
			String master = (insultPlayer == Player.RED ? SettingManager
					.getValue("PLAYER_RED") : SettingManager.getValue("PLAYER_BLUE"));
			String loser = (insultPlayer == Player.RED ? SettingManager
					.getValue("PLAYER_BLUE") : SettingManager.getValue("PLAYER_RED"));
			int guiPrintTextDelay = Integer.parseInt(SettingManager
					.getValue("GUI_PRINT_TEXT_DELAY"));
			int timeOut = (type.compareTo("cheat_insult") == 0 ? Integer
					.parseInt(SettingManager.getValue("INSULT_TIMEOUT_SECONDS"))
					: Integer.parseInt(SettingManager
							.getValue("INSULT_YOUR_MAMA_TIMEOUT_SECONDS")));
			while (true) {
				Thread.sleep(guiPrintTextDelay + 1000 * r.nextInt(timeOut));
				Output.guiPrint(type, loser, master);
			}
		} catch (InterruptedException e) {
		}
	}
}
