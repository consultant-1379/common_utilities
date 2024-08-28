package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSION;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_ATTRS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONS_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSION_GROUP;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UNIQUE_MEMBER;
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
import com.ericsson.eniq.ldap.entity.PermissionGroup;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.util.ValidationUtil;

/**
 * LDAP Management implementation class for Permission Group administration.
 * 
 * This class can be used to add/update/delete/find permission groups of
 * user(s).
 * 
 * @author eramano
 *
 */
public class PermissionGroupManagement implements ILDAPManagement {	
	
	@Override
	public MESSAGES create(final ILDAPObject objLdap, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(objLdap,conn);
		final Set<String> permissionsToBeAdded = new HashSet<String>();
		// Add new permission group
		try{
			final Attributes permGroupAttrs = objLdap.getAttributes(COMMON_NAME);
			if ( permGroupAttrs.get(UNIQUE_MEMBER) != null ){
				final Attribute permissionsAttr = permGroupAttrs.get( UNIQUE_MEMBER );
				for ( int permissionsCount = 0 ; permissionsCount < permissionsAttr.size() ; permissionsCount++ ){
					permissionsToBeAdded.add(LDAPUtil.formatAttrValue((String)permissionsAttr.get(permissionsCount)));
				}
			}
			// Proceed only if all permissions assigned to this new permission
			// group is already present in LDAP
			final boolean permissionsValid = this.isAllPermissionsValid(permissionsToBeAdded, conn);
			if ( permissionsValid == true ){
				// Step 1. Build DN for new permission group
				final String permGroupId = (String)permGroupAttrs.get(COMMON_NAME).get();
				final String permGroupDN = buildPermissionGroupDN(permGroupId);
				// Step 2. Add Permission Group
				conn.bind(permGroupDN, objLdap);
			} else {
				throw new LDAPException(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST,
						new Exception(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage()));
			}
		} catch (final NameAlreadyBoundException nabException) {
			throw new LDAPException( MESSAGES.ERR_PERMGROUP_ALREADY_EXISTS,nabException );
		} catch (NamingException namException ){
			throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (final LDAPException exception) {
			throw exception;
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	@Override
	public MESSAGES modify(final ILDAPObject ldapObj, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(ldapObj,conn);
		try {
			final Attributes permGroupAttributes  = ldapObj.getAttributes(COMMON_NAME);
			//read id of permission group
			final String permGroupId = (String)permGroupAttributes.get(COMMON_NAME).get();
			//build the DN of permission group
			final String permGroupDN = buildPermissionGroupDN(permGroupId);
			//Update permission group
			conn.rebind(permGroupDN,ldapObj);
		} catch (NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return MESSAGES.MSG_SUCCESS;
	}

	@Override
	public MESSAGES delete(final ILDAPObject ldapObj, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(ldapObj,conn);
		try {
			final Attributes permGroupAttributes  = ldapObj.getAttributes(COMMON_NAME);
			//read id of permission group
			final String permGroupId = (String)permGroupAttributes.get(COMMON_NAME).get();
			//build the DN of permission group
			final String permGroupDN = buildPermissionGroupDN(permGroupId);
			//Remove references to this permission group from roles
			removePermissionGroupReferenceInRoles(permGroupDN, conn);
			//Remove permission group
			conn.unbind(permGroupDN);
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
		final List<ILDAPObject> permGroups = new ArrayList<ILDAPObject>();
		// Construct a search filter
		final StringBuffer permGroupFilter = new StringBuffer();
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(OBJECT_CLASS);
		permGroupFilter.append(OP_EQUALS);
		permGroupFilter.append(PERMISSION_GROUP);
		permGroupFilter.append(RIGHT_BRACE);
		try {
			// Construct a search control with search filter
			final SearchControls permGroupSearchControl = new SearchControls();
			permGroupSearchControl.setReturningAttributes(PERMISSIONGROUP_ATTRS);
		    permGroupSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		    // Do the search
	    
	    	final NamingEnumeration<SearchResult> results = conn.search(PERMISSIONGROUP_BASE_DN, permGroupFilter.toString(), permGroupSearchControl);
		    while (results.hasMore()) {
			   final SearchResult sr = results.next();
			   final Attributes attrs = sr.getAttributes();
			   final PermissionGroup permGroup =  new PermissionGroup ( attrs );
			   permGroups.add(permGroup);
			}
		} catch (final NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return permGroups;
	}

	@Override
	public ILDAPObject findById(final String id, final DirContext conn) throws LDAPException {
		ValidationUtil.checkIfNull(id,conn);
		PermissionGroup permGroup = null;
		// Construct a search filter
		final StringBuffer permGroupFilter = new StringBuffer();
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(OP_AND);
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(OBJECT_CLASS);
		permGroupFilter.append(OP_EQUALS);
		permGroupFilter.append(PERMISSION_GROUP); 
		permGroupFilter.append(RIGHT_BRACE);
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(LEFT_BRACE);
		permGroupFilter.append(COMMON_NAME);
		permGroupFilter.append(OP_EQUALS);
		permGroupFilter.append(id);
		permGroupFilter.append(RIGHT_BRACE);
		permGroupFilter.append(RIGHT_BRACE);
		permGroupFilter.append(RIGHT_BRACE);
		// Construct a search control with search filter
		final SearchControls permGroupSearchControl = new SearchControls();
		permGroupSearchControl.setReturningAttributes(PERMISSIONGROUP_ATTRS);
	    permGroupSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    // Do the search
	    try {
			final NamingEnumeration<SearchResult> results = searchLDAP(conn, permGroupFilter.toString(), permGroupSearchControl,
					PERMISSIONGROUP_BASE_DN);
		    while (results.hasMore()) {
			   final SearchResult sr = results.next();
			   final Attributes attrs = sr.getAttributes();
			   permGroup =  new PermissionGroup ( attrs );
			}
		} catch (final NamingException namException) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException);
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		return permGroup;
	}	
	
	/**
	 * 
	 * Check if the selected permissions for this permission group are
	 * configured in LDAP.
	 * 
	 * @param conn
	 * @param permissions
	 * @throws LDAPException
	 */
	protected boolean isAllPermissionsValid(final Set<String> permissions, final DirContext conn) throws LDAPException {
		
		// Assume permissions are not valid in the beginning
		boolean allPermissionsValid = false;
		
		// (&(objectclass=permission)(|(cn=permission1)(|cn=permission2)))

		// Construct an LDAP search filter
		final StringBuffer permissionsFilter = new StringBuffer();
		permissionsFilter.append(LEFT_BRACE);
		permissionsFilter.append(OP_AND);
		permissionsFilter.append(LEFT_BRACE);
		permissionsFilter.append(OBJECT_CLASS);
		permissionsFilter.append(OP_EQUALS);
		permissionsFilter.append(PERMISSION);
		permissionsFilter.append(RIGHT_BRACE);
		permissionsFilter.append(LEFT_BRACE);

		if (permissions.size() > 1) {
		permissionsFilter.append(OP_OR);
		}

		for ( final String permission : permissions ){
			permissionsFilter.append(LEFT_BRACE);
			permissionsFilter.append(COMMON_NAME);
			permissionsFilter.append(OP_EQUALS);
			permissionsFilter.append(permission);
			permissionsFilter.append(RIGHT_BRACE);
		}
		permissionsFilter.append(RIGHT_BRACE);
		permissionsFilter.append(RIGHT_BRACE);

		// Construct a search control with search filter
		final SearchControls permissionsSearchControl = new SearchControls();
	    // We are interested only in CN of permission
		final String[] attributeFilter = { COMMON_NAME };
	    permissionsSearchControl.setReturningAttributes(attributeFilter);
	    permissionsSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    int numPermisssionsReturned = 0;
	    // Do the search
	    try {
			final NamingEnumeration<SearchResult> results = searchLDAP(conn, permissionsFilter.toString(), permissionsSearchControl,
					PERMISSIONS_BASE_DN);
	    	while (results.hasMore()) {
			   numPermisssionsReturned++;
				final SearchResult sr = results.next();
				final Attributes attrs = sr.getAttributes();
				final Attribute attr = attrs.get(COMMON_NAME);
			   if ( permissions.contains( attr.get() ) ){
				   allPermissionsValid = true;
			   } else {
				   allPermissionsValid = false; // if one permission is missing, break.
				   break;
			   }
			}
		} catch (final NamingException exception) {
			throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,exception);
		} catch (final Exception exception) {
			throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception);
		}
		
		// Even if allPermissionsValid becomes true after search, number of
		// permissions returned in search should be equal to number of
		// permissions selected.
		if ( allPermissionsValid == true && numPermisssionsReturned != permissions.size() ) {
			allPermissionsValid = false;
		}
		return allPermissionsValid;
	}
	
	/**
	 * Retrieve role(s) having the permission group being deleted as uniqueMember.
	 * Remove uniqueMember refrence from those roles as the group is going to be deleted.
	 * @param permGroupDN DN of the permission group
	 * @param conn LDAP Connection
	 * @throws LDAPException
	 */
	private void removePermissionGroupReferenceInRoles(final String permGroupDN, final DirContext conn) throws LDAPException {
		// retrieve roles having this permission group
		final StringBuffer rolesFilter = new StringBuffer();
		rolesFilter.append(LEFT_BRACE);
		rolesFilter.append(OP_AND);
		rolesFilter.append(LEFT_BRACE);
		rolesFilter.append(OBJECT_CLASS);
		rolesFilter.append(OP_EQUALS);
		rolesFilter.append(ROLE);
		rolesFilter.append(RIGHT_BRACE);
		rolesFilter.append(LEFT_BRACE);
		rolesFilter.append(UNIQUE_MEMBER);
		rolesFilter.append(OP_EQUALS);
		rolesFilter.append(permGroupDN);
		rolesFilter.append(RIGHT_BRACE);
		rolesFilter.append(RIGHT_BRACE);
		// Construct a search control with search filter
		final SearchControls rolesSearchControl = new SearchControls();
	    // We are interested only in CN of role
		final String[] attributeFilter = { COMMON_NAME };
	    rolesSearchControl.setReturningAttributes(attributeFilter);
	    rolesSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		try {
			//find all roles with this permission group
			final NamingEnumeration<SearchResult> results = searchLDAP(conn, rolesFilter.toString(), rolesSearchControl,
					ROLES_BASE_DN);
			// iterate through each roles and delete uniqueMember reference
			while (results.hasMore()) {
				final SearchResult sr = results.next();
				final Attributes attrs = sr.getAttributes();
				final String roleName = (String)attrs.get(COMMON_NAME).get();
				final String roleDN = buildRoleDN(roleName);
				removeUniqueMemberReferenceInRole(roleDN,permGroupDN,conn);
			}	
		} catch (NamingException namException){
			throw new LDAPException(MESSAGES.ERR_FAILED_TO_DELETE_PERMGROUP_FROM_ROLES,namException);
		}
	}

	/**
	 * Remove permission group from a role
	 * @param roleName name of the role
	 * @param permGroupDN DN of permission group
	 * @param conn LDAP Connection
	 * @throws NamingException
	 */
	protected void removeUniqueMemberReferenceInRole(final String roleDN,
			final String permGroupDN, final DirContext conn) throws NamingException {
		final ModificationItem item = 
			new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(UNIQUE_MEMBER,permGroupDN));
		final ModificationItem[] items = {item};
		conn.modifyAttributes(roleDN, items);
	}

	/**
	 * extracted out to assist unit testing
	 * 
	 * @param conn
	 * @param filter
	 * @param searchControl
	 * @param baseDn
	 * @return
	 * @throws NamingException
	 */
	protected NamingEnumeration<SearchResult> searchLDAP(final DirContext conn, final String filter, final SearchControls searchControl,
			final String baseDn) throws NamingException {
		final NamingEnumeration<SearchResult> results = conn.search(baseDn, filter.toString(), searchControl);
		return results;
	}
	
	/**
	 * Build DN for permission group
	 * @param permGroupId name of permission group
	 * @return permission group DN
	 */
	private String buildPermissionGroupDN(final String permGroupId) {
		final StringBuffer permGroupDN = new StringBuffer();
		permGroupDN.append(COMMON_NAME);
		permGroupDN.append(OP_EQUALS);
		permGroupDN.append(permGroupId);
		permGroupDN.append(OP_COMMA);
		permGroupDN.append(PERMISSIONGROUP_BASE_DN);
		return permGroupDN.toString();
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
