package hex.exception;

public class NetworkPlayerException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public NetworkPlayerException(String s) {
		super(s);
	}
	
	public NetworkPlayerException(Exception e) {
		super(e);
	}
}
