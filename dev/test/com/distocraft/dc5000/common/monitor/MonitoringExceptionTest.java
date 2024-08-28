package com.distocraft.dc5000.common.monitor;

import java.sql.SQLException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MonitoringExceptionTest {

  @Test
  public void testSQLExceptionCause(){
    final SQLException wrapper = new SQLException("wrapper");




    MonitoringException me = new MonitoringException(wrapper);
    assertEquals(wrapper, me.getCause());


    final SQLException real = new SQLException("real");
    wrapper.setNextException(real);

    me = new MonitoringException(wrapper);
    assertEquals(real, me.getCause());


  }
}
