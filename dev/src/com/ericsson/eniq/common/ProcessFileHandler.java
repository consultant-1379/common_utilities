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

public class ProcessFileHandler extends Handler {

  private static final DateFormat FORM = new SimpleDateFormat("yyyy_MM_dd");

  private final String logdir;

  private Writer out = null;

  private static final String date = null;
  
  public String pname = null;

  private boolean deb = false;

  public ProcessFileHandler() throws IOException, SecurityException {

    final String plogdir = System.getProperty("LOG_DIR");
    if (plogdir == null) {
      throw new IOException("System property \"LOG_DIR\" not defined");
    }
      
    pname = System.getProperty("pname");

    logdir = plogdir + File.separator + pname;

    setLevel(Level.ALL);
    setFormatter(new ConsoleLogFormatter());

    final String xdeb = System.getProperty("ProcessLogger.debug");
    if (xdeb != null && xdeb.length() > 0) {
      deb = true;
    }
    
  }

  public synchronized void flush() {

    if (deb) {
      System.err.println("PL.flush()");
    }
      
    if (out != null) {
      try {
        out.flush();
      } catch(Exception e) {}
    }

  }

  public synchronized void close() {

    if (deb) {
      System.err.println("PL.close()");
    }
      
    try {

      if (out != null) {
        out.close();
      }

    } catch (Exception e) {
      if (deb) {
        System.err.println("PL.close failed");
        e.printStackTrace();
      }
    }

  }

  /**
   * Publish a LogRecord
   */
  public synchronized void publish(final LogRecord record) {

    if (deb) {
      System.err.println("PL.publish(" + record.getLoggerName() + ")");
    }
      
    // Determine that level is loggable and filter passes
    if (!isLoggable(record)) {
      return;
    }

    try {

      final Date dat = new Date(record.getMillis());

      final String dstamp = FORM.format(dat);

      if (date == null || !dstamp.equals(date)) {
        rotate(dstamp);
      }

      out.write(getFormatter().format(record));

      if (deb) {
        System.err.println("Written: " + record.getMessage());
      }
        
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  private void rotate(final String timestamp) {

    if (deb) {
      System.err.println("PL.rotate(" + timestamp + ")");
    }
      
    try {

      if (out != null) { // a file is already open
        out.close();
      }
        
      final File dir = new File(logdir);
      if (!dir.exists()) {
        dir.mkdirs();
      }

      final File f = new File(dir, pname + "-" + timestamp + ".log");

      if (deb) {
        System.err.println("PL: FileName is " + f.getCanonicalPath());
      }
        
      out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f, true)));

      out.write(getFormatter().getHead(this));

    } catch (Exception e) {
      System.err.println("PL: LogRotation failed");
      e.printStackTrace();
    }

  }

}
