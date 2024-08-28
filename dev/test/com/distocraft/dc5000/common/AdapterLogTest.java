package com.distocraft.dc5000.common;

import static org.junit.Assert.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.JUnit4TestAdapter;

import org.junit.*;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;

/**
 * 
 * @author ejarsok
 * 
 */

public class AdapterLogTest {

    private static File adapter;

    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "AdapterLogTest");

    private static String dateID = "dateID_value";

    private static AdapterLog aL;

    @BeforeClass
    public static void init() {

        if (!TMP_DIR.exists() && !TMP_DIR.mkdirs()) {
            fail("Failed to create " + TMP_DIR.getPath());
        }

        final Logger log = Logger.getLogger("etlengine.common.SessionLogger.ADAPTER");
        log.setLevel(Level.ALL);

        final Properties prop = new Properties();
        prop.setProperty("SessionHandling.log.ADAPTER.inputTableDir", TMP_DIR.getPath());

        try {
            StaticProperties.giveProperties(prop);
            aL = new AdapterLog();

        } catch (final Exception e1) {
            e1.printStackTrace();
            fail("testAdapterLog failed");
        }
    }

    @AfterClass
    public static void afterClass() {
        DirectoryHelper.delete(TMP_DIR);
    }

    @Test
    public void testAdapterLog() {
        final Map<String, Object> map = new HashMap<String, Object>();

        final Long time = System.currentTimeMillis();

        map.put("dateID", dateID);
        map.put("sessionStartTime", "100");
        map.put("sessionEndTime", "200");
        map.put("srcLastModified", String.valueOf(time));

        aL.log(map);
        adapter = new File(TMP_DIR, "ADAPTER." + dateID + ".unfinished");
        adapter.deleteOnExit();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            final String expected = "null\tnull\t" + dateID + "\tnull\t" + sdf.format(new Date(100L)) + "\t" + sdf.format(new Date(200L))
                    + "\tnull\tnull\t" + sdf.format(new Date(time)) + "\t0"
                    + "\tLOADED\t\t1111-11-11 11:11:11.111\t\t\t\t\t\t1111-11-11 11:11:11.111\t";
            final String actual = new HelpClass().readFileToString(adapter);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            e.printStackTrace();
            fail("testAdapterLog() failed");
        }

        if (adapter.exists()) {
            adapter.delete();
        }

    }

    @Test
    public void testAdapterLogWithCounts() {

        final Map<String, Object> map = new HashMap<String, Object>();

        final Long time = System.currentTimeMillis();

        map.put("dateID", dateID);
        map.put("sessionStartTime", "100");
        map.put("sessionEndTime", "200");
        map.put("srcLastModified", String.valueOf(time));

        final Map<String, Map<String, String>> cvols = new HashMap<String, Map<String, String>>();

        final Map<String, String> cvol = new HashMap<String, String>();
        cvol.put("typeName", "type");
        cvol.put("ropStarttime", "");
        cvol.put("rowCount", "100");
        cvol.put("counterVolume", "1000");

        cvols.put("one", cvol);

        map.put("counterVolumes", cvols);

        adapter = new File(TMP_DIR, "ADAPTER." + dateID + ".unfinished");

        if (adapter.exists()) {
            adapter.delete();
        }

        aL.log(map);

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            final String expected = "null\tnull\t" + dateID + "\tnull\t" + sdf.format(new Date(100L)) + "\t" + sdf.format(new Date(200L))
                    + "\tnull\tnull\t" + sdf.format(new Date(time)) + "\t0" + "\tLOADED" + "\ttype\t"+ sdf.format(new Date(100L)) +"\t100\t1000\t\t\t\t"
                    + sdf.format(new Date(200L)) + "\t";
            final String actual = new HelpClass().readFileToString(adapter);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            e.printStackTrace();
            fail("testAdapterLog() failed");
        }

        if (adapter.exists()) {
            adapter.delete();
        }

    }

    @Test
    public void testAdapterLogNoDateID() {
        final Map<String, Object> map = new HashMap<String, Object>();

        final Long time = System.currentTimeMillis();

        map.put("sessionStartTime", "100");
        map.put("sessionEndTime", "200");
        map.put("srcLastModified", String.valueOf(time));

        adapter = new File(TMP_DIR, "ADAPTER." + dateID + ".unfinished");
        if (adapter.exists()) {
            adapter.delete();
        }

        aL.log(map);
        adapter = new File(TMP_DIR, "ADAPTER." + dateID + ".unfinished");

        if (adapter.exists()) {
            adapter.delete();
            fail("Should not produce row if dateid is not present");
        }

    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AdapterLogTest.class);
    }
}
