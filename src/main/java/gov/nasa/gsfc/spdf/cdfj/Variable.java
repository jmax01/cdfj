package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteOrder;

/**
 * Interface that defines methods for getting properties of
 * a CDF variable.
 */
public interface Variable extends VariableMetaData {

    /**
     * Returns this variable's values as byte[] if variable type is byte,
     * unsigned byte or char.Otherwise,
     *
     * @return the byte[]
     */
    byte[] asByteArray();

    /**
     * Returns this variable's values for a range of records as byte[] if
     * variable type is byte, unsigned byte or char.Otherwise, throws
     * Throwable
     *
     * @param pt the pt
     *
     * @return the byte[]
     */
    byte[] asByteArray(int[] pt);

    /**
     * Returns this variable's values as double[].If variable type cannot be cast to
     * double, a Throwable is thrown.
     *
     * @return the double[]
     */
    double[] asDoubleArray();

    /**
     * Returns this variable's values for the specified range of records as
     * double[].If variable type cannot be cast to double, a Throwable is thrown.If
     * preserve is true, a Throwable is thrown for variables of type long
     * to signal possible loss of precision.
     *
     * @param preserve the preserve
     * @param pt       the pt
     *
     * @return the double[]
     */
    double[] asDoubleArray(boolean preserve, int[] pt);

    /**
     * Returns this variable's values for the specified range of records as
     * double[].If variable type cannot be cast to double, a Throwable is thrown.
     *
     * @param pt the pt
     *
     * @return the double[]
     */
    double[] asDoubleArray(int[] pt);

    /**
     * Returns this variable's values as float[].If variable type cannot be cast to
     * float, a Throwable is thrown.
     *
     * @return the float[]
     */
    float[] asFloatArray();

    /**
     * Returns this variable's values for the specified range of records as
     * float[].If variable type cannot be cast to float, a Throwable is thrown.If
     * preserve is true, a Throwable is thrown for variables of type double,
     * long or int to signal possible loss of precision.
     *
     * @param preserve the preserve
     * @param pt       the pt
     *
     * @return the float[]
     */
    float[] asFloatArray(boolean preserve, int[] pt);

    /**
     * Returns this variable's values for the specified range of records as
     * float[].If variable type cannot be cast to float, a Throwable is thrown.
     *
     * @param pt the pt
     *
     * @return the float[]
     */
    float[] asFloatArray(int[] pt);

    /**
     * Returns this variable's values as int[] for variables of type
     * int, short or unsigned short, byte or unsigned byte. for
     * variables of other types.
     *
     * @return the int[]
     */
    int[] asIntArray();

    /**
     * Returns this variable's values for the specified range of records as
     * int[] for variables of type int, short or unsigned short, byte or
     * unsigned byte, or unsigned int (only if preserve is false).
     * for variables of other types.
     *
     * @param preserve the preserve
     * @param pt       the pt
     *
     * @return the int[]
     */
    int[] asIntArray(boolean preserve, int[] pt);

    /**
     * Returns this variable's values for the specified range of records as
     * int[] for variables of type int, short or unsigned short, byte or
     * unsigned byte. for variables of other types.
     *
     * @param pt the pt
     *
     * @return the int[]
     */
    int[] asIntArray(int[] pt);

    /**
     * Returns this variable's values as long[] for variables of type long.throws
     * Throwable for variables of other types.
     *
     * @return the long[]
     */
    long[] asLongArray();

    /**
     * Returns this variable's values for the specified range of records as
     * long[] for variables of type long. for variables of other
     * types.
     *
     * @param pt the pt
     *
     * @return the long[]
     */
    long[] asLongArray(int[] pt);

    /**
     * Returns this variable's values as short[] for variables of type
     * short, byte or unsigned byte
     * for variables of other types.
     *
     * @return the short[]
     */
    short[] asShortArray();

    /**
     * Returns this variable's values for the specified range of records as
     * short[] for variables of type short, byte or unsigned byte, or
     * unsigned short (only if preserve is false). for variables of
     * other types.
     *
     * @param preserve the preserve
     * @param pt       the pt
     *
     * @return the short[]
     */
    short[] asShortArray(boolean preserve, int[] pt);

    /**
     * Returns this variable's values for the specified range of records as
     * short[] for variables of type short, byte or unsigned byte.
     * for variables of other types.
     *
     * @param pt the pt
     *
     * @return the short[]
     */
    short[] asShortArray(int[] pt);

    /**
     * Returns byte {@link VDataContainer.CByte DataContainer} for a
     * range of points.
     *
     * @param pt the pt
     *
     * @return the byte container
     */
    VDataContainer.CByte getByteContainer(int[] pt);

    /**
     * Returns {@link CDFImpl CDFImpl} object containing this variable.
     *
     * @return the cdf
     */
    CDFImpl getCDF();

    /**
     * Returns double {@link VDataContainer.CDouble DataContainer} for a
     * range of points using native ByteOrder, optionally accepting possible
     * loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     *
     * @return the double container
     */
    VDataContainer.CDouble getDoubleContainer(int[] pt, boolean preserve);

    /**
     * Returns double {@link VDataContainer.CDouble DataContainer} for a
     * range of points, optionally accepting possible loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     *
     * @return the double container
     */
    VDataContainer.CDouble getDoubleContainer(int[] pt, boolean preserve, ByteOrder bo);

    /**
     * Returns float {@link VDataContainer.CFloat DataContainer} for a
     * range of points using native ByteOrder, optionally accepting possible
     * loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     *
     * @return the float container
     */
    VDataContainer.CFloat getFloatContainer(int[] pt, boolean preserve);

    /**
     * Returns float {@link VDataContainer.CFloat DataContainer} for a
     * range of points, optionally accepting possible loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     *
     * @return the float container
     */
    VDataContainer.CFloat getFloatContainer(int[] pt, boolean preserve, ByteOrder bo);

    /**
     * Returns int {@link VDataContainer.CInt DataContainer} for a
     * range of points using native ByteOrder, optionally accepting possible
     * loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     *
     * @return the int container
     */
    VDataContainer.CInt getIntContainer(int[] pt, boolean preserve);

    /**
     * Returns int {@link VDataContainer.CInt DataContainer} for a
     * range of points, optionally accepting possible loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     *
     * @return the int container
     */
    VDataContainer.CInt getIntContainer(int[] pt, boolean preserve, ByteOrder bo);

    /**
     * Returns short {@link VDataContainer.CLong DataContainer} for a
     * range of points using native ByteOrder.
     *
     * @param pt the pt
     *
     * @return the long container
     */
    VDataContainer.CLong getLongContainer(int[] pt);

    /**
     * Returns long {@link VDataContainer.CLong DataContainer} for a
     * range of points.
     *
     * @param pt the pt
     * @param bo the bo
     *
     * @return the long container
     */
    VDataContainer.CLong getLongContainer(int[] pt, ByteOrder bo);

    /**
     * Returns short {@link VDataContainer.CShort DataContainer} for a
     * range of points using native ByteOrder, optionally accepting possible
     * loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     *
     * @return the short container
     */
    VDataContainer.CShort getShortContainer(int[] pt, boolean preserve);

    /**
     * Returns short {@link VDataContainer.CShort DataContainer} for a
     * range of points, optionally accepting possible loss of precision.
     *
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     *
     * @return the short container
     */
    VDataContainer.CShort getShortContainer(int[] pt, boolean preserve, ByteOrder bo);

    /**
     * Returns String {@link VDataContainer.CString DataContainer} for a
     * range of points.
     *
     * @param pt the pt
     *
     * @return the string container
     */
    VDataContainer.CString getStringContainer(int[] pt);

}
