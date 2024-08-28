package com.ericsson.eniq.ldap.util;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.DESCRIPTION;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.GIVEN_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.MAIL;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ORGANISATION;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PREDEFINED;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PWD_ACCOUNT_LOCKED_TIME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PWD_CHANGED_TIME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.SECOND_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.TELEPHONE_NUMBER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.TITLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UNIQUE_MEMBER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER_PASSWORD;
import static com.ericsson.eniq.ldap.util.LDAPConstants.DEFAULT_PHONE_NUMBER;
import static com.ericsson.eniq.ldap.util.LDAPConstants.EMPTY_STRING;
import static com.ericsson.eniq.ldap.util.LDAPConstants.ONE_SPACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.PASSWORD_PLACEHOLDER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.LAST_LOGIN_DATE_FORMAT;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.LAST_LOGIN_DATE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;
import com.ericsson.eniq.ldap.vo.RoleVO;
import com.ericsson.eniq.ldap.vo.UserVO;

/**
 * Utility class to create value objects from LDAP entities.
 *
 * This utility is mainly used in handler classes before returning VOs to UI layer.
 *
 * @author eramano
 *
 */
public final class ValueObjectTransformer {

	static Log log = LogFactory.getLog(ValueObjectTransformer.class);
	
	/**
	 * empty private constructor
	 */
	private ValueObjectTransformer(){
		// to avoid instantiation
	}

	/**
	 * Create user value object from user and user profile LDAP objects
	 * @param user LDAP User object
	 * @param userProfile LDAP Userprofile object
	 * @param maxPasswordAgeLimit password age limit
	 * @return User value object
	 * @throws LDAPException
	 */
	public static UserVO createUserVOFromLdapObject(final ILDAPObject user, final ILDAPObject userProfile,
			final long maxPasswordAgeLimit) throws LDAPException {
		final UserVO userVo = new UserVO();
		if ( user != null && userProfile != null ){
			try {
				
				final Attributes userAttrs = user.getAttributes(UID);
				
				if ( null != userAttrs.get(UID)) {
					userVo.setUserId((String)userAttrs.get(UID).get());
					log.debug("Creating user for uid: " + (String)userAttrs.get(UID).get());
				}
				if ( null != userAttrs.get(GIVEN_NAME)){
					userVo.setFname((String)userAttrs.get(GIVEN_NAME).get());
				}
				if (null != userAttrs.get(SECOND_NAME) ) {
					userVo.setLname((String)userAttrs.get(SECOND_NAME).get());
				}
				if ( null != userAttrs.get(MAIL) && !ONE_SPACE.equals(userAttrs.get(MAIL).get())) {
					userVo.setEmail((String)userAttrs.get(MAIL).get());
				} else {
					userVo.setEmail(EMPTY_STRING);
				}
				if ( null != userAttrs.get(TELEPHONE_NUMBER)) {
					final String teleNum = (String)userAttrs.get(TELEPHONE_NUMBER).get();
					if ( DEFAULT_PHONE_NUMBER.equals(teleNum) ) {
					   userVo.setPhone(EMPTY_STRING);
					} else {
						userVo.setPhone((String)userAttrs.get(TELEPHONE_NUMBER).get());
					}
				}
				if ( null != userAttrs.get(ORGANISATION) && !ONE_SPACE.equals(userAttrs.get(ORGANISATION).get())) {
					userVo.setOrg((String)userAttrs.get(ORGANISATION).get());
				} else {
					userVo.setOrg(EMPTY_STRING);
				}
				if ( null != userAttrs.get(USER_PASSWORD)) {
					userVo.setPassword(PASSWORD_PLACEHOLDER);
				}
				final Attributes userProfileAttrs = userProfile.getAttributes(UID);

				final Attribute roleAttr = userProfileAttrs.get( UNIQUE_MEMBER );
				if ( null != roleAttr ){
					final Set<String> roles = new TreeSet<String>();
					for ( int roleCount = 0 ; roleCount < roleAttr.size() ; roleCount++ ){
					   	roles.add(LDAPUtil.formatAttrValue((String)roleAttr.get(roleCount)));
					}
					userVo.setRoles(roles);
				}
				if ( userProfileAttrs.get(PREDEFINED) != null ) {
					userVo.setPredefined(Boolean.parseBoolean((String)userProfileAttrs.get(PREDEFINED).get()));
				}
				// determine user sate i.e locked, expired, no roles or normal
				if ( null != userAttrs.get(PWD_ACCOUNT_LOCKED_TIME)){
				    userVo.setUserState( USERSTATE.STATE_LOCKED );
				} else if ( isUserPasswordExpired( userAttrs, maxPasswordAgeLimit) ) {
					userVo.setUserState(USERSTATE.STATE_EXPIRED);
				} else if ( null == userVo.getRoles() || userVo.getRoles().isEmpty() ){
					userVo.setUserState( USERSTATE.STATE_NO_ROLES );
				} else {
					userVo.setUserState( USERSTATE.STATE_NORMAL);
				}
				
				// if last login date exists, parse using LAST_LOGIN_DATE_FORMAT
				if (null != userAttrs.get(LAST_LOGIN_DATE)) {
					log.debug("LAST_LOGIN_DATE: " + (String)userAttrs.get(LAST_LOGIN_DATE).get());
					SimpleDateFormat sdf = new SimpleDateFormat(LAST_LOGIN_DATE_FORMAT);
					Date lastLoginDate = null;
					
					try {
						lastLoginDate = sdf.parse((String)userAttrs.get(LAST_LOGIN_DATE).get());
					} catch (Exception e) {
						lastLoginDate = null;
					}
					
					userVo.setLastLoginDate(lastLoginDate);
				}
      } catch (ParseException e){
        throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION, e);
			} catch (NamingException namException) {
				throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException );
			} catch (Exception exception){
				throw new LDAPException( MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception );
			}
		}
		return userVo;
	}

	/**
	 * Check if user's password is expired.
	 * @param userAttributes
	 * @param maxPasswordAgeLimit
	 * @return
	 * @throws Exception
	 */
  private static boolean isUserPasswordExpired(final Attributes userAttributes,
                                               final long maxPasswordAgeLimit) throws Exception {
    boolean isUserPasswordExpired = false;
    // opendj [\d]+\.[\d]{3,4}
    // openldap [\d]
    if (userAttributes.get(PWD_CHANGED_TIME) != null) {
      String passwordChangedTime = (String) userAttributes.get(PWD_CHANGED_TIME).get();
      String opendjTimeformat = "yyyyMMddHHmmss.SSS";
      if(passwordChangedTime.contains("Z")){
        passwordChangedTime = passwordChangedTime.replace('Z', '0');
        opendjTimeformat += "S";
      }
      final SimpleDateFormat dateFormat;
      if (passwordChangedTime.matches("[\\d]+")) {
        // openldap
        dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
      } else if (passwordChangedTime.matches("[\\d]+\\.[\\d]{3,4}")) {
        dateFormat = new SimpleDateFormat(opendjTimeformat, Locale.getDefault());
      } else {
        throw new ParseException("Can not parse the date string '"+passwordChangedTime+"' unknown format!", -1);
      }
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      final Date date = dateFormat.parse(passwordChangedTime);
      long passwordChangeTimeInMillis = date.getTime() + (maxPasswordAgeLimit * 1000);
      if (passwordChangeTimeInMillis < System.currentTimeMillis()) {
        isUserPasswordExpired = true;
      }
    }
    return isUserPasswordExpired;
  }

	/**
	 * Create user value object from role LDAP object
	 * @param role LDAP role object
	 * @return roleVO role value object
	 * @throws NamingException
	 */
	public static IValueObject createRoleVOFromLdapObject(final ILDAPObject role) throws LDAPException {
		final RoleVO roleVo = new RoleVO();
		if ( role != null ){
			try {
				final Attributes roleAttrs = role.getAttributes(COMMON_NAME);
				if ( roleAttrs.get(COMMON_NAME) != null ) {
					roleVo.setRoleName((String)roleAttrs.get(COMMON_NAME).get());
				}
				if ( roleAttrs.get(TITLE) != null ) {
					roleVo.setTitle((String)roleAttrs.get(TITLE).get());
				}
				if ( roleAttrs.get(DESCRIPTION) != null && !ONE_SPACE.equals((roleAttrs.get(DESCRIPTION).get()))) {
					roleVo.setDescription((String)roleAttrs.get(DESCRIPTION).get());
				} else {
					roleVo.setDescription(EMPTY_STRING);
				}
				if ( roleAttrs.get(PREDEFINED) != null ) {
					roleVo.setPredefined(Boolean.parseBoolean((String)roleAttrs.get(PREDEFINED).get()));
				}
				if ( roleAttrs.get(UNIQUE_MEMBER) != null ){
					final Attribute permGroupAttr = roleAttrs.get( UNIQUE_MEMBER );
					final Set<String> permGroups = new TreeSet<String>();
					for ( int permGroupCount = 0 ; permGroupCount < permGroupAttr.size() ; permGroupCount++ ){
						permGroups.add(LDAPUtil.formatAttrValue((String)permGroupAttr.get(permGroupCount)));
					}
					roleVo.setPermissionGroups(permGroups);
				}
				if ( null == roleVo.getPermissionGroups() || roleVo.getPermissionGroups().isEmpty() ){
					roleVo.setRemarks(MESSAGES.ERR_NO_PERMISSION_GROUPS_ASSIGNED_TO_ROLE.getMessage());
				} else {
					roleVo.setRemarks(EMPTY_STRING);
				}
			} catch (NamingException namException) {
				throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException );
			} catch (Exception exception){
				throw new LDAPException( MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception );
			}
		}
		return roleVo;
	}

	/**
	 * Create permission group value object from permission group LDAP object
	 * @param permGroup LDAP permission group object
	 * @return permissionGroupVO permission group value object
	 * @throws LDAPException
	 */
	public static IValueObject createPermissionGroupVOFromLdapObject(
			ILDAPObject permGroup) throws LDAPException {
		final PermissionGroupVO permissionGroupVo = new PermissionGroupVO();
		if ( permGroup != null ){
			try {
				final Attributes permGroupAttrs = permGroup.getAttributes(COMMON_NAME);
				if ( permGroupAttrs.get(COMMON_NAME) != null ) {
					permissionGroupVo.setPermissionGroupName((String)permGroupAttrs.get(COMMON_NAME).get());
				}
				if ( permGroupAttrs.get(TITLE) != null ) {
					permissionGroupVo.setTitle((String)permGroupAttrs.get(TITLE).get());
				}
				if ( permGroupAttrs.get(DESCRIPTION) != null && !ONE_SPACE.equals((permGroupAttrs.get(DESCRIPTION).get()))) {
					permissionGroupVo.setDescription((String)permGroupAttrs.get(DESCRIPTION).get());
				}else{
					permissionGroupVo.setDescription(EMPTY_STRING);
				}
				if ( permGroupAttrs.get(PREDEFINED) != null ) {
					permissionGroupVo.setPredefined(Boolean.parseBoolean((String)permGroupAttrs.get(PREDEFINED).get()));
				}
				if ( permGroupAttrs.get(UNIQUE_MEMBER) != null ){
					final Attribute permissionsAttr = permGroupAttrs.get( UNIQUE_MEMBER );
					final Set<String> permissions = new TreeSet<String>();
					for ( int permissionsCount = 0 ; permissionsCount < permissionsAttr.size() ; permissionsCount++ ){
						permissions.add(LDAPUtil.formatAttrValue((String)permissionsAttr.get(permissionsCount)));
					}
					permissionGroupVo.setPermissions(permissions);
				}
			} catch (NamingException namException) {
				throw new LDAPException( MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION,namException );
			} catch (Exception exception){
				throw new LDAPException( MESSAGES.ERR_UNEXPECTED_EXCEPTION,exception );
			}
		}
		return permissionGroupVo;
	}
}
