package com.distocraft.dc5000.common;

import org.junit.Test;

public class RmiUrlFactoryTest {
	
	RmiUrlFactory testInstance = RmiUrlFactory.getInstance();
	String output ;
	
	@Test
	public void testgetMultiESRmiUrlIpv6Address() {
		output = testInstance.getMultiESRmiUrl("2002:2112:32f4:12de::10");
		System.out.println(output);
		
	}
	
	@Test
	public void testgetMultiESRmiUrlIpv4Address() {
		output = testInstance.getMultiESRmiUrl("10.3.43.67");
		System.out.println(output);
		
	}

}