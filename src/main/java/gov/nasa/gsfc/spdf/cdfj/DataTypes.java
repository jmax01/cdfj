package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * The Class DataTypes.
 */
public final class DataTypes {

    /** The Constant ENCODING_COUNT. */
    public static final int ENCODING_COUNT = 17;

    static final ByteOrder[] endian_ness = new ByteOrder[ENCODING_COUNT];

    static {

        for (int i = 0; i < ENCODING_COUNT; i++) {
            endian_ness[i] = null;
        }

        endian_ness[1] = ByteOrder.BIG_ENDIAN;
        endian_ness[2] = ByteOrder.BIG_ENDIAN;
        endian_ness[4] = ByteOrder.LITTLE_ENDIAN;
        endian_ness[5] = ByteOrder.BIG_ENDIAN;
        endian_ness[6] = ByteOrder.LITTLE_ENDIAN;
        endian_ness[7] = ByteOrder.BIG_ENDIAN;
        endian_ness[9] = ByteOrder.BIG_ENDIAN;
        endian_ness[12] = ByteOrder.BIG_ENDIAN;
        endian_ness[13] = ByteOrder.LITTLE_ENDIAN;
        endian_ness[16] = ByteOrder.LITTLE_ENDIAN;
    }

    /** The Constant EPOCH16. */
    public static final int EPOCH16 = 32;

    /** The Constant CDF_TIME_TT2000. */
    public static final int CDF_TIME_TT2000 = 33;

    /** The Constant FLOAT. */
    public static final int FLOAT = 0;

    /** The Constant DOUBLE. */
    public static final int DOUBLE = 1;

    /** The Constant SIGNED_INTEGER. */
    public static final int SIGNED_INTEGER = 2;

    /** The Constant UNSIGNED_INTEGER. */
    public static final int UNSIGNED_INTEGER = 3;

    /** The Constant STRING. */
    public static final int STRING = 4;

    /** The Constant LONG. */
    public static final int LONG = 5;

    /** The Constant LAST_TYPE. */
    public static final int LAST_TYPE = 53;

    static final Method[] method = new Method[LAST_TYPE];

    static final int[] typeCategory = new int[LAST_TYPE];

    static final int[] size = new int[LAST_TYPE];

    static final long[] longInt = new long[LAST_TYPE];

    static {

        for (int i = 0; i < LAST_TYPE; i++) {
            method[i] = null;
            size[i] = 1;
            typeCategory[i] = -1;
        }

        // byte

        try {
            Class<ByteBuffer> bb = ByteBuffer.class;
            Method meth = bb.getMethod("get");
            method[1] = meth;
            typeCategory[1] = SIGNED_INTEGER;
            method[11] = meth;
            typeCategory[11] = UNSIGNED_INTEGER;
            method[41] = meth;
            typeCategory[41] = SIGNED_INTEGER;
            meth = bb.getMethod("getShort");
            method[2] = meth;
            typeCategory[2] = SIGNED_INTEGER;
            size[2] = 2;
            method[12] = meth;
            typeCategory[12] = UNSIGNED_INTEGER;
            size[12] = 2;
            meth = bb.getMethod("getInt");
            method[4] = meth;
            typeCategory[4] = SIGNED_INTEGER;
            size[4] = 4;
            method[14] = meth;
            typeCategory[14] = UNSIGNED_INTEGER;
            size[14] = 4;
            meth = bb.getMethod("getLong");
            method[8] = meth;
            typeCategory[8] = LONG;
            size[8] = 8;
            method[33] = meth;
            typeCategory[33] = LONG;
            size[33] = 8;
            meth = bb.getMethod("getFloat");
            method[21] = meth;
            typeCategory[21] = FLOAT;
            size[21] = 4;
            method[44] = meth;
            typeCategory[44] = FLOAT;
            size[44] = 4;
            meth = bb.getMethod("getDouble");
            method[22] = meth;
            typeCategory[22] = DOUBLE;
            size[22] = 8;
            method[45] = meth;
            typeCategory[45] = DOUBLE;
            size[45] = 8;
            method[31] = meth;
            typeCategory[31] = DOUBLE;
            size[31] = 8;
            method[32] = meth;
            typeCategory[32] = DOUBLE;
            size[32] = 8;
            typeCategory[51] = STRING;
            typeCategory[52] = STRING;
        } catch (NoSuchMethodException | SecurityException ex) {

            throw new IllegalStateException("Failed to populate arrays", ex);
        }

        for (int i = 0; i < LAST_TYPE; i++) {

            if (size[i] <= 4) {
                longInt[i] = (1L) << (8 * size[i]);
            }

        }

    }

    /**
     * Instantiates a new data types.
     */
    private DataTypes() {
        Class<?> tc = getClass();

        try {
            Method meth = tc.getMethod("getString", ByteBuffer.class, Integer.class);
            method[51] = meth;
            method[52] = meth;
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalStateException("Failed to obtain getString methods", ex);
        }

    }

    /**
     * Default pad.
     *
     * @param type the type
     *
     * @return the object
     */
    public static Object defaultPad(final int type) {

        if (isLongType(type)) {
            return -9_223_372_036_854_775_807L;
        }

        if (isStringType(type)) {
            return " ".getBytes(StandardCharsets.US_ASCII)[0];
        }

        return Double.valueOf(0);
    }

    /**
     * Gets the byte order.
     *
     * @param encoding the encoding
     *
     * @return the byte order
     * @
     */
    public static ByteOrder getByteOrder(final int encoding) {

        if (endian_ness[encoding] != null) {
            return endian_ness[encoding];
        }

        throw new IllegalArgumentException("Unsupported encoding " + encoding);
    }

    /**
     * Gets the string.
     *
     * @param buf the buf
     * @param nc  the nc
     *
     * @return the string
     */
    public static String getString(final ByteBuffer buf, final Integer nc) {
        ByteBuffer slice = buf.slice();
        byte[] ba = new byte[nc];
        int i = 0;

        for (; i < ba.length; i++) {
            ba[i] = slice.get();

            if (ba[i] == 0) {
                break;
            }

        }

        return new String(ba, 0, i);
    }

    /**
     * Checks if is long type.
     *
     * @param type the type
     *
     * @return true, if is long type
     */
    public static boolean isLongType(final int type) {
        return (typeCategory[type] == LONG);
    }

    /**
     * Checks if is string type.
     *
     * @param type the type
     *
     * @return true, if is string type
     */
    public static boolean isStringType(final int type) {
        return (typeCategory[type] == STRING);
    }
}
