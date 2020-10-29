package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

final class CDF3Impl extends CDFImpl implements CDF3, java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6571034001247038621L;

    public long GDROffset;

    FileChannel fc;

    protected CDF3Impl(ByteBuffer buf) throws Throwable {
        this(buf, null);
    }

    protected CDF3Impl(ByteBuffer buf, FileChannel ch) throws Throwable {
        super(buf);
        this.fc = ch;
        setOffsets();
        this.thisCDF = this;
        IntBuffer ibuf = buf.asIntBuffer();
        super.getRecord(0);
        ibuf.position(2); // skip magic numbers
        ibuf.get(); // Record Size
        ibuf.get(); // Record Size
        ibuf.get(); // Record Type
        this.GDROffset = ibuf.get();
        this.GDROffset = (this.GDROffset << 32) + ibuf.get();
        this.version = ibuf.get();

        if (this.version != CDF_VERSION) {
            throw new Throwable("Version " + this.version + "is not accepted by this reader.");
        }

        this.release = ibuf.get();
        this.encoding = ibuf.get();
        this.byteOrder = DataTypes.getByteOrder(this.encoding);
        setByteOrder(this.byteOrder);
        this.flags = ibuf.get();
        ibuf.get();
        ibuf.get();
        this.increment = ibuf.get();
        // validate and extract GDR info
        int pos = (int) this.GDROffset + 8;
        buf.position(pos);
        int x;

        if ((x = buf.getInt()) != GDR_RECORD) {
            throw new Throwable("Bad GDR type " + x);
        }

        this.rVDRHead = buf.getLong();
        this.zVDRHead = buf.getLong();
        this.ADRHead = buf.getLong();
        buf.getLong();
        this.numberOfRVariables = buf.getInt();
        this.numberOfAttributes = buf.getInt();
        buf.getInt(); // skip rMaxRec
        int numberOfRDims = buf.getInt();
        this.numberOfZVariables = buf.getInt();
        buf.getInt(); // skip UIRhead
        buf.getInt(); // skip UIRhead
        buf.getInt(); // skip rfuC
        this.lastLeapSecondId = buf.getInt(); // since 3.6
        this.rDimSizes = new int[numberOfRDims];

        if (numberOfRDims > 0) { // skip next integer field
            buf.getInt();

            for (int i = 0; i < this.rDimSizes.length; i++) {
                this.rDimSizes[i] = buf.getInt();
            }

        }

        buf.position(0);
        // if (ch == null) {
        this.variableTable = variables();
        this.attributeTable = attributes();
        // }
    }

    @Override
    public String getString(long offset) {

        if (this.fc == null) {
            return getString(offset, MAX_STRING_SIZE);
        }

        ByteBuffer _buf;

        try {
            _buf = getRecord(offset, MAX_STRING_SIZE);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }

        return getString(_buf, MAX_STRING_SIZE);
    }

    @Override
    public long longInt(ByteBuffer buf) {
        return buf.getLong();
    }

    @Override
    public int lowOrderInt(ByteBuffer buf) {
        return (int) buf.getLong();
    }

    @Override
    public int lowOrderInt(ByteBuffer buf, int offset) {
        return (int) buf.getLong(offset);
    }

    @Override
    protected ByteBuffer getRecord(long offset) {

        if (this.fc == null) {
            return super.getRecord(offset);
        }

        ByteBuffer lenBuf = ByteBuffer.allocate(4);

        synchronized (this.fc) {

            try {
                this.fc.position(offset + 4);
                this.fc.read(lenBuf);
                int size = lenBuf.getInt(0);
                return getRecord(offset, size);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return null;
            }

        }

    }

    protected ByteBuffer getRecord(long offset, int size) throws Throwable {
        ByteBuffer bb = ByteBuffer.allocate(size);
        this.fc.position(offset);
        int got = this.fc.read(bb);

        if (got != size) {
            System.out.println("Needed " + size + " bytes. Got " + got);
            return null;
        }

        bb.position(0);
        return bb;
    }

    void setOffsets() {
        this.offset_NEXT_VDR = OFFSET_NEXT_VDR;
        this.offset_NEXT_ADR = OFFSET_NEXT_ADR;
        this.offset_ATTR_NAME = ATTR_OFFSET_NAME;
        this.offset_SCOPE = OFFSET_SCOPE;
        this.offset_AgrEDRHead = AGR_EDRHEAD_OFFSET;
        this.offset_AzEDRHead = AZ_EDRHEAD_OFFSET;
        this.offset_NEXT_AEDR = OFFSET_NEXT_AEDR;
        this.offset_ENTRYNUM = OFFSET_ENTRYNUM;
        this.offset_ATTR_DATATYPE = ATTR_OFFSET_DATATYPE;
        this.offset_ATTR_NUM_ELEMENTS = ATTR_OFFSET_NUM_ELEMENTS;
        this.offset_VALUE = OFFSET_VALUE;
        this.offset_VAR_NAME = VAR_OFFSET_NAME;
        this.offset_VAR_NUM_ELEMENTS = VAR_OFFSET_NUM_ELEMENTS;
        this.offset_NUM = OFFSET_NUM;
        this.offset_FLAGS = OFFSET_FLAGS;
        this.offset_sRecords = OFFSET_SRECORDS;
        this.offset_BLOCKING_FACTOR = OFFSET_BLOCKING_FACTOR;
        this.offset_VAR_DATATYPE = VAR_OFFSET_DATATYPE;
        this.offset_zNumDims = OFFSET_Z_NUMDIMS;
        this.offset_FIRST_VXR = OFFSET_FIRST_VXR;
        this.offset_NEXT_VXR = OFFSET_NEXT_VXR;
        this.offset_NENTRIES = OFFSET_NENTRIES;
        this.offset_NUSED = OFFSET_NUSED;
        this.offset_FIRST = OFFSET_FIRST;
        this.offset_RECORD_TYPE = OFFSET_RECORD_TYPE;
        this.offset_RECORDS = OFFSET_RECORDS;
        this.offset_CSIZE = OFFSET_CSIZE;
        this.offset_CDATA = OFFSET_CDATA;
    }

}
