package com.ericsson.eniq.ldap.vo;

import java.util.*;

/**
 * Permission Group value object
 * 
 * @author eramano
 * 
 */
public class PermissionGroupVO implements IValueObject {

  /**
   * Permission Group Name
   */
  private String permissionGroupName;

  /**
   * Permission Group Description
   */
  private String description = "";

  /**
   * Permission Group Title
   */
  private String title;

  /**
   * Field indicating if the permission group is a predefined group
   */
  private boolean isPredefined;

  /**
   * List of permissions assigned to this group
   */
  private Set<String> permissions;

  public String getPermissionGroupName() {
    return this.permissionGroupName;
  }

  public void setPermissionGroupName(final String permissionGroupName) {
    this.permissionGroupName = permissionGroupName;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public boolean isPredefined() {
    return this.isPredefined;
  }

  public void setPredefined(final boolean isPredefined) {
    this.isPredefined = isPredefined;
  }

  public Set<String> getPermissions() {
    return this.permissions;
  }

  public void setPermissions(final Set<String> permissions) {
    this.permissions = permissions;
  }

}
