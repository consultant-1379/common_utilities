                               /**
 * 
 */
package com.ericsson.eniq.common;

import static org.junit.Assert.*;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author edeamai
 *
 */
public class OSSIDMappingCacheTest {
  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "OSSIDMappingCacheTest");
	final File ossIdMappingFile = new File(TMP_DIR, "OSSID_MAPPING.txt");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
    DirectoryHelper.delete(TMP_DIR);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ossIdMappingFile.delete();
		final PrintWriter pw = new PrintWriter(new FileWriter(ossIdMappingFile));
		pw.println("eniq_events_1,events_oss_1,eniq_oss_4");
		pw.println("eniq_events_1,events_oss_2,eniq_oss_3");
		pw.println("eniq_events_2,events_oss_3,eniq_oss_2");
		pw.flush();
		pw.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		ossIdMappingFile.delete();
	}
	
	@Test
	public void testInitialize() throws Exception {
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		final OSSIDMappingCache ossidcache = OSSIDMappingCache.getCache();
		
		assertTrue(ossidcache.map1.size() == 3);
		assertTrue(ossidcache.map2.size() == 3);
		
		assertTrue(ossidcache.map1.get("eniq_events_1events_oss_1").equalsIgnoreCase("eniq_oss_4"));
		assertTrue(ossidcache.map1.get("eniq_events_1events_oss_2").equalsIgnoreCase("eniq_oss_3"));
		assertTrue(ossidcache.map1.get("eniq_events_2events_oss_3").equalsIgnoreCase("eniq_oss_2"));
		
		assertTrue(ossidcache.map2.get("eniq_events_1eniq_oss_4").equalsIgnoreCase("events_oss_1"));
		assertTrue(ossidcache.map2.get("eniq_events_1eniq_oss_3").equalsIgnoreCase("events_oss_2"));
		assertTrue(ossidcache.map2.get("eniq_events_2eniq_oss_2").equalsIgnoreCase("events_oss_3"));

	}
	
	
	
	@Test
	public void testRevalidate() throws Exception {
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		final OSSIDMappingCache ossidcache = OSSIDMappingCache.getCache();
		
		//Add 1 extra mapping to the mapping file
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ossIdMappingFile, true));
		writer.append("eniq_events_2,events_oss_4,eniq_oss_1");
		writer.flush();
		
		ossidcache.revalidate();
		
		//Check the extra mapping is successfully added to both maps
		assertTrue(ossidcache.map1.size() == 4);
		assertTrue(ossidcache.map2.size() == 4);
		assertTrue(ossidcache.map1.get("eniq_events_2events_oss_4").equalsIgnoreCase("eniq_oss_1"));
		assertTrue(ossidcache.map2.get("eniq_events_2eniq_oss_1").equalsIgnoreCase("events_oss_4"));
	}

	@Test
	public void testGetOssid() throws Exception{
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		final OSSIDMappingCache ossidcache = OSSIDMappingCache.getCache();
		final String eventsId = "eniq_events_1";
		final String eventsossid = "events_oss_2";
		final String expected = "eniq_oss_3";
		
		final String result = ossidcache.getOssid(eventsId, eventsossid);
		
		assertTrue(result.equalsIgnoreCase(expected));
	}
	
	@Test
	public void testGetAllMappingForEventsid() throws Exception{
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		final OSSIDMappingCache ossidcache = OSSIDMappingCache.getCache();
		
		final HashMap<String,String> mapEvents1 = ossidcache.getAllMappingForEVENTSId("eniq_events_1");
		final HashMap<String,String> mapEvents2 = ossidcache.getAllMappingForEVENTSId("eniq_events_2");
		
		assertTrue(mapEvents1.size()==2);
		assertTrue(mapEvents2.size()==1);
		mapEvents1.get("events_oss_1").equalsIgnoreCase("eniq_oss_4");
		mapEvents1.get("events_oss_2").equalsIgnoreCase("eniq_oss_3");
		mapEvents2.get("events_oss_3").equalsIgnoreCase("eniq_oss_2");
	}
	
	@Test
	public void testGetAllMappingEventsAsKey() throws Exception{
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		final OSSIDMappingCache ossidcache = OSSIDMappingCache.getCache();
		
		final HashMap<String,HashMap<String,String>> map = ossidcache.getAllMappingEventsAsKey();
		
		assertTrue(map.size()==2);
	}
	
	@Test
	public void testGetMappingForOssid() throws Exception{
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		final OSSIDMappingCache ossidcache = OSSIDMappingCache.getCache();
		String[] result = ossidcache.getMappingForOssid("eniq_oss_2");
		assertTrue(result[0].equalsIgnoreCase("eniq_events_2"));
		assertTrue(result[1].equalsIgnoreCase("events_oss_3"));
		assertTrue(result[2].equalsIgnoreCase("eniq_oss_2"));
	}
	
	@Test
	public void testInitializeInvalidFormat1() throws Exception{
		//Add a  mapping to the mapping file that has invalid format - too many entries
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ossIdMappingFile, true));
		writer.append("eniq_events_2,events_oss_3,eniq_oss_4,eniq_oss_5"); //
		writer.flush();
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		assertTrue(null==OSSIDMappingCache.getCache());
	}
	
	@Test
	public void testInitializeInvalidFormat2() throws Exception{
		//Add a  mapping to the mapping file that has invalid format - too few entries
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ossIdMappingFile, true));
		writer.append("eniq_events_2,events_oss_3");
		writer.flush();
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		assertTrue(null==OSSIDMappingCache.getCache());
	}
	
	@Test
	public void testInitializeInvalidFormat3() throws Exception{
		//Add a  mapping to the mapping file that has invalid format - events id (the 1st entry) doesn't begin with "eniq_events_"
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ossIdMappingFile, true));
		writer.append("blahblah_2,events_oss_3,eniq_oss_4");
		writer.flush();
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		assertTrue(null==OSSIDMappingCache.getCache());
	}
	
	@Test
	public void testInitializeInvalidFormat4() throws Exception{
		//Add a  mapping to the mapping file that has invalid format - events oss id (the 2nd entry) doesn't begin with "events_oss_"
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ossIdMappingFile, true));
		writer.append("eniq_events_2,zipitidooda_3,eniq_oss_4");
		writer.flush();
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		assertTrue(null==OSSIDMappingCache.getCache());
	}
	
	@Test
	public void testInitializeInvalidFormat5() throws Exception{
		//Add a  mapping to the mapping file that has invalid format -  oss id (the 3rd entry) doesn't begin with "oss_"
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ossIdMappingFile, true));
		writer.append("eniq_events_2,events_oss_3,yipi_eye_yay_moth...");
		writer.flush();
		OSSIDMappingCache.initialize(ossIdMappingFile.getPath());
		assertTrue(null==OSSIDMappingCache.getCache());
	}

}
