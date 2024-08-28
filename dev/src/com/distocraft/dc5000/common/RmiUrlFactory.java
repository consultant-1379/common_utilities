package com.distocraft.dc5000.common;
/**
 * @author eanguan
 */
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RmiUrlFactory {
	private final static String defaultRMIPortS = "1200" ;
	private final static int defaultRMIPortI = 1200 ;
	private final static String defaultSchedulerRefName = "Scheduler" ;
	private final static String defaultEngineRefName = "TransferEngine" ;
	private final static String defaultLicRefName = "LicensingCache" ;
	private final static String defaultMultiESRefName = "MultiESController";
	private final static String defaultEniqHost = "localhost" ;
	private final static String engineServiceName = "engine";
	private final static String schedulerServiceName = "scheduler";
	private final static String licServiceName = "licenceservice";
	
	private static final Logger log = Logger.getLogger("com.distocraft.dc5000.etl.gui.common.RmiUrlFactory");
	private static RmiUrlFactory _staticInstance;
	private static Properties appProps ;
	
	/**
	 * Public method to get the singleton object of class RmiUrlFactory
	 * @return RmiUrlFactory
	 */
	public static RmiUrlFactory getInstance(){
		if(_staticInstance == null){
			_staticInstance = new RmiUrlFactory();
		}
		return _staticInstance ;
	}
	
	/**
	 * private Constructor
	 */
	private RmiUrlFactory(){
		getStarted();
	}
	
	/**
	 * Method to load the properties defined in ETLC properties file
	 */
	private void getStarted() {
		String confDir = System.getProperty("dc5000.config.directory","/eniq/sw/conf/");
		if (!confDir.endsWith(File.separator)) {
			confDir += File.separator;
		}
		appProps = new Properties();
		FileInputStream file = null;
		try{
			file = new  FileInputStream(confDir + "ETLCServer.properties");
			appProps.load(file);
			
		}catch(final Exception e){
			log.log(Level.SEVERE, "Failed to read the ETLC properties file. ", e);
		}finally{
			try{
				file.close();
			}catch(final Exception e){
				//Don't do anything
			}
		}		
		//String serverHostName = appProps.getProperty("ENGINE_HOSTNAME");
	}


	/**
	 * To get the RMI URL for engine
	 * @return String: engine RMI URL
	 */
	public String getEngineRmiUrl(){
		log.finest("Begin: getEngineRmiUrl");
		String rmiURL = null ;
		String engineHostName = appProps.getProperty("ENGINE_HOSTNAME");
		if(engineHostName == null || engineHostName.isEmpty()){
			log.finest("Engine host name is not defined in ETLC properties file.");
			//Get from service name of engine
			try{
				log.finest("Getting Engine host name via service name: " + engineServiceName);
				engineHostName = ServicenamesHelper.getServiceHost(engineServiceName, defaultEniqHost);
			}catch(final Exception e){
				log.finest("Exception comes while getting Engine host name via service name. Setting it to default hostname: " + defaultEniqHost);
				engineHostName = defaultEniqHost ;
			}
		}
		log.finest("Engine host name set as: " + engineHostName);
		
		final String enginePortS = appProps.getProperty("ENGINE_PORT", defaultRMIPortS);
		int enginePortI = defaultRMIPortI ;
		try {
			enginePortI = Integer.parseInt(enginePortS);
		}catch (final Exception e) {
			enginePortI = defaultRMIPortI ;
		}
		log.finest("Engine RMI port set as: " + enginePortI);

		final String engineRefName = appProps.getProperty("ENGINE_REFNAME", defaultEngineRefName);
		log.finest("Engine Refrence Name set as: " + enginePortI);
		
		rmiURL = "//" + engineHostName + ":" + enginePortI + "/" + engineRefName ;
		log.info("Created RMI URL for engine : " + rmiURL);
		log.finest("End: getEngineRmiUrl");
		return rmiURL;
	}
	
	/**
	 * To get the RMI URL for scheduler
	 * @return String: scheduler RMI URL
	 */
	public String getSchedulerRmiUrl(){
		log.finest("Begin: getSchedulerRmiUrl");
		String rmiURL = null ;
		String schedulerHostName = appProps.getProperty("SCHEDULER_HOSTNAME");
		if(schedulerHostName == null || schedulerHostName.isEmpty()){
			log.finest("Scheduler host name is not defined in ETLC properties file.");
			//Get from service name of engine
			try{
				log.finest("Getting Scheduler host name via service name: " + schedulerServiceName);
				schedulerHostName = ServicenamesHelper.getServiceHost(schedulerServiceName, defaultEniqHost);
			}catch(final Exception e){
				log.finest("Exception comes while getting Scheduler host name via service name. Setting it to default hostname: " + defaultEniqHost);
				schedulerHostName = defaultEniqHost ;
			}
		}
		log.finest("Scheduler host name set as: " + schedulerHostName);
		
		final String schedulerPortS = appProps.getProperty("SCHEDULER_PORT", defaultRMIPortS);
		int schedulerPortI = defaultRMIPortI ;
		try {
			schedulerPortI = Integer.parseInt(schedulerPortS);
		}catch (final Exception e) {
			schedulerPortI = defaultRMIPortI ;
		}
		log.finest("Scheduler RMI port set as: " + schedulerPortI);

		final String schedulerRefName = appProps.getProperty("SCHEDULER_REFNAME", defaultSchedulerRefName);
		log.finest("Scheduler Refrence Name set as: " + schedulerRefName);
		
		rmiURL = "//" + schedulerHostName + ":" + schedulerPortI + "/" + schedulerRefName ;
		log.info("Created RMI URL for scheduler : " + rmiURL);
		log.finest("End: getSchedulerRmiUrl");
		return rmiURL;
	}
	
	/**
	 * To get the RMI URL for licmgr
	 * @return String: licmgr RMI URL
	 */
	public String getLicmgrRmiUrl(){
		log.finest("Begin: getLicmgrRmiUrl");
		String rmiURL = null ;
		String licHostName = appProps.getProperty("LICENSING_HOSTNAME");
		if(licHostName == null || licHostName.isEmpty()){
			log.finest("Licensing host name is not defined in ETLC properties file.");
			//Get from service name of engine
			try{
				log.finest("Getting Licensing host name via service name: " + licServiceName);
				licHostName = ServicenamesHelper.getServiceHost(licServiceName, defaultEniqHost);
			}catch(final Exception e){
				log.finest("Exception comes while getting Licensing host name via service name. Setting it to default hostname: " + defaultEniqHost);
				licHostName = defaultEniqHost ;
			}
		}
		log.finest("Licensing host name set as: " + licHostName);
		
		final String licPortS = appProps.getProperty("LICENSING_PORT", defaultRMIPortS);
		int licPortI = defaultRMIPortI ;
		try {
			licPortI = Integer.parseInt(licPortS);
		}catch (final Exception e) {
			licPortI = defaultRMIPortI ;
		}
		log.finest("Licensing RMI port set as: " + licPortI);

		final String licRefName = appProps.getProperty("LICENSING_REFNAME", defaultLicRefName);
		log.finest("Licensing Refrence Name set as: " + licRefName);
		
		rmiURL = "//" + licHostName + ":" + licPortI + "/" + licRefName ;
		log.info("Created RMI URL for Licensing : " + rmiURL);
		log.finest("End: getLicmgrRmiUrl");
		return rmiURL;
	}
	
	/**
	 * To get the RMI URL for RAT/NAT Slave servers
	 * @return String: MultiES RMI URL
	 */
	public String getMultiESRmiUrl(String ipAdd){
		Logger log1 = Logger.getLogger("symboliclinkcreator.rat");
//		log1.finest("Begin: getMultiESRmiUrl");
		String rmiURL = null ;
		String multiEsHost = ipAdd;
		
//		log1.finest("Slave server IP Address set as: " + multiEsHost);
		
		final String multiESPortS = appProps.getProperty("MULTI_ES_PORT", defaultRMIPortS);
		int multiESPortI = defaultRMIPortI ;
		try {
			multiESPortI = Integer.parseInt(multiESPortS);
		}catch (final Exception e) {
			multiESPortI = defaultRMIPortI ;
		}
//		log1.finest("Multi ES RMI port set as: " + multiESPortI);

		final String multiESRefName = appProps.getProperty("MULTI_ES_REFNAME", defaultMultiESRefName);
//		log1.finest("Slave Refrence Name set as: " + multiESRefName);
		
		if(multiEsHost.contains(":")) {
			rmiURL = "//[" + multiEsHost + "]:" + multiESPortI + "/" + multiESRefName ;
		}else {
		rmiURL = "//" + multiEsHost + ":" + multiESPortI + "/" + multiESRefName ;
		}
//		log1.finest("Created RMI URL for Slave : " + rmiURL);
//		log1.finest("End: getMultiESRmiUrl");
		return rmiURL;
	}

}
