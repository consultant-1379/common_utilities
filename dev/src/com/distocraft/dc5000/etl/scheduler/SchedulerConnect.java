package com.distocraft.dc5000.etl.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Properties;import java.util.logging.Logger;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.distocraft.dc5000.common.ServicenamesHelper;import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;

public class SchedulerConnect {
	private static final Logger log = Logger.getLogger("com.distocraft.dc5000.etl.scheduler.SchedulerConnect");
	private SchedulerConnect() {
		
	}
	
	/**
	 * Returns Engine RMI object
	 */
	public static ITransferEngineRMI connectEngine() throws IOException, NotBoundException {
//		String sysPropDC5000 = System.getProperty("dc5000.config.directory","/eniq/sw/conf/");
//    if (!sysPropDC5000.endsWith(File.separator)) {
//      sysPropDC5000 += File.separator;
//    }
//
//    final Properties appProps = new Properties();
//    
//    FileInputStream file = null;
//    try {
//    	file = new  FileInputStream(sysPropDC5000 + "ETLCServer.properties");
//      appProps.load(file);
//    } finally {
//    	try {
//    		file.close();
//    	} catch(Exception e) {
//    	}
//    }
//    
//    String serverHostName = appProps.getProperty("ENGINE_HOSTNAME");
//    if (serverHostName == null) { // trying to determine hostname
//      serverHostName = "localhost";
//      //20120213 EANGUAN:: To get the hostname based upon the service name :: change for SMF/HSS IP      
//      try{
//    	  serverHostName = ServicenamesHelper.getServiceHost("engine", "localhost");  
//      }catch(final Exception e){    	  
//    	  serverHostName = "localhost" ;
//      }
////      try {//
////        serverHostName = InetAddress.getLocalHost().getHostName();//
////      } catch (java.net.UnknownHostException ex) {//
////        // default to localhost//
////      }
//    }
//
//    int serverPort = 1200;
//    final String sporttmp = appProps.getProperty("ENGINE_PORT", "1200");
//    try {
//      serverPort = Integer.parseInt(sporttmp);
//    } catch (NumberFormatException nfe) {
//      // default to 1200
//    }
//
//    final String serverRefName = appProps.getProperty("ENGINE_REFNAME", "TransferEngine");
//    final String rmiURL = "//" + serverHostName + ":" + serverPort + "/" + serverRefName ;
//	log.info("Creating RMI connection to engine with URL: " + rmiURL);
    return (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
  }
	
	/**
	 * Returns Scheduler RMI object
	 */
	public static ISchedulerRMI connectScheduler() throws IOException, NotBoundException {
//		String sysPropDC5000 = System.getProperty("dc5000.config.directory","/eniq/sw/conf/");
//    if (!sysPropDC5000.endsWith(File.separator)) {
//      sysPropDC5000 += File.separator;
//    }
//
//    final Properties appProps = new Properties();
//    
//    FileInputStream file = null;
//    try {
//    	file = new  FileInputStream(sysPropDC5000 + "ETLCServer.properties");
//      appProps.load(file);
//    } finally {
//    	try {
//    		file.close();
//    	} catch(Exception e) {
//    	}
//    }
//    
//    String serverHostName = appProps.getProperty("SCHEDULER_HOSTNAME");
//    if (serverHostName == null) { // trying to determine hostname
//      serverHostName = "localhost";
//      //20120213 EANGUAN:: To get the hostname based upon the service name :: change for SMF/HSS IP
//      try{
//    	  serverHostName = ServicenamesHelper.getServiceHost("scheduler", "localhost");
//      }catch(final Exception e){
//    	  serverHostName = "localhost" ;
//      }      
////      try {//
////        serverHostName = InetAddress.getLocalHost().getHostName();//
////      } catch (java.net.UnknownHostException ex) {//
////        // default to localhost//
////      }
//    }
//
//    int serverPort = 1200;
//    final String sporttmp = appProps.getProperty("SCHEDULER_PORT", "1200");
//    try {
//      serverPort = Integer.parseInt(sporttmp);
//    } catch (NumberFormatException nfe) {
//      // default to 1200
//    }
//
//    final String serverRefName = appProps.getProperty("SCHEDULER_REFNAME", "Scheduler");
//    final String rmiURL = "//" + serverHostName + ":" + serverPort + "/" + serverRefName ;
//    log.info("Creating RMI connection to scheduler with URL: " + rmiURL);
    return (ISchedulerRMI) Naming.lookup(RmiUrlFactory.getInstance().getSchedulerRmiUrl());

  }
	
}
