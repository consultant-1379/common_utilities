package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.monitor.DependentService;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.RMIService;
import com.distocraft.dc5000.common.monitor.ServiceMonitor;
import com.distocraft.dc5000.common.monitor.SourceState;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Permission;

/**
 * RMI Poller implmentation, just tried to open a connection to an RMI server
 */
class RmiServicePollerImpl extends AbstractServicePoller {
  /**
   * Default poll period
   */
  protected static final int DEFAULT_RMI_MONITOR_PERIOD = 10000;
  /**
   * RMI URL
   */
  private final String rmiAddress;
  /**
   * Last RMI reference obtainted, used to detect restarts.
   */
  private Remote lastReference = null;

  /**
   * @param callback Callback object
   * @param service  Service being monitored
   * @param rmiName  RMI Object name
   * @param rmiPort  RMI Registry Port
   * @param rmiHost  RMI Registry Host
   */
  RmiServicePollerImpl(final DependentService callback, final PlatformServices service,
                       final String rmiName, final String rmiPort, final String rmiHost) {
    super(callback, service);
    this.rmiAddress = "//" + rmiHost + ":" + rmiPort + "/" + rmiName;
    System.setSecurityManager(new RMISecurityManager() {
      @Override
      public void checkPermission(final Permission perm) {
        /*0*/
      }
    });
  }

  /*
   * Tests override this...
   */
  protected Remote lookup() throws NotBoundException, MalformedURLException, RemoteException {
    final Remote remote = Naming.lookup(this.rmiAddress);
    /*try {*/
    if (remote instanceof RMIService) {
      final RMIService service = (RMIService) remote;
      service.ping();
    } else {
      //fallback and check the status method.
      try {
        final Method statusMethod = remote.getClass().getMethod("status");
        statusMethod.invoke(remote);
      } catch (NoSuchMethodException e) {
        throw new MonitoringException("RMI Interface need to implement 'public List<String> status()' method", e);
      } catch (InvocationTargetException e) {
        if (e.getCause() == null) {
          throw new RemoteException(e.getMessage(), e);
        } else {
          throw new RemoteException(e.getCause().getMessage(), e.getCause());
        }
      } catch (IllegalAccessException e) {
        throw new MonitoringException("RMI Interface need to implement 'public List<String> status()' method", e);
      }
    }
    return remote;
  }

  /**
   * RMI Poll implementation
   *
   * @return State of service e.g. Online, Offline or Restarted
   */
  @Override
  public SourceState pollService() {
    SourceState state;
    debug("Polling " + toString());
    try {
      final Remote remoteObject = lookup();
      debug("Polling OK " + toString());
      state = SourceState.Online;
      if (lastReference != null && !lastReference.equals(remoteObject)) {
        state = SourceState.Restarted;
      }
      lastReference = remoteObject;
    } catch (NotBoundException e) {
      state = SourceState.Offline;
    } catch (MalformedURLException e) {
      error("Malformed RMI URL '" + this.rmiAddress + "'", e);
      throw new MonitoringException("Malformed RMI URL", e);
    } catch (RemoteException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        error("No Stub Class found, is codebase set?", e);
        throw new MonitoringException("Could not connect to RMI source", e);
      } else if (e.getCause() instanceof java.rmi.ConnectException || e.getCause() instanceof java.net.ConnectException) {
        state = SourceState.Offline;
      } else {
        error("Error trying to connect to RMI source", e);
        throw new MonitoringException("Could not connect to RMI source", e);
      }
    } catch (Throwable t) {
      error("Unknown error trying to connect to RMI source", t);
      throw new MonitoringException("Could not connect to RMI source", t);
    }
    if (state != SourceState.Online) {
      lastReference = null;
    }
    return state;
  }

  /**
   * Get delay between sucessive RMI polls
   *
   * @return Value from property RMI_MONITOR_PERIOD or DEFAULT_RMI_MONITOR_PERIOD if not defined.
   */
  @Override
  public int getPollPeriod() {
    return getIntStaticProperty(ServiceMonitor.RMI_MONITOR_PERIOD, DEFAULT_RMI_MONITOR_PERIOD);
  }

  @Override
  public String toString() {
    final StringWriter sb = new StringWriter();
    sb.append("RmiMonitor");
    sb.append("{rmiAddress='").append(rmiAddress).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
