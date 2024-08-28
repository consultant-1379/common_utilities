package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for LogFormatter class in com.distrocraft.dc5000.common. <br>
 * <br>
 * Testing that the LogFormatter formats the log correctly and how it handles in
 * when null value is given.
 * 
 * @author EJAAVAH
 * 
 */
public class LogFormatterTest {

  private static LogFormatter objUnderTest;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    try {
      objUnderTest = new LogFormatter();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    objUnderTest = null;
  }

  /**
   * Testing formatting with general input.
   */
  @Test
  public void testFormatGeneral() throws Exception {
    // Initializing logrecord for testing
    LogRecord record = new LogRecord(Level.SEVERE, "testGeneral");
    record.setLoggerName("logrecordname");
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");

    // Test for the format() method using general input to see if the output
    // matches expected result
    String expected = sdf.format(new Date()) + " SEVERE logrecordname testGeneral\n";
    assertEquals(expected, objUnderTest.format(record));
  }

  /**
   * Testing if the throwable clause is formatted correctly.
   */
  @Test
  public void testFormatThrowable() throws Exception {
    // Initializing logrecord for testing
    LogRecord record = new LogRecord(Level.FINE, "testThrowable");
    record.setLoggerName("logrecordname");
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");
    Throwable thrown = new Throwable("throwableMessage");
    record.setThrown(thrown);

    // Test for the format() method with throwable exception to see if the
    // formatter returns the stacktrace (only the start of the stacktrace is
    // checked)
    String expected = sdf.format(new Date()) + " FINE logrecordname testThrowable\n"
        + "java.lang.Throwable: throwableMessage\n";
    String result = objUnderTest.format(record);
    assertEquals(true, result.startsWith(expected));
  }

  /**
   * Testing formatting using null value - Should throw exception.
   */
  @Test
  public void testFormatWithNullLogRecord() throws Exception {
    // Initializing logrecord for testing
    LogRecord record = null;

    // Test for the format() method with null logrecord
    try {
      objUnderTest.format(record);
      fail("Test failed - NullPointerException expected as the LogRecord is null");
    } catch (NullPointerException npe) {
      // Exception catched, test has passed
    } catch (Exception e) {
      fail("Unexpected error occured - NullPointerException was expected" + e);
    }
  }

  // Making the test work with ant 1.6.5 and JUnit 4.x
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(LogFormatterTest.class);
  }
}
