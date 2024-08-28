package com.ericsson.eniq.common;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.distocraft.dc5000.common.HelpClass;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

/**
 * 
 * @author ejarsok
 * 
 */

public class EngineLoggerTest {

  private static Method rotateMethod;

  private static EngineLogger instance;

  private static LogRecord lR;

  private static String dstamp;

  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "EngineLoggerTest");
  
  @BeforeClass
  public static void init() {
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }
    final DateFormat form = new SimpleDateFormat("yyyy_MM_dd");
    final Date dat = new Date(10000);
    dstamp = form.format(dat);
    System.setProperty("LOG_DIR", TMP_DIR.getPath());
    System.setProperty("EngineLogger.debug", "foobar");
    lR = new LogRecord(Level.parse("1000"), "Message");
    lR.setLoggerName("file.Logger.Log");
    lR.setMillis(10000);
   

    try {
      instance = new EngineLogger();
      final Class secretClass = instance.getClass();

      rotateMethod = secretClass.getDeclaredMethod("rotate", new Class[] { String.class, String.class, String.class });
      rotateMethod.setAccessible(true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("init() failed");
    }
  }

  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(TMP_DIR);
  }

  /**
   * Test that log files exists
   * 
   */

  @Test
  public void testPublish() {

    instance.publish(lR);
    instance.flush();
    instance.close();

    final File logFile = new File(TMP_DIR, "engine" + File.separator + "error" + "-" + dstamp + ".log");
    final File logFile2 = new File(TMP_DIR, "engine" + File.separator + "Logger" + File.separator + "file"
        + "-" + dstamp + ".log");
    logFile.canExecute();
    logFile.canRead();
    logFile.canWrite();
    
    logFile2.canExecute();
    logFile2.canRead();
    logFile2.canWrite();
    
    final String expected = "01.01 01:00:10 10 SEVERE file.Logger.Log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      final String log2Actual = new HelpClass().readFileToString(logFile2);
      
      assertEquals(expected, logActual);
      assertEquals(expected, log2Actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish() failed");
    }
    
    assertEquals(true, logFile.isFile());
    assertEquals(true, logFile2.isFile());
    
    logFile.delete();
    logFile2.delete();  
  }
  
  //@Test
  public void testPublish2() {

    lR.setLoggerName("etl.Logger.Log");
    
    instance.publish(lR);
    instance.flush();
    instance.close();

    final File logFile = new File(TMP_DIR, "engine" + File.separator + "error" + "-" + dstamp + ".log");
    final File logFile2 = new File(TMP_DIR, "engine" + File.separator + "Logger" + File.separator + "engine"
        + "-" + dstamp + ".log");
    logFile.canExecute();
    logFile.canRead();
    logFile.canWrite();
    
    logFile2.canExecute();
    logFile2.canRead();
    logFile2.canWrite();
    final String expected = "01.01 01:00:10 10 SEVERE etl.Logger.Log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      final String log2Actual = new HelpClass().readFileToString(logFile2);
      
      assertEquals(expected, logActual);
      assertEquals(expected, log2Actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish2() failed");
    }
    
    assertEquals(true, logFile.isFile());
    assertEquals(true, logFile2.isFile());
    
    //logFile.delete();
    //logFile2.delete();  
  }
  
  //@Test
  public void testPublish3() {

    lR.setLoggerName("sql.Logger.Log");
    
    instance.publish(lR);
    instance.flush();
    instance.close();

    final File logFile = new File(TMP_DIR, "engine" + File.separator + "error" + "-" + dstamp + ".log");
    final File logFile2 = new File(TMP_DIR, "engine" + File.separator + "Logger" + File.separator + "sql"
        + "-" + dstamp + ".log");
    logFile.canExecute();
    logFile.canRead();
    logFile.canWrite();
    
    logFile2.canExecute();
    logFile2.canRead();
    logFile2.canWrite();
    final String expected = "01.01 01:00:10 10 SEVERE sql.Logger.Log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      final String log2Actual = new HelpClass().readFileToString(logFile2);
      
      assertEquals(expected, logActual);
      assertEquals(expected, log2Actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish3() failed");
    }
    
    assertEquals(true, logFile.isFile());
    assertEquals(true, logFile2.isFile());
    
  //  logFile.delete();
 //   logFile2.delete();  
  }
  
 // @Test
  public void testPublish4() {

    lR.setLoggerName("sqlerror.Logger.Log");
    
    instance.publish(lR);
    instance.flush();
    instance.close();

    final File logFile = new File(TMP_DIR, "engine" + File.separator + "error" + "-" + dstamp + ".log");
    final File logFile2 = new File(TMP_DIR, "engine" + File.separator + "Logger" + File.separator + "sqlerror"
        + "-" + dstamp + ".log");
    logFile.canExecute();
    logFile.canRead();
    logFile.canWrite();
    
    logFile2.canExecute();
    logFile2.canRead();
    logFile2.canWrite();
    final String expected = "01.01 01:00:10 10 SEVERE sqlerror.Logger.Log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      final String log2Actual = new HelpClass().readFileToString(logFile2);
      
      assertEquals(expected, logActual);
      assertEquals(expected, log2Actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish4() failed");
    }
    
    assertEquals(true, logFile.isFile());
    assertEquals(true, logFile2.isFile());
    
    //logFile.delete();
   // logFile2.delete();  
  }
  
  @Test
  public void testRotate() {
    try {
      rotateMethod.invoke(instance, new Object[] { "Logger", "engine", "timestamp" });
      instance.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("testRotate() failed");
    }
    
    final File logFile3 = new File(TMP_DIR, "engine" + File.separator + "Logger" + File.separator + "engine"
        + "-" + "timestamp" + ".log");
    
    assertEquals(true, logFile3.isFile());
    
    logFile3.delete();
  }

  @AfterClass
  public static void clean() {
    final File logDir = new File(TMP_DIR, "engine");
    final File logDir2 = new File(logDir, "Logger");
    logDir.canExecute();
    logDir.canRead();
    logDir.canWrite();
    
    logDir2.canExecute();
    logDir2.canRead();
    logDir2.canWrite();
    logDir2.delete();
    logDir.delete();
  }
  
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(EngineLoggerTest.class);
  }
}
