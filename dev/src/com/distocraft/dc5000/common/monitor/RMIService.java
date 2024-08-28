package com.distocraft.dc5000.common.monitor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIService extends Remote {
  /**
   * Service Monitor will call this method on monitored RMI services to determine if
   * the service is actually available or not (checking if a Object is bound in the RmiRegistry is not enough)
   *
   * This method should return without doing anything i.e. implement as
   * <code>public void ping(){} throws RemoteException</code>
   *
   * @throws RemoteException
   */
  void ping() throws RemoteException;
}
