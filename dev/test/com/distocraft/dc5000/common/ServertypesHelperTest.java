package com.distocraft.dc5000.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;



public class ServertypesHelperTest {

	private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"), "ServertypesHelperTest");

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		System.setProperty("CONF_DIR", TMPDIR.getPath());
		System.setProperty("CONF_DIR", TMPDIR.getPath());
		ServicenamesTestHelper.setupEmpty(TMPDIR);
	}

	@Before
	public void setUp() throws Exception {
		// clear server types
		ServertypesHelper.setServerTypeDetails(null);
	}

	@After
	public void tearDown() throws Exception {
		// clear server types
		ServertypesHelper.setServerTypeDetails(null);
	}

	@Test
	public void testNoServertypesFile(){
		final String old = System.setProperty("CONF_DIR", "123");
		try{
			ServertypesHelper.getServertypesFile();
			fail("Exptected FileNotFoundException to be thrown!");
		} catch (FileNotFoundException e){
			/* Expected this*/
		} finally {
			System.setProperty("CONF_DIR", old);
		}
	}

	@Test
	public void test_getServertypesStats() throws IOException {
		createStatsServerTypesFile();
		final Map<String, ServertypesHelper.ServerTypeDetails> details = ServertypesHelper.getServertypeDetails();
		assertTrue(details.containsKey("atrcxb2332"));
		final ServertypesHelper.ServerTypeDetails testTypes = details.get("atrcxb2332");
		assertEquals("Server Type not set correctly", "stats_coordinator", testTypes.getServerType());
		assertEquals("Hostname not set correctly", "atrcxb2332", testTypes.getHostname());
		assertEquals("IP Address not set correctly", "10.45.192.184", testTypes.getIpAddress());
		assertEquals("Wrong number of servertypes", 5, details.size());
	}

	@Test
	public void test_getServertypesEvents() throws IOException {
		createEventsServerTypesFile();
		final Map<String, ServertypesHelper.ServerTypeDetails> details = ServertypesHelper.getServertypeDetails();
		assertTrue(details.containsKey("atrcxb1365"));
		final ServertypesHelper.ServerTypeDetails testTypes = details.get("atrcxb1365");
		assertEquals("Server Type not set correctly", "eniq_mz", testTypes.getServerType());
		assertEquals("Hostname not set correctly", "atrcxb1365", testTypes.getHostname());
		assertEquals("IP Address not set correctly", "10.44.95.43", testTypes.getIpAddress());
		assertEquals("Wrong number of servertypes", 8, details.size());
	}

	@Test
	public void test_getServertypesEmpty() throws IOException {
		createEmptyServerTypesFile();
		final Map<String, ServertypesHelper.ServerTypeDetails> details = ServertypesHelper.getServertypeDetails();
		assertEquals("Wrong number of servertypes", 0, details.size());
	}

	@Test
	public void test_getServertypesBad() throws IOException {
		createBadServerTypesFile();
		final Map<String, ServertypesHelper.ServerTypeDetails> details = ServertypesHelper.getServertypeDetails();
		assertEquals("Wrong number of servertypes", 0, details.size());
	}
	
	@Test
	public void test_dontReloadServertypesIfLoaded() throws IOException {
		// get server types from file on first attempt
		createStatsServerTypesFile();
		Map<String, ServertypesHelper.ServerTypeDetails> details = ServertypesHelper.getServertypeDetails();
		assertEquals("Wrong number of server types", 5, details.size());

		// alter server types file and get the details again - 
		// returned details should not change 
		createEventsServerTypesFile();
		details = ServertypesHelper.getServertypeDetails();
		assertEquals("Details of server types should not be reloaded", 5, details.size());
	}

	@Test
	public void test_reloadServertypesAfterClearing() throws IOException {
		// get server types from file on first attempt
		createStatsServerTypesFile();
		Map<String, ServertypesHelper.ServerTypeDetails> details = ServertypesHelper.getServertypeDetails();
		assertEquals("Wrong number of server types", 5, details.size());

		// alter server types file, clear existing details and 
		// get the details again - returned details should have change 
		createEventsServerTypesFile();
		ServertypesHelper.setServerTypeDetails(null);
		details = ServertypesHelper.getServertypeDetails();
		assertEquals("Details of server types should have been reloaded", 8, details.size());
	}
	
    @Test
    public void test_getServertype() throws IOException {
		createStatsServerTypesFile();
    	assertEquals("Wrong server type returned.", "stats_coordinator", ServertypesHelper.getServertype("atrcxb2332"));
    }
	
    @Test
    public void test_getDisplayServertype() throws IOException {
		createStatsServerTypesFile();
    	String displayServerType = ServertypesHelper.getDisplayServertype("atrcxb2332");
    	assertEquals("Wrong server type returned.", ServertypesHelper.COORDINATOR_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb2333");
    	assertEquals("Wrong server type returned.", ServertypesHelper.IQR_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb2334");
    	assertEquals("Wrong server type returned.", ServertypesHelper.ENGINE_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb2335");
    	assertEquals("Wrong server type returned.", ServertypesHelper.IQR_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb2336");
    	assertEquals("Wrong server type returned.", ServertypesHelper.STATS_NAME, displayServerType);

		ServertypesHelper.setServerTypeDetails(null);
		createEventsServerTypesFile();
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb1731");
    	assertEquals("Wrong server type returned.", ServertypesHelper.MEDIATION_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb1361");
    	assertEquals("Wrong server type returned.", ServertypesHelper.COORDINATOR_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb1362");
    	assertEquals("Wrong server type returned.", ServertypesHelper.UI_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb1363");
    	assertEquals("Wrong server type returned.", ServertypesHelper.IQR_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("atrcxb1337");
    	assertEquals("Wrong server type returned.", ServertypesHelper.EVENTS_NAME, displayServerType);
    	displayServerType = ServertypesHelper.getDisplayServertype("asdasdas");
    	assertEquals("Wrong server type returned.", ServertypesHelper.UNKNOWN_SERVER_TYPE, displayServerType);
    
    
    }
	
    @Test
    public void test_getServertypeUnknownServer() throws IOException {
		createStatsServerTypesFile();
    	assertNull("Wrong server type returned.", ServertypesHelper.getServertype("xyz123"));
    }
	
	private void createEventsServerTypesFile() throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.write("10.44.95.137::atrcxb1731::eniq_mz\n");
		writer.write("10.44.95.39::atrcxb1361::eniq_coordinator\n");
		writer.write("10.44.95.40::atrcxb1362::eniq_ui\n");
		writer.write("10.44.95.41::atrcxb1363::eniq_iqr\n");
		writer.write("10.44.95.42::atrcxb1364::eniq_iqr\n");
		writer.write("10.44.95.43::atrcxb1365::eniq_mz\n");
		writer.write("10.44.95.45::atrcxb1336::eniq_iqr\n");
		writer.write("10.44.95.46::atrcxb1337::eniq_events\n");
		writer.close();
	}
	
	private void createStatsServerTypesFile() throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.write("10.45.192.184::atrcxb2332::stats_coordinator\n");
		writer.write("10.45.192.185::atrcxb2333::stats_iqr\n");
		writer.write("10.45.192.186::atrcxb2334::stats_engine\n");
		writer.write("10.45.192.187::atrcxb2335::stats_iqr\n");
		writer.write("10.45.192.188::atrcxb2336::eniq_stats\n");
		writer.close();
	}

	private void createEmptyServerTypesFile() throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.close();
	}

	private void createBadServerTypesFile() throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.write("asdjahjkdhasjdhka sjdha sklda\n");
		writer.write("\n");
		writer.close();
	}
	
}
