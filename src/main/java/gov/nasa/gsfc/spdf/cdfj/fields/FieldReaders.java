package gov.nasa.gsfc.spdf.cdfj.fields;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import gov.nasa.gsfc.spdf.cdfj.records.RecordReaders;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FieldReaders {

    /**
     * Read null terminated string.
     *
     * @param source        the source
     * @param offset        the offset from the start of the buffer
     * @param maxStringSize the max string size
     * 
     * @return the string
     */
    public static String readNullTerminatedString(final ByteBuffer source, final int offset, final int maxStringSize) {

        ByteBuffer duplicate = source.duplicate();

        ByteBuffer slice = duplicate.position(offset)
                .slice();

        return readNullTerminatedString(slice, maxStringSize);
    }

    /**
     * Read null terminated string.
     * <p>
     * Reads from the buffer until the a NUL is hit or maxStringSize is reached.
     * 
     * @param source        the source buffer. Method will not mutate the buffer
     * @param maxStringSize the max string size
     * 
     * @return the string
     */
    public static String readNullTerminatedString(final ByteBuffer source, final int maxStringSize) {

        byte[] ba = new byte[maxStringSize];

        int i = 0;

        for (; i < ba.length; i++) {
            ba[i] = source.get(i);

            if (ba[i] == 0) {
                break;
            }

        }

        return new String(ba, 0, i, StandardCharsets.US_ASCII);
    }

    /**
     * Read null terminated string.
     *
     * @param source the source. The method will not mutate the channel
     * @param offset the offset in the channel to start reading
     * @param size   the number of bytes to read
     * 
     * @return the string
     * 
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException              Signals that an I/O exception has occurred.
     */
    public static String readNullTerminatedString(final FileChannel source, final long offset, final int size)
            throws IllegalArgumentException, IOException {

        ByteBuffer buffer = RecordReaders.read(source, offset, size);
        return readNullTerminatedString(buffer, buffer.capacity());
    }
}
