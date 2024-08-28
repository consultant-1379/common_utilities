package com.ericsson.eniq.ldap.vo;


/**
 * Login Value Object
 * 
 * @author eramano
 */
public class LoginVO implements IValueObject{
	/**
	 * Login User ID
	 */
	private String loginId=null;
	/**
	 * Login password 
	 */
	private String password=null;
	
	/**
	 * Get Login ID
	 * @return loginId
	 */
	public String getLoginId() {
		return this.loginId;
	}
	/**
	 * Set Login Id
	 * @param loginId
	 */
	public void setLoginId(final String loginId) {
		this.loginId = loginId;
	}
	/**
	 * Get password
	 * @return password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * Set password
	 * @param password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}
}
