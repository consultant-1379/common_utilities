package com.ericsson.eniq.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Map;
import java.util.logging.Logger;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

/**
 * 
 * @author ejarsok
 *
 */

public class INIGetTest {

	private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"), "INIGetTest");
	private static final File iniFile = new File(tmpDir, "file.ini");
	private static final File iniFile2 = new File(tmpDir, "file2.ini");

	private final INIGet ig = new INIGet();

	@BeforeClass
	public static void beforeClass() throws IOException {

		// build test ini file - file.ini
		if(!tmpDir.exists() && !tmpDir.mkdirs()){
			fail("Failed to create directory " + tmpDir.getPath());
		}
		tmpDir.deleteOnExit();
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(new FileWriter(iniFile, false));
			pw.print("[foobar]\n");
			pw.print("Parameter=PAR\n");
			pw.print("Parameter2\n");
			pw.print("[fobr]\n");
			pw.print("Parameter=PAR2\n");
		} finally {
			if(pw != null){
				pw.close();
			}
		}
		iniFile.deleteOnExit();

		// build second test ini file - file2.ini
		// includes blank lines
		try{
			pw = new PrintWriter(new FileWriter(iniFile2, false));
			pw.print(";\n");
			pw.print("[foobar]\n");
			pw.print("Parameter=PAR\n");
			pw.print("Parameter2\n");
			pw.print("\n");
			pw.print("[fobr]\n");
			pw.print("Parameter=PAR2\n");
		} finally {
			if(pw != null){
				pw.close();
			}
		}
		iniFile2.deleteOnExit();
	}

	@AfterClass
	public static void afterClass(){
		iniFile.delete();
	}  

	/**
	 * Test set and get methods
	 *
	 */

	@Test
	public void testSetAndGetSetFile() {
		ig.setFile("File");
		assertEquals("File", ig.getFile());  
	}

	@Test
	public void testSetAndGetSetParameter() {
		ig.setParameter("Parameter");
		assertEquals("Parameter", ig.getParameter());
	}

	@Test
	public void testSetAndGetSetParameterValue() {
		ig.setParameterValue("ParameterValue");
		assertEquals("ParameterValue", ig.getParameterValue());
	}

	@Test
	public void testSetAndGetSetSection() {
		ig.setSection("Section");
		assertEquals("Section", ig.getSection());
	}


	/**
	 * check that correct parameter value is loaded from INIGetFile File
	 * 
	 */

	@Test
	public void testExecute() {

		final INIGet ig = new INIGet();
		ig.setFile(iniFile.getPath());
		ig.setSection("foobar");
		ig.setParameter("Parameter");

		ig.execute(Logger.getLogger("Log"));
		assertEquals("PAR", ig.getParameterValue());
	}

	@Test
	public void testNonExistIniFile() {
		final INIGet ig = new INIGet();
		ig.setFile("foobaar");
		ig.setSection("foo");
		ig.setParameter("bar");
	}

	/* TODO: more tests for readIniFile */

	/**
	 * check that readIniFile is working as expected
	 * 
	 */
	@Test
	public void testReadIniFile() throws IOException {
		final Map<String, Map<String, String>> ini = ig.readIniFile(iniFile);
		assertTrue(ini.containsKey("foobar"));
		final Map<String, String> foobar = ini.get("foobar");
		assertEquals("PAR", foobar.get("Parameter"));
		assertNull(foobar.get("Parameter2"));

		assertTrue(ini.containsKey("fobr"));
		final Map<String, String> fobr = ini.get("fobr");
		assertEquals("PAR2", fobr.get("Parameter"));
	}

	/**
	 * check that readIniFile is working as expected for file with comments/blank lines
	 * 
	 */
	@Test
	public void testReadIniFile2() throws IOException {
		final Map<String, Map<String, String>> ini = ig.readIniFile(iniFile2);
		assertTrue(ini.containsKey("foobar"));
		final Map<String, String> foobar = ini.get("foobar");
		assertEquals("PAR", foobar.get("Parameter"));
		assertNull(foobar.get("Parameter2"));

		assertTrue(ini.containsKey("fobr"));
		final Map<String, String> fobr = ini.get("fobr");
		assertEquals("PAR2", fobr.get("Parameter"));
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(INIGetTest.class);
	}
}
