package com.distocraft.dc5000.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Created on 12.7.2005
 * This class is for rotating log-files. java.util.logging API is used for logging. 
 * @author vesterinen
 */
public class DailyFileHandler {

  private Logger logger = null;
  private StreamHandler handler = new StreamHandler();
  private static final String logName = "engine-";
  
  public DailyFileHandler(final String classname){
    logger = Logger.getLogger(classname);
  }
  
  public void setLoggerClassName(final String classname){
    logger = Logger.getLogger(classname);
  }
  
  /**
   * Writes log information in log file. java.util.logging is used.
   * @param line the line to write
   * @param severity - level of logging. Please refer to class <code>java.util.logging.Level</code>.
   * @param exception - if file isnt found
   */
  public synchronized void writeLine(final String line, final int severity, final Exception exception) {

    try {
      final BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(selectLog(), true));
      
      handler = new StreamHandler(bout, new LogFormatter());
      handler.setLevel(Level.ALL);

      logger.addHandler(handler);
      logger.setLevel(Level.ALL);
      
      if (Level.WARNING.intValue() == severity) {
        logger.warning(line);
      } else if (Level.INFO.intValue() == severity) {
        logger.info(line);
      } else if (Level.SEVERE.intValue() == severity) {
        logger.log(Level.SEVERE, line , exception);
      }
      handler.close();
    } catch (FileNotFoundException e) {
      logger.log(Level.SEVERE, "file not found." , e);
    }
   }

  /**
   * Selects log that is used at current date. Logs are named as angine-yyyy_mm_dd.log
   * @return The log file
   */
  private File selectLog() {

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    final String file = sdf.format(new Date());
    final String tempDir = System.getProperty("java.io.tmpdir");

    return new File(tempDir, "engine" + File.separator + logName + file + ".log");
  }

}
