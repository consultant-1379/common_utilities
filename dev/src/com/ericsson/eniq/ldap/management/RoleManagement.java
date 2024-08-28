package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSION_GROUP;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLE_ATTRS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UNIQUE_MEMBER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERPROFILES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER_PROFILE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.LEFT_BRACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_AND;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_OR;
import static com.ericsson.eniq.ldap.util.LDAPConstants.RIGHT_BRACE;

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
import com.ericsson.eniq.ldap.entity.Role;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;

/**
 * LDAP Management implementation class for Role administration.
 * 
 * This class can be used to add/update/delete/find roles of user(s).
 * 
 * @author eramano
 * 
 */
public class RoleManagement implements ILDAPManagement {

	@Override
	public MESSAGES create(final ILDAPObject objLdap, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(objLdap,conn);
		// Add new role
		try {
			final Set<String> permGroupsToBeAdded = new HashSet<String>();
			final Attributes roleAttrs = objLdap.getAttributes(COMMON_NAME);
			if (roleAttrs.get(UNIQUE_MEMBER) != null) {
				final Attribute permGroupsAttr = roleAttrs.get(UNIQUE_MEMBER);
				for (int permGroupsCount = 0; permGroupsCount < permGroupsAttr.size(); permGroupsCount++) {
					permGroupsToBeAdded.add(LDAPUtil.formatAttrValue((String) permGroupsAttr.get(permGroupsCount)));
				}
			}
			// Proceed only if all permissions assigned to this new permission
			// group is already present in LDAP
			final boolean permGroupsValid = this.isAllPermissionGroupsValid(permGroupsToBeAdded, conn);
			if (permGroupsValid == true) {
				// Step 1. Build DN for new role
				final String roleDN = buildRoleDN((String) roleAttrs.get(COMMON_NAME).get());
				// Step 2. Add role
				conn.bind(roleDN, objLdap);
			} else {
				throw new LDAPException(MESSAGES.ERR_SELECTED_PERMGROUPS_DO_NOT_EXIST,
						new Exception(MESSAGES.ERR_SELECTED_PERMGROUPS_DO_NOT_EXIST.getMessage()));
			}
		} catch (final NameAlreadyBoundException nabException) {
			throw new LDAPException(MESSAGES.ERR_ROLE_ALREADY_EXISTS,nabException);
		} catch (final LDAPException exception) {
			throw exception;
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	@Override
	public MESSAGES modify(final ILDAPObject objLdap, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(objLdap,conn);
		try {
			final Attributes roleAttributes  = objLdap.getAttributes(COMMON_NAME);
			//read role name
			final String roleId = (String)roleAttributes.get(COMMON_NAME).get();
			//build the DN of role
			final String roleDN = buildRoleDN(roleId);
			//Update role
			conn.rebind(roleDN,objLdap);
		} catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	@Override
	public MESSAGES delete(final ILDAPObject role, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(role,conn);
		try {
			final Attributes roleAttributes  = role.getAttributes(COMMON_NAME);
			//read role name
			final String roleId = (String)roleAttributes.get(COMMON_NAME).get();
			//build the DN of role
			final String roleDN = buildRoleDN(roleId);
			//Remove references to this role from user profiles
			removeRoleReferenceInRoles(roleDN, conn);
			//Remove role
			conn.unbind(roleDN);
		} catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (LDAPException exception) {
			throw exception;
		}catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	@Override
	public List<ILDAPObject> findAll(final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(conn);
		final List<ILDAPObject> roles = new ArrayList<ILDAPObject>();
		// Construct a search filter
		final StringBuffer roleFilter = new StringBuffer();
		roleFilter.append(LEFT_BRACE);
		roleFilter.append(OBJECT_CLASS);
		roleFilter.append(OP_EQUALS);
		roleFilter.append(ROLE);
		roleFilter.append(RIGHT_BRACE);

		// Construct a search control with search filter
		final SearchControls roleSearchControl = new SearchControls();
		roleSearchControl.setReturningAttributes(ROLE_ATTRS);
		roleSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// Do the search
		try {
			final NamingEnumeration<SearchResult> results = conn.search(ROLES_BASE_DN, roleFilter.toString(), roleSearchControl);
			while (results.hasMore()) {
				final SearchResult sr = results.next();
				final Attributes attrs = sr.getAttributes();
				final Role role = new Role(attrs);
				roles.add(role);
			}
		} catch (final NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return roles;
	}

	@Override
	public ILDAPObject findById(final String id, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(id,conn);
		Role role = null;
		// Construct a search filter
		final StringBuffer roleFilter = new StringBuffer();
		roleFilter.append(LEFT_BRACE);
		roleFilter.append(OP_AND);
		roleFilter.append(LEFT_BRACE);
		roleFilter.append(OBJECT_CLASS);
		roleFilter.append(OP_EQUALS);
		roleFilter.append(ROLE);
		roleFilter.append(RIGHT_BRACE);
		roleFilter.append(LEFT_BRACE);

		roleFilter.append(LEFT_BRACE);
		roleFilter.append(COMMON_NAME);
		roleFilter.append(OP_EQUALS);
		roleFilter.append(id);
		roleFilter.append(RIGHT_BRACE);
		roleFilter.append(RIGHT_BRACE);
		roleFilter.append(RIGHT_BRACE);
		// Construct a search control with search filter
		final SearchControls roleSearchControl = new SearchControls();
		roleSearchControl.setReturningAttributes(ROLE_ATTRS);
		roleSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// Do the search
		try {
			final NamingEnumeration<SearchResult> results = conn.search(ROLES_BASE_DN, roleFilter.toString(), roleSearchControl);
			while (results.hasMore()) {
				final SearchResult sr = results.next();
				final Attributes attrs = sr.getAttributes();
				role = new Role(attrs);
			}
		} catch (final NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return role;
	}

	/**
	 * 
	 * Check if the selected permission groups for this role are configured in
	 * LDAP.
	 * 
	 * @param conn
	 * @param permission
	 *            groups
	 * @throws LDAPException
	 */
	protected boolean isAllPermissionGroupsValid(final Set<String> permissionGroups, final DirContext conn) throws LDAPException {

		// Assume permission groups are not valid in the beginning
		boolean allPermGroupsValid = false;

		// Construct an LDAP search filter
		final StringBuffer permGroupFilter = new StringBuffer();
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(OP_AND);
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(OBJECT_CLASS);
		permGroupFilter.append(OP_EQUALS);
		permGroupFilter.append(PERMISSION_GROUP);
		permGroupFilter.append(RIGHT_BRACE);
		permGroupFilter.append(LEFT_BRACE);

		if (permissionGroups.size() > 1) {
			permGroupFilter.append(OP_OR);
		}

		for (final String permGroup : permissionGroups) {
			permGroupFilter.append(LEFT_BRACE);
			permGroupFilter.append(COMMON_NAME);
			permGroupFilter.append(OP_EQUALS);
			permGroupFilter.append(permGroup);
			permGroupFilter.append(RIGHT_BRACE);
		}

		permGroupFilter.append(RIGHT_BRACE);
		permGroupFilter.append(RIGHT_BRACE);
		// Construct a search control with search filter
		final SearchControls permGroupSearchControl = new SearchControls();
		// We are interested only in CN of permission group
		final String[] attributeFilter = { COMMON_NAME };
		permGroupSearchControl.setReturningAttributes(attributeFilter);
		permGroupSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		int numPermGroupsReturned = 0;
		// Do the search
		try {
			final NamingEnumeration<SearchResult> results = conn.search(PERMISSIONGROUP_BASE_DN, permGroupFilter.toString(), permGroupSearchControl);
			while (results.hasMore()) {
				numPermGroupsReturned++;
				final SearchResult sr = results.next();
				final Attributes attrs = sr.getAttributes();
				final Attribute attr = attrs.get(COMMON_NAME);
				if (permissionGroups.contains(attr.get())) {
					allPermGroupsValid = true;
				} else {
					allPermGroupsValid = false; // if one permission group is
												// missing, break.
					break;
				}
			}
		} catch (final NamingException exception) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}

		// Even if allPermGroupsValid becomes true after search, number of
		// permission group returned in search should be equal to number of
		// permissio groups selected.
		if (allPermGroupsValid == true && numPermGroupsReturned != permissionGroups.size()) {
			allPermGroupsValid = false;
		}
		return allPermGroupsValid;
	}
	
	/**
	 * Retrieve user profile(s) having the role being deleted as uniqueMember.
	 * Remove uniqueMember refrence from those user profiles as the role is going to be deleted.
	 * @param roleDN DN of the role
	 * @param conn LDAP Connection
	 * @throws LDAPException
	 */
	private void removeRoleReferenceInRoles(final String roleDN, final DirContext conn) throws LDAPException {
		// retrieve user profiles having this role
		final StringBuffer userProfileFilter = new StringBuffer();
		userProfileFilter.append(LEFT_BRACE);
		userProfileFilter.append(OP_AND);
		userProfileFilter.append(LEFT_BRACE);
		userProfileFilter.append(OBJECT_CLASS);
		userProfileFilter.append(OP_EQUALS);
		userProfileFilter.append(USER_PROFILE);
		userProfileFilter.append(RIGHT_BRACE);
		userProfileFilter.append(LEFT_BRACE);
		userProfileFilter.append(UNIQUE_MEMBER);
		userProfileFilter.append(OP_EQUALS);
		userProfileFilter.append(roleDN);
		userProfileFilter.append(RIGHT_BRACE);
		userProfileFilter.append(RIGHT_BRACE);
		// Construct a search control with search filter
		final SearchControls userProfileSearchControl = new SearchControls();
	    // We are interested only in UID of user profile
		final String[] attributeFilter = { UID };
	    userProfileSearchControl.setReturningAttributes(attributeFilter);
	    userProfileSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		try {
			//find all user profiles with this role
			final NamingEnumeration<SearchResult> results = conn.search(USERPROFILES_BASE_DN, userProfileFilter.toString(), userProfileSearchControl);
			// iterate through each user profiles and delete uniqueMember reference
			while (results.hasMore()) {
				final SearchResult sr = results.next();
				final Attributes attrs = sr.getAttributes();
				final String userProfileName = (String)attrs.get(UID).get();
				final String userProfileDN = buildUserProfileDN(userProfileName);
				removeUniqueMemberReferenceInUserProfile(userProfileDN,roleDN,conn);
			}	
		} catch (NamingException namException){
			throw new LDAPException(MESSAGES.ERR_FAILED_TO_DELETE_ROLES_FROM_USER_PROFILES,namException);
		}
	}

	/**
	 * Remove role from a user profile
	 * @param userProfileDN DN of user profile
	 * @param roleDN DN of role
	 * @param conn LDAP Connection
	 * @throws NamingException
	 */
	protected void removeUniqueMemberReferenceInUserProfile(final String userProfileDN,
			final String roleDN, final DirContext conn) throws NamingException {
		final ModificationItem item = 
			new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(UNIQUE_MEMBER,roleDN));
		final ModificationItem[] items = {item};
		conn.modifyAttributes(userProfileDN, items);
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
	 * Build DN for role
	 * @param roleId name of role
	 * @return role DN
	 */
	private String buildRoleDN(final String roleId) {
		final StringBuffer roleDN = new StringBuffer();
		roleDN.append(COMMON_NAME);
		roleDN.append(OP_EQUALS);
		roleDN.append(roleId);
		roleDN.append(OP_COMMA);
		roleDN.append(ROLES_BASE_DN);
		return roleDN.toString();
	}
}
