package com.ericsson.eniq.ldap.util;


/**
 * User state enumerations.
 * @author eramano
 *
 */
public enum USERSTATE {
	
	STATE_LOCKED(100,"User is Locked"),
	STATE_EXPIRED(101,"Password has expired"),
	STATE_NO_ROLES(102,"User has no roles assigned"),
	STATE_NORMAL(103,"");;
	
	private final String statusMessage;
    
    private final int statusCode;
    
    private USERSTATE(final int code, final String message) {
        this.statusMessage = message;
        this.statusCode= code;
    }
    
    public String getMessage() {
        return this.statusMessage;
    }
    
    public int getCode() {
        return this.statusCode;
    }
}
