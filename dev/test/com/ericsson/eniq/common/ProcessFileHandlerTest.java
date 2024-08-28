package com.ericsson.eniq.common;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import com.distocraft.dc5000.common.HelpClass;

/**
 * 
 * @author ejarsok
 *
 */

public class ProcessFileHandlerTest {

  private static String timestamp;
  
  private static String dstamp;
  
  private static Method rotateMethod;
  
  private static ProcessFileHandler instance;
  
  private static LogRecord lR;

  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "ProcessFileHandlerTest");
  
  @BeforeClass
  public static void init() {
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }
    final DateFormat form = new SimpleDateFormat("yyyy_MM_dd");
    final Date dat = new Date(10000);
    rotateMethod = null;
    System.setProperty("LOG_DIR", TMP_DIR.getPath());
    System.setProperty("pname", "propertyName");
    System.setProperty("ProcessLogger.debug", "foobar");
    timestamp = "timestamp";
    dstamp = form.format(dat);
    lR = new LogRecord(Level.parse("1000"), "Message");
    lR.setMillis(10000);
    
    try {
      instance = new ProcessFileHandler();
      final Class secretClass = instance.getClass();

      rotateMethod = secretClass.getDeclaredMethod("rotate", new Class[] { String.class});
      rotateMethod.setAccessible(true);
   
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
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
    
    final String pname = System.getProperty("pname");
    
    final File logFile2 = new File(TMP_DIR, File.separator + pname + File.separator + pname + "-" + dstamp + ".log");
      
    final String expected = "01.01 01:00:10 10 SEVERE null : Message";
    try {
      final String log2Actual = new HelpClass().readFileToString(logFile2);
      
      assertEquals(expected, log2Actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish() failed");
    } 
    
    assertEquals(true, logFile2.isFile());

    logFile2.delete();   
  }
  
  @Test
  public void testRotate() {
    try {
      rotateMethod.invoke(instance, new Object[] {timestamp});
      instance.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("testRotate()");
    }
    
    final String pname = System.getProperty("pname");
    final File logFile = new File(TMP_DIR, File.separator + pname + File.separator + pname + "-" + timestamp + ".log");
    
    assertEquals(true, logFile.isFile());
    
    logFile.delete();
  }
  
  @AfterClass
  public static void clean() {
    final String pname = System.getProperty("pname");
    final File logDir = new File(TMP_DIR, pname);
    
    logDir.delete();
  }
  
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(ProcessFileHandlerTest.class);
  }
}
