package com.distocraft.dc5000.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * @author ejarsok
 * 
 */

public class LoaderLogTest {

  private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"), "LoaderLogTest");

  private static String dateID = "dateID_value";
  
  private static LoaderLog lL;
  
  private static File adapter;
    
  private Collection<Map<String, Object>> collection;
  
  private static File file;

  private static long startTime = 1312553742434L;
  private static long stopTime = 1312553740300L;
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
  private static final String startDate = sdf.format(new Date(startTime));
  private static final String stoptDate = sdf.format(new Date(stopTime));

  @BeforeClass
  public static void init() {
  	final Logger log = Logger.getLogger("etlengine.common.SessionLogger.LOADER");
  	log.setLevel(Level.ALL);
  	
    final Properties prop = new Properties();
    prop.setProperty("SessionHandling.log.LOADER.inputTableDir", tmpDir.getPath());

    file = new File(tmpDir, "loaderLogFile");
    file.deleteOnExit();
    
    try {
      StaticProperties.giveProperties(prop);
      lL  = new LoaderLog();
      
    } catch (Exception e1) {
      e1.printStackTrace();
      fail("testLoaderLog failed");
    }
  }

  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(tmpDir);
  }
  
  @Before
  public void setup(){
      //Create a new HashMap...
	  collection = new ArrayList<Map<String, Object>>();
      
      for(int i = 0; i < 10000; i++){
	      Map<String, String> sessionMap = new HashMap<String, String>();
	      sessionMap.put("LOADERSET_ID", "100");
    	  sessionMap.put("SESSION_ID", "SESSION_ID"+i);
    	  sessionMap.put("BATCH_ID", "BATCH_ID"+i);
    	  sessionMap.put("DATE_ID", dateID);
    	  sessionMap.put("TIMELEVEL", "TIMELEVEL"+i);
    	  sessionMap.put("DATATIME", "DATATIME"+i);
    	  sessionMap.put("DATADATE", "DATADATE"+i);
    	  sessionMap.put("ROWCOUNT", String.valueOf(i));
        sessionMap.put("SESSIONENDTIME", Long.toString(stopTime));
     	  sessionMap.put("SESSIONSTARTTIME", Long.toString(startTime));
    	  sessionMap.put("STATUS", "OK"+i);
    	  sessionMap.put("TYPENAME", "TYPENAME"+i);
    	  sessionMap.put("SOURCE", "SOURCE"+i);
    	  
    	  collection.add(new HashMap<String, Object>(sessionMap));
      }
  }

  @Test
  public void bulkLoadIntoSessionLog(){
	  lL.bulkLog(collection);
	  adapter = new File(tmpDir, "LOADER." + dateID + ".unfinished");  
	  adapter.deleteOnExit();
    final String startDate = sdf.format(new Date(startTime));
    final String stoptDate = sdf.format(new Date(stopTime));
	  try {
      final String expected = "100\tSESSION_ID9999\tBATCH_ID9999\tdateID_value\tTIMELEVEL9999\tDATADATE9999\tDATATIME9999\t9999\t"+startDate+"\t"+stoptDate+"\tSOURCE9999\tOK9999\tTYPENAME9999\t0\t";
		  final String actual = new HelpClass().readFileToString(adapter);
		  assertEquals(expected, actual);
	  } catch (Exception e) {
		  e.printStackTrace();
		  fail("testAdapterLog() failed");
	  }
  }
  
  @Test
  public void bulkLoadIntoSessionLogError(){
      //Create a new HashMap...
	  Collection<Map<String, Object>> collectionWithError = new ArrayList<Map<String, Object>>();
      
      for(int i = 0; i < 1; i++){
	      Map<String, Object> sessionMap = new HashMap<String, Object>();
	      sessionMap.put("LOADERSET_ID", "100");
    	  sessionMap.put("SESSION_ID", "SESSION_ID"+i);
    	  sessionMap.put("BATCH_ID", "BATCH_ID"+i);
    	  sessionMap.put("TIMELEVEL", "TIMELEVEL"+i);
    	  sessionMap.put("DATATIME", "DATATIME"+i);
    	  sessionMap.put("DATADATE", "DATADATE"+i);
    	  sessionMap.put("ROWCOUNT", String.valueOf(i));
        sessionMap.put("SESSIONENDTIME", Long.toString(stopTime));
     	  sessionMap.put("SESSIONSTARTTIME", Long.toString(startTime));
    	  sessionMap.put("STATUS", "OK"+i);
    	  sessionMap.put("TYPENAME", "TYPENAME"+i);
    	  sessionMap.put("SOURCE", "SOURCE"+i);
    	  
    	  collectionWithError.add(new HashMap<String, Object>(sessionMap));
      }
	  lL.bulkLog(collectionWithError);
	  //Check that there was no file created when DATE_ID == null
	  adapter = new File(tmpDir, "LOADER..unfinished");  
	  assertFalse(adapter.isFile());
  }
  
  @Test
  public void bulkLoadIntoSessionLogError2(){
	  //Create a new HashMap...
	  Collection<Map<String, Object>> collectionWithError = new ArrayList<Map<String, Object>>();

	  Map<String, Object> sessionMap = new HashMap<String, Object>();
	  sessionMap.put("LOADERSET_ID", "100");
	  sessionMap.put("SESSION_ID", "SESSION_ID1");
	  sessionMap.put("BATCH_ID", "BATCH_ID1");
	  sessionMap.put("TIMELEVEL", "TIMELEVEL1");
	  sessionMap.put("DATATIME", "DATATIME1");
	  sessionMap.put("DATADATE", "DATADATE1");
	  sessionMap.put("ROWCOUNT", String.valueOf(1));
	  sessionMap.put("SESSIONENDTIME", "1312553742434");
	  sessionMap.put("SESSIONSTARTTIME", "1312553740300");
	  sessionMap.put("STATUS", "OK");
	  sessionMap.put("TYPENAME", "TYPENAME1");
	  sessionMap.put("SOURCE", "SOURCE1");

	  collectionWithError.add(new HashMap<String, Object>(sessionMap));

	  sessionMap.put("LOADERSET_ID", "100");
	  sessionMap.put("SESSION_ID", "SESSION_ID2");
	  sessionMap.put("BATCH_ID", "BATCH_ID2");
	  sessionMap.put("DATE_ID", dateID);
	  sessionMap.put("TIMELEVEL", "TIMELEVEL2");
	  sessionMap.put("DATATIME", "DATATIME2");
	  sessionMap.put("DATADATE", "DATADATE2");
	  sessionMap.put("ROWCOUNT", String.valueOf(2));
    sessionMap.put("SESSIONENDTIME", Long.toString(stopTime));
 	  sessionMap.put("SESSIONSTARTTIME", Long.toString(startTime));
	  sessionMap.put("STATUS", "OK");
	  sessionMap.put("TYPENAME", "TYPENAME2");
	  sessionMap.put("SOURCE", "SOURCE2");

	  collectionWithError.add(new HashMap<String, Object>(sessionMap));

	  lL.bulkLog(collectionWithError);
	  adapter = new File(tmpDir, "LOADER." + dateID + ".unfinished");  
	  adapter.deleteOnExit();
	  try {
		  String expected = "100\tSESSION_ID2\tBATCH_ID2\tdateID_value\tTIMELEVEL2\tDATADATE2\tDATATIME2\t2\t"+startDate+"\t"+stoptDate+"\tSOURCE2\tOK\tTYPENAME2\t0\t";
		  String actual = new HelpClass().readFileToString(adapter);

		  assertEquals(expected, actual);
	  } catch (Exception e) {
		  e.printStackTrace();
		  fail("testAdapterLog() failed");
	  }
  }

  
  @Test
  public void testLoaderLog() {
    final Map<String, Object> map = new HashMap<String, Object>();
    final Long time = System.currentTimeMillis();

    map.put("DATE_ID", dateID);
    map.put("SESSIONSTARTTIME", "100");
    map.put("SESSIONENDTIME", "200");
    map.put("srcLastModified", String.valueOf(time));

    final File adapter = new File(tmpDir, "LOADER." + dateID + ".unfinished");
    adapter.deleteOnExit();

    if (adapter.exists()) {
    	adapter.delete();
    }
    
  	lL.log(map);
    try {
      final String expected = "null\tnull\tnull\t" + dateID + "\tnull\tnull\tnull\tnull\t" + sdf.format(new Date(100L)) + "\t" + sdf.format(new Date(200L))+ "\tnull\tnull\tnull\t0\t";
      final String actual = new HelpClass().readFileToString(adapter);
      
      assertEquals(expected, actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testAdapterLog() failed");
    }

    if (adapter.exists()) {
    	adapter.delete();
    }
  
  }
  
  @Test
  public void testLoaderLogNoDateID() {
  	final Map<String, Object> map = new HashMap<String,Object>();
  	
  	final File adapter = new File(tmpDir, "LOADER." + dateID + ".unfinished");
  	if (adapter.exists()) {
  		adapter.delete();
  	}
  	
    final Long time = System.currentTimeMillis();

    map.put("SESSIONSTARTTIME", "100");
    map.put("SESSIONENDTIME", "200");
    map.put("srcLastModified", String.valueOf(time));

    lL.log(map);

    if (adapter.exists()) {
    	adapter.delete();
    	fail("Should not produce row if dateid is not present");
    }
    
  }
  
  @Test
  public void rotateTest() {
  	
  	// Rotate should rename file1->rfile1 & file2->rfile2
  	
		final File file1 = new File(tmpDir, "LOADER.1.unfinished");
		final File rfile1 = new File(tmpDir, "LOADER.1");
		final File file2 = new File(tmpDir, "LOADER.2");
		final File file3 = new File(tmpDir, "LOADER.3.unfinished");
		final File rfile3 = new File(tmpDir, "LOADER.3");
		
  	try {
  		file1.createNewFile();
  		file2.createNewFile();
  		file3.createNewFile();
  	
  		lL.rotate();
  		
  		if (file1.exists() || file3.exists()) {
  			fail(".Unfinished files are left after rotate");
  		}
  		
  		if (!rfile1.exists() || !file2.exists() || !rfile3.exists()) {
  			fail("All files do not exist.");
  		}
  		
  	} catch (Exception e) {
  		fail("Exception not expected");
  	} finally {
  		try {
  			file1.delete();
  		} catch(Exception e) {}

  		try {
  			rfile1.delete();
  		} catch(Exception e) {}
  		
  		try {
  			file2.delete();
  		} catch(Exception e) {}

  		try {
  			file3.delete();
  		} catch(Exception e) {}
  		
  		try {
  			rfile3.delete();
  		} catch(Exception e) {}
  		
  	}
  	
  }
}

