package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.CMD_EXEC_FAILED;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.NO_ARGS;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.UNKNOWN;

class ILWPHelperRMIImpl extends UnicastRemoteObject implements ILWPHelperRMI {
  /**
   * Current systems new line character
   */
  private static final String NEW_LINE = System.getProperty("line.separator");
  /**
   * RMI Object name in RMI Registry
   */
  public static final String RMI_NAME = "lwphelper";
  /**
   * Logger instance
   */
  private static final Logger LOGGER = Logger.getLogger(RMI_NAME);

  private static ILWPHelperRMIImpl LOCAL_LWP_HANDLER = null;

  static {
    try {
      LOCAL_LWP_HANDLER = new ILWPHelperRMIImpl();
    } catch (RemoteException e) {/***/
    }
  }

  /**
   * Default RMI constructor
   */
  protected ILWPHelperRMIImpl() throws RemoteException {
    super();
  }
  
  /**
   * Default RMI constructor
   */
  protected ILWPHelperRMIImpl(int lwpHelperUserPort) throws RemoteException {
    super(lwpHelperUserPort);
  }
  

  public static ILWPHelperRMI getLocalLwpHandler() {
    return LOCAL_LWP_HANDLER;
  }

  @Override
  public LwpOutput execute(final List<String> command, final boolean redirectStderr) throws RemoteException, LwpException {
    LogHelper.log(LOGGER, Level.INFO, "Executing command " + command);
    if (command == null || command.isEmpty()) {
      throw new LwpException("Command is null or empty", NO_ARGS);
    }
    final ProcessBuilder pBuilder = new ProcessBuilder(command);
    try {
      pBuilder.redirectErrorStream(redirectStderr);
      final Process process = pBuilder.start();
      LogHelper.log(LOGGER, Level.INFO, "Starting process for " + command);
      final long startTime = System.currentTimeMillis();
      final ProcessOutputReader stdoutReader = new ProcessOutputReader(command.toString(), process.getInputStream());
      stdoutReader.start();
      ProcessOutputReader stderrReader = null;
      if (!redirectStderr) {
        stderrReader = new ProcessOutputReader(command.toString(), process.getErrorStream());
        stderrReader.start();
      }
      final LwpOutput output = new LwpOutput();
      output.setExitCode(process.waitFor());
      final long endTime = System.currentTimeMillis();
      LogHelper.log(LOGGER, Level.INFO, "Process execution for command " + command + " took " + (endTime - startTime) +
        "msec, exit code was " + output.getExitCode());
      stdoutReader.close();
      if (stderrReader != null) {
        stderrReader.close();
        output.setStderr(stderrReader.getProcessOutput().trim());
      }
      output.setStdout(stdoutReader.getProcessOutput().trim());
      if (LOGGER.isLoggable(Level.FINEST)) {
        LogHelper.log(LOGGER, Level.FINEST, "Output from command " + command + " was " + output.toString());
      }
      return output;
    } catch (IOException e) {
      LogHelper.error(LOGGER, "Command " + command + " failed", e);
      throw new LwpException("Failed to execute " + command, e, CMD_EXEC_FAILED);
    } catch (InterruptedException e) {
      LogHelper.error(LOGGER, "Command " + command + " interrupted!", e);
      throw new LwpException("Failed to execute " + command, e, CMD_EXEC_FAILED);
    }
  }

  @Override
  public void ping() throws RemoteException {
    //Do nothing...
  }

  /**
   * Class to read the output from an executed process
   */
  private class ProcessOutputReader extends Thread {
    private final BufferedReader stdoutReader;
    private final StringBuilder output;
    private Throwable readError = null;

    public ProcessOutputReader(final String name, final InputStream inputStream) {
      super(name);
      setDaemon(true);
      final InputStreamReader isr = new InputStreamReader(inputStream);
      this.stdoutReader = new BufferedReader(isr);
      this.output = new StringBuilder();
    }

    public String getProcessOutput() throws LwpException {
      if (readError != null) {
        throw new LwpException("Error reader process output", readError, UNKNOWN);
      }
      return output.toString();
    }

    @Override
    public void run() {
      String line;
      try {
        while ((line = stdoutReader.readLine()) != null) {
          output.append(line).append(NEW_LINE);
        }
      } catch (IOException e) {
        LogHelper.error(LOGGER, "IO error reading process output", e);
        readError = e;
      } catch (Throwable e) {
        LogHelper.error(LOGGER, "Error reading process output", e);
        readError = e;
      }
    }

    public void close() {
      try {
        stdoutReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}