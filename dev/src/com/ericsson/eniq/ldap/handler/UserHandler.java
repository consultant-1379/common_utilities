package com.ericsson.eniq.ldap.handler;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.User;
import com.ericsson.eniq.ldap.entity.UserProfile;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPAttributes;
import com.ericsson.eniq.ldap.management.LDAPConnectionFactory;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.UserManagement;
import com.ericsson.eniq.ldap.management.UserProfileManagement;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;
import com.ericsson.eniq.ldap.util.ValueObjectTransformer;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.UserProfileVO;
import com.ericsson.eniq.ldap.vo.UserVO;

/**
 * Handler implementation for managing ENIQ Events Users
 * 
 * @author eramano
 * 
 */
public class UserHandler implements IHandler {

	@Override
	public String create(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO,ldapVO);
		final ILDAPManagement userManagement = getUserManagementInstance();
		final ILDAPManagement userProfileManagement = getUserProfileManagementInstance();
		MESSAGES result = null;
		final UserVO userVO = (UserVO) ldapVO;
		final LoginVO login = (LoginVO) loginVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			final MESSAGES validationMessage = ValidationUtil.validateUserDetails(userVO);
			final MESSAGES operationAllowedMessage = getCreateAllowedMessage(login, userVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == validationMessage ) {
				if ( MESSAGES.MSG_SUCCESS == operationAllowedMessage ) {
					// Add user
					final User user = new User(userVO);
					result = userManagement.create(user, ctxt);
					// Add user profile
					final UserProfile userProfile = getUserProfileForUser(userVO);
					result = userProfileManagement.create(userProfile, ctxt);
				} else {
					throw new LDAPException(operationAllowedMessage,new Exception(operationAllowedMessage.getMessage()));
				}
			} else {
				throw new LDAPException(validationMessage,new Exception(validationMessage.getMessage()));
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
		final ILDAPManagement userManagement = getUserManagementInstance();
		final ILDAPManagement userProfileManagement = getUserProfileManagementInstance();
		MESSAGES result = null;
		final UserVO userVO = (UserVO) ldapVO;
		final LoginVO login = (LoginVO) loginVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			final MESSAGES validationMessage = ValidationUtil.validateUserDetails(userVO);
			final MESSAGES operationAllowedMessage = getModifyAllowedMessage(login, userVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == validationMessage ) {
				if ( MESSAGES.MSG_SUCCESS == operationAllowedMessage ) {
					// Modify user
					final User user = new User(userVO);
					result = userManagement.modify(user, ctxt);
					// Modify user profile
					final UserProfile userProfile = getUserProfileForUser(userVO);
					result = userProfileManagement.modify(userProfile, ctxt);
				} else {
					throw new LDAPException(operationAllowedMessage,new Exception(operationAllowedMessage.getMessage()));
				}
			} else {
				throw new LDAPException(validationMessage,new Exception(validationMessage.getMessage()));
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
		final ILDAPManagement userManagement = getUserManagementInstance();
		final ILDAPManagement userProfileManagement = getUserProfileManagementInstance();
		MESSAGES result = null;
		final UserVO userVO = (UserVO) ldapVO;
		final LoginVO login = (LoginVO) loginVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Check if delete is allowed for the user performing delete and
			// user being deleted
			final MESSAGES deleteAllowedMessage = getDeleteAllowedMessage(login, userVO, ctxt);
			if (MESSAGES.MSG_SUCCESS == deleteAllowedMessage) {
				// first delete user profile
				final UserProfile userProfile = getUserProfileForUser(userVO);
				result = userProfileManagement.delete(userProfile, ctxt);
				// then delete user
				final User user = new User(userVO);
				result = userManagement.delete(user, ctxt);
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
		final ILDAPManagement userManagement = getUserManagementInstance();
		final ILDAPManagement userProfileManagement = getUserProfileManagementInstance();
		ILDAPObject user = null;
		ILDAPObject userProfile = null;
		final LoginVO login = (LoginVO) loginVO;
		final UserVO incomingUserVO = (UserVO) ldapVO;
		UserVO outgoingUserVO = new UserVO();
		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Find user by user id
			user = userManagement.findById(incomingUserVO.getUserId(), ctxt);
			// Find user profile for roles
			userProfile = userProfileManagement.findById(incomingUserVO.getUserId(), ctxt);
			
			// if user and user profile exists, create User VO and return
			if (user != null && userProfile != null) {
				// read password age limit from password policy
				final long maxPasswordAgeLimit = getMaxPasswordAgeLimit(ctxt, (String) user.getAttributes(LDAPAttributes.PWD_POLICY_SUB_ENTRY).get(LDAPAttributes.PWD_POLICY_SUB_ENTRY).get());
				outgoingUserVO = ValueObjectTransformer.createUserVOFromLdapObject(user, userProfile, maxPasswordAgeLimit);
			}
		} catch (final LDAPException exception) {
			throw exception;
		} catch (final NamingException exception) {
			
		}
		finally {
			try {
				if (null != ctxt) {
					ctxt.close();
				}
			} catch (final Exception exception) {
				throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
			}
		}
		return outgoingUserVO;
	}

	@Override
	public List<IValueObject> findAll(final IValueObject loginVO) throws LDAPException {
		//Make sure incoming parameters are not null
		ValidationUtil.checkIfNull(loginVO);
		final ILDAPManagement userManagement = getUserManagementInstance();
		final ILDAPManagement userProfileManagement = getUserProfileManagementInstance();
		final List<IValueObject> userVOs = new ArrayList<IValueObject>();
		final LoginVO login = (LoginVO) loginVO;

		// get connection to LDAP
		final DirContext ctxt = getLDAPConnection(login);
		
		try {
			// Retrieve all configured users
			final List<ILDAPObject> users = userManagement.findAll(ctxt);
			if (users != null) {
				// for each users, retrieve user profile
				for (final ILDAPObject ldapObj : users) {
					final User user = (User) ldapObj;
					String userId = null;
					userId = (String) user.getAttributes(LDAPAttributes.UID).get(LDAPAttributes.UID).get();
					// retrieve user profile
					final UserProfile userProfile = (UserProfile) userProfileManagement.findById(userId, ctxt);
					// if found, create User VO from user and userprofile
					// objects
					if (null != userProfile) {
						// read password age limit from password policy
						final long maxPasswordAgeLimit = getMaxPasswordAgeLimit(ctxt, (String) user.getAttributes(LDAPAttributes.PWD_POLICY_SUB_ENTRY).get(LDAPAttributes.PWD_POLICY_SUB_ENTRY).get());
						userVOs.add(ValueObjectTransformer.createUserVOFromLdapObject(user, userProfile, maxPasswordAgeLimit));
					}
				}
			}
		} catch (final NamingException exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,
					new Exception(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage()));
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
		return userVOs;
	}
	
    /**
     * Read maximum allowed age limit for password from LDAP.
     * @param ctxt
     * @param Password policy of the user
     * @return maxPasswordAgeLimit
     */
    protected long getMaxPasswordAgeLimit(final DirContext ctxt, String passwordPolicy) throws LDAPException {
		return LDAPUtil.getMaxAgeLimitForPassword(ctxt, passwordPolicy);
	}

	/**
	 * Create a user profile vo from incoming user vo
	 * 
	 * @param userVO
	 * @return UserProfile LDAP Object
	 */
	private UserProfile getUserProfileForUser(final UserVO userVO) {
		final UserProfileVO userProfileVO = new UserProfileVO();
		userProfileVO.setUserId(userVO.getUserId());
		userProfileVO.setPredefined(userVO.isPredefined());
		userProfileVO.setRoles(userVO.getRoles());
		final UserProfile userProfile = new UserProfile(userProfileVO);
		return userProfile;
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
	
	/**
	 * Verify if user is allowed to create users.
	 * @param login
	 * @param userVO
	 * @param ctxt
	 * @return
	 * @throws LDAPException
	 */
	protected MESSAGES getCreateAllowedMessage(final LoginVO login, final UserVO userVO, final DirContext ctxt) throws LDAPException {
		return LDAPUtil.isCreateUserAllowed(login, userVO, ctxt);
	}
	
	/**
	 * Verify if user is allowed to modify users.
	 * @param login
	 * @param userVO
	 * @param ctxt
	 * @return
	 * @throws LDAPException
	 */
	protected MESSAGES getModifyAllowedMessage(final LoginVO login, final UserVO userVO, final DirContext ctxt) throws LDAPException {
		return LDAPUtil.isModifyUserAllowed(login, userVO, ctxt);
	}
	
	/**
	 * Verify if user is allowed to delete users.
	 * @param login
	 * @param userVO
	 * @param ctxt
	 * @return
	 * @throws LDAPException
	 */
	protected MESSAGES getDeleteAllowedMessage(final LoginVO login, final UserVO userVO, final DirContext ctxt) throws LDAPException {
		return LDAPUtil.isDeleteUserAllowed(login, userVO, ctxt);
	}
	
	/**
	 * Create an object of UserManagement. Mainly for unit testing.
	 * 
	 * @return ILDAPManagement user management
	 */
	protected ILDAPManagement getUserManagementInstance() {
		return new UserManagement();
	}

	/**
	 * Create an object of UserProfileManagement. Mainly for unit testing.
	 * 
	 * @return ILDAPManagement user profile management
	 */
	protected ILDAPManagement getUserProfileManagementInstance() {
		return new UserProfileManagement();
	}
}
