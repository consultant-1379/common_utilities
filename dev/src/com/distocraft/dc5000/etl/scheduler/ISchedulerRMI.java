package com.distocraft.dc5000.etl.scheduler;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ISchedulerRMI extends Remote {

  /**
   * Activates Scheduler
   * 
   * @throws RemoteException
   */
  void reload() throws RemoteException;

  /**
   * Activates Scheduler
   * 
   * @throws RemoteException
   */
  void reloadLoggingProperties() throws RemoteException;
 
  
  /**
   * Deactivates Scheduler
   * 
   * @throws RemoteException
   */
  void hold() throws RemoteException;

  /**
   * Terminates Scheduler
   * 
   * @throws RemoteException
   */
  void shutdown() throws RemoteException;
  
  /**
   * Returns Scheduler status
   */
  List<String> status() throws RemoteException;

  /**
   * Triggers a set in scheduler
   * 
   * @param name
   * 	name of the set to be started
   * @throws RemoteException
   */
  void trigger(String name) throws RemoteException;

  
  /**
   * Triggers a list of sets in scheduler
   * 
   * @param name
   *    name of the set to be started
   * @throws RemoteException
   */
  @Deprecated
  void trigger(List<String> list) throws RemoteException;
  
  /**
   * Triggers a another set with parameters in scheduler
   * 
   * @param obj
   * @throws RemoteException
   */
  void trigger(List<String> list, Map<String, String> map) throws RemoteException;
  
  /**
   * Triggers a set in scheduler
   * 
   * @param name
   * 	name of the set to be started
   * @throws RemoteException
   */
  void trigger(String name, String command) throws RemoteException;

  
}