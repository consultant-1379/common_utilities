/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.eniq.ldap.entity;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.*;
import static com.ericsson.eniq.ldap.util.LDAPConstants.*;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import com.ericsson.eniq.ldap.vo.UserVO;

/**
 * User LDAP entity. An instance of this class can be persisted to LDAP
 * directly.
 * 
 * @author etonnee
 * @author eramano
 * 
 */
public class User extends LDAPObject {

  /**
   * User Attributes
   */
  private final Attributes userAttrs;

  /**
   * Create a User LDAP entity from user value object
   * 
   * @param userVO
   *          user value object
   */
  public User(final UserVO userVO) {
    this.userAttrs = new BasicAttributes(true);
    final Attribute objectClass = new BasicAttribute("objectclass");
    objectClass.add("EniqEvents");
    objectClass.add("inetOrgPerson");
    objectClass.add("organizationalPerson");
    objectClass.add("person");
    objectClass.add("top");
    final String fullName = userVO.getFullName();
    userAttrs.put(objectClass);
    // first set resetpassword to false - not forcing user to change password on
    // login
    userAttrs.put(RESET_PWD_ONLOGIN, FALSE);
    // password policy DIT entry
    userAttrs.put(PWD_POLICY_SUB_ENTRY_OPENDJ, PASSWORD_POLICY_DN);
    // userAttrs.put(PWD_POLICY_SUB_ENTRY, PASSWORD_POLICY_DN);
    // first name
    if (userVO.getFname() != null && !userVO.getFname().isEmpty()) {
      userAttrs.put(GIVEN_NAME, userVO.getFname());
    } else {
      userAttrs.put(GIVEN_NAME, ONE_SPACE);
    }
    // last name
    if (userVO.getLname() != null && !userVO.getLname().isEmpty()) {
      userAttrs.put(SECOND_NAME, userVO.getLname());
    } else {
      userAttrs.put(SECOND_NAME, ONE_SPACE);
    }
    // common name is full name
    if (fullName != null && !fullName.isEmpty()) {
      userAttrs.put(COMMON_NAME, fullName);
    } else {
      userAttrs.put(COMMON_NAME, ONE_SPACE);
    }
    // user id
    if (userVO.getUserId() != null && !userVO.getUserId().isEmpty()) {
      userAttrs.put(UID, userVO.getUserId());
    } else {
      userAttrs.put(UID, ONE_SPACE);
    }
    // email
    if (userVO.getEmail() != null && !userVO.getEmail().isEmpty()) {
      userAttrs.put(MAIL, userVO.getEmail());
    } else {
      userAttrs.put(MAIL, ONE_SPACE);
    }
    // password
    if (userVO.getPassword() != null && !userVO.getPassword().isEmpty()) {
      userAttrs.put(USER_PASSWORD, userVO.getPassword());
    } else {
      userAttrs.put(USER_PASSWORD, ONE_SPACE);
    }
    // phone number
    if (userVO.getPhone() != null && !userVO.getPhone().isEmpty()) {
      userAttrs.put(TELEPHONE_NUMBER, userVO.getPhone());
    } else {
      userAttrs.put(TELEPHONE_NUMBER, DEFAULT_PHONE_NUMBER);
    }
    // organization
    if (userVO.getOrg() != null && !userVO.getOrg().isEmpty()) {
      userAttrs.put(ORGANISATION, userVO.getOrg());
    } else {
      userAttrs.put(ORGANISATION, ONE_SPACE);
    }
    // password reset
    if (null != userVO.getRoles() && userVO.getRoles().contains(PREDEFINED_SYSADMIN_ROLE)) {
      userAttrs.put(RESET_PWD_ONLOGIN, FALSE); // admins do not have to reset
                                               // password on login
    } else if (userVO.getPassword() != null && !PASSWORD_PLACEHOLDER.equals(userVO.getPassword())) {
      userAttrs.put(RESET_PWD_ONLOGIN, TRUE); // non-admin has to change
                                              // password if password is changed
    }
    this.userAttrs.put(PWD_POLICY_SUB_ENTRY_OPENDJ, PASSWORD_POLICY_DN);
  }

  /**
   * Create a user LDAP entity from given attributes
   * 
   * @param attributes
   * @throws NamingException
   */
  public User(final Attributes attributes) throws NamingException {
    this.userAttrs = attributes;
  }

  @Override
  public Attributes getAttributes(final String name) {
    return userAttrs;
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
      target = userAttrs.get(ids[i]);
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
