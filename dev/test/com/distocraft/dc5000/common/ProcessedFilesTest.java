package com.distocraft.dc5000.common;

import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import junit.framework.JUnit4TestAdapter;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Tests for ProcessedFiles class in com.distrocraft.dc5000.common. <br>
 * <br>
 * Testing the handling of processed files. This class has methods to see if a
 * given file is processed and add unprocessed files to the list of processed
 * files.
 * 
 * @author EJAAVAH
 * 
 */
public class ProcessedFilesTest {

  private static ProcessedFiles objUnderTest;

  private static Properties conf;

  private static Method parseFileName;

  private static Method readFileList;

  private static File testProcessDir;

  private static File testReadFile;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    File userDir = new File(System.getProperty("user.dir"));
    String testDirName = System.getProperty("user.name") + "testProcessDir";
    testProcessDir = new File(userDir, testDirName);
    testReadFile = new File(userDir, "file_isProcessed.txt");

    conf = new Properties();
    conf.setProperty("ProcessedFiles.processedDir", testDirName);
    conf.setProperty("ProcessedFiles.fileNameFormat", "(.+)");

    try {
      objUnderTest = new ProcessedFiles(conf);
      Class PF = objUnderTest.getClass();
      parseFileName = PF.getDeclaredMethod("parseFileName", new Class[] { String.class, String.class });
      readFileList = PF.getDeclaredMethod("readFileList", new Class[] { String.class });
    } catch (Exception e) {
      e.printStackTrace();
    }
    parseFileName.setAccessible(true);
    readFileList.setAccessible(true);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    objUnderTest = null;
    testProcessDir.delete();
    testReadFile.delete();
  }

  /**
   * Testing that a processed file is added to the "is processed" list and that
   * unprocessed file is not.
   */
  @Test
  public void testIsProcessed() throws Exception {
    objUnderTest.addToProcessed("isProcessed", "file");
    assertEquals(true, objUnderTest.isProcessed("isProcessed", "file"));
    assertEquals(false, objUnderTest.isProcessed("notProcessed", "file"));
  }

  /**
   * Test if contents of the processed filelist is written into a textfile.
   */
  @Test
  public void testWriteProcessedFile() throws Exception {
    objUnderTest.writeProcessedToFile();
    String result = new HelpClass().readFileToString(testReadFile);
    assertEquals("isProcessed", result);
  }

  /**
   * Testing if filelist is read from a file to a Set object with existing and
   * not existing files.
   */
  @Test
  public void testReadFileList() throws Exception {
    // Expected set object for testing
    Set<String> set = new HashSet<String>();
    assertEquals(set, readFileList.invoke(objUnderTest, new Object[] { null }));
    set.add("isProcessed");
    assertEquals(set, readFileList.invoke(objUnderTest, new Object[] { "file_isProcessed.txt" }));
  }

  /**
   * Test if all filenames from a file is loaded into a Set object.
   */
  @Test
  public void testGetLoadedFiles() throws Exception {
    Set<String> set = new HashSet<String>();
    set.add("isProcessed");
    assertEquals(set, objUnderTest.getLoadedFiles("file_isProcessed.txt"));
  }

  /**
   * Test for parsing filenames. For example timestamp could be parsed from a
   * filename using certain pattern. This is tested with generic input and value
   * that doesn't match the pattern.
   */
  @Test
  public void testParseFileName() throws Exception {
    // Pattern to be used when parsing the file (this will parse the text
    // between "_" characters)
    conf.setProperty("ProcessedFiles.fileNameFormat", ".+_(.+)_.+");
    objUnderTest = new ProcessedFiles(conf);
    assertEquals("source_01012008.txt", parseFileName.invoke(objUnderTest, new Object[] { "file_01012008_x.txt",
        "source" }));

    // Testing if Exception is thrown when trying to parse filename that doesn't
    // match the pattern
    try {
      parseFileName.invoke(objUnderTest, new Object[] { "anotherfileName", "source" });
      fail("Test failed - Exception should have been thrown as the filename does not match the pattern");
    } catch (Exception e) {
      // Test passed
    }
  }

  /**
   * Asserting that getProcessedDir() method parses <i>user.name</i> system
   * property and return the correct directoryname for the directory being
   * tested.
   */
  @Test
  public void testGetProcessedDir() throws Exception {
    assertEquals(System.getProperty("user.name") + "testProcessDir", objUnderTest
        .getProcessedDir("${user.name}testProcessDir"));
  }
  
  // Making the test work with ant 1.6.5 and JUnit 4.x
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(ProcessedFilesTest.class);
  }
}
