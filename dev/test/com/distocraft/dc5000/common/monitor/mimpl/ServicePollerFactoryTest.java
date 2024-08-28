package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.ServicePoller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBDRIVERNAME;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBPASSWORD;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBURL;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBUSERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServicePollerFactoryTest {
  private static final File confDir = new File(System.getProperty("java.io.tmpdir"), "ServicePollerFactoryTest");
  private static final File hosts = new File(confDir, "hosts");
  private static final File service_names = new File(confDir, "service_names");

  private static final String jdbcUrl = "jdbc:hsqldb:mem:service-db-test";
  private static final String jdbcDriver = "org.hsqldb.jdbcDriver";
  private static Connection conn = null;

  @BeforeClass
  public static void beforeClass() throws IOException, ClassNotFoundException, SQLException {
    System.setProperty("CONF_DIR", confDir.getPath());
    System.setProperty("ETC_DIR", confDir.getPath());
    delete(confDir);
    if (!confDir.exists() && !confDir.mkdirs()) {
      fail("Failed to create setup dir " + confDir.getPath());
    }
    final File etlcProperties = new File(confDir, "ETLCServer.properties");
    if (!etlcProperties.exists() && !etlcProperties.createNewFile()) {
      fail("Failed to create " + etlcProperties.getPath());
    }
    final BufferedWriter writer = new BufferedWriter(new FileWriter(etlcProperties));
    try {
      writer.write("ENGINE_DB_URL=" + jdbcUrl + "\n");
      writer.write("ENGINE_DB_USERNAME=SA\n");
      writer.write("ENGINE_DB_PASSWORD=\n");
      writer.write("ENGINE_DB_DRIVERNAME=" + jdbcDriver + "\n");
      writer.newLine();
    } finally {
      writer.close();
    }

    if (!hosts.exists() && !hosts.createNewFile()) {
      fail("Failed to create file " + hosts.getPath());
    }

    if (!service_names.exists() && !service_names.createNewFile()) {
      fail("Failed to create file " + service_names.getPath());
    }

    ServicePollerFactory.setTestProperties(null);// resets it...


    Class.forName(jdbcDriver);
    conn = DriverManager.getConnection(jdbcUrl, "SA", "");
    final Statement stmt = conn.createStatement();

    stmt.execute("CREATE TABLE META_DATABASES (\n" +
      "\tUSERNAME varchar(30),\n" +
      "\tVERSION_NUMBER varchar(32),\n" +
      "\tTYPE_NAME varchar(15),\n" +
      "\tCONNECTION_ID numeric(38, 0),\n" +
      "\tCONNECTION_NAME varchar(30),\n" +
      "\tCONNECTION_STRING varchar(400),\n" +
      "\tPASSWORD varchar(30),\n" +
      "\tDESCRIPTION varchar(32000),\n" +
      "\tDRIVER_NAME varchar(100),\n" +
      "\tDB_LINK_NAME varchar(128)\n" +
      ");");

    stmt.execute("insert into Meta_databases (\n" +
      "\tUSERNAME,\n" +
      "\tVERSION_NUMBER,\n" +
      "\tTYPE_NAME,\n" +
      "\tCONNECTION_ID,\n" +
      "\tCONNECTION_NAME,\n" +
      "\tCONNECTION_STRING,\n" +
      "\tPASSWORD,\n" +
      "\tDESCRIPTION,\n" +
      "\tDRIVER_NAME)\n" +
      " values (\n" +
      "\t'test-username',\n" +
      "\t'0',\n" +
      "\t'USER',\n" +
      "\t'2',\n" +
      "\t'dwh',\n" +
      "\t'" + jdbcUrl + "',\n" +
      "\t'',\n" +
      "\t'The DataWareHouse Database',\n" +
      "\t'" + jdbcDriver + "');");

    stmt.close();


  }

  @AfterClass
  public static void afterClass() {
    delete(confDir);
    try {
      conn.createStatement().execute("SHUTDOWN;");
    } catch (SQLException e) {/**/}
  }

  @Test
  public void testGetPollersNoHostsSetup() throws IOException {
    //Cover case where the service_name entry exists but the hosts entry doesn't
    //The servicename hostname part should be used.
    BufferedWriter writer = new BufferedWriter(new FileWriter(hosts, false));
    writer.write("127.0.0.1  localhost some_host webserver");
    writer.newLine();
    writer.close();

    writer = new BufferedWriter(new FileWriter(service_names, false));
    writer.write("127.0.0.1::some_host::engine");
    writer.newLine();
    writer.close();
    //engine entry in service_names but not in hosts file
    final ServicePollerFactory test = ServicePollerFactory.getInstance();
    final ServicePoller poller = test.getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof RmiServicePollerImpl);
    assertTrue(poller.toString().contains("//some_host:"));
  }

  @Test
  public void testGetPollers() {

    final ServicePollerFactory test = ServicePollerFactory.getInstance();

    ServicePoller poller = test.getMonitor(PlatformServices.Engine, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof RmiServicePollerImpl);

    poller = test.getMonitor(PlatformServices.Licensing, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof RmiServicePollerImpl);

    poller = test.getMonitor(PlatformServices.Scheduler, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof RmiServicePollerImpl);

    poller = test.getMonitor(PlatformServices.ldap, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof LdapServicePollerImpl);

    poller = test.getMonitor(PlatformServices.dwhdb, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof JdbcServicePollerImpl);

    poller = test.getMonitor(PlatformServices.repdb, null, Logger.getAnonymousLogger());
    assertTrue(poller instanceof JdbcServicePollerImpl);
  }

  @Test
  public void testGetDWhdbLoginDetails() {
    final ServicePollerFactory factory = new ServicePollerFactory();
    factory.setDefaultProperties();
    final Properties dwhdb = factory.getDWhdbLoginDetails();
    assertEquals(jdbcUrl, dwhdb.get(DBURL));
    assertEquals("test-username", dwhdb.get(DBUSERNAME));
    assertEquals("", dwhdb.get(DBPASSWORD));
    assertEquals(jdbcDriver, dwhdb.get(DBDRIVERNAME));
  }


  @Test
  public void testGetRepdbLoginDetails() {
    final ServicePollerFactory factory = new ServicePollerFactory();
    factory.setDefaultProperties();
    final Properties repdb = factory.getRepdbLoginDetails();
    assertEquals(jdbcUrl, repdb.get("ENGINE_DB_URL"));
    assertEquals("SA", repdb.get("ENGINE_DB_USERNAME"));
    assertEquals("", repdb.get("ENGINE_DB_PASSWORD"));
    assertEquals(jdbcDriver, repdb.get("ENGINE_DB_DRIVERNAME"));
  }


  @Test
  public void testDefaultSettings() throws IOException {
    final ServicePollerFactory factory = ServicePollerFactory.getInstance();
    final Map<String, String> monitorDefaults = factory.getDefaultProperties();
    assertEquals("engine", monitorDefaults.get("ENGINE_HOSTNAME"));
    assertEquals("1200", monitorDefaults.get("ENGINE_PORT"));
    assertEquals("TransferEngine", monitorDefaults.get("ENGINE_REFNAME"));

    assertEquals("scheduler", monitorDefaults.get("SCHEDULER_HOSTNAME"));
    assertEquals("1200", monitorDefaults.get("SCHEDULER_PORT"));
    assertEquals("Scheduler", monitorDefaults.get("SCHEDULER_REFNAME"));

    assertEquals("licenceservice", monitorDefaults.get("LICENSING_HOSTNAME"));
    assertEquals("1200", monitorDefaults.get("LICENSING_PORT"));
    assertEquals("LicensingCache", monitorDefaults.get("LICENSING_REFNAME"));

    assertEquals("9001", monitorDefaults.get("LDAP_PORT_NUMBER"));
    assertEquals("ldapserver", monitorDefaults.get("LDAP_SERVICE_NAME"));

    assertEquals(jdbcUrl, monitorDefaults.get("ENGINE_DB_URL"));
    assertEquals("SA", monitorDefaults.get("ENGINE_DB_USERNAME"));
    assertEquals("", monitorDefaults.get("ENGINE_DB_PASSWORD"));
    assertEquals(jdbcDriver, monitorDefaults.get("ENGINE_DB_DRIVERNAME"));

  }

  private static boolean delete(final File file) {
    if (!file.exists()) {
      return true;
    }
    if (file.isDirectory()) {
      final File[] sub = file.listFiles();
      for (File sf : sub) {
        if (!delete(sf)) {
          System.out.println("Couldn't delete directory " + sf.getPath());
          return false;
        }
      }
    }
    if (!file.delete()) {
      System.out.println("Couldn't delete file " + file.getPath());
      return false;
    }
    return true;
  }
}
