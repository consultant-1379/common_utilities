package com.distocraft.dc5000.common;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 * 
 * @author ejarsok
 * 
 */

public class PropertiesTest {

  private static Properties p;

  private static Properties p2;

  private static String source;

  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "PropertiesTest");
  
  @BeforeClass
  public static void init() {
    System.setProperty("dc5000.config.directory", TMP_DIR.getPath());
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }

    final File prop = new File(TMP_DIR, "test.properties");
    prop.deleteOnExit();

    source = "value";
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(prop));
      pw.print(source + ".logProperty=log");
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Can´t write in file!");
    }

    Hashtable hT = new Hashtable();
    hT.put("property1", "value1");
    hT.put(source + ".property2", "value2");

    try {
      p = new Properties(source);
      p2 = new Properties(source, hT);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(TMP_DIR);
  }

  @Test
  public void testProperties() {
    assertEquals(source, p.getProperty("source")); // Return String source
  }

  @Test
  public void testProperties2() {
    assertEquals("log", p.getProperty("logProperty")); // Return
    // "logProperty" value
    // from file
  }

  @Test
  public void testProperties3() {
    assertEquals("value1", p2.getProperty("property1")); // Return "property1"
    // value from
    // hashTable
  }

  @Test
  public void testProperties4() {
    assertEquals("value2", p2.getProperty("property2")); // Return source + "."
    // + "property2" value
    // from hashTable
  }

  @Test
  public void testProperties5() {
    assertEquals("default", p2.getProperty("notExists", "default")); // Return
    // default
    // value
    // "default"
  }

  @Test
  public void testProperties6() {
    try {
      assertEquals("Value", p2.getProperty("notExists")); // property value not
      // exists, catch
      // NullPointerException
      fail("Should not execute this");
    } catch (NullPointerException e) {

    } catch (Exception e) {
      e.printStackTrace();
      fail("testProperties failed");
    }
  }

  @Test
  public void testProperties7() {
    try {
      assertEquals("Value", p2.getProperty(null));
      // null, catch
      // NullPointerException
      fail("Should not execute this");
    } catch (NullPointerException e) {

    } catch (Exception e) {
      e.printStackTrace();
      fail("testProperties failed");
    }
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(PropertiesTest.class);
  }
}
