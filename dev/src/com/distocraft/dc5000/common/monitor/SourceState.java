package com.distocraft.dc5000.common.monitor;

/**
 * Service states
 */
public enum SourceState {
  /**
   * Service is online
   */
  Online,
  /**
   * Service is offline
   */
  Offline,
  /**
   * Service was restarted between polling calls
   */
  Restarted,
  /**
   * Service status couldn't be determined, usually an error case
   */
  Undetermined
}
