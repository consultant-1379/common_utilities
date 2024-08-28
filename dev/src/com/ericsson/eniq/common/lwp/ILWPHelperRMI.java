package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ILWPHelperRMI extends Remote {
  /**
   * Execute an OS command.
   *
   * @param command Command to execute
   * @return Result of command, exit code & ant output (stdout/stderr)
   */
  LwpOutput execute(final List<String> command, final boolean redirectStderr) throws RemoteException, LwpException;

  /**
   * Ping function for a client to check if RMI object is valid or not.
   * No exception means RMI object is valid, object is invalid if excpetion is thrown.
   */
  void ping() throws RemoteException;
}
