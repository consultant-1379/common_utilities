package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.distocraft.dc5000.common.ServicenamesHelper.ServiceHostDetails;

public class ServicenamesHelperFactoryTest {

	public static final int DUMMY_CORE_COUNT = 808;

	@Test
	public void test_getInstance_defaultInstance() {
		
		ServicenamesHelper defaultHelper = ServicenamesHelperFactory.getInstance();

		assertEquals("ServicenamesHelper", defaultHelper.getClass().getSimpleName());
	}

	@Test
	public void test_getInstance_nonDefaultInstance() throws IOException {
		
		ServicenamesHelperFactory.setInstance(new MockedServicenamesHelper());
		ServicenamesHelper defaultHelper = ServicenamesHelperFactory.getInstance();

		assertEquals("MockedServicenamesHelper", defaultHelper.getClass().getSimpleName());

	}
	
	
	class MockedServicenamesHelper extends ServicenamesHelper {

//		@Override
//		public int getServiceHostCoreCount(final ServiceHostDetails hostDetails,
//				final String username, final String passwd) throws IOException {
//			return DUMMY_CORE_COUNT;
//		}

	}
}
