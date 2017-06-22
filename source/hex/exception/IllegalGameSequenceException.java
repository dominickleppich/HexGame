package hex.exception;

public class IllegalGameSequenceException extends GameException {
	public IllegalGameSequenceException(){
		super();
	}
	
	public IllegalGameSequenceException(String s){
		super(s);
	}
	
	private static final long serialVersionUID = 1L;
}
