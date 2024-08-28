/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.eniq.ldap.management;

public final class LDAPAttributes {

  private LDAPAttributes() {
  }

  // USER
  public static final String UID = "uid";

  public static final String MAIL = "mail";

  public static final String COMMON_NAME = "cn";

  public static final String USER_PASSWORD = "userPassword";

  public static final String TELEPHONE_NUMBER = "telephoneNumber";

  public static final String SECOND_NAME = "sn";

  public static final String ORGANISATION = "o";

  public static final String GIVEN_NAME = "givenname"; // First Name

  // ROLES
  public static final String PREDEFINED = "predefined";

  public static final String TITLE = "title";

  public static final String DESCRIPTION = "description";

  // PERMISSIONS

  public static final String SOLUTION_SET_NAME = "solutionSetName";

  // GENERAL

  public static final String UNIQUE_MEMBER = "uniqueMember";

  public static final String PWD_POLICY_SUB_ENTRY = "pwdPolicySubentry";

  /* OpenDJ tag to create the default password policy */
  public static final String PWD_POLICY_SUB_ENTRY_OPENDJ = "ds-pwp-password-policy-dn;collective";

  public static final String RESET_PWD_ONLOGIN = "resetPasswordOnLogin";

  public static final String PWD_ACCOUNT_LOCKED_TIME = "pwdAccountLockedTime";

  public static final String PWD_FAILURE_TIME = "pwdFailureTime";

  public static final String PWD_GRACE_USE_TIME = "pwdGraceUseTime";

  public static final String PWD_GRACE_AUTH_LIMIT = "pwdGraceAuthNLimit";

  public static final String PWD_CHANGED_TIME = "pwdChangedTime";

  public static final String PWD_MAX_AGE = "pwdMaxAge";

  public static final String OPENDJ_DEFAULT_PWD_MAX_AGE = "ds-cfg-max-password-age";

  public static final String OBJECT_CLASS = "objectclass";

  public static final String USER = "person";

  public static final String PERMISSION = "permission";

  public static final String PERMISSION_GROUP = "permissiongroup";

  public static final String ROLE = "role";

  public static final String USER_PROFILE = "userprofile";

  public static final String[] USER_PROFILE_ATTRS = { PREDEFINED, UNIQUE_MEMBER, UID };

  public static final String[] ROLE_ATTRS = { COMMON_NAME, DESCRIPTION, TITLE, PREDEFINED, UNIQUE_MEMBER };

  public static final String[] PERMISSIONGROUP_ATTRS = { COMMON_NAME, DESCRIPTION, TITLE, PREDEFINED, UNIQUE_MEMBER };

  public static final String[] PERMISSION_ATTRS = { COMMON_NAME, DESCRIPTION, TITLE, SOLUTION_SET_NAME };

  public static final String FALSE = "FALSE";

  public static final String TRUE = "TRUE";

  public static final String SECURITY_AUTHENTICATION_SIMPLE = "simple";

  public static final String SECURITY_AUTHENTICATION_NONE = "none";

  public static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

  public static final String PROVIDER_URL = "ldap://ldapserver:9001";

  public static final String CONNECTION_POOL = "com.sun.jndi.ldap.connect.pool";

  public static final String ENABLE_CONNECTION_POOL = "true";

  public static final String DISABLE_CONNECTION_POOL = "true";

  public static final String USERS_BASE_DN = "ou=users,dc=ericsson,dc=se";

  public static final String USERPROFILES_BASE_DN = "ou=userprofiles,ou=pm,ou=oss,dc=ericsson,dc=se";

  public static final String ROLES_BASE_DN = "ou=roles,ou=pm,ou=oss,dc=ericsson,dc=se";

  public static final String PERMISSIONGROUP_BASE_DN = "ou=permissiongroups,ou=pm,ou=oss,dc=ericsson,dc=se";

  public static final String PERMISSIONS_BASE_DN = "ou=permissions,ou=pm,ou=oss,dc=ericsson,dc=se";

  public static final String PREDEFINED_ADMIN_USERS_DN = "cn=Admin Users,ou=roles,dc=ericsson,dc=se";

  public static final String PREDEFINED_UI_USERS_DN = "cn=UI Users,ou=roles,dc=ericsson,dc=se";

  public static final String PASSWORD_POLICY_DN = "cn=default,ou=pwpolicies,dc=ericsson,dc=se";

  public static final String OPENDJ_DEFAULT_PASSWORD_POLICY_DN = "cn=Default Password Policy,cn=Password Policies,cn=config";

  public static final String OPENDJ_PRIVILEGE_NAME = "ds-privilege-name";

  public static final String CONFIG_READ = "config-read";

  public static final String PREDEFINED_SYSADMIN_ROLE = "sysadmin";

  public static final String PREDEFINED_SYSADMIN_USER = "admin";

  public static final String USER_OBJECTCLASS = "objectclass=person";

  public static final String USERPROFILE_OBJECTCLASS = "objectclass=userprofile";

  public static final String ROLE_OBJECTCLASS = "objectclass=role";

  public static final String PERMISSIONGROUP_OBJECTCLASS = "objectclass=permissiongroup";

  public static final String PERMISSION_OBJECTCLASS = "objectclass=permission";

  public static final String ALL_OBJECTCLASS = "objectclass=*";

  public static final String LAST_LOGIN_DATE = "ds-pwp-last-login-time";
  
  public static final String LAST_LOGIN_DATE_FORMAT = "yyyyMMddHHmm";

  public static final String[] USER_ATTRS = { UID, MAIL, COMMON_NAME, USER_PASSWORD, TELEPHONE_NUMBER, SECOND_NAME,
      ORGANISATION, GIVEN_NAME, OBJECT_CLASS, RESET_PWD_ONLOGIN, PWD_ACCOUNT_LOCKED_TIME, PWD_POLICY_SUB_ENTRY,
      PWD_CHANGED_TIME, LAST_LOGIN_DATE };


}
