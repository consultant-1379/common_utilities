package com.ericsson.eniq.common.lwp;

import static com.ericsson.eniq.common.lwp.LwpFailureCause.*;
import static junit.framework.Assert.*;

import java.io.*;
import java.net.URISyntaxException;
import java.rmi.NoSuchObjectException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.*;

import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;

public class LwpServerTest extends LwpBase {
    private static File LWP_LOG_PROPS = null;
    public static final String JAVA_UTIL_LOGGING_CONFIG_FILE = "java.util.logging.config.file";
    private static String oldLogConfFile = null;

    @Test
    public void testLogWatcher() throws InterruptedException, URISyntaxException, IOException {
        final Logger logger = Logger.getLogger(ILWPHelperRMIImpl.RMI_NAME);
        logger.setLevel(Level.OFF);
        final LwpServer.LogWatcher logWatcher = new LwpServer.LogWatcher(logger);
        logWatcher.start();
        Thread.sleep(2000);//let the watcher get a reading on the file....

        final Level expectedLevel = Level.FINEST;
        final BufferedWriter writer = new BufferedWriter(new FileWriter(LWP_LOG_PROPS, false));//dont append...
        writer.write(ILWPHelperRMIImpl.RMI_NAME + ".level=" + expectedLevel.getName());
        writer.newLine();
        writer.close();
        //Lest the watcher thread scan the file again..
        Thread.sleep(2000);
        final Level actualLevel = logger.getLevel();
        logWatcher.stopWatcher();
        logWatcher.join();//wait for it to stop...
        assertEquals("Logger didnt refresh its Log Levels automatically", expectedLevel, actualLevel);
    }

    @Test
    public void test_registerRmi_NoRegistryStarted() {
        stopRegistry();
        try {
            LwpServer.registerRmi(registryPort, null);
            fail("No Registry is started to how could a bind work?!");
        } catch (final LwpException e) {
            assertEquals("Wrong error thrown", REGISTRY_NOT_FOUND, e.getCauseCode());
        }
    }

    @Test
    public void test_getRegistryHost() throws LwpException {
        final String host = LwpServer.getRegistryHost();
        assertEquals("Registry host not got from properties correctly", registryHost, host);
    }

    @Test
    public void test_getRegistryPort() throws LwpException {
        final int port = LwpServer.getRegistryPort();
        assertEquals("Registry port not got from properties correctly", registryPort, port);
    }

    @Test
    public void test_GetOfflineRegistry() throws NoSuchObjectException {
        stopRegistry();
        try {
            LwpServer.getRegistry(registryPort, null);
            fail("No RMI Registry should be started so an exception should have been thrown!");
        } catch (final LwpException e) {
            assertEquals("Wrong cause code in error", REGISTRY_NOT_FOUND, e.getCauseCode());
        }
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "LwpServerTest");
        ServicenamesTestHelper.setupEmpty(TMP_DIR);

        createEtlcProperties();
        setupCodebase();

        LWP_LOG_PROPS = new File(TMP_DIR, "engineLogging.properties");
        /* Setup logger stuff */
        final BufferedWriter writer = new BufferedWriter(new FileWriter(LWP_LOG_PROPS));
        writer.write(ILWPHelperRMIImpl.RMI_NAME + ".level=INFO");
        writer.newLine();
        writer.close();

        oldLogConfFile = System.setProperty(JAVA_UTIL_LOGGING_CONFIG_FILE, LWP_LOG_PROPS.getPath());
    }

    @AfterClass
    public static void afterClass2() throws NoSuchObjectException {
        if (oldLogConfFile == null) {
            System.clearProperty(JAVA_UTIL_LOGGING_CONFIG_FILE);
        } else {
            System.setProperty(JAVA_UTIL_LOGGING_CONFIG_FILE, oldLogConfFile);
        }
        stopRegistry();
    }
}
