package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

final class CDF2Impl extends CDFImpl implements CDF2, java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9104124634870941614L;

    public int GDROffset;

    public int VAR_OFFSET_NAME;

    public int OFFSET_zNumDims;

    public int VAR_OFFSET_NUM_ELEMENTS;

    public int OFFSET_NUM;

    FileChannel fc;

    public CDF2Impl(ByteBuffer buf, int release, FileChannel ch) throws Throwable {
        super(buf);
        this.fc = ch;

        if (release < 5) {
            this.VAR_OFFSET_NAME = 192;
            this.VAR_OFFSET_NUM_ELEMENTS = 48 + 128;
            this.OFFSET_NUM = 52 + 128;
        } else {
            this.VAR_OFFSET_NAME = 64;
            this.VAR_OFFSET_NUM_ELEMENTS = 48;
            this.OFFSET_NUM = 52;
        }

        this.OFFSET_zNumDims = this.VAR_OFFSET_NAME + MAX_STRING_SIZE;
        setOffsets();
        this.thisCDF = this;
        IntBuffer ibuf = buf.asIntBuffer();
        getRecord(0);
        ibuf.position(2);
        ibuf.get();
        ibuf.position(3);
        ibuf.get();
        this.GDROffset = ibuf.get();
        this.version = ibuf.get();

        if (this.version != CDF_VERSION) {
            throw new Throwable("Version " + this.version + "is not accepted by this reader.");
        }

        release = ibuf.get();
        this.encoding = ibuf.get();
        this.byteOrder = DataTypes.getByteOrder(this.encoding);
        setByteOrder(this.byteOrder);
        this.flags = ibuf.get();
        ibuf.get();
        ibuf.get();
        this.increment = ibuf.get();
        // validate and extract GDR info
        int pos = this.GDROffset + 4;
        buf.position(pos);
        int x;

        if ((x = buf.getInt()) != GDR_RECORD) {
            throw new Throwable("Bad GDR type " + x);
        }

        this.rVDRHead = buf.getInt();
        this.zVDRHead = buf.getInt();
        this.ADRHead = buf.getInt();
        buf.getInt();
        this.numberOfRVariables = buf.getInt();
        this.numberOfAttributes = buf.getInt();
        buf.getInt(); // skip rMaxRec
        int numberOfRDims = buf.getInt();
        this.numberOfZVariables = buf.getInt();
        buf.getInt(); // skip UIRhead
        this.rDimSizes = new int[numberOfRDims];

        if (numberOfRDims > 0) { // skip next 3 integer fields
            buf.getInt();
            buf.getInt();
            buf.getInt();

            for (int i = 0; i < this.rDimSizes.length; i++) {
                this.rDimSizes[i] = buf.getInt();
            }

        }

        buf.position(0);
        this.variableTable = variables();
        this.attributeTable = attributes();
    }

    protected CDF2Impl(ByteBuffer buf, int release) throws Throwable {
        this(buf, release, null);
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
        return buf.getInt();
    }

    @Override
    public int lowOrderInt(ByteBuffer buf) {
        return buf.getInt();
    }

    @Override
    public int lowOrderInt(ByteBuffer buf, int offset) {
        return buf.getInt(offset);
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
        this.offset_VAR_NAME = this.VAR_OFFSET_NAME;
        this.offset_VAR_NUM_ELEMENTS = this.VAR_OFFSET_NUM_ELEMENTS;
        this.offset_NUM = this.OFFSET_NUM;
        this.offset_FLAGS = OFFSET_FLAGS;
        this.offset_sRecords = OFFSET_SRECORDS;
        this.offset_BLOCKING_FACTOR = OFFSET_BLOCKING_FACTOR;
        this.offset_VAR_DATATYPE = VAR_OFFSET_DATATYPE;
        this.offset_zNumDims = this.OFFSET_zNumDims;
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
