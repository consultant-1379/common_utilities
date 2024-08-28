package com.ericsson.eniq.enminterworking;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xarjsin
 *
 */
public interface IEnmInterworkingRMI extends Remote {

	/**
	 * Gets current role assigned to the server
	 * 
	 * @return Current role of server
	 * @
	 */
	String getCurrentRole() throws RemoteException;
	
	/**
	 * Updates the server as a Slave server
	 * Also updates master details
	 * 
	 * @param masterHost
	 * @param masterIP
	 * @return Slave engine hostname if successful, FAIL if failed
	 * @
	 */
	String updateSlave(String masterHost, String masterIP) throws RemoteException;
	
	/**
	 * Gets the master details if assigned as a slave server
	 * 
	 * @return Master details
	 * @
	 */
	ResultSet getMaster() throws RemoteException;
	
	/**
	 * Inserts the ENIQS_Node_Assignment table when the master server gets inserted.
	 * 
	 * @param eniqIdentifier
	 * @param fdn
	 * @param neType
	 * @param enmHostName
	 * @return True if successful, false if failed
	 * @
	 */
	public boolean natInsert(String eniqIdentifier,String fdn ,String neType,String enmHostName) throws RemoteException;
			
	/**
	 * Inserts the ENIQS_Policy_Criteria table when the user inputs the policy.
	 * 
	 * @param technology
	 * @param namingConvention
	 * @param eniqIdentifier
	 * @param enmHostName
	 * @return True if successful, false if failed
	 * @
	 */
	public boolean policyCriteriaInsert(String technology, String namingConvention, 
			String eniqIdentifier, String enmHostName) throws RemoteException;
	
		
	/**
	 * Updates the ENIQS_Policy_Criteria when the user edits the policies.
	 * 
	 * @param technology
	 * @param regexString
	 * @param identifier
	 * @param enmHostName
	 * @param oldIdentifier
	 * @param oldTechnology
	 * @param oldNaming
	 * @param oldEnmHostName
	 * @return True if successful, false if failed
	 * @
	 */
	
	public boolean policyCriteriaUpdate(String... values) throws RemoteException;
	
	/**
	 * Deletes the ENIQS_Policy_Criteria when the user deletes the policies.
	 * 
	 * @param technology
	 * @param regexString
	 * @param identifier
	 * @param enmHostName
	 * @return True if successful, false if failed
	 * @
	 */
	public boolean deletePolicyCriteria(String[] values) throws RemoteException;
	/**
	 * Updates the ENIQS_Node_Assignment when the user tries manually to assign the nodes.
	 * 
	 * @param eniqIdentifier
	 * @param fdn
	 * @return True if successful, false if failed
	 * @
	 */
	public boolean natUpdate(String eniq_identifier, String nodeFDN , String enmHostName) throws RemoteException ;
	
	/**
	 * Returns true of FlsConf file is present (integrated with ENM).
	 * @deprecated Using this method to determine FLS service status is discouraged.
	 * Use IsflsServiceEnabled() instead.
	 * 
	 * @return True if file exists
	 * 
	 */
	@Deprecated
	public boolean flsConfExists() throws RemoteException;

	/**
	 * Method to check whether the FLS service is enabled or not.
	 * 
	 * @return true is at least one entry is present in fls_conf file. Else returns false.
	 * @throws RemoteException
	 */
	public boolean IsflsServiceEnabled() throws RemoteException;
	
	/**
	 * Method to check whether the FLS service is enabled or not.
	 * 
	 * @param ossId
	 * @return
	 * @throws RemoteException
	 */
	public boolean IsflsServiceEnabled(String ossId) throws RemoteException;
	
	/**
	 * This Method facilitates the addition of given details into ENIQs_Node_Assignment table.
	 * 
	 * @param node_type
	 * @param node_fdn
	 * @param ENM_hostname
	 * @throws RemoteException
	 */
	public void addingToBlockingQueue(String node_type, String node_fdn, String ENM_hostname) throws RemoteException;
	
	/**
	 * This Method facilitates the addition of given details into ENIQs_Node_Assignment table.
	 * 
	 * @param node_type
	 * @param node_fdn
	 * @param ENM_hostname
	 * @param mixedNodeTechnologies
	 * @throws RemoteException
	 */
	public void addingToBlockingQueue(String node_type, String node_fdn, String ENM_hostname, String mixedNodeTechnologies) throws RemoteException;
	
	/**
	 * This Method facilitates the addition of given details into ENIQs_Node_Assignment table.
	 * 
	 * @param node_type
	 * @param node_fdn
	 * @param ENM_hostname
	 * @param deletePolicy
	 * @throws RemoteException
	 */
	public void addingToBlockingQueue(String node_type, String node_fdn, String ENM_hostname, boolean deletePolicy) throws RemoteException;
	
	/**
	 * Method to check whether the FLS have to be monitored.
	 * 
	 * @param ossId
	 * @return true if fls_admin_flag is set and false otherwise
	 * @throws RemoteException
	 */
	public boolean isFlsMonitorOn(String ossId) throws RemoteException;
	
	/**
	 * Method to get list of ENMs for which the FLS have to monitored.
	 * 
	 * @return List of ENM aliases for which the fls_admin_flag is set.
	 */
	public List<String> getFlsToMonitorList() throws RemoteException;
			
	/**
	 * Method to query FLS from admin UI.
	 * 
	 * @param ossId
	 * @param flsUserStartDateTime
	 * @throws RemoteException
	 */
	public void adminuiFlsQuery(String ossId, String flsUserStartDateTime) throws RemoteException;
	
	/**
	 * Method to return list of ENMs for which the FLS is enabled
	 * 
	 * @return List containing FLs enabled ENM aliases
	 */
	public List<String> getFlsEnabledEnms() throws RemoteException;
	
	public void refreshNodeAssignmentCache() throws RemoteException;
	
	public void shutDownMain() throws RemoteException;
	
	/**
	 * Clears RoleTable to unassign server
	 * 
	 * @return Current role of server
	 * @
	 */
	String unAssignSelfSlave() throws RemoteException;

	/**
	 * Deletes row in master RoleTable with Slave entry
	 *  to unassign server
	 * 
	 * @return Current role of server
	 * @
	 */
	String unAssignSpecSlave(String engineHostname, String engineIP) throws RemoteException;
		
	public ArrayList<String> MasterNATDetail()throws RemoteException;
	
	/**
	 * Method to display the detailed status of FLS process
	 * 
	 * @return status of FLS process
	 * @throws RemoteException
	 */
	public List<List<String>> status() throws RemoteException;
			
	/**
	 * Method to change FLS active profile.
	 * 
	 * @param ossId
	 * @param profileName
	 * @return
	 * @throws RemoteException
	 */
	public List<Boolean> changeProfile(String ossId, String profileName) throws RemoteException;
	
	/**
	 * 
	 * @param oldHostName
	 * @param oldIpAddress
	 * @param newHostName
	 * @param newIpAddress
	 * @return true if successful, false if failed
	 * @throws RemoteException
	 */
	public boolean updateFlsTables(String oldHostName, String oldIpAddress , String newHostName, String newIpAddress) throws RemoteException;
	
	/**
	 * Method that executes a single sql statement (insert/delete/update)
	 * 
	 * @param sql
	 * @return Returns true if execution is successful and false otherwise.
	 * @throws RemoteException
	 */
	public boolean executeSql(String sql) throws RemoteException ;
	
	/**
	 * Method that returns the OSS alias to host name mapping
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Map<String, String> getOssIdToHostNameMap() throws RemoteException;
	
	/**
	 * Retains the nodes present in the provided input file
	 * 
	 * @param inputFile
	 * @throws RemoteException
	 */
	public void retainNodes(String inputFile, String enmHostName) throws RemoteException;
	
	/**
	 * This Method facilitates the addition of given details into ENIQs_Node_Assignment table.
	 * 
	 * @param node_type
	 * @param node_fdn
	 * @param ENM_hostname
	 * @param isSingleConnectedEnmForSlave
	 * @throws RemoteException
	 */
	public void addingToBlockingQueue(String node_type, String node_fdn, String ENM_hostname, String eniqId, boolean isSingleConnectedEnmForSlave, boolean isRetention) throws RemoteException;
	
	/**
	 * Updates the SlaveNAT and retain slave nodes on adding a slave server.
	 * 
	 * @param IP Address of the master
	 * @return FAIL on failure and slave's host name on success
	 * @throws RemoteException
	 */
	public String updateSlaveNAT(String masterIP) throws RemoteException;
	
	/**
	 * Method that returns true when the RMI connectivity is success
	 * 
	 * @return 
	 * @throws RemoteException
	 */
	public boolean checkHealth() throws RemoteException;
	
	/**
	 * Starts the HealthMonitor - preferably for Master ENIQ-S
	 * 
	 * @throws RemoteException
	 */
	public void startHealthMonitor() throws RemoteException;
	
	/**
	 * Method that returns the technology type for the supplied mixed node.
	 * 
	 * @param nodeFdn
	 * @param ossId
	 * @throws RemoteException
	 */
	public String getMixedNodeTechnologyType(String nodeFdn, String ossId) throws RemoteException;
	
			
}