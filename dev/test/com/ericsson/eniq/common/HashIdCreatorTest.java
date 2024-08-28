package com.ericsson.eniq.common;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Test;

public class HashIdCreatorTest {

    @Test
    public void checkThatStringIsConvertedToHashId() throws NoSuchAlgorithmException, IOException {
    final HashIdCreator hashId = new HashIdCreator();

        assertEquals(4956847150856741762L, hashId.hashStringToLongId("1"));
        assertEquals(2318431741638412123L, hashId.hashStringToLongId("123"));
        assertEquals(4956847150856741762L, hashId.hashStringToLongId("1"));
        assertEquals(3164553332293943742L, hashId.hashStringToLongId("1234567890123456"));
        assertEquals(3650468854192837135L, hashId.hashStringToLongId("1ONRM_RootMo_R:RNC03:RNC031135Ericsson"));
        assertEquals(1767008173202600382L, hashId.hashStringToLongId("0|BSC1||ERICSSON"));
        assertEquals(8432899184272901578L, hashId.hashStringToLongId("0|BSC1|ERICSSON"));
        assertEquals(5241260070646413395L, hashId.hashStringToLongId("0|BSC1||CELL-353-77-30-1566|ERICSSON"));
        assertEquals(1805042870849258408L, hashId.hashStringToLongId("1|ONRM_RootMo_R:RNC01:RNC01|ERICSSON"));
        assertEquals(6337019889400695412L, hashId.hashStringToLongId("1|ONRM_RootMo_R:RNC01:RNC01||ERICSSON"));

        assertEquals(5032042619824625184L,
                hashId.hashStringToLongId("1|ONRM_RootMo_R:RNC01:RNC01||SAC-353-87-11449-18151|ERICSSON"));

    }

    @Test
    public void checkThatEVNTSRC_IDReturnCorrectCols() throws NoSuchAlgorithmException, IOException {
        final HashIdCreator hashId = new HashIdCreator();

        final List<String> cols = hashId.getColsForHashId("EVNTSRC_ID");
        assertEquals(1, cols.size());
        assertEquals("EVENT_SOURCE_NAME", cols.get(0));
    }

    @Test
    public void checkThatHIER3_IDReturnCorrectCols() throws NoSuchAlgorithmException, IOException {
        final HashIdCreator hashId = new HashIdCreator();

        final List<String> cols = hashId.getColsForHashId("HIER3_ID");
        assertEquals(3, cols.size());
        assertEquals("RAT", cols.get(0));
        assertEquals("HIERARCHY_3", cols.get(1));
        assertEquals("VENDOR", cols.get(2));
    }

    @Test
    public void checkThatHIER32_IDReturnCorrectCols() throws NoSuchAlgorithmException, IOException {
        final HashIdCreator hashId = new HashIdCreator();

        final List<String> cols = hashId.getColsForHashId("HIER32_ID");
        assertEquals(4, cols.size());
        assertEquals("RAT", cols.get(0));
        assertEquals("HIERARCHY_3", cols.get(1));
        assertEquals("HIERARCHY_2", cols.get(2));
        assertEquals("VENDOR", cols.get(3));
    }

    @Test
    public void checkThatHIER321_IDReturnCorrectCols() throws NoSuchAlgorithmException, IOException {
        final HashIdCreator hashId = new HashIdCreator();

        final List<String> cols = hashId.getColsForHashId("HIER321_ID");
        assertEquals(5, cols.size());
        assertEquals("RAT", cols.get(0));
        assertEquals("HIERARCHY_3", cols.get(1));
        assertEquals("HIERARCHY_2", cols.get(2));
        assertEquals("HIERARCHY_1", cols.get(3));
        assertEquals("VENDOR", cols.get(4));
    }

    @Test
    public void checkThatHIER3_CELL_IDReturnCorrectCols() throws NoSuchAlgorithmException, IOException {
        final HashIdCreator hashId = new HashIdCreator();

        final List<String> cols = hashId.getColsForHashId("HIER3_CELL_ID");
        assertEquals(4, cols.size());
        assertEquals("RAT", cols.get(0));
        assertEquals("HIERARCHY_3", cols.get(1));
        assertEquals("CELL_ID", cols.get(2));
        assertEquals("VENDOR", cols.get(3));
    }

    @Test
    public void checkThatHasdIdColsAreCorrect() throws NoSuchAlgorithmException, IOException {
        final HashIdCreator hashId = new HashIdCreator();

        final List<String> cols = hashId.getHashIdColumns();
        assertEquals(5, cols.size());
        assertEquals("HIER321_ID", cols.get(0));
        assertEquals("HIER3_ID", cols.get(1));
        assertEquals("HIER32_ID", cols.get(2));
        assertEquals("HIER3_CELL_ID", cols.get(3));
        assertEquals("EVNTSRC_ID", cols.get(4));
    }
}