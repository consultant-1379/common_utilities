package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for SessionHandler class in com.distrocraft.dc5000.common. <br>
 * <br>
 * Testing the sessionhandling process. Sessionhandler logs session data to a
 * unfinished logfile. When logfile is determined to be ready it is rotated and
 * labeled as finished.
 * 
 * @author EJARSOK, EJAAVAH
 * 
 */
public class SessionHandlerTest {

  private static Properties prop;

  private static File sf;

  private static File itd;

  private static File unfinishedLogfile;

  private static File finishedLogfile;

  private static String storageFile;

  private static String inputTableDir;

  private static Field sessions;
  private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "SessionHandlerTest");
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    if(!TMP_DIR.exists() && !TMP_DIR.mkdirs()){
      fail("Failed to create " + TMP_DIR.getPath());
    }

    sessions = SessionHandler.class.getDeclaredField("sessions");
    sessions.setAccessible(true);

    inputTableDir = TMP_DIR.getPath() + File.separator + "inputTableDir";
    storageFile = TMP_DIR.getPath() + File.separator + "storageFile";
    sf = new File(storageFile);
    sf.deleteOnExit();
    itd = new File(inputTableDir);
    itd.deleteOnExit();
    unfinishedLogfile = new File(inputTableDir, "ADAPTER.dateID.unfinished");
    unfinishedLogfile.deleteOnExit();
    finishedLogfile = new File(inputTableDir, "ADAPTER.dateID");
    finishedLogfile.deleteOnExit();

    try {
            final PrintWriter pw = new PrintWriter(new FileWriter(sf));
      pw.print("testSession=7356\n");
      pw.close();
        } catch (final Exception e) {
      e.printStackTrace();
      fail("Can´t write in file!");
    }

    // Setting up configurations like which sessionhandler to use and
    // directorypaths to log data
    prop = new Properties();
    prop.setProperty("SessionHandling.storageFile", storageFile);
    prop.setProperty("SessionHandling.log.types", "SessionHandler");
    prop.setProperty("SessionHandling.log.SessionHandler.class", AdapterLog.class.getName());
    prop.setProperty("SessionHandling.log.ADAPTER.inputTableDir", inputTableDir);

    try {
      StaticProperties.giveProperties(prop);
        } catch (final Exception e) {
      e.printStackTrace();
      fail("testSessionHandler failed");
    }
  }



  @AfterClass
  public static void afterClass(){
    DirectoryHelper.delete(TMP_DIR);
  }

  /**
   * Testing the initializer method by checking if storageFile and inputTableDir
   * are created for logging purposes.
   */
  @Test
  public void testSessionHandlerInit() throws Exception {
    try {
      SessionHandler.init();
        } catch (final Exception e) {
      fail("testSessionHandler failed - could not initialize SessionHandler");
    }
    assertEquals(true, sf.exists());
    assertEquals(true, itd.exists());
  }

  /**
   * Testing if the unfinished logfile is created and sessiondata is written to
   * it.
   */
  @Test
  public void testSessionHandlerLog() throws Exception {
        final HashMap data = new HashMap();
    data.put("sessionID", "sessionID");
    data.put("batchID", "batchID");
    data.put("dateID", "dateID");
    data.put("fileName", "filename");
    data.put("sessionStartTime", "1000000000");
    data.put("sessionEndTime", "2000000000");
    data.put("source", "source");
    data.put("status", "status");
    data.put("srcLastModified", "3000000000");
    SessionHandler.log("SessionHandler", data);

        //final String expected = "sessionID\tbatchID\tdateID\tLOADED\tfilename\t1970-01-12_14:46:40\t1970-01-24_04:33:20\tsource\tstatus\t1970-02-04_18:20:00.000\t0\t\t\t\t\t";
        String expected = "sessionID\tbatchID\tdateID\tfilename\t1970-01-12 14:46:40.000\t1970-01-24 04:33:20.000\tsource\tstatus\t1970-02-04 18:20:00.000\t0\tLOADED\t\t1111-11-11 11:11:11.111\t\t\t\t\t\t1111-11-11 11:11:11.111\t";
        final String result = new HelpClass().readFileToString(unfinishedLogfile);

    assertEquals(true, unfinishedLogfile.exists());
    assertEquals(expected, result);
  }

  /**
   * Testing if the logfile is labeled as finished when it is rotated and that
   * it has the data it should.
   */
  @Test
  public void testSessionHandlerRotate() throws Exception {
    SessionHandler.rotate("SessionHandler");
        String expected = "sessionID\tbatchID\tdateID\tfilename\t1970-01-12 14:46:40.000\t1970-01-24 04:33:20.000\tsource\tstatus\t1970-02-04 18:20:00.000\t0\tLOADED\t\t1111-11-11 11:11:11.111\t\t\t\t\t\t1111-11-11 11:11:11.111\t";
        final String result = new HelpClass().readFileToString(finishedLogfile);

    assertEquals(true, finishedLogfile.exists());
    assertEquals(expected, result);
  
    SessionHandler.rotate("SessionDummy");
  
  }

  /**
   * Testing if getSessionID() method returns correct ID for new session and
   * already existing session.
   */
  @Test
  public void testSessionHandlerGetSessionID() throws Exception {
        assertEquals(1, SessionHandler.getSessionID("newTestSession"));
        assertEquals(7357, SessionHandler.getSessionID("testSession"));
  }

  /**
   * Testing SessionID fetching with different max session IDs in database
   * (table META_TRANSFER_BATCHES).
   */
  @Test
  public void testGetSessionIDwDBMaxSessionIDCheck() throws Exception {

    /* SessionType object for engine session IDs */
        final SessionType st = new SessionType();

    /* Map storing the sessionType objects */
        final HashMap sessionMap = new HashMap();

    /* If current session ID is higher than in DB, Continuing as usual */
    st.current = 1001L;
    st.lastReserved = 100L;
    sessionMap.put("engine", st);
    sessions.set(SessionHandler.currentThread(), sessionMap);
    SessionHandler.setDBMaxSessionID(1000L);
        assertEquals(1002, SessionHandler.getSessionID("engine"));

    /* If current ID is lower than in DB, it will be changed to be the same */
    st.current = 500L;
    st.lastReserved = 510L;
    sessionMap.put("engine", st);
    sessions.set(SessionHandler.currentThread(), sessionMap);
    SessionHandler.setDBMaxSessionID(1000L);
        assertEquals(1001, SessionHandler.getSessionID("engine"));
  
    /* If current ID is higher than 4294967290L, it will reset to 0 */     
    st.current = 4294967290L;
    st.lastReserved = 510L;
    sessionMap.put("engine", st);
    sessions.set(SessionHandler.currentThread(), sessionMap);
    SessionHandler.setDBMaxSessionID(1000L);
    assertEquals(0, SessionHandler.getSessionID("engine"));   
        
  }
}
