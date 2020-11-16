package gov.nasa.gsfc.spdf.cdfj;

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

    private CDFDataTypes() {}

    /**
     * Checks if is 8 byte double percision type.
     *
     * @param type the type
     *
     * @return true, if is 8 byte double percision type
     */
    public static boolean is8ByteDoublePercisionType(final int type) {

        switch (type) {
            case CDF_REAL8_INTERNAL_VALUE:
            case CDF_DOUBLE_INTERNAL_VALUE:
            case CDF_EPOCH_INTERNAL_VALUE:
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
}
