package com.ericsson.eniq.ldap.entity;

import javax.naming.directory.DirContext;

/**
 * Marker interface to denote LDAP entities.
 * 
 * This interface extends DirContext, thus entities can be bound/unbound/rebound to LDAP.
 * 
 * @author eramano
 *
 */
public interface ILDAPObject extends DirContext {

}
