package com.ericsson.eniq.ldap.entity;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.*;
import static com.ericsson.eniq.ldap.util.LDAPConstants.*;

import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import com.ericsson.eniq.ldap.management.LDAPAttributes;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;

/**
 * Permission Group LDAP entity. An instance of this class can be persisted to
 * LDAP directly.
 * 
 * @author eramano
 * 
 */
public class PermissionGroup extends LDAPObject {

  /**
   * Permission group attributes like CN, PREDEFINED.
   */
  private final Attributes permGroupAttrs;

  /**
   * Create a permission group LDAP object from permission group value object.
   * 
   * @param permGroupVO
   *          permission group value object
   */
  public PermissionGroup(final PermissionGroupVO permGroupVO) {

    this.permGroupAttrs = new BasicAttributes(true);
    final Attribute objectClass = new BasicAttribute("objectclass");
    objectClass.add("permissiongroup");
    this.permGroupAttrs.put(objectClass);
    if (permGroupVO.getPermissionGroupName() != null && !permGroupVO.getPermissionGroupName().isEmpty()) {
      this.permGroupAttrs.put(LDAPAttributes.COMMON_NAME, permGroupVO.getPermissionGroupName());
    }
    if (permGroupVO.getTitle() != null && !permGroupVO.getTitle().isEmpty()) {
      this.permGroupAttrs.put(LDAPAttributes.TITLE, permGroupVO.getTitle());
    } else {
      this.permGroupAttrs.put(LDAPAttributes.TITLE, ONE_SPACE);
    }
    if (permGroupVO.getDescription() != null && !permGroupVO.getDescription().isEmpty()) {
      this.permGroupAttrs.put(LDAPAttributes.DESCRIPTION, permGroupVO.getDescription());
    } else {
      this.permGroupAttrs.put(LDAPAttributes.DESCRIPTION, ONE_SPACE);
    }
    if (permGroupVO.isPredefined()) {
      this.permGroupAttrs.put(LDAPAttributes.PREDEFINED, TRUE);
    } else {
      this.permGroupAttrs.put(LDAPAttributes.PREDEFINED, FALSE);
    }
    final Attribute permissionsAttr = new BasicAttribute(LDAPAttributes.UNIQUE_MEMBER);
    final Set<String> permissions = permGroupVO.getPermissions();
    if (null != permissions) {
      for (final String permission : permissions) {
        final String qualifiedPermission = "cn=" + permission + "," + PERMISSIONS_BASE_DN;
        permissionsAttr.add(qualifiedPermission);
      }
      this.permGroupAttrs.put(permissionsAttr);
    }
  }

  /**
   * Create a permission group LDAP object from a set of attributes
   * 
   * @param attributes
   * @throws NamingException
   */
  public PermissionGroup(final Attributes attributes) throws NamingException {
    this.permGroupAttrs = attributes;
  }

  @Override
  public Attributes getAttributes(final String name) {
    return this.permGroupAttrs;
  }

  @Override
  public Attributes getAttributes(final Name name) {
    return getAttributes(name.toString());
  }

  @Override
  public Attributes getAttributes(final String name, final String[] ids) {
    final Attributes answer = new BasicAttributes(true);
    Attribute target = null;
    for (int i = 0; i < ids.length; i++) {
      target = this.permGroupAttrs.get(ids[i]);
      if (target != null) {
        answer.put(target);
      }
    }
    return answer;
  }

  @Override
  public Attributes getAttributes(final Name name, final String[] ids) {
    return getAttributes(name.toString(), ids);
  }
}
