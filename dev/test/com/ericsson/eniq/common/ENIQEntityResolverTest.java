package com.ericsson.eniq.common;

import static org.junit.Assert.*;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ejarsok
 *
 */

public class ENIQEntityResolverTest {
  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "ENIQEntityResolverTest");

  @BeforeClass
  public static void beforeClass(){
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }
  }

  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(TMP_DIR);
  }

  @Test
  public void testResolveEntity() {
    InputSource is = null;
    final ENIQEntityResolver er = new ENIQEntityResolver("Log");

    try {
      is = er.resolveEntity("publicId", "systemId");
      assertNotNull(is);
      assertEquals("publicId", is.getPublicId());
      assertEquals("systemId", is.getSystemId());
    } catch (SAXException e) {
      e.printStackTrace();
      fail("SAXException");
    }
  }

  /**
   * InputSource is = er.resolveEntity("publicId/publicIdFile", "systemId");<br />
   * What should publicIdFile contain??
   *
   */

  @Test
  public void testResolveEntity2() {
    System.setProperty("CONF_DIR", TMP_DIR.getPath());
    final File dtdDir = new File(TMP_DIR, "dtd");
    dtdDir.mkdir();

    InputSource is = null;
    final ENIQEntityResolver er = new ENIQEntityResolver("Log");

    try {
      is = er.resolveEntity("publicId", "systemId/systemIdFile");
      assertNotNull(is);
      assertEquals("publicId", is.getPublicId());
      assertEquals("systemIdFile", is.getSystemId());
    } catch (SAXException e) {
      e.printStackTrace();
      fail("SAXException");
    }

    dtdDir.delete();
  }

  @Test
  public void testResolveEntity3() {
    InputSource is = null;
    final ENIQEntityResolver er = new ENIQEntityResolver("Log");

    try {
      is = er.resolveEntity(null, null);
      assertNotNull(is);
      assertEquals(null, is.getPublicId());
      assertEquals(null, is.getSystemId());
    } catch (SAXException e) {
      e.printStackTrace();
      fail("SAXException");
    }
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(ENIQEntityResolverTest.class);
  }
}
