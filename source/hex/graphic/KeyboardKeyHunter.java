package hex.graphic;

import hex.game.Game;
import hex.graphic.extended.GraphicalGUI;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardKeyHunter extends KeyAdapter {
	private boolean fullscreenSwitcher;
	private GraphicalGUI gui;
	
	public KeyboardKeyHunter(GraphicalGUI gui, boolean value) {
		this.gui = gui;
		fullscreenSwitcher = value;
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && fullscreenSwitcher)
			gui.switchFullscreen();
		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			Game.switchPlayers();
		else if (e.getKeyCode() == KeyEvent.VK_F1)
			gui.showHelp(true);
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}
}
