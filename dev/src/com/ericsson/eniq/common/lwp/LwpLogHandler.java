package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import com.distocraft.dc5000.common.ConsoleLogFormatter;
import com.ericsson.eniq.common.EngineLogger;
import java.io.File;
import java.io.IOException;

public class LwpLogHandler extends EngineLogger {
  public LwpLogHandler() throws IOException, SecurityException {
    final String plogdir = System.getProperty("LOG_DIR");
    if (plogdir == null) {
      throw new IOException("System property \"LOG_DIR\" not defined");
    }

    logdir = new File(plogdir, "lwphelper").getPath();
    setFormatter(new ConsoleLogFormatter());
  }
}
