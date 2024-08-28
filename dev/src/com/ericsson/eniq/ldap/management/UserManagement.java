package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.FALSE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.RESET_PWD_ONLOGIN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERS_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER_PASSWORD;
import static com.ericsson.eniq.ldap.util.LDAPConstants.LEFT_BRACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_AND;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.PASSWORD_IN_PASSWORD_HISTORY_EXCEPTION_MESSAGE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.PASSWORD_PLACEHOLDER;
import static com.ericsson.eniq.ldap.util.LDAPConstants.RIGHT_BRACE;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.User;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;


/**
 * Class does CRUD operations for User.
 * 
 * @author etonnee
 * @author eramano
 *
 */
public class UserManagement implements ILDAPManagement {

	@Override
	public MESSAGES create(final ILDAPObject ldapObj, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(ldapObj,conn);
		try {
			//Get UID for new user
			final String uid = (String)ldapObj.getAttributes(UID).get(UID).get();
			//build the DN for new User
			final String userDN = buildUserDN(uid);
			// Add new user
			conn.bind(userDN, ldapObj);
		} catch (NameAlreadyBoundException nabException ){
			throw new LDAPException( MESSAGES.ERR_USER_ALREADY_EXISTS,nabException);
		} catch (NamingException exception) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}
	
	@Override
	public MESSAGES modify(final ILDAPObject objLdap, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(objLdap,conn);
		try {
			final Attributes userAttributes = objLdap.getAttributes(UID);
			//Get uid of user being modified
			final String uid = (String)userAttributes.get(UID).get();
			//build the DN for new User
			final String userDN = buildUserDN(uid);
			// iterate through user attributes and modify one by one
			// if password is unchanged, ignore the attribute
			NamingEnumeration<String> userAttrIDs = userAttributes.getIDs();
			while(userAttrIDs.hasMore()){
				final String attrID = userAttrIDs.next();
				if ( USER_PASSWORD.equals(attrID) && PASSWORD_PLACEHOLDER.equals(userAttributes.get(attrID).get()) ){
					userAttributes.remove(attrID);
				} else if ( RESET_PWD_ONLOGIN.equals(attrID) && isResetPasswordOnLoginEnabled(uid,conn) ){
					userAttributes.remove(attrID);
				}
			}
			conn.modifyAttributes(userDN,DirContext.REPLACE_ATTRIBUTE,userAttributes);
		} catch (NamingException exception) {
			if ( exception.getMessage().contains(PASSWORD_IN_PASSWORD_HISTORY_EXCEPTION_MESSAGE) ) {
				throw new LDAPException(MESSAGES.ERR_PASSWORD_IN_PASSWORD_HISTORY, exception);
			} else {
				throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
			}
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}
	
	@Override
	public MESSAGES delete(final ILDAPObject user, final DirContext conn)throws LDAPException  {
		ValidationUtil.checkIfNull(user,conn);
		try {
			final Attributes userAttributes = user.getAttributes(UID);
			//read UID of user
			final String uid = (String)userAttributes.get(UID).get();
			//build the DN for user
			final String userDN = buildUserDN(uid);
			//Remove user
			conn.unbind(userDN);
		} catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}
	
	@Override
	public List<ILDAPObject> findAll(final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(conn);
		List<ILDAPObject> users = new ArrayList<ILDAPObject>();
		// Construct a search filter
		final StringBuffer userFilter = new StringBuffer();
		userFilter.append(LEFT_BRACE);
		userFilter.append(OBJECT_CLASS);
		userFilter.append(OP_EQUALS);
		userFilter.append(USER);
		userFilter.append(RIGHT_BRACE);
		// Construct a search control with search filter
		final SearchControls userSearchControl = new SearchControls();
		userSearchControl.setReturningAttributes(LDAPAttributes.USER_ATTRS);
	    userSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    // Do the search
	    try {
	    	final NamingEnumeration<SearchResult> results = conn.search(USERS_BASE_DN, userFilter.toString(), userSearchControl);
		    while (results.hasMore()) {
			   final SearchResult sr = results.next();
			   final Attributes attrs = sr.getAttributes();
			   final User user =  new User ( attrs );
			   users.add(user);
			}
		}catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		}catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return users;
	}

	@Override
	public ILDAPObject findById(final String id, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(id,conn);
		User user = null;
		// Construct a search filter
		final String userFilter = buildUserSearchFilter(id);	
		// Construct a search control with search filter
		final SearchControls userSearchControl = new SearchControls();
		userSearchControl.setReturningAttributes(LDAPAttributes.USER_ATTRS);
	    userSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    // Do the search
	    try {
	    	final NamingEnumeration<SearchResult> results = conn.search(USERS_BASE_DN, userFilter, userSearchControl);
			while (results.hasMore()) {
			   final SearchResult sr = results.next();
			   final Attributes attrs = sr.getAttributes();
			   user =  new User(attrs);
			}
		}catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		}catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return user;
	}
	
	/**
	 * Check if resetPasswordOnLogin is already set to true.
	 * @param uid user id
	 * @param conn connection
	 * @return 
	 */
	private boolean isResetPasswordOnLoginEnabled(final String uid,final DirContext conn) throws NamingException, LDAPException{
		boolean resetPasswordEnabled = true;
		// find existing user
		final User existingUser = (User)findById( uid, conn);
		// check if resetPasswordOnLogin is set to false
	    if (FALSE.equals(existingUser.getAttributes(UID).get(RESET_PWD_ONLOGIN).get())){
	    	resetPasswordEnabled = false;
	    } else {
	    	resetPasswordEnabled = true;
	    }
	    return resetPasswordEnabled;
	}
	
	/**
	 * Build DN for user
	 * @param userId user name
	 * @return user DN
	 */
	private String buildUserDN(final String userId) {
		final StringBuffer userDN = new StringBuffer();
		userDN.append(UID);
		userDN.append(OP_EQUALS);
		userDN.append(userId);
		userDN.append(OP_COMMA);
		userDN.append(USERS_BASE_DN);
		return userDN.toString();
	}
	
	/**
	 * Build user search filter for a single user
	 * @param userId
	 * @return
	 */
	private String buildUserSearchFilter(final String userId){
		final StringBuffer userFilter = new StringBuffer();
		userFilter.append(LEFT_BRACE);
		userFilter.append(OP_AND);
		userFilter.append(LEFT_BRACE);
		userFilter.append(OBJECT_CLASS);
		userFilter.append(OP_EQUALS);
		userFilter.append(USER);
		userFilter.append(RIGHT_BRACE);
		userFilter.append(LEFT_BRACE);
		userFilter.append(LEFT_BRACE);
		userFilter.append(UID);
		userFilter.append(OP_EQUALS);
		userFilter.append(userId);
		userFilter.append(RIGHT_BRACE);
		userFilter.append(RIGHT_BRACE);
		userFilter.append(RIGHT_BRACE);
		return userFilter.toString();
	}
}
