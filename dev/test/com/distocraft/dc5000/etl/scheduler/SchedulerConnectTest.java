package com.distocraft.dc5000.etl.scheduler;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.ConnectException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;


public class SchedulerConnectTest {

  private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"), SchedulerConnectTest.class.getSimpleName());

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
    if(!tmpDir.exists() && !tmpDir.mkdirs()){
      fail("Failed to created " + tmpDir.getPath());
    }
    tmpDir.deleteOnExit();
		
		final Properties props = new Properties();
		//props.setProperty("SCHEDULER_HOSTNAME", "localhost");
		props.setProperty("SCHEDULER_PORT", "12367");
		props.setProperty("SCHEDULER_REFNAME", "Scheduler");
		//props.setProperty("ENGINE_HOSTNAME", "localhost");
		props.setProperty("ENGINE_PORT", "12367");
		props.setProperty("ENGINE_REFNAME", "TransferEngine");
		

    final File propsFile = new File(tmpDir, "ETLCServer.properties");
		propsFile.deleteOnExit();
	
		FileOutputStream fos = null;
		
		try {
			fos = new FileOutputStream(propsFile, false);
			props.store(fos, "");
		} finally {
			try {
				fos.close();
			} catch(Exception e) {}
		}
			
	}
	
	@Test
	public void testSchedNoConfig() {
		System.setProperty("dc5000.config.directory", "sdfsdf");
		try {
			SchedulerConnect.connectScheduler(); //NOPMD
			fail("Should throw ConnectException as can't find config file so defaults are used");
    } catch (ConnectException e){
      // OK
		} catch (Exception e) {
			fail("Should throw ConnectException as can't find config file so defaults are used");
		}
		
	}

	@Test
	public void testSchedSysPropSet() {
		System.setProperty("dc5000.config.directory", tmpDir.getPath());
		try {
			SchedulerConnect.connectScheduler(); //NOPMD
			fail("Should throw ConnectException as no Naming running");
		} catch (ConnectException ce) {
			// OK
		} catch (Exception e) {
			fail("Should throw ConnectException as no Naming running");			
		}
		
	}
	
	@Test
	public void testEngineNoConfig() {
		System.setProperty("dc5000.config.directory", "sdfsdf");
		
		try {
			SchedulerConnect.connectEngine(); //NOPMD
			fail("Should throw ConnectException as can't find config file so defaults are used");
		} catch (ConnectException fne) {
			// OK
		} catch (Exception e) {
			fail("Should throw ConnectException as can't find config file so defaults are used");
		}
		
	}

	@Test
	public void testEngineSysPropSet() {
		System.setProperty("dc5000.config.directory", tmpDir.getPath());
		try {
			SchedulerConnect.connectEngine(); //NOPMD
			fail("Should throw ConnectException as no Naming running");
		} catch (ConnectException ce) {
			// OK
		} catch (Exception e) {
			fail("Should throw ConnectException as no Naming running");			
		}
		
	}
	
}
