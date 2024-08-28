package com.distocraft.dc5000.common.monitor;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.mimpl.ServicePollerFactory;
import com.distocraft.dc5000.common.monitor.stubs.CallbackStub;
import com.ericsson.eniq.common.testutilities.EngineRmiStub;
import com.ericsson.eniq.common.testutilities.PollerBaseTestCase;
import com.ericsson.eniq.common.testutilities.RmiHelper;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceMonitorTest extends PollerBaseTestCase {

  private CallbackStub callback = null;

  private final AtomicBoolean offline = new AtomicBoolean(false);
  private final AtomicBoolean online = new AtomicBoolean(false);
  private final AtomicBoolean restarted = new AtomicBoolean(false);

  @Before
  public void before(){
    RmiHelper.getInstance().unbind(testRmiName);
    callback = new CallbackStub(offline, online, restarted);
  }

  @After
  public void after(){
    ServiceMonitor.stopMonitoring();
  }

  @Test
  public void testEngineGoneOffline() throws RemoteException, InterruptedException {
    final EngineRmiStub rmiStub = EngineRmiStub.getStub(true);
    RmiHelper.getInstance().bind(testRmiName, rmiStub);

    ServiceMonitor.monitorService(PlatformServices.Engine, callback);
    // Let the poller work for a bit
    Thread.sleep(2000);

    //Stop the Rmi instance
    RmiHelper.getInstance().unbind(testRmiName);
    callback.resetStates();
    Thread.sleep(2000);

    Assert.assertEquals("No serviceOffline() call on callback object", true, offline.get());
    Assert.assertEquals("Unexpected serviceAvailable() call on callback object", false, online.get());
    Assert.assertEquals("Unexpected serviceRestarted() call on callback object", false, restarted.get());
  }

  @Test
  public void testEngineRestartedBetweenPolls() throws RemoteException, InterruptedException {
    // different than testEngineGained() in that the rmi service is restarted between polls
    // online -(restarted)-> online

    staticProps.setProperty(ServiceMonitor.RMI_MONITOR_PERIOD, "3000");
    StaticProperties.giveProperties(staticProps);
    ServicePollerFactory.setTestProperties(etlcProperties);


    EngineRmiStub rmiStub = EngineRmiStub.getStub(true);
    RmiHelper.getInstance().bind(testRmiName, rmiStub);

    ServiceMonitor.monitorService(PlatformServices.Engine, callback);

    Thread.sleep(4000);

    callback.resetStates();
    RmiHelper.getInstance().unbind(testRmiName);
    rmiStub = EngineRmiStub.getStub(true);
    //this needs to be a different instance
    RmiHelper.getInstance().bind(testRmiName, rmiStub);

    Thread.sleep(4000);

    Assert.assertEquals("Unexpected serviceOffline() call on callback object", false, offline.get());
    Assert.assertEquals("Unexpected serviceAvailable() call on callback object", false, online.get());
    Assert.assertEquals("No serviceRestarted() call on callback object", true, restarted.get());

  }

  /*
   * Test to check that polls will pick up on an offlined, then onlined service
   */
  @Test
  public void testEngineRestarted_OffOn() throws RemoteException, InterruptedException {
    //online --> offline --> online
    EngineRmiStub rmiStub = EngineRmiStub.getStub(true);
    RmiHelper.getInstance().bind(testRmiName, rmiStub);

    ServiceMonitor.monitorService(PlatformServices.Engine, callback);
    // Let the poller work for a bit
    Thread.sleep(2000);

    //Stop the Rmi instance
    RmiHelper.getInstance().unbind(testRmiName);
    callback.resetStates();
    // Let the poller work for a bit
    Thread.sleep(2000);

    Assert.assertEquals("No serviceOffline() call on callback object", true, offline.get());
    Assert.assertEquals("Unexpected serviceAvailable() call on callback object", false, online.get());
    Assert.assertEquals("Unexpected serviceRestarted() call on callback object", false, restarted.get());

    rmiStub = EngineRmiStub.getStub(true);
    RmiHelper.getInstance().bind(testRmiName, rmiStub);
    callback.resetStates();

    // Let the poller work for a bit
    Thread.sleep(3000);

    Assert.assertEquals("No serviceAvailable() call on callback object", true, online.get());
    Assert.assertEquals("Unexpected serviceOffline() call on callback object", false, offline.get());
    Assert.assertEquals("Unexpected serviceRestarted() call on callback object", false, restarted.get());
  }

  @Test
  public void testEngineMonitoring() throws RemoteException, InterruptedException {
    final EngineRmiStub rmiStub = EngineRmiStub.getStub(true);
    RmiHelper.getInstance().bind(testRmiName, rmiStub);

    ServiceMonitor.monitorService(PlatformServices.Engine, callback);

    Thread.sleep(3000);

    Assert.assertEquals(false, offline.get());
    Assert.assertEquals(true, online.get());
    Assert.assertEquals(false, restarted.get());

  }

  @Test
  public void testSmfDefault(){
    // defaults, nothing set
    Assert.assertTrue(ServiceMonitor.isSmfEnabled());

    //Get the value form system
    System.setProperty("smf.enabled", "false");
    Assert.assertFalse(ServiceMonitor.isSmfEnabled());
    System.clearProperty("smf.enabled");

    //get it from static
    final Properties props = new Properties();
    props.setProperty("smf.enabled", "true");
    StaticProperties.giveProperties(props);
    Assert.assertTrue(ServiceMonitor.isSmfEnabled());

    //System overrides static
    System.setProperty("smf.enabled", "false");
    StaticProperties.giveProperties(props);
    Assert.assertFalse(ServiceMonitor.isSmfEnabled());
  }

}
