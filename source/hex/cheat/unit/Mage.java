package hex.cheat.unit;

import hex.graphic.extended.GraphicComponent;
import hex.manager.GraphicComponentManager;
import hex.manager.SoundManager;

import java.awt.Image;
import java.awt.Point;
import java.util.Random;

public class Mage extends GraphicComponent {
	public static final int WALKING = 0;
	public static final int EXPLODING = 1;
	public static final int DEAD = 2;
	
	private double xPos = 0.0;
	
	private int status = 0;
	private int counter = 0;
	
	private int limit = new Random().nextInt(300);
	private double speed = new Random().nextDouble() * 5;

	public Mage(GraphicComponentManager gcm, String componentName, Point p, Image[] images) {
		super(gcm, componentName, new Point(new Random().nextInt(300) + 10, new Random().nextInt(400) + 10), gcm.images("cheatBattleUnitMageRedWalkMiddle"));
	}
	
	private void updateMage() {
		counter++;
		if (status == WALKING)
		xPos += speed;
		p = new Point((int)xPos, p.y);
		
		switch(status) {
		case WALKING:
			if (counter > limit) {
				counter = 0;
				frames(gcm.images("cheatBattleUnitExplosion"));
				status = EXPLODING;
			}	
			break;
		case EXPLODING:
			if (counter > 13) {
				counter = 0;
				frames(gcm.images("cheatBattleUnitDead"));
				status = DEAD;
				SoundManager.playSound("explosion");
			}	
		}
	}
	
	public Image frame() {
		if (frames == null)
			return null;
		
		if (frames.size() == 0)
			return null;
		
		Image i = frames.get(currentFrame);
		currentFrame = (currentFrame + 1) % frames.size();
		
		updateMage();
		
		return i;
	}
	
}
