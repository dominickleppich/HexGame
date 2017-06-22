package hex.manager;

import java.util.LinkedList;
import java.util.Random;

public class StringList {
	private LinkedList<String> list;
	
	// ---------------------------------------------------------------
	
	public StringList() {
		list = new LinkedList<String>();
	}
	
	public StringList(String s) {
		this();
		list.add(s);
	}
	
	// ---------------------------------------------------------------
	
	public void add(String s) {
		if (!list.contains(s))
			list.add(s);
	}
	
	public void remove(String s) {
		if (list.contains(s))
			list.remove(s);
	}
	
	public String get() {
		if (list.isEmpty())
			return null;
		
		if (list.size() == 1)
			return list.get(0);
		
		return list.get(new Random().nextInt(list.size()));
	}
}
