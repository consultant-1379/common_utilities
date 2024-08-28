package com.ericsson.eniq.ldap.vo;

import java.util.Set;

/**
 * Role value object.
 * 
 * @author eramano
 * 
 */
public class RoleVO implements IValueObject {

  /**
   * Role Name
   */
  private String roleName;

  /**
   * Role Description
   */
  private String description = "";

  /**
   * Role Title
   */
  private String title;

  /**
   * Field indicating if the role is a predefined role
   */
  private boolean isPredefined;

  /**
   * List of permission groups assigned to this role
   */
  private Set<String> permissionGroups;
  
  /**
   * remark field to indicate if a role configuration is inconsistent
   */
  private String remarks;

  /**
   * Get role name
   * 
   * @return roleName
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * Set role name
   * 
   * @param roleName
   *          Role Name
   */
  public void setRoleName(final String roleName) {
    this.roleName = roleName;
  }

  /**
   * Get Role Description
   * 
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Set Role Description
   * 
   * @param description
   *          Role Description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Get Role Title
   * 
   * @return title Role Title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Set Role Title
   * 
   * @param title
   *          role title
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Check if this is a predefined role
   * 
   * @return true, if predefined,else false.
   */
  public boolean isPredefined() {
    return this.isPredefined;
  }

  /**
   * Set if this role is a predefined role
   * 
   * @param isPredefined
   *          true or false
   */
  public void setPredefined(final boolean isPredefined) {
    this.isPredefined = isPredefined;
  }

  /**
   * Get all permission groups associated with this role.
   * 
   * @return <code>Set<String><code> set of permission groups
   */
  public Set<String> getPermissionGroups() {
    return this.permissionGroups;
  }

  /**
   * Set all permission groups associated with this role
   * 
   * @param permissionGroups
   *          set of permission groups
   */
  public void setPermissionGroups(final Set<String> permissionGroups) {
    this.permissionGroups = permissionGroups;
  }

  /**
   * Set remarks
   * @param remarks
   */
  public void setRemarks(final String remarks) {
	  this.remarks = remarks;
  }

  /**
   * Get remarks
   * @return
   */
  public String getRemarks() {
	  return this.remarks;
  }
}
