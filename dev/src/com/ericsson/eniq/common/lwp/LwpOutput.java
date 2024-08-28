package com.ericsson.eniq.common.lwp;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

import java.io.Serializable;

public class LwpOutput implements Serializable {
  private int exitCode;
  private String stdout = null;
  private String stderr = null;

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(final int exitCode) {
    this.exitCode = exitCode;
  }

  public String getStderr() {
    return stderr;
  }

  public void setStderr(final String stderr) {
    this.stderr = stderr;
  }

  public String getStdout() {
    return stdout;
  }

  public void setStdout(final String stdout) {
    this.stdout = stdout;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("LwpOutput{");
    sb.append("exit-code=").append(exitCode);
    sb.append(", stdout='").append(getStdout()).append('\'');
    if (getStderr() != null) {
      sb.append(", stderr='").append(getStderr()).append('\'');
    }
    sb.append('}');
    return sb.toString();
  }
}
