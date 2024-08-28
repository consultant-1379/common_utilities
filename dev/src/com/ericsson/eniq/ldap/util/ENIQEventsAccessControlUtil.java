package com.ericsson.eniq.ldap.util;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_OBJECTCLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLE_OBJECTCLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UNIQUE_MEMBER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERPROFILES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERPROFILE_OBJECTCLASS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.LEFT_BRACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_AND;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.RIGHT_BRACE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.ericsson.eniq.ldap.management.LDAPConnectionFactory;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;

/**
 * Utility class to find permissions of a user for enforcing role based access control in ENIQ Events UI.
 * 
 * @author eramano
 *
 */
public final class ENIQEventsAccessControlUtil {

    /**
     * Private constructor to avoid instantiation. 
     */
    private ENIQEventsAccessControlUtil() {
        //do nothing
    }

    /**
     * Retrieve permissions of a user.
     * @param loginVO Requesting party's login credentials
     * @return unique set of permissions
     * @throws LDAPException if failed to retrieve permissions from ENIQ Events Directory server
     */
    public static Set<String> findPermissionsByUserId(final IValueObject loginVO) throws LDAPException {
        ValidationUtil.checkIfNull(loginVO);
        // There could be maximum 4 permissions for a user 
        final Set<String> uniquePermissions = new HashSet<String>(4);
        DirContext ctxt = null;
        final LoginVO login = (LoginVO) loginVO;
        try {
            ctxt = LDAPConnectionFactory.getConnection(login.getLoginId(), login.getPassword());
            if (ctxt != null) {
                // Find roles of user from user profile
                final List<String> roles = findRolesOfUser(login.getLoginId(), ctxt);
                // For each role, find the permission groups attached
                // One permission group might be mapped to more than one role
                // so get only unique set of permission groups
                final Set<String> uniquePermissionGroups = new HashSet<String>();
                for (final String role : roles) {
                    final List<String> permissionGroups = findPermissionGroupsOfRole(role, ctxt);
                    for (final String permGroup : permissionGroups) {
                        uniquePermissionGroups.add(permGroup);
                    }
                }
                // For each unique permission group, find the permissions attached
                // One permission might be mapped to more than one permission group
                // so get only unique set of permissions
                for (final String permissionGroup : uniquePermissionGroups) {
                    final List<String> permissions = findPermissionsOfPermissionGroup(permissionGroup, ctxt);
                    for (final String permission : permissions) {
                        uniquePermissions.add(permission);
                    }
                }
            }
        } catch (final LDAPException ldapException) {
            throw ldapException;
        } catch (final Exception exception) {
            throw new LDAPException(MESSAGES.ERR_UNABLE_TO_RETRIEVE_PERM_FOR_USER, exception);
        } finally {
            try {
                if (null != ctxt) {
                    ctxt.close();
                }
            } catch (final Exception exception) {
                throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_CONNECTION_CLOSE_EXCEPTION, exception);
            }
        }
        return uniquePermissions;
    }

    /**
     * Find roles of a user based on user id.
     * TODO Use UserManagement API
     * @param userId
     * @param conn
     * @return list of roles
     * @throws LDAPException
     */
    private static List<String> findRolesOfUser(final String userId, final DirContext conn) throws LDAPException {
        // Retrieve User Profile and get roles mapped to user 
        final StringBuilder userProfileFilter = new StringBuilder(LEFT_BRACE).append(OP_AND).append(LEFT_BRACE)
                .append(USERPROFILE_OBJECTCLASS).append(RIGHT_BRACE).append(LEFT_BRACE).append(LEFT_BRACE).append(UID)
                .append(OP_EQUALS).append(userId).append(RIGHT_BRACE).append(RIGHT_BRACE).append(RIGHT_BRACE);

        return findUniqueMembers(userProfileFilter.toString(), USERPROFILES_BASE_DN, conn);
    }

    /**
     * Find Permission Groups of a role based on role id.
     * TODO Use RoleManagement API
     * @param userId
     * @param conn
     * @return list of permission groups
     * @throws LDAPException
     */
    private static List<String> findPermissionGroupsOfRole(final String role, final DirContext conn)
            throws LDAPException {
        // Retrieve role and get permission groups mapped to role
        final StringBuilder rolesFilter = new StringBuilder(LEFT_BRACE).append(OP_AND).append(LEFT_BRACE)
                .append(ROLE_OBJECTCLASS).append(RIGHT_BRACE).append(LEFT_BRACE).append(LEFT_BRACE).append(COMMON_NAME)
                .append(OP_EQUALS).append(role).append(RIGHT_BRACE).append(RIGHT_BRACE).append(RIGHT_BRACE);

        return findUniqueMembers(rolesFilter.toString(), ROLES_BASE_DN, conn);
    }

    /**
     * Find Permissions in a permission group based on group id.
     * TODO Use PermissionGroupManagement API
     * @param userId
     * @param conn
     * @return list of permissions
     * @throws LDAPException
     */
    private static List<String> findPermissionsOfPermissionGroup(final String permissionGroup, final DirContext conn)
            throws LDAPException {
        // Retrieve permission group and get permissions associated with group
        final StringBuilder permGroupFilter = new StringBuilder(LEFT_BRACE).append(OP_AND).append(LEFT_BRACE)
                .append(PERMISSIONGROUP_OBJECTCLASS).append(RIGHT_BRACE).append(LEFT_BRACE).append(LEFT_BRACE)
                .append(COMMON_NAME).append(OP_EQUALS).append(permissionGroup).append(RIGHT_BRACE).append(RIGHT_BRACE)
                .append(RIGHT_BRACE);

        return findUniqueMembers(permGroupFilter.toString(), PERMISSIONGROUP_BASE_DN, conn);
    }

    /**
     * Retrieve unique members of a LDAP entry (user/role/permissiongroup)
     * @param searchFilter
     * @param searchBase
     * @param conn
     * @return list of unique members
     * @throws LDAPException
     */
    private static List<String> findUniqueMembers(final String searchFilter, final String searchBase,
            final DirContext conn) throws LDAPException {
        final List<String> uniqueMemberValues = new ArrayList<String>();
        // Construct a search control with search filter
        final SearchControls searchControl = new SearchControls();
        // search only for uniqueMember attributes
        final String[] attributeFilter = { UNIQUE_MEMBER };
        searchControl.setReturningAttributes(attributeFilter);
        searchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Do the search
        try {
            final NamingEnumeration<SearchResult> results = conn.search(searchBase, searchFilter, searchControl);
            while (results.hasMore()) {
                final SearchResult sr = results.next();
                final Attributes attrs = sr.getAttributes();
                if ( null != attrs ) {
                	final Attribute attr = attrs.get(UNIQUE_MEMBER);
                	if ( null != attr ) {
                		for (int index = 0; index < attr.size(); index++) {
                			String attrValue = (String) attr.get(index);
                			attrValue = LDAPUtil.formatAttrValue(attrValue);
                			uniqueMemberValues.add(attrValue);
                		}
                	}
                }
            }
        } catch (final NamingException namException) {
            throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, namException);
        } catch (final Exception exception) {
            throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION, exception);
        }
        return uniqueMemberValues;
    }
}
