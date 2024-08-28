/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C)                   ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */
package com.ericsson.eniq.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to create hashing IDs based on certain column values
 * 
 * @author epaujor
 * 
 */
public class HashIdCreator {

  private static final String HIER3_CELL_ID = "HIER3_CELL_ID";

  private static final String CELL_ID = "CELL_ID";

  private static final String EVENT_SOURCE_NAME = "EVENT_SOURCE_NAME";

  private static final String HIER321_ID = "HIER321_ID";

  private static final String HIER32_ID = "HIER32_ID";

  private static final String HIER3_ID = "HIER3_ID";

  private static final String EVNTSRC_ID = "EVNTSRC_ID";

  private static final String HIERARCHY_1 = "HIERARCHY_1";

  private static final String HIERARCHY_2 = "HIERARCHY_2";

  private static final String VENDOR = "VENDOR";

  private static final String HIERARCHY_3 = "HIERARCHY_3";

  private static final String RAT = "RAT";

  private static final String UTF8 = "UTF8";

  private final MessageDigest msgDigest;

  private static final String ALGORITHM = "MD5";

  private final static Map<String, List<String>> ID_TO_COLUMNS_LOOKUP = new HashMap<String, List<String>>();

  private final static List<String> EVNTSRC_ID_COLS = new ArrayList<String>();

  private final static List<String> HIER3_ID_COLS = new ArrayList<String>();

  private final static List<String> HIER32_ID_COLS = new ArrayList<String>();

  private final static List<String> HIER321_ID_COLS = new ArrayList<String>();

  private final static List<String> HIER3_CELL_ID_COLS = new ArrayList<String>();
  static {
    EVNTSRC_ID_COLS.add(EVENT_SOURCE_NAME);

    HIER3_ID_COLS.add(RAT);
    HIER3_ID_COLS.add(HIERARCHY_3);
    HIER3_ID_COLS.add(VENDOR);

    HIER32_ID_COLS.add(RAT);
    HIER32_ID_COLS.add(HIERARCHY_3);
    HIER32_ID_COLS.add(HIERARCHY_2);
    HIER32_ID_COLS.add(VENDOR);

    HIER321_ID_COLS.add(RAT);
    HIER321_ID_COLS.add(HIERARCHY_3);
    HIER321_ID_COLS.add(HIERARCHY_2);
    HIER321_ID_COLS.add(HIERARCHY_1);
    HIER321_ID_COLS.add(VENDOR);

    HIER3_CELL_ID_COLS.add(RAT);
    HIER3_CELL_ID_COLS.add(HIERARCHY_3);
    HIER3_CELL_ID_COLS.add(CELL_ID);
    HIER3_CELL_ID_COLS.add(VENDOR);

    ID_TO_COLUMNS_LOOKUP.put(EVNTSRC_ID, EVNTSRC_ID_COLS);
    ID_TO_COLUMNS_LOOKUP.put(HIER3_ID, HIER3_ID_COLS);
    ID_TO_COLUMNS_LOOKUP.put(HIER32_ID, HIER32_ID_COLS);
    ID_TO_COLUMNS_LOOKUP.put(HIER321_ID, HIER321_ID_COLS);
    ID_TO_COLUMNS_LOOKUP.put(HIER3_CELL_ID, HIER3_CELL_ID_COLS);
  }

  public HashIdCreator() throws NoSuchAlgorithmException {
    msgDigest = MessageDigest.getInstance(ALGORITHM);
  }

  private byte[] stringToByteArray(final String s) throws UnsupportedEncodingException {
    return s.getBytes(UTF8);
  }

  private long byteArrayToLong(final byte[] bArray) {
    final ByteBuffer bBuffer = ByteBuffer.wrap(bArray);
    final LongBuffer lBuffer = bBuffer.asLongBuffer();
    return lBuffer.get();
  }

  /**
   * Converts the input string into a hash ID of datatype long
   * 
   * @param input
   * @return
   * @throws NoSuchAlgorithmException
   * @throws IOException
   */
  public long hashStringToLongId(final String input) throws IOException {
    msgDigest.update(stringToByteArray(input));
    byte[] byteArray = Arrays.copyOfRange(msgDigest.digest(), 0, 8);
    // Remove the last bit as we never want to get LOG.MIN_VALUE as absolute
    // cannot be got for this
    byteArray[0] &= 0x7f;
    return Math.abs(byteArrayToLong(byteArray));
  }

  /**
   * Returns the columns that make up this hash ID
   * 
   * @param hashId
   *          the hash ID column
   * @return
   */
  public List<String> getColsForHashId(final String hashId) {
    return ID_TO_COLUMNS_LOOKUP.get(hashId);
  }

  /**
   * Returns the list of hash ID columns
   * 
   * @return
   */
  public List<String> getHashIdColumns() {
    final List<String> hashIds = new ArrayList<String>();
    hashIds.addAll(ID_TO_COLUMNS_LOOKUP.keySet());
    return hashIds;
  }
}
