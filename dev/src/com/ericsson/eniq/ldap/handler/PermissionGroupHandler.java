package com.ericsson.eniq.ldap.handler;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.DirContext;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.PermissionGroup;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPConnectionFactory;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.PermissionGroupManagement;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;
import com.ericsson.eniq.ldap.util.ValueObjectTransformer;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;

/**
 * Handler implementation class for managing Permission Groups.
 * 
 * @author eramano
 * 
 */
public class PermissionGroupHandler implements IHandler {

	@Override
	public String create(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO,ldapVO);
		MESSAGES result = null;
		final ILDAPManagement permGroupManagement = getPermissionGroupManagement();
		final LoginVO login = (LoginVO) loginVO;
		final PermissionGroupVO permGroupVO = (PermissionGroupVO) ldapVO;
		final PermissionGroup permGroup = new PermissionGroup(permGroupVO);

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);

		try {
			// Check if user is allowed to create
			final MESSAGES createAllowedMessage = getAllowedMessage(login, permGroupVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == createAllowedMessage) {
				// Create permission group
				result = permGroupManagement.create(permGroup, ctxt);
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
		final ILDAPManagement permGroupManagement = getPermissionGroupManagement();
		final LoginVO login = (LoginVO) loginVO;
		final PermissionGroupVO permGroupVO = (PermissionGroupVO) ldapVO;
		final PermissionGroup permGroup = new PermissionGroup(permGroupVO);

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);

		try {
			// Check if user is allowed to modify
			final MESSAGES modifyAllowedMessage = getAllowedMessage(login, permGroupVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == modifyAllowedMessage) {
				// Modify permission group
				result = permGroupManagement.modify(permGroup, ctxt);
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
		final ILDAPManagement permGroupManagement = getPermissionGroupManagement();
		final LoginVO login = (LoginVO) loginVO;
		final PermissionGroupVO permGroupVO = (PermissionGroupVO) ldapVO;
		final PermissionGroup permGroup = new PermissionGroup(permGroupVO);

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Check if user is allowed to delete
			final MESSAGES deleteAllowedMessage = LDAPUtil.isAllowedToCUDPermGroup(login, permGroupVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == deleteAllowedMessage) {
				// Delete permission group
				result = permGroupManagement.delete(permGroup, ctxt);
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
		final ILDAPManagement permGroupManagement = getPermissionGroupManagement();
		IValueObject permGroupVO = new PermissionGroupVO();
		final LoginVO login = (LoginVO) loginVO;
		final PermissionGroupVO incomingPermGroupVO = (PermissionGroupVO) ldapVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			if (null != ctxt) {
				// Find permission group using its id
				final ILDAPObject permGroup = permGroupManagement.findById(incomingPermGroupVO.getPermissionGroupName(), ctxt);
				// if found, convert LDAP object to VO
				if (permGroup != null) {
					permGroupVO = ValueObjectTransformer.createPermissionGroupVOFromLdapObject(permGroup);
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
		return permGroupVO;
	}

	@Override
	public List<IValueObject> findAll(final IValueObject loginVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO);
		final ILDAPManagement permGroupManagement = getPermissionGroupManagement();
		final List<IValueObject> permGroupVOs = new ArrayList<IValueObject>();
		final LoginVO login = (LoginVO) loginVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Find all permission groups
			final List<ILDAPObject> permGroups = permGroupManagement.findAll(ctxt);
			// if there are permission groups, convert them to VOs
			if (permGroups != null) {
				for (final ILDAPObject ldapObj : permGroups) {
					final PermissionGroup permGroup = (PermissionGroup) ldapObj;
					permGroupVOs.add(ValueObjectTransformer.createPermissionGroupVOFromLdapObject(permGroup));
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
		return permGroupVOs;
	}
	
	/**
	 * Verify the validity of operation being performed.
	 * Based on various properties of permission group being updated and
	 * the user performing the action
	 * @param login
	 * @param permGroupVO
	 * @param ctxt
	 * @return
	 * @throws LDAPException
	 */
	protected MESSAGES getAllowedMessage(final LoginVO login, final PermissionGroupVO permGroupVO, final DirContext ctxt) throws LDAPException {
		return LDAPUtil.isAllowedToCUDPermGroup(login, permGroupVO, ctxt);
	}

	/**
	 * Create and return an instance of PermissionGroupManagement
	 * Refactored to a method mainly for unit testing.
	 * @return ILDAPManagement
	 */
	protected ILDAPManagement getPermissionGroupManagement() {
		final ILDAPManagement permGroupManagement = new PermissionGroupManagement();
		return permGroupManagement;
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
