package com.distocraft.dc5000.common;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.common.CalcFirstDayOfWeek;

public class CalcFirstDayOfWeekTest {

	@Before
	public void setUp() throws Exception {
		
		 /* Home directory path */
	    File homeDir = new File(System.getProperty("user.dir"));
		 /* Creating static property file */
	    File sp = new File(homeDir, "static.properties");
	    sp.deleteOnExit();
	    try {
	      PrintWriter pw = new PrintWriter(new FileWriter(sp));
	      pw.print("firstDayOfTheWeek=5\n");
	      pw.close();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    /* Setting the system property for static property file */
	    System.setProperty("CONF_DIR", homeDir.getPath());

	    /* Initializing Static Properties in order to initialize SessionHandler */
	   // StaticProperties.reload();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalcFirstDayOfWeek() {
		
		int firstDayOfTheWeek=0;
		
		try {
				
			firstDayOfTheWeek = CalcFirstDayOfWeek.calcFirstDayOfWeek();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(5, firstDayOfTheWeek);
		
	}

}
