package com.distocraft.dc5000.common;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author ejarsok
 */
public class AggregatorLogTest {

  private static File adapter;

  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "AggregatorLogTest");

  private static String dateID = "dateID_value";
  
  private static AggregatorLog aL;

  @BeforeClass
  public static void init() {

    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }
  	
  	final Logger log = Logger.getLogger("etlengine.common.SessionLogger.AGGREGATOR");
  	log.setLevel(Level.ALL);

    final Properties prop = new Properties();
    prop.setProperty("SessionHandling.log.AGGREGATOR.inputTableDir", TMP_DIR.getPath());

    try {
      StaticProperties.giveProperties(prop);
      aL = new AggregatorLog();
      
    } catch (Exception e1) {
      e1.printStackTrace();
      fail("testAggregatorLog failed");
    }
  }

  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(TMP_DIR);
  }

  @Test
  public void testAggregatorLog() {
  	final Map<String, Object> map = new HashMap<String,Object>();

  	final Long time = System.currentTimeMillis();
    map.put("DATE_ID", dateID);
    map.put("DATADATE", String.valueOf(time));
    map.put("DATATIME", String.valueOf(time));
    map.put("DATADATE", "100");
    map.put("DATATIME", "200");
    map.put("SESSIONSTARTTIME", "300");
    map.put("SESSIONENDTIME", "400");
  	
    aL.log(map);
    adapter = new File(TMP_DIR, "AGGREGATOR." + dateID + ".unfinished");
    adapter.deleteOnExit();
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    
    try {
      //String expected = "AGGREGATORSET_ID\tSESSION_ID\tBATCH_ID\tDATE_ID\tTIMELEVEL\tDATADATE\tDATATIME\tROWCOUNT\tSESSIONSTARTTIME\tSESSIONENDTIME\tSOURCE\tSTATUS\tTYPENAME\tFLAG\t";
      final String expected = "null\tnull\tnull\t" + dateID + "\tnull\t" + sdf.format(new Date(100L)) + "\t" + sdf.format(new Date(200L)) + "\tnull\t" + sdf.format(new Date(300L)) + "\t" + sdf.format(new Date(400L)) + "\tnull\tnull\tnull\t0\t";
      final String actual = new HelpClass().readFileToString(adapter);

      assertEquals(expected, actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testAdapterLog() failed");
    }
  }
  
  @Test
  public void testAggregatorLogNoDateID() {
  	final Map<String, Object> map = new HashMap<String,Object>();
  	
  	adapter = new File(TMP_DIR, "AGGREGATOR." + dateID + ".unfinished");
  	if (adapter.exists()) {
  		adapter.delete();
  	}
  	
  	final Long time = System.currentTimeMillis();
    map.put("DATADATE", String.valueOf(time));
    map.put("DATATIME", String.valueOf(time));
    map.put("DATADATE", "100");
    map.put("DATATIME", "200");
    map.put("SESSIONSTARTTIME", "300");
    map.put("SESSIONENDTIME", "400");
  	
    aL.log(map);
    adapter = new File(TMP_DIR, "AGGREGATOR." + dateID + ".unfinished");
    if (adapter.exists()) {
    	adapter.delete();
    	fail("Should not produce row if dateid is not present");
    }
    
  }
  
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(AggregatorLogTest.class);
  }
}
