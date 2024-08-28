package com.distocraft.dc5000.common;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Level;
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

public class TPDailyRotationFileHandlerTest {

  private static TPDailyRotationFileHandler objUnderTest;
  
  private static Method getLevel;
  
  private static Method rotateMethod;
  
  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "TPDailyRotationFileHandlerTest");

  private static String log = "Log";
  
  private static String dstamp;
  
  private static LogRecord lR;

  @BeforeClass
  public static void init() {
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }
	  final DateFormat form = new SimpleDateFormat("yyyy_MM_dd");
    final Date dat = new Date(10000);
    dstamp = form.format(dat);
    System.setProperty("LOG_DIR", TMP_DIR.getPath());
    final LogManager manager = LogManager.getLogManager();
    lR = new LogRecord(Level.parse("1000"), "Message");
    lR.setLoggerName("file.Logger.log");
    lR.setMillis(10000);

    final String[] array = TMP_DIR.getPath().split("\\\\");
    String logDir = "";
    for (int i = 0; i < array.length; i++){
      logDir += array[i] + "/";
    }
    
    final File x = new File(TMP_DIR, "properties");
    x.deleteOnExit();
    PrintWriter pw;
    BufferedInputStream ins;
    try {
      pw = new PrintWriter(new FileWriter(x));
      pw.print("com.distocraft.dc5000.common.TPDailyRotationFileHandler.dirpattern=" + logDir + log + "\n");
      pw.print(TPDailyRotationFileHandler.class.getName() + ".debug" + "=" + "dep_true" + "\n");
      pw.close();
      
      ins = new BufferedInputStream(new FileInputStream(x));
      manager.readConfiguration(ins);
      ins.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    try {
      objUnderTest = new TPDailyRotationFileHandler();
      final Class tpdrfh = objUnderTest.getClass();
      getLevel = tpdrfh.getDeclaredMethod("getLevel", new Class[] { String.class });
      rotateMethod = tpdrfh.getDeclaredMethod("rotate", new Class[] { String.class, String.class, String.class });
      getLevel.setAccessible(true);
      rotateMethod.setAccessible(true);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }

  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(TMP_DIR);
  }
  
  @Test
  public void testPublish() {

    try {
      final TPDailyRotationFileHandler tRH = new TPDailyRotationFileHandler();
      tRH.publish(lR);
      tRH.flush();
      tRH.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed, Exception");
    }
    
    final File logFile = new File(TMP_DIR, log + File.separator + "Logger" +File.separator+ "file-" + dstamp + ".log");
     
    final String expected = "01.01 01:00:10 SEVERE file.Logger.log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      
      assertEquals(expected, logActual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish() failed");
    }

    assertEquals(true, logFile.exists()); // checks that file exists

    logFile.delete();   
  }

  @Test
  public void testPublish2() {

    lR.setLoggerName("etl.Logger.log");
    
    try {
      final TPDailyRotationFileHandler tRH = new TPDailyRotationFileHandler();
      tRH.publish(lR);
      tRH.flush();
      tRH.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed, Exception");
    }
    
    final File logFile = new File(TMP_DIR, log + File.separator + "Logger" +File.separator+ "engine-" + dstamp + ".log");
     
    final String expected = "01.01 01:00:10 SEVERE etl.Logger.log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      
      assertEquals(expected, logActual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish2() failed");
    }

    assertEquals(true, logFile.exists()); // checks that file exists

    logFile.delete();   
  }
  
  @Test
  public void testPublish3() {
    
    lR.setLoggerName("sql.Logger.log");

    try {
      final TPDailyRotationFileHandler tRH = new TPDailyRotationFileHandler();
      tRH.publish(lR);
      tRH.flush();
      tRH.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed, Exception");
    }
    
    final File logFile = new File(TMP_DIR, log + File.separator + "Logger" +File.separator+ "sql-" + dstamp + ".log");
     
    final String expected = "01.01 01:00:10 SEVERE sql.Logger.log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      
      assertEquals(expected, logActual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish3() failed");
    }

    assertEquals(true, logFile.exists()); // checks that file exists

    logFile.delete();   
  }
  
  @Test
  public void testPublish4() {
    
    lR.setLoggerName("sqlerror.Logger.log");

    try {
      final TPDailyRotationFileHandler tRH = new TPDailyRotationFileHandler();
      tRH.publish(lR);
      tRH.flush();
      tRH.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed, Exception");
    }
    
    final File logFile = new File(TMP_DIR, log + File.separator + "Logger" +File.separator+ "sqlerror-" + dstamp + ".log");
     
    final String expected = "01.01 01:00:10 SEVERE sqlerror.Logger.log : Message";
    try {
      final String logActual = new HelpClass().readFileToString(logFile);
      
      assertEquals(expected, logActual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testPublish4() failed");
    }

    assertEquals(true, logFile.exists()); // checks that file exists

    logFile.delete();   
  }
  
  @Test
  public void testRotate() {
    try {
      rotateMethod.invoke(objUnderTest, new Object[] { "Logger", "dailyRotation", "timestamp" });
      objUnderTest.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("testRotate() failed");
    }
    
    final File logFile3 = new File(TMP_DIR, log + File.separator + "Logger" + File.separator + "dailyRotation"
        + "-" + "timestamp" + ".log");
    
    assertEquals(true, logFile3.isFile());
    
    logFile3.delete(); 
  }
  
  @Test
  public void testGetLevel() throws Exception {
    // Testing if the getLevel() method returns correctloglevel
    assertEquals(Level.SEVERE, getLevel.invoke(objUnderTest, new Object[] { "SEVERE" }));
    assertEquals(Level.WARNING, getLevel.invoke(objUnderTest, new Object[]{"WARNING"}));
    assertEquals(Level.CONFIG, getLevel.invoke(objUnderTest, new Object[]{"CONFIG"}));
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
      // test passed
    }
  }
  
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(TPDailyRotationFileHandlerTest.class);
  }
}
