/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.INITIAL_CONTEXT_FACTORY;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PROVIDER_URL;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.SECURITY_AUTHENTICATION_NONE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.SECURITY_AUTHENTICATION_SIMPLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERS_BASE_DN;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;

import java.util.Hashtable;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * Factory class to create a connection to the ENIQ Events OpenLDAP server. 
 * @author etonnee
 * @author eramano
 *
 */
public final class LDAPConnectionFactory {

    
	/**
	 * Private constructor to avoid instantiation. 
	 */
	private LDAPConnectionFactory(){
		//do nothing
	}
	
    /**
     * Get connection to ENIQ Events LDAP server
     * @param userName
     * @param password
     * @return DirContext connection
     * @throws Exception, if failed to establish connection
     */
    public static DirContext getConnection(final String userName,
    		final String password) throws Exception {
        // Create the initial context
    	final DirContext dirctx = new InitialDirContext(setConnectionDetails(userName,password));
        return dirctx;
    }

   /* /**
     * Close an already open LDAP connection.
     * @param dirctx connection
     * @return true, if closed. false, otherwise.
     * @throws Exception if failed to close connection
     *//*
    public static boolean close(final DirContext dirctx) throws Exception {
    	boolean closedFlag = false;
    	try {
    		//Delete the initial context
    		if(dirctx!=null){
    			dirctx.close();
    			closedFlag=true;
    		}
    	} catch (final NamingException namException) {
    		throw namException;
    	} catch (final Exception exception) {
    		throw exception;
    	}
    	return closedFlag;
    }*/
  
    /**
     * Populate properties for creating LDAP Connection.
     * @param userName
     * @param password
     * @return properties for LDAP Connection
     * @throws Exception
     */
    private static Hashtable<String, String> setConnectionDetails(final String userName,
    		final String password) throws Exception {
        final Hashtable<String, String> ldapConfiguration = new Hashtable<String, String>(11);
        ldapConfiguration.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        ldapConfiguration.put(javax.naming.Context.PROVIDER_URL,PROVIDER_URL);
        // TODO Enable connection pooling
        // ldapConfiguration.put(LDAPConstants.CONNECTION_POOL , LDAPConstants.ENABLE_CONNECTION_POOL );
        if(userName !=null && password !=null) {
        	ldapConfiguration.put(javax.naming.Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION_SIMPLE);
        	final StringBuilder userDN = new StringBuilder();
			userDN.append(UID);
			userDN.append(OP_EQUALS);
			userDN.append(userName);
			userDN.append(OP_COMMA);
			userDN.append(USERS_BASE_DN);
        	ldapConfiguration.put(javax.naming.Context.SECURITY_PRINCIPAL, userDN.toString());
        	ldapConfiguration.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
        } else {
        	ldapConfiguration.put(javax.naming.Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION_NONE);
        }
        return ldapConfiguration;
    }    
}
