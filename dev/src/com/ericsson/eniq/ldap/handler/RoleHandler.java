package com.ericsson.eniq.ldap.handler;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.DirContext;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.Role;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPConnectionFactory;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.RoleManagement;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;
import com.ericsson.eniq.ldap.util.ValueObjectTransformer;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.RoleVO;

/**
 * Handler implementation for managing roles.
 * 
 * @author eramano
 * 
 */
public class RoleHandler implements IHandler {

	@Override
	public String create(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO,ldapVO);
		MESSAGES result = null;
		final ILDAPManagement roleManagement = getRoleManagement();
		final LoginVO login = (LoginVO) loginVO;
		final RoleVO roleVO = (RoleVO) ldapVO;
		final Role role = new Role(roleVO);

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Check if user is allowed to create
			final MESSAGES createAllowedMessage = getAllowedMessage(login, roleVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == createAllowedMessage) {// Create Role
				result = roleManagement.create(role, ctxt);
			} else {
				throw new LDAPException(createAllowedMessage,new Exception(createAllowedMessage.getMessage()));
			}
		} catch (final LDAPException exception) {
			throw exception;
		} finally {
			try {
				if (null != ctxt) {
					ctxt.close();
				}
			} catch (final Exception exception) {
				throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
			}
		}
		return result.getMessage();
	}

	@Override
	public String modify(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO,ldapVO);
		MESSAGES result = null;
		final ILDAPManagement roleManagement = getRoleManagement();
		final LoginVO login = (LoginVO) loginVO;
		final RoleVO roleVO = (RoleVO) ldapVO;
		final Role role = new Role(roleVO);

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Check if user is allowed to modify
			final MESSAGES modifyAllowedMessage = getAllowedMessage(login, roleVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == modifyAllowedMessage) {// Modify Role
				result = roleManagement.modify(role, ctxt);
			} else {
				throw new LDAPException(modifyAllowedMessage,new Exception(modifyAllowedMessage.getMessage()));
			}
		} catch (final LDAPException exception) {
			throw exception;
		} finally {
			try {
				if (null != ctxt) {
					ctxt.close();
				}
			} catch (final Exception exception) {
				throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
			}
		}
		return result.getMessage();
	}

	@Override
	public String delete(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO,ldapVO);
		MESSAGES result = null;
		final ILDAPManagement roleManagement = getRoleManagement();
		final LoginVO login = (LoginVO) loginVO;
		final RoleVO roleVO = (RoleVO) ldapVO;
		final Role role = new Role(roleVO);

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Check if user is allowed to delete
			final MESSAGES deleteAllowedMessage = getAllowedMessage(login, roleVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == deleteAllowedMessage) {
				// Delete role
				result = roleManagement.delete(role, ctxt);
			} else {
				throw new LDAPException(deleteAllowedMessage,new Exception(deleteAllowedMessage.getMessage()));
			}
		} catch (final LDAPException exception) {
			throw exception;
		} finally {
			try {
				if (null != ctxt) {
					ctxt.close();
				}
			} catch (final Exception exception) {
				throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
			}
		}
		return result.getMessage();
	}

	@Override
	public IValueObject findById(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO,ldapVO);
		final ILDAPManagement roleManagement = getRoleManagement();
		IValueObject roleVO = new RoleVO();
		final LoginVO login = (LoginVO) loginVO;
		final RoleVO incomingRoleVO = (RoleVO) ldapVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// find role by its id
			final ILDAPObject role = roleManagement.findById(incomingRoleVO.getRoleName(), ctxt);
			// if found, convert to VO
			if (role != null) {
				roleVO = ValueObjectTransformer.createRoleVOFromLdapObject(role);
			}
		} catch (final LDAPException exception) {
			throw exception;
		} finally {
			try {
				if (null != ctxt) {
					ctxt.close();
				}
			} catch (final Exception exception) {
				throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
			}
		}
		return roleVO;
	}

	@Override
	public List<IValueObject> findAll(final IValueObject loginVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO);
		final ILDAPManagement roleManagement = getRoleManagement();
		final List<IValueObject> roleVOs = new ArrayList<IValueObject>();
		final LoginVO login = (LoginVO) loginVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// retrieve all roles
			final List<ILDAPObject> roles = roleManagement.findAll(ctxt);
			// if roles are retrieved, convert them to VOs
			if (roles != null) {
				for (final ILDAPObject ldapObj : roles) {
					final Role role = (Role) ldapObj;
					roleVOs.add(ValueObjectTransformer.createRoleVOFromLdapObject(role));
				}
			}
		} catch (final LDAPException exception) {
			throw exception;
		} finally {
			try {
				if (null != ctxt) {
					ctxt.close();
				}
			} catch (final Exception exception) {
				throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
			}
		}
		return roleVOs;
	}
	
	/**
	 * Create an instance of RoleManagement.
	 * Refactored to method mainly for unit testing.
	 * @return
	 */
	protected ILDAPManagement getRoleManagement() {
		final ILDAPManagement roleManagement = new RoleManagement();
		return roleManagement;
	}

	/**
	 * Verify the validity of operation being performed.
	 * Based on various properties of role being updated and
	 * the user performing the action
	 * @param login
	 * @param roleVO
	 * @param ctxt
	 * @return
	 * @throws LDAPException
	 */
	protected MESSAGES getAllowedMessage(final LoginVO login, final RoleVO roleVO, final DirContext ctxt) throws LDAPException {
		return LDAPUtil.isAllowedToCUDRole(login, roleVO, ctxt);
	}

	/**
	 * Get LDAP Connection using logged in users user id and password
	 * @param login value object with login id and password
	 * @return DirContext
	 * @throws LDAPException
	 */
	protected DirContext getLDAPConnection(final LoginVO login) throws LDAPException {
		DirContext ctxt = null;
		try {
			// Get LDAP Connection
			ctxt = LDAPConnectionFactory.getConnection(login.getLoginId(), login.getPassword());
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_CONNECTION_EXCEPTION,exception);
		}
		return ctxt;
	}
}
