package hex.graphic.extended;

import hex.manager.GraphicComponentManager;

import java.awt.Image;
import java.awt.Point;
import java.util.LinkedList;

public class GraphicComponent {
	protected GraphicComponentManager gcm;
	protected String myName;
	protected Point p;
	protected LinkedList<Image> frames;

	protected int currentFrame = 0;

	// ---------------------------------------------------------------

//	public GraphicComponent(Image... images) {
//		this(new Point(0, 0), images);
//	}
//
//	public GraphicComponent(LinkedList<Image> list) {
//		this(new Point(0, 0), list);
//	}
	
	public GraphicComponent(GraphicComponentManager gcm, String componentName, Point p, LinkedList<Image> list) {
		this.gcm = gcm;
		myName = componentName;
		this.p = p;
		frames = list;
	}
	
	public GraphicComponent(GraphicComponentManager gcm, String componentName, Point p, Image... images) {
		this.gcm = gcm;
		myName = componentName;
		this.p = p;
		frames = new LinkedList<Image>();
		for (Image i : images)
			frames.add(i);
	}

	// ---------------------------------------------------------------
	
	public void remove() {
		gcm.remove(myName);
	}
	
	// ---------------------------------------------------------------
	
	public Point point() {
		return p;
	}
	
	public void point(Point p) {
		this.p = p;
	}
	
	public void frames(LinkedList<Image> list) {
		frames = list;
	}
	
	public void frames(Image... images) {
		frames = new LinkedList<Image>();
		for (Image i : images)
			frames.add(i);
		currentFrame = 0;
	}
	
	public Image frame() {
		if (frames == null)
			return null;
		
		if (frames.size() == 0)
			return null;
		
		Image i = frames.get(currentFrame);
		currentFrame = (currentFrame + 1) % frames.size();
		
		return i;
	}
	
	public Image[] frames() {
		Image[] images = new Image[frames.size()];
		for (int i = 0; i < frames.size(); i++)
			images[i] = frames.get(i);
		return images;
	}
	
	public int size() {
		return frames.size();
	}
}
