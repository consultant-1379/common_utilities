package com.ericsson.eniq.common.test;

import java.util.HashMap;
import java.util.Map;

import com.distocraft.dc5000.common.AdapterLog;
import com.distocraft.dc5000.common.StaticProperties;

public class SessionLogTester {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    try {
      System.setProperty("dc5000.config.directory", "/tmp/conf");
      StaticProperties.reload();

      SessionLogTester slt = new SessionLogTester();
      
      if(args[0].equalsIgnoreCase("generate")) {
        slt.generateLogs();
      } else {
        System.err.println("Unknown command: " + args[0]);
      }
    
    } catch(Exception e) {
      e.printStackTrace();
    }
    
  }

  private void generateLogs() throws Exception {

    AdapterLog al = new AdapterLog();

    Map amap = new HashMap();

    amap.put("dateID", "2007-07-01");
    amap.put("sessionID", "1");
    amap.put("batchID", "1");
    amap.put("fileName", "foo.bar");
    amap.put("sessionStartTime", String.valueOf(System.currentTimeMillis() - 10000L));
    amap.put("sessionEndTime", String.valueOf(System.currentTimeMillis()));
    amap.put("source", "sorsa");
    amap.put("status", "PANIC");
    amap.put("srcLastModified", String.valueOf(System.currentTimeMillis() - 30000L));

    al.log(amap);

    for (int i = 2 ; i < 100 ; i++ ) {
      amap.put("batchID", String.valueOf(i));
      if (i%2 == 0) {
        amap.put("dateID", "2007-07-01");
      } else {
        amap.put("dateID", "2007-07-02");
      }
      al.log(amap);
    }
      
    al.rotate();
    
    amap.put("sessionID", "2");
    
    for (int i = 1 ; i < 100 ; i++ ) {
      amap.put("batchID", String.valueOf(i));
      if (i%2 == 0) {
        amap.put("dateID", "2007-07-01");
      } else {
        amap.put("dateID", "2007-07-02");
      }
      al.log(amap);
    }
    
  }

}
