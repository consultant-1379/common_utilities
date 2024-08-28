package com.ericsson.eniq.ldap.util;

import static com.ericsson.eniq.ldap.util.LDAPConstants.EMPTY_STRING;
import static com.ericsson.eniq.ldap.util.LDAPConstants.PASSWORD_PLACEHOLDER;

import com.ericsson.eniq.ldap.vo.*;

/**
 * Utility class for general validation of input parameters.
 * 
 * @author eramano
 * 
 */
public final class ValidationUtil {

  // VALIDATION

  /**
   * A valid ENIQ Events user password must be of at least 8 characters length
   * and must contain at least one digit, one upper case letter and one lower
   * case letter.
   */
  public static final String VALID_PASSWORD_REGEX = "^(?=.{8,255})(?=.*\\d)(?=.*[a-zA-Z])[\\S]*$";

  /**
   * A valid ENIQ Events user email : john.doe@ericsson.com
   */
  public static final String VALID_EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";

  /**
   * A valid ENIQ Events user telephone number should contain only numbers and
   * of minimum length 2.
   */
  public static final String VALID_PHONE_REGEX = "\\d{2,}";

  private ValidationUtil() {
    // do nothing
  }

  /**
   * Validate user details.
   * 
   * This method checks for the validity of password, email and phone number.
   * 
   * @param userVO
   * @return MSG_SUCCESS on successful validation, else error messages.
   */
  public static MESSAGES validateUserDetails(final UserVO userVO) {
    MESSAGES message = MESSAGES.MSG_SUCCESS;
    // first validate password
    if (!isValidPassword(userVO.getPassword())) {
      message = MESSAGES.ERR_INVALID_PASSWORD;
    } else if (!isValidEmail(userVO.getEmail())) {
      message = MESSAGES.ERR_INVALID_EMAIL;
    } else if (!isValidPhoneNumber(userVO.getPhone())) {
      message = MESSAGES.ERR_INVALID_PHONE_NUMBER;
    }
    return message;
  }

  /**
   * Validates if selected telephone number is a valid telephone number.
   * Telephone Number is not a mandatory parameter, so it can be null or empty.
   * If not null and not empty, then it should contain only numbers.
   * 
   * @param phone
   * @return true, if satisfies. else, false.
   */
  public static boolean isValidPhoneNumber(final String phone) {
    boolean isPhoneValid = true;
    if (null != phone && !EMPTY_STRING.equals(phone) && !phone.matches(VALID_PHONE_REGEX)) {
      isPhoneValid = false;
    }
    return isPhoneValid;
  }

  /**
   * Validates if selected email is a valid email address. Email is not a
   * mandatory parameter, so it can be null or empty. If not null and not empty,
   * then it should be a valid email address.
   * 
   * @param email
   * @return true, if satisfies. else, false.
   */
  public static boolean isValidEmail(final String email) {
    boolean isEmailValid = true;
    if (null != email && !EMPTY_STRING.equals(email) && !email.matches(VALID_EMAIL_REGEX)) {
      isEmailValid = false;
    }
    return isEmailValid;
  }

  /**
   * Validates if selected password satisfies the password policy.
   * 
   * @return true, if satisfies. else, false.
   */
  public static boolean isValidPassword(final String userPassword) {
    boolean isPasswordValid = true;
    if (null == userPassword && EMPTY_STRING.equals(userPassword)) {
      isPasswordValid = false;
    } else if (PASSWORD_PLACEHOLDER.equals(userPassword)) {
      isPasswordValid = true;
    } else {
      isPasswordValid = userPassword.matches(VALID_PASSWORD_REGEX);
    }
    return isPasswordValid;
  }

  /**
   * Check if any of the passed in arguments are null. Throw exception if any
   * one of them is null.
   * 
   * @param args
   */
  public static void checkIfNull(final Object... args) {
    for (final Object obj : args) {
      if (null == obj) {
        throw new IllegalArgumentException("Mandatory parameter cannot be null");
      }
    }
  }

}
