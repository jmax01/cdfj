package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 *
 * @author nand
 */
public class VariableDataBuffer {

    int firstRecord;

    int lastRecord;

    ByteBuffer buffer;

    boolean compressed;

    VariableDataBuffer(int first, int last, ByteBuffer buf, boolean comp) {
        this.firstRecord = first;
        this.lastRecord = last;
        this.buffer = buf;
        this.compressed = comp;
    }

    /**
     *
     * @return
     */
    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    /**
     *
     * @return
     */
    public int getFirstRecord() {
        return this.firstRecord;
    }

    /**
     *
     * @return
     */
    public int getLastRecord() {
        return this.lastRecord;
    }

    /**
     *
     * @return
     */
    public boolean isCompressed() {
        return this.compressed;
    }
}
