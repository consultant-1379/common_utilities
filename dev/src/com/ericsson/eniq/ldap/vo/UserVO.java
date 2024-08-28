package com.ericsson.eniq.ldap.vo;

import java.util.Date;
import java.util.Set;

import com.ericsson.eniq.ldap.util.USERSTATE;

/**
 * User Value Object
 * 
 * @author eramano
 *
 */

public class UserVO  implements IValueObject{
	
	/**
	 * Number of milliseconds in a day
	 */
	static final double MILIIS_IN_DAY =  (24 * 60 * 60 * 1000);

	/**
	 * User ID 
	 */
	private String userId=null;
	/**
	 * Password 
	 */
	private String password=null;
	/**
	 * First Name 
	 */
	private String fname=null;
	/**
	 * Last Name 
	 */
	private String lname=null;
	/**
	 * EMail 
	 */
	private String email="";
	/**
	 * Phone Number 
	 */
	private String phone="";
	/**
	 * Organization 
	 */
	private String org=null;
	
	/**
	 * Field indicating if the role is a predefined role
	 */
	private boolean isPredefined;
	
	/**
	 * Field to track user state (locked, expired etc)
	 */
	private USERSTATE userState;
	
	/**
	 * Set of roles 
	 */
	private Set<String> roles=null;
	
	/**
	 * Last login date
	 */
	private Date lastLoginDate=null;
	
	/**
	 * @return
	 */
	public String getFullName() {
		return this.fname+" "+this.lname;
	}
	
	public String getUserId() {
		return this.userId;
	}
	public void setUserId(final String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return this.password;
	}
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getFname() {
		return this.fname;
	}
	public void setFname(final String fname) {
		this.fname = fname;
	}
	public String getLname() {
		return this.lname;
	}
	public void setLname(final String lname) {
		this.lname = lname;
	}
	public String getEmail() {
		return this.email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}
	public String getPhone() {
		return this.phone;
	}
	public void setPhone(final String phone) {
		this.phone = phone;
	}
	public String getOrg() {
		return this.org;
	}
	public void setOrg(final String org) {
		this.org = org;
	}
	public boolean isPredefined() {
		return this.isPredefined;
	}

	public void setPredefined(final boolean isPredefined) {
		this.isPredefined = isPredefined;
	}
	public Set<String> getRoles() {
		return this.roles;
	}
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public void setUserState(final USERSTATE state) {
		this.userState = state;
	}

	public USERSTATE getUserState() {
		return userState;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	
	/**
	 * If last login date is known, return number of days since last login date to now
	 * (rounded to nearest day). If the last login date is unknown return "-".
	 * 
	 * @return Number of days since last login or "-" if date is unknown.
	 */
	public String getDaysSinceLastLogin() {
		String daysSinceLastLogin = "-";
		
		if (lastLoginDate != null) {
			final long now = new Date().getTime();
			daysSinceLastLogin = String.valueOf(Math.round((now - lastLoginDate.getTime())/MILIIS_IN_DAY)); 
		}

		return daysSinceLastLogin;
	}
}
