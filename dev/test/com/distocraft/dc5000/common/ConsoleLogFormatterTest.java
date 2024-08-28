package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.JUnit4TestAdapter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for ConsoleLogFormatter class in com.distrocraft.dc5000.common. <br>
 * <br>
 * Testing that the LogFormatter formats the log correctly and how it handles in
 * when null value is given.
 * 
 * @author EJAAVAH
 * 
 */
public class ConsoleLogFormatterTest {

  private static ConsoleLogFormatter objUnderTest;

  @BeforeClass
  public static void setUpBeforeClass() {
    try {
      objUnderTest = new ConsoleLogFormatter();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void tearDownAfterClass() {
    objUnderTest = null;
  }

  /**
   * Testing formatting with general input.
   */
  @Test
  public void testFormatGeneral() {
    // Initializing logrecord with different values which are later formatted
    final LogRecord record = new LogRecord(Level.SEVERE, "testGeneral");
    record.setLoggerName("logrecordname");
    record.setThreadID(10);
    record.setMillis(999);

    // Test for the format() method using general input to see if the output
    // matches expected result
    final String expected = "01.01 01:00:00 10 SEVERE logrecordname : testGeneral\n";
    assertEquals(expected, objUnderTest.format(record));
  }

  /**
   * Testing if the throwable clause is formatted correctly.
   */
  @Test
  public void testFormatThrowable() {
    // Initializing logrecord for testing
    final LogRecord record = new LogRecord(Level.FINE, "testThrowable");
    record.setLoggerName("logrecordname");
    record.setThreadID(15);
    record.setMillis(999);
    final Throwable thrown = new Throwable("throwableMessage"); //NOPMD
    record.setThrown(thrown);

    // Test for the format() method with throwable exception to see if the
    // formatter returns the stacktrace
    final String expected = "01.01 01:00:00 15 FINE logrecordname : testThrowable\n"
        + "   java.lang.Throwable: throwableMessage\n";
    final String result = objUnderTest.format(record);
    assertEquals(true, result.startsWith(expected));
  }

  /**
   * Testing formatting using null value - Should throw exception.
   */
  @Test
  public void testFormatWithNullLogRecord() {

    // Test for the format() method with null logrecord
    try {
      objUnderTest.format(null);
      fail("Test failed - NullPointerException expected as the LogRecord is null");
    } catch (NullPointerException npe) {
      // Exception catched, test has passed
    } catch (Exception e) {
      fail("Unexpected error occured - NullPointerException was expected" + e);
    }
  }

  // Making the test work with ant 1.6.5 and JUnit 4.x
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(ConsoleLogFormatterTest.class);
  }
}
