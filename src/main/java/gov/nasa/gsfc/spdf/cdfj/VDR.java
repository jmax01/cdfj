package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

/**
 *
 * @author nand
 */
public class VDR {

    ByteBuffer record = ByteBuffer.allocate(8 + 4 + 8 + 4 + 4 + 8 + 8 + 4 + 4 + 4 + (2 * 4) + 4 + 4 + 8 + 4 + 256 + 4);

    long vDRNext;

    long longMask = (1L << 32) - 1;

    byte[] padValues;

    /**
     *
     */
    protected int position;

    String sname;

    int dataType;

    int maxRec = -1;

    long vXRHead;

    long vXRTail = -1L;

    int flags;

    int sRecords = 0;

    /**
     *
     */
    protected int numElems = 1;

    int num;

    long cPROffset;

    int blockingFactor;

    byte[] name = new byte[256];

    int zNumDims;

    ByteBuffer dimBuf;

    /**
     *
     */
    protected int itemsPerPoint = 1;

    /**
     *
     */
    protected Vector<Integer> efdim;

    /**
     *
     * @param string
     * @param i
     * @param ints
     * @param blns
     *
     * @throws Throwable
     */
    public VDR(String name, int dataType, int[] dim, boolean[] varys) throws Throwable {
        this(name, dataType, dim, varys, false);
    }

    /**
     *
     * @param string
     * @param i
     * @param ints
     * @param blns
     * @param bln
     *
     * @throws Throwable
     */
    public VDR(String name, int dataType, int[] dim, boolean[] varys, boolean compressed) throws Throwable {
        this(name, dataType, dim, varys, true, compressed, null, 1, SparseRecordOption.NONE);
    }

    /**
     *
     * @param string
     * @param i
     * @param ints
     * @param blns
     * @param bln
     * @param bln1
     * @param o
     * @param i1
     * @param sro
     *
     * @throws Throwable
     */
    public VDR(String name, int dataType, int[] dim, boolean[] varys, boolean recordVariance, boolean compressed,
            Object pad, int size, SparseRecordOption option) throws Throwable {
        this.sname = name;
        setName(name);
        setDataType(dataType);

        if (dim.length != varys.length) {
            throw new Throwable("Length of varys and dim arrays differ.");
        }

        this.numElems = size;
        this.itemsPerPoint = size;
        setDimensions(dim, varys, dataType);

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
                throw new Throwable("Pad must be an array.");
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
                            buf.asIntBuffer().put(values);
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
                    buf.asFloatBuffer().put(values);
                } else {

                    if (category == DataTypes.DOUBLE) {
                        double[] values = new double[_pad.length];

                        for (int i = 0; i < values.length; i++) {
                            values[i] = _pad[i].doubleValue();
                        }

                        buf = ByteBuffer.allocate(8 * values.length);
                        buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.asDoubleBuffer().put(values);
                    } else {

                        if (category == DataTypes.LONG) {
                            long[] values = new long[_pad.length];

                            for (int i = 0; i < values.length; i++) {
                                values[i] = _pad[i].longValue();
                            }

                            buf = ByteBuffer.allocate(8 * values.length);
                            buf.order(ByteOrder.LITTLE_ENDIAN);
                            buf.asLongBuffer().put(values);
                        } else {

                            if (category != DataTypes.STRING) {
                                throw new Throwable("Unrecognized type " + " pad value");
                            }

                            // String[] values = (String[])pad;
                            String[] values = new String[] { ((String[]) pad)[0] };
                            int len = values[0].length();
                            len *= values.length;
                            buf = ByteBuffer.allocate(len);

                            for (String value : values) {

                                try {
                                    buf.put(value.getBytes());
                                } catch (Exception ex) {
                                    throw new Throwable("encoding");
                                }

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
     *
     * @return
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
     *
     * @return
     */
    public String getName() {
        return this.sname;
    }

    /**
     *
     * @return
     */
    public int getNum() {
        return this.num;
    }

    /**
     *
     * @return
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
     *
     * @return
     */
    public boolean isCompressed() {
        return ((this.flags & 0x04) != 0);
    }

    /**
     *
     * @param n
     */
    public void setBlockingFactor(int n) {
        this.blockingFactor = n;
    }

    /**
     *
     * @param l
     */
    public void setCPROffset(long l) {
        this.cPROffset = l;
    }

    /**
     *
     * @param n
     */
    public void setDataType(int n) {
        this.dataType = n;
    }

    /**
     *
     * @param dim
     * @param varys
     * @param dataType
     */
    public void setDimensions(int[] dim, boolean[] varys, int dataType) {
        this.zNumDims = dim.length;

        if (dataType == 32) {
            this.itemsPerPoint = 2;
            this.zNumDims = 0;
        }

        this.efdim = new Vector<>();

        if (this.zNumDims == 0) {
            return;
        }

        for (int i = 0; i < dim.length; i++) {

            if (varys[i]) {
                this.itemsPerPoint *= dim[i];
            }

        }

        this.dimBuf = ByteBuffer.allocate(4 * this.zNumDims * 2);

        for (int i = 0; i < this.zNumDims; i++) {
            this.dimBuf.putInt(dim[i]);
        }

        for (int i = 0; i < this.zNumDims; i++) {
            this.dimBuf.putInt(varys[i] ? -1 : 0);

            if (varys[i]) {
                this.efdim.add(dim[i]);
            }

        }

        this.dimBuf.position(0);
    }

    /**
     *
     * @param n
     */
    public void setFlags(int n) {
        this.flags = n;
    }

    /**
     *
     * @param n
     */
    public void setMaxRec(int n) {
        this.maxRec = n;
    }

    /**
     *
     * @param s
     */
    public void setName(String s) {
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
     *
     * @param n
     */
    public void setNum(int n) {
        this.num = n;
    }

    /**
     *
     * @param dim
     * @param varys
     */
    public void setNumElems(int[] dim, boolean[] varys) {
        this.numElems = 1;
        /*
         * This is always 1 for numeric data
         * for (int i = 0; i < dim.length; i++) {
         * if (varys[i]) numElems *= dim[i];
         * }
         */
    }

    /**
     *
     * @param option
     */
    public void setSparseRecordOption(SparseRecordOption option) {
        this.sRecords = option.getValue();
    }

    /**
     *
     * @param l
     */
    public void setVDRNext(long l) {
        this.vDRNext = l;
    }

    /**
     *
     * @param l
     */
    public void setVXRHead(long l) {
        this.vXRHead = l;
    }

    /**
     *
     * @param l
     */
    public void setVXRTail(long l) {
        this.vXRTail = l;
    }
}
