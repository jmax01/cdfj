package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.GDR.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

class GDRTest {

    static final String DEFAULT_NAME_VALUE = "namevalue";

    static final Logger LOGGER = Logger.getLogger(GDRTest.class.getCanonicalName());

    static GDR newDefaultGDRInstance() {
        final GDR GDR = new GDR();

        return GDR;
    }

    @Test
    void testLogger() {
        LOGGER.log(Level.WARNING, "Warning");
        LOGGER.log(Level.SEVERE, "SEVERE");
        LOGGER.log(Level.INFO, "INFO");
    }

    //
    // @Test
    // void testGetSize() {
    // final GDR GDR = newDefaultGDRInstance();
    // assertEquals(GDR_RECORD_SIZE, GDR.getSize());
    //
    // }
    //
    // @Test
    // void testRecordCapacity() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int capacity = GDR.record.capacity();
    // assertEquals(GDR_RECORD_SIZE, capacity);
    // }
    //
    // @Test
    // void testSetGDRNext() {
    //
    // final GDR GDR = newDefaultGDRInstance();
    // final long expected = 1L;
    // GDR.setGDRNext(expected);
    // assertEquals(expected, GDR.GDRNext);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // final long GDRNextFromRecord = GDRRecord.getLong(GDR_GDR_NEXT_FIELD_OFFSET);
    // assertEquals(expected, GDRNextFromRecord);
    // }
    //
    // @Test
    // void testSetAgrEDRHead() {
    // final GDR GDR = newDefaultGDRInstance();
    // final long expected = 2L;
    // GDR.setAgrEDRHead(expected);
    // assertEquals(expected, GDR.agrEDRHead);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getLong(GDR_AGR_EDR_HEAD_FIELD_OFFSET));
    // }
    //
    // @Test
    // void testSetAzEDRHead() {
    // final GDR GDR = newDefaultGDRInstance();
    // final long expected = 3L;
    // GDR.setAzEDRHead(expected);
    // assertEquals(expected, GDR.azEDRHead);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getLong(GDR_AZ_EDR_HEAD_FIELD_OFFSET));
    // }
    //
    // @Test
    // void testSetMAXgrEntry() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int expected = 2;
    // GDR.setMAXgrEntry(expected);
    // assertEquals(expected, GDR.mAXgrEntry);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getInt(GDR_MAX_GR_ENTRY_FIELD_OFFSET));
    // }
    //
    // @Test
    // void testSetMAXzEntry() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int expected = 3;
    // GDR.setMAXzEntry(expected);
    // assertEquals(expected, GDR.mAXzEntry);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getInt(GDR_MAX_Z_ENTRIES_FIELD_OFFSET));
    // }
    //
    // @Test
    // void testSetName() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int expected = 5;
    // GDR.setNum(expected);
    // assertEquals(expected, GDR.num);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getInt(GDR_NUM_FIELD_OFFSET));
    //
    // }
    //
    // @Test
    // void testSetNameTooLong() {
    //
    // final String tooLong = new String(new byte[GDR_NAME_FIELD_LENGTH + 1]);
    //
    // final Exception e = assertThrows(IllegalArgumentException.class, () -> {
    // new GDR().setName(tooLong);
    // });
    // assertEquals("GDR names cannot exceed " + GDR_NAME_FIELD_LENGTH + " bytes.
    // The supplied name, " + tooLong
    // + " is " + tooLong.length() + " bytes long.", e.getMessage());
    // }
    //
    // @Test
    // void testSetNgrEntries() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int expected = 4;
    // GDR.setNgrEntries(expected);
    // assertEquals(expected, GDR.ngrEntries);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getInt(GDR_NGR_ENTRIES_FIELD_OFFSET));
    // }
    //
    // @Test
    // void testSetNum() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int expected = 5;
    // GDR.setNum(expected);
    // assertEquals(expected, GDR.num);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getInt(GDR_NUM_FIELD_OFFSET));
    //
    //
    // @Test
    // void testSetNzEntries() {
    // final GDR GDR = newDefaultGDRInstance();
    // final int expected = 6;
    // GDR.setNzEntries(expected);
    // assertEquals(expected, GDR.nzEntries);
    //
    // final ByteBuffer GDRRecord = GDR.get();
    // assertEquals(expected, GDRRecord.getInt(GDR_NZ_ENTRIES_FIELD_OFFSET));
    // }


}
