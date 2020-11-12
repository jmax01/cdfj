package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class VDR.
 *
 * @author nand
 */
public class VDR {

    ByteBuffer record = ByteBuffer.allocate(8 + 4 + 8 + 4 + 4 + 8 + 8 + 4 + 4 + 4 + (2 * 4) + 4 + 4 + 8 + 4 + 256 + 4);

    long vDRNext;

    long longMask = (1L << 32) - 1;

    byte[] padValues;

    /** The position. */
    protected int position;

    String sname;

    int dataType;

    int maxRec = -1;

    long vXRHead;

    long vXRTail = -1L;

    int flags;

    int sRecords = 0;

    /** The num elems. */
    protected int numElems = 1;

    int num;

    long cPROffset;

    int blockingFactor;

    byte[] name = new byte[256];

    int zNumDims;

    ByteBuffer dimBuf;

    /** The items per point. */
    protected int itemsPerPoint = 1;

    /** The efdim. */
    protected final List<Integer> efdim;

    /**
     * Instantiates a new vdr.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param varys    the varys
     */
    public VDR(final String name, final int dataType, final int[] dim, final boolean[] varys) {
        this(name, dataType, dim, varys, false);
    }

    /**
     * Instantiates a new vdr.
     *
     * @param name       the name
     * @param dataType   the data type
     * @param dim        the dim
     * @param varys      the varys
     * @param compressed the compressed
     */
    public VDR(final String name, final int dataType, final int[] dim, final boolean[] varys,
            final boolean compressed) {
        this(name, dataType, dim, varys, true, compressed, null, 1, SparseRecordOption.NONE);
    }

    /**
     * Instantiates a new vdr.
     *
     * @param name           the name
     * @param dataType       the data type
     * @param dim            the dim
     * @param varys          the varys
     * @param recordVariance the record variance
     * @param compressed     the compressed
     * @param pad            the pad
     * @param size           the size
     * @param option         the option
     */
    public VDR(final String name, final int dataType, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad, final int size,
            final SparseRecordOption option) {
        this.sname = name;
        setName(name);
        setDataType(dataType);

        if (dim.length != varys.length) {
            throw new IllegalArgumentException(
                    "Length of varys (" + varys.length + ") and dim arrays (" + dim.length + ") differ.");
        }

        this.numElems = size;
        this.itemsPerPoint = size;
        this.efdim = setDimensions(dim, varys, dataType);

        // setNumElems(dim, varys);
        if (compressed) {
            this.flags |= 0x04;
        }

        if (recordVariance) {
            this.flags |= 0x01;
        }

        setSparseRecordOption(option);

        if (pad != null) {

            Class<?> cl = pad.getClass();

            if (!cl.isArray()) {
                throw new IllegalArgumentException("Pad must be an array.");
            }

            Number[] _pad = null;

            if (cl.getComponentType() != String.class) {
                _pad = new Number[1];

                if (cl.getComponentType() == Double.TYPE) {
                    _pad[0] = ((double[]) pad)[0];
                }

                if (cl.getComponentType() == Float.TYPE) {
                    _pad[0] = ((float[]) pad)[0];
                }

                if (cl.getComponentType() == Integer.TYPE) {
                    _pad[0] = ((int[]) pad)[0];
                }

                if (cl.getComponentType() == Long.TYPE) {
                    _pad[0] = ((long[]) pad)[0];
                }

                if (cl.getComponentType() == Short.TYPE) {
                    _pad[0] = ((short[]) pad)[0];
                }

                if (cl.getComponentType() == Byte.TYPE) {
                    _pad[0] = ((byte[]) pad)[0];
                }

            }

            int category = DataTypes.typeCategory[dataType];
            this.flags |= 0x02;
            ByteBuffer buf = null;

            if ((category == DataTypes.SIGNED_INTEGER) || (category == DataTypes.UNSIGNED_INTEGER)) {

                if ((DataTypes.size[dataType] == 4) && (category == DataTypes.UNSIGNED_INTEGER)) {

                    long[] lvalues = new long[_pad.length];

                    for (int i = 0; i < lvalues.length; i++) {
                        lvalues[i] = _pad[i].longValue();
                    }

                    buf = ByteBuffer.allocate(4 * lvalues.length);
                    buf.order(ByteOrder.LITTLE_ENDIAN);

                    for (long lvalue : lvalues) {
                        buf.putInt((int) (lvalue & this.longMask));
                    }

                    buf.position(0);

                } else {

                    int[] values = new int[_pad.length];

                    for (int i = 0; i < values.length; i++) {
                        values[i] = _pad[i].intValue();
                    }

                    buf = ByteBuffer.allocate(DataTypes.size[dataType] * values.length);
                    buf.order(ByteOrder.LITTLE_ENDIAN);

                    if (DataTypes.size[dataType] == 1) {

                        for (int value : values) {
                            buf.put((byte) (value & 0xff));
                        }

                    } else {

                        if (DataTypes.size[dataType] == 2) {

                            for (int value : values) {
                                buf.putShort((short) (value & 0xffff));
                            }

                        } else {
                            buf.asIntBuffer()
                                    .put(values);
                        }

                    }

                }

            } else {

                if (category == DataTypes.FLOAT) {
                    float[] values = new float[_pad.length];

                    for (int i = 0; i < values.length; i++) {
                        values[i] = _pad[i].floatValue();
                    }

                    buf = ByteBuffer.allocate(4 * values.length);
                    buf.order(ByteOrder.LITTLE_ENDIAN);
                    buf.asFloatBuffer()
                            .put(values);
                } else {

                    if (category == DataTypes.DOUBLE) {
                        double[] values = new double[_pad.length];

                        for (int i = 0; i < values.length; i++) {
                            values[i] = _pad[i].doubleValue();
                        }

                        buf = ByteBuffer.allocate(8 * values.length);
                        buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.asDoubleBuffer()
                                .put(values);
                    } else {

                        if (category == DataTypes.LONG) {
                            long[] values = new long[_pad.length];

                            for (int i = 0; i < values.length; i++) {
                                values[i] = _pad[i].longValue();
                            }

                            buf = ByteBuffer.allocate(8 * values.length);
                            buf.order(ByteOrder.LITTLE_ENDIAN);
                            buf.asLongBuffer()
                                    .put(values);
                        } else {

                            if (category != DataTypes.STRING) {
                                throw new IllegalStateException("Unrecognized type pad value");
                            }

                            // String[] values = (String[])pad;
                            String[] values = { ((String[]) pad)[0] };
                            int len = values[0].length();
                            len *= values.length;
                            buf = ByteBuffer.allocate(len);

                            for (String value : values) {

                                buf.put(value.getBytes());

                                buf.position(0);
                            }

                        }

                    }

                }

            }

            this.padValues = new byte[buf.limit()];
            buf.position(0);
            buf.get(this.padValues);
        }

    }

    /**
     * Gets the.
     *
     * @return the byte buffer
     */
    public ByteBuffer get() {
        int capacity = this.record.capacity();

        if (this.padValues != null) {
            capacity += this.padValues.length;
        }

        if (this.zNumDims > 0) {
            capacity += this.dimBuf.capacity();
        }

        ByteBuffer buf = ByteBuffer.allocate(capacity);
        this.record.position(0);
        this.record.putLong(capacity);
        this.record.putInt(8);
        this.record.putLong(this.vDRNext);
        this.record.putInt(this.dataType);
        this.record.putInt(this.maxRec);
        this.record.putLong(this.vXRHead);
        this.record.putLong((this.vXRTail < 0) ? this.vXRHead : this.vXRTail);
        this.record.putInt(this.flags);
        this.record.putInt(this.sRecords);
        this.record.putInt(0);
        this.record.putInt(-1);
        this.record.putInt(-1);
        this.record.putInt(this.numElems);
        this.record.putInt(this.num);
        this.record.putLong(this.cPROffset);
        this.record.putInt(this.blockingFactor);
        this.record.put(this.name);
        this.record.putInt(this.zNumDims);
        this.record.position(0);
        buf.put(this.record);

        if (this.zNumDims > 0) {
            buf.put(this.dimBuf);
        }

        if (this.padValues != null) {
            buf.put(this.padValues);
        }

        buf.position(0);
        return buf;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.sname;
    }

    /**
     * Gets the num.
     *
     * @return the num
     */
    public int getNum() {
        return this.num;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        int size = this.record.capacity();

        if (this.zNumDims > 0) {
            size += this.dimBuf.capacity();
        }

        if (this.padValues != null) {
            size += this.padValues.length;
        }

        return size;
    }

    /**
     * Checks if is compressed.
     *
     * @return true, if is compressed
     */
    public boolean isCompressed() {
        return ((this.flags & 0x04) != 0);
    }

    /**
     * Sets the blocking factor.
     *
     * @param n the new blocking factor
     */
    public void setBlockingFactor(final int n) {
        this.blockingFactor = n;
    }

    /**
     * Sets the CPR offset.
     *
     * @param l the new CPR offset
     */
    public void setCPROffset(final long l) {
        this.cPROffset = l;
    }

    /**
     * Sets the data type.
     *
     * @param n the new data type
     */
    public void setDataType(final int n) {
        this.dataType = n;
    }

    /**
     * Sets the dimensions.
     *
     * @param dim      the dim
     * @param varys    the varys
     * @param dataType the data type
     *
     * @return the list
     */
    private List<Integer> setDimensions(final int[] dim, final boolean[] varys, final int dataType) {
        this.zNumDims = dim.length;

        if (dataType == 32) {
            this.itemsPerPoint = 2;
            this.zNumDims = 0;
        }

        if (this.zNumDims == 0) {
            return Collections.emptyList();
        }

        for (int i = 0; i < dim.length; i++) {

            if (varys[i]) {
                this.itemsPerPoint *= dim[i];
            }

        }

        List<Integer> dimensions = new ArrayList<>();
        this.dimBuf = ByteBuffer.allocate(4 * this.zNumDims * 2);

        for (int i = 0; i < this.zNumDims; i++) {
            this.dimBuf.putInt(dim[i]);
        }

        for (int i = 0; i < this.zNumDims; i++) {
            this.dimBuf.putInt(varys[i] ? -1 : 0);

            if (varys[i]) {
                dimensions.add(dim[i]);
            }

        }

        this.dimBuf.position(0);
        return Collections.unmodifiableList(dimensions);
    }

    /**
     * Sets the flags.
     *
     * @param n the new flags
     */
    public void setFlags(final int n) {
        this.flags = n;
    }

    /**
     * Sets the max rec.
     *
     * @param n the new max rec
     */
    public void setMaxRec(final int n) {
        this.maxRec = n;
    }

    /**
     * Sets the name.
     *
     * @param s the new name
     */
    public void setName(final String s) {
        byte[] bs = s.getBytes();
        int i = 0;

        for (; i < bs.length; i++) {
            this.name[i] = bs[i];
        }

        for (; i < this.name.length; i++) {
            this.name[i] = 0;
        }

    }

    /**
     * Sets the num.
     *
     * @param n the new num
     */
    public void setNum(final int n) {
        this.num = n;
    }

    /**
     * Sets the num elems.
     *
     * @param dim   the dim
     * @param varys the varys
     */
    public void setNumElems(final int[] dim, final boolean[] varys) {
        this.numElems = 1;
        /*
         * This is always 1 for numeric data
         * for (int i = 0; i < dim.length; i++) {
         * if (varys[i]) numElems *= dim[i];
         * }
         */
    }

    /**
     * Sets the sparse record option.
     *
     * @param option the new sparse record option
     */
    public void setSparseRecordOption(final SparseRecordOption option) {
        this.sRecords = option.getValue();
    }

    /**
     * Sets the VDR next.
     *
     * @param l the new VDR next
     */
    public void setVDRNext(final long l) {
        this.vDRNext = l;
    }

    /**
     * Sets the VXR head.
     *
     * @param l the new VXR head
     */
    public void setVXRHead(final long l) {
        this.vXRHead = l;
    }

    /**
     * Sets the VXR tail.
     *
     * @param l the new VXR tail
     */
    public void setVXRTail(final long l) {
        this.vXRTail = l;
    }
}
