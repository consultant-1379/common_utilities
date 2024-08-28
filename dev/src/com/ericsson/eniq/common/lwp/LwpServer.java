package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.OBJECT_NOT_FOUND;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.REGISTRY_NOT_FOUND;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.RMI_BIND_FAILED;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.STALE_RMI_OBJECT;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.UNKNOWN;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.UNKNOWN_RMI_HOST;

public class LwpServer {
  private static final String RMI_NAME = "lwphelper";
  private static final Logger LOGGER = Logger.getLogger(RMI_NAME);

  private static final String CONF_DIR = "CONF_DIR";
  private static final String CONF_DIR_DEFAULT = "/eniq/sw/conf";
  private static final String ETLCPROPERTIES = "ETLCServer.properties";

  public static final String ENGINE_PORT = "ENGINE_PORT";
  private static final String ENGINE_PORT_DEFAULT = "1200";
  public static final String ENGINE_HOST = "ENGINE_HOSTNAME";
  private static final String ENGINE_HOST_DEFAULT = "engine";
  private static final String LWP_RMI_PROCESS_PORT = "LWP_RMI_PROCESS_PORT";
  private static final String LWP_RMI_PROCESS_PORT_DEFAULT = "60005";

  private static final File ETLC_PROPERTIES = new File(System.getProperty(CONF_DIR, CONF_DIR_DEFAULT), ETLCPROPERTIES);
  private static Properties etlcProperties = null;

  public static void main(final String[] args) {
    if (args != null && args.length > 0 && "ping".equalsIgnoreCase(args[0])) {
      int exitCode = 0;
      try {
        pingHandler();
      } catch (LwpException e) {
        System.err.println(e.getMessage());
        exitCode = 1;
      }
      System.exit(exitCode);
    }
    final LogWatcher logPropertiesWatcher = new LogWatcher(LOGGER);
    Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
      @Override
      public void run() {
        System.out.println("Shutting down at " + new Date());
        logPropertiesWatcher.stopWatcher();
      }
    });
    LogHelper.log(LOGGER, Level.INFO, "Starting on " + new Date());
    logPropertiesWatcher.start();
    try {
      final int registryPort = getRegistryPort();
      registerRmi(registryPort, LOGGER);
    } catch (LwpException e) {
      e.printStackTrace(System.out);
      System.exit(1);
    } catch (Throwable t) {
      t.printStackTrace(System.out);
      System.exit(1);
    }
  }

  private static Properties getEtlcServerProperties() throws LwpException {
    if (etlcProperties == null) {
      try {
        etlcProperties = new Properties();
        etlcProperties.load(new FileInputStream(ETLC_PROPERTIES));
      } catch (FileNotFoundException e) {
        LOGGER.log(Level.WARNING, e.getMessage(), e);
      } catch (IOException e) {
        throw new LwpException(e, UNKNOWN);
      }
    }
    return etlcProperties;
  }

  /**
   * Check the validity of the RMI object bound to the RMI Registry.
   *
   * @throws LwpException thrown if the RMI Registry if unavailable, the RMI obect id not bound to the RMI
   *                      Registry or the bound object is invalid
   */
  public static void pingHandler() throws LwpException {
    getLwpHandler(null);
  }

  /**
   * Get an instance of the lightweight process handler
   *
   * @throws LwpException thrown if the RMI Registry if unavailable, the RMI obect id not bound to the RMI
   *                      Registry or the bound object is invalid
   */
  public static ILWPHelperRMI getLwpHandler(final Logger logger) throws LwpException {
    final int registryPort = LwpServer.getRegistryPort();
    final String registryHost = LwpServer.getRegistryHost();
    final Registry rmiRegistry = LwpServer.getRegistry(registryHost, registryPort, logger);
    try {
      rmiRegistry.list();//force an error if the registry isnt available
      final ILWPHelperRMI helper = (ILWPHelperRMI) rmiRegistry.lookup(RMI_NAME);
      try {
        helper.ping(); // force an error if its not avaiable..
      } catch (RemoteException e) {
        final String msg = "Stale RMI reference registered for " + RMI_NAME;
        LogHelper.error(logger, msg, e);
        throw new LwpException(msg, e, STALE_RMI_OBJECT);
      }
      return helper;
    } catch (RemoteException e) {
      final String msg = "Errors looking up RMI object for " + RMI_NAME;
      LogHelper.error(logger, msg, e);
      throw new LwpException(msg, e, UNKNOWN);
    } catch (NotBoundException e) {
      final String msg = "No RMI object bound for " + RMI_NAME;
      LogHelper.error(logger, msg, e);
      throw new LwpException(msg, e, OBJECT_NOT_FOUND);
    }
  }


  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  public static int getRegistryPort() throws LwpException {
    try {
      final String value = getEtlcServerProperties().getProperty(ENGINE_PORT, ENGINE_PORT_DEFAULT);
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new LwpException(e, UNKNOWN);
    }
  }

  public static String getRegistryHost() throws LwpException {
    return getEtlcServerProperties().getProperty(ENGINE_HOST, ENGINE_HOST_DEFAULT);
  }

  static void registerRmi(final int registryPort, final Logger logger) throws LwpException {
    final Registry rmiRegistry = getRegistry(registryPort, logger);
    try {
      final ILWPHelperRMIImpl lwpHelper = new ILWPHelperRMIImpl(Integer.parseInt(getEtlcServerProperties().getProperty(LWP_RMI_PROCESS_PORT,LWP_RMI_PROCESS_PORT_DEFAULT)));
      LogHelper.log(LOGGER, Level.INFO, "Binding " + RMI_NAME + " into RMI Registry");
      rmiRegistry.rebind(RMI_NAME, lwpHelper);
      LogHelper.log(LOGGER, Level.INFO, "Bound " + RMI_NAME + " into RMI Registry");
    } catch (RemoteException e) {
      final String msg = "Could not bind helper to RMI Registry";
      LogHelper.error(LOGGER, msg, e);
      throw new LwpException(msg, e, RMI_BIND_FAILED);
    }
  }

  public static Registry getRegistry(final int registryPort, final Logger logger) throws LwpException {
    return getRegistry("localhost", registryPort, logger);
  }

  public static Registry getRegistry(final String registryHost, final int registryPort, final Logger logger) throws LwpException {
    final String registryAddress = registryHost + ":" + registryPort;
    try {
      LogHelper.log(logger, Level.FINE, "Looking up RMI registry on " + registryAddress);
      final Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
      registry.list();//force an error if the registry isnt available
      LogHelper.log(logger, Level.FINE, "Found an RMI Registry at " + registryAddress);
      return registry;
    } catch (ConnectException e) {
      final String msg = "No RMI Registry available at " + registryAddress;
      LogHelper.error(logger, msg, e);
      throw new LwpException(msg, e, REGISTRY_NOT_FOUND);
    } catch (RemoteException e) {
      String msg = "Error trying to get RMI Registry at " + registryAddress;
      LwpFailureCause cause = UNKNOWN;
      if (e.getMessage().contains("Connection reset by peer")) {
        msg = "No RMI Registry available at " + registryAddress;
        cause = REGISTRY_NOT_FOUND;
      } else if (e.getCause() instanceof UnknownHostException) {
        msg = "RMI host not found - " + registryAddress;
        cause = UNKNOWN_RMI_HOST;
      }
      LogHelper.error(logger, msg, e);
      throw new LwpException(msg, e, cause);
    }
  }

  static class LogWatcher extends Thread {
    private final Logger logger;
    private boolean watchLogProperties = true;

    public LogWatcher(final Logger logger) {
      super("log-config-watcher");
      this.logger = logger;
      setDaemon(true);
    }

    public void stopWatcher() {
      watchLogProperties = false;
      interrupt();
    }

    @Override
    public void run() {
      final String logpFileName = System.getProperty("java.util.logging.config.file");
      if (logpFileName == null) {
        return;
      }
      final File logpFile = new File(logpFileName);
      if (!logpFile.exists()) {
        LogHelper.log(logger, Level.ALL, "Log configuration file " + logpFileName + " does not exits");
        return;
      }
      long lastModified = logpFile.lastModified();
      while (watchLogProperties) {
        if (lastModified < logpFile.lastModified()) {
          lastModified = logpFile.lastModified();

          final Level old = logger.getLevel();
          LogHelper.log(logger, Level.INFO, "Reloading " + logpFileName);
          try {
            LogManager.getLogManager().readConfiguration();
          } catch (IOException e) {
            LogHelper.warning(logger, "Failed to reload logging properties", e);
          }
          if (old.intValue() != logger.getLevel().intValue()) {
            LogHelper.log(logger, Level.INFO, logger.getName() + " log level changed from " + old.getName() +
              " to " + logger.getLevel().getName());
          }
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {/*--*/}
      }
    }
  }
}
