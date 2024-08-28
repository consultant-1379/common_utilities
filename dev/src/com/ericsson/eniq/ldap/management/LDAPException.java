package com.ericsson.eniq.ldap.management;

import com.ericsson.eniq.ldap.util.MESSAGES;

/**
 * @author eramano
 * 
 * Exception class for all LDAP operations in common utilities.
 *
 */
public class LDAPException extends Exception {
	
	/**
	 * Error Message
	 */
	private String errorMessage;
	
	/**
	 * Error Code
	 */
	private int errorCode;
	
	/**
	 * Create LDAPException from other exception
	 * @param e
	 */
	public LDAPException(Exception e) {
		super(e);
	}

	/**
	 * Create LDAPException using a error message
	 * @param stringMsg
	 */
	public LDAPException(String stringMsg) {
		super(stringMsg);
	}

	/**
	 * Create LDAPException using message and actual exception
	 * @param message
	 * @param exception
	 */
	public LDAPException(final MESSAGES message, final Exception exception ){
		super(exception);
		this.errorMessage = message.getMessage();
		this.errorCode = message.getCode();
	}

	/**
	 * Get error message
	 * @return errormessage
	 */ 
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Set error message
	 * @param errorMessage
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Get error code
	 * @return
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Set error code
	 * @param errorCode
	 */
	public void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}

	
	/**
	 * For serialisation
	 */
	private static final long serialVersionUID = 1L;

	
	
}
