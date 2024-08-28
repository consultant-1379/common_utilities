package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

public class LwpException extends Exception {
  private LwpFailureCause causeCode = null;


  public LwpException(final String msg, final LwpFailureCause causeCode) {
    super(msg);
    this.causeCode = causeCode;
  }

  public LwpException(final String msg, final Throwable cause, final LwpFailureCause causeCode) {
    super(msg, cause);
    this.causeCode = causeCode;
  }

  public LwpException(final Throwable cause, final LwpFailureCause causeCode) {
    super(cause);
    this.causeCode = causeCode;
  }

  public LwpException(final Throwable cause) {
    super(cause);
    this.causeCode = LwpFailureCause.UNKNOWN;
  }

  public LwpFailureCause getCauseCode() {
    return this.causeCode;
  }
}
