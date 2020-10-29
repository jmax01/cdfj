package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 *
 * @author nand
 */
public class GDR {

    ByteBuffer record = ByteBuffer.allocate(8 + 4 + 8 + 8 + 8 + 8 + 4 + 4 + 4 + 4 + 4 + 8 + 4 + 4 + 4);

    /**
     *
     */
    protected long position;

    long zVDRHead;

    long aDRHead;

    long eof;

    int numAttr;

    int nzVars;

    int lastLeapSecondId;

    /**
     *
     * @return
     */
    public ByteBuffer get() {
        this.record.position(0);
        this.record.putLong(this.record.capacity());
        this.record.putInt(2);
        this.record.putLong(0);
        this.record.putLong(this.zVDRHead);
        this.record.putLong(this.aDRHead);
        this.record.putLong(this.eof);
        this.record.putInt(0);
        this.record.putInt(this.numAttr);
        this.record.putInt(-1);
        this.record.putInt(0);
        this.record.putInt(this.nzVars);
        this.record.putLong(0);
        this.record.putInt(0);
        this.record.putInt(this.lastLeapSecondId);
        this.record.putInt(0);
        this.record.position(0);
        return this.record;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return this.record.limit();
    }

    /**
     *
     * @param l
     */
    public void setADRHead(long l) {
        this.aDRHead = l;
    }

    /**
     *
     * @param l
     */
    public void setEof(long l) {
        this.eof = l;
    }

    /**
     *
     * @param n
     */
    public void setLastLeapSecondId(int n) {
        this.lastLeapSecondId = n;
    }

    /**
     *
     * @param n
     */
    public void setNumAttr(int n) {
        this.numAttr = n;
    }

    /**
     *
     * @param n
     */
    public void setNzVars(int n) {
        this.nzVars = n;
    }

    /**
     *
     * @param l
     */
    public void setZVDRHead(long l) {
        this.zVDRHead = l;
    }
}
