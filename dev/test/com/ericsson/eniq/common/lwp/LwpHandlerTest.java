package com.ericsson.eniq.common.lwp;

import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.CMD_EXEC_FAILED;
import static com.ericsson.eniq.common.lwp.LwpFailureCause.NO_ARGS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

public class LwpHandlerTest extends LwpBase {

  private ILWPHelperRMI testInstance = null;


  @Test
  public void test_execute_OK() throws RemoteException, LwpException {
    final LwpOutput results = testInstance.execute(CMD_OK, true);
    assertEquals("Wrong exit code reported", 0, results.getExitCode());
    assertNotNull("STDOUT not recorded", results.getStdout());
  }

  @Test
  public void test_execute_ERROR_INVALID_EXE() throws RemoteException {
    try {
      testInstance.execute(CMD_EXE_ERROR, true);
      fail("Exception should have been thrown!");
    } catch (LwpException e) {
      assertEquals("Wrong error thrown", CMD_EXEC_FAILED, e.getCauseCode());
    }
  }

  @Test
  public void test_execute_ERROR_INVALID_CMDARGS() throws RemoteException {
    try {
      final LwpOutput results = testInstance.execute(CMD_ARGS_ERROR, true);
      assertFalse("Wrong exit code reported", results.getExitCode() == 0);
      assertNotNull("STDOUT not recorded", results.getStdout());
    } catch (LwpException e) {
      fail("No error should have been thrown " + e.toString());
    }
  }


  @Test
  public void test_execute_NoArgs() throws RemoteException {
    try {
      testInstance.execute(null, true);
    } catch (LwpException e) {
      assertEquals("Wrong error thrown", NO_ARGS, e.getCauseCode());
    }
    try {
      testInstance.execute(new ArrayList<String>(0), true);
    } catch (LwpException e) {
      assertEquals("Wrong error thrown", NO_ARGS, e.getCauseCode());
    }
  }

  @Test
  public void test_ping() throws RemoteException {
    testInstance.ping();
  }

  @Before
  public void before() throws RemoteException {
    testInstance = new ILWPHelperRMIImpl();
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
    TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "LwpHandlerTest");
    ServicenamesTestHelper.setupEmpty(TMP_DIR);
    /* Setup RMI location stuff */
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(TMP_DIR, "ETLCServer.properties")));
    writer.write(LwpServer.ENGINE_HOST + "=" + registryHost);
    writer.newLine();
    writer.write(LwpServer.ENGINE_PORT + "=" + registryPort);
    writer.newLine();
    writer.close();
  }

}