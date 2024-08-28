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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.distocraft.dc5000.common.ConsoleLogFormatter;

public class SchedulerLogger extends Handler {

  private static final DateFormat form = new SimpleDateFormat("yyyy_MM_dd");

  private final String logdir;

  private final Map<String,OutputDetails> logs = new HashMap<String,OutputDetails>();

  private final boolean deb;

  public SchedulerLogger() throws IOException, SecurityException {

    final String tlogdir = System.getProperty("LOG_DIR");
    if (tlogdir == null) {
      throw new IOException("System property \"LOG_DIR\" not defined");
    }
      
    logdir = tlogdir + File.separator + "scheduler";
    
    setLevel(Level.ALL);
    setFormatter(new ConsoleLogFormatter());

    final String xdeb = System.getProperty("SchedulerLogger.debug");
    if (xdeb != null && xdeb.length() > 0) {
      deb = true;
    } else {
      deb = false;
    }
  }

  /**
   * Does nothing because publish will handle flush after writing
   */
  public synchronized void flush() {

    if (deb) {
      System.err.println("SL.flush()");
    }
  }

  public synchronized void close() {

    if (deb) {
      System.err.println("SL.close()");
    }
      
    final Iterator<String> i = logs.keySet().iterator();

    while (i.hasNext()) {

      try {

        final String key = i.next();

        final OutputDetails od = logs.get(key);
        od.out.close();
        od.out = null;

        i.remove();

      } catch (Exception e) {
      }

    }

  }

  /**
   * Publish a LogRecord
   */
  public synchronized void publish(final LogRecord record) {

    if (deb) {
      System.err.println("SL.publish(" + record.getLoggerName() + ")");
    }
      
    // Determine that level is loggable and filter passes
    if (!isLoggable(record)) {
      return;
    }

    String tp = "NA";
    final String type = "scheduler";

    try {

      final String logname = record.getLoggerName();

      // Special handling for these loggers
      if (logname.startsWith("tp.")) {
        final int ix = logname.indexOf(".") + 1;
        final int eix = logname.indexOf(".",ix+1);
        
        if(eix > 0) {
          tp = logname.substring(ix, eix);
        } else {
          tp = logname.substring(ix);
        }  
      }

    } catch (Exception e) {
      if(deb) {
        e.printStackTrace();
      }
    }

    if (deb) {
      System.err.println("SL: TechPackName is \"" + tp + "\" type is \"" + type + "\"");
    }
      
    OutputDetails od = logs.get(tp + "_" + type);

    final Date dat = new Date(record.getMillis());

    final String dstamp = form.format(dat);

    if (od == null || !dstamp.equals(od.dat)) {
      od = rotate(tp, type, dstamp);
    }

    try {
      od.out.write(getFormatter().format(record));
      od.out.flush();

      if (deb) {
        System.err.println("SL: Written: " + record.getMessage());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    final int levelInt = record.getLevel().intValue();
    if (levelInt >= Level.WARNING.intValue()) {
      if(deb) {
        System.err.println("SL: Logging error");
      }
        
      OutputDetails odw = logs.get("WARN_error");
      
      if (odw == null || !dstamp.equals(odw.dat)) {
        odw = rotate("WARN", "error", dstamp);
      }

      try {
        odw.out.write(getFormatter().format(record));
        odw.out.flush();

        if (deb) {
          System.err.println("SL: Written to error: " + record.getMessage());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    }

  }

  private OutputDetails rotate(final String tp, final String type, final String timestamp) {

    if (deb) {
      System.err.println("SL.rotate(" + tp + " " + type + " " + timestamp + ")");
    }
      
    OutputDetails od = null;

    try {

      od = logs.get(tp + "_" + type);

      if (od == null) {
        od = new OutputDetails();
      } else if (od.out != null) { // a file is already open
        od.out.close();
      }
      
      String dirx = null;

      if ("NA".equals(tp)) {
        dirx = logdir + File.separator;
      } else if ("WARN".equals(tp)) {
        dirx = logdir + File.separator;
      } else {
        dirx = logdir + File.separator + tp;
      }
        
      final File dir = new File(dirx);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      
      final File f = new File(dir, type + "-" + timestamp + ".log");

      if (deb) {
        System.err.println("SL: FileName is " + f.getCanonicalPath());
      }
      
      od.out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f, true)));
      od.dat = timestamp;

      od.out.write(getFormatter().getHead(this));

      logs.put(tp + "_" + type, od);

    } catch (Exception e) {
      System.err.println("SL: LogRotation failed");
      e.printStackTrace();
    }

    return od;

  }

  public class OutputDetails {

    public Writer out = null;

    public String dat = null;

  };

}