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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.distocraft.dc5000.common.ConsoleLogFormatter;

/**
 * Copied and commented version of the EngineLogger class.
 * Changed and adapted for the licensing module.
 * 
 * @author ecarbjo 
 */
public class LicensingLogger extends Handler {

	// The data format of the logs.
	private static final DateFormat form = new SimpleDateFormat("yyyy_MM_dd");
	
	// The dir where the logs should be placed.
	private final String logdir;
	
	// debug flag.
	private boolean debugMode = false;

	// the string describing the licensing subsystem (used for dirnames and log entries.)
	private static final String SUBSYSTEMSTR = "licensemanager";
	
	private Writer currentLog;
	private String currentDate;
	
	/**
	 * Default constructor.
	 * @throws IOException
	 * @throws SecurityException
	 */
	public LicensingLogger() throws IOException, SecurityException {	
		// get the logdir from the system property LOG_DIR
		final String plogdir = System.getProperty("LOG_DIR");
		
		if (plogdir == null) {
			throw new IOException("System property \"LOG_DIR\" not defined");
		}
			
		// Define our logdir as a licensemanager subdir in the LOG_DIR
		logdir = plogdir + File.separator + SUBSYSTEMSTR;

		// set the logging level to Level.ALL to include all log messages. 
		setLevel(Level.ALL);
		
		// set the formatter to a an instance of the com.distrocraft.dc5000.common.ConsoleLogFormatter.
		setFormatter(new ConsoleLogFormatter());

		// get the debug mode from the system properties.
		final String systemDebugFlag = System.getProperty("LicensingLogger.debug");
		if (systemDebugFlag != null && systemDebugFlag.length() > 0) {
			debugMode = true;
		}
	}

	/**
	 * Does nothing because publish will handle flush after writing
	 */
	public synchronized void flush() {
		if (debugMode) {
			System.err.println("LicensingLogger.flush()");
		}
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */
	public synchronized void close() {
		if (debugMode) {
			System.err.println("LicensingLogger.close()");
		}
			
		// close the output log stream.
		try {
			currentLog.flush();
			currentLog.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Publish a LogRecord
	 */
	public synchronized void publish(final LogRecord record) {

		if (debugMode) {
			System.err.println("LicensingLogger.publish(" + record.getLoggerName() + ")");
		}
			
		// Determine that level is loggable and filter passes
		if (!isLoggable(record)) {
			return;
		}

		// get the current date stamp
		final Date dateNow = new Date(record.getMillis());
		final String dateStamp = form.format(dateNow);

		// check if we need to rotate the logs.
		if (currentLog == null || !currentDate.equals(dateStamp)) {
			rotate(dateStamp);
		}

		// write the log entry to the writer and flush the data.
		try {
			currentLog.write(getFormatter().format(record));
			currentLog.flush();

			if (debugMode) {
				System.err.println("Written: " + record.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Rotate the log onto a new one for this date.
	 * @param dateStamp the new date stamp to use.
	 */
	private void rotate(final String dateStamp) {

		if (debugMode) {
			System.err.println("LicensingLogger.rotate("+ dateStamp+ ")");
		}
			
		try {
			// close the old file if a log file is already active.
			if (currentLog != null) {
				currentLog.close();
			}
			
			// make the log dir if it doesn't exist.
			final File dir = new File(logdir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			// create a file handle for the new logfile for this date stamp.
			final File logFile = new File(dir, SUBSYSTEMSTR + "-" + dateStamp + ".log");

			if (debugMode) {
				System.err.println("LicensingLogger: FileName is " + logFile.getCanonicalPath());
			}
			
			// open the streams and set the current date stamp
			currentLog = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)));
			currentDate = dateStamp;

			// write the header to the log file.
			currentLog.write(getFormatter().getHead(this));

		} catch (Exception e) {
			System.err.println("LicensingLogger: LogRotation failed");
			e.printStackTrace();
		}
	}
}
