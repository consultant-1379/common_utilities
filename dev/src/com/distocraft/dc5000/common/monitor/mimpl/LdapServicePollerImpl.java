package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.monitor.DependentService;
import com.distocraft.dc5000.common.monitor.PlatformServices;
import com.distocraft.dc5000.common.monitor.ServiceMonitor;
import com.distocraft.dc5000.common.monitor.SourceState;

import java.io.StringWriter;
import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.INITIAL_CONTEXT_FACTORY;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.SECURITY_AUTHENTICATION_NONE;

/**
 * LDAP Poller implmentation, only tried to connect
 */
class LdapServicePollerImpl extends AbstractServicePoller {
  /**
   * Default poll period
   */
  protected static final int DEFAULT_LDAP_MONITOR_PERIOD = 5000;
  /**
   * LDAP URL
   */
  private final String ldapUrl;
  /**
   * Last RMI reference obtainted, used to detect restarts.
   */
  private DirContext lastReference = null;

  /**
   * @param callback    Callback object
   * @param service     Service being monitored
   * @param serviceName LDAP Host
   * @param port        LDAP Port
   */
  LdapServicePollerImpl(final DependentService callback, final PlatformServices service,
                        final String serviceName, final String port) {
    super(callback, service);
    ldapUrl = "ldap://" + serviceName + ":" + port;
  }

  /**
   * LDAP Poll implementation
   *
   * @return State of service e.g. Online
   */
  @Override
  @SuppressWarnings({"PMD.ConfusingTernary"})
  public SourceState pollService() {
    SourceState state;
    debug("Polling " + toString());
    try {
      final Hashtable<String, String> ldapConfiguration = new Hashtable<String, String>();
      ldapConfiguration.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
      ldapConfiguration.put(javax.naming.Context.PROVIDER_URL, ldapUrl);
      ldapConfiguration.put(javax.naming.Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION_NONE);

      final DirContext ctx = getDirContext(ldapConfiguration);
      debug("Polling OK " + toString());
      if (lastReference != null && !lastReference.equals(ctx)) {
        closeLastContext();
        state = SourceState.Restarted;
      } else if (ctx == null) {
        state = SourceState.Offline;
      } else {
        ctx.getEnvironment(); // overridden in tests to check error handling...
        state = SourceState.Online;
      }
      lastReference = ctx;
    } catch (NamingException e) {
      error("Error trying to connect to LDAP source", e);
      state = SourceState.Offline;
    } catch (Throwable t) {
      error("Unknown error trying to connect to LDAP source", t);
      state = SourceState.Offline;
    }
    return state;
  }

  @Override
  public boolean cancel() {
    closeLastContext();
    return super.cancel();
  }

  private void closeLastContext() {
    try {
      lastReference.close();
    } catch (Throwable t) {/**/}
  }

  /**
   * Split out to make testing easier
   *
   * @param ldapConfiguration LDAP login properties
   * @return Ldap Connection
   * @throws NamingException On errors
   */
  protected DirContext getDirContext(final Hashtable<String, String> ldapConfiguration) throws NamingException {
    return new InitialDirContext(ldapConfiguration);
  }

  /**
   * Get delay between sucessive LDAP polls
   *
   * @return Value from property LDAP_MONITOR_PERIOD or DEFAULT_LDAP_MONITOR_PERIOD if not defined.
   */
  @Override
  public int getPollPeriod() {
    return getIntStaticProperty(ServiceMonitor.LDAP_MONITOR_PERIOD, DEFAULT_LDAP_MONITOR_PERIOD);
  }

  @Override
  public String toString() {
    final StringWriter sb = new StringWriter();
    sb.append("LdapMonitor");
    sb.append("{ldapUrl='").append(ldapUrl).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
