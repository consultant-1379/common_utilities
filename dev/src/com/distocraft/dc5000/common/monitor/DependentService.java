package com.distocraft.dc5000.common.monitor;

/**
 * Application wanting to know if a service they require (or are interested in) stops/restarts/offlines implement
 * this interface. The ServiceMonitor task will call various methods on this interface if a service
 * start/stops/restarts
 *
 * Service lost (
 *
 */
public interface DependentService {
  /**
   * Called if a dependent service can't be contacted
   * @param service The service that can't be contacted
   */
  void serviceOffline(final PlatformServices service);

  /**
   * Called if a connection was re-established to a service.
   * @param service The service that can be contacted again
   */
  void serviceAvailable(final PlatformServices service);

  /**
   * Called if a service was restarted with in the polling period
   * @param service The service that was restarted
   */
  void serviceRestarted(final PlatformServices service);

  /**
   * Applicatoin name, used mainly for logging purposes.
   * @return Monitoring application e.g. engine
   */
  String getName();
}
