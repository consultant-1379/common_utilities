package com.distocraft.dc5000.common;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Class to create and get rmireistry on provided hostname and port
 * @author eanguan
 *
 */
public class ENIQRMIRegistryManager {
	private final static Logger log = Logger.getLogger("com.distocraft.dc5000.common.ENIQRMIRegistryManager");
	
	private String _hostName ;
	private int _portNumber ;
	
	/**
	 * Constructor
	 * @param host - host name on which rmi operation needs to be carried out
	 * @param port - port number on which rmi operation needs to be carried out
	 */
	public ENIQRMIRegistryManager(final String host, final int port){
		_hostName = host ;
		_portNumber = port ;
		System.setProperty("sun.rmi.transport.tcp.responseTimeout", "540000");
	}
	
	/**
	 * 
	 * @return - returns registry object running on the given hostname and port number. If no registry was running then returns null
	 * @throws RemoteException
	 */
	public Registry getRegistry() throws RemoteException{
		log.info("Starts getting RMI Regsitry on host: " + _hostName + " on Port: " + _portNumber);
		Registry rmi = null ;
		try{
			rmi = LocateRegistry.getRegistry(_hostName, _portNumber);
			log.info("RMI Regsitry got successful on host: " + _hostName + " on Port: " + _portNumber);
		}catch(final RemoteException e){
			log.log(Level.WARNING, "failed to get RMI Regsitry on host: " + _hostName + " on Port: " + _portNumber, e);
			throw e ;
		}
		log.info("Stops getting RMI Regsitry on host: " + _hostName + " on Port: " + _portNumber);
		return rmi;
	}
	
	/**
	 * 
	 * @return - returns newly created registry object on the given hostname and port number. Returns null if creation of registry fails
	 * @throws RemoteException
	 */
	public Registry createNewRegistry() throws RemoteException{
		log.info("Starts creating RMI Regsitry on host: " + _hostName + " on Port: " + _portNumber);
		Registry rmi = null ;
		try{
			rmi = LocateRegistry.createRegistry(_portNumber);
			log.info("RMI Regsitry created successful on host: " + _hostName + " on Port: " + _portNumber);
		}catch(final RemoteException e){
			log.log(Level.WARNING, "RMI Regsitry creation failed on host: " + _hostName + " on Port: " + _portNumber, e);
			throw e ;
		}
		log.info("Stops creating RMI Regsitry on host: " + _hostName + " on Port: " + _portNumber);
		return rmi ;
	}
	
	/**
	 * 
	 * @return - returns the list of service names which is registered on the given hostname and port number. 
	 * @throws RemoteException
	 */
	public List<String> getRMIServiceUsers() throws RemoteException {
		List<String> RMIServiceUserList = new ArrayList<String>();
		try
	      {
	         final Registry registry = LocateRegistry.getRegistry(_hostName, _portNumber);
	         RMIServiceUserList.addAll(Arrays.asList(registry.list()));
	      }
	      catch (ConnectException connectEx)
	      {
	         log.severe("ConnectException while retriving RMI registry details from the host="+_hostName+", port="
	            + _portNumber+" ::" + connectEx.toString());
	         throw connectEx;
	      } catch (RemoteException remoteEx)
	      {
	          log.severe("RemoteException while retriving RMI registry details from the host="+_hostName+", port=\": " + remoteEx.toString());
	          throw remoteEx;
	       }
		
		return RMIServiceUserList;
	}
}
