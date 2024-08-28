package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for DailyRotationFileHandler class in com.distrocraft.dc5000.common.
 * <br>
 * <br>
 * Testing if new file is created with current date timestamp on it and logdata
 * is added to that file using.
 *
 * @author EJAAVAH
 *
 */
public class DailyRotationFileHandlerTest {

  private static DailyRotationFileHandler objUnderTest;

  private static LogManager logManager;

  private static LogRecord logRecord;

  private static Method getLevel;

  private static File logPropertiesFile;

  private static File publishedLog;
  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "DailyRotationFileHandlerTest");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }

    logRecord = new LogRecord(Level.SEVERE, "testMessage");
    System.setProperty("LOG_DIR", TMP_DIR.getPath() + File.separator);
    logManager = LogManager.getLogManager();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");

    try {
      logPropertiesFile = new File(TMP_DIR, "logProperties");
      publishedLog = new File(TMP_DIR, "test" + sdf.format(new Date())+ ".log");

      // constructor creates the name of the logfile using this propertyfile
      PrintWriter pw = new PrintWriter(new FileWriter(logPropertiesFile));
      pw.print("com.distocraft.dc5000.common.DailyRotationFileHandler.pattern=$logtest");
      pw.close();

      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(logPropertiesFile));
      logManager.readConfiguration(bis);
      bis.close();

      objUnderTest = new DailyRotationFileHandler();
      Class drfh = objUnderTest.getClass();
      getLevel = drfh.getDeclaredMethod("getLevel", new Class[] { String.class });
    } catch (Exception e) {
      e.printStackTrace();
    }
    getLevel.setAccessible(true);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    objUnderTest = null;
    // Stream is left open for testlogfile and in order to remove the file,
    // garbage collector is called
    System.gc();
    DirectoryHelper.delete(TMP_DIR);
  }

  /**
   * Testing if the constructor creates the necessary files - Rotate() method is
   * called on initializing which creates new logfile with todays timestamp and
   * opens an outputstream for it, logdata is then written to this file via
   * publish() method.
   */
  @Test
  public void testConstructor() throws Exception {
    assertEquals(true, publishedLog.exists());
  }

  /**
   * Test if the publish() method writes the logrecord to the logfile.
   */
  @Test
  public void testPublish() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");
    objUnderTest.publish(logRecord);
    String expected = sdf.format(new Date(logRecord.getMillis())) + " SEVERE null : testMessage";
    String actual = new HelpClass().readFileToString(publishedLog);
    assertEquals(expected, actual);
  }

  /**
   * Testing if the getLevel() method returns correct loglevel.
   */
  @Test
  public void testGetLevel() throws Exception {
    assertEquals(Level.SEVERE, getLevel.invoke(objUnderTest, new Object[] { "SEVERE" }));
    assertEquals(Level.WARNING, getLevel.invoke(objUnderTest, new Object[] { "WARNING" }));
    assertEquals(Level.CONFIG, getLevel.invoke(objUnderTest, new Object[] { "CONFIG" }));
    assertEquals(Level.INFO, getLevel.invoke(objUnderTest, new Object[] { "INFO" }));
    assertEquals(Level.FINE, getLevel.invoke(objUnderTest, new Object[] { "FINE" }));
    assertEquals(Level.FINER, getLevel.invoke(objUnderTest, new Object[] { "FINER" }));
    assertEquals(Level.FINEST, getLevel.invoke(objUnderTest, new Object[] { "FINEST" }));
    assertEquals(Level.OFF, getLevel.invoke(objUnderTest, new Object[] { "OFF" }));
    assertEquals(Level.ALL, getLevel.invoke(objUnderTest, new Object[] { "RandomLevel" }));
    try {
      getLevel.invoke(objUnderTest, new Object[] { null });
      fail("Test failed - Exception expected as null object was given");
    } catch (Exception e) {
      // test passed - exception caught
    }
  }

  // Making the test work with ant 1.6.5 and JUnit 4.x
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(DailyRotationFileHandlerTest.class);
  }
}
