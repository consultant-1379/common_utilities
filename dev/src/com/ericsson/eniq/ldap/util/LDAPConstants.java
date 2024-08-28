/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.ldap.util;


/**
 * String constants to be used for LDAP connection management and security information management.
 * @author etonnee
 * @author eramano
 * @since 2011
 *
 */
public interface LDAPConstants {
	
    /* Used for RBAC(LDAP) Permissions */
    
    String EVENTS_UI_NETWORK_VIEW = "eventsui.network.view";
    
    String EVENTS_UI_TERMINAL_VIEW = "eventsui.terminal.view";
    
    String EVENTS_UI_SUBSCRIBER_VIEW = "eventsui.subscriber.view";
    
    String EVENTS_UI_RANKINGS_VIEW = "eventsui.ranking.view";
    
    /* Used for LDAP search filter and DN management */
    
    String LEFT_BRACE = "(";
    
    String RIGHT_BRACE = ")";
    
    String OP_AND = "&";
    
    String OP_OR = "|";
    
    String OP_EQUALS = "=";
    
    String OP_COMMA = ",";
    
    String EMPTY_STRING = "";
    
    String ONE_SPACE = " ";
    
    String DEFAULT_PHONE_NUMBER = "1";

	/**
     * A placeholder password for users read from LDAP.
     * It is not possible to retrieve password from LDAP as it is stored as cipher text.
     */
    String PASSWORD_PLACEHOLDER = "UNCHANGED";
    
    /**
     * Exception message when a user enters a password same as the last 5 passwords.
     */
    String PASSWORD_IN_PASSWORD_HISTORY_EXCEPTION_MESSAGE = "LDAP: error code 19";
}
