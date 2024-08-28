package com.distocraft.dc5000.common;

import com.ericsson.eniq.common.testutilities.PollerBaseTestCase;
import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;
import com.ericsson.eniq.common.testutilities.UnitDatabaseTestCase;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServicenamesHelperTest {

	private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"), "ServicenamesHelperTest");

	@BeforeClass
	public static void beforeClass() throws IOException {

		System.setProperty("ETC_DIR", TMPDIR.getPath());
		System.setProperty("CONF_DIR", TMPDIR.getPath());

		ServicenamesTestHelper.setupEmpty(TMPDIR);
		ServicenamesHelper.setHostsFile(ServicenamesTestHelper.getTempHostsFile());

	}

	@AfterClass
	public static void afterClass() {
		PollerBaseTestCase.delete(TMPDIR);
	}

	@Test
	public void testGetWriters_NothingDefined() throws IOException {
		//Regression test, no DWH_READER or DWH_WRITER entries in dwh.ini/niq.ini.

		ServicenamesTestHelper.createDefaultNiqIni();

		final List<String> writerNodes = ServicenamesHelper.getWriterNodes();
		assertNotNull(writerNodes);
		assertEquals(1, writerNodes.size());
		assertEquals("dwhdb", writerNodes.get(0));
	}

	@Test
	public void testGetWriters_EmptyReaderBlock() throws IOException {
		//Regression test, empty reader block in dwh.ini/niq.ini.

		ServicenamesTestHelper.createNiqIniEmptyReaderBlock();

		final List<String> writerNodes = ServicenamesHelper.getWriterNodes();
		assertNotNull(writerNodes);
		assertEquals(1, writerNodes.size());
		assertEquals("dwhdb", writerNodes.get(0));
	}

	@Test
	public void testGetWriters_EmptyWriterBlock() throws IOException {
		//Regression test, empty writer block in dwh.ini/niq.ini.

		ServicenamesTestHelper.createNiqIniEmptyWriterBlock();

		final List<String> writerNodes = ServicenamesHelper.getWriterNodes();
		assertNotNull(writerNodes);
		assertEquals(1, writerNodes.size());
		assertEquals("dwhdb", writerNodes.get(0));
	}

	@Test
	public void testGetWriters_MissingWriterDefinition() throws IOException {

		ServicenamesTestHelper.createNiqIniMissingWriterDefinition();

		final List<String> writerNodes = ServicenamesHelper.getWriterNodes();
		assertNotNull(writerNodes);
		assertEquals(1, writerNodes.size());
		assertEquals("dwh_writer_20", writerNodes.get(0));
	}


	@Test
	public void testGetWriters_OnlyReadersDefined() throws IOException {
		// Only DWH_READERs defined (events type),
		// first reader defaults to being the writer node if more than one reader defined

		ServicenamesTestHelper.createNiqIniOnlyReadersDefined();

		final List<String> writerNodes = ServicenamesHelper.getWriterNodes();
		assertNotNull(writerNodes);
		assertEquals(1, writerNodes.size());
		assertEquals("dwh_reader_1", writerNodes.get(0));
	}

	@Test
	public void testGetWriters_WritersDefined() throws IOException {

		ServicenamesTestHelper.createNiqIniWritersDefined();

		final List<String> writerNodes = ServicenamesHelper.getWriterNodes();
		assertNotNull(writerNodes);
		assertEquals(2, writerNodes.size());
		assertEquals("dwh_writer_19", writerNodes.get(0));
		assertEquals("dwh_writer_20", writerNodes.get(1));
	}

	@Test
	public void test_getAllNodes_nothingDefined() throws IOException {

		ServicenamesTestHelper.createDefaultNiqIni();

		final List<String> allNodes = ServicenamesHelper.getAllIQNodes();
		assertNotNull(allNodes);
		assertEquals(1, allNodes.size());
		assertEquals("dwhdb", allNodes.get(0));
	}

	@Test
	public void test_getAllNodes_OnlyReadersDefined() throws IOException {
		// Only DWH_READERs defined (events type),
		// first reader defaults to being the writer node if more than one reader defined

		ServicenamesTestHelper.createNiqIniOnlyReadersDefined();

		final List<String> allNodes = ServicenamesHelper.getAllIQNodes();
		assertNotNull(allNodes);
		assertEquals(3, allNodes.size());
		assertEquals("dwhdb", allNodes.get(0));
		assertEquals("dwh_reader_1", allNodes.get(1));
		assertEquals("dwh_reader_2", allNodes.get(2));
	}

	@Test
	public void test_getReaderNodes_ReadersOnlyDefined() throws IOException {
		ServicenamesTestHelper.createNiqIniOnlyReadersDefined();
		final List<String> readers = ServicenamesHelper.getReaderNodes();
		assertNotNull(readers);
		assertEquals(1, readers.size());
		assertEquals("dwh_reader_2", readers.get(0));
	}

	@Test
	public void test_getReaderNodes_WritersAlsoDefined() throws IOException {
		ServicenamesTestHelper.createNiqIniWritersDefined();
		final List<String> readers = ServicenamesHelper.getReaderNodes();
		assertNotNull(readers);
		assertEquals(2, readers.size());
		assertEquals("dwh_reader_1", readers.get(0));
		assertEquals("dwh_reader_2", readers.get(1));
	}

	@Test
	public void test_getReaderNodes_NoReadersOrWriters() throws IOException {
		ServicenamesTestHelper.createNiqIniEmptyReaderBlock();
		final List<String> readers = ServicenamesHelper.getReaderNodes();
		assertNotNull(readers);
		assertEquals(1, readers.size());
		assertEquals("dwhdb", readers.get(0));
	}

	@Test @Ignore("No ssh keys are shared....")
	public void testGetCoreCount() throws IOException {
		final String remoteHost = System.getProperty(UnitDatabaseTestCase.INTEGRATION_HOST);
		if(remoteHost == null){
			fail("No -D"+UnitDatabaseTestCase.INTEGRATION_HOST+"=???? property defined");
		}
		final String serviceName = "test_service";
		final String ipAddress = InetAddress.getByName(remoteHost).getHostAddress();
		ServicenamesTestHelper.createHostsFile(ipAddress, remoteHost);
		ServicenamesTestHelper.createSampleServicenamesFile(ipAddress, remoteHost, serviceName);

		final ServicenamesHelper helper = ServicenamesHelperFactory.getInstance();
		final ServicenamesHelper.ServiceHostDetails service = helper.createServiceHostDetails(serviceName, remoteHost, ipAddress);
		final int cores = helper.getServiceHostCoreCount(service, "dcuser");
		assertEquals("CPU Core Count Incorrect!", 8, cores);
	}

	@Test @Ignore("No ssh keys are shared....")
	public void testGetCPUCount() throws IOException {
		final String remoteHost = System.getProperty(UnitDatabaseTestCase.INTEGRATION_HOST);
		if(remoteHost == null){
			fail("No -D"+UnitDatabaseTestCase.INTEGRATION_HOST+"=???? property defined");
		}
		final String serviceName = "test_service";
		final String ipAddress = InetAddress.getByName(remoteHost).getHostAddress();
		ServicenamesTestHelper.createHostsFile(ipAddress, remoteHost);
		ServicenamesTestHelper.createSampleServicenamesFile(ipAddress, remoteHost, serviceName);

		final ServicenamesHelper helper = ServicenamesHelperFactory.getInstance();
		final ServicenamesHelper.ServiceHostDetails service = helper.createServiceHostDetails(serviceName, remoteHost, ipAddress);
		final int cpus = helper.getServiceHostCpuCount(service, "dcuser", "dcuser");
		assertEquals("CPU Count Incorrect!", 2, cpus);
	}

	@Test
	public void test_getAllNodes_ReadersWritersDefined() throws IOException {

		ServicenamesTestHelper.createNiqIniWritersDefined();

		final List<String> allNodes = ServicenamesHelper.getAllIQNodes();
		assertNotNull(allNodes);
		assertEquals(5, allNodes.size());

		assertEquals("dwhdb", allNodes.get(0));
		assertEquals("dwh_writer_19", allNodes.get(1));
		assertEquals("dwh_writer_20", allNodes.get(2));
		assertEquals("dwh_reader_1", allNodes.get(3));
		assertEquals("dwh_reader_2", allNodes.get(4));
	}

	private void createServicenamesFile() throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(ServicenamesHelper.getServicenamesFile(), false));
		writer.write("#\n");
		writer.write("# ENIQ Events service list\n");
		writer.write("# Format is:\n");
		writer.write("# <ip_address>::<hostname>::<service>\n");
		writer.write("#\n");
		writer.write("127.0.0.1::somehost::controlzone\n");
		writer.write("127.0.0.1::somehost::dwh_reader_1\n");
		writer.write("127.0.0.1::somehost::dwhdb\n");
		writer.write("127.0.0.65::ec_1_hostname::ec_1\n");
		writer.write("127.0.0.11::somehost::engine\n");
		writer.write("127.0.0.1::somehost::glassfish\n");
		writer.write("127.0.0.1::somehost::ldapserver\n");
		writer.write("127.0.0.12::somehost::licenceservice\n");
		writer.write("127.0.0.1::somehost::repdb\n");
		writer.write("127.0.0.1::somehost::webserver\n");
		writer.write("127.0.0.42::another_service_hostname::another_service\n");
		writer.write("127.0.0.33::another_service_hostname::WIFI\n");
		writer.write("127.0.0.43::another_service_hostname::OSS_Aliase\n");
		writer.close();
	}

	@Test
	public void test_getServiceHost_AllDefined() throws IOException {
		createServicenamesFile();
		ServicenamesTestHelper.createHostsFile_Events();
		//entry in servicenames & hosts file
		final String name = ServicenamesHelper.getServiceHost("engine");
		assertEquals("127.0.0.11", name);
	}

	@Test
	public void test_getServiceHost_IncorrectServiceName() throws IOException {
		createServicenamesFile();
		ServicenamesTestHelper.createHostsFile_Events();
		//entry in servicenames & hosts file
		final String name = ServicenamesHelper.getServiceHost("engine");
		assertEquals("127.0.0.11", name);
		assertNull(ServicenamesHelper.getServiceHost("WIFI"));
		assertNull(ServicenamesHelper.getServiceHost("OSS_Aliases"));
	}

	@Test
	public void test_getServiceHost_NoServicenameDefined() throws IOException {
		createServicenamesFile();
		ServicenamesTestHelper.createHostsFile_Stats();
		//No service_names entry but there is a hosts file entry
		final String name = ServicenamesHelper.getServiceHost("some_new_service");
		assertEquals(null, name);
	}

	@Test
	public void test_getServiceHost_NoHostsDefined() throws IOException {
		createServicenamesFile();
		ServicenamesTestHelper.createHostsFile_Stats();
		//There's an entry in the service_names file but no hosts file entry 
		// for the service, the service_name entry ip address is used
		//the entry will only be retrieved for valid service names.
		//"controlzone", "dwhdb", "engine", "glassfish", "ldapserver", "licenceservice", "repdb", "scheduler", "webserver", "mediator"
		//"dwh_reader_", "ec_", "ec_lteefa_", "ec_ltees_", "ec_sgeh_"
		final String name = ServicenamesHelper.getServiceHost("another_service");
		assertNull(name);
	}

	@Test
	public void test_getServiceHost_HostsDefined() throws IOException {
		createServicenamesFile();
		ServicenamesTestHelper.createHostsFile_Stats();
		//There's an entry in the service_names file but no hosts file entry for the service but the service_name hostname is
		// defined on the hosts file
		final String name = ServicenamesHelper.getServiceHost("licenceservice");
		assertEquals("127.0.0.12", name);
	}

	@Test
	public void test_getServiceHost_NothingDefined() throws IOException {
		createServicenamesFile();
		ServicenamesTestHelper.createHostsFile_Stats();
		//There's no entry in either the hosts or service_names files
		String name = ServicenamesHelper.getServiceHost("blaaaaaaa");
		assertNull(name);
		name = ServicenamesHelper.getServiceHost("blaaaaaaa", "_default_");
		assertEquals("_default_", name);
	}

	@Test
	public void test_getServicenames() throws IOException {
		createServicenamesFile();
		final Map<String, ServicenamesHelper.ServiceHostDetails> details = ServicenamesHelper.getServiceDetails();
		assertTrue(details.containsKey("ec_1"));
		final ServicenamesHelper.ServiceHostDetails ec_1 = details.get("ec_1");
		assertEquals("Servicename not set correctly", "ec_1", ec_1.getServiceName());
		assertEquals("Hostname not set correctly", "ec_1_hostname", ec_1.getServiceHostname());
		assertEquals("IP Address not set correctly", "127.0.0.65", ec_1.getServiceIpAddress());
	}

	@Test
	public void test_getHostsFile_EventsFormat() throws Exception {
		ServicenamesHelper.setHostsFile(createHostsFile_Events());
		final Map<String, String> hosts = ServicenamesHelper.getHosts();
		assertTrue(hosts.containsKey("glassfish"));
		assertEquals("10.44.194.185", hosts.get("glassfish"));
	}

	@Test
	public void testNoServicenamesFile(){
		final String old = System.setProperty("CONF_DIR", "123");
		try{
			ServicenamesHelper.getServicenamesFile();
			fail("Exptected FileNotFoundException to be thrown!");
		} catch (FileNotFoundException e){
			/* Expected this*/
		} finally {
			System.setProperty("CONF_DIR", old);
		}
	}

	@Test
	public void testNoHostsFile(){
		final String old = System.setProperty("ETC_DIR", "123");
		ServicenamesHelper.setHostsFile(null);
		try{
			ServicenamesHelper.getHostsFile();
			fail("Expected FileNotFoundException to be thrown!");
		} catch (FileNotFoundException e){
			/* Expected this*/
		} finally {
			System.setProperty("ETC_DIR", old);
		}
		try {
			ServicenamesHelper.setHostsFile(ServicenamesTestHelper.getTempHostsFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void test_getHostsFile_StatsFormat() throws Exception {

		final String repdb = "repdb";
		final String address = "10.44.194.186";
		//ServicenamesTestHelper.createHostsFile_Stats();
		ServicenamesHelper.setHostsFile(createHostsFile_Events());

		final Map<String, String> hosts = ServicenamesHelper.getHosts();
		assertTrue(hosts.containsKey(repdb));
		assertEquals(address, hosts.get(repdb));
	}

	@Test
	public void test_getAllServiceNodes() throws Exception {
		final List<String> expected = ServicenamesTestHelper.createDefaultServicenamesFile();
		final Set<String> services = ServicenamesHelper.getAllServiceNodes();
		assertEquals("Wrong number of service name returned", expected.size(), services.size());
		for(String name : expected){
			assertTrue("Expected service '"+name+"' not returned in list " + services, services.contains(name));
		}
	}
	
	private  File createHostsFile_Events() throws IOException {
		String tempDir = System.getProperty("java.io.tmpdir");
	    final File tmpHosts = File.createTempFile("hosts", "", new File(tempDir));
	    System.setProperty("ETC_DIR", tmpHosts.getParent());
			final BufferedWriter writer = new BufferedWriter(new FileWriter(tmpHosts, false));
			writer.write("10.45.18.27 nasconsole\n");
			writer.write("#\n");
			writer.write("# Internet host table\n");
			writer.write("#\n");
			writer.write("::1             localhost\n");
			writer.write("127.0.0.1       localhost\n");
			writer.write("10.44.194.186   atrcxb1274      loghost\n");			
			writer.write("10.45.18.23 nas1\n");
			writer.write("#<---- HOSTSYNC START MARKER ---->\n");
			writer.write("# WARNING DO NOT REMOVE OR EDIT BETWEEN THE HOSTSYNC MARKERS\n");
			writer.write("10.44.194.186  atrcxb1274  controlzone\n");
			writer.write("10.44.194.186  atrcxb1274  dwh_reader_1\n");
			writer.write("10.44.194.186  atrcxb1274  dwhdb\n");
			writer.write("10.44.194.186  atrcxb1274  ec_1\n");
			writer.write("10.44.194.186  atrcxb1274  engine\n");
			writer.write("10.44.194.185  glassfish_host  glassfish\n");
			writer.write("10.44.194.186  atrcxb1274  ldapserver\n");
			writer.write("10.44.194.186  atrcxb1274  licenceservice\n");
			writer.write("10.44.194.186  atrcxb1274  repdb\n");
			writer.write("10.44.194.186  atrcxb1274  webserver\n");
			writer.write("10.44.194.186  atrcxb1274  scheduler\n");
			writer.write("#<---- HOSTSYNC END MARKER ---->\n");
			writer.close();
			return tmpHosts;
		}
}