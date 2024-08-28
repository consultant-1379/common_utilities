package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class used to execute OS processes.
 * <p/>
 * On Solaris, a Runtime.exec() spawns a process using the Solaris fork() function when tries tried to reserve the
 * same amount of swap and the current JVM process. In the case of Engine on Stats, this can be 4Gig. If there's not
 * 4G swap avaialble the Runtime.exec() will fail with a Not Engough Space error (even though its not going to use 4G
 * swap)
 * <p/>
 * <p/>
 * This tries to get a handle on a light weight process helper (RMI Object) and use that to execute the process.
 * If the RMI Object isnt available, it will try and exec the process directly i.e. spawn the process form the current
 * JVM. See Java Bug 5049299
 */
public class LwProcess {

  /**
   * Execute an OS command.
   * This will try and get the light weight process helper and use that to execute the process. If hte lwp helper isnt
   * available, it will try and exec the process form the current JVM.
   * <p/>
   * This will only throw LwpHandlerException is the process cant be spawned e.g. executable not found. Errors from a
   * spawned process are contained in the StringBuilder object passed in.
   *
   * @param command       The command to execute
   * @param logger        Log object
   * @throws LwpException If there are eny errors trying to execute the process
   */
  public static LwpOutput execute(final List<String> command, final boolean redirectStderr,
                            final Logger logger) throws LwpException {
    ILWPHelperRMI handler;
    try {
      LogHelper.log(logger, Level.FINE, "Getting lwp handler");
      handler = LwpServer.getLwpHandler(logger);
      LogHelper.log(logger, Level.FINE, "Got RMI handler");
    } catch (LwpException e) {
      if (LwpFailureCause.isUnavailableCause(e.getCauseCode())) {
        // Remote object isnt avaiable for some reason, try getting a local instance of the handler
        // This brings back the possibility of the NotEnoughSpace errors when running on Solaris (forks)
        LogHelper.log(logger, Level.INFO, "No RMI handler available, trying with local handler");
        handler = ILWPHelperRMIImpl.getLocalLwpHandler();
      } else {
        throw new LwpException(e);
      }
    }
    try {
      LogHelper.log(logger, Level.INFO, "Executing " + command);
      final LwpOutput lwpOutput = handler.execute(command, redirectStderr);
      LogHelper.log(logger, Level.FINE, "Command " + command + " executed with exit code " + lwpOutput.getExitCode());
      return lwpOutput;
    } catch (RemoteException e) {
      LogHelper.error(logger, "Remote error running command", e);
      throw new LwpException(e);
    }
  }

  public static LwpOutput execute(final String command) throws LwpException {
    return execute(command, true, null);
  }

  /**
   * Execute an OS command.
   * This will try and get the light weight process helper and use that to execute the process. If hte lwp helper isnt
   * available, it will try and exec the process form the current JVM.
   * <p/>
   * This will only throw LwpHandlerException is the process cant be spawned e.g. executable not found. Errors from a
   * spawned process are contained in the StringBuilder object passed in.
   *
   * @param command       The command to execute
   * @param logger        Log object
   * @throws LwpException If there are eny errors trying to execute the process
   */
  public static LwpOutput execute(final String command, final boolean redirectStderr,
                            final Logger logger) throws LwpException {
    //final List<String> commandList = Arrays.asList(command.split(" "));
	  List<String> commandList = new ArrayList<String>();
	  commandList.add("/bin/sh");
	  commandList.add("-c");
	  commandList.add(command);
    return execute(commandList, redirectStderr, logger);
  }
}
