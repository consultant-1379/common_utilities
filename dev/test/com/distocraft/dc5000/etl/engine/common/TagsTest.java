package com.distocraft.dc5000.etl.engine.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

/**
 * 
 * @author ejarsok
 */
public class TagsTest {

  @Test
  public void testGetTagPairs() {

    try {
    	final HashMap<String,String> hm = Tags.GetTagPairs("id", ",", "name1=value1,name2=value2");
      assertEquals("value1", hm.get("idname1"));
      assertEquals("value2", hm.get("idname2"));
    } catch (Exception e) {
      e.printStackTrace();
      fail("testGetTagPairs() failed, Exception");
    }
  }

}
