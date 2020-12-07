package gov.nasa.gsfc.spdf.cdfj.fields;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import lombok.experimental.*;

@UtilityClass
public final class NameFields {

    /** The Constant NAME_FIELD_SIZE_V2_IN_BYTES. */
    public static final int NAME_FIELD_SIZE_V2_IN_BYTES = 64;

    /** The Constant NAME_FIELD_SIZE_V3_IN_BYTES. */
    public static final int NAME_FIELD_SIZE_V3_IN_BYTES = 256;

    /**
     * Read a v2 name field at the supplied offset
     *
     * @param source the file channel
     * @param offset the offset in the channel to start reading
     *
     * @return the string
     *
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException              Signals that an I/O exception has occurred.
     */
    public static String readV2NameField(final FileChannel source, final long offset)
            throws IllegalArgumentException, IOException {

        return FieldReaders.readNullTerminatedString(source, offset, NAME_FIELD_SIZE_V2_IN_BYTES);
    }

    /**
     * Read v2 name field.
     *
     * @param source the source buffer. Method will not mutate the buffer
     * 
     * @return the string
     * 
     * @throws IllegalArgumentException the illegal argument exception
     */
    public static String readV2NameField(final ByteBuffer source) throws IllegalArgumentException {

        return readNameField(source, NAME_FIELD_SIZE_V2_IN_BYTES);
    }

    /**
     * Read V2 name field.
     *
     * @param byteBufferSource the byte buffer source
     * @param offset           the offset
     * 
     * @return the string
     */
    public static String readV2NameField(final ByteBuffer byteBufferSource, final int offset) {
        return readNameField(byteBufferSource, offset, NAME_FIELD_SIZE_V2_IN_BYTES);
    }

    /**
     * Read v3 name field.
     *
     * @param source the file channel
     * @param offset the offest to start reading
     * 
     * @return the string
     * 
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException              Signals that an I/O exception has occurred.
     */
    public static String readV3NameField(final FileChannel source, final long offset)
            throws IllegalArgumentException, IOException {

        return FieldReaders.readNullTerminatedString(source, offset, NAME_FIELD_SIZE_V3_IN_BYTES);
    }

    /**
     * Read v3 name field from the supplied {@link ByteBuffer}
     *
     * @param source the source buffer.
     * 
     * @return the string
     * 
     * @throws IllegalArgumentException the illegal argument exception if the buffer is shorter
     */
    public static String readV3NameField(final ByteBuffer source) {

        return readNameField(source, NAME_FIELD_SIZE_V3_IN_BYTES);
    }

    public static String readV3NameField(final ByteBuffer source, final int offset) {

        return readNameField(source, offset, NAME_FIELD_SIZE_V3_IN_BYTES);
    }

    static String readNameField(final FileChannel fileChannel, final long offset, final int size)
            throws IllegalArgumentException, IOException {

        return FieldReaders.readNullTerminatedString(fileChannel, offset, size);
    }

    static String readNameField(final ByteBuffer byteBuffer, final int offset, final int size) {
        return FieldReaders.readNullTerminatedString(byteBuffer, offset, size);
    }

    static String readNameField(final ByteBuffer byteBuffer, final int size) {
        return FieldReaders.readNullTerminatedString(byteBuffer, size);
    }
}
