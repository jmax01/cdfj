package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author nand
 */
public class AEDR {

    static int INT_TYPE = 4;

    static int FLOAT_TYPE = 21;

    static int DOUBLE_TYPE = 22;

    static int LONG_TYPE = 8;

    static int SHORT_TYPE = 2;

    static int BYTE_TYPE = 1;

    static int STRING_TYPE = 51;

    static String STRINGDELIMITER = "\\N ";

    ByteBuffer record = ByteBuffer.allocate(8 + 4 + 8 + 4 + 4 + 4 + 4 + (3 * 4) + (2 * 4));

    long aEDRNext;

    /**
     *
     */
    protected long position;

    int attributeType;

    int attrNum;

    int dataType = -1;

    int num;

    int numElems;

    byte[] values;

    /**
     * Constructs AEDR of a given type and value for an ADR.Specification of type is
     * deferred if type = -1.Once set,
     * data type cannot be changed.
     *
     * @param adr
     * @param type
     * @param value
     *
     * @throws java.lang.Throwable
     */
    public AEDR(ADR adr, int type, Object value) throws Throwable {
        setAttrNum(adr.num);
        setDataType(type);
        Class<?> c = value.getClass();

        if (c == String.class) {
            String s = (String) value;
            setValues(s);
            return;
        }

        if (c.isArray() && (c.getComponentType() == String.class)) {
            String[] strings = (String[]) value;
            int x;
            StringBuilder str = new StringBuilder();

            for (x = 0; x < strings.length; ++x) {
                str.append(strings[x]);

                if (x != (strings.length - 1)) {
                    str.append(STRINGDELIMITER);
                }

            }

            setValues(str.toString());
            return;
        }

        if (!c.isArray()) {
            throw new Throwable("supplied object not an array");
        }

        c = c.getComponentType();

        if (c == Long.TYPE) {
            long[] la = (long[]) value;
            setValues(la);
            return;
        }

        if (c == Double.TYPE) {
            double[] da = (double[]) value;
            setValues(da);
            return;
        }

        if (c == Float.TYPE) {
            float[] fa = (float[]) value;
            setValues(fa);
            return;
        }

        if (c == Integer.TYPE) {
            int[] ia = (int[]) value;
            setValues(ia);
            return;
        }

        if (c == Short.TYPE) {
            short[] sa = (short[]) value;
            setValues(sa);
            return;
        }

        if (c == Byte.TYPE) {
            byte[] ba = (byte[]) value;
            setValues(ba);
            return;
        }

        throw new Throwable("Arrays of type " + c + " not supported");
    }

    /**
     *
     * @param adr
     * @param o
     *
     * @throws Throwable
     */
    public AEDR(ADR adr, Object value) throws Throwable {
        this(adr, -1, value);
    }

    /**
     *
     * @return
     */
    public ByteBuffer get() {
        int capacity = this.record.capacity() + this.values.length;
        ByteBuffer buf = ByteBuffer.allocate(capacity);
        this.record.position(0);
        this.record.putLong(capacity);
        this.record.putInt(this.attributeType);
        this.record.putLong(this.aEDRNext);
        this.record.putInt(this.attrNum);
        this.record.putInt(this.dataType);
        this.record.putInt(this.num);
        this.record.putInt(this.numElems);

        if ((this.attributeType != 5) && ((this.dataType == 51) || (this.dataType == 52))) {
            int lastIndex = 0;
            int count = 1;

            while ((lastIndex = new String(this.values).indexOf(STRINGDELIMITER, lastIndex)) != -1) {
                count++;
                lastIndex += STRINGDELIMITER.length() - 1;
            }

            this.record.putInt(count);

            for (int i = 0; i < 2; i++) {
                this.record.putInt(0);
            }

        } else {

            for (int i = 0; i < 3; i++) {
                this.record.putInt(0);
            }

        }

        for (int i = 0; i < 2; i++) {
            this.record.putInt(-1);
        }

        this.record.position(0);
        buf.put(this.record);
        buf.put(this.values);
        buf.position(0);
        return buf;
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
        return this.record.capacity() + this.values.length;
    }

    /**
     *
     * @param l
     */
    public void setAEDRNext(long l) {
        this.aEDRNext = l;
    }

    /**
     *
     * @param n
     */
    public void setAttributeType(int n) {
        this.attributeType = n;
    }

    /**
     *
     * @param n
     */
    public void setAttrNum(int n) {
        this.attrNum = n;
    }

    /**
     *
     * @param n
     *
     * @throws Throwable
     */
    public void setDataType(int n) throws Throwable {

        if (this.dataType != -1) {
            throw new Throwable("Data type is already defined");
        }

        this.dataType = n;
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
     * @param n
     */
    public void setNumElems(int n) {
        this.numElems = n;
    }

    /**
     *
     * @param ba
     *
     * @throws Throwable
     */
    public void setValues(byte[] ba) throws Throwable {

        if (this.dataType == -1) {
            setDataType(BYTE_TYPE);
        } else {

            if (((this.dataType != 1) && (this.dataType != 11))) {
                throw new Throwable("Incompatible data type " + this.dataType + " for Byte.");
            }

        }

        this.values = new byte[ba.length];
        System.arraycopy(ba, 0, this.values, 0, ba.length);
        setNumElems(ba.length);
    }

    /**
     *
     * @param da
     *
     * @throws Throwable
     */
    public void setValues(double[] da) throws Throwable {
        setNumElems(da.length);

        if (this.dataType == -1) {
            setDataType(DOUBLE_TYPE);
            ByteBuffer buf = ByteBuffer.allocate(8 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.asDoubleBuffer().put(da);
            this.values = new byte[8 * da.length];
            buf.get(this.values);
            return;
        }

        if ((this.dataType == 22) || (this.dataType == 45) || (this.dataType == 31) || (this.dataType == 32)) {

            if (this.dataType == 32) {
                setNumElems(da.length / 2);
            }

            ByteBuffer buf = ByteBuffer.allocate(8 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.asDoubleBuffer().put(da);
            this.values = new byte[8 * da.length];
            buf.get(this.values);
            return;
        }

        if ((this.dataType == 21) || (this.dataType == 44)) {
            ByteBuffer buf = ByteBuffer.allocate(4 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            for (double v : da) {
                buf.putFloat((float) v);
            }

            this.values = new byte[4 * da.length];
            buf.position(0);
            buf.get(this.values);
            return;
        }

        if ((this.dataType == 1) || (this.dataType == 11)) {
            this.values = new byte[da.length];

            for (int i = 0; i < da.length; i++) {
                this.values[i] = (byte) da[i];
            }

            return;
        }

        if ((this.dataType == 2) || (this.dataType == 12)) {
            ByteBuffer buf = ByteBuffer.allocate(2 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            for (double v : da) {
                buf.putShort((short) v);
            }

            this.values = new byte[2 * da.length];
            buf.position(0);
            buf.get(this.values);
            return;
        }

        if (this.dataType == 4) {
            ByteBuffer buf = ByteBuffer.allocate(4 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            for (double v : da) {
                buf.putInt((int) v);
            }

            this.values = new byte[4 * da.length];
            buf.position(0);
            buf.get(this.values);
            return;
        }

        if (this.dataType == 14) {
            ByteBuffer buf = ByteBuffer.allocate(4 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            for (double v : da) {
                long lval = (long) v;
                buf.putInt((int) lval);
            }

            this.values = new byte[4 * da.length];
            buf.position(0);
            buf.get(this.values);
            return;
        }

        throw new Throwable("Incompatible data type " + this.dataType + " for Double.");
    }

    /**
     *
     * @param fa
     *
     * @throws Throwable
     */
    public void setValues(float[] fa) throws Throwable {
        setNumElems(fa.length);

        if (this.dataType == -1) {
            setDataType(FLOAT_TYPE);
        } else {

            if (((this.dataType != 21) && (this.dataType != 44))) {
                throw new Throwable("Incompatible data type " + this.dataType + " for Float.");
            }

        }

        ByteBuffer buf = ByteBuffer.allocate(4 * fa.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asFloatBuffer().put(fa);
        this.values = new byte[4 * fa.length];
        buf.get(this.values);
    }

    /**
     *
     * @param ia
     *
     * @throws Throwable
     */
    public void setValues(int[] ia) throws Throwable {
        setNumElems(ia.length);

        if (this.dataType == -1) {
            setDataType(INT_TYPE);
        } else {

            if (((this.dataType != 4) && (this.dataType != 14))) {
                throw new Throwable("Incompatible data type " + this.dataType + " for Int.");
            }

        }

        ByteBuffer buf = ByteBuffer.allocate(4 * ia.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asIntBuffer().put(ia);
        this.values = new byte[4 * ia.length];
        buf.get(this.values);
    }

    /**
     *
     * @param la
     *
     * @throws Throwable
     */
    public void setValues(long[] la) throws Throwable {

        if (this.dataType == -1) {
            setDataType(LONG_TYPE);
        } else {

            if (((this.dataType != 8) && (this.dataType != 33))) {
                throw new Throwable("Incompatible data type " + this.dataType + " for Long.");
            }

        }

        setNumElems(la.length);
        ByteBuffer buf = ByteBuffer.allocate(8 * la.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asLongBuffer().put(la);
        this.values = new byte[8 * la.length];
        buf.get(this.values);
    }

    /**
     *
     * @param sa
     *
     * @throws Throwable
     */
    public void setValues(short[] sa) throws Throwable {
        setNumElems(sa.length);

        if (this.dataType == -1) {
            setDataType(SHORT_TYPE);
        } else {

            if (((this.dataType != 2) && (this.dataType != 12))) {
                throw new Throwable("Incompatible data type " + this.dataType + " for Short.");
            }

        }

        ByteBuffer buf = ByteBuffer.allocate(2 * sa.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asShortBuffer().put(sa);
        this.values = new byte[2 * sa.length];
        buf.get(this.values);
    }

    /**
     *
     * @param s
     *
     * @throws Throwable
     */
    public void setValues(String s) throws Throwable {
        setNumElems(s.length());

        if (this.dataType == -1) {
            setDataType(STRING_TYPE);
        } else {

            if ((this.dataType < 50) || (this.dataType > 52)) {
                throw new Throwable("Incompatible data type " + this.dataType + " for String.");
            }

        }

        this.values = s.getBytes();
    }

    /**
     *
     * @param s
     *
     * @throws Throwable
     */
    public void setValues(String[] s) throws Throwable {
        int x = s.length;
        int i;
        StringBuilder str = new StringBuilder();

        for (i = 0; i < x; ++i) {
            str.append(s[i]);

            if (i != (x - 1)) {
                str.append(STRINGDELIMITER);
            }

        }

        this.setValues(str.toString());
    }
}
