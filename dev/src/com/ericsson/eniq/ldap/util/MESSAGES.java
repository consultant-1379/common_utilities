package com.ericsson.eniq.ldap.util;

/**
 * Error messages enumeration
 * @author etonnee
 *
 */
public enum MESSAGES{
	
	MSG_SUCCESS(100,"TRUE"),
	MSG_FAILED(101,"FALSE"),
	
	ERR_USER_ALREADY_EXISTS(1000,"User already exists"),
	ERR_USER_DOES_NOT_EXIST(1001,"User does not exist"),
	ERR_USER_PROFILE_ALREADY_EXISTS(1002,"User profile already exists"),
	ERR_USER_PROFILE_DOES_NOT_EXIST(1003,"User profile does not exist"),
	ERR_CANNOT_DELETE_PREDEFINED_USERS(1004,"Cannot delete predefined user"),
	ERR_FAILED_TO_DELETE_USERS_FROM_LOGIN_PROFILES(1005,"Error while removing user from login profiles"),
	ERR_CANNOT_SELF_DELETE(1006,"No user is allowed to delete himself or herself"),
	ERR_NOT_ENOUGH_ROLE_TO_DELETE_USERS(1007,"Only admin users are allowed to delete other users"),
	ERR_NOT_ENOUGH_ROLE_TO_DELETE_SYSADMIN_USERS(1008,"Only predefined admin user can delete other admin users"),
	ERR_INVALID_PASSWORD(1009,"A valid password must have a minimum length of eight characters and contain one upper case letter, one lower case letter and a numeric value"),
	ERR_INVALID_EMAIL(1010,"Invalid Email Address"),
	ERR_INVALID_PHONE_NUMBER(1011,"Invalid Phone Number"),
	ERR_NOT_ENOUGH_ROLE_TO_MODIFY_USERS(1012,"Only admin users are allowed to modify other users"),
	ERR_NOT_ENOUGH_ROLE_TO_MODIFY_SYSADMIN_USERS(1013,"Only predefined admin user can modify other admin users"),
	ERR_NOT_ENOUGH_ROLE_TO_CREATE_USERS(1014,"Only admin users are allowed to create other users"),
	ERR_NOT_ENOUGH_ROLE_TO_CREATE_SYSADMIN_USERS(1015,"Only predefined admin user can create other admin users"),
	ERR_CANNOT_CREATE_PREDEFINED_USERS(1016,"Cannot create predefined user"),
	ERR_ONLY_LOCKED_USERS_CAN_BE_UNLOCKED(1017,"Only locked users can be unlocked"),
	ERR_NOT_ENOUGH_ROLE_TO_UNLOCK_USERS(1018,"Only admin users are allowed to unlock other users"),
	ERR_NOT_ENOUGH_ROLE_TO_UNLOCK_SYSADMIN_USERS(1019,"Only predefined admin user can unlock other admin users"),
	ERR_PASSWORD_IN_PASSWORD_HISTORY(1020,"Unable to change password, please choose a password other than your last five passwords"),
	
	ERR_ROLE_ALREADY_EXISTS(2000,"Role already exists"),
	ERR_ROLE_DOES_NOT_EXIST(2001,"Role does not exist"),
	ERR_SELECTED_ROLES_DO_NOT_EXIST(2002,"Some of the selected roles do not exist"),
	ERR_CANNOT_UPDATE_PREDEFINED_ROLES(2003,"Cannot create/modify/delete a predefined role"),
	ERR_NOT_ENOUGH_ROLE_TO_UPDATE_ROLES(2004,"Only admin users are allowed to create/modify/delete roles"),
	ERR_FAILED_TO_DELETE_ROLES_FROM_USER_PROFILES(2005,"Error while removing role from user profiles"),
	ERR_NO_PERMISSION_GROUPS_ASSIGNED_TO_ROLE(2006,"Role has no permission groups assigned"),
	
	ERR_PERMGROUP_ALREADY_EXISTS(3000,"Permission Group already exists"),
	ERR_PERMGROUP_DOES_NOT_EXIST(3001,"Permission Group does not exist"),
	ERR_SELECTED_PERMGROUPS_DO_NOT_EXIST(3002,"Some of the selected permission groups do not exist"),
	ERR_CANNOT_UPDATE_PREDEFINED_PERMGROUPS(3003,"Cannot create/modify/delete a predefined permission group"),
	ERR_NOT_ENOUGH_ROLE_TO_UPDATE_PERMGROUPS(3004,"Only admin users are allowed to create/modify/delete permission groups"),
	ERR_FAILED_TO_DELETE_PERMGROUP_FROM_ROLES(3005,"Error while removing permission group from role"),
	
	ERR_PERMISSION_DOES_NOT_EXIST(4000,"Permission does not exist"),
	ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST(4001,"Some of the selected permissions do not exist"),
	
    ERR_UNABLE_TO_RETRIEVE_PERM_FOR_USER(5000,"Unable to retrieve permissions for user"),
    
    ERR_DIRECTORY_SERVER_CONNECTION_EXCEPTION(10000,"Failed to connect to directory server"),
	
	ERR_DIRECTORY_SERVER_EXCEPTION(10001,"Directory Server Error"),
	
	ERR_UNEXPECTED_EXCEPTION(10002,"Unexpected Error"),
	
	ERR_DIRECTORY_SERVER_CONNECTION_CLOSE_EXCEPTION(10003,"Failed to close connection to directory server"),
	
	ERR_FILE_NOT_FOUND_EXCEPTION(10004, "Could not find /eniq/sw/conf/niq.ini"),
	
	ERR_IO_EXCEPTION(10005, "Failed to read from file");
	
    private final String _message;
    
    private final int _code;
    
    private MESSAGES(final int code, final String message) {
        this._message = message;
        this._code= code;
    }
    
    public String getMessage() {
        return this._message;
    }
    
    public int getCode() {
        return this._code;
    }
}	