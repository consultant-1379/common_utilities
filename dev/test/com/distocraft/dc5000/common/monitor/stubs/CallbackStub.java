package com.distocraft.dc5000.common.monitor.stubs;

import com.distocraft.dc5000.common.monitor.DependentService;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallbackStub implements DependentService {
  private final AtomicBoolean lost;
  private final AtomicBoolean gained;
  private final AtomicBoolean restarted;


  public CallbackStub(final AtomicBoolean lost, final AtomicBoolean gained, final AtomicBoolean restarted) {
    this.lost = lost;
    this.gained = gained;
    this.restarted = restarted;
    resetStates();
  }

  @Override
  public void serviceOffline(final PlatformServices service) {
    System.out.println("serviceOffline("+service+")");
    lost.set(true);
  }

  @Override
  public void serviceAvailable(final PlatformServices service) {
    System.out.println("serviceAvailable("+service+")");
    gained.set(true);
  }

  @Override
  public void serviceRestarted(final PlatformServices service) {
    System.out.println("serviceRestarted("+service+")");
    restarted.set(true);
  }

  @Override
  public String getName() {
    return "callback-stub";
  }

  public void resetStates() {
    set(lost, false);
    set(gained, false);
    set(restarted, false);
  }

  private void set(final AtomicBoolean ab, final boolean value) {
    if (ab != null) {
      ab.set(value);
    }
  }
}
