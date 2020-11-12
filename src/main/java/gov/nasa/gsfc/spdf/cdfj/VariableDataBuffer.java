package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * The Class VariableDataBuffer.
 *
 * @author nand
 */
public class VariableDataBuffer {

    int firstRecord;

    int lastRecord;

    ByteBuffer buffer;

    boolean compressed;

    VariableDataBuffer(final int first, final int last, final ByteBuffer buf, final boolean comp) {
        this.firstRecord = first;
        this.lastRecord = last;
        this.buffer = buf;
        this.compressed = comp;
    }

    /**
     * Gets the buffer.
     *
     * @return the buffer
     */
    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    /**
     * Gets the first record.
     *
     * @return the first record
     */
    public int getFirstRecord() {
        return this.firstRecord;
    }

    /**
     * Gets the last record.
     *
     * @return the last record
     */
    public int getLastRecord() {
        return this.lastRecord;
    }

    /**
     * Checks if is compressed.
     *
     * @return true, if is compressed
     */
    public boolean isCompressed() {
        return this.compressed;
    }
}
