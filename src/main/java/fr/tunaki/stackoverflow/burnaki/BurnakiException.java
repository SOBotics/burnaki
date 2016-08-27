package fr.tunaki.stackoverflow.burnaki;

public class BurnakiException extends RuntimeException {

	private static final long serialVersionUID = -6399682495766921774L;

	public BurnakiException(String message) {
		super(message);
	}

	public BurnakiException(String message, Throwable cause) {
		super(message, cause);
	}

}
