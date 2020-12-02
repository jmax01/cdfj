package gov.nasa.gsfc.spdf.cdfj.fields;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import gov.nasa.gsfc.spdf.cdfj.records.RecordReaders;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RecordSizeFields {

    /** The Constant RECORD_SIZE_FIELD_SIZE_V2_IN_BYTES. */
    public static final int RECORD_SIZE_FIELD_SIZE_V2_IN_BYTES = 4;

    /** The Constant RECORD_SIZE_FIELD_SIZE_V3_IN_BYTES. */
    public static final int RECORD_SIZE_FIELD_SIZE_V3_IN_BYTES = 8;

    /**
     * Read V3 record size field.
     *
     * @param fileChannel the file channel
     * @param offset      the offset
     * 
     * @return the recordSize
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int readV3RecordSizeField(final FileChannel fileChannel, final long offset) throws IOException {

        return readRecordSizeField(fileChannel, offset, RECORD_SIZE_FIELD_SIZE_V3_IN_BYTES);

    }

    /**
     * Read v3 record size field.
     *
     * @param source the source
     * @param offset the offset
     * 
     * @return the long
     */
    public static long readV3RecordSizeField(final ByteBuffer source, int offset) {

        return source.getLong(offset);
    }

    /**
     * Read V2 record size field.
     *
     * @param fileChannel the file channel
     * @param offset      the offset
     * 
     * @return the recordSize
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int readV2RecordSizeField(final FileChannel fileChannel, final long offset) throws IOException {

        return readRecordSizeField(fileChannel, offset, RECORD_SIZE_FIELD_SIZE_V2_IN_BYTES);
    }

    public static int readV2RecordSizeField(final ByteBuffer source, int offset) {

        return source.getInt(offset);
    }

    /**
     * Read record size field.
     *
     * @param fileChannel                the file channel
     * @param offset                     the offset
     * @param recordSizeFieldSizeInBytes the record size field size in bytes
     * 
     * @return the int
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int readRecordSizeField(final FileChannel fileChannel, final long offset,
            final int recordSizeFieldSizeInBytes) throws IOException {

        ByteBuffer recordSizeFieldByteBuffer = RecordReaders.read(fileChannel, offset, recordSizeFieldSizeInBytes);

        int size = readRecordSizeFromBuffer(recordSizeFieldByteBuffer, recordSizeFieldSizeInBytes);

        return size;
    }

    private static int readRecordSizeFromBuffer(ByteBuffer recordSizeFieldByteBuffer,
            final int recordSizeFieldSizeInBytes) {

        int size = Long.BYTES == recordSizeFieldSizeInBytes ? (int) recordSizeFieldByteBuffer.getLong(0)
                : recordSizeFieldByteBuffer.getInt(0);

        return size;
    }

}
