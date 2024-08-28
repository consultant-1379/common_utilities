package com.distocraft.dc5000.common;

import java.util.logging.Filter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class PrefixFilter implements Filter {

  private String prefix = "";

  public PrefixFilter() {
    
    final LogManager manager = LogManager.getLogManager();

    prefix = manager.getProperty(PrefixFilter.class.getName() + ".prefix");
    if(prefix == null) {
      prefix = "";
    }
  }
  
  public PrefixFilter(final String prefix) {
    this.prefix = prefix;
  }
  
  public boolean isLoggable(final LogRecord record) {
    return (record != null && record.getLoggerName().startsWith(prefix));
  }

}
