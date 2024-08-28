package com.ericsson.eniq.ldap.management;

import java.util.List;

import javax.naming.directory.DirContext;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.util.MESSAGES;

/**
 * LDAP Management Interface.
 * 
 * This interface is used to add/update/delete/find security information from ENIQ Events Directory Server.
 * 
 * @author etonnee
 * @author eramano
 *
 */
public interface ILDAPManagement {

	/**
	 * Create an entity in LDAP (User/Role/PermissionGroups)
	 * @param ldapObj
	 * @param conn LDAP Connection
	 * @return MESSAGES
	 * @throws LDAPException
	 */
	MESSAGES create(final ILDAPObject ldapObj,final DirContext conn) throws LDAPException;
	/**
	 * Modify an existing entity in LDAP (User/Role/PermissionGroups)
	 * @param ldapObj object to be modified
	 * @param conn LDAP Connection
	 * @return MESSAGES
	 * @throws LDAPException
	 */
	MESSAGES modify(final ILDAPObject ldapObj,final DirContext conn) throws LDAPException;
	/**
	 * Delete an entity from LDAP using its key (User/Role/PermissionGroups)
	 * @param ldapObj object to be deleted
	 * @param conn LDAP Connection
	 * @return MESSAGES
	 * @throws LDAPException
	 */
	MESSAGES delete(final ILDAPObject ldapObj,final DirContext conn) throws LDAPException;
	/**
	 * Find all entities from LDAP (User/Role/PermissionGroups)
	 * @param conn LDAP Connection
	 * @return list of entities
	 * @throws LDAPException
	 */
	List<ILDAPObject> findAll(final DirContext conn)throws LDAPException;
	/**
	 * Find a unique entity from LDAP using its key (User/Role/PermissionGroups)
	 * @param id key of LDAP entity to be found
	 * @param conn LDAP Connection
	 * @return LDAP object 
	 * @throws LDAPException
	 */
	ILDAPObject findById(final String id,final DirContext conn)throws LDAPException;
}


