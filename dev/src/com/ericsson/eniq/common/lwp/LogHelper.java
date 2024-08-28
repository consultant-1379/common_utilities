package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class LogHelper {

  static void error(final Logger logger, final String msg, final Throwable cause) {
    logException(logger, Level.SEVERE, msg, cause);
  }

  static void warning(final Logger logger, final String msg, final Throwable cause) {
    logException(logger, Level.WARNING, msg, cause);
  }

  static void logException(final Logger logger, final Level level, final String msg, final Throwable cause) {
    if (logger == null) {
      System.err.println(msg);
      cause.printStackTrace(System.err);
    } else {
      logger.log(level, msg, cause);
    }
  }

  static void log(final Logger logger, final Level level, final String msg) {
    if (logger == null) {
      PrintStream pStream = System.out;
      if (level == Level.SEVERE) {
        pStream = System.err;
      }
      pStream.println(msg);
    } else {
      logger.log(level, msg);
    }
  }
}
