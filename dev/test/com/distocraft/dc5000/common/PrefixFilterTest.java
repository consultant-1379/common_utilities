package com.distocraft.dc5000.common;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * 
 * @author ejarsok
 * 
 */

public class PrefixFilterTest {

  private Field field;

  private PrefixFilter instance = new PrefixFilter();

  private PrefixFilter instance2 = new PrefixFilter("PREFIX");

  @Test
  public void testPrefixFilter() {
    Class secretClass = instance.getClass();

    try {
      field = secretClass.getDeclaredField("prefix");
      field.setAccessible(true);

      assertEquals("", field.get(instance));
      assertEquals("PREFIX", field.get(instance2));

    } catch (Exception e) {
      e.printStackTrace();
      fail("testPrefixFilter() failed, Exception");
    }
  }

  @Test
  public void testPrefixFilter2() {
    LogRecord lR = new LogRecord(Level.parse("100"), "message");
    lR.setLoggerName("PREFIX_log");
    assertEquals(false, instance.isLoggable(null));
    assertEquals(true, instance2.isLoggable(lR));
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(PrefixFilterTest.class);
  }
}
