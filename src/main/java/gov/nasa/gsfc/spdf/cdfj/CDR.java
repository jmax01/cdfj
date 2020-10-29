package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 *
 * @author nand
 */
public class CDR {

    static int libraryVersion = 3;

    static int libraryRelease = 7;

    static int libraryIncrement = 1;

    static int libraryIdentifier = 1;

    static final byte[] magic = new byte[] { (byte) 0xCD, (byte) 0xF3, 0, 1, 0, 0, (byte) 0xFF, (byte) 0xFF };

    static String copyRight = "\012Common Data Format (CDF)\012https://cdf.gsfc.nasa.gov\012Space Physics Data Facility\012NASA/Goddard Space Flight Center\012Greenbelt, Maryland 20771 USA\012(User support: gsfc-cdf-support@lists.nasa.gov)\012";

    int encoding = 6;

    int flags = 0x2; // single file always

    ByteBuffer record = ByteBuffer.allocate(8 + 4 + 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 256);

    /**
     *
     * @return
     */
    public ByteBuffer get() {
        this.record.position(0);
        this.record.putLong(this.record.capacity());
        this.record.putInt(1);
        this.record.putLong((long) (this.record.capacity()) + magic.length);
        this.record.putInt(libraryVersion);
        this.record.putInt(libraryRelease);
        this.record.putInt(this.encoding);
        this.record.putInt(this.flags);
        this.record.putInt(0);
        this.record.putInt(0);
        this.record.putInt(libraryIncrement);
        this.record.putInt(libraryIdentifier);
        this.record.putInt(0);
        this.record.put(copyRight.getBytes());
        int len = 256 - copyRight.length();
        this.record.put(String.format("%-" + len + "." + len + "s", " ").getBytes());
        // for (int i = copyRight.length(); i < 256; i++) {
        // record.put((byte)0x20);
        // }
        this.record.position(0);
        ByteBuffer buf = ByteBuffer.allocate(this.record.capacity() + magic.length);
        buf.put(magic);
        buf.put(this.record);
        buf.position(0);
        return buf;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return this.record.capacity() + magic.length;
    }

    /**
     *
     * @param enc
     */
    public void setEncoding(int enc) {
        this.encoding = enc;
    }

    /**
     *
     * @param needDigest
     */
    public void setMD5Needed(boolean needDigest) {

        if (needDigest) {
            this.flags |= 0xc;
        }

        if (!needDigest) {
            this.flags &= 0xffff_fff3;
        }

    }

    /**
     *
     * @param majority
     */
    public void setRowMajority(boolean majority) {

        if (majority) {
            this.flags |= 1;
        }

        if (!majority) {
            this.flags &= 0xffff_fffe;
        }

    }
}
