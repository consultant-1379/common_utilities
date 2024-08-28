package com.ericsson.eniq.RMIServerAdminUtils;

import com.ericsson.eniq.common.RMIServiceAdmin;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.StringTokenizer;

public abstract class Command
{
  final String[] arguments;
  
  public Command(String[] args)
  {
    this.arguments = args;
  }
  
  public Command()
  {
    this.arguments = null;
  }
  
  public void validateArguments()
    throws IllegalArgumentException
  {
    checkNumberOfArguments();
    checkAndConvertArgumentTypes();
  }
  
  void checkNumberOfArguments()
    throws IllegalArgumentException
  {
    int correctArgumentsLength = getCorrectArgumentsLength();
    if (this.arguments.length != correctArgumentsLength) {
      throw new IllegalArgumentException("Incorrect number of arguments supplied! " + 
        getUsageMessage());
    }
  }
  
  abstract void checkAndConvertArgumentTypes()
    throws IllegalArgumentException;
  
  long convertArgumentToLong(String argumentAsString)
    throws IllegalArgumentException
  {
    try
    {
      return Long.parseLong(argumentAsString);
    }
    catch (NumberFormatException e)
    {
      throw new IllegalArgumentException("Invalid arguments type!" + getUsageMessage());
    }
  }
  
  int convertArgumentToInteger(String argumentAsString)
    throws IllegalArgumentException
  {
    try
    {
      return Integer.parseInt(argumentAsString);
    }
    catch (NumberFormatException e)
    {
      throw new IllegalArgumentException("Invalid arguments type!" + getUsageMessage());
    }
  }
  
  abstract String getUsageMessage();
  
  public abstract void performCommand()
    throws Exception;
  
  protected RMIServiceAdmin createNewRMIServiceAdmin()
  {
    return new RMIServiceAdmin();
  }
  
  protected abstract int getCorrectArgumentsLength();
  
  String createPropertyString(String str)
    throws IllegalArgumentException
  {
    String result = "";
    try
    {
      Properties prop = new Properties();
      StringTokenizer st = new StringTokenizer(str, "= ");
      while (st.hasMoreTokens())
      {
        String key = st.nextToken();
        String value = st.nextToken();
        if (key.equals("aggDate"))
        {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try
          {
            if (sdf.parse(value).getTime() < 0L) {
              throw new ParseException("aggDate Value should be in 'yyyy-MM-dd'", 0);
            }
            value = Long.toString(sdf.parse(value).getTime());
          }
          catch (ParseException e)
          {
            throw new NumberFormatException("Please check the aggDate value: " + value + 
              ". It should be in 'yyyy-MM-dd' format");
          }
        }
        prop.setProperty(key.trim(), value.trim());
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      prop.store(baos, "");
      
      result = baos.toString();
    }
    catch (NumberFormatException e)
    {
      throw new IllegalArgumentException(e.getMessage());
    }
    catch (Exception e)
    {
      System.out.println("Warning: Invalid argument Schedule. Usage: engine -e " + getUsageMessage());
      System.out.println("Using default value for Schedule to execute the command.");
    }
    return result;
  }
}
