package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.ServicenamesHelper;
import com.distocraft.dc5000.common.monitor.DependentService;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.ServicePoller;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.common.CommonUtils;
import com.ericsson.eniq.common.INIGet;
import com.ericsson.eniq.repository.ETLCServerProperties;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBURL;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBDRIVERNAME;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBUSERNAME;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBPASSWORD;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

/**
 * Factory class to get the poller for a service
 */
public class ServicePollerFactory {

  /**
   * singleton instance
   */
  private static ServicePollerFactory __instance__ = null;

  /**
   * Default properties
   */
  private final Map<String, String> defaultSettings;

  /**
   * test properties.
   * Only used for JUnits.
   */
  private static Properties testProperties = null;

  /**
   * niq.ini file
   */
  private final File iniFile;

  private static final String NIQ_INI_NAME = "niq.ini";

  ServicePollerFactory() {
    defaultSettings = new HashMap<String, String>();
    if (System.getProperty("CONF_DIR") == null) {
      System.setProperty("CONF_DIR", "/eniq/sw/conf");
    }
    final String confDir = CommonUtils.expandPathWithVariable("${CONF_DIR}");
    iniFile = new File(confDir, NIQ_INI_NAME);
  }

  /**
   * Get the ServicePollerFactory instance
   *
   * @return Singletone ServicePollerFactory instance
   * @throws MonitoringException Erorrs setting default properties
   */
  public static ServicePollerFactory getInstance() throws MonitoringException {
    if (__instance__ == null) {
      __instance__ = new ServicePollerFactory();
      __instance__.setDefaultProperties();
    }
    return __instance__;
  }

  /**
   * Get the poller class for a service
   *
   * @param service   The service to poll
   * @param dependent The callback to inform of service state changes
   * @param logger A Logger instance to use if needed
   * @return Poller instance
   * @throws MonitoringException Errors setting up poller
   */
  public ServicePoller getMonitor(final PlatformServices service,
                                  final DependentService dependent, final Logger logger) throws MonitoringException {
    switch (service) {
      case Engine:
        return getRmiPoller(service, dependent, "ENGINE_REFNAME", "ENGINE_PORT", "ENGINE_HOSTNAME", logger);
      case Scheduler:
        return getRmiPoller(service, dependent, "SCHEDULER_REFNAME", "SCHEDULER_PORT", "SCHEDULER_HOSTNAME", logger);
      case Licensing:
        return getRmiPoller(service, dependent, "LICENSING_REFNAME", "LICENSING_PORT", "LICENSING_HOSTNAME", logger);
      case repdb:
        final Properties props = getRepdbLoginDetails();
        return getJdbcPoller(service, dependent, props);
      case dwhdb:
        final Properties dwhProps = getDWhdbLoginDetails();
        return getJdbcPoller(service, dependent, dwhProps);
      case ldap:
        return getLdapPoller(service, dependent, logger);
      default:
        throw new MonitoringException("Not Yet Implemented : " + service);
    }
  }

  private ServicePoller getRmiPoller(final PlatformServices service, final DependentService dependent,
                                     final String nameProp, final String portProp, final String hostProp,
                                     final Logger logger) {
    final String rmiName = defaultSettings.get(nameProp);
    final String rmiPort = defaultSettings.get(portProp);
    final String rmiServiceHost = defaultSettings.get(hostProp);
    String networkName;
    try {
      networkName = ServicenamesHelper.getServiceHost(rmiServiceHost, null);
      if(networkName == null){
        logger.warning("No service_names or hosts entry for '"+rmiServiceHost+"', using localhost as default.");
        networkName = "localhost";
      }
    } catch (IOException e) {
      throw new MonitoringException(e);
    }
    return new RmiServicePollerImpl(dependent, service, rmiName, rmiPort, networkName);
  }

  private ServicePoller getJdbcPoller(final PlatformServices service, final DependentService dependent,
                                      final Properties connProperties) {
    return new JdbcServicePollerImpl(dependent, service, connProperties);
  }

  private ServicePoller getLdapPoller(final PlatformServices service, final DependentService dependent,
                                      final Logger logger) {
    final String servicePort = defaultSettings.get("LDAP_PORT_NUMBER");
    final String serviceName = defaultSettings.get("LDAP_SERVICE_NAME");
    String networkName;
    try {
      networkName = ServicenamesHelper.getServiceHost(serviceName, null);
      if(networkName == null){
        logger.warning("No service_names or hosts entry for '"+serviceName+"', using localhost as default.");
        networkName = "localhost";
      }
    } catch (IOException e) {
      throw new MonitoringException(e);
    }
    return new LdapServicePollerImpl(dependent, service, networkName, servicePort);
  }


  final Properties getDWhdbLoginDetails() throws MonitoringException {
    final Meta_databases dwh = getLoginDetails("dwh", "USER");
    final Properties props = new Properties();
    props.put(DBURL, dwh.getConnection_string());
    props.put(DBDRIVERNAME, dwh.getDriver_name());
    props.put(DBUSERNAME, dwh.getUsername());
    props.put(DBPASSWORD, dwh.getPassword());
    return props;
  }

  final Properties getRepdbLoginDetails() {
    final Properties props = new Properties();
    props.put(DBURL, defaultSettings.get(DBURL));
    props.put(DBDRIVERNAME, defaultSettings.get(DBDRIVERNAME));
    props.put(DBUSERNAME, defaultSettings.get(DBUSERNAME));
    props.put(DBPASSWORD, defaultSettings.get(DBPASSWORD));
    return props;
  }

  private Meta_databases getLoginDetails(final String connName, final String typeName) throws MonitoringException {
    final Properties repdb = getRepdbLoginDetails();
    final String etlrepUrl = repdb.getProperty(DBURL);
    final String etlrepDriver = repdb.getProperty(DBDRIVERNAME);
    final String etlrepUsername = repdb.getProperty(DBUSERNAME);
    final String etlrepPassword = repdb.getProperty(DBPASSWORD);
    RockFactory etlrep = null;
    try {
      etlrep = new RockFactory(etlrepUrl, etlrepUsername, etlrepPassword, etlrepDriver,
        "ConnMonitorSU", true);
      final Meta_databases where = new Meta_databases(etlrep);
      where.setConnection_name(connName);
      where.setType_name(typeName);
      return new Meta_databases(etlrep, where);
    } catch (SQLException e) {
      throw new MonitoringException(e);
    } catch (RockException e) {
      throw new MonitoringException(e);
    } finally {
      try {
        etlrep.getConnection().close();
      } catch (SQLException e) {/**/}
    }
  }

  final void setDefaultProperties() throws MonitoringException {
    final Properties etlcProperties = initEtlcProperties();
    getSetDefault("ENGINE_HOSTNAME", "engine", etlcProperties);
    getSetDefault("ENGINE_PORT", "1200", etlcProperties);
    getSetDefault("ENGINE_REFNAME", "TransferEngine", etlcProperties);

    getSetDefault("SCHEDULER_HOSTNAME", "scheduler", etlcProperties);
    getSetDefault("SCHEDULER_PORT", "1200", etlcProperties);
    getSetDefault("SCHEDULER_REFNAME", "Scheduler", etlcProperties);

    getSetDefault("LICENSING_HOSTNAME", "licenceservice", etlcProperties);
    getSetDefault("LICENSING_PORT", "1200", etlcProperties);
    getSetDefault("LICENSING_REFNAME", "LicensingCache", etlcProperties);

    final INIGet iniGet = new INIGet();
    iniGet.setFile(iniFile.getPath());
    iniGet.setSection("LDAP");
    iniGet.setParameter("LDAP_PORT_NUMBER");

    String ldapPort = iniGet.getParameterValue();
    if (ldapPort == null || ldapPort.length() == 0) {
      if(etlcProperties.containsKey("LDAP_PORT_NUMBER")){
        ldapPort = etlcProperties.get("LDAP_PORT_NUMBER").toString();
      } else {
        ldapPort = "9001";
      }
    }
    defaultSettings.put("LDAP_PORT_NUMBER", ldapPort);

    iniGet.setParameter("LDAP_SERVICE_NAME");
    String ldapService = iniGet.getParameterValue();
    if (ldapService == null || ldapService.length() == 0) {
      if(etlcProperties.containsKey("LDAP_SERVICE_NAME")){
        ldapService = etlcProperties.get("LDAP_SERVICE_NAME").toString();
      } else {
        ldapService = "ldapserver";
      }
    }
    defaultSettings.put("LDAP_SERVICE_NAME", ldapService);

    defaultSettings.put(DBURL, etlcProperties.getProperty(DBURL));
    defaultSettings.put(DBDRIVERNAME, etlcProperties.getProperty(DBDRIVERNAME));
    defaultSettings.put(DBUSERNAME, etlcProperties.getProperty(DBUSERNAME));
    defaultSettings.put(DBPASSWORD, etlcProperties.getProperty(DBPASSWORD));
  }

  private void getSetDefault(final String pName, final String _default, final Properties etlcProperties) {
    final String value = etlcProperties.getProperty(pName, _default);
    defaultSettings.put(pName, value);
  }

  final Map<String, String> getDefaultProperties(){
    return Collections.unmodifiableMap(defaultSettings);
  }

  private Properties initEtlcProperties() throws MonitoringException {
    if (testProperties == null) {
      final String conf_dir = System.getProperty("CONF_DIR", "/eniq/sw/conf");
      final File etlc = new File(conf_dir, "ETLCServer.properties");
      try {
        return new ETLCServerProperties(etlc.getPath());
      } catch (IOException e) {
        throw new MonitoringException(e);
      }
    } else {
      return testProperties;
    }
  }

  /**
   * Set test Static Properties
   *
   * @param properties properties to use
   */
  public static void setTestProperties(final Properties properties) {
    testProperties = properties;
    __instance__ = null;// reset
  }
}
