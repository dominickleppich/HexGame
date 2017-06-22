package hex.exception;

public class GameException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GameException() {
		super();
	}
	
	public GameException(String s) {
		super(s);
	} 
}
