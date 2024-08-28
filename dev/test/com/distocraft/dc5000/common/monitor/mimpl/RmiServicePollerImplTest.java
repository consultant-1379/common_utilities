package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;

import com.distocraft.dc5000.common.monitor.ServiceMonitor;
import com.distocraft.dc5000.common.monitor.ServicePoller;
import com.distocraft.dc5000.common.monitor.SourceState;
import com.distocraft.dc5000.common.monitor.stubs.CallbackStub;
import com.ericsson.eniq.common.testutilities.EngineRmiStub;
import com.ericsson.eniq.common.testutilities.PollerBaseTestCase;
import com.ericsson.eniq.common.testutilities.RmiHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Test;
import ssc.rockfactory.RockException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class RmiServicePollerImplTest extends PollerBaseTestCase {

  @Test
  public void testPollMalformedUrl() throws IOException, RockException, SQLException {

    final RmiServicePollerImpl poller = new RmiServicePollerImpl(new CallbackStub(null, null, null),
      PlatformServices.Engine, "rmi:sdf", "-1", ".");
    try {
      poller.pollService();
      fail("MalformedURLException should have been thrown");
    } catch (MonitoringException e) {
      assertTrue(e.getCause().getClass() == MalformedURLException.class);
    }
  }

  @After
  public void after() throws RemoteException {
    stopRmiStub(testRmiName);
  }

  @Test
  public void testRemoteConnectErrorsHandled() throws RemoteException {
    final String msg = "should be handled";
    final RemoteException re = new RemoteException(msg, new ConnectException());
    final RmiServicePollerImpl poller = new RmiServicePollerImpl(null, PlatformServices.Engine,
      testRmiName, RmiHelper.REGISTRY_HOST, "localhost") {
      @Override
      protected Remote lookup() throws MalformedURLException, NotBoundException, RemoteException {
        if (re == null) {
          throw new NullPointerException(msg);
        } else {
          throw re;
        }
      }
    };
    startRmiStub(testRmiName);
    final SourceState state = poller.pollService();
    assertEquals("ConnecExceptions should result in poll state of Offline", SourceState.Offline, state);
  }

  @Test
  public void testUnknownConnectErrorsHandled() throws RemoteException {
    final String msg = "should be handled";
    final RemoteException re = new RemoteException(msg, new NullPointerException());
    checkConnectWithError(re, msg);
  }

  @Test
  public void testClassErrorsHandled() throws RemoteException {
    final String msg = "should be handled";
    final RemoteException re = new RemoteException(msg, new ClassNotFoundException(getClass().getName()));
    checkConnectWithError(re, msg);
  }

  @Test
  public void testUnknownErrorsHandled() throws RemoteException {
    final String msg = "should be handled";
    checkConnectWithError(null, msg);
  }

  private void checkConnectWithError(final RemoteException re, final String eMessage) throws RemoteException {
    final RmiServicePollerImpl poller = new RmiServicePollerImpl(null, PlatformServices.Engine,
      testRmiName, RmiHelper.REGISTRY_HOST, "localhost") {
      @Override
      protected Remote lookup() throws MalformedURLException, NotBoundException, RemoteException {
        if (re == null) {
          throw new NullPointerException(eMessage);
        } else {
          throw re;
        }
      }
    };
    startRmiStub(testRmiName);
    try {
      poller.pollService();
      fail("Expected Exception not thrown or it was buried!");
    } catch (MonitoringException e) {
      if (re == null) {
        assertEquals("Expected exception not handled/cought?", eMessage, e.getCause().getMessage());
      } else {
        final Throwable t = re.getCause();
        assertEquals("Excpected RemoteException not handled/cought?", re.getCause(), t);
      }
      //OK, expected to catch this.
    }
  }

  @Test
  public void testGetPollPeriod() {
    final int pollPeriod = 24278;
    final Properties sp = new Properties();
    sp.setProperty(ServiceMonitor.RMI_MONITOR_PERIOD, Integer.toString(pollPeriod));
    StaticProperties.giveProperties(sp);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    final int pp = poller.getPollPeriod();
    assertEquals("JDBC Poll Period not read correctly from Static Properties", pollPeriod, pp);
  }

  @Test
  public void testGetPollPeriod_Default() {
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    final int pp = poller.getPollPeriod();
    assertEquals("RMI Poll Period not read correctly from Static Properties", TEST_RMI_MONITOR_PERIOD, pp);
  }

  @Test
  public void testPollRestarted() throws IOException, RockException, SQLException {
    startRmiStub(testRmiName);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    SourceState state = poller.pollService();
    assertEquals(SourceState.Online, state);
    stopRmiStub(testRmiName);
    startRmiStub(testRmiName);
    state = poller.pollService();
    assertEquals(SourceState.Restarted, state);
  }

  @Test
  public void testPollOnline() throws IOException, RockException, SQLException {
    startRmiStub(testRmiName);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    SourceState state = poller.pollService();
    assertEquals(SourceState.Online, state);

    //test polling same thing doesn't detect it as a restart
    state = poller.pollService();
    assertEquals(SourceState.Online, state);
  }

  @Test
  public void testPollOffline() throws IOException, RockException, SQLException {
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    final SourceState state = poller.pollService();
    assertEquals(SourceState.Offline, state);
  }

  @Test
  public void testPollOfflineProcessKilled() throws RemoteException {
    startRmiStub(testRmiName);

    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    SourceState state = poller.pollService();
    assertEquals(SourceState.Online, state);

    //Dont unbind, just destoy the server object...
    EngineRmiStub.destroyStub();
    RmiHelper.getInstance().restartRegistry();

    
    state = poller.pollService();
    assertEquals(SourceState.Offline, state);
  }
}
