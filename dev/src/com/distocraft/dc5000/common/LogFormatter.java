package com.distocraft.dc5000.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created on 13.7.2005
 * Formatter class for logger.
 * @author vesterinen
 */
public class LogFormatter extends Formatter {

  public String format(final LogRecord rec) {
    final StringBuilder buf = new StringBuilder();
    
    final SimpleDateFormat date = new SimpleDateFormat("dd.MM HH:mm:ss");
    buf.append(date.format(new Date(rec.getMillis())));
        
    buf.append(' ');
    buf.append(rec.getLevel());
    buf.append(' ');
    buf.append(rec.getLoggerName());
    buf.append(' ');
    buf.append(formatMessage(rec));
    
    if (rec.getThrown() != null) { 
      final StackTraceElement[] stak = rec.getThrown().getStackTrace();
      buf.append('\n');
      buf.append(rec.getThrown().fillInStackTrace());
      buf.append('\n');
      for (StackTraceElement elem : stak) {
        buf.append('(');
        buf.append(elem);
        buf.append(')');
        buf.append('\n');
      }
    }
    
    if (rec.getThrown() == null) {
      buf.append("\n");
    }
    
    return buf.toString();
  }
  
}
