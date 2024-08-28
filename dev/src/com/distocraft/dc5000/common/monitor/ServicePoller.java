package com.distocraft.dc5000.common.monitor;

import java.util.TimerTask;

/**
 * Polling interface.
 */
public interface ServicePoller {
  /**
   * Poll a service and return its status e.g. Online or Offline
   * @return Service status
   */
  SourceState pollService();

  /**
   * Get the delay between each poll.
   * @return Poll delay in milliseconds
   */
  int getPollPeriod();

  boolean cancel();

  /**
   * Get the timer task for the poller
   * @return ExecutorService timer task for the poller
   */
  TimerTask toTask();
}
