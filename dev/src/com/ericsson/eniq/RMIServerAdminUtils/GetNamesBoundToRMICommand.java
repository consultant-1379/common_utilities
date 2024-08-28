package com.ericsson.eniq.RMIServerAdminUtils;

import java.util.ArrayList;
import java.util.List;

import com.distocraft.dc5000.common.ENIQRMIRegistryManager;
import com.ericsson.eniq.common.RMIServiceAdmin;

public class GetNamesBoundToRMICommand
  extends Command
{
  private String hostname;
  private int port;
  
  public GetNamesBoundToRMICommand(String[] args)
  {
    super(args);
  }
  
  @Override
  void checkAndConvertArgumentTypes()
    throws IllegalArgumentException
  {
    this.hostname = this.arguments[1];
    try
    {
      this.port = new Integer(this.arguments[2]).intValue();
    }
    catch (NumberFormatException e)
    {
      throw new IllegalArgumentException("Port number must be of type integer, usage: " + 
        getUsageMessage());
    }
  }
  
  @Override
  void checkNumberOfArguments()
    throws IllegalArgumentException
  {
    if (this.arguments.length != 3) {
      throw new IllegalArgumentException("Incorrect number of arguments supplied, usage:" + 
        getUsageMessage());
    }
  }
  
  @Override
  String getUsageMessage()
  {
    return "getNameBoundToRMI <hostname> <port number> .";
  }
  
  @Override
  public void performCommand()
    throws Exception
  {
    this.getNamesBoundToRMI(this.hostname, this.port);
  }
  
  @Override
  protected int getCorrectArgumentsLength()
  {
    return 0;
  }
  
  public void getNamesBoundToRMI(String _host,int _port) throws Exception {
    List<String> RMIServiceUserList = new ArrayList<String>();
    ENIQRMIRegistryManager rmiManager = new ENIQRMIRegistryManager(_host, _port);
    RMIServiceUserList = rmiManager.getRMIServiceUsers();
    if(!RMIServiceUserList.isEmpty()) {
        System.out.println("Below are the list of RMI services running on the host:"+_host+""
                + " and port:"+_port+",");
        for(int index=0;index < RMIServiceUserList.size();index++)
            System.out.println("RMIService"+index+"::"+RMIServiceUserList.get(index));
    } else {
        System.out.println("No users has found on the RMI services running on the host:"+_host+""
                + " and port:"+_port+"!!");
    }	
  }
  
}
