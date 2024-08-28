package com.ericsson.eniq.exception;

/**
 * Generic exception type for licensing related issues.
 * 
 * @author etuolem
 */
public class LicensingException extends Exception {

	/**
	 * Constructor with message only.
	 */
	public LicensingException(final String message) {
		super(message);
	}
	
	/**
	 * Constructor with cause exception.
	 */
	public LicensingException(final String message, final Exception cause) {
		super(message,cause);
	}
	
}
