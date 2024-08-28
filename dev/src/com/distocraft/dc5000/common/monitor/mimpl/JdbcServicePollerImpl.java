package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.monitor.DependentService;
import com.distocraft.dc5000.common.monitor.MonitoringException;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.ServiceMonitor;
import com.distocraft.dc5000.common.monitor.SourceState;

import static com.ericsson.eniq.repository.ETLCServerProperties.DBURL;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBDRIVERNAME;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBUSERNAME;
import static com.ericsson.eniq.repository.ETLCServerProperties.DBPASSWORD;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


/**
 * JDBC Poller implmentation, tried to open a connection to a database.
 * In the case of Sybase it also gets the start time (to detect restarts)
 */
class JdbcServicePollerImpl extends AbstractServicePoller {

  /**
   * Default poll period
   */
  protected static final int DEFAULT_JDBC_MONITOR_PERIOD = 5000;

  /**
   * JDBC URL
   */
  private final String jdbcUrl;
  /**
   * JDBC Username
   */
  private final String jdbcUsername;
  /**
   * JDBC Password
   */
  private final String jdbcPassword;
  /**
   * JDBC Driver class
   */
  private final String jdbcDriver;

  /**
   * Time the database was started (sourced from database)
   */
  private String lastStartTime = null;

  /**
   * List of SQL Error codes for offline JDBC sources
   */
  private static final List<String> CONNECTION_ERRORS = Arrays.asList(
    /*Sybase Offline*/"JZ006",
    /*HSQL Db Not created(testing purposes)*/"S1000"
  );

  /**
   * Database start time, used to detect restarts.
   */
  private final String getStartTime;

  /**
   * Defalt constructoor
   *
   * @param listener  Callback object
   * @param service   Service being monitored
   * @param connProps Connection properties (URL, USERNAME, PASSWORD, DRIVER)
   */
  JdbcServicePollerImpl(final DependentService listener, final PlatformServices service, final Properties connProps) {
    super(listener, service);
    this.jdbcUrl = connProps.getProperty(DBURL);
    this.jdbcDriver = connProps.getProperty(DBDRIVERNAME);
    this.jdbcUsername = connProps.getProperty(DBUSERNAME);
    this.jdbcPassword = connProps.getProperty(DBPASSWORD);
    this.getStartTime = System.getProperty("db_starttime_query", "SELECT PROPERTY ('StartTime');");
  }

  /**
   * JDBC Poll implementation
   *
   * @return State of service e.g. Online
   */
  @Override @SuppressWarnings({"PMD.CloseResource", "PMD.ConfusingTernary"})
  public SourceState pollService() {
    Connection conn = null;
    SourceState state;
    debug("Polling " + toString());
    try {
      Class.forName(this.jdbcDriver);
      conn = DriverManager.getConnection(this.jdbcUrl, this.jdbcUsername, this.jdbcPassword);
      final String startTime = getDbStartTime(conn);
      if (lastStartTime != null && !lastStartTime.equals(startTime)) {
        state = SourceState.Restarted;
      } else {
        state = SourceState.Online;
      }
      lastStartTime = startTime;
      debug("Polling OK " + toString());
    } catch (ClassNotFoundException e) {
      debug("No Driver Class found: " + this.jdbcDriver);
      throw new MonitoringException("Driver Class Not Found '"+this.jdbcDriver+"'", e);
    } catch (SQLException e) {
      if (CONNECTION_ERRORS.contains(e.getSQLState())) {
        state = SourceState.Offline;
      } else {
        error("Error trying to connect to JDBC source", e);
        throw new MonitoringException("Could not connect to JDBC source", e);
      }
    } catch (Throwable t) {
      error("Unknown error trying to connect to JDBC source", t);
      throw new MonitoringException("Could not connect to JDBC source", t);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {/**/}
      }
    }
    return state;
  }

  /**
   * Get delay between sucessive JDBC polls
   *
   * @return Value from property JDBC_MONITOR_PERIOD or DEFAULT_JDBC_MONITOR_PERIOD if not defined.
   */
  @Override
  public int getPollPeriod() {
    return getIntStaticProperty(ServiceMonitor.JDBC_MONITOR_PERIOD, DEFAULT_JDBC_MONITOR_PERIOD);
  }

  /**
   * Get the database start time.
   * (Tests override this method to get start times for junit databases)
   * @param conn Db connection
   * @return Start time (timestamp)
   * @throws SQLException On errors
   */
  protected String getDbStartTime(final Connection conn) throws SQLException {
    final Statement stmt = conn.createStatement();
    try{
      return getDbProperty(stmt, getStartTime);
    } finally {
      try {
        stmt.close();
      } catch (SQLException e) {/**/}
    }
  }

  private String getDbProperty(final Statement stmt, final String query) throws SQLException {
    final ResultSet rs = stmt.executeQuery(query);
    try {
      if (rs.next()) {
        return rs.getString(1);
      } else {
        return null;
      }
    } finally {
      rs.close();
    }
  }

  @Override
  public String toString() {
    final StringWriter sb = new StringWriter();
    sb.append("JdbcMonitor");
    sb.append("{jdbcUsername='").append(jdbcUsername).append('\'');
    sb.append(", jdbcUrl='").append(jdbcUrl).append('\'');
    sb.append(", jdbcDriver='").append(jdbcDriver).append('\'');
    sb.append('}');
    return sb.toString();
  }

}
