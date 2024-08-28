package com.ericsson.eniq.ldap.vo;

/**
 * Permission value object.
 * 
 * @author eramano
 * 
 */
public class PermissionVO implements IValueObject {

	/**
	   * Permission Name
	   */
	  private String permissionName;

	  /**
	   * Permission Description
	   */
	  private String description = "";

	  /**
	   * Permission Title
	   */
	  private String title;

	  /**
	   * Permission Solution Set Name
	   */
	  private String solutionSetName;
	  
	  /**
	   * Get Permission name
	   * 
	   * @return permissionName
	   */
	  public String getPermissionName() {
	    return this.permissionName;
	  }

	  /**
	   * Set Permission name
	   * 
	   * @param permissionName Permission Name
	   */
	  public void setPermissionName(final String permissionName) {
	    this.permissionName = permissionName;
	  }

	  /**
	   * Get Permission Description
	   * 
	   * @return description
	   */
	  public String getDescription() {
	    return this.description;
	  }

	  /**
	   * Set Permission Description
	   * 
	   * @param description Permission Description
	   */
	  public void setDescription(final String description) {
	    this.description = description;
	  }

	  /**
	   * Get Permission Title
	   * 
	   * @return title Permission Title
	   */
	  public String getTitle() {
	    return this.title;
	  }

	  /**
	   * Set Permission Title
	   * 
	   * @param title Permission title
	   */
	  public void setTitle(final String title) {
	    this.title = title;
	  }
	  
	  /**
	   * Get permission's solution set name
	   * @return solutionSetName
	   */
	  public String getSolutionSetName() {
			return this.solutionSetName;
	  }

	  /**
	   * Set permission's solution set name
	   * @param solutionSetName
	   */
	  public void setSolutionSetName(final String solutionSetName) {
			this.solutionSetName = solutionSetName;
	  }	  
}
