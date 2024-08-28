package com.ericsson.eniq.ldap.handler;

import java.util.List;

import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.IValueObject;

/**
 * Handler interface for security administration (AdminUI).
 * 
 * Security Administration user interface components must use this interface
 * for performing CRUD operations on ENIQ Events Directory server.
 * 
 * @author eramano
 *
 */
public interface IHandler {
	/**
	 * Create an entity in LDAP
	 * @param loginVO credentials of user performing this operation
	 * @param ldapVO values of entity being created
	 * @return message 
	 * @throws LDAPException
	 */
	String create(final IValueObject loginVO,final IValueObject ldapVO) throws LDAPException;
	/**
	 * Modify an entity in LDAP
	 * @param loginVO credentials of user performing this operation
	 * @param ldapVO values of entity being modified
	 * @return message 
	 * @throws LDAPException
	 */
	String modify(final IValueObject loginVO,final IValueObject ldapVO) throws LDAPException;
	/**
	 * Delete and entity in LDAP
	 * @param loginVO credentials of user performing this operation
	 * @param ldapVO values of entity being modified
	 * @return message 
	 * @throws LDAPException
	 */
	String delete(final IValueObject loginVO,final IValueObject ldapVO) throws LDAPException;
	/**
	 * Find and retrieve an entity from LDAP using its unique id.
	 * @param loginVO credentials of user performing this operation
	 * @param ldapVO values of entity to be retrieved
	 * @return IValueObject entity 
	 * @throws LDAPException
	 */
	IValueObject findById(final IValueObject loginVO,final IValueObject ldapVO) throws LDAPException;
	/**
	 * Find all entities from LDAP.
	 * @param loginVO credentials of user performing this operation
	 * @return List<ValueObject> list of entities 
	 * @throws LDAPException
	 */
	List<IValueObject> findAll(final IValueObject loginVO) throws LDAPException;
}
