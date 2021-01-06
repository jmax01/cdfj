package gov.nasa.gsfc.spdf.cdfj.records;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import gov.nasa.gsfc.spdf.cdfj.fields.*;
import lombok.experimental.*;

/**
 * The Class RecordReaders.
 */
@UtilityClass
public class RecordReaders {

    /**
     * Read V 3 record.
     *
     * @param source the source
     * @param offset the offset
     * 
     * @return the byte buffer
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ByteBuffer readV3Record(final FileChannel source, final long offset) throws IOException {

        int recordSize = RecordSizeFields.readV3RecordSizeField(source, offset);

        return read(source, offset, recordSize);

    }

    /**
     * Read V 3 record.
     *
     * @param source the source
     * @param offset the offset
     * 
     * @return the byte buffer
     */
    public static ByteBuffer readV3Record(final ByteBuffer source, final int offset) {

        int recordSize = (int) RecordSizeFields.readV3RecordSizeField(source, offset);

        return read(source, offset, recordSize);

    }

    /**
     * Read V 2 record.
     *
     * @param source the source
     * @param offset the offset
     * 
     * @return the byte buffer
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ByteBuffer readV2Record(final FileChannel source, final long offset) throws IOException {

        int recordSize = RecordSizeFields.readV2RecordSizeField(source, offset);

        return read(source, offset, recordSize);

    }

    /**
     * Read V 2 record.
     *
     * @param source the source
     * @param offset the offset
     * 
     * @return the byte buffer
     */
    public static ByteBuffer readV2Record(final ByteBuffer source, final int offset) {

        int recordSize = RecordSizeFields.readV2RecordSizeField(source, offset);

        return read(source, offset, recordSize);

    }

    /**
     * Reads a file channel into a new byte buffer
     * <p>
     *
     * @param fileChannel                the file channel to read
     * @param offset                     the offset to start reading
     * @param recordSizeFieldSizeInBytes the record size field size in bytes
     *
     * @return the byte buffer
     *
     * @throws IOException on error
     */
    static ByteBuffer readRecord(final FileChannel fileChannel, final long offset, final int recordSizeFieldSizeInBytes)
            throws IOException {

        int size = RecordSizeFields.readRecordSizeField(fileChannel, offset, recordSizeFieldSizeInBytes);

        try {
            return read(fileChannel, offset, size);
        }
        catch (IOException e) {
            throw new IOException("Failed to read " + size + " byte record at, offset, " + offset + ".", e);
        }

    }

    /**
     * Reads from the supplied {@link FileChannel} into a {@link ByteBuffer} of the supplied size from the supplied
     * offset in the {@link FileChannel}.
     *
     * @param source the source
     * @param offset the offset to start reading
     * @param size   number of bytes to read
     * 
     * @return the byte buffer with the data
     */
    public static ByteBuffer read(final ByteBuffer source, final long offset, final int size) {

        byte[] asByteArray = new byte[size];

        for (int i = (int) offset, j = (int) offset; i < (int) offset + size; i++, j++) {
            asByteArray[i] = source.get(j);
        }

        ByteBuffer bb = ByteBuffer.wrap(asByteArray);
        bb.position(0);
        return bb;

    }

    /**
     * Reads from the supplied {@link FileChannel} into a {@link ByteBuffer} of the supplied size from the supplied
     * offset in the {@link FileChannel}.
     *
     * @param fileChannel the file channel
     * @param offset      the offset to start reading
     * @param size        number of bytes to read
     * 
     * @return the byte buffer with the data
     * 
     * @throws IllegalArgumentException If size copied is less than the supplied size
     * @throws IOException              Signals If the read fails an {@link UncheckedIOException} is thrown
     */
    public static ByteBuffer read(final FileChannel fileChannel, final long offset, final int size)
            throws IllegalArgumentException, IOException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);

        try {

            int got = fileChannel.read(byteBuffer, offset);

            if (got != size) {

                throw new IllegalArgumentException(String
                        .format("Failed to read at offset %s, needed %s bytes, got %s bytes.", offset, size, got));
            }

            byteBuffer.position(0);
            return byteBuffer;
        }
        catch (IOException e) {
            throw new IOException(String.format("Failed to read at offset %s, with size %s", offset, size), e);
        }

    }

}
