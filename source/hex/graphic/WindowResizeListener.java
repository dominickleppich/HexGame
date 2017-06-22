package hex.graphic;

import hex.game.Output;
import hex.graphic.extended.GraphicalGUI;
import hex.manager.GFXManager;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WindowResizeListener extends ComponentAdapter {
	public static Thread t;

	private GraphicalGUI gui;
	private int oldWidth, oldHeight;
	private static int width, height;

	public WindowResizeListener(GraphicalGUI gui, int width, int height) {
		this.gui = gui;
		oldWidth = width;
		oldHeight = height;
	}

	public void componentResized(final ComponentEvent e) {
		width = e.getComponent().getWidth();
		height = e.getComponent().getHeight();
		if (t == null && width != oldWidth) {
			t = new Thread() {
				public void run() {
					try {
						double scaleFactor = 0;
						do {
							oldWidth = width;
							oldHeight = height;
							Thread.sleep(1000);
							if (width == oldWidth && height == oldHeight) {
								double scaleFactor1 = width / (double) GFXManager.backgroundOriginalWidth();
								double scaleFactor2 = height / (double) GFXManager.backgroundOriginalHeight();
								scaleFactor = (scaleFactor1 > scaleFactor2 ? scaleFactor1 : scaleFactor2);
								gui.gcm().scaleFactor(scaleFactor);
								gui.pack();
							}
						} while (width != oldWidth || height != oldHeight);
						while ((width != (int)(scaleFactor * GFXManager.backgroundOriginalWidth()) && (height != (int)(scaleFactor * GFXManager.backgroundOriginalHeight()))))
							Thread.sleep(100);
						oldWidth = width;
						oldHeight = height;
						Thread.sleep(1000);
						WindowResizeListener.t = null;
					} catch (InterruptedException e) {
						Output.error("error_interrupted_exception", e.toString());
					}
				}
			};
			t.start();
		}
	}
}
