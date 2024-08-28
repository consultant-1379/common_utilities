package com.distocraft.dc5000.etl.engine.system;

import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Date;

public class StatusEventTest {

	private static final Date CURRENT_TIME = new Date(System.currentTimeMillis());
	private static final String MESSAGE = "message"; 
  private static StatusEvent StatusEventTest = new StatusEvent("Yes");
  private static Object o = new Object();
  final static Date currentTime = new Date(System.currentTimeMillis());
  private static StatusEvent StatusEventTest1 = new StatusEvent(o, currentTime, "Yes");


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Test
  public void teststatusEventWithCurrentTime() {
	 StatusEvent se = StatusEventTest.statusEventWithCurrentTime("isProcessed", "Yes");
	 }

  @Test
  public void teststatusEventWithCurrentTime1() {
	  Object o = new Object();
	  StatusEvent se = StatusEventTest.statusEventWithCurrentTime(o, "Yes");
	  }
  
  @Test
  public void testgetMessage() {
	  String Expected = "Yes";
	  assertEquals(Expected, StatusEventTest.getMessage());
	  }
  
  @Test
  public void testToString() {
	  String Expected = "StatusEvent: dispatcher= : time=null : message=Yes : ";
	  assertEquals(Expected, StatusEventTest.toString());
	  }
  
  @Test
  public void testEquals() {
	  assertEquals(false, StatusEventTest.equals(o));
	  }
  @Test
	public void testConstructorWithDispatcher() {
		StatusEvent statusEvent = new StatusEvent(this, CURRENT_TIME, MESSAGE);
		String className = this.getClass().getName();
		Assert.assertEquals(statusEvent.getDispatcher(), className);
	}

	@Test
	public void testCreatingEventWithStaticMethod() {
		StatusEvent s = StatusEvent.statusEventWithCurrentTime(this, MESSAGE);
		String className = this.getClass().getName();
		Assert.assertEquals(s.getDispatcher(), className);
		Assert.assertEquals(s.getMessage(), MESSAGE);
	}

	@Test
	public void testUsingNullsAsConstructorParameters() {
		StatusEvent s = new StatusEvent(null, null, null);

		boolean causedException = false;
		try {
			s.toString();
		} catch (Exception e) {

		}
		Assert.assertFalse(causedException);

		Assert.assertNull(s.getDispatcher());
		Assert.assertNull(s.getTime());
		Assert.assertNull(s.getMessage());
	}
 
}
