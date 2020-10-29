package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

/**
 *
 * @author nand
 */
public abstract class BaseVarContainer implements Runnable {

    static final int CHUNK_SIZE = 1_024;

    final CDFImpl thisCDF;

    final Variable var;

    final int[] pt;

    final int[] overlap;

    final int type;

    final int itemSize;

    final int elements;

    final ByteOrder order;

    final Class _class;

    final int recordsPerChunk;

    final int csize;

    final boolean chunking;

    final Vector buffers = new Vector();

    final int fillCount;

    final boolean singlePoint;

    Boolean allocationMode;

    ByteBuffer userBuffer;

    /**
     *
     * @param cdfi
     * @param vrbl
     * @param ints
     * @param bln
     * @param bo
     * @param type
     *
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws Throwable
     */
    protected BaseVarContainer(CDFImpl thisCDF, Variable var, int[] pt, boolean preserve, ByteOrder bo, Class cl)
            throws IllegalAccessException, InvocationTargetException, Throwable {
        this.type = var.getType();

        if (!isCompatible(this.type, preserve, cl)) {
            throw new Throwable("Variable " + var.getName() + " may result in loss of precision");
        }

        this.thisCDF = thisCDF;
        this.var = var;
        this.order = bo;
        this._class = cl;
        this.itemSize = var.getDataItemSize();
        this.elements = this.itemSize / DataTypes.size[this.type];
        int[] range = var.getRecordRange();

        if (range == null) {
            // if (pt == null) {
            throw new Throwable("Variable " + var.getName() + " has no " + "records.");
            // }
        }

        if (pt == null) {
            this.singlePoint = false;
            this.pt = range;
        } else {
            this.singlePoint = (pt.length == 1);
            this.pt = (pt.length == 1) ? new int[] { pt[0], pt[0] } : new int[] { pt[0], pt[1] };
        }

        int _fillCount = 0;
        int[] _overlap = null;

        if (pt != null) {

            if (var.recordVariance()) {

                if (pt[0] < 0) {
                    throw new Throwable("Negative start of Record Range ");
                }

                if (pt.length > 1) {

                    if (pt[0] > pt[1]) {
                        throw new Throwable("Invalid record Range " + "first " + pt[0] + ", last " + pt[1]);
                    }

                }

                if ((!var.missingRecordValueIsPad() && !var.missingRecordValueIsPrevious())) {

                    if ((range[0] > pt[0]) || (range[1] < pt[0])) {
                        throw new Throwable("Invalid start of Record " + "Range " + pt[0]
                                + ". Available record range is " + range[0] + " - " + range[1]);
                    }

                    if (pt.length > 1) {

                        if (range[1] < pt[1]) {
                            throw new Throwable(
                                    "Invalid end of Record Range " + pt[1] + ". Last available record is " + range[1]);
                        }

                        _overlap = new int[] { pt[0], pt[1] };
                    } else {
                        _overlap = new int[] { pt[0], pt[0] };
                    }

                } else {

                    if (pt.length == 1) {

                        if ((pt[0] < range[0]) || (pt[0] > range[1])) {
                            _fillCount = 1;
                        } else {
                            _overlap = new int[] { pt[0], pt[0] };
                        }

                    } else {

                        if ((pt[0] > range[1]) || (pt[1] < range[0])) {
                            _fillCount = (pt[1] - pt[0]) + 1;
                        } else { // partial overlap

                            if (pt[0] < range[0]) {
                                _fillCount = range[0] - pt[0];
                                _overlap = new int[] { range[0], pt[1] };
                            } else {
                                _overlap = new int[] { pt[0], pt[1] };
                            }

                        }

                    }

                }

            } else {
                _overlap = new int[] { 0, 0 };
            }

        } else {
            _overlap = new int[] { range[0], range[1] };
        }

        this.fillCount = _fillCount;
        this.overlap = _overlap;

        if ((DataTypes.size[this.type] > 1) || (this._class != Byte.TYPE)) {
            int _recordsPerChunk = (CHUNK_SIZE / this.elements);
            this.recordsPerChunk = (_recordsPerChunk == 0) ? 1 : _recordsPerChunk;
            this.csize = this.recordsPerChunk * this.elements;
            this.chunking = true;
        } else {
            this.recordsPerChunk = -1;
            this.csize = -1;
            this.chunking = false;
        }

    }

    /**
     *
     * @param type
     * @param preserve
     * @param cl
     *
     * @return
     */

    public static boolean isCompatible(int type, boolean preserve, Class cl) {

        if (cl == Long.TYPE) {
            return (DataTypes.typeCategory[type] == DataTypes.SIGNED_INTEGER)
                    || (DataTypes.typeCategory[type] == DataTypes.UNSIGNED_INTEGER)
                    || (DataTypes.typeCategory[type] == DataTypes.LONG);
        }

        if (cl == Double.TYPE) {

            if (type > 50) {
                return false;
            }

            return (DataTypes.typeCategory[type] != DataTypes.LONG) || !preserve;
        }

        if (cl == Float.TYPE) {

            if (type > 50) {
                return false;
            }

            if (DataTypes.typeCategory[type] == DataTypes.FLOAT) {
                return true;
            }

            if ((DataTypes.typeCategory[type] == DataTypes.LONG)
                    || (DataTypes.typeCategory[type] == DataTypes.DOUBLE)) {
                return !preserve;
            }

            return !preserve || ((type != 4) && (type != 14));
        }

        if (cl == Integer.TYPE) {

            if (type > 50) {
                return false;
            }

            return ((DataTypes.typeCategory[type] == DataTypes.SIGNED_INTEGER)
                    || (DataTypes.typeCategory[type] == DataTypes.UNSIGNED_INTEGER)) && (!preserve || (type != 14));
        }

        if (cl == Short.TYPE) {

            if (type > 50) {
                return false;
            }

            if ((type == 1) || (type == 41) || (type == 2)) {
                return true;
            }

            return (type == 11) || ((type == 12) && !preserve);
        }

        if (cl == Byte.TYPE) {

            if (preserve) {
                return (type == 1) || (type == 41) || (type == 11) || (type > 50);
            }

            return true;
        }

        return false;
    }

    static boolean validElement(Variable var, int[] idx) {
        int elements = (((Integer) var.getElementCount().elementAt(0)));

        for (int j : idx) {

            if ((j >= 0) && (j < elements)) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */

    public Object as1DArray() {
        ByteBuffer b = getBuffer();

        if (b == null) {
            return null;
        }

        if (this._class == Long.TYPE) {
            long[] la = new long[(b.remaining()) / 8];
            b.asLongBuffer().get(la);
            return la;
        }

        if (this._class == Double.TYPE) {
            double[] da = new double[(b.remaining()) / 8];
            b.asDoubleBuffer().get(da);
            return da;
        }

        if (this._class == Float.TYPE) {
            float[] fa = new float[(b.remaining()) / 4];
            b.asFloatBuffer().get(fa);
            return fa;
        }

        if (this._class == Integer.TYPE) {
            int[] ia = new int[(b.remaining()) / 4];
            b.asIntBuffer().get(ia);
            return ia;
        }

        if (this._class == Short.TYPE) {
            short[] sa = new short[(b.remaining()) / 2];
            b.asShortBuffer().get(sa);
            return sa;
        }

        byte[] ba = new byte[(b.remaining())];
        b.get(ba);
        return ba;
    }

    /**
     *
     * @param cmtarget
     *
     * @return
     */
    public Object asOneDArray(boolean cmtarget) {
        return asOneDArray(cmtarget, null);
    }

    /**
     *
     * @param cmtarget
     * @param stride
     *
     * @return
     */
    public Object asOneDArray(boolean cmtarget, Stride stride) {
        int[] dim = this.var.getEffectiveDimensions();

        if ((dim.length <= 1) || (!cmtarget && this.var.rowMajority()) || (cmtarget && !this.var.rowMajority())) {

            if (stride == null) {
                return as1DArray();
            }

            return asSampledArray(stride);
        }

        int[] _dim = dim;

        if (!this.var.rowMajority()) {
            _dim = new int[dim.length];

            for (int i = 0; i < dim.length; i++) {
                _dim[i] = dim[dim.length - 1 - i];
            }

        }

        return makeArray(_dim, stride);
    }

    /**
     *
     * @param stride
     *
     * @return
     */
    public Object asSampledArray(Stride stride) {
        int[] range = getRecordRange();
        int numberOfValues = (range[1] - range[0]) + 1;
        int _stride = stride.getStride(numberOfValues);

        if (_stride > 1) {
            int n = (numberOfValues / _stride);

            if ((numberOfValues % _stride) != 0) {
                n++;
            }

            numberOfValues = n;
        }

        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = this.elements * numberOfValues;
        int advance = _stride * this.elements;
        int pos = 0;
        int off = 0;

        if (this._class == Float.TYPE) {
            FloatBuffer _buf = buf.asFloatBuffer();
            float[] sampled = new float[words];

            for (int i = 0; i < numberOfValues; i++) {
                _buf.position(pos);
                _buf.get(sampled, off, this.elements);
                off += this.elements;
                pos += advance;
            }

            return sampled;
        }

        if (this._class == Double.TYPE) {
            DoubleBuffer _buf = buf.asDoubleBuffer();
            double[] sampled = new double[words];

            for (int i = 0; i < numberOfValues; i++) {
                _buf.position(pos);
                _buf.get(sampled, off, this.elements);
                off += this.elements;
                pos += advance;
            }

            return sampled;
        }

        if (this._class == Integer.TYPE) {
            IntBuffer _buf = buf.asIntBuffer();
            int[] sampled = new int[words];

            for (int i = 0; i < numberOfValues; i++) {
                _buf.position(pos);
                _buf.get(sampled, off, this.elements);
                off += this.elements;
                pos += advance;
            }

            return sampled;
        }

        if (this._class == Short.TYPE) {
            ShortBuffer _buf = buf.asShortBuffer();
            short[] sampled = new short[words];

            for (int i = 0; i < numberOfValues; i++) {
                _buf.position(pos);
                _buf.get(sampled, off, this.elements);
                off += this.elements;
                pos += advance;
            }

            return sampled;
        }

        if (this._class == Byte.TYPE) {
            ByteBuffer _buf = buf.duplicate();
            byte[] sampled = new byte[words];

            for (int i = 0; i < numberOfValues; i++) {
                _buf.position(pos);
                _buf.get(sampled, off, this.elements);
                off += this.elements;
                pos += advance;
            }

            return sampled;
        }

        if (this._class == Long.TYPE) {
            LongBuffer _buf = buf.asLongBuffer();
            long[] sampled = new long[words];

            for (int i = 0; i < numberOfValues; i++) {
                _buf.position(pos);
                _buf.get(sampled, off, this.elements);
                off += this.elements;
                pos += advance;
            }

            return sampled;
        }

        return null;
    }

    /**
     *
     * @return
     */
    public ByteBuffer getBuffer() {

        if (this.buffers.size() == 0) {
            return null;
        }

        ContentDescriptor cd = (ContentDescriptor) this.buffers.get(0);
        return cd.getBuffer();
    }

    /**
     *
     * @return
     */
    public int getCapacity() {
        int numberOfValues = (this.pt[1] - this.pt[0]) + 1;
        int words = this.elements * numberOfValues;
        return words * getLength();
    }

    /**
     *
     * @return
     */
    public int[] getRecordRange() {

        if (this.buffers.size() == 0) {
            return null;
        }

        ContentDescriptor cd = (ContentDescriptor) this.buffers.get(0);
        return new int[] { cd.getFirstRecord(), cd.getLastRecord() };
    }

    /**
     *
     * @return
     */
    public Variable getVariable() {
        return this.var;
    }

    @Override
    public void run() {

        if (this.buffers.size() > 0) {
            return;
        }

        int numberOfValues = (this.pt[1] - this.pt[0]) + 1;
        int words = this.elements * numberOfValues;
        ByteBuffer _buf;
        int _words = words * getLength();

        if (this.allocationMode == null) {

            if (this.userBuffer == null) {
                _buf = ByteBuffer.allocateDirect(_words);
            } else {
                _buf = this.userBuffer;
            }

        } else {

            if (this.allocationMode) {
                _buf = ByteBuffer.allocateDirect(_words);
            } else {
                _buf = ByteBuffer.allocate(_words);
            }

        }

        _buf.order(this.order);
        Object data = null;

        if (this.overlap == null) {
            data = allocateDataArray(words);
            doMissing(this.fillCount, _buf, data, -1);

            if (this.buffers.size() == 0) {
                this.buffers.add(new ContentDescriptor(_buf, this.pt[0], this.pt[1]));
            }

            return;
        }

        int begin = this.overlap[0];
        int end = this.overlap[1];

        if (this.chunking) {
            data = allocateDataArray(Math.min(words, this.csize));
        }

        if (this.fillCount > 0) {
            doMissing(this.fillCount, _buf, data, -1);
        }

        Vector locations = ((CDFImpl.DataLocator) this.var.getLocator()).locations;
        ByteBuffer bv;
        int blk = 0;
        int next = begin;

        if (next > 0) {// position to first needed block
            int _first = -1;
            int prev = -1;

            for (; blk < locations.size(); blk++) {
                long[] loc = (long[]) locations.elementAt(blk);
                _first = (int) loc[0];

                if (loc[1] >= next) {
                    break;
                }

                prev = (int) loc[1];
            }

            int tofill = 0;

            if (blk == locations.size()) { // past prev available
                tofill = (end - begin) + 1;

                if ((!this.var.missingRecordValueIsPad() && !this.var.missingRecordValueIsPrevious())) {
                    return;
                }

            } else {

                if (next < _first) { // some missing records
                    tofill = _first - next;

                    if (end < _first) {
                        tofill = (end + 1) - next;
                    }

                }

            }

            if (tofill > 0) {

                if (this.var.missingRecordValueIsPrevious()) {
                    doMissing(tofill, _buf, data, (blk == 0) ? -1 : prev);
                } else {
                    doMissing(tofill, _buf, data, -1);
                }

                next += tofill;

                if (next > end) {

                    if (this.buffers.size() == 0) {
                        this.buffers.add(new ContentDescriptor(_buf, begin, end));
                    }

                    return;
                }

            }

        }

        // there is valid data to send back
        // begin may lie before blk. This is handled later
        boolean firstBlock = true;

        for (; blk < locations.size(); blk++) {
            long[] loc = (long[]) locations.elementAt(blk);
            int first = (int) loc[0];
            int last = (int) loc[1];

            int count = ((last - first) + 1);
            bv = this.thisCDF.positionBuffer(this.var, loc[2], count);

            if (firstBlock) {

                if (this.pt != null) {

                    if (begin > first) {
                        int pos = bv.position() + ((begin - first) * this.itemSize);
                        bv.position(pos);
                    }

                    if (end == begin) { // single point needed

                        try {
                            doData(bv, this.type, this.elements, 1, _buf, data);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }

                        if (this.buffers.size() == 0) {
                            this.buffers.add(new ContentDescriptor(_buf, begin, end));
                        }

                        return;
                    }

                }

                firstBlock = false;
            } else {

                // pad if necessary
                if (next < first) { // next cannot exceed first
                    int target = (end >= first) ? first : end + 1;
                    int n = target - next;

                    if (this.var.missingRecordValueIsPrevious()) {
                        int rec = (int) ((long[]) locations.elementAt(blk - 1))[1];
                        doMissing(n, _buf, data, rec);
                    } else {
                        doMissing(n, _buf, data, -1);
                    }

                    if (target > end) {
                        break;
                    }

                    next = first;
                }

            }

            while (next <= end) {
                int rem = (end - next) + 1;
                int _count = (last - next) + 1;

                if (this.chunking) {

                    if (_count > this.recordsPerChunk) {
                        _count = this.recordsPerChunk;
                    }

                }

                if (_count > rem) {
                    _count = rem;
                }

                try {
                    doData(bv, this.type, this.elements, _count, _buf, data);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    return;
                }

                // System.out.println(bv);
                // System.out.println(_buf);
                next += _count;

                if (next > last) {
                    break;
                }

            }

            if (next > end) {
                break;
            }

        }

        if (next <= end) {

            if (this.var.missingRecordValueIsPrevious()) {
                doMissing((end - next) + 1, _buf, data, (next - 1));
            } else {
                doMissing((end - next) + 1, _buf, data, -1);
            }

        }

        if (this.buffers.size() == 0) {
            this.buffers.add(new ContentDescriptor(_buf, begin, end));
        }

    }

    /**
     *
     * @param direct
     */
    public void setDirect(boolean direct) {

        if (this.allocationMode == null) {
            this.allocationMode = direct;
        }

    }

    /**
     *
     * @param buf
     *
     * @return
     */
    public boolean setUserBuffer(ByteBuffer buf) {

        if (this.allocationMode != null) {
            return false;
        }

        this.userBuffer = buf;
        return true;
    }

    abstract ByteBuffer allocateBuffer(int words);

    abstract Object allocateDataArray(int size);

    /*
     * public static ArrayStore getArrayStore() {return new ArrayStore();}
     * public static ArrayStore getArrayStore(Object o, int offset,
     * int first, int last) throws Throwable {
     * return new ArrayStore(o, offset, first, last);
     * }
     * static class ArrayStore {
     * Object array;
     * int offset;
     * int length = -1;
     * int first;
     * ArrayStore() {
     * }
     * ArrayStore(Object o, int offset, int first, int last) throws
     * Throwable {
     * Class c = componentType(o);
     * if (c == null) throw new Throwable("not an array");
     * if (!c.equals(_class)) throw new Throwable("incompatible type");
     * length = elements*(last - first + 1);
     * array = o;
     * offset = off;
     * }
     * public Object getArray() {return array;}
     * int getSize() {return length;}
     * int getOffset() {return offset;}
     * }
     */

    Class componentType(Object o) {

        if (!o.getClass().isArray()) {
            return null;
        }

        Class _cl = o.getClass();

        while (_cl.isArray()) {
            _cl = _cl.getComponentType();
        }

        return _cl;
    }

    abstract void doData(ByteBuffer bv, int type, int elements, int toprocess, ByteBuffer buf, Object data)
            throws Throwable;

    abstract void doMissing(int records, ByteBuffer buf, Object data, int rec);

    int getLength() {

        if (this._class == Long.TYPE) {
            return 8;
        }

        if (this._class == Double.TYPE) {
            return 8;
        }

        if (this._class == Float.TYPE) {
            return 4;
        }

        if (this._class == Integer.TYPE) {
            return 4;
        }

        if (this._class == Short.TYPE) {
            return 2;
        }

        if (this._class == Byte.TYPE) {
            return 1;
        }

        return -1;
    }

    Object makeArray(int[] _dim, Stride stride) {
        ByteBuffer b = getBuffer();

        if (b == null) {
            return null;
        }

        int pt_size = -1;

        if (_dim.length == 2) {
            pt_size = _dim[0] * _dim[1];
        }

        if (_dim.length == 3) {
            pt_size = _dim[0] * _dim[1] * _dim[2];
        }

        int _stride = 1;
        int[] range = getRecordRange();
        int pts = (range[1] - range[0]) + 1;

        if (stride != null) {
            _stride = stride.getStride(pts);

            if (_stride > 1) {
                int n = (pts / _stride);

                if ((pts % _stride) != 0) {
                    n++;
                }

                pts = n;
            }

        }

        int words = this.elements * pts;
        int advance = _stride * pt_size;
        int offset = 0;
        int n = 0;

        if (this._class == Long.TYPE) {
            long[] la = new long[words];
            // pts = la.length/pt_size;
            LongBuffer lbuf = b.asLongBuffer();

            if (_dim.length == 2) {

                for (int p = 0; p < pts; p++) {

                    for (int j = 0; j < _dim[1]; j++) {

                        for (int i = 0; i < _dim[0]; i++) {
                            la[n++] = lbuf.get(offset + (i * _dim[1]) + j);
                        }

                    }

                    offset += advance;
                }

            }

            if (_dim.length == 3) {

                for (int p = 0; p < pts; p++) {

                    for (int k = 0; k < _dim[2]; k++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int i = 0; i < _dim[0]; i++) {
                                la[n++] = lbuf.get(offset + (i * _dim[1] * _dim[2]) + (j * _dim[2]) + k);
                            }

                        }

                    }

                    offset += advance;
                }

            }

            return la;
        }

        if (this._class == Double.TYPE) {
            double[] da = new double[words];
            // pts = da.length/pt_size;
            DoubleBuffer dbuf = b.asDoubleBuffer();

            if (_dim.length == 2) {

                for (int p = 0; p < pts; p++) {

                    for (int j = 0; j < _dim[1]; j++) {

                        for (int i = 0; i < _dim[0]; i++) {
                            da[n++] = dbuf.get(offset + (i * _dim[1]) + j);
                        }

                    }

                    offset += advance;
                }

            }

            if (_dim.length == 3) {

                for (int p = 0; p < pts; p++) {

                    for (int k = 0; k < _dim[2]; k++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int i = 0; i < _dim[0]; i++) {
                                da[n++] = dbuf.get(offset + (i * _dim[1] * _dim[2]) + (j * _dim[2]) + k);
                            }

                        }

                    }

                    offset += advance;
                }

            }

            return da;
        }

        if (this._class == Float.TYPE) {
            float[] fa = new float[words];
            // pts = fa.length/pt_size;
            FloatBuffer fbuf = b.asFloatBuffer();

            if (_dim.length == 2) {

                for (int p = 0; p < pts; p++) {

                    for (int j = 0; j < _dim[1]; j++) {

                        for (int i = 0; i < _dim[0]; i++) {
                            fa[n++] = fbuf.get(offset + (i * _dim[1]) + j);
                        }

                    }

                    offset += advance;
                }

            }

            if (_dim.length == 3) {

                for (int p = 0; p < pts; p++) {

                    for (int k = 0; k < _dim[2]; k++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int i = 0; i < _dim[0]; i++) {
                                fa[n++] = fbuf.get(offset + (i * _dim[1] * _dim[2]) + (j * _dim[2]) + k);
                            }

                        }

                    }

                    offset += advance;
                }

            }

            return fa;
        }

        if (this._class == Integer.TYPE) {
            int[] ia = new int[words];
            // pts = ia.length/pt_size;
            IntBuffer ibuf = b.asIntBuffer();

            if (_dim.length == 2) {

                for (int p = 0; p < pts; p++) {

                    for (int j = 0; j < _dim[1]; j++) {

                        for (int i = 0; i < _dim[0]; i++) {
                            ia[n++] = ibuf.get(offset + (i * _dim[1]) + j);
                        }

                    }

                    offset += advance;
                }

            }

            if (_dim.length == 3) {

                for (int p = 0; p < pts; p++) {

                    for (int k = 0; k < _dim[2]; k++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int i = 0; i < _dim[0]; i++) {
                                ia[n++] = ibuf.get(offset + (i * _dim[1] * _dim[2]) + (j * _dim[2]) + k);
                            }

                        }

                    }

                    offset += advance;
                }

            }

            return ia;
        }

        if (this._class == Short.TYPE) {
            short[] sa = new short[words];
            // pts = sa.length/pt_size;
            ShortBuffer sbuf = b.asShortBuffer();

            if (_dim.length == 2) {

                for (int p = 0; p < pts; p++) {

                    for (int j = 0; j < _dim[1]; j++) {

                        for (int i = 0; i < _dim[0]; i++) {
                            sa[n++] = sbuf.get(offset + (i * _dim[1]) + j);
                        }

                    }

                    offset += advance;
                }

            }

            if (_dim.length == 3) {

                for (int p = 0; p < pts; p++) {

                    for (int k = 0; k < _dim[2]; k++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int i = 0; i < _dim[0]; i++) {
                                sa[n++] = sbuf.get(offset + (i * _dim[1] * _dim[2]) + (j * _dim[2]) + k);
                            }

                        }

                    }

                    offset += advance;
                }

            }

            return sa;
        }

        byte[] ba = new byte[words];

        // pts = ba.length/pt_size;
        if (_dim.length == 2) {

            for (int p = 0; p < pts; p++) {

                for (int j = 0; j < _dim[1]; j++) {

                    for (int i = 0; i < _dim[0]; i++) {
                        ba[n++] = b.get(offset + (i * _dim[1]) + j);
                    }

                }

                offset += advance;
            }

        }

        if (_dim.length == 3) {

            for (int p = 0; p < pts; p++) {

                for (int k = 0; k < _dim[2]; k++) {

                    for (int j = 0; j < _dim[1]; j++) {

                        for (int i = 0; i < _dim[0]; i++) {
                            ba[n++] = b.get(offset + (i * _dim[1] * _dim[2]) + (j * _dim[2]) + k);
                        }

                    }

                }

                offset += advance;
            }

        }

        b.flip();
        return ba;
    }

    class ContentDescriptor {

        final ByteBuffer buf;

        final int first;

        final int last;

        protected ContentDescriptor(final ByteBuffer _buf, final int _first, final int _last) {
            this.buf = _buf;
            this.first = _first;
            this.last = _last;
        }

        ByteBuffer getBuffer() {
            ByteBuffer rbuf = this.buf.asReadOnlyBuffer();
            rbuf.order(this.buf.order());
            rbuf.position(0);
            return rbuf;
        }

        int getFirstRecord() {
            return this.first;
        }

        int getLastRecord() {
            return this.last;
        }
    }
    /* compatible means value is valid java type -- */
}
