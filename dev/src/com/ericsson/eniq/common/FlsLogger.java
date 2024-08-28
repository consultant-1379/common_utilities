/**
 * Log Handler class for symboliclinkcreator module
 */
package com.ericsson.eniq.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.distocraft.dc5000.common.ConsoleLogFormatter;

/**
 * Log handler for symboliclinkcreator module
 * @author xarjsin
 */
public class FlsLogger extends Handler {
	
	private static final DateFormat form = new SimpleDateFormat("yyyy_MM_dd");
	private boolean deb = false;
	private String logdir;
	private Map<String, WriterObject> writerMap = new HashMap<>();
	
	/**
	 * @throws IOException, SecurityException 
	 * 
	 */
	public FlsLogger() throws IOException, SecurityException {
		final String plogdir = System.getProperty("LOG_DIR");
	    if (plogdir == null) {
	      throw new IOException("System property \"LOG_DIR\" not defined");
	    }
	    logdir = plogdir + File.separator + "symboliclinkcreator";
	    setLevel(Level.ALL);
	    setFormatter(new ConsoleLogFormatter());
	    final String xdeb = System.getProperty("FlsLogger.debug");
	    if (xdeb != null && xdeb.length() > 0) {
	      deb = true;
	    }
	}

	/** 
	 * Close the output log stream
	 * 
	 */
	@Override
	public void close() throws SecurityException {
		if (deb) {
		      System.err.println("FL.close()");
		    }
		try{
			Writer writer = null;
			for(String type : writerMap.keySet()) {
				writer = writerMap.get(type).getWriter();
				writer.flush();
				writer.close();
			}
		}
		catch(Exception e){
			System.err.println("Failed to close log file");
		}
	}

	/**
	 *  Does nothing because publish will handle flush after writing
	 */
	@Override
	public void flush() {
		if (deb) {
		      System.err.println("FL.flush()");
		    }
	}

	/** 
	 * Publish a LogRecord
	 */
	@Override
	public synchronized void publish(final LogRecord record) {
		
		String type = record.getLoggerName();
		final String toDate = form.format(new Date(record.getMillis()));
		if(deb){
			System.err.println("FL.publish(" + record.getLoggerName() + ")");
		}
		// Determine that level is loggable and filter passes
		if (!isLoggable(record)) {
			return;
		}
		Writer writer = null;
		String dat = null;
		WriterObject writerObj = writerMap.get(type);
		if (writerObj != null) {
			writer = writerObj.getWriter();
			dat = writerObj.getDat();
		}
		if (writer == null || !toDate.equals(dat)) {
			writer = rotate(type,toDate, writerObj);
		}
		try {
			writer.write(getFormatter().format(record));
			writer.flush();
			if (deb) {
				System.err.println("Written: " + record.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param type
	 * @param timestamp
	 */
	private Writer rotate(final String type, final String timestamp, WriterObject writerObj) {
		Writer writer = null;
		boolean rotate = false;
		String ossId = null;
		File dir = new File(logdir);
		if (deb) {
			System.err.println("FlsLogger.rotate("+ timestamp + ")");
		}
		try{
			if(writerObj != null && writerObj.getWriter() != null) {
				writerObj.getWriter().close();
				rotate = true;
			}
			if(!dir.exists()) {
				dir.mkdirs();
			}
			// create a file handle for the new logfile for this date stamp.
			File logFile = null;
			if(type.contains(".")){
				if(type != null && type.contains("_")) {
					if((ossId = getOssId(type)) != null ) {
						logFile = new File(dir, type.substring(0,type.indexOf("."))+"_"+ossId+ "-" + timestamp + ".log");
					}else {
						if(deb) {
							System.err.println("FlsLogger.rotate: Cannot deduce ossId from type");
						}
					}
				} else {
					logFile = new File(dir, type.substring(0,type.indexOf(".")) + "-" + timestamp + ".log");
				}
			} else {				
				logFile = new File(dir, type + "-" + timestamp + ".log");
			}
			if (deb) {
				System.err.println("FlsLogger: FileName is " + logFile.getCanonicalPath());
			}
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)));
			if (rotate) {
				//rotation should happen, Existing writer needs to be updated.
				writerObj.setWriter(writer);
				writerObj.setDat(timestamp);
			} else {
				//first time creation.
				writerMap.put(type, new WriterObject(writer, timestamp));
			}
			writer.write(getFormatter().getHead(this));
		}
		catch (Exception e) {
			System.err.println("FlsLogger: LogRotation failed");
			e.printStackTrace();
		}
		return writer;
	}
	
	private String getOssId(String type) {
		Pattern pattern = Pattern.compile("[^_]*[_](.+)");
		Matcher matcher = pattern.matcher(type);
		if(matcher.matches()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
	
	public class WriterObject {
	    private Writer writer;
	    private String dat;
	    
	    public WriterObject(Writer writer, String dat) {
	    	this.writer = writer;
	    	this.dat = dat;
	    }
	    
	    public Writer getWriter() {
			return writer;
		}
		public void setWriter(Writer out) {
			this.writer = out;
		}
		public String getDat() {
			return dat;
		}
		public void setDat(String dat) {
			this.dat = dat;
		}
	  }
}
