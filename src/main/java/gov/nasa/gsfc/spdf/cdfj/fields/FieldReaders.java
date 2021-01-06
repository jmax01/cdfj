package gov.nasa.gsfc.spdf.cdfj.fields;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

import gov.nasa.gsfc.spdf.cdfj.records.*;
import lombok.experimental.*;

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

        duplicate.position(offset);

        return readNullTerminatedString(duplicate.slice(), maxStringSize);
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

        source.get(ba);

        int i = 0;

        for (; i < ba.length; i++) {

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
