package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PREDEFINED_ADMIN_USERS_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PREDEFINED_SYSADMIN_ROLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PREDEFINED_UI_USERS_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UNIQUE_MEMBER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERPROFILES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERS_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER_PROFILE_ATTRS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.UserProfile;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;


/**
 * LDAP Management implementation class for User Profile administration.
 * 
 * This class can be used to add/update/delete/find user profiles of user(s).
 * 
 * @author eramano
 *
 */
public class UserProfileManagement implements ILDAPManagement {

	@Override
	public MESSAGES create(final ILDAPObject ldapObj, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(ldapObj,conn);
		// Add new role
		try{
			final Set<String> rolesToBeAdded = getUserRoles(ldapObj);
			// Proceed only if all roles assigned to this new user is already present in LDAP
			final boolean rolesValid = isAllRolesValid(rolesToBeAdded, conn);
			if ( rolesValid == true ){
				//Get UID for new user profile
				String uid = (String)ldapObj.getAttributes(UID).get(UID).get();
				//build the DN for new User Profile
				final String userProfileDN = buildUserProfileDN(uid);
				// Add new user profile
				conn.bind(userProfileDN, ldapObj);
				// update login profiles for modified user
				addUserToLoginProfilesByRoles(uid,rolesToBeAdded,conn);
			} else {
				throw new LDAPException(MESSAGES.ERR_SELECTED_ROLES_DO_NOT_EXIST,
						new Exception(MESSAGES.ERR_SELECTED_ROLES_DO_NOT_EXIST.getMessage()));
			}
		} catch (NameAlreadyBoundException nabException ){
			throw new LDAPException( MESSAGES.ERR_USER_PROFILE_ALREADY_EXISTS,nabException);
		} catch (NamingException exception ){
			throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
		} catch (LDAPException exception ){
			throw exception;
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	@Override
	public MESSAGES modify(final ILDAPObject ldapObj, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(ldapObj,conn);
		// Modify a role
		try{
			final Set<String> rolesToBeAdded = getUserRoles(ldapObj);
			// Proceed only if all roles assigned to this new user is already present in LDAP
			final boolean rolesValid = isAllRolesValid(rolesToBeAdded, conn);
			if ( rolesValid == true ){
				//Get UID for new user profile
				String uid = (String)ldapObj.getAttributes(UID).get(UID).get();
				final String usePerformingModify = getLoginUserFromConnection(conn);
				// No user, even admins, can self modify their profiles 
				if ( !usePerformingModify.equals(uid) ) {
					removeUserFromLoginProfiles(uid, conn);
					//build the DN for User Profile
					final String userProfileDN = buildUserProfileDN(uid);
					// Modify user profile
					conn.rebind(userProfileDN, ldapObj);
					// update login profiles for modified user
					addUserToLoginProfilesByRoles(uid,rolesToBeAdded,conn);
				}
			} else {
				throw new LDAPException(MESSAGES.ERR_SELECTED_ROLES_DO_NOT_EXIST,
						new Exception(MESSAGES.ERR_SELECTED_ROLES_DO_NOT_EXIST.getMessage()));
			}
		} catch (NamingException exception ){
			throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
		} catch (LDAPException exception ){
			throw exception;
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	/**
	 * Retrieve user performing actions from connection object.
	 * @param conn
	 * @return
	 * @throws NamingException
	 */
	protected String getLoginUserFromConnection(final DirContext conn) throws NamingException {
		final String loginUser = (String)conn.getEnvironment().get(javax.naming.Context.SECURITY_PRINCIPAL);
		return LDAPUtil.formatAttrValue(loginUser);
	}

	@Override
	public MESSAGES delete(final ILDAPObject ldapObj, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(ldapObj,conn);
		try {
			final Attributes userProfileAttributes  = ldapObj.getAttributes(UID);
			//read user name
			final String userId = (String)userProfileAttributes.get(UID).get();
			//remove this user from login profiles
			removeUserFromLoginProfiles(userId, conn);
			//build the DN of user profile
			final String userProfileDN = buildUserProfileDN(userId);
			//Remove user profile
			conn.unbind(userProfileDN);
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
		List<ILDAPObject> userProfiles = new ArrayList<ILDAPObject>();
		// Construct a search filter
		final String userProfileFilter = LDAPUtil.getAllUserProfileSearchFilter();
		// Construct a search control with search filter
		final SearchControls userProfileSearchControl = new SearchControls();
		userProfileSearchControl.setReturningAttributes(USER_PROFILE_ATTRS);
	    userProfileSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    // Do the search
	    try {
	    	final NamingEnumeration<SearchResult> results =
	    		conn.search(USERPROFILES_BASE_DN, userProfileFilter.toString(), userProfileSearchControl);
		    while (results.hasMore()) {
			   final SearchResult sr = results.next();
			   final Attributes attrs = sr.getAttributes();
			   final UserProfile userProfile =  new UserProfile ( attrs );
			   userProfiles.add(userProfile);
			}
		}catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		}catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return userProfiles;
	}

	@Override
	public ILDAPObject findById(final String id, final DirContext conn)
			throws LDAPException {
		ValidationUtil.checkIfNull(id,conn);
		UserProfile userProfile = null;
		final String userProfileFilter = LDAPUtil.getUserProfileSearchFilterById(id);
		// Construct a search control with search filter
		final SearchControls userProfilesSearchControl = new SearchControls();
	    userProfilesSearchControl.setReturningAttributes(USER_PROFILE_ATTRS);
	    userProfilesSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    // Do the search
	    try {
	    	final NamingEnumeration<SearchResult> results =
	    		conn.search(USERPROFILES_BASE_DN, userProfileFilter, userProfilesSearchControl);
			if ( null != results ) {
				while ( results.hasMore() ) {
				   final SearchResult sr = results.next();
				   final Attributes attrs = sr.getAttributes();
				   userProfile = new UserProfile(attrs);
				}	
			}
		}catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		}catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return userProfile;
	}
	
	/**
	 * Get roles of a user from incoming ldap object
	 * @param ldapObj
	 * @return set of roles
	 * @throws NamingException
	 */
	private Set<String> getUserRoles(final ILDAPObject ldapObj) throws NamingException {
		final Set<String> rolesToBeAdded = new HashSet<String>();
		final Attributes userProfileAttrs = ldapObj.getAttributes(UID);
		if ( userProfileAttrs != null ){
			final Attribute roleAttr = userProfileAttrs.get( UNIQUE_MEMBER );
			for ( int rolesCount = 0 ; rolesCount < roleAttr.size() ; rolesCount++ ){
				rolesToBeAdded.add(LDAPUtil.formatAttrValue((String)roleAttr.get(rolesCount)));
			}
		}
		return rolesToBeAdded;
	}
	
	/**
	 * Check if the selected roles for this user is existing in LDAP.
	 * 
	 * @param conn
	 * @param roles
	 * @throws LDAPException
	 */
	protected boolean isAllRolesValid(final Set<String> roles, final DirContext conn ) throws LDAPException {
		// Assume roles are not valid in the beginning
		boolean allRolesValid = false;
		// Construct an LDAP search filter
		final String rolesFilter = LDAPUtil.getSpecifiedRolesSearchFilter(roles);
		// Construct a search control with search filter
		final SearchControls rolesSearchControl = new SearchControls();
	    // We are interested only in CN for role
		final String[] attributeFilter = { COMMON_NAME };
	    rolesSearchControl.setReturningAttributes(attributeFilter);
	    rolesSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    int numRolesReturned = 0;
	    // Do the search
	    try {
	    	NamingEnumeration<SearchResult> results = conn.search(ROLES_BASE_DN, rolesFilter.toString(), rolesSearchControl);
	    	while (results.hasMore()) {
			   numRolesReturned++;
			   SearchResult sr = results.next();
			   Attributes attrs = sr.getAttributes();
			   Attribute attr = attrs.get(COMMON_NAME);
			   if ( roles.contains( attr.get() ) ){
				   allRolesValid = true;
			   } else {
				   allRolesValid = false; // if one role is missing, break.
				   break;
			   }
			}
		}catch ( NamingException exception ){
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
		}catch ( Exception exception ){
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		// Even if allRolesValid becomes true after search, number of roles returned in search should be equal to number of roles selected.
		if ( allRolesValid == true && numRolesReturned != roles.size() ) {
			allRolesValid = false;
		}
		return allRolesValid;
	}
	
	/**
	 * Add a user to login profiles (Admin Users/UI Users) when the user is being created/modified.
	 * 
	 * @param uid
	 * @param rolesToBeAdded
	 * @param conn
	 * @throws NamingException
	 */
	private void addUserToLoginProfilesByRoles(final String uid, final Set<String> rolesToBeAdded,
			final DirContext conn) throws NamingException {
		if ( rolesToBeAdded.contains(PREDEFINED_SYSADMIN_ROLE)){
			//if sysadmin, add to cn=Admin Users,ou=roles,dc=ericsson,dc=se
			updateLoginProfiles(uid, PREDEFINED_ADMIN_USERS_DN, DirContext.ADD_ATTRIBUTE, conn);	
			// and to cn=UI Users,ou=roles,dc=ericsson,dc=se
			updateLoginProfiles(uid, PREDEFINED_UI_USERS_DN, DirContext.ADD_ATTRIBUTE,conn);
		} else {
			// add to cn=UI Users,ou=roles,dc=ericsson,dc=se
			updateLoginProfiles(uid, PREDEFINED_UI_USERS_DN, DirContext.ADD_ATTRIBUTE, conn);
		}
	}
	
	/**
	 * Remove a user from login profiles (Admin Users/UI Users) when the user is being deleted
	 * 
	 * @param userId
	 * @param conn
	 * @throws LDAPException
	 * @throws NamingException
	 */
	private void removeUserFromLoginProfiles(final String userId, final DirContext conn) throws LDAPException,NamingException {
		final boolean isAdminUser = isAdminUser(userId, conn);
		if (isAdminUser){
			//if sysadmin, remove from cn=Admin Users,ou=roles,dc=ericsson,dc=se
			updateLoginProfiles(userId, PREDEFINED_ADMIN_USERS_DN, DirContext.REMOVE_ATTRIBUTE, conn);	
			// and from cn=UI Users,ou=roles,dc=ericsson,dc=se
			updateLoginProfiles(userId, PREDEFINED_UI_USERS_DN, DirContext.REMOVE_ATTRIBUTE, conn);
		} else {
			// remove from cn=UI Users,ou=roles,dc=ericsson,dc=se
			updateLoginProfiles(userId, PREDEFINED_UI_USERS_DN, DirContext.REMOVE_ATTRIBUTE, conn);
		}
	}
	
	/**
	 * Users can be sysadmins (access to AdminUI and Events UI) or UI users (only to Events UI).
	 * 
	 * When a user is created, add him to Admin Users and/or UI users depending on his role.
	 * When a user is deleted, remove him from Admin Users and/or UI users depending on his role. 
	 * 
	 * @param uid User ID
	 * @param loginRoleDN Role DN (Admin Users/UI Users)
	 * @param operation DirContext.ADD_ATTRIBUTE or DirContext.REMOVE_ATTRIBUTE
	 * @param conn connection
	 * 
	 * @throws NamingException
	 */
	protected void updateLoginProfiles(final String uid, final String loginRoleDN, final int operation, final DirContext conn)
			throws NamingException {
		final String userDN = buildUserDN(uid);
		final ModificationItem item = 
			new ModificationItem(operation, new BasicAttribute(UNIQUE_MEMBER,userDN));
		final ModificationItem[] items = {item};
		conn.modifyAttributes(loginRoleDN, items);
	}
	
	/**
	 * Check if user is an admin user
	 * @param userId
	 * @param conn
	 * @return true if admin user. else, false.
	 * @throws LDAPException
	 * @throws NamingException
	 */
	protected boolean isAdminUser(final String userId, final DirContext conn ) throws LDAPException,NamingException {
		return LDAPUtil.isAdminUser(userId, conn);
	}
	
	/**
	 * Build DN for user profile
	 * @param userProfileId name of user profile
	 * @return user profile DN
	 */
	private String buildUserProfileDN(final String userProfileId) {
		final StringBuffer userProfileDN = new StringBuffer();
		userProfileDN.append(UID);
		userProfileDN.append(OP_EQUALS);
		userProfileDN.append(userProfileId);
		userProfileDN.append(OP_COMMA);
		userProfileDN.append(USERPROFILES_BASE_DN);
		return userProfileDN.toString();
	}
	
	/**
	 * Build DN for user
	 * @param uid user id
	 * @return user DN
	 */
	private String buildUserDN(final String uid) {
		final StringBuilder userDN = new StringBuilder(UID);
		userDN.append(OP_EQUALS);
		userDN.append(uid);
		userDN.append(OP_COMMA);
		userDN.append(USERS_BASE_DN);
		return userDN.toString();
	}
}
