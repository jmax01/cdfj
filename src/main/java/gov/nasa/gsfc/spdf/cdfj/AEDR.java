package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.CDFDataTypes.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Attribute Entry Descriptor Record.
 *
 * @author nand
 */
public class AEDR {

    /** The Constant AEDR_RECORD_SIZE_OFFSET. */
    public static final int AEDR_RECORD_SIZE_OFFSET = 0;

    /** The Constant AEDR_RECORD_SIZE_FIELD_LENGTH. */
    public static final int AEDR_RECORD_SIZE_FIELD_LENGTH = 8;

    /** The Constant AEDR_RECORD_TYPE_OFFSET. */
    public static final int AEDR_RECORD_TYPE_OFFSET = AEDR_RECORD_SIZE_OFFSET + AEDR_RECORD_SIZE_FIELD_LENGTH;

    /** The Constant AEDR_RECORD_TYPE_FIELD_LENGTH. */
    public static final int AEDR_RECORD_TYPE_FIELD_LENGTH = 4;

    /** The Constant AEDR_AEDR_NEXT_FIELD_OFFSET. */
    public static final int AEDR_AEDR_NEXT_FIELD_OFFSET = AEDR_RECORD_TYPE_OFFSET + AEDR_RECORD_TYPE_FIELD_LENGTH;

    /** The Constant AEDR_AEDR_NEXT_FIELD_LENGTH. */
    public static final int AEDR_AEDR_NEXT_FIELD_LENGTH = 8;

    /** The Constant AEDR_ATTR_NUM_OFFSET. */
    public static final int AEDR_ATTR_NUM_OFFSET = AEDR_AEDR_NEXT_FIELD_OFFSET + AEDR_AEDR_NEXT_FIELD_LENGTH;

    /** The Constant AEDR_ATTR_NUM_FIELD_LENGTH. */
    public static final int AEDR_ATTR_NUM_FIELD_LENGTH = 4;

    /** The Constant AEDR_DATA_TYPE_OFFSET. */
    public static final int AEDR_DATA_TYPE_OFFSET = AEDR_ATTR_NUM_OFFSET + AEDR_ATTR_NUM_FIELD_LENGTH;

    /** The Constant AEDR_DATA_TYPE_FIELD_LENGTH. */
    public static final int AEDR_DATA_TYPE_FIELD_LENGTH = 4;

    /** The Constant AEDR_NUM_OFFSET. */
    public static final int AEDR_NUM_OFFSET = AEDR_DATA_TYPE_OFFSET + AEDR_DATA_TYPE_FIELD_LENGTH;

    /** The Constant AEDR_NUM_FIELD_LENGTH. */
    public static final int AEDR_NUM_FIELD_LENGTH = 4;

    /** The Constant AEDR_NUM_ELEMS_OFFSET. */
    public static final int AEDR_NUM_ELEMS_OFFSET = AEDR_NUM_OFFSET + AEDR_NUM_FIELD_LENGTH;

    /** The Constant AEDR_NUM_ELEMS_FIELD_LENGTH. */
    public static final int AEDR_NUM_ELEMS_FIELD_LENGTH = 4;

    /** The Constant AEDR_NUM_STRINGS_OFFSET. */
    public static final int AEDR_NUM_STRINGS_OFFSET = AEDR_NUM_ELEMS_OFFSET + AEDR_NUM_ELEMS_FIELD_LENGTH;

    /** The Constant AEDR_NUM_STRINGS_FIELD_LENGTH. */
    public static final int AEDR_NUM_STRINGS_FIELD_LENGTH = 4;

    /** The Constant AEDR_RFU_B_OFFSET. */
    public static final int AEDR_RFU_B_OFFSET = AEDR_NUM_STRINGS_OFFSET + AEDR_NUM_STRINGS_FIELD_LENGTH;

    /** The Constant AEDR_RFU_B_FIELD_LENGTH. */
    public static final int AEDR_RFU_B_FIELD_LENGTH = 4;

    /** The Constant AEDR_RFU_C_OFFSET. */
    public static final int AEDR_RFU_C_OFFSET = AEDR_RFU_B_OFFSET + AEDR_RFU_B_FIELD_LENGTH;

    /** The Constant AEDR_RFU_C_FIELD_LENGTH. */
    public static final int AEDR_RFU_C_FIELD_LENGTH = 4;

    /** The Constant AEDR_RFU_D_OFFSET. */
    public static final int AEDR_RFU_D_OFFSET = AEDR_RFU_C_OFFSET + AEDR_RFU_C_FIELD_LENGTH;

    /** The Constant AEDR_RFU_D_FIELD_LENGTH. */
    public static final int AEDR_RFU_D_FIELD_LENGTH = 4;

    /** The Constant AEDR_RFU_E_OFFSET. */
    public static final int AEDR_RFU_E_OFFSET = AEDR_RFU_D_OFFSET + AEDR_RFU_D_FIELD_LENGTH;

    /** The Constant AEDR_RFU_E_FIELD_LENGTH. */
    public static final int AEDR_RFU_E_FIELD_LENGTH = 4;

    /** The Constant AEDR_VALUE_OFFSET. */
    public static final int AEDR_VALUE_OFFSET = AEDR_RFU_E_OFFSET + AEDR_RFU_E_FIELD_LENGTH;

    /** The Constant AEDR_FIXED_FIELDS_SIZE. */
    public static final int AEDR_FIXED_FIELDS_SIZE = AEDR_VALUE_OFFSET;

    static final int DEFAULT_AEDR_INT_TYPE = CDF_INT4_INTERNAL_VALUE;

    static final int DEFAULT_AEDR_FLOAT_TYPE = CDF_REAL4_INTERNAL_VALUE;

    static final int DEFAULT_AEDR_DOUBLE_TYPE = CDF_REAL8_INTERNAL_VALUE;

    static final int DEFAULT_AEDR_LONG_TYPE = CDF_INT8_INTERNAL_VALUE;

    static final int DEFAULT_AEDR_SHORT_TYPE = CDF_INT2_INTERNAL_VALUE;

    static final int DEFAULT_AEDR_BYTE_TYPE = CDF_INT1_INTERNAL_VALUE;

    static final int DEFAULT_AEDR_STRING_TYPE = CDF_CHAR_INTERNAL_VALUE;

    static final String STRINGDELIMITER = "\\N ";

    // ByteBuffer record = ByteBuffer.allocate(AEDR_VALUE_OFFSET);

    long aEDRNext;

    long position;

    int aedrAttributeType;

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
     * @param adr   the adr
     * @param type  the type
     * @param value the value must be a string or an array.
     */
    public AEDR(final ADR adr, final int type, final Object value) {

        setAttrNum(adr.num);
        setDataType(type);
        Class<?> clazz = value.getClass();

        if (clazz == String.class) {
            String s = (String) value;
            setValues(s);
            return;
        }

        if (!clazz.isArray()) {
            throw new IllegalArgumentException(
                    "Only String and array types are valid, supplied type was " + clazz.getCanonicalName());
        }

        Class<?> componentType = clazz.getComponentType();

        if (componentType.getComponentType() == String.class) {
            String[] strings = (String[]) value;
            StringBuilder str = new StringBuilder();

            for (int x = 0; x < strings.length; ++x) {
                str.append(strings[x]);

                if (x != (strings.length - 1)) {
                    str.append(STRINGDELIMITER);
                }

            }

            setValues(str.toString());
            return;
        }

        if (componentType == Long.TYPE) {
            long[] la = (long[]) value;
            setValues(la);
            return;
        }

        if (componentType == Double.TYPE) {
            double[] da = (double[]) value;
            setValues(da);
            return;
        }

        if (componentType == Float.TYPE) {
            float[] fa = (float[]) value;
            setValues(fa);
            return;
        }

        if (componentType == Integer.TYPE) {
            int[] ia = (int[]) value;
            setValues(ia);
            return;
        }

        if (componentType == Short.TYPE) {
            short[] sa = (short[]) value;
            setValues(sa);
            return;
        }

        if (componentType == Byte.TYPE) {
            byte[] ba = (byte[]) value;
            setValues(ba);
            return;
        }

        throw new IllegalArgumentException("Arrays of type " + componentType + " are not supported");
    }

    /**
     * Instantiates a new aedr.
     *
     * @param adr   the adr
     * @param value the value
     */
    public AEDR(final ADR adr, final Object value) {
        this(adr, -1, value);
    }

    /**
     * Checks if is AEDR record type.
     *
     * @param aedrRecordType the aedr record type
     *
     * @return true, if is AEDR record type
     */
    public static boolean isAEDRRecordType(final int aedrRecordType) {
        return (aedrRecordType == GlobalAttributeEntry.GLOBAL_ATTRIBUTE_RECORD_TYPE)
            || (aedrRecordType == VariableAttributeEntry.VARIABLE_ATTRIBUTE_RECORD_TYPE);
    }

    /**
     * Gets the.
     *
     * @return the byte buffer
     */
    public ByteBuffer get() {

        int recordSize = AEDR_FIXED_FIELDS_SIZE + this.values.length;

        ByteBuffer fixedFields = ByteBuffer.allocateDirect(AEDR_FIXED_FIELDS_SIZE);

        ByteBuffer buf = ByteBuffer.allocate(recordSize);

        fixedFields.position(0);
        fixedFields.putLong(recordSize);
        fixedFields.putInt(getAEDRAttributeType());
        fixedFields.putLong(this.aEDRNext);
        fixedFields.putInt(this.attrNum);
        fixedFields.putInt(this.dataType);
        fixedFields.putInt(this.num);
        fixedFields.putInt(this.numElems);

        if ((this.aedrAttributeType != GlobalAttributeEntry.GLOBAL_ATTRIBUTE_RECORD_TYPE)
            && CDFDataTypes.isCharType(this.dataType)) {

            int lastIndex = 0;

            int count = 1;

            while ((lastIndex = new String(this.values).indexOf(STRINGDELIMITER, lastIndex)) != -1) {
                count++;
                lastIndex += STRINGDELIMITER.length() - 1;
            }

            fixedFields.putInt(count);

            for (int i = 0; i < 2; i++) {
                fixedFields.putInt(0);
            }

        } else {

            for (int i = 0; i < 3; i++) {
                fixedFields.putInt(0);
            }

        }

        for (int i = 0; i < 2; i++) {
            fixedFields.putInt(-1);
        }

        fixedFields.position(0);

        buf.put(fixedFields);

        buf.put(this.values);

        buf.position(0);

        return buf;
    }

    /**
     * Gets the AEDR attribute type.
     *
     * @return the AEDR attribute type
     */
    public int getAEDRAttributeType() {
        return this.aedrAttributeType;
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
        return AEDR_FIXED_FIELDS_SIZE + this.values.length;
    }

    /**
     * Sets the AEDR next.
     *
     * @param l the new AEDR next
     */
    public void setAEDRNext(final long l) {
        this.aEDRNext = l;
    }

    /**
     * Sets the attribute type.
     *
     * @param aedrAttributeType the new attribute type
     *
     * @deprecated Do not use
     */
    @Deprecated
    public void setAttributeType(final int aedrAttributeType) {

        if (!isAEDRRecordType(aedrAttributeType)) {
            throw new IllegalArgumentException(
                    "The supplied aedrAttributeType, " + aedrAttributeType + ", is not a valid aedrAttributeType."
                            + " Valid types are " + GlobalAttributeEntry.GLOBAL_ATTRIBUTE_RECORD_TYPE + " (Global) or "
                            + VariableAttributeEntry.VARIABLE_ATTRIBUTE_RECORD_TYPE + " (variable");
        }

        this.aedrAttributeType = aedrAttributeType;
    }

    /**
     * Sets the attr num.
     *
     * @param n the new attr num
     */
    public void setAttrNum(final int n) {
        this.attrNum = n;
    }

    /**
     * Sets the data type.
     *
     * @param n the new data type
     */
    public void setDataType(final int n) {

        if (this.dataType != -1) {
            throw new IllegalArgumentException("Data type is already defined");
        }

        this.dataType = n;
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
     * @param n the new num elems
     */
    public void setNumElems(final int n) {
        this.numElems = n;
    }

    /**
     * Sets the values.
     *
     * @param ba the new values
     */
    public void setValues(final byte[] ba) {

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_BYTE_TYPE);
        } else {

            if (((this.dataType != 1) && (this.dataType != 11))) {
                throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for Byte.");
            }

        }

        this.values = new byte[ba.length];
        System.arraycopy(ba, 0, this.values, 0, ba.length);
        setNumElems(ba.length);
    }

    /**
     * Sets the values.
     *
     * @param da the new values
     */
    public void setValues(final double[] da) {
        setNumElems(da.length);

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_DOUBLE_TYPE);
            ByteBuffer buf = ByteBuffer.allocate(8 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.asDoubleBuffer()
                    .put(da);
            this.values = new byte[8 * da.length];
            buf.get(this.values);
            return;
        }

        if ((this.dataType == 22) || (this.dataType == 45)
            || (this.dataType == 31)
            || (this.dataType == 32)) {

            if (this.dataType == 32) {
                setNumElems(da.length / 2);
            }

            ByteBuffer buf = ByteBuffer.allocate(8 * da.length);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.asDoubleBuffer()
                    .put(da);
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

        throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for Double.");
    }

    /**
     * Sets the values.
     *
     * @param fa the new values
     */
    public void setValues(final float[] fa) {
        setNumElems(fa.length);

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_FLOAT_TYPE);
        } else {

            if (((this.dataType != 21) && (this.dataType != 44))) {
                throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for Float.");
            }

        }

        ByteBuffer buf = ByteBuffer.allocate(4 * fa.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asFloatBuffer()
                .put(fa);
        this.values = new byte[4 * fa.length];
        buf.get(this.values);
    }

    /**
     * Sets the values.
     *
     * @param ia the new values
     */
    public void setValues(final int[] ia) {
        setNumElems(ia.length);

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_INT_TYPE);
        } else {

            if (((this.dataType != 4) && (this.dataType != 14))) {
                throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for Int.");
            }

        }

        ByteBuffer buf = ByteBuffer.allocate(4 * ia.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asIntBuffer()
                .put(ia);
        this.values = new byte[4 * ia.length];
        buf.get(this.values);
    }

    /**
     * Sets the values.
     *
     * @param la the new values
     */
    public void setValues(final long[] la) {

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_LONG_TYPE);
        } else {

            if (((this.dataType != 8) && (this.dataType != 33))) {
                throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for Long.");
            }

        }

        setNumElems(la.length);
        ByteBuffer buf = ByteBuffer.allocate(8 * la.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asLongBuffer()
                .put(la);
        this.values = new byte[8 * la.length];
        buf.get(this.values);
    }

    /**
     * Sets the values.
     *
     * @param sa the new values
     */
    public void setValues(final short[] sa) {
        setNumElems(sa.length);

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_SHORT_TYPE);
        } else {

            if (((this.dataType != 2) && (this.dataType != 12))) {
                throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for Short.");
            }

        }

        ByteBuffer buf = ByteBuffer.allocate(2 * sa.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asShortBuffer()
                .put(sa);
        this.values = new byte[2 * sa.length];
        buf.get(this.values);
    }

    /**
     * Sets the values.
     *
     * @param s the new values
     */
    public void setValues(final String s) {
        setNumElems(s.length());

        if (this.dataType == -1) {
            setDataType(DEFAULT_AEDR_STRING_TYPE);
        } else {

            if ((this.dataType < 50) || (this.dataType > 52)) {
                throw new IllegalArgumentException("Incompatible data type " + this.dataType + " for String.");
            }

        }

        this.values = s.getBytes();
    }

    /**
     * Sets the values.
     *
     * @param s the new values
     */
    public void setValues(final String[] s) {
        int x = s.length;
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < x; ++i) {
            str.append(s[i]);

            if (i != (x - 1)) {
                str.append(STRINGDELIMITER);
            }

        }

        this.setValues(str.toString());
    }
}