package com.ericsson.eniq.ldap.util;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.*;
import static com.ericsson.eniq.ldap.util.LDAPConstants.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPAttributes;
import com.ericsson.eniq.ldap.management.LDAPConnectionFactory;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.PermissionGroupManagement;
import com.ericsson.eniq.ldap.management.RoleManagement;
import com.ericsson.eniq.ldap.management.UserManagement;
import com.ericsson.eniq.ldap.management.UserProfileManagement;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;
import com.ericsson.eniq.ldap.vo.PermissionVO;
import com.ericsson.eniq.ldap.vo.RoleVO;
import com.ericsson.eniq.ldap.vo.UserVO;
import com.ericsson.eniq.repository.DBUsersGet;

/**
 * Utility class for miscellaneous LDAP operations.
 * 
 * @author eramano
 * 
 */
public class LDAPUtil {
	
  public LDAPUtil() {
    // do nothing
  }

  public static Set<IValueObject> getAllPermissionsAsVOs(final IValueObject loginVO) throws LDAPException {
    ValidationUtil.checkIfNull(loginVO);
    DirContext ctxt = null;
    final Set<IValueObject> permissions = new HashSet<IValueObject>();
    final LoginVO login = (LoginVO) loginVO;
    try {
      // Get LDAP Connection
      ctxt = LDAPConnectionFactory.getConnection(login.getLoginId(), login.getPassword());
    } catch (final Exception exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_CONNECTION_EXCEPTION, exception);
    }
    if (ctxt != null) {
      // Construct a search filter
      final StringBuffer permissionFilter = new StringBuffer();
      permissionFilter.append(LEFT_BRACE);
      permissionFilter.append(OBJECT_CLASS);
      permissionFilter.append(OP_EQUALS);
      permissionFilter.append(PERMISSION);
      permissionFilter.append(RIGHT_BRACE);
      try {
        // Construct a search control with search filter
        final SearchControls permissionSearchControl = new SearchControls();
        permissionSearchControl.setReturningAttributes(PERMISSION_ATTRS);
        permissionSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Do the search
        final NamingEnumeration<SearchResult> results = ctxt.search(PERMISSIONS_BASE_DN, permissionFilter.toString(),
            permissionSearchControl);
        while (results.hasMore()) {
          final SearchResult sr = results.next();
          final Attributes attrs = sr.getAttributes();
          permissions.add(getPermissionVOFromAttributes(attrs));
        }
      } catch (final NamingException namException) {
        throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, namException);
      } catch (final Exception exception) {
        throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION, exception);
      }
    }
    return permissions;
  }

  /**
   * Create Permission VO from attributes retrieved from LDAP.
   * 
   * @param attrs
   * @return permissionVO
   */
  private static IValueObject getPermissionVOFromAttributes(final Attributes attrs) throws NamingException {
    final PermissionVO permissionVO = new PermissionVO();
    if (attrs.get(COMMON_NAME) != null) {
      permissionVO.setPermissionName((String) attrs.get(COMMON_NAME).get());
    }
    if (attrs.get(TITLE) != null) {
      permissionVO.setTitle((String) attrs.get(TITLE).get());
    }
    if (attrs.get(DESCRIPTION) != null) {
      permissionVO.setDescription((String) attrs.get(DESCRIPTION).get());
    }
    if (attrs.get(SOLUTION_SET_NAME) != null) {
      permissionVO.setSolutionSetName((String) attrs.get(SOLUTION_SET_NAME).get());
    }
    return permissionVO;
  }

  /**
   * Unique Members are stored in LDAP as
   * cn=eventsui.terminal.view,ou=permissions,ou=pm,ou=oss,dc=ericsson,dc=se.
   * This method is to take the unique ID in this. Unique ID falls between first
   * index of '=' and first index of ','.
   * 
   * @param attrValue
   *          unique member absolute name
   * @return unique id substring
   */
  public static String formatAttrValue(final String attrValue) {
    ValidationUtil.checkIfNull(attrValue);
    return attrValue.substring(attrValue.indexOf(OP_EQUALS) + 1, attrValue.indexOf(OP_COMMA));
  }
  
  /**
   * maximum allowed age limit for password from LDAP.
   * 
   * @param ctxt
   * @param password policy DN
   * @return maximum password age limit
   * @throws LDAPException
   */
  public static long getMaxAgeLimitForPassword(final DirContext ctxt, String passwordPolicy) throws LDAPException {
    long maxAgeLimit = 0L;
    final SearchControls searchCtls = new SearchControls();
    final String returnedAttributes[] = { PWD_MAX_AGE };
    searchCtls.setReturningAttributes(returnedAttributes);
    searchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);
    // Search for objects using the filter
    try {
    	final Hashtable<String, String> ldapConfiguration = getAdministratorLDAPConfig();
    	final DirContext ctxtAdmin = new InitialDirContext(ldapConfiguration);
    	
      final NamingEnumeration<SearchResult> result = ctxtAdmin.search(passwordPolicy, ALL_OBJECTCLASS, searchCtls);
      if (result != null && result.hasMoreElements()) {
        final SearchResult searchResult = result.next();
        final Attributes pwdAttrs = searchResult.getAttributes();
        if (pwdAttrs != null && pwdAttrs.size() > 0) {
          maxAgeLimit = Long.parseLong(pwdAttrs.get(PWD_MAX_AGE).get().toString());
        }
      }
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    } catch (final Exception exception) {
      throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION, exception);
    }
    return maxAgeLimit;
  }

private static Hashtable<String, String> getAdministratorLDAPConfig() throws LDAPException{
	final Hashtable<String, String> ldapConfiguration = new Hashtable<String, String>(11);
	ldapConfiguration.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
	ldapConfiguration.put(javax.naming.Context.PROVIDER_URL,PROVIDER_URL);
	ldapConfiguration.put(javax.naming.Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION_SIMPLE);
	ldapConfiguration.put(javax.naming.Context.SECURITY_PRINCIPAL, "cn=Administrator,dc=ericsson,dc=se");
	String password = "";
	
	try {
		List<Meta_databases> ldapEntries = DBUsersGet.getMetaDatabases("LDAP_BIND_PASSWORD", "LDAP");
		password = getAdministratorPassword(ldapEntries);
	} catch (Exception exception) {
		new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION, exception);
	} 
		
	ldapConfiguration.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
	return ldapConfiguration;
}

	/**
	 * Gets the LDAP admin password.
	 * @return The password from the META_DATABASES table.
	 * @throws LDAPException 
	 */
	protected static String getAdministratorPassword(List<Meta_databases> ldapEntries) throws LDAPException {
		String password = "";
		if (ldapEntries == null) {
			throw new LDAPException("No results returned for LDAP Admin password from META_DATABASES table in etlrep.");
		} else if (ldapEntries.size() <= 0){
			throw new LDAPException("No results returned for LDAP Admin password from META_DATABASES table in etlrep.");
		}
				
		Meta_databases ldapEntryInMetaDbs = ldapEntries.get(0);
		if (ldapEntryInMetaDbs == null) {
			throw new LDAPException("No entry found in META_DATABASES table in etlrep, query returned null.");
		}
		password = ldapEntryInMetaDbs.getPassword();
		if (password == null) {
			throw new LDAPException("No entry found in META_DATABASES table in etlrep, password value was null.");
		}
		return password;
	}
  
  /**
   * Verify delete is allowed for user performing delete and user being deleted.
   * 
   * @param loginVO
   * @param userVO
   * @param ctxt
   * @return
   * @throws LDAPException
   */
  public static MESSAGES isDeleteUserAllowed(final LoginVO loginVO, final UserVO userVO, final DirContext ctxt)
      throws LDAPException {
    MESSAGES message = MESSAGES.MSG_SUCCESS;
    try {
      // Confirm if user is not deleting himself
      final boolean isDeletingSelf = loginVO.getLoginId().equals(userVO.getUserId());

      // Confirm if user performing delete is predefined SysAdmin user - ONLY
      // USED WHILE DELETING OTHER ADMIN USERS
      final boolean isPredefinedAdminPerformingDelete = PREDEFINED_SYSADMIN_USER.equals(loginVO.getLoginId());
      // Confirm if user performing delete has admin role
      final boolean isAdminPerformingDelete = isAdminUser(loginVO.getLoginId(), ctxt);

      // check if user being deleted has admin role
      final boolean isAdminUserBeingDeleted = isAdminUser(userVO.getUserId(), ctxt);
      // check if user being deleted is predefined
      final boolean isPredefinedUserBeingDeleted = isPredefinedUser(userVO.getUserId(), ctxt);

      if (isDeletingSelf) {
        // Deleting a user by himself is not allowed
        message = MESSAGES.ERR_CANNOT_SELF_DELETE;
      } else if (isPredefinedUserBeingDeleted) {
        // Predefined users cannot be deleted
        message = MESSAGES.ERR_CANNOT_DELETE_PREDEFINED_USERS;
      } else if (!isAdminPerformingDelete) {
        // Only admin users are allowed to delete other users
        message = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_DELETE_USERS;
      } else if (isAdminUserBeingDeleted && !isPredefinedAdminPerformingDelete) {
        // Only predefined sysadmin user is allowed to delete other admin users
        message = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_DELETE_SYSADMIN_USERS;
      }
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    }
    return message;
  }

  /**
   * Verify modify is allowed for user performing modify and user being
   * modified.
   * 
   * @param loginVO
   * @param userVO
   * @param ctxt
   * @return
   * @throws LDAPException
   */
  public static MESSAGES isModifyUserAllowed(final LoginVO loginVO, final UserVO userVO, final DirContext ctxt)
      throws LDAPException {
    MESSAGES message = MESSAGES.MSG_SUCCESS;
    try {
      // Check if user is modifying himself
      final boolean isModifyingSelf = loginVO.getLoginId().equals(userVO.getUserId());
      // Confirm if user performing modify is predefined SysAdmin user - ONLY
      // USED WHILE MODIFYING OTHER ADMIN USERS
      final boolean isPredefinedAdminPerformingModify = PREDEFINED_SYSADMIN_USER.equals(loginVO.getLoginId());
      // Confirm if user performing delete has admin role
      final boolean isAdminPerformingModify = isAdminUser(loginVO.getLoginId(), ctxt);
      // check if user being deleted has admin role
      final boolean isAdminUserBeingModified = isAdminUser(userVO.getUserId(), ctxt);
      if (!isAdminPerformingModify) {
        // only admin users are allowed to modify other UI users
        message = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_MODIFY_USERS;
      } else if (isAdminUserBeingModified && !isModifyingSelf && !isPredefinedAdminPerformingModify) {
        // Only predefined admin user is allowed to modify other admin users
        // but, an admin user can modify himself
        message = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_MODIFY_SYSADMIN_USERS;
      }
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    }
    return message;
  }

  /**
   * Verify create is allowed for user performing create.
   * 
   * @param loginVO
   * @param userVO
   * @param ctxt
   * @return
   * @throws LDAPException
   */
  public static MESSAGES isCreateUserAllowed(final LoginVO loginVO, final UserVO userVO, final DirContext ctxt)
      throws LDAPException {
    MESSAGES message = MESSAGES.MSG_SUCCESS;
    try {
      // Confirm if user performing create is predefined SysAdmin user - ONLY
      // USED WHILE CREATE OTHER ADMIN USERS
      final boolean isPredefinedAdminPerformingCreate = PREDEFINED_SYSADMIN_USER.equals(loginVO.getLoginId());
      // Confirm if user performing create has admin role
      final boolean isAdminPerformingCreate = isAdminUser(loginVO.getLoginId(), ctxt);
      // check if user being created is predefined (just to avoid bad usage of
      // vo)
      final boolean isPredefinedUserBeingCreated = userVO.isPredefined();
      // check if user being created is predefined (just to avoid bad usage of
      // vo)
      boolean isAdminUserBeingCreated = false;
      if (userVO.getRoles() != null) {
        isAdminUserBeingCreated = userVO.getRoles().contains(PREDEFINED_SYSADMIN_ROLE);
      }

      if (isPredefinedUserBeingCreated) {
        // cannot create predefined users after installation.
        message = MESSAGES.ERR_CANNOT_CREATE_PREDEFINED_USERS;
      } else if (!isAdminPerformingCreate) {
        // only admin users are allowed to create other users
        message = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_CREATE_USERS;
      } else if (isAdminUserBeingCreated && !isPredefinedAdminPerformingCreate) {
        // Only predefined admin user is allowed to modify other admin users
        message = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_CREATE_SYSADMIN_USERS;
      }
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    }
    return message;
  }

  /**
   * Find if the user is admin user by using userId
   * 
   * @param userId
   * @param ctxt
   * @return
   * @throws LDAPException
   */
  public static boolean isAdminUser(final String userId, final DirContext ctxt) throws NamingException, LDAPException {
    boolean isAdmin = false;
    final ILDAPManagement userProfileManagement = new UserProfileManagement();
    final ILDAPObject userProfile = userProfileManagement.findById(userId, ctxt);
    if (null != userProfile) {
      final Attributes userProfileAttrs = userProfile.getAttributes(UID);
      if (userProfileAttrs.get(UNIQUE_MEMBER) != null) {
        final Attribute roleAttr = userProfileAttrs.get(UNIQUE_MEMBER);
        for (int roleCount = 0; roleCount < roleAttr.size(); roleCount++) {
          final String roleName = formatAttrValue((String) roleAttr.get(roleCount));
          if (PREDEFINED_SYSADMIN_ROLE.equals(roleName)) {
            isAdmin = true;
            break;
          }
        }
      }
    }
    return isAdmin;
  }

  /**
   * Determine if a user is predefined user
   * 
   * @param userId
   * @param ctxt
   * @return
   * @throws NamingException
   * @throws LDAPException
   */
  private static boolean isPredefinedUser(final String userId, final DirContext ctxt) throws NamingException,
      LDAPException {
    boolean isPredefined = false;
    final ILDAPManagement userProfileManagement = new UserProfileManagement();
    final ILDAPObject userProfile = userProfileManagement.findById(userId, ctxt);
    if (null != userProfile) {
      final Attributes userProfileAttrs = userProfile.getAttributes(UID);
      if (userProfileAttrs.get(LDAPAttributes.PREDEFINED) != null) {
        isPredefined = TRUE.equals(userProfileAttrs.get(LDAPAttributes.PREDEFINED).get());
      }
    }
    return isPredefined;
  }

  /**
   * Verify if a user is allowed to create/modify/delete roles. Also check if
   * role being updated is predefined. If predefined, block the user.
   * 
   * @param login
   * @param roleVO
   * @param ctxt
   * @return
   * @throws LDAPException
   */
  public static MESSAGES isAllowedToCUDRole(final LoginVO login, final RoleVO roleVO, final DirContext ctxt)
      throws LDAPException {
    MESSAGES allowedMessage = MESSAGES.MSG_SUCCESS;
    try {
      if (roleVO.isPredefined()) {
        allowedMessage = MESSAGES.ERR_CANNOT_UPDATE_PREDEFINED_ROLES;
      }
      if (!isAdminUser(login.getLoginId(), ctxt)) {
        allowedMessage = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_UPDATE_ROLES;
      }
      if (isPredefinedRole(roleVO.getRoleName(), ctxt)) {
        allowedMessage = MESSAGES.ERR_CANNOT_UPDATE_PREDEFINED_ROLES;
      }
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    }
    return allowedMessage;
  }

  /**
   * Determine if a role is predefined role
   * 
   * @param roleName
   * @param ctxt
   * @return
   * @throws NamingException
   * @throws LDAPException
   */
  private static boolean isPredefinedRole(final String roleName, final DirContext ctxt) throws NamingException,
      LDAPException {
    boolean isPredefined = false;
    final ILDAPManagement roleManagement = new RoleManagement();
    final ILDAPObject role = roleManagement.findById(roleName, ctxt);
    if (null != role) {
      final Attributes roleAttrs = role.getAttributes(UID);
      if (roleAttrs.get(LDAPAttributes.PREDEFINED) != null) {
        isPredefined = TRUE.equals(roleAttrs.get(LDAPAttributes.PREDEFINED).get());
      }
    }
    return isPredefined;
  }

  /**
   * Verify if a user is allowed to create/modify/delete permission groups. Also
   * check if permission group being updated is predefined. If predefined, block
   * the user.
   * 
   * @param login
   * @param permGroupVO
   * @param ctxt
   * @return
   * @throws LDAPException
   */
  public static MESSAGES isAllowedToCUDPermGroup(final LoginVO login, final PermissionGroupVO permGroupVO,
      final DirContext ctxt) throws LDAPException {
    MESSAGES allowedMessage = MESSAGES.MSG_SUCCESS;
    try {
      if (permGroupVO.isPredefined()) {
        allowedMessage = MESSAGES.ERR_CANNOT_UPDATE_PREDEFINED_PERMGROUPS;
      }
      if (!isAdminUser(login.getLoginId(), ctxt)) {
        allowedMessage = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_UPDATE_PERMGROUPS;
      }
      if (isPredefinedPermGroup(permGroupVO.getPermissionGroupName(), ctxt)) {
        allowedMessage = MESSAGES.ERR_CANNOT_UPDATE_PREDEFINED_PERMGROUPS;
      }
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    }
    return allowedMessage;
  }

  /**
   * Determine if a permission group is predefined permission group
   * 
   * @param permGroupName
   * @param ctxt
   * @return
   * @throws NamingException
   * @throws LDAPException
   */
  private static boolean isPredefinedPermGroup(final String permGroupName, final DirContext ctxt)
      throws NamingException, LDAPException {
    boolean isPredefined = false;
    final ILDAPManagement permGroupManagement = new PermissionGroupManagement();
    final ILDAPObject permGroup = permGroupManagement.findById(permGroupName, ctxt);
    if (null != permGroup) {
      final Attributes permGroupAttrs = permGroup.getAttributes(UID);
      if (permGroupAttrs.get(LDAPAttributes.PREDEFINED) != null) {
        isPredefined = TRUE.equals(permGroupAttrs.get(LDAPAttributes.PREDEFINED).get());
      }
    }
    return isPredefined;
  }

  public static MESSAGES unlockUser(final IValueObject loginVO, final IValueObject ldapVO) throws LDAPException {
    final MESSAGES result = MESSAGES.MSG_SUCCESS;
    final UserVO userVO = (UserVO) ldapVO;
    final LoginVO login = (LoginVO) loginVO;
    DirContext ctxt = null;
    try {
      ctxt = LDAPConnectionFactory.getConnection(login.getLoginId(), login.getPassword());
      if (ctxt != null) {
        final MESSAGES isUnlockAllowed = isUnlockAllowed(login.getLoginId(), userVO.getUserId(), ctxt);
        if (MESSAGES.MSG_SUCCESS == isUnlockAllowed) {
          final ModificationItem[] attributeTobeModified = getUnlockAttributes();
          final String userDN = buildUserDN(userVO.getUserId());
          ctxt.modifyAttributes(userDN, attributeTobeModified);
        } else {
          throw new LDAPException(isUnlockAllowed, new Exception(isUnlockAllowed.getMessage()));
        }
      }
    } catch (final LDAPException exception) {
      throw exception;
    } catch (final NamingException exception) {
      throw new LDAPException(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, exception);
    } catch (final Exception exception) {
      throw new LDAPException(MESSAGES.ERR_UNEXPECTED_EXCEPTION, exception);
    }
    return result;
  }

  /**
   * Create attributes to be modified for user so that he is unlocked.
   * 
   * @return
   */
  private static ModificationItem[] getUnlockAttributes() {
    final ModificationItem[] attributeTobeModified = new ModificationItem[2];
    attributeTobeModified[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(
        PWD_ACCOUNT_LOCKED_TIME));
    attributeTobeModified[1] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(PWD_FAILURE_TIME));
    /*
     * attributeTobeModified[1] = new
     * ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
     * RESET_PWD_ONLOGIN,TRUE));
     */// unlock doesn't require user to change password on next login
    return attributeTobeModified;
  }

  /**
   * Verify if unlock operations is allowed for logged in user and the user
   * being unlocked
   * 
   * @param loginId
   * @param userId
   * @param ctxt
   * @return
   * @throws NamingException
   * @throws LDAPException
   */
  private static MESSAGES isUnlockAllowed(final String loginId, final String userId, final DirContext ctxt)
      throws NamingException, LDAPException {
    MESSAGES result = MESSAGES.MSG_SUCCESS;
    final boolean isPredefinedAdminPerformingUnlock = PREDEFINED_SYSADMIN_USER.equals(loginId);
    final boolean isAdminPerformingUnlock = isAdminUser(loginId, ctxt);
    final boolean isAdminBeingUnlocked = isAdminUser(userId, ctxt);
    final boolean isLockedUser = isLockedUser(userId, ctxt);
    if (!isLockedUser) {
      result = MESSAGES.ERR_ONLY_LOCKED_USERS_CAN_BE_UNLOCKED;
    } else if (!isAdminPerformingUnlock) {
      // Only admin users can unlock other users
      result = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_UNLOCK_USERS;
    } else if (isAdminBeingUnlocked && !isPredefinedAdminPerformingUnlock) {
      // Only predefined admin can unlock other admins
      result = MESSAGES.ERR_NOT_ENOUGH_ROLE_TO_UNLOCK_SYSADMIN_USERS;
    }
    return result;
  }

  /**
   * Find if a user is locked
   * 
   * @param userId
   * @param ctxt
   * @return
   * @throws LDAPException
   * @throws NamingException
   */
  private static boolean isLockedUser(final String userId, final DirContext ctxt) throws LDAPException, NamingException {
    boolean isLocked = false;
    final ILDAPManagement userManagement = new UserManagement();
    final ILDAPObject user = userManagement.findById(userId, ctxt);
    if (null != user) {
      final Attributes userAttrs = user.getAttributes(UID);
      if (userAttrs.get(PWD_ACCOUNT_LOCKED_TIME) != null) {
        isLocked = true;
      }
    }
    return isLocked;
  }

  /**
   * Build DN for user
   * 
   * @param userId
   *          user name
   * @return user DN
   */
  private static String buildUserDN(final String userId) {
    final StringBuffer userDN = new StringBuffer();
    userDN.append(UID);
    userDN.append(OP_EQUALS);
    userDN.append(userId);
    userDN.append(OP_COMMA);
    userDN.append(USERS_BASE_DN);
    return userDN.toString();
  }

  /**
   * Create a search filter string for searching user profiles for a user based
   * on ID. Example for filter: (&(objectclass=userprofile)((uid=admin)))
   * 
   * @param id
   * @return
   */
  public static String getUserProfileSearchFilterById(final String id) {
    final StringBuilder userProfileFilter = new StringBuilder();
    userProfileFilter.append(LEFT_BRACE);
    userProfileFilter.append(OP_AND);
    userProfileFilter.append(LEFT_BRACE);
    userProfileFilter.append(USERPROFILE_OBJECTCLASS);
    userProfileFilter.append(RIGHT_BRACE);
    userProfileFilter.append(LEFT_BRACE);
    userProfileFilter.append(LEFT_BRACE);
    userProfileFilter.append(UID);
    userProfileFilter.append(OP_EQUALS);
    userProfileFilter.append(id);
    userProfileFilter.append(RIGHT_BRACE);
    userProfileFilter.append(RIGHT_BRACE);
    userProfileFilter.append(RIGHT_BRACE);
    return userProfileFilter.toString();
  }

  /**
   * Create a search filter string for searching all user profiles. Example for
   * filter: (objectclass=userprofile)
   * 
   * @param id
   * @return search filter string
   */
  public static String getAllUserProfileSearchFilter() {
    final StringBuilder userProfileFilter = new StringBuilder();
    userProfileFilter.append(LEFT_BRACE);
    userProfileFilter.append(USERPROFILE_OBJECTCLASS);
    userProfileFilter.append(RIGHT_BRACE);
    return userProfileFilter.toString();
  }

  /**
   * Create a search filter string for searching specified roles. Example for
   * filter: (&(objectclass=role)(|(cn=role1)(cn=role2)))
   * 
   * @param roles
   *          set of roles
   * @return search filter string
   */
  public static String getSpecifiedRolesSearchFilter(final Set<String> roles) {
    final StringBuilder rolesFilter = new StringBuilder();
    rolesFilter.append(LEFT_BRACE);
    rolesFilter.append(OP_AND);
    rolesFilter.append(LEFT_BRACE);
    rolesFilter.append(ROLE_OBJECTCLASS);
    rolesFilter.append(RIGHT_BRACE);
    rolesFilter.append(LEFT_BRACE);
    rolesFilter.append(OP_OR);
    for (final String role : roles) {
      rolesFilter.append(LEFT_BRACE);
      rolesFilter.append(COMMON_NAME);
      rolesFilter.append(OP_EQUALS);
      rolesFilter.append(role);
      rolesFilter.append(RIGHT_BRACE);
    }
    rolesFilter.append(RIGHT_BRACE);
    rolesFilter.append(RIGHT_BRACE);
    return rolesFilter.toString();
  }
}
