package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * The Class CPR.
 *
 * @author nand
 */
public class CPR {

    ByteBuffer record = ByteBuffer.allocate(
            8/* RecordSize */ + 4/* RecordType */ + 4/* cType */ + 4/* rfuA */ + 4/* pCount */ + 4/* cParms */);

    /** The position. */
    protected long position;

    /**
     * Gets the.
     *
     * @return the byte buffer
     */
    public ByteBuffer get() {
        this.record.position(0);
        this.record.putLong(this.record.capacity());
        this.record.putInt(11);
        this.record.putInt(5);
        this.record.putInt(0);
        this.record.putInt(1);
        this.record.putInt(9);
        this.record.position(0);
        return this.record;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        return this.record.capacity();
    }
}
