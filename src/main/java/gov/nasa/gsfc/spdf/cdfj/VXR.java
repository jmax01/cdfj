package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * The Class VXR.
 *
 * @author nand
 */
public class VXR {

    ByteBuffer record = ByteBuffer.allocate(8 + 4 + 8 + 4 + 4);

    long vXRNext = 0L;

    /** The position. */
    protected int position;

    /** The num entries. */
    protected int numEntries;

    ByteBuffer firstbuf;

    ByteBuffer lastbuf;

    ByteBuffer locbuf;

    /**
     * Gets the.
     *
     * @return the byte buffer
     */
    public ByteBuffer get() {
        int capacity = this.record.capacity() + (16 * this.numEntries);
        ByteBuffer.allocate(capacity);
        this.record.position(0);
        this.record.putLong(capacity);
        this.record.putInt(6);
        this.record.putLong(this.vXRNext);
        this.record.putInt(this.numEntries);
        this.record.putInt(this.numEntries);
        this.record.position(0);
        /*
         * buf.put(record);
         * buf.put(firstbuf);
         * buf.put(lastbuf);
         * buf.put(locbuf);
         * buf.position(0);
         */
        return this.record;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        int size = this.record.capacity() + (16 * this.numEntries);
        return size;
    }

    /**
     * Sets the locations.
     *
     * @param locs the new locations
     */
    public void setLocations(final List<int[]> locs) {
        this.numEntries = locs.size();
        this.firstbuf = ByteBuffer.allocate(4 * this.numEntries);
        this.lastbuf = ByteBuffer.allocate(4 * this.numEntries);
        this.locbuf = ByteBuffer.allocate(8 * this.numEntries);

        for (int i = 0; i < this.numEntries; i++) {
            int[] locarr = locs.get(i);
            this.firstbuf.putInt(locarr[0]);
            this.lastbuf.putInt(locarr[1]);
            this.locbuf.putLong(locarr[2]);
        }

        this.firstbuf.position(0);
        this.lastbuf.position(0);
        this.locbuf.position(0);
    }

    /**
     * Sets the VXR next.
     *
     * @param l the new VXR next
     */
    public void setVXRNext(final long l) {
        this.vXRNext = l;
    }
}
