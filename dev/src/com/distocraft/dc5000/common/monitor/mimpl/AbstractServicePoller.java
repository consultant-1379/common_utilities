package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.DependentService;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.ServicePoller;
import com.distocraft.dc5000.common.monitor.SourceState;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class implementing with most of the polling functionality.
 * Protocol pollers should extend this class and they just need to implement the pollService() method
 */
abstract class AbstractServicePoller extends TimerTask implements ServicePoller {

  /**
   * State of last polling call
   */
  protected SourceState sourceState = SourceState.Undetermined;

  /**
   * Callback interface to inform of state changes.
   */
  private final DependentService callback;

  /**
   * The service being monitored
   */
  private final PlatformServices service;

  /**
   * Logger
   */
  private final Logger logger;

//  private boolean onlyNotifyChanges = true;

  /**
   * Logging counter
   */
  private static final AtomicInteger COUNT = new AtomicInteger(0);

  /**
   * Default constructor
   *
   * @param callback Object wanting to know about state changes.
   * @param service  The service being monitored
   */
  AbstractServicePoller(final DependentService callback, final PlatformServices service) {
    this.callback = callback;
    this.service = service;
    logger = Logger.getLogger("monitoring." + service.name() + "." + COUNT.getAndIncrement());
  }

  /**
   * @see TimerTask#run()
   */
  @Override
  public void run() {
    try {
      debug("Polling " + service);
      final SourceState state = pollService();
      debug("Got State of " + state  + " from "+service+" poller");
      if (state == SourceState.Restarted) {
        sourceState = SourceState.Restarted;
        serviceRestarted();
      } else if (state == SourceState.Online && sourceState != SourceState.Online) {
        sourceState = SourceState.Online;
        serviceAvailable();
      } else if (state == SourceState.Offline && sourceState != SourceState.Offline) {
        sourceState = SourceState.Offline;
        serviceOffline();
      }
      debug("Service " + service.name() + " state set to " + sourceState);
    } catch (MonitoringException e) {
      sourceState = SourceState.Undetermined;
      error("Exception polling service " + service.name() + ", setting state to " + sourceState, e);
    } catch (Throwable t) {
      sourceState = SourceState.Undetermined;
      error("Unknown exception polling service " + service.name() + ", setting state to " + sourceState, t);
    }
    debug("Determined state for " + service.name() + " as " + sourceState);
  }

  /**
   * Inform callback of service restart
   */
  private void serviceRestarted() {
    debug("Informing " + callback.getName() + " of restarted service " + service.name());
    callback.serviceRestarted(service);
  }

  /**
   * Inform callback of service regained
   */
  private void serviceAvailable() {
    debug("Informing " + callback.getName() + " of gained service " + service.name());
    callback.serviceAvailable(service);
  }

  /**
   * Inform callback of service lost
   */
  private void serviceOffline() {
    debug("Informing " + callback.getName() + " of lost service " + service.name());
    callback.serviceOffline(service);
  }

  /**
   * Log at level INFO
   *
   * @param msg Text to log
   */
  protected void info(final String msg) {
    log(Level.INFO, msg, null);
  }

  /**
   * Log at level FINE
   *
   * @param msg Text to log
   */
  protected void debug(final String msg) {
    log(Level.FINE, msg, null);
  }

  /**
   * Log an error
   * This will handle the cases where SQLException.getNextException() != null
   *
   * @param msg   Text to log
   * @param error The exception, can be null
   */
  protected void error(final String msg, final Throwable error) {
    Throwable real = error;
    if (error != null) {
      if (error instanceof SQLException) {
        final SQLException sqle = (SQLException) error;
        if (sqle.getNextException() != null) {
          real = sqle.getNextException();
        }
      }
    }
    log(Level.SEVERE, msg, real);
  }


  /**
   * Log an error
   *
   * @param level The Level to log it at
   * @param msg   Text to log
   * @param error The exception, can be null
   */
  protected void log(final Level level, final String msg, final Throwable error) {
    if (error == null) {
      logger.log(level, msg);
    } else {
      logger.log(level, msg, error);
    }
  }

  /**
   * AbstractServicePoller extends TimerTask to it just returns itself
   *
   * @see ServicePoller#toTask()
   */
  @Override
  public TimerTask toTask() {
    return this;
  }

  /**
   * Get a String value from StaticProperties, if the property isn't defined, return the default value
   * @param propName The property name
   * @param defaultValue Default value to return in the property isn't found
   * @return The value for the property
   */
  protected String getStringStaticProperty(final String propName, final String defaultValue) {
    String returnValue;
    try {
      returnValue = StaticProperties.getProperty(propName);
      debug("Static Property '" + propName + "' --> '" + returnValue + "'");
    } catch (NoSuchFieldException e) {
      debug("No Static Property called '" + propName + "' found, using default value of '" + defaultValue + "'");
      returnValue = defaultValue;
    } catch (NullPointerException e) {
      debug("Static Properties not initialized, using default value of '" + defaultValue + "' for '" + propName + "'");
      returnValue = defaultValue;
    }
    return returnValue;
  }

  /**
   * Get an Integer value from StaticProperties, if the property isn't defined, return the default value
   *
   * @param propName     Property name
   * @param defaultValue Default value if not defined
   * @return Property value
   */
  protected int getIntStaticProperty(final String propName, final int defaultValue) {
    int returnValue;
    final String value = getStringStaticProperty(propName, null);
    if (value == null) {
      returnValue = defaultValue;
    } else {
      returnValue = Integer.parseInt(value);
    }
    debug("Static Property '" + propName + "' --> '" + returnValue + "'");
    return returnValue;
  }
}
