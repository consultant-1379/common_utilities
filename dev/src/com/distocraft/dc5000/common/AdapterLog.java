package com.distocraft.dc5000.common;

import com.ericsson.eniq.exception.ConfigurationException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdapterLog extends SessionLogger {

  private static final String colDelimiter = "\t";
  private static final String rowDelimiter = "\n";

  //Date format changed in order to keep the format in load table command and log file in same format
  //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  //Added for TR HR64745
  private static final SimpleDateFormat sdf_e = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  public AdapterLog() throws ConfigurationException, FileNotFoundException {
    super("ADAPTER");
  }

  private void format(final Map<String/*dataid*/, List<String>/*log-entry*/> lines,
                      final Map<String, Object> data) throws Exception {
	  //Testing for FT Observation EU07 starts here
	  //log.finest("Private void Format....");
	  log.finest("Data Map Content....");
	  for(String name:data.keySet())
	  {
		  String key =name.toString();
          Object value =data.get(name).toString();  
          log.finest(key + " " + value);
	  }
	  //Testing for FT Observation EU07 Ends here
 if(data!=null) {		  
    // get the base session information
    final String dateID = (String) data.get("dateID");
    final String sessionID = (String) data.get("sessionID");
    final String batchID = (String) data.get("batchID");
    final String rowStatus = "LOADED";
    final String fileName = (String) data.get("fileName");
    final long sstamp = Long.parseLong((String) data.get("sessionStartTime"));
    final String startStamp =prvDateFmt.get().format(new Date(sstamp));
    final long estamp = Long.parseLong((String) data.get("sessionEndTime"));
    final String endStamp = prvDateFmt.get().format(new Date(estamp));
    final String endStamp_e = sdf_e.format(new Date(estamp)); //Added for TR HR64745
    final String source = (String) data.get("source");
    final String status = (String) data.get("status");
    final long mstamp = Long.parseLong((String) data.get("srcLastModified"));
    final String modifiedStamp = prvDateFmt.get().format(new Date(mstamp));
    final String flag = "0";
    // dateID needed for log-session-adapter is partitioned by dateID
    if (dateID == null || dateID.trim().equalsIgnoreCase("")) {
      throw new Exception("Date id not defined");
    } else {

      // NON-ROP here means dimension, unpartitioned, topology tables or
      // "measurement types"
      final String datetimeForNoneROP = dateID + " 00:00:00.000";

      final StringBuilder baseSessionInfo = new StringBuilder();
      final StringBuilder sessionInfoRow = new StringBuilder();

      // build up part of the row with base session information
      baseSessionInfo.append(sessionID);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(batchID);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(dateID);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(fileName);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(startStamp);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(endStamp);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(source);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(status);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(modifiedStamp);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(flag);
      baseSessionInfo.append(colDelimiter);

      baseSessionInfo.append(rowStatus);
      baseSessionInfo.append(colDelimiter);

      // add base session information into row
      sessionInfoRow.append(baseSessionInfo);

      final Map<String, Map<String, String>> counterVolumes = (Map<String, Map<String, String>>) data.get("counterVolumes");

      if (counterVolumes == null || counterVolumes.size() == 0) {
        // counter volume information columns as empty since no info found
        sessionInfoRow.append(colDelimiter);
        //This is intentional because Load template expects YYYY-MM-DD HH:NN:SS.SSS
        sessionInfoRow.append("1111-11-11 11:11:11.111");
        sessionInfoRow.append(colDelimiter);
        sessionInfoRow.append(colDelimiter);
        sessionInfoRow.append(colDelimiter);

        //Addition of New fields in LOG_SESSION_ADAPTER table,code added t p fix TR HO36998.
        sessionInfoRow.append(colDelimiter);
        sessionInfoRow.append(colDelimiter);
        sessionInfoRow.append(colDelimiter);
        sessionInfoRow.append("1111-11-11 11:11:11.111");
        sessionInfoRow.append(colDelimiter);

        // end of row
        sessionInfoRow.append(rowDelimiter);

        final List<String> dlines;
        if (lines.containsKey(dateID)) {
          dlines = lines.get(dateID);
        } else {
          dlines = new ArrayList<String>();
          lines.put(dateID, dlines);
        }
        dlines.add(sessionInfoRow.toString());

      } else {
        // get all counter volume information and log them with base session
        // information
    	 
    	  
        for (String key : counterVolumes.keySet()) {
          final StringBuilder finalRow = new StringBuilder();
          final StringBuilder counterVolumeCols = new StringBuilder();

          final Map<String, String> counterVolumeInfo = counterVolumes.get(key);
          
          log.finest("CounterVolumeInfo");
          for(String name:counterVolumeInfo.keySet())
    	  {
    		  String keys =name.toString();
              String values =counterVolumeInfo.get(name).toString();  
              log.finest(keys + " " + values);
    	  }

          final String typeName = counterVolumeInfo.get("typeName");
          counterVolumeCols.append(typeName);
          counterVolumeCols.append(colDelimiter);
        //Addition for TR HS63535 starts here
          String ropStarttime =counterVolumeInfo.get("ropStarttime");
          log.finest("Before matching pattern ropstarttime="+ropStarttime);
          Pattern patternTillSec = Pattern.compile("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])\\s([01]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])");
          Pattern patternTillMin = Pattern.compile("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])\\s([01]?[0-9]|2[0-3]):[0-5][0-9]");
          Matcher matchedForPatternTillSec = patternTillSec.matcher(ropStarttime);
          Matcher matchedForPatternTillMin = patternTillMin.matcher(ropStarttime);
          if (matchedForPatternTillSec.matches()) {
           log.finest(" matchedForPatternTillSec found");
          ropStarttime = ropStarttime + ".000";
          } else if (matchedForPatternTillMin.matches()) {
           log.finest(" matchedForPatternTillMin found");
          ropStarttime = ropStarttime + ":00" + ".000";
          } else {
          log.finest("Invalid time" + ropStarttime);				
          }
          //Addition for TR HS63535 ends here
          //Addition for TR HR70452 starts here
          String timelevel = counterVolumeInfo.get("timelevel");
          //Addition of TR HS68397 Start here
          int iRop = 0;
          int minutes = 60; 
          log.finest(" Adapter timelevel : " + timelevel);
          //iRop = getROP(typeName);
          if(!"".equalsIgnoreCase(timelevel) && timelevel != null){
        	  
        	  if(timelevel.contains("MIN"))
        	  {      
        	  timelevel = timelevel.substring(0, timelevel.length()-3);
        	  log.finest("TimeLevel="+timelevel);
        	  iRop = Integer.parseInt(timelevel);
        	  log.finest(" Adapter ROP : " + iRop);
        	  }
        	  else if (timelevel.contains("HOUR"))
        	  {
        		  timelevel = timelevel.substring(0, timelevel.length()-4);
    			  if(timelevel.equalsIgnoreCase(""))
 			       {
 		    		  iRop = minutes;
 		    		  log.finest("iRop =" + iRop);
 			      }
    			  else
    			  {		    	   				  
					  iRop = Integer.parseInt(timelevel);
					  iRop = minutes*iRop;
					  log.finest("iRop : " + iRop);
					  
    			  }
        	  }
        	  
        	  
	      }
          //Addition of TR HS68397 End here
          Calendar calObj = Calendar.getInstance();
          String ropEndTime = null;
          
          if(iRop != 0){
        	  log.finest("After matching pattern ropstarttime="+ropStarttime);//Addition for TR HS63535
	          calObj.setTime(prvDateFmt.get().parse(ropStarttime));
	          calObj.set(Calendar.MINUTE,calObj.get(Calendar.MINUTE)+iRop);
	          ropEndTime = prvDateFmt.get().format(new Date(calObj.getTimeInMillis()));
          }
          log.finest(" Adapter ropEndTime : " + ropEndTime);
          //Addition for TR HR70452 ends here
          
          log.log(Level.FINER,"sesssion_id="+sessionID+",typename="+typeName+",rop_starttime="+ropStarttime+",rop_endtime="+ropEndTime+",timelevel="+timelevel+",");


          if (null == ropStarttime || "".equals(ropStarttime)) {
            ropStarttime = startStamp;
          }
          counterVolumeCols.append(ropStarttime);
          counterVolumeCols.append(colDelimiter);

          final String rowCount = counterVolumeInfo.get("rowCount");
          counterVolumeCols.append(rowCount);
          counterVolumeCols.append(colDelimiter);

          final String counterVolume = counterVolumeInfo.get("counterVolume");
          counterVolumeCols.append(counterVolume);
          counterVolumeCols.append(colDelimiter);

          //Adding Column Delimiter to support 4 columns specific to ENIQ Events
          //WORKFLOW_TYPE, WORKFLOW_NAME, NUM_OF_CORRUPTED_ROWS, ROP_ENDTIME
          counterVolumeCols.append(colDelimiter);
          counterVolumeCols.append(colDelimiter);
          counterVolumeCols.append(colDelimiter);
          if(iRop != 0){
        	  counterVolumeCols.append(ropEndTime);//Added for HR70452
          }else{
        	  counterVolumeCols.append(endStamp);
          }

          counterVolumeCols.append(colDelimiter);

          // end of row
          counterVolumeCols.append(rowDelimiter);

          // build up the final row
          finalRow.append(sessionInfoRow);
          finalRow.append(counterVolumeCols);

          final List<String> dlines;
          if (lines.containsKey(dateID)) {
            dlines = lines.get(dateID);
          } else {
            dlines = new ArrayList<String>();
            lines.put(dateID, dlines);
          }
          dlines.add(finalRow.toString());
        }
        }
      }
    }
  }


  /**
   * Logs one entry.
   */
  public void log(final Map<String, Object> data) {
    try {
      final Map<String/*dataid*/, List<String>/*log-entry*/> lines = new HashMap<String, List<String>>();
      format(lines, data);
      if (!lines.isEmpty()) {
        for (String date : lines.keySet()) {
          final StringBuilder buffer = new StringBuilder();
          for (String line : lines.get(date)) {
            buffer.append(line);
          }
          writeLogEntry(date, buffer.toString());
        }
      }

    } catch (Exception e) {
      log.warning("FAILED record: \n+" + "sessionID=\"" + data.get("sessionID") + "\"\n" + "batchID=\""
        + data.get("batchID") + "\"\n" + "dateID=\"" + data.get("dateID") + "\"\n" + "fileName=\""
        + data.get("fileName") + "\"\n" + "sessionStartTime=\"" + data.get("sessionStartTime") + "\"\n"
        + "sessionEndTime=\"" + data.get("sessionEndTime") + "\"\n" + "source=\"" + data.get("source") + "\"\n"
        + "status=\"" + data.get("status") + "\"\n" + "srcLasModified=\"" + data.get("srcLastModified") + "\"\n"
        + "counterVolumes=" + data.get("counterVolumes"));

      log.log(Level.WARNING, "Logging error", e);
    }

    if (log.isLoggable(Level.FINEST)) {
      log.finest("LogEntry [" + name + "]:");

      for (String key : data.keySet()) {
        log.finest("  " + key + " = " + data.get(key));
      }
    }

  }

  @Override
  public void bulkLog(final Collection<Map<String, Object>> bulkData) {

    final Iterator<Map<String, Object>> iterator = bulkData.iterator();
    final Map<String/*dataid*/, List<String>/*log-entry*/> lines = new HashMap<String, List<String>>();
    try {
      while (iterator.hasNext()) {
        final Map<String, Object> data = iterator.next();
        format(lines, data);
      }
      for (String date : lines.keySet()) {
        final StringBuilder buffer = new StringBuilder();
        for (String line : lines.get(date)) {
          buffer.append(line);
        }
        writeLogEntry(date, buffer.toString());
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "Logging error", e);
    }
  }
  private static final ThreadLocal<SimpleDateFormat> prvDateFmt = new ThreadLocal<SimpleDateFormat>(){
      
	    @Override
	  protected SimpleDateFormat initialValue() {
	      // TODO Auto-generated method stub
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	     return sdf;
	  }
	};
}