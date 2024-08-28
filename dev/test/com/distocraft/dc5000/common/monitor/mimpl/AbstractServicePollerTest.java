package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.SourceState;
import com.distocraft.dc5000.common.monitor.stubs.CallbackStub;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractServicePollerTest {


  private CallbackStub callback = null;
  private final AtomicBoolean lost = new AtomicBoolean(false);
  private final AtomicBoolean gained = new AtomicBoolean(false);
  private final AtomicBoolean restarted = new AtomicBoolean(false);

  @Before
  public void before() throws RemoteException {
    callback = new CallbackStub(lost, gained, restarted);
    callback.resetStates();
  }

  @Test
  public void testStaticPropertiesSimple(){
    StaticProperties.giveProperties(null);
    final String expected = "default_value";
    final TestPoller poller = new TestPoller(SourceState.Offline, callback);
    final String actual = poller.getStringStaticProperty("abc", expected);
    assertEquals("Default value not returned", expected, actual);
  }

  @Test
  public void testLogInfo(){
    final String message = "info_message";
    final TestPoller poller = new TestPoller(SourceState.Offline, callback){
      @Override
      protected void log(final Level level, final String msg, final Throwable error) {
        assertEquals("Info Logged at wrong level", Level.INFO, level);
        assertEquals("Wrong message logged", message, msg);
        assertNull("No Error should be logged (use error)", error);
      }
    };
    poller.info(message);
  }

  @Test
  public void testLogRealSqlError(){
    final SQLException real = new SQLException("read-exception");
    final SQLException wrapper = new SQLException("wrapper-exception");

    final AtomicBoolean checkOk = new AtomicBoolean(false);
    final TestPoller poller = new TestPoller(SourceState.Offline, callback){
      @Override
      protected void log(final Level level, final String msg, final Throwable error) {
        if(wrapper.getNextException() == null){
          checkOk.set(wrapper.equals(error));
        } else {
          checkOk.set(real.equals(error));
        }
      }
    };
    poller.error("check", wrapper);
    assertTrue("Actual SQL error not logged!", checkOk.get());
    /**
     * The root cause SQLException can be wrapped in another SQLException.
     * e.g. login error
     * try{
     *  //login with wrong password or connection limit exceeded
     * } catch (SQLException e){...} e.getMessage() is general login error |
     *
     * e.getMessage() is general login error (login failed)
     * e.getNextException() is proper error e.g. wrong username/password or connection limit exceeded
     * Note getNextException() can return null.
     */
    wrapper.setNextException(real);
    checkOk.set(false);
    poller.error("check", wrapper);
    assertTrue("Actual SQL error not logged!", checkOk.get());
  }

  @Test
  public void testToTask(){
    final TestPoller poller = new TestPoller(SourceState.Offline, callback);
    final TimerTask task = poller.toTask();
    assertEquals(poller, task);
  }

  @Test
  public void testGetStaticPropertyDefault(){
    final TestPoller poller = new TestPoller(SourceState.Offline, callback);
    final int value = poller.getIntStaticProperty("abc", 1);
    assertEquals("Wrong default value returned", 1, value);
  }

  @Test
  public void testGetStaticProperty(){
    final Properties p = new Properties();
    p.setProperty("abc", "8");
    StaticProperties.giveProperties(p);
    final TestPoller poller = new TestPoller(SourceState.Offline, callback);
    int value = poller.getIntStaticProperty("abc", 10);
    assertEquals("Wring property value returned", 8, value);

    // prop not defined, return default
    value = poller.getIntStaticProperty("def", 6);
    assertEquals("Wring property value returned", 6, value);
  }

  @Test
  public void testMonitoringExceptionUndetermined() {
    final TestPoller poller = new TestPoller(SourceState.Offline, callback) {
      @Override
      public SourceState pollService() {
        throw new MonitoringException("");
      }
    };
    poller.run();
    assertStates(false, false, false);
  }

  @Test
  public void testRunServiceGained() {
    final TestPoller poller = new TestPoller(SourceState.Offline, callback);
    poller.run();
    callback.resetStates();
    poller.run(SourceState.Online);
    assertStates(false, true, false);
  }

  @Test
  public void testRunServiceOffline() {
    final AbstractServicePoller poller = new TestPoller(SourceState.Offline, callback);
    poller.run();
    assertStates(true, false, false);
  }

  @Test
  public void testRunServiceRestarted() {
    final AbstractServicePoller poller = new TestPoller(SourceState.Restarted, callback);
    poller.run();
    assertStates(false, false, true);
  }

  @Test
  public void testRunServiceOnline() {
    final AbstractServicePoller poller = new TestPoller(SourceState.Online, callback);
    poller.run();
    assertStates(false, true, false);
  }

  @Test
  public void testNoExceptionFromTask() {
    final AbstractServicePoller poller = new TestPoller(callback);
    try {
      poller.run();
    } catch (Throwable e) {
      fail("No Exceptions should be thrown from the run() method");
    }
  }

  private void assertStates(final boolean eLost, final boolean eGained, final boolean eRestarted) {
    assertEquals("Unexpected serviceOffline() call on callback object", eLost, lost.get());
    assertEquals("Unexpected serviceAvailable() call on callback object", eGained, gained.get());
    assertEquals("Unexpected serviceRestarted() call on callback object", eRestarted, restarted.get());
  }

  private class TestPoller extends AbstractServicePoller {
    private SourceState returnState;

    public TestPoller(final CallbackStub callback) {
      super(callback, PlatformServices.Engine);
      returnState = null;
    }

    public TestPoller(final SourceState state, final CallbackStub callback) {
      this(callback);
      returnState = state;
    }

    public void run(final SourceState state) {
      returnState = state;
      super.run();
    }

    @Override
    public SourceState pollService() {
      if (returnState == null) {
        throw new RuntimeException("");
      } else {
        return returnState;
      }
    }

    @Override
    public int getPollPeriod() {
      return 0;
    }
  }
}
