package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import junit.framework.JUnit4TestAdapter;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ejarsok
 *
 */
public class StaticPropertiesTest {

  @BeforeClass
  public static void setup() throws Exception {

   
    StaticProperties.giveProperties(null);
    System.clearProperty("dc5000.config.directory");
  }


  @Test
  public void testGetProperty() {

    try {
      StaticProperties.getProperty("property1"); // Should catch
      // NullPointerException
      fail("Failed, getProperty should throw NullPointerException");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {

    }
  }

  @Test
  public void testGetProperty2() {

    try {
      StaticProperties.getProperty("property", "value"); // Should catch
      // NullPointerException
      fail("Failed, getProperty should throw NullPointerException");
    } catch (NullPointerException e) {

    }
  }

  @Test
  public void testGetProperty3() {
    final Properties prop = new Properties();
    prop.setProperty("property1", "value1");
    try {
      StaticProperties.giveProperties(prop);
      assertEquals("value1", StaticProperties.getProperty("property1")); // Should
      // catch
      // NoSuchFieldException
      StaticProperties.getProperty("notExist");
      fail("Failed, should not execute this");
    } catch (NoSuchFieldException e) {

    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
    }
  }

  @Test
  public void testGetProperty4() {
    assertEquals("value", StaticProperties.getProperty("notExist", "value")); // return
    // default
    // value
  }

  @Test
  public void testReload() {

    try {
      StaticProperties.reload(); // Should catch NullPointerException,
      // System.getProperty("dc5000.config.directory");
      // not set yet
      fail("Failed, should not execute this");
    } catch (NullPointerException e) {

    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
    }
  }

  private File createFile(final String p, final String v) throws IOException {
    final String homeDir = System.getProperty("java.io.tmpdir");

    final File propFile = new File(homeDir, "static.properties");
    propFile.deleteOnExit();
    final PrintWriter pw = new PrintWriter(new FileWriter(propFile));
    pw.print(p+"="+v);
    pw.close();
    return propFile;
  }

  @Test
  public void testSetPropertyAndSave() throws Exception {
    final String old = System.getProperty(StaticProperties.DC5000_CONFIG_DIR);

    final String pName = "property-x";
    final String pValue = "value-x";

    final File propFile = createFile("a", "b");
    if(old == null){
      System.setProperty(StaticProperties.DC5000_CONFIG_DIR, propFile.getParent());
    }
    try{
      StaticProperties.reload();
      final boolean saved = StaticProperties.setProperty(pName, pValue);
      assertTrue("File not saved", saved);
      String readValue = StaticProperties.getProperty(pName);
      assertEquals("Set Value not stored correctly.", pValue, readValue);
      readValue = StaticProperties.getProperty("a");
      assertEquals("Stored Value not retrieved correctly.", "b", readValue);
    } finally {
      if(old == null){
        System.clearProperty(StaticProperties.DC5000_CONFIG_DIR);
      } else {
        System.setProperty(StaticProperties.DC5000_CONFIG_DIR, old);
      }
    }
  }

  @Test
  public void testSetProperynoLoad(){
    StaticProperties.giveProperties(null);
    final boolean saved = StaticProperties.setProperty("a", "v");
    assertFalse("Set should not save", saved);
  }

  @Test
  public void testReload2() throws Exception {
    final String old = System.getProperty(StaticProperties.DC5000_CONFIG_DIR);
    final File propFile = createFile("foo", "bar");
    if(old == null){
      System.setProperty(StaticProperties.DC5000_CONFIG_DIR, propFile.getParent());
    }
    try{
      StaticProperties.reload();
      assertEquals("bar", StaticProperties.getProperty("foo")); // return
    } finally {
      if(old == null){
        System.clearProperty(StaticProperties.DC5000_CONFIG_DIR);
      } else {
        System.setProperty(StaticProperties.DC5000_CONFIG_DIR, old);
      }
    }
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(StaticPropertiesTest.class);
  }
}
