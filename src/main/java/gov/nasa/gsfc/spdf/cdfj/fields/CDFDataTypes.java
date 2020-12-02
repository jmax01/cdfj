package gov.nasa.gsfc.spdf.cdfj.fields;

import java.math.*;
import java.nio.*;
import java.time.*;
import java.util.stream.*;

/**
 * The Class CDFDataTypes.
 */
public final class CDFDataTypes {

    /** 1-byte, signed integer. */
    public static final int CDF_INT1_INTERNAL_VALUE = 1;

    /** 2-byte, signed integer.. */
    public static final int CDF_INT2_INTERNAL_VALUE = 2;

    /** 4-byte, signed integer. */
    public static final int CDF_INT4_INTERNAL_VALUE = 4;

    /** 8-byte, signed integer. */
    public static final int CDF_INT8_INTERNAL_VALUE = 8;

    /** 1-byte, unsigned integer. */
    public static final int CDF_UINT1_INTERNAL_VALUE = 11;

    /** 2-byte, unsigned integer. */
    public static final int CDF_UINT2_INTERNAL_VALUE = 12;

    /** 4-byte, unsigned integer. */
    public static final int CDF_UINT4_INTERNAL_VALUE = 14;

    /** 1-byte, signed integer. */
    public static final int CDF_BYTE_INTERNAL_VALUE = 41;

    /** 4-byte, single-precision floating-point. */
    public static final int CDF_REAL4_INTERNAL_VALUE = 21;

    /** 8-byte, double-precision floating-point. */
    public static final int CDF_REAL8_INTERNAL_VALUE = 22;

    /** 4-byte, single-precision floating-point. */
    public static final int CDF_FLOAT_INTERNAL_VALUE = 44;

    /** 8-byte, double-precision floating-point. */
    public static final int CDF_DOUBLE_INTERNAL_VALUE = 45;

    /** 8-byte, double-precision floating-point. */
    public static final int CDF_EPOCH_INTERNAL_VALUE = 31;

    /** 8-byte, double-precision floating-point. */
    public static final int CDF_EPOCH16_INTERNAL_VALUE = 32;

    /** 8-bye, signed integer. */
    public static final int CDF_TIME_TT2000_INTERNAL_VALUE = 33;

    /** 1-byte, signed character (ASCII). */
    public static final int CDF_CHAR_INTERNAL_VALUE = 51;

    /** 1-byte, unsigned character (ASCII). */
    public static final int CDF_UCHAR_INTERNAL_VALUE = 52;

    public static final OffsetDateTime CDF_EPOCH_AS_OFFSET_DATE_TIME = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC);

    public static final long MILLIS_FROM_CDF_EPOCH = CDF_EPOCH_AS_OFFSET_DATE_TIME.toInstant()
            .toEpochMilli();

    public static final double DOUBLE_FILL = -1.0e31;

    public static final BigDecimal DOUBLE_FILL_AS_BIG_DECIMAL = BigDecimal.valueOf(DOUBLE_FILL);

    private CDFDataTypes() {}

    /**
     * Size in bytes.
     *
     * @param dataType the data type
     * 
     * @return the int
     */
    public static int sizeInBytes(int dataType) {

        switch (dataType) {
            case CDF_BYTE_INTERNAL_VALUE:
            case CDF_INT1_INTERNAL_VALUE:
            case CDF_UINT1_INTERNAL_VALUE:
            case CDF_CHAR_INTERNAL_VALUE:
            case CDF_UCHAR_INTERNAL_VALUE:
                return 1;

            case CDF_INT2_INTERNAL_VALUE:
            case CDF_UINT2_INTERNAL_VALUE:
                return 2;

            case CDF_INT4_INTERNAL_VALUE:
            case CDF_UINT4_INTERNAL_VALUE:
            case CDF_REAL4_INTERNAL_VALUE:
            case CDF_FLOAT_INTERNAL_VALUE:
                return 4;

            case CDF_INT8_INTERNAL_VALUE:
            case CDF_REAL8_INTERNAL_VALUE:
            case CDF_DOUBLE_INTERNAL_VALUE:
            case CDF_EPOCH_INTERNAL_VALUE:
            case CDF_TIME_TT2000_INTERNAL_VALUE:
                return 8;

            case CDF_EPOCH16_INTERNAL_VALUE:
                return 16;
            default:
                throw new IllegalArgumentException("Unknown datatype: " + dataType);

        }

    }

    /**
     * Checks if is datatype size is 1 byte.
     *
     * @param type the type
     * 
     * @return true, if is 1 byte signed int
     */
    public static boolean is1Byte(final int type) {

        switch (type) {
            case CDF_BYTE_INTERNAL_VALUE:
            case CDF_INT1_INTERNAL_VALUE:
            case CDF_UINT1_INTERNAL_VALUE:
            case CDF_CHAR_INTERNAL_VALUE:
            case CDF_UCHAR_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is datatype size is 1 byte.
     *
     * @param type the type
     * 
     * @return true, if is 1 byte signed int
     */
    public static boolean is1ByteInteger(final int type) {

        switch (type) {
            case CDF_INT1_INTERNAL_VALUE:
            case CDF_UINT1_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 1 byte signed int.
     *
     * @param type the type
     * 
     * @return true, if is 1 byte signed int
     */
    public static boolean is1ByteSignedInteger(final int type) {

        switch (type) {
            case CDF_INT1_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 1 byte unsigned int.
     *
     * @param type the type
     * 
     * @return true, if is 1 byte unsigned int
     */
    public static boolean is1ByteUnsignedInteger(final int type) {

        switch (type) {
            case CDF_UINT1_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 2 byte integer.
     *
     * @param type the type
     * 
     * @return true, if is 2 byte integer
     */
    public static boolean is2ByteInteger(final int type) {

        switch (type) {
            case CDF_INT2_INTERNAL_VALUE:
            case CDF_UINT2_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 2 byte signed integer.
     *
     * @param type the type
     * 
     * @return true, if is 2 byte signed integer
     */
    public static boolean is2ByteSignedInteger(final int type) {

        switch (type) {
            case CDF_INT2_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 2 byte unsigned integer.
     *
     * @param type the type
     * 
     * @return true, if is 2 byte unsigned integer
     */
    public static boolean is2ByteUnsignedInteger(final int type) {

        switch (type) {
            case CDF_UINT2_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    public static boolean is4ByteInteger(int type) {

        switch (type) {
            case CDF_INT4_INTERNAL_VALUE:
            case CDF_UINT4_INTERNAL_VALUE:

                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 4-byte, signed integer.
     *
     * @param type the type
     *
     * @return true, if is 4-byte, signed integer.
     */
    public static boolean is4ByteSignedInteger(final int type) {

        switch (type) {
            case CDF_INT4_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    public static boolean is4ByteUnsignedInteger(final int type) {

        switch (type) {
            case CDF_UINT4_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 8 byte signed.
     *
     * @param type the type
     * 
     * @return true, if is 8 byte signed
     */
    public static boolean is8ByteSigned(final int type) {

        switch (type) {
            case CDF_INT8_INTERNAL_VALUE:
            case CDF_TIME_TT2000_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    public static boolean is8ByteInteger(int type) {

        switch (type) {
            case CDF_INT8_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 8 byte signed.
     *
     * @param type the type
     * 
     * @return true, if is 8 byte signed
     */
    public static boolean is8ByteSignedInteger(final int type) {

        switch (type) {
            case CDF_INT8_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 4 byte single precision type.
     *
     * @param type the type
     *
     * @return true, if is 4 byte single precision type
     */
    public static boolean is4ByteSinglePrecisionType(final int type) {

        switch (type) {
            case CDF_REAL4_INTERNAL_VALUE:
            case CDF_FLOAT_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 8 byte double precision type.
     *
     * @param type the type
     *
     * @return true, if is 8 byte double precision type
     */
    public static boolean is8ByteDoublePrecisionType(final int type) {

        switch (type) {
            case CDF_REAL8_INTERNAL_VALUE:
            case CDF_TIME_TT2000_INTERNAL_VALUE:
            case CDF_EPOCH_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is 8 byte double precision type.
     *
     * @param type the type
     *
     * @return true, if is 8 byte double precision type
     */
    public static boolean isTemporalType(final int type) {

        switch (type) {
            case CDF_EPOCH_INTERNAL_VALUE:
            case CDF_TIME_TT2000_INTERNAL_VALUE:
            case CDF_EPOCH16_INTERNAL_VALUE:
                return true;
            default:
                return false;
        }

    }

    /**
     * Checks if is char type.
     *
     * @param type the type
     *
     * @return true, if is char type
     */
    public static boolean isCharType(final int type) {
        return (type == CDF_CHAR_INTERNAL_VALUE) || (type == CDF_UCHAR_INTERNAL_VALUE);
    }

    /**
     * To short array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the short[]
     */
    public static short[] toShortArray(byte[] bytes, int numberOfElements) {

        short[] result = new short[numberOfElements];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < numberOfElements; i++) {
            result[i] = buffer.getShort();
        }

        return result;
    }

    /**
     * To int array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the int[]
     */
    public static int[] toIntArray(byte[] bytes, int numberOfElements) {

        int[] result = new int[numberOfElements];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < numberOfElements; i++) {
            result[i] = buffer.getInt();
        }

        return result;
    }

    /**
     * To long array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the long[]
     */
    public static long[] toLongArray(byte[] bytes, int numberOfElements) {

        long[] result = new long[numberOfElements];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < numberOfElements; i++) {
            result[i] = buffer.getLong();
        }

        return result;
    }

    /**
     * To float array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the float[]
     */
    public static float[] toFloatArray(byte[] bytes, int numberOfElements) {

        float[] result = new float[numberOfElements];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < numberOfElements; i++) {

            result[i] = buffer.getFloat();
        }

        return result;
    }

    /**
     * To double array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the double[]
     */
    public static double[] toDoubleArray(byte[] bytes, int numberOfElements) {

        double[] result = new double[numberOfElements];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < numberOfElements; i++) {
            result[i] = buffer.getDouble();
        }

        return result;
    }

    /**
     * To double stream.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the double stream
     */
    public static DoubleStream toDoubleStream(byte[] bytes, int numberOfElements) {

        return DoubleStream.of(toDoubleArray(bytes, numberOfElements));
    }

}
