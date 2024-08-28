package com.ericsson.eniq.common;

import com.distocraft.dc5000.common.StaticProperties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommonUtilsTest {

  private static final File baseDir = new File(System.getProperty("java.io.tmpdir"), "CommonUtilsTest");

  @BeforeClass
  public static void beforeClass(){
    if(!baseDir.exists() && !baseDir.mkdirs()){
      Assert.fail("Test case setup failed, dir not created : " + baseDir.getPath());
    }
    System.setProperty(CommonUtils.CONFIG_DIR_PROPERTY_NAME, baseDir.getPath());
    System.setProperty(CommonUtils.DC_CONFIG_DIR_PROPERTY_NAME, baseDir.getPath());
    System.setProperty(StaticProperties.DC5000_CONFIG_DIR, baseDir.getPath());
    StaticProperties.giveProperties(new Properties());
  }

  @AfterClass
  public static void afterClass() {
    delete(baseDir);
  }

  @Test
  public void test_getNumOfDirectories() throws IOException {
    final int fsCount = 5;
    int filenum = CommonUtils.getNumOfDirectories(Logger.getAnonymousLogger());
    Assert.assertEquals("DIRECTORY_STRUCTURE.FileSystems not read correctly", 0, filenum);

    createNiqIni(fsCount);

    filenum = CommonUtils.getNumOfDirectories(Logger.getAnonymousLogger());
    Assert.assertEquals("DIRECTORY_STRUCTURE.FileSystems not read correctly", fsCount, filenum);
  }

  @Test
  public void test_EventsEtlDataDir() {
    final String vValue = "/eniq/data/etldata_/00/event_e_sgeh_err/raw/";
    final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(vValue, 4, false);
    Assert.assertNotNull("Null list shouldn't have been returned", expanded);
    Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == 1);
    final File vValFile = new File(vValue);
    Assert.assertEquals("Path not expanded correctly ", vValFile.getPath(), expanded.get(0).getPath());
  }


  @Test
  public void test_expandPathWithMountPoints_AlreadyExpanded() {
    final String vName = "ETLDATA_DIR";
    final String vValue = "/eniq/data/etldata";
    final String oldValue = System.setProperty(vName, vValue);
    final String dir = vValue + "/00/dc_e_abc/";
    final int etlCount = 1;
    try {
      final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(dir, etlCount, false);
      Assert.assertNotNull("Null list shouldn't have been returned", expanded);
      Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == etlCount);
      final File vValFile = new File(dir);
      Assert.assertEquals("Path not expanded correctly ", vValFile.getPath(), expanded.get(0).getPath());
    } finally {
      resetVariable(vName, oldValue);
    }
  }

  @Test
  public void test_expandPathWithMountPoints_WithPrune() {
    final String oldValue1 = System.setProperty(CommonUtils.ETLDATA_DIR, baseDir.getPath());
    final String oldValue2 = System.setProperty(CommonUtils.EVENTS_ETLDATA_DIR, baseDir.getPath());


    final String sub = "dc_e_abc";
    final File okFile = new File(baseDir, "02/" + sub);

    if (!okFile.exists() && !okFile.mkdirs()) {
      Assert.fail("Setup failed to create " + okFile.getPath());
    }
    final String dir = "${" + CommonUtils.ETLDATA_DIR + "}/" + sub + "/";

    final int etlCount = 4;
    try {
      final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(dir, etlCount);
      Assert.assertNotNull("Null list shouldn't have been returned", expanded);
      Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == 1);
      Assert.assertEquals("Path not expanded correctly ", okFile.getPath(), expanded.get(0).getPath());
    } finally {
      resetVariable(CommonUtils.ETLDATA_DIR, oldValue1);
      resetVariable(CommonUtils.EVENTS_ETLDATA_DIR, oldValue2);
    }
  }

  @Test
  public void test_expandPathWithMountPoints_VarSet() {
    final String vName = "ETLDATA_DIR";
    final String vValue = "/eniq/data/etldata";
    final String oldValue = System.setProperty(vName, vValue);
    final String dir = "${" + vName + "}/dc_e_abc/";
    final int etlCount = 4;
    try {
      final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(dir, etlCount, false);
      Assert.assertNotNull("Null list shouldn't have been returned", expanded);
      Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == etlCount);
      final File vValFile = new File(vValue);
      for (File f : expanded) {
        Assert.assertTrue("Path not expanded with correct sub-path", f.getPath().startsWith(vValFile.getPath()));
      }
    } finally {
      resetVariable(vName, oldValue);
    }
  }

  @Test
  public void test_expandPathWithMountPoints_VarUnset() {
    final int etlCount = 4;
    final String dir = "${VAR43}/dc_e_abc/";
    final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(dir, etlCount, false);
    Assert.assertNotNull("Null list shouldn't have been returned", expanded);
    Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == 1);
    Assert.assertEquals("Path not expanded correctly (variable unset)",
      "null"+File.separatorChar+"dc_e_abc", expanded.get(0).getPath());
  }

  @Test
  public void test_expandPathWithNoMountPoints() {
    final String vName = "PMDATA_DIR";
    final String vValue = "/eniq/data/pmdata";

    final String oldValue = System.setProperty(vName, vValue);
    final String dir = "${" + vName + "}/dc_e_abc/";
    final int etlCount = 4;
    try {
      final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(dir, etlCount, false);
      Assert.assertNotNull("Null list shouldn't have been returned", expanded);
      Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == 1);
      final File vValFile = new File(vValue, "dc_e_abc");
      Assert.assertEquals("Path didn't need to be expanded", expanded.get(0).getPath(), vValFile.getPath());
    } finally {
      resetVariable(vName, oldValue);
    }
  }

  @Test
  public void test_expandEtlRoot(){
    final String vValue = "/eniq/data/etldata/";
    final List<File> expanded = CommonUtils.expandEtlPathWithMountPoints(vValue, 4, false);
    Assert.assertNotNull("Null list shouldn't have been returned", expanded);
    Assert.assertTrue("Path not expanded to the correct number of dirs", expanded.size() == 1);
    final File vValFile = new File(vValue);
    Assert.assertEquals("Path didn't need to be expanded", expanded.get(0).getPath(), vValFile.getPath());
  }


  @Test
  public void test_ExpandStatsEtlRootDir() {
    final String path = "${" + CommonUtils.ETLDATA_DIR + "}/abc";
    final String oldSValue = System.setProperty(CommonUtils.ETLDATA_DIR, "/eniq/data/etldata");
    final String oldEValue = System.setProperty(CommonUtils.EVENTS_ETLDATA_DIR, "/eniq/data/etldata_");
    try {
      final String expanded = CommonUtils.expandPathWithVariable(path);
      Assert.assertEquals("Stats ETLDATA_DIR not expanded correctly", "/eniq/data/etldata/abc", expanded);
    } finally {
      resetVariable(CommonUtils.ETLDATA_DIR, oldSValue);
      resetVariable(CommonUtils.EVENTS_ETLDATA_DIR, oldEValue);
    }
  }

  @Test
  public void testExpandEventsEtl_RootDir() {
    final String path = "${" + CommonUtils.EVENTS_ETLDATA_DIR + "}/abc";
    final String oldSValue = System.setProperty(CommonUtils.ETLDATA_DIR, "/eniq/data/etldata");
    final String oldEValue = System.setProperty(CommonUtils.EVENTS_ETLDATA_DIR, "/eniq/data/etldata_");
    try {
      final String expanded = CommonUtils.expandPathWithVariable(path);
      //Note the underscore
      Assert.assertEquals("Events ETLDATA_DIR not expanded correctly", "/eniq/data/etldata_/abc", expanded);
    } finally {
      resetVariable(CommonUtils.ETLDATA_DIR, oldSValue);
      resetVariable(CommonUtils.EVENTS_ETLDATA_DIR, oldEValue);
    }
  }

  @Test
  public void test_ExpandVarPath_NoVariable() {
    final String ePath = "/a/b/c";
    final String aPath = CommonUtils.expandPathWithVariable(ePath);
    Assert.assertEquals("Path not expanded correctly ", ePath, aPath);
  }

  @Test
  public void test_ExpandVarPath_WithVariableSet() {
    final String vName = "VAR1";
    final String vValue = "/a/bd";
    System.setProperty(vName, vValue);
    final String ePath = "${" + vName + "}/c";
    try {
      final String aPath = CommonUtils.expandPathWithVariable(ePath);
      final String expected = vValue + "/" + "c";
      Assert.assertEquals("Path not expanded correctly ", expected, aPath);
    } finally {
      resetVariable(vName, null);
    }
  }

  @Test
  public void test_ExpandVarPath_WithVariableUnSet() {
    final String ePath = "${VAR2}/c";
    final String aPath = CommonUtils.expandPathWithVariable(ePath);
    final String expected = "null/c";
    Assert.assertEquals("Path not expanded correctly ", expected, aPath);
  }

  private void createNiqIni(final int fsCount) throws IOException {
    final File ini = new File(baseDir, "niq.ini");
    if(!ini.exists() && !ini.createNewFile()){
      Assert.fail("Test case setup failed, file not created : " + ini.getPath());
    }
    final BufferedWriter writer = new BufferedWriter(new FileWriter(ini, false));
    writer.write("[DIRECTORY_STRUCTURE]");
    writer.newLine();
    writer.write("FileSystems=" + fsCount);
    writer.newLine();
    writer.close();
    ini.deleteOnExit();
  }

  private void resetVariable(final String name, final String oldvalue) {
    if (oldvalue == null) {
      System.clearProperty(name);
    } else {
      System.setProperty(name, oldvalue);
    }
  }

  private static boolean delete(final File file) {
    if (!file.exists()) {
      return true;
    }
    if (file.isDirectory()) {
      final File[] sub = file.listFiles();
      for (File sf : sub) {
        if (!delete(sf)) {
          System.out.println("Couldn't delete directory " + sf.getPath());
          return false;
        }
      }
    }
    if (!file.delete()) {
      System.out.println("Couldn't delete file " + file.getPath());
      return false;
    }
    return true;
  }
}
