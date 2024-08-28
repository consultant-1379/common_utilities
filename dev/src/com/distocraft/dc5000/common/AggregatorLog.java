package com.distocraft.dc5000.common;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import com.ericsson.eniq.exception.ConfigurationException;

public class AggregatorLog extends SessionLogger {

  public AggregatorLog() throws ConfigurationException, FileNotFoundException {
    super("AGGREGATOR");
  }

  public void log(final Map<String, Object> data) {

    try {

      final String dateID = (String) data.get("DATE_ID");
      
      if (dateID == null || dateID.trim().equalsIgnoreCase("")) {
        throw new Exception("Date id not defined");
      } else {

      	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
      	final StringBuilder sb = new StringBuilder();
      	
        sb.append((String) data.get("AGGREGATORSET_ID"));
        sb.append("\t");
        sb.append((String) data.get("SESSION_ID"));
        sb.append("\t");
        sb.append((String) data.get("BATCH_ID"));
        sb.append("\t");
        sb.append(dateID);
        sb.append("\t");
        sb.append((String) data.get("TIMELEVEL"));
        sb.append("\t");
        final long ddstamp = Long.parseLong((String) data.get("DATADATE"));
        sb.append(sdf.format(new Date(ddstamp)));
        sb.append("\t");
        final long dtstamp = Long.parseLong((String) data.get("DATATIME"));
        sb.append(sdf.format(new Date(dtstamp)));
        sb.append("\t");
        sb.append((String) data.get("ROWCOUNT"));
        sb.append("\t");
        final long estamp = Long.parseLong((String) data.get("SESSIONSTARTTIME"));
        sb.append(sdf.format(new Date(estamp)));
        sb.append("\t");
        final long sstamp = Long.parseLong((String) data.get("SESSIONENDTIME"));
        sb.append(sdf.format(new Date(sstamp)));
        sb.append("\t");
        sb.append((String) data.get("SOURCE"));
        sb.append("\t");
        sb.append((String) data.get("STATUS"));
        sb.append("\t");
        sb.append((String) data.get("TYPENAME"));
        sb.append("\t0\t\n");
        
        writeLogEntry(dateID, sb.toString());
        
      }

    } catch (Exception e) {
    	
    	System.out.println(log + " " + data);
    	
      log.warning("FAILED record: \n+" + "AGGREGATORSET_ID=\"" + data.get("AGGREGATORSET_ID") + "\"\n"
          + "SESSION_ID=\"" + data.get("SESSION_ID") + "\"\n" + "BATCH_ID=\"" + data.get("BATCH_ID") + "\"\n"
          + "DATE_ID=\"" + data.get("DATE_ID") + "\"\n" + "TIMELEVEL=\"" + data.get("TIMELEVEL") + "\"\n"
          + "DATADATE=\"" + data.get("DATADATE") + "\"\n" + "DATATIME=\"" + data.get("DATATIME") + "\"\n"
          + "ROWCOUNT=\"" + data.get("ROWCOUNT") + "\"\n" + "SESSIONSTARTTIME=\"" + data.get("SESSIONSTARTTIME")
          + "\"\n" + "SESSIONENDTIME=\"" + data.get("SESSIONENDTIME") + "\"\n" + "SOURCE=\"" + data.get("SOURCE")
          + "\"\n" + "STATUS=\"" + data.get("STATUS") + "\"\n" + "TYPENAME=\"" + data.get("TYPENAME") + "\"");

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
  public void bulkLog(Collection<Map<String, Object>> data) {
    // TODO Auto-generated method stub
    //Not used yet...
  }

}
