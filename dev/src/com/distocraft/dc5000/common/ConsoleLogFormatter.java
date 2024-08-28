package com.distocraft.dc5000.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Java.util.Logging implementation for formatting log output. <br>
 * <br>
 * Configuration: none <br>
 * <br>
 * $id$ <br>
 * <br>
 * Copyright Distocraft 2005 <br>
 * 
 * @author lemminkainen
 */
public class ConsoleLogFormatter extends Formatter {

  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");
  
  /**
   * Formats one log entry.
   * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
   */
  public String format(final LogRecord lr) {

    final StringBuilder res = new StringBuilder();

    res.append(sdf.format(new Date(lr.getMillis())));
    res.append(" ");
    res.append(lr.getThreadID());
    res.append(" ");
    res.append(lr.getLevel().getName());
    res.append(" ");
    res.append(lr.getLoggerName());
    res.append(" : ");
    res.append(lr.getMessage());
    res.append("\n");

    Throwable t = lr.getThrown();
    int inten = 3;

    while (t != null) {
      appendException(t, inten, res);
      inten += 3;

      t = t.getCause();
    }

    return res.toString();
  }

  private void appendException(final Throwable t, final int inten, final StringBuilder res) {
    if (t != null) {

      for (int i = 0; i < inten; i++) {
        res.append(" ");
      }

      res.append(t.getClass().getName());
      res.append(": ");
      res.append(t.getMessage());
      res.append("\n");

      for (StackTraceElement elem : t.getStackTrace()) {
      
        for (int i = 0; i < inten + 5; i++) {
          res.append(" ");
        }

        res.append(elem.getClassName());
        res.append(".");
        res.append(elem.getMethodName());
        res.append("(");
        if (elem.getFileName() == null) {
          res.append("Unknown Source");
        } else {
          res.append(elem.getFileName());
          res.append(":");
          res.append(elem.getLineNumber());
        }
        res.append(")");
        res.append("\n");
      }

    }

  }

}
