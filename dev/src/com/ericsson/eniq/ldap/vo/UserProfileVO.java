package com.ericsson.eniq.ldap.vo;

import java.util.Set;

/**
 * User Profile VO
 * @author eramano
 */
public class UserProfileVO implements IValueObject{
	
	/**
	 * User ID 
	 */
	private String userId=null;
	/**
	 * Is a predefined user? 
	 */
	private boolean predefined;
	/**
	 * Set of roles mapped to user
	 */
	private Set<String> roles=null;
	
	public String getUserId() {
		return this.userId;
	}
	public void setUserId(final String userId) {
		this.userId = userId;
	}
	public boolean isPredefined() {
		return this.predefined;
	}
	public void setPredefined(final boolean predefined) {
		this.predefined = predefined;
	}
	public Set<String> getRoles() {
		return this.roles;
	}
	public void setRoles(final Set<String> roles) {
		this.roles = roles;
	}
}
