package hex.graphic;


public class GUIRefresherThread extends Thread {
	private GUI gui;

	public GUIRefresherThread(GUI gui) {
		this.gui = gui;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(20);
				gui.renderScreen();
				gui.updateScreen();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
