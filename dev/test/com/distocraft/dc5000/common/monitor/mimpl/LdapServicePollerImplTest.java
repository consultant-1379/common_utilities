package com.distocraft.dc5000.common.monitor.mimpl;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.common.monitor.PlatformServices;

import com.distocraft.dc5000.common.monitor.ServiceMonitor;
import com.distocraft.dc5000.common.monitor.ServicePoller;
import com.distocraft.dc5000.common.monitor.SourceState;
import com.ericsson.eniq.common.testutilities.PollerBaseTestCase;

import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LdapServicePollerImplTest extends PollerBaseTestCase {


  private StubbedDirContext ldapContext = null;
  private ServicePoller testInstance = null;

  @Before
  public void before() throws NamingException {
    ldapContext = new StubbedDirContext();
    testInstance = new LdapPoller();
  }

  @Test
  public void testExceptionsHandled() {
    ldapContext.throwException(new NamingException("some-error"));
    SourceState status = testInstance.pollService();
    Assert.assertEquals(SourceState.Offline, status);


    ldapContext.throwException(new NullPointerException("np-e"));
    status = testInstance.pollService();
    Assert.assertEquals(SourceState.Offline, status);
  }

  @Test
  public void testGetPollPeriod() {
    final int pollPeriod = 123456;
    final Properties sp = new Properties();
    sp.setProperty(ServiceMonitor.LDAP_MONITOR_PERIOD, Integer.toString(pollPeriod));
    StaticProperties.giveProperties(sp);
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.ldap, null, Logger.getAnonymousLogger());
    final int pp = poller.getPollPeriod();
    assertEquals("LDAP Poll Period not read correctly from Static Properties", pollPeriod, pp);
  }

  @Test
  public void testGetPollPeriod_Default() {
    final ServicePoller poller = ServicePollerFactory.getInstance().getMonitor(PlatformServices.ldap, null, Logger.getAnonymousLogger());
    final int pp = poller.getPollPeriod();
    assertEquals("LDAP Poll Period not read correctly from Static Properties",
      LdapServicePollerImpl.DEFAULT_LDAP_MONITOR_PERIOD, pp);
  }

  @Test
  public void testCancel(){
    testInstance.pollService();
    ldapContext.closed = false;
    testInstance.toTask().cancel();
    Assert.assertTrue(ldapContext.closed);

  }


  @Test
  public void testRestarted() throws NamingException {
    SourceState status = testInstance.pollService();
    Assert.assertEquals(SourceState.Online, status);

    ldapContext = null;

    ldapContext = new StubbedDirContext();
    status = testInstance.pollService();
    Assert.assertEquals(SourceState.Restarted, status);
  }


  @Test
  public void testOnline() throws NamingException {
    ldapContext = new StubbedDirContext();
    final SourceState status = testInstance.pollService();
    Assert.assertEquals(SourceState.Online, status);
  }

  @Test
  public void testPollOffline() {
    ldapContext = null;
    final SourceState status = testInstance.pollService();
    Assert.assertEquals(SourceState.Offline, status);
  }

  private class StubbedDirContext extends InitialDirContext {
    public boolean closed;
    private Throwable error = null;

    public StubbedDirContext() throws NamingException {
      closed = false;
    }

    public void throwException(final Throwable error){
      this.error = error;
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
      if(error == null){
        return null;
      } else {
        if(error instanceof NamingException) {
          throw (NamingException)error;
        } else {
          throw new RuntimeException(error);
        }
      }
    }

    @Override
    public void close() throws NamingException {
      closed = true;
    }
  }

  private class LdapPoller extends LdapServicePollerImpl {
    LdapPoller() {
      super(null, PlatformServices.ldap, null, null);
    }

    @Override
    protected DirContext getDirContext(Hashtable<String, String> ldapConfiguration) throws NamingException {
      return ldapContext;
    }
  }
}
