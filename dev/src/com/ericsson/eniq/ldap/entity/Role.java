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

import com.ericsson.eniq.ldap.vo.RoleVO;

/**
 * Role LDAP entity. An instance of this class can be persisted to LDAP
 * directly.
 * 
 * @author eramano
 * 
 */
public class Role extends LDAPObject {

  /**
   * Role attributes
   */
  private final Attributes roleAttrs;

  /**
   * Create a role LDAP object from role value object.
   * 
   * @param roleVO
   *          role value object
   */
  public Role(final RoleVO roleVO) {

    this.roleAttrs = new BasicAttributes(true);
    final Attribute objectClass = new BasicAttribute("objectclass");
    objectClass.add("role");
    this.roleAttrs.put(objectClass);
    if (roleVO.getRoleName() != null && !roleVO.getRoleName().isEmpty()) {
      this.roleAttrs.put(COMMON_NAME, roleVO.getRoleName());
    }
    if (roleVO.getTitle() != null && !roleVO.getTitle().isEmpty()) {
      this.roleAttrs.put(TITLE, roleVO.getTitle());
    }
    if (roleVO.getDescription() != null && !roleVO.getDescription().isEmpty()) {
      this.roleAttrs.put(DESCRIPTION, roleVO.getDescription());
    } else {
      this.roleAttrs.put(DESCRIPTION, ONE_SPACE);
    }

    if (roleVO.isPredefined()) {
      this.roleAttrs.put(PREDEFINED, TRUE);
    } else {
      this.roleAttrs.put(PREDEFINED, FALSE);
    }
    final Attribute permGroupsAttr = new BasicAttribute(UNIQUE_MEMBER);
    final Set<String> permGroups = roleVO.getPermissionGroups();
    if (null != permGroups) {
      for (final String permGroup : permGroups) {
        final String qualifiedPermGroup = "cn=" + permGroup + "," + PERMISSIONGROUP_BASE_DN;
        permGroupsAttr.add(qualifiedPermGroup);
      }
      this.roleAttrs.put(permGroupsAttr);
    }
  }

  /**
   * Create a role LDAP object from given attributes
   * 
   * @param attributes
   * @throws NamingException
   */
  public Role(final Attributes attributes) throws NamingException {
    this.roleAttrs = attributes;
  }

  @Override
  public Attributes getAttributes(final String name) {
    return this.roleAttrs;
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
      target = this.roleAttrs.get(ids[i]);
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
