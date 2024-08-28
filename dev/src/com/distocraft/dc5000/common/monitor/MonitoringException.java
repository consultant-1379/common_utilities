package com.distocraft.dc5000.common.monitor;

import java.sql.SQLException;

/**
 * General monitoring excpetion.
 * This should only be thrown on unrecoverable errors e.g. No Driver class found.
 */
public class MonitoringException extends RuntimeException {

  private Throwable _cause = null;

  public MonitoringException(final String message, final Throwable cause) {
    super(message, cause);
    setRealSqlCause(cause);
  }

  public MonitoringException(final String message) {
    super(message);
  }

  public MonitoringException(final Throwable cause) {
    super(cause);
    setRealSqlCause(cause);
  }

  private void setRealSqlCause(final Throwable t){
    if(t instanceof SQLException){
      final SQLException sqle = (SQLException)t;
      _cause = sqle.getNextException() == null ? null : sqle.getNextException();
    }
  }

  @Override
  public Throwable getCause() {
    if(_cause == null){
      return super.getCause();
    } else {
      return _cause;
    }
  }
}
