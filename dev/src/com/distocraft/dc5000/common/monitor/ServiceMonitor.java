package com.distocraft.dc5000.common.monitor;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.mimpl.ServicePollerFactory;
import java.util.Timer;
import java.util.logging.Logger;

/**
 * Service Monitoring entry point
 * Usage: ServiceMonitor.monitorService(PlatformServices.repdb, callback);
 * If connection is lost to repdb, <code></code>callback.serviceOffline(PlatformServices.repdb)</code> will be called
 */
public class ServiceMonitor {

  /**
   * Static Property defining poll interval for RMI connections
   */
  public static final String RMI_MONITOR_PERIOD = "rmi_monitor_period";
  /**
   * Static Property defining poll interval for LDAP connections
   */
  public static final String LDAP_MONITOR_PERIOD = "ldap_monitor_period";
  /**
   * Static Property defining poll interval for JDBC connections
   */
  public static final String JDBC_MONITOR_PERIOD = "jdbc_monitor_period";
  /**
   * Task Scheduler
   */
  private static Timer taskTimer = null;
  /**
   * Logger...
   */
  private static final Logger logger = Logger.getLogger("service_monitoring");

  private static ServiceMonitor __instance__ = null;

  private static final Object mutex = new Object();

  private ServiceMonitor() {
    taskTimer = new Timer();
  }

  public static ServiceMonitor getInstance() {
    if (__instance__ == null) {
      __instance__ = new ServiceMonitor();
    }
    return __instance__;
  }

  /**
   * Stop all monitoriong tasks synchronously.
   */
  public static void stopMonitoring() {
    if (__instance__ != null) {
      __instance__.destroy();
      __instance__ = null;
    }
  }

  /**
   * Request a service be monitored and what client to inform of state changes.
   *
   * @param service  The service to monitor
   * @param callback The client to inform of state changes
   * @return The poller instance
   * @throws MonitoringException On unrecoverable errors
   */
  public static ServicePoller monitorService(final PlatformServices service,
                                             final DependentService callback) throws MonitoringException {
    logger.fine("Requested to " + service.name() + " by " + callback.getName());
    final ServiceMonitor serviceMonitor = getInstance();
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(service, callback, logger);
    final int pollPeriod = poller.getPollPeriod();
    serviceMonitor.schedule(poller, pollPeriod);
    logger.fine("Monitor for " + service.name() + ":" + callback.getName() + " scheduler at a period of " + pollPeriod);
    return poller;
  }

  /**
   * Scheduler the task synchronously
   *
   * @param monitor       Task to schedule
   * @param monitorPeriod Perion, in milliseconds, to execute task
   */
  private void schedule(final ServicePoller monitor, final int monitorPeriod) {
    synchronized (mutex) {
      taskTimer.scheduleAtFixedRate(monitor.toTask(), 0, monitorPeriod);
    }
  }

  /**
   * Find out if SMF is being used to manage the Start/Stop/Restart process dependancies.
   *
   * Without anything set, this will return <code>true</code> i.e. SMF should be used to managed the service
   * dependancies with regard to start/stop/restart if a dependant service goes offline/online/restarted
   *
   * Can be overriden via static property or system property 'smf.enabled = true|false'
   * System property overrided static property
   *
   * @return <code>trrue</code> if SMF is being used to managd dependancies, <code>false</code> otherwise (i.e.
   * application need to take care of it)
   */
  public static boolean isSmfEnabled(){
    final String smf_property = "smf.enabled";
    return Boolean.valueOf(System.getProperty(smf_property, StaticProperties.getProperty(smf_property, "true")));
  }

  private void destroy() {
    synchronized (mutex) {
      taskTimer.cancel();
    }
    taskTimer = null;
  }
}
