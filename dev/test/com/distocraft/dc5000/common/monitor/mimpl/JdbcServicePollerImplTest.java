package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;

import com.distocraft.dc5000.common.monitor.ServiceMonitor;
import com.distocraft.dc5000.common.monitor.ServicePoller;
import com.distocraft.dc5000.common.monitor.SourceState;
import com.ericsson.eniq.common.testutilities.PollerBaseTestCase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ssc.rockfactory.RockException;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBDRIVERNAME;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBURL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JdbcServicePollerImplTest extends PollerBaseTestCase {

  private Connection jdbStub = null;
  private static final String db_starttime_query = "db_starttime_query";

  @Before
  public void before() {
    System.setProperty(db_starttime_query, "call now();");
  }

  @After
  public void after() {
    System.clearProperty(db_starttime_query);
    stopJdbcStub(jdbStub);
  }

  @Test
  public void testGetPollPeriod(){
    final int pollPeriod = 123456;
    final Properties sp = new Properties();
    sp.setProperty(ServiceMonitor.JDBC_MONITOR_PERIOD, Integer.toString(pollPeriod));
    StaticProperties.giveProperties(sp);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    final int pp = poller.getPollPeriod();
    assertEquals("JDBC Poll Period not read correctly from Static Properties", pollPeriod, pp);
  }

  @Test
  public void testGetPollPeriod_Default() {
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    final int pp = poller.getPollPeriod();
    assertEquals("JDBC Poll Period not read correctly from Static Properties",
      JdbcServicePollerImpl.DEFAULT_JDBC_MONITOR_PERIOD, pp);
  }

  @Test
  public void testUnknownErrorsHandled(){
    final String msg = "should be handled";
    final JdbcServicePollerImpl poller = new JdbcServicePollerImpl(null, PlatformServices.repdb, etlcProperties){
      @Override
      protected String getDbStartTime(Connection conn) throws SQLException {
        throw new NullPointerException(msg);
      }
    };
    jdbStub = startJdbcStub(etlcProperties);
    try{
      poller.pollService();
      fail("Expected Exception not thrown or it was buried!");
    } catch (MonitoringException e){
      assertEquals("Expected exception not cought?", msg, e.getCause().getMessage());
      //OK, expected to catch this.
    }
  }

  @Test
  public void testPollMalformedUrl(){
    final Properties props = new Properties(etlcProperties);
    props.put(DBURL, ".../.");
    ServicePollerFactory.setTestProperties(props);

    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    try{
      poller.pollService();
      fail("No error thrown since URL is invalid");
    } catch (MonitoringException e){
      //OK, expected to catch this.
    }
  }

  @Test
  public void testPollWrongDriver(){
    final Properties props = new Properties(etlcProperties);
    props.put(DBDRIVERNAME, "bbbbbbbbb");
    ServicePollerFactory.setTestProperties(props);

    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    try{
      poller.pollService();
      fail("No error thrown since driver is not a valid class");
    } catch (MonitoringException e){
      //OK, expected to catch this.
    }
  }

  @Test
  public void testPollOffline() {
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    final SourceState state = poller.pollService();
    assertEquals(SourceState.Offline, state);
  }

  @Test
  public void testPollOnline() {
    jdbStub = startJdbcStub(etlcProperties);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    final SourceState state = poller.pollService();
    assertEquals(SourceState.Online, state);
  }

  @Test
  public void testPollRestarted() throws IOException, RockException, SQLException {
    jdbStub = startJdbcStub(etlcProperties);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    SourceState state = poller.pollService();
    assertEquals(SourceState.Online, state);
    stopJdbcStub(jdbStub);
    jdbStub = startJdbcStub(etlcProperties);
    state = poller.pollService();
    assertEquals(SourceState.Restarted, state);
  }

}
