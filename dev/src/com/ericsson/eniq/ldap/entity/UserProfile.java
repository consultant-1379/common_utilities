/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

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

import com.ericsson.eniq.ldap.vo.UserProfileVO;

/**
 * 
 * User Profile LDAP entity An instance of this class can be persisted to LDAP
 * directly.
 * 
 * @author etonnee
 * @author eramano
 * 
 */
public class UserProfile extends LDAPObject {

  /**
   * User Profile attributes
   */
  private final Attributes userProfileAttrs;

  /**
   * Create a User Profile LDAP entity from user profile value object
   * 
   * @param userProfileVO
   *          user profile value object
   */
  public UserProfile(final UserProfileVO userProfileVO) {
    this.userProfileAttrs = new BasicAttributes(true);
    final Attribute objectClass = new BasicAttribute("objectclass");
    objectClass.add("userprofile");
    userProfileAttrs.put(objectClass);
    if (userProfileVO.getUserId() != null && userProfileVO.getUserId() != EMPTY_STRING) {
      userProfileAttrs.put(UID, userProfileVO.getUserId());
    }
    if (userProfileVO.isPredefined()) {
      userProfileAttrs.put(PREDEFINED, TRUE);
    } else {
      userProfileAttrs.put(PREDEFINED, FALSE);
    }
    final Attribute roleAttr = new BasicAttribute(UNIQUE_MEMBER);
    final Set<String> roles = userProfileVO.getRoles();
    if (null != roles) {
      for (final String role : roles) {
        final String qualifiedRole = "cn=" + role + "," + ROLES_BASE_DN;
        roleAttr.add(qualifiedRole);
      }
      this.userProfileAttrs.put(roleAttr);
    }
  }

  /**
   * Create a User Profile LDAP entity from given attributes
   * 
   * @param attributes
   * @throws NamingException
   */
  public UserProfile(final Attributes attributes) throws NamingException {
    this.userProfileAttrs = attributes;
  }

  @Override
  public Attributes getAttributes(final String name) {
    return this.userProfileAttrs;
  }

  @Override
  public Attributes getAttributes(final Name name) {
    return getAttributes(name.toString());
  }

  @Override
  public Attributes getAttributes(final String name, final String[] ids) {
    final Attributes answer = new BasicAttributes(true);
    Attribute target;
    for (int i = 0; i < ids.length; i++) {
      target = userProfileAttrs.get(ids[i]);
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
