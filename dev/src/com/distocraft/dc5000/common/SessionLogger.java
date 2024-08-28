package com.distocraft.dc5000.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.ericsson.eniq.exception.ConfigurationException;

public abstract class SessionLogger {

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  
  protected final Logger log;

  protected final String name;

  protected File inputTableDir = null;

  protected int rowcount = 0;

  /* Acts like a Mutex for Thread safety */
  private static final Object threadsafetyMutex = new Object();
  
  /**
   * Initialisation of protected content. This must be done before accepting any
   * log entries.
   * 
   * @param name
   *          name of logger
   */
  SessionLogger(final String name) throws ConfigurationException, FileNotFoundException {
    this.name = name;

    log = Logger.getLogger("etlengine.common.SessionLogger." + name);

    log.finer("init...");

    try {
      String loc = StaticProperties.getProperty("SessionHandling.log." + name + ".inputTableDir");
      if (loc.startsWith("${ETLDATA_DIR}")) {
        loc = System.getProperty("ETLDATA_DIR") + loc.substring(14);
      }
      inputTableDir = new File(loc);
    } catch (Exception e) {
      throw new ConfigurationException("static.properties","SessionHandling.log." + name + ".inputTableDir", ConfigurationException.Reason.MISSING);
    }
      
    if (!inputTableDir.exists()) {
      inputTableDir.mkdirs();
    }

    if (!inputTableDir.exists() || !inputTableDir.isDirectory() || !inputTableDir.canWrite()) {
      throw new FileNotFoundException("Can't access inputTableDir " + inputTableDir.getName());
    }

    log.finer("sucessfully initialized");

  }

  /**
   * Returns writer for certain day. If open file does not exists, it is
   * created.
   */
  protected synchronized PrintWriter getWriter(final String date) throws FileNotFoundException {
    log.finest("Trying to open new writer " + date);
    final File newFile = new File(inputTableDir.getAbsolutePath() + File.separator + name + "." + date + ".unfinished");
    return new PrintWriter(new BufferedOutputStream(new FileOutputStream(newFile, true)));
  }

  /**
   * Atomically write one log entry to log file.
   */
  protected synchronized void writeLogEntry(final String date, final String logEntry) throws FileNotFoundException {
  	log.finest("Trying to open new writer " + date);
  	PrintWriter pw = null;
  	try {
  		pw =  getWriter(date);
  		pw.print(logEntry);
  	} finally {
  		try {
  			pw.flush();
  		} catch (Exception e) {//
  		}
  		
  		try {
  			pw.close();
  		} catch (Exception e) {//
  		}
  	}
  	
  }

  public String getName() {
    return name;
  }

  /**
   * Logs one sessionLog entry.
   * 
   * @param data
   *          SessionLog entry
   */
  public abstract void log(final Map<String, Object> data);
  
  /**
   * Logs all sessionLog entries
   *
   * @param data
   *          A collection of SessionLog entries
   */
  public abstract void bulkLog(final Collection<Map<String, Object>> data);

  /**
   * Rotation: Renames .unfinished for loading
   * 
   * @throws Exception
   *           is thrown on error
   */
  public synchronized void rotate() {

    log.fine("Rotating...");

    // List unfinished files
    final File[] fils = inputTableDir.listFiles(new FilenameFilter() {

      public boolean accept(final File dir, final String fname) {
        return (fname.endsWith(".unfinished"));
      }
    });

    log.finest("Found " + fils.length + " unfinished files");

    // Mark unfinished to finished
    for (File uff : fils) {
      String ffn = uff.getName().substring(0,uff.getName().lastIndexOf(".unfinished"));
   
      // KLUDGE: Ensure that upgrade does not crash to old unfinished files
      if(ffn.indexOf(".") < 0) {
        ffn += "." + sdf.format(new Date(System.currentTimeMillis()));
      }
      
      uff.renameTo(new File(uff.getParentFile(),ffn));
    }

  }

  protected void writeLog(final StringBuffer text, final String date) throws Exception {
    try {
      synchronized (threadsafetyMutex) {
        final PrintWriter pw = getWriter(date);
        pw.print(text);
        pw.flush();
        pw.close();
      }
    } catch (Exception e) {
      throw e;
    }
  }
  
}
