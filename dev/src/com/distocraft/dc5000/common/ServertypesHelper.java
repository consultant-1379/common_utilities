package com.distocraft.dc5000.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServertypesHelper {

	public static final String COORDINATOR_NAME = "Co-ordinator";
	public static final String ENGINE_NAME = "Engine";
	public static final String IQR_NAME = "Reader";
	public static final String MEDIATION_NAME = "Mediation Server";
	public static final String UI_NAME = "Presentation";
	public static final String STATS_NAME = "Eniq Stats";
	public static final String EVENTS_NAME = "Eniq Events";
	public static final String UNKNOWN_SERVER_TYPE = "Unknown";

	/**
	 * server_types regex matcher/splitter, Format is <ip_address>::<hostname>::<servertype>
	 */
	private static final String GROUP_REGEX = "(.*)::(.*)::(.*)";
	/**
	 * Splitter helper
	 */
	private static final Pattern splitter = Pattern.compile(GROUP_REGEX);
	/**
	 * Default instance
	 */
	private static ServertypesHelper helper = new ServertypesHelper();
	
	private static Map<String, ServerTypeDetails> serverTypeDetails = null;
	
	
	
	/**
	 * Get the server_types file location, defaults to ${CONF_DIR}/server_types
	 *
	 * @return server_types file
	 * @throws FileNotFoundException if the server_types file can't be found
	 */
	public static File getServertypesFile() throws FileNotFoundException {
		final File file = new File(System.getProperty("CONF_DIR", "/eniq/sw/conf"),
				System.getProperty("server_types", "server_types"));
		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}
		return file;
	}

	/**
	 * Parse the server_types file
	 *
	 * @return .
	 * @throws IOException If the file isnt found or cant be read
	 */
	private Map<String, ServerTypeDetails> parseServerTypes() throws IOException {
		final File ServerTypesFile = getServertypesFile();
		final BufferedReader reader = new BufferedReader(new FileReader(ServerTypesFile));
		String line;
		final Map<String, ServerTypeDetails> servers = new HashMap<String, ServerTypeDetails>();
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.length() == 0) {
					// comment|empty line...
					continue;
				}
				final Matcher matcher = splitter.matcher(line);
				if (matcher.matches() && matcher.groupCount() == 3) {
					final ServerTypeDetails details = createServerTypeDetails(matcher.group(3), matcher.group(2), matcher.group(1));
					servers.put(details.getHostname(), details);
				}
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				/*-*/
			}
		}
		return servers;
	}

	/**
	 * Return map of server type details keyed on host name
	 * 
	 * If details are already loaded, return the loaded version
	 * otherwise get from server_types file.
	 * 
	 * To force reload, clear the loaded version using setServerTypeDetails(null)
	 *
	 * @return server_types entries
	 * @throws IOException If the server_types file cant be ready
	 */
	public static Map<String, ServerTypeDetails> getServertypeDetails() throws IOException {
		
		if (serverTypeDetails==null) {
			setServerTypeDetails(helper.parseServerTypes());
		} 
		
		return serverTypeDetails;
	}
	
	/** 
	 * Setter for serverTypeDetails 
	 * 
	 * To force reload of details from file, use this to set serverTypeDetails 
	 * to null
	 * 
	 * **/
	public static void setServerTypeDetails(final Map<String, ServerTypeDetails> serverTypeDetails) {
		ServertypesHelper.serverTypeDetails = serverTypeDetails;
	}

	
	private ServerTypeDetails createServerTypeDetails(final String serverType, final String hostname, final String ipAddress){
		    return new ServerTypeDetails(serverType, hostname, ipAddress);
		  }

	/** 
	 * get server type for specified host
	 * 
	 * @param hostname
	 * @throws IOException 
	 */
	public static String getServertype(final String hostname) throws IOException {
		String serverType = null;
		setServerTypeDetails(helper.parseServerTypes());
		 
		final ServerTypeDetails details = serverTypeDetails.get(hostname);
		
		if (details != null) {
			serverType = details.getServerType();
		}
		
		return serverType;
	}
	
	/** 
	 * get display text of server type for specified host
	 * 
	 * @param hostname
	 * @throws IOException 
	 */
	public static String getDisplayServertype(final String hostname) throws IOException {
		String displayServerType = null;
		String serverType = getServertype(hostname); 

		if (serverType == null) {
			displayServerType = UNKNOWN_SERVER_TYPE;
		} else {
			if (serverType.indexOf("_")>=0) {
				serverType = serverType.substring(serverType.indexOf("_")+1);
			}
			
			if (serverType.equals("coordinator")) {
				displayServerType = COORDINATOR_NAME;
			} else if (serverType.equals("iqr")) {
				displayServerType = IQR_NAME;
			} else if (serverType.equals("mz")) {
				displayServerType = MEDIATION_NAME;
			} else if (serverType.equals("ui")) {
				displayServerType = UI_NAME;
			} else if (serverType.equals("engine")) {
				displayServerType = ENGINE_NAME;
			} else if (serverType.equals("stats")) {
				displayServerType = STATS_NAME;
			} else if (serverType.equals("events")) {
				displayServerType = EVENTS_NAME;
			} else {
				displayServerType = serverType;
			}
		}
		
		return displayServerType;
	}
	
	public static void setHelper(final ServertypesHelper newHelper) {
		helper = newHelper;
	}

	public static ServertypesHelper getHelper() {
		return helper;
	}

	/**
	 * Struct for server_types entries
	 */
	public final class ServerTypeDetails {

		private final String hostName;
		private final String ipAddress;
		private final String serverType;

		ServerTypeDetails(final String serverType, final String hostname, final String ipaddress) {
			this.serverType = serverType;
			this.hostName = hostname;
			this.ipAddress = ipaddress;
		}

		/**
		 * Get the physical hostname associated with the serverType
		 *
		 * @return physical hostname associated with the serverType
		 */
		public String getHostname() {
			return hostName;
		}

		/**
		 * Get the ipaddress associated with the serverType
		 *
		 * @return ipaddress associated with the serverType
		 */
		public String getIpAddress() {
			return ipAddress;
		}

		/**
		 * The serverType
		 *
		 * @return serverType
		 */
		public String getServerType() {
			return serverType;
		}

	}
	
}
