package com.ericsson.eniq.common.lwp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.*;

import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;

/**
 * User: eeipca Date: 03/07/12 Time: 11:55
 */
public class LwProcessTest extends LwpBase {

    @Test
    public void test_execute_NoRmiServer() throws Exception {
        final LwpOutput result = do_execute(CMD_OK, true);
        assertEquals("Wrong exit code reported", 0, result.getExitCode());
        assertNotNull("Output buffer shouldnt be NULL", result.getStdout());
    }

    @Test
    public void test_execute_stringcmd() throws LwpException {
        final StringBuilder cmd = new StringBuilder();
        for (final String arg : CMD_OK) {
            cmd.append(arg).append(" ");
        }
        do_execute(cmd.toString(), true);
    }

    private LwpOutput do_execute(final List<String> cmd, final boolean local) throws LwpException {
        init(local);
        return LwProcess.execute(cmd, false, null);
    }

    private LwpOutput do_execute(final String cmd, final boolean local) throws LwpException {
        init(local);
        return LwProcess.execute(cmd, false, null);
    }

    private void init(final boolean local) throws LwpException {
        if (!local) {
            startRegistry();
            LwpServer.registerRmi(registryPort, null);
        }
    }

    @After
    public void after() {
        stopRegistry();
    }

    @Before
    public void before() {
        stopRegistry();
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "LwProcessTest");
        ServicenamesTestHelper.setupEmpty(TMP_DIR);
        createEtlcProperties();
        setupCodebase();
    }
}
