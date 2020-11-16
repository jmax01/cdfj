package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import gov.nasa.gsfc.spdf.cdfj.CDFFactory.CDFSource;

final class CDF2Impl extends CDFImpl implements CDF2 {

    private static final long serialVersionUID = 9104124634870941614L;

    /** The GDR offset. */
    public int GDROffset;

    /** The var offset name. */
    public int VAR_OFFSET_NAME;

    /** The OFFSE T z num dims. */
    public int OFFSET_zNumDims;

    /** The var offset num elements. */
    public int VAR_OFFSET_NUM_ELEMENTS;

    /** The offset num. */
    public int OFFSET_NUM;

    /**
     * Instantiates a new CDF 2 impl.
     *
     * @param buf     the buf
     * @param release the release
     * @param ch      the ch
     */
    CDF2Impl(final ByteBuffer buf, final int release, final FileChannel ch) {
        super(buf, ch);

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
        getRecord(8);
        ibuf.position(2);
        ibuf.get();
        ibuf.position(3);
        ibuf.get();
        this.GDROffset = ibuf.get();
        this.version = ibuf.get();

        if (this.version != CDF_VERSION) {
            throw new IllegalArgumentException("Version " + this.version + "is not accepted by this reader.");
        }

        // read release field
        ibuf.get();
        this.encoding = ibuf.get();
        this.byteOrder = DataTypes.getByteOrder(this.encoding);
        setByteOrder(this.byteOrder);
        this.flags = ibuf.get();

        // rfuA
        ibuf.get();
        // rfuB
        ibuf.get();

        this.increment = ibuf.get();
        // validate and extract GDR info
        int pos = this.GDROffset + 4;
        buf.position(pos);
        int x;

        if ((x = buf.getInt()) != GDR_RECORD) {
            throw new IllegalArgumentException("Bad GDR type " + x);
        }

        // ByteBuffer globalDescriptorRecord = getRecord(this.GDROffset);
        // int zv = globalDescriptorRecord.getInt(8);

        this.rVDRHead = buf.getInt();

        this.zVDRHead = buf.getInt();
        this.ADRHead = buf.getInt();
        // eof
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
        variables();
        attributes();
    }

    /**
     * Instantiates a new CDF 2 impl.
     *
     * @param buf     the buf
     * @param release the release
     */
    CDF2Impl(final ByteBuffer buf, final int release) {
        this(buf, release, (FileChannel) null);
    }

    CDF2Impl(final ByteBuffer buf, final int release, final CDFSource cdfSource) {
        this(buf, release);
        this.source = cdfSource;
    }

    CDF2Impl(final ByteBuffer buf, final int release, final FileChannel ch, final CDFSource cdfSource) {
        this(buf, release, ch);
        this.source = cdfSource;
    }

    @Override
    public String getString(final long offset) {

        if (this.fileChannel == null) {
            return getString(offset, MAX_STRING_SIZE);
        }

        ByteBuffer _buf;

        try {
            _buf = getRecord(offset, MAX_STRING_SIZE);
        } catch (RuntimeException th) {
            th.printStackTrace();
            return null;
        }

        return getString(_buf, MAX_STRING_SIZE);
    }

    @Override
    public long longInt(final ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }

    @Override
    public int lowOrderInt(final ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }

    @Override
    public int lowOrderInt(final ByteBuffer byteBuffer, final int offset) {
        return byteBuffer.getInt(offset);
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

    @Override
    int readRecordSizeFieldAsInt(final ByteBuffer recordSizeFieldByteBuffer) {
        return recordSizeFieldByteBuffer.getInt(0);
    }
}
