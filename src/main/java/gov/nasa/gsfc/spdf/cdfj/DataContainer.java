package gov.nasa.gsfc.spdf.cdfj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

/**
 * The Class DataContainer.
 *
 * @author nand
 */
public class DataContainer {

    static final int VVR_PREAMBLE = 12;

    static final int CVVR_PREAMBLE = 24;

    VDR vdr;

    VXR vxr;

    CopyOnWriteArrayList<Integer> firstRecords = new CopyOnWriteArrayList<>();

    CopyOnWriteArrayList<Integer> lastRecords = new CopyOnWriteArrayList<>();

    CopyOnWriteArrayList<ByteBuffer> bufs = new CopyOnWriteArrayList<>();

    List<Integer> points = new CopyOnWriteArrayList<>();

    /** The position. */
    protected long position;

    final boolean rowMajority;

    final int CXR_MAX_ENTRIES = 6;

    CPR cpr;

    DataContainer timeContainer;

    CopyOnWriteArrayList<Integer> _firstRecords;

    CopyOnWriteArrayList<Integer> _lastRecords;

    CopyOnWriteArrayList<ByteBuffer> _bufs;

    Boolean phantom = null;

    Boolean _doNotCompress = null;

    boolean doNotCompress = false;

    long[] locs;

    VXR[] vxrs;

    /**
     * Instantiates a new data container.
     *
     * @param vdr the vdr
     */
    public DataContainer(final VDR vdr) {
        this(vdr, true);
    }

    /**
     * Instantiates a new data container.
     *
     * @param vdr         the vdr
     * @param rowMajority the row majority
     */
    public DataContainer(final VDR vdr, final boolean rowMajority) {
        this.vdr = vdr;
        this.vxr = new VXR();
        this.rowMajority = rowMajority;
    }

    /**
     * Adds the data.
     *
     * @param data        the data
     * @param recordRange the record range
     * @param oned        the oned
     */
    public void addData(final Object data, final int[] recordRange, final boolean oned) {
        addData(data, recordRange, oned, false);
    }

    /**
     * Adds the java array.
     *
     * @param data     the data
     * @param dataType the data type
     * @param relax    the relax
     *
     * @return the byte buffer
     */
    public ByteBuffer addJavaArray(final Object data, final int dataType, final boolean relax) {

        ArrayAttribute aa = new ArrayAttribute(data);

        Class<?> cl = aa.getType();

        CDFDataType ctype = SupportedTypes.cdfType(dataType);

        if (ctype == null) {
            throw new IllegalArgumentException("datatype, " + dataType + " , is not supported.");
        }

        if (cl == Long.TYPE) {
            LongArray la = new LongArray(data, this.rowMajority);
            boolean ok = (ctype == CDFDataType.INT8) || (ctype == CDFDataType.TT2000);

            if (ok) {
                return la.buffer();
            }

            if (ctype == CDFDataType.UINT4) {
                return la.buffer(Integer.TYPE);
            }

        }

        if (cl == Double.TYPE) {
            DoubleArray da = new DoubleArray(data, this.rowMajority);
            boolean ok = (ctype == CDFDataType.DOUBLE) || (ctype == CDFDataType.EPOCH)
                || (ctype == CDFDataType.EPOCH16);

            if (ok) {
                return da.buffer();
            }

            if (ctype == CDFDataType.FLOAT) {
                return da.buffer(Float.TYPE);
            }

        }

        if (cl == Float.TYPE) {
            FloatArray fa = new FloatArray(data, this.rowMajority);

            if (ctype == CDFDataType.FLOAT) {
                return fa.buffer();
            }

        }

        if (cl == Integer.TYPE) {
            IntArray ia = new IntArray(data, this.rowMajority);

            if (ctype == CDFDataType.INT4) {
                return ia.buffer();
            }

            if (ctype == CDFDataType.UINT2) {
                return ia.buffer(Short.TYPE);
            }

            if (relax && (ctype == CDFDataType.UINT4)) {
                return ia.buffer();
            }

        }

        if (cl == Short.TYPE) {
            ShortArray sa = new ShortArray(data, this.rowMajority);

            if (ctype == CDFDataType.INT2) {
                return sa.buffer();
            }

            if (ctype == CDFDataType.UINT1) {
                return sa.buffer(Byte.TYPE);
            }

            if (relax && (ctype == CDFDataType.UINT2)) {
                return sa.buffer();
            }

        }

        if (cl == Byte.TYPE) {
            ByteArray ba = new ByteArray(data, this.rowMajority);

            if (ctype == CDFDataType.INT1) {
                return ba.buffer();
            }

            if (relax && (ctype == CDFDataType.UINT1)) {
                return ba.buffer();
            }

        }

        if (cl == String.class) {
            StringArray st = new StringArray(data, this.rowMajority);

            if (ctype == CDFDataType.CHAR) {
                return st.buffer(this.vdr.numElems);
            }

        }

        return null;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        // update vdr
        int size = this.vdr.getSize();

        if (this.vdr.isCompressed()) {
            this.cpr = new CPR();
            this.cpr.position = this.position + size;
            this.vdr.setCPROffset(this.cpr.position);
            size += this.cpr.getSize();
        }

        if (!this.bufs.isEmpty()) {
            int last = -1;
            int nbuf = this.bufs.size() - 1;

            while (nbuf >= 0) {

                if (this.bufs.get(nbuf) != null) {
                    last = this.lastRecords.get(nbuf);
                    break;
                }

                nbuf--;
            }

            if (last < 0) {
                return size;
            }

            this.vdr.setMaxRec(last);
        } else {
            return size;
        }

        this.vdr.setVXRHead(this.position + size);

        this._firstRecords = new CopyOnWriteArrayList<>();
        this._lastRecords = new CopyOnWriteArrayList<>();
        this._bufs = new CopyOnWriteArrayList<>();

        if (this.timeContainer == null) {
            int nbuf = 0;

            while (nbuf < this.bufs.size()) {

                if (this.bufs.get(nbuf) != null) {
                    this._firstRecords.add(this.firstRecords.get(nbuf));
                    this._lastRecords.add(this.lastRecords.get(nbuf));
                    this._bufs.add(this.bufs.get(nbuf));
                }

                nbuf++;
            }

        } else {
            int nbuf = 0;

            while (nbuf < this.bufs.size()) {

                if (this.bufs.get(nbuf) != null) {
                    int _first = this.firstRecords.get(nbuf);

                    if (_first < this.timeContainer.firstRecords.get(nbuf)) {
                        _first = this.timeContainer.firstRecords.get(nbuf);
                    }

                    this._firstRecords.add(_first);
                    this._lastRecords.add((_first + this.lastRecords.get(nbuf)) - this.firstRecords.get(nbuf));
                    this._bufs.add(this.bufs.get(nbuf));
                }

                nbuf++;
            }

        }

        int vxrsNeeded = this._bufs.size() / this.CXR_MAX_ENTRIES;
        int lastVXREntries = this._bufs.size() - (vxrsNeeded * this.CXR_MAX_ENTRIES);

        if (lastVXREntries > 0) {
            vxrsNeeded++;
        } else {
            lastVXREntries = this.CXR_MAX_ENTRIES;
        }

        this.vxrs = new VXR[vxrsNeeded];
        this.locs = new long[this._bufs.size()];
        int nbuf = 0;
        long _position = -1L;

        for (int v = 0; v < this.vxrs.length; v++) {
            _position = this.position + size;
            this.vxrs[v] = new VXR();
            int entries = this.CXR_MAX_ENTRIES;

            if (v == (this.vxrs.length - 1)) {
                entries = lastVXREntries;
            }

            this.vxrs[v].numEntries = entries;
            size += this.vxrs[v].getSize();

            if (!this.vdr.isCompressed()) {

                for (int e = 0; e < entries; e++) {
                    this.locs[nbuf] = this.position + size;
                    int len = VVR_PREAMBLE + this._bufs.get(nbuf)
                            .limit();
                    size += len;
                    nbuf++;
                }

            } else {
                this.vdr.setBlockingFactor(getBlockingFactor());

                if (this.doNotCompress) {

                    for (int e = 0; e < entries; e++) {
                        this.locs[nbuf] = this.position + size;
                        int len = CVVR_PREAMBLE + this._bufs.get(nbuf)
                                .limit();
                        size += len;
                        nbuf++;
                    }

                    return size;
                }

                for (int e = 0; e < entries; e++) {
                    this.locs[nbuf] = this.position + size;
                    ByteBuffer b = this._bufs.get(nbuf);
                    byte[] uncompressed = null;

                    if (b.hasArray()) {
                        uncompressed = b.array();
                    } else {
                        uncompressed = new byte[b.remaining()];
                        b.get(uncompressed);
                        this._bufs.set(nbuf, null);
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length);

                    try {
                        GZIPOutputStream gzos = new GZIPOutputStream(baos);
                        gzos.write(uncompressed, 0, uncompressed.length);
                        gzos.finish();
                        baos.flush();
                        b = ByteBuffer.wrap(baos.toByteArray());
                        this._bufs.set(nbuf, b);
                        int len = CVVR_PREAMBLE + b.limit();
                        size += len;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    nbuf++;
                }

            }

            if (v != (this.vxrs.length - 1)) {
                this.vxrs[v].setVXRNext(this.position + size);
            }

        }

        if (this.vxrs.length > 1) {
            this.vdr.setVXRTail(_position);
        }

        return size;
    }

    /**
     * Gets the vdr.
     *
     * @return the vdr
     */
    public VDR getVDR() {
        return this.vdr;
    }

    /**
     * Gets the vxr.
     *
     * @return the vxr
     */
    public VXR getVXR() {
        return this.vxr;
    }

    /**
     * Update.
     *
     * @param buf the buf
     *
     * @return the byte buffer
     */
    public ByteBuffer update(final ByteBuffer buf) {
        buf.position((int) this.position);
        buf.put(this.vdr.get());

        if (this.vdr.isCompressed()) {
            buf.put(this.cpr.get());
        }

        if (this._bufs == null) {
            return buf;
        }

        if (!this._bufs.isEmpty()) {

            int nbuf = 0;
            for (VXR vxr1 : this.vxrs) {
                buf.put(vxr1.get());

                for (int e = 0; e < vxr1.numEntries; e++) {
                    int n = this._firstRecords.get(nbuf + e);
                    buf.putInt(n);
                }

                for (int e = 0; e < vxr1.numEntries; e++) {
                    int n = this._lastRecords.get(nbuf + e);
                    buf.putInt(n);
                }

                for (int e = 0; e < vxr1.numEntries; e++) {
                    buf.putLong(this.locs[nbuf + e]);
                }

                if (!this.vdr.isCompressed()) {

                    for (int e = 0; e < vxr1.numEntries; e++) {
                        buf.putLong(VVR_PREAMBLE + this._bufs.get(nbuf + e)
                                .limit());
                        buf.putInt(7);
                        buf.put(this._bufs.get(nbuf + e));
                    }

                } else {

                    for (int e = 0; e < vxr1.numEntries; e++) {
                        ByteBuffer b = this._bufs.get(nbuf + e);
                        buf.putLong(CVVR_PREAMBLE + b.limit());
                        buf.putInt(13);
                        buf.putInt(0);
                        buf.putLong(b.limit());
                        buf.put(b);
                    }

                }

                nbuf += vxr1.numEntries;
            }

        }

        return buf;
    }

    /**
     * Update.
     *
     * @param channel the channel
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void update(final FileChannel channel) throws IOException {
        channel.position(this.position);
        channel.write(this.vdr.get());

        if (this.vdr.isCompressed()) {
            channel.write(this.cpr.get());
        }

        if (this._bufs == null) {
            return;
        }

        ByteBuffer longbuf = ByteBuffer.allocate(8);
        ByteBuffer intbuf = ByteBuffer.allocate(4);

        if (!this._bufs.isEmpty()) {

            int nbuf = 0;
            for (VXR vxr1 : this.vxrs) {
                channel.write(vxr1.get());

                for (int e = 0; e < vxr1.numEntries; e++) {
                    int n = this._firstRecords.get(nbuf + e);
                    writeInt(channel, intbuf, n);
                }

                for (int e = 0; e < vxr1.numEntries; e++) {
                    int n = this._lastRecords.get(nbuf + e);
                    writeInt(channel, intbuf, n);
                }

                for (int e = 0; e < vxr1.numEntries; e++) {
                    writeLong(channel, longbuf, this.locs[nbuf + e]);
                }

                if (!this.vdr.isCompressed()) {

                    for (int e = 0; e < vxr1.numEntries; e++) {
                        writeLong(channel, longbuf, VVR_PREAMBLE + this._bufs.get(nbuf + e)
                                .limit());
                        writeInt(channel, intbuf, 7);
                        channel.write(this._bufs.get(nbuf + e));
                    }

                } else {

                    for (int e = 0; e < vxr1.numEntries; e++) {
                        ByteBuffer b = this._bufs.get(nbuf + e);
                        writeLong(channel, longbuf, CVVR_PREAMBLE + b.limit());
                        writeInt(channel, intbuf, 13);
                        writeInt(channel, intbuf, 0);
                        writeLong(channel, longbuf, b.limit());
                        channel.write(b);
                    }

                }

                nbuf += vxr1.numEntries;
            }

        }

    }

    void addData(final Object data, final int[] recordRange, final boolean oned, final boolean relax) {

        ByteBuffer buf = null;

        if (ByteBuffer.class.isAssignableFrom(data.getClass())) {
            buf = (ByteBuffer) data;

            if (DataTypes.size[this.vdr.dataType] > 1) {

                if (buf.order() != ByteOrder.LITTLE_ENDIAN) {
                    throw new IllegalArgumentException(
                            "For data types of size > 1, supplied buffer must be in LITTLE_ENDIAN order");
                }

            }

            if (this.vdr.isCompressed()) {

                if (recordRange == null) {
                    throw new IllegalArgumentException("Record range must be specified since " + this.vdr.getName()
                            + "is to be stored as compressed.");
                }

                if (this._doNotCompress == null) {
                    this.doNotCompress = (recordRange.length == 2);
                    this._doNotCompress = this.doNotCompress;
                } else {

                    if (this.doNotCompress ? (recordRange.length > 2) : (recordRange.length == 2)) {
                        String t = "compressed";

                        if (!this.doNotCompress) {
                            t = "uncompressed";
                        }

                        throw new IllegalArgumentException("Changing compression mode of input. Previous = " + t + ".");
                    }

                }

            }

        } else {

            if (!(data.getClass()
                    .isArray())) {
                throw new IllegalArgumentException("supplied object not an array or a ByteBuffer");
            }

        }

        int first = (recordRange == null) ? 0 : recordRange[0];

        if (!this.lastRecords.isEmpty()) {
            int _last = -1;

            if (this.timeContainer != null) {
                _last = this.timeContainer.getLastRecord(this.lastRecords.size() - 1);
            } else {
                _last = getLastRecord();
            }

            if (recordRange == null) {
                first = _last + 1;
                int expected = getLastRecord() + 1;

                if ((first - expected) > 0) {

                    if (this.vdr.sRecords == 0) {
                        System.out.println("Gap: " + expected + " - " + first + " for " + this.vdr.getName());
                        throw new IllegalArgumentException(" SparseRecordOption must be set. There are "
                                + " missing records between files for " + this.vdr.getName());
                    }

                }

            } else {

                if (recordRange[0] <= _last) {
                    throw new IllegalArgumentException(
                            "first record " + recordRange[0] + " must follow the last seen record " + _last);
                }

                if (recordRange[0] > (_last + 1)) {

                    if (this.vdr.sRecords == 0) {
                        throw new IllegalArgumentException("Specified start of the range " + recordRange[0]
                                + " does not follow last record " + _last + " immediately."
                                + " SparseRecordOption must be set if the CDF is missing records");
                    }

                }

            }

        } else { // first cannot be nonzero unless sparseness option was chosen

            if (first != 0) {

                if (this.vdr.sRecords == 0) {
                    throw new IllegalArgumentException("SparseRecordOption must be set if the CDF is missing records");
                }

            }

        }

        boolean done = false;
        int npt = 0;
        int last = -1;

        if (!done && (buf != null)) {

            if (recordRange == null) {
                npt = buf.remaining() / DataTypes.size[this.vdr.dataType];
                npt /= this.vdr.itemsPerPoint;
                last = (first + npt) - 1;
            } else {
                last = recordRange[1];
                npt = (last - first) + 1;
            }

            this.firstRecords.add(first);
            this.lastRecords.add(last);
            this.bufs.add(buf);
            this.points.add(npt);
            return;
        }

        ArrayAttribute aa = new ArrayAttribute(data);

        // if (aa.getDimensions().length > 1) {
        if (!oned) {
            /*
             * if (!rowMajority) throw new Throwable("column majority " +
             * "feature not supported in this context in this version.");
             */
            npt = java.lang.reflect.Array.getLength(data);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            List<Integer> vdim = null;

            if (this.vdr.dataType == 32) {
                vdim = new ArrayList<>();
                vdim.add(2);
            } else {
                vdim = this.vdr.efdim;
            }

            if (!vdim.isEmpty()) {
                int[] dcheck = new int[1 + vdim.size()];
                dcheck[0] = npt;

                for (int i = 0; i < vdim.size(); i++) {
                    dcheck[i + 1] = (vdim.get(i));
                }

                // if (!(new AArray(data)).validateDimensions(dcheck)) {
                if (!Arrays.equals(aa.getDimensions(), dcheck)) {
                    StringBuilder sbe = new StringBuilder();

                    for (int k : dcheck) {
                        sbe.append(",")
                                .append(k);
                    }

                    StringBuilder sbf = new StringBuilder();
                    int[] fdim = aa.getDimensions();

                    for (int j : fdim) {
                        sbf.append(",")
                                .append(j);
                    }

                    throw new IllegalStateException("Dimension mismatch, expected: " + sbe + " found " + sbf + ".");
                }

            }

            last = (first + npt) - 1;
            buf = addJavaArray(data, this.vdr.dataType, relax);

            if (buf != null) {
                done = true;
            }

        }

        if (!done && ((this.vdr.dataType == 1) || (relax && (this.vdr.dataType == 11))
            || ((this.vdr.dataType > 50) && (aa.getType() == Byte.TYPE)))) {
            byte[] values = (byte[]) data;
            npt = (values.length / this.vdr.itemsPerPoint);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            buf = ByteBuffer.wrap(values);
            last = (first + npt) - 1;
            done = true;
        }

        if (!done && ((this.vdr.dataType == 2) || (relax && (this.vdr.dataType == 12)))) {
            short[] values = (short[]) data;
            npt = (values.length / this.vdr.itemsPerPoint);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            last = (first + npt) - 1;
            buf = ByteBuffer.allocateDirect(2 * values.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                buf.asShortBuffer()
                        .put(values);
            } else {

                for (short value : values) {
                    buf.putShort(value);
                }

                buf.position(0);
            }

            done = true;
        }

        if (!done && ((this.vdr.dataType == 4) || (relax && (this.vdr.dataType == 14)))) {
            int[] values = (int[]) data;
            npt = (values.length / this.vdr.itemsPerPoint);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            last = (first + npt) - 1;
            buf = ByteBuffer.allocateDirect(4 * values.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                buf.asIntBuffer()
                        .put(values);
            } else {

                for (int value : values) {
                    buf.putInt(value);
                }

                buf.position(0);
            }

            done = true;
        }

        if (!done && ((this.vdr.dataType == 21) || (this.vdr.dataType == 44))) {
            float[] values = (float[]) data;
            npt = (values.length / this.vdr.itemsPerPoint);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            last = (first + npt) - 1;
            buf = ByteBuffer.allocateDirect(4 * values.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                buf.asFloatBuffer()
                        .put(values);
            } else {

                for (float value : values) {
                    buf.putFloat(value);
                }

                buf.position(0);
            }

            done = true;
        }

        if (!done && ((this.vdr.dataType == 22) || (this.vdr.dataType == 45)
            || (this.vdr.dataType == 31)
            || (this.vdr.dataType == 32))) {
            double[] values = (double[]) data;
            npt = (values.length / this.vdr.itemsPerPoint);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            last = (first + npt) - 1;
            buf = ByteBuffer.allocateDirect(8 * values.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                buf.asDoubleBuffer()
                        .put(values);
            } else {

                for (double value : values) {
                    buf.putDouble(value);
                }

                buf.position(0);
            }

            done = true;
        }

        if (!done && ((this.vdr.dataType == 33) || (this.vdr.dataType == 8))) {
            long[] values = (long[]) data;
            npt = (values.length / this.vdr.itemsPerPoint);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            last = (first + npt) - 1;
            buf = ByteBuffer.allocateDirect(8 * values.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                buf.asLongBuffer()
                        .put(values);
            } else {

                for (long value : values) {
                    buf.putLong(value);
                }

                buf.position(0);
            }

            done = true;
        }

        if (!done && (this.vdr.dataType > 50)) { // data is String[]
            String[] values = (String[]) data;
            npt = ((values.length * this.vdr.numElems) / this.vdr.itemsPerPoint);
            // System.out.println(values.length);
            // System.out.println(vdr.itemsPerPoint);
            // System.out.println(npt);

            if (recordRange != null) {

                if (npt != ((recordRange[1] - recordRange[0]) + 1)) {
                    throw new IllegalStateException("array size not consistent with given record range");
                }

            }

            last = (first + npt) - 1;
            buf = ByteBuffer.allocateDirect(this.vdr.numElems * values.length);

            for (String value : values) {
                int len = value.length();

                if (len > this.vdr.numElems) {
                    throw new IllegalStateException("String " + value + " is longer than the length of variable.");
                }

                byte[] _bar = value.getBytes();
                buf.put(_bar);

                for (int f = 0; f < (this.vdr.numElems - _bar.length); f++) {
                    buf.put((byte) 0x20);
                }

            }

            buf.position(0);
            done = true;
        }

        if (!done) {

            if (relax) {
                throw new IllegalStateException("Unsupported data type.");
            }

            if ((this.vdr.dataType > 10) && (this.vdr.dataType < 20)) {
                throw new IllegalStateException(
                        "Possible incompatibility for unsigned. Use relax = true to force acceptance");
            }

        }

        if (this.phantom == Boolean.TRUE) {
            this.firstRecords.clear();
            this.lastRecords.clear();
            this.bufs.clear();
            this.phantom = Boolean.FALSE;
        }

        this.firstRecords.add(first);
        this.lastRecords.add(last);
        this.bufs.add(buf);
        this.points.add(npt);
    }

    void addPhantomEntry() {

        if (this.phantom != null) {
            return;
        }

        this.firstRecords.add(-1);
        this.lastRecords.add(-1);
        this.bufs.add(null);
        this.phantom = Boolean.TRUE;
    }

    int getBlockingFactor() {
        int n = -1;

        for (int p : this.points) {

            if (p > n) {
                n = p;
            }

        }

        return n;
    }

    int getLastRecord() {
        return getLastRecord(this.lastRecords.size() - 1);
    }

    int getLastRecord(final int start) {
        int n = start;

        if (n < 0) {
            return -1;
        }

        while (n >= 0) {
            int l = this.lastRecords.get(n);

            if (l >= 0) {
                return l;
            }

            n--;
        }

        return -1;
    }

    void setTimeContainer(final DataContainer dc) {
        this.timeContainer = dc;
    }

    boolean timeOrderOK(final Object nextTime) {
        int last = this.bufs.size() - 1;

        if (last < 0) {
            return true;
        }

        ByteBuffer buf = null;

        while ((buf = this.bufs.get(last)) == null) {

            if (last == 0) {
                break;
            }

            last--;
        }

        if (buf == null) {
            return true;
        }

        if (CDFTimeType.TT2000.getValue() == this.vdr.dataType) {
            return (((long[]) nextTime)[0] > buf.getLong(buf.limit()));
        }

        if (CDFTimeType.EPOCH16.getValue() == this.vdr.dataType) {
            double[] e16 = new double[2];
            e16[0] = buf.getDouble(buf.limit() - 16);
            e16[1] = buf.getDouble(buf.limit() - 8);
            double[] next = (double[]) nextTime;

            if (next[0] > e16[0]) {
                return true;
            }

            return (next[0] >= e16[0]) && (next[1] > e16[1]);
        }

        double[] next = (double[]) nextTime;
        return (next[0] > buf.getDouble(buf.limit() - 8));
    }

    void writeInt(final WritableByteChannel ch, final ByteBuffer buf, final int value) throws IOException {
        buf.position(0);
        buf.putInt(value);
        buf.position(0);
        ch.write(buf);
    }

    void writeLong(final WritableByteChannel ch, final ByteBuffer buf, final long value) throws IOException {
        buf.position(0);
        buf.putLong(value);
        buf.position(0);
        ch.write(buf);
    }
}
