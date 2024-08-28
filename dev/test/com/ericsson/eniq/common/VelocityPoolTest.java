package com.ericsson.eniq.common;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.velocity.app.VelocityEngine;

/**
 * 
 * @author ejarsok
 *
 */

public class VelocityPoolTest {
  
  @Test
  public void testVelocityPool(){
    VelocityEngine ve0 = null;
    VelocityEngine ve1 = new VelocityEngine();
    VelocityEngine ve2 = new VelocityEngine();
    VelocityEngine ve3 = null;
    
    try {
      ve0 = VelocityPool.reserveEngine();
    } catch (Exception e1) {
      e1.printStackTrace();
      fail("Failed, Exception");
    }
    
    assertNotNull(ve0);
    
    VelocityPool.releaseEngine(ve1);
    VelocityPool.releaseEngine(ve2);
    VelocityPool.releaseEngine(ve3);
    
    try {
      assertEquals(ve1, VelocityPool.reserveEngine());
      assertEquals(ve2, VelocityPool.reserveEngine());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
    }
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(VelocityPoolTest.class);
  }
}
