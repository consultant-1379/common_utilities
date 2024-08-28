/**
 * 
 */
package com.ericsson.eniq.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This is A cache that provides a mapping between OSSIDs on local ENIQ Stats server and OSSIDs on mounted ENIQ EVENTS servers.
 * Mapping is read from a file. The file is to be an text file formatted as shown in this example:
 * eniq_events_1,events_oss_1,eniq_oss_3
 * eniq_events_1,events_oss_2,eniq_oss_4
 * eniq_events_1,events_oss_3,eniq_oss_1
 * eniq_events_1,events_oss_4,eniq_oss_2
 * eniq_events_2,events_oss_1,eniq_oss_3
 * eniq_events_2,events_oss_2,eniq_oss_2
 * 
 * @author edeamai
 *
 */
public class OSSIDMappingCache {

	public static OSSIDMappingCache ossidcache = null;
	public File ossMappingFile;
	public HashMap<String,String> map1;
	public HashMap<String,String> map2;
	public HashMap<String,HashMap<String,String>> map3;
	public HashMap<String,String[]> map4;
	public HashMap<String,String[]> map5;
	//Id position in mapping file:
	public static int OSS = 2;
	public static int EVENTS = 0;
	public static int EVENTS_OSS = 1;
		
	private static boolean initializing = true;
	private Logger log = Logger.getLogger("OSSIDMappingCache");
	

	public static OSSIDMappingCache getCache() {
		return ossidcache;
	}

	/**
	 * Creates an instance of OSSIDMappingCache, pointed to by static field ossidcache, and Initializes it's fields. Then populates cache from given location
	 * @param filePath is a String indicates path to file containing the OSSID Mapping. 
	 * @throws Exception
	 */
	public static void initialize(String filePath) throws Exception {
		initializing = true;
		ossidcache = new OSSIDMappingCache();
		ossidcache.ossMappingFile = new File(filePath);
		ossidcache.map1 = new HashMap<String,String>();
		ossidcache.map2 = new HashMap<String,String>();
		ossidcache.map3 = new HashMap<String,HashMap<String,String>>();
		ossidcache.map4 = new HashMap<String,String[]>();
		ossidcache.revalidate();
		initializing = false;
	}

	/**
	 * Populates the cache from a file specified by field ossMappingFile.
	 * @throws Exception
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void revalidate() {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ossMappingFile)));
			String line = reader.readLine(); //Read in first mapping from file.
			while (line!=null) {
				final String[] ids = line.split(",");
				if (ids.length != 3 || !ids[EVENTS].startsWith("eniq_events_") || !ids[EVENTS_OSS].startsWith("events_oss_") || !ids[OSS].startsWith("eniq_oss_")) {
					throw new Exception("This line read from OSSID mapping file has invalid format: \""+line+"\"");
				}
				//Concatenate the events id and events_oss id, and map them to the oss id:
				map1.put(ids[EVENTS]+ids[EVENTS_OSS], ids[OSS]);
				//Concatenate the events id with the oss id, and map to the events oss id:
				map2.put(ids[EVENTS]+ids[OSS], ids[EVENTS_OSS]);
				//Make a HashMap that contains a mapping of events id to a sub-HashMap that contains mapping of event oss id to oss id:
				if (!map3.containsKey(ids[0])){
					map3.put(ids[EVENTS], new HashMap<String,String>());
				}				
				map3.get(ids[EVENTS]).put(ids[EVENTS_OSS], ids[OSS]);
				//Make a HashMap that contains a mapping of oss id to a sub-HashMap that contains mapping of event id to events oss id:
				if ( initializing==true  &&  null!=map4.get(ids[OSS]) ){
					log.warning("A duplicate OSSID ("+ids[OSS]+") was found in: "+ossMappingFile.getPath()+"  Please check the file and reload if necessary.");
				}
				map4.put(ids[OSS], ids);
				log.info("This OSSID mapping has been put in cache: "+ids[EVENTS]+" - "+ids[EVENTS_OSS]+" - "+ids[OSS]);
				line = reader.readLine(); //Read in the next mapping from file
			}
		
		} catch (FileNotFoundException e){
			log.info("OSSID Mapping file not found: "+ossMappingFile.getPath()+"  Cache will not be populated. EVENTS Interfaces will not activate.");
			if (initializing){
				ossidcache = null;
			}
		} catch (Exception e){
			log.warning("Exception during population of OSS ID Mapping Cache. Cache will not be populated. EVENTS Interfaces will not activate.");
			e.printStackTrace();
			if (initializing){
				ossidcache = null;
			}
		}
	}
	
	/**
	 * Gets oss id for a given events id and events oss id.
	 * @param eventsId
	 * @param eventsossid
	 * @return oss id as String
	 */
	public String getOssid(String eventsId, String eventsossid){
		final String key = eventsId+eventsossid;
		return map1.get(key);
	}
	
	/**
	 * Gets events oss id for a given events id and oss id 
	 * @param eventsId
	 * @param ossid
	 * @return events oss id as String
	 */
	public String getEventsOssid(String eventsId, String ossid){
		final String key = eventsId+ossid;
		return map2.get(key);
	}
	
	/**
	 * Returns a mapping of event oss id to oss id for a given events id
	 * @param eventsId
	 * @return Mapping for an events id as HashMap of structure: HashMap<String, String>
	 */
	public HashMap<String,String> getAllMappingForEVENTSId(String eventsId){
		return map3.get(eventsId);
	}
	
	/**
	 * Gets the entire OSSID Mapping in the form of a HashMap that contains a mapping of events id to a sub-HashMap that contains mapping of event oss id to oss id:
	 * @return Entire mapping as HashMap with structure: HashMap<String,HashMap<String,String>>  
	 */
	public HashMap<String,HashMap<String,String>> getAllMappingEventsAsKey(){
		return map3;
	}
	
	public String[] getMappingForOssid(String ossid){
		return map4.get(ossid);
	}
	
}
