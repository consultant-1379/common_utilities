package com.distocraft.dc5000.common;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import com.ericsson.eniq.exception.ConfigurationException;

public class LoaderLog extends SessionLogger {


  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

	public LoaderLog() throws ConfigurationException, FileNotFoundException {
		super("LOADER");
	}

	public void log(final Map<String, Object> data) {
		final StringBuilder sb = new StringBuilder();

		try {

			final String dateID = (String) data.get("DATE_ID");

			if (dateID == null  || dateID.trim().equalsIgnoreCase("")) {
				throw new Exception("Date id not defined");
			} else {

				sb.append((String) data.get("LOADERSET_ID"));
				sb.append("\t");
				sb.append((String) data.get("SESSION_ID"));
				sb.append("\t");
				sb.append((String) data.get("BATCH_ID"));
				sb.append("\t");
				sb.append(dateID);
				sb.append("\t");
				sb.append((String) data.get("TIMELEVEL"));
				sb.append("\t");
				sb.append((String) data.get("DATADATE"));
				sb.append("\t");
				sb.append((String) data.get("DATATIME"));
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
			log.warning("FAILED record: \n+" + "LOADERSET_ID=\"" + data.get("LOADERSET_ID") + "\"\n" + "SESSION_ID=\""
					+ data.get("SESSION_ID") + "\"\n" + "BATCH_ID=\"" + data.get("BATCH_ID") + "\"\n" + "DATE_ID=\""
					+ data.get("DATE_ID") + "\"\n" + "TIMELEVEL=\"" + data.get("TIMELEVEL") + "\"\n" + "DATADATE=\""
					+ data.get("DATADATE") + "\"\n" + "DATATIME=\"" + data.get("DATATIME") + "\"\n" + "ROWCOUNT=\""
					+ data.get("ROWCOUNT") + "\"\n" + "SESSIONSTARTTIME=\"" + data.get("SESSIONSTARTTIME") + "\"\n"
					+ "SESSIONENDTIME=\"" + data.get("SESSIONENDTIME") + "\"\n" + "SOURCE=\"" + data.get("SOURCE") + "\"\n"
					+ "STATUS=\"" + data.get("STATUS") + "\"\n" + "TYPENAME=\"" + data.get("TYPENAME") + "\"");

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
  public void bulkLog(final Collection<Map<String, Object>> collection) {
	//EQEV-43393 Modified to make the stringbuffer synchronised as there was mismatch 
		//in data received in collection and after adding to buffer
	    StringBuffer textTowrite ;
	    String currentDateID = "DEFAULT";
	    synchronized("threadsafe_buffer"){
	    	textTowrite = new StringBuffer();
  
    final Iterator<Map<String, Object>> iterator = collection.iterator();
    while (iterator.hasNext()) {
      Map<String, Object> temp = iterator.next();
      final String dateID = (String)temp.get("DATE_ID");         
      if (dateID == null || dateID.trim().equalsIgnoreCase("")) {
        log.warning("FAILED record: \n+" + "LOADERSET_ID=\"" + temp.get("LOADERSET_ID") + "\"\n" + "SESSION_ID=\""
          + temp.get("SESSION_ID") + "\"\n" + "BATCH_ID=\"" + temp.get("BATCH_ID") + "\"\n" + "DATE_ID=\""
          + temp.get("DATE_ID") + "\"\n" + "TIMELEVEL=\"" + temp.get("TIMELEVEL") + "\"\n" + "DATADATE=\""
          + temp.get("DATADATE") + "\"\n" + "DATATIME=\"" + temp.get("DATATIME") + "\"\n" + "ROWCOUNT=\""
          + temp.get("ROWCOUNT") + "\"\n" + "SESSIONSTARTTIME=\"" + temp.get("SESSIONSTARTTIME") + "\"\n"
          + "SESSIONENDTIME=\"" + temp.get("SESSIONENDTIME") + "\"\n" + "SOURCE=\"" + temp.get("SOURCE") + "\"\n"
          + "STATUS=\"" + temp.get("STATUS") + "\"\n" + "TYPENAME=\"" + temp.get("TYPENAME") + "\"");

        log.log(Level.WARNING, "Logging error");
      } else {

        if (currentDateID.equalsIgnoreCase("DEFAULT")) {
          currentDateID = dateID;
        }

        textTowrite.append(temp.get("LOADERSET_ID"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("SESSION_ID"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("BATCH_ID"));
        textTowrite.append("\t");
        textTowrite.append(dateID);
        textTowrite.append("\t");
        textTowrite.append(temp.get("TIMELEVEL"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("DATADATE"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("DATATIME"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("ROWCOUNT"));
        textTowrite.append("\t");
        final long estamp = Long.parseLong( (String)temp.get("SESSIONSTARTTIME"));
        textTowrite.append(sdf.format(new Date(estamp)));
        textTowrite.append("\t");
        final long sstamp = Long.parseLong( (String)temp.get("SESSIONENDTIME"));
        textTowrite.append(sdf.format(new Date(sstamp)));
        textTowrite.append("\t");
        textTowrite.append(temp.get("SOURCE"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("STATUS"));
        textTowrite.append("\t");
        textTowrite.append(temp.get("TYPENAME"));
        textTowrite.append("\t0\t\n");
      }
    }
	    }
    try {
      //Only Write to the file if there are successful Loads...
      if (textTowrite.length() > 0) {
        writeLog(textTowrite, currentDateID);
      }
    } catch (Exception e) {
      log.warning("FAILED record:" + textTowrite.toString());
      log.log(Level.WARNING, "Logging error", e);
    }
  }
}
