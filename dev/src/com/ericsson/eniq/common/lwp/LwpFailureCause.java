package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 * Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

public enum LwpFailureCause {
  UNKNOWN_RMI_HOST,
  REGISTRY_NOT_FOUND,
  OBJECT_NOT_FOUND,
  STALE_RMI_OBJECT,
  RMI_BIND_FAILED,
  CMD_EXEC_FAILED,
  NO_ARGS,
  UNKNOWN;


  public static boolean isUnavailableCause(final LwpFailureCause cause) {
    return cause == UNKNOWN_RMI_HOST || cause == REGISTRY_NOT_FOUND || cause == OBJECT_NOT_FOUND || cause == STALE_RMI_OBJECT;
  }
}
