package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

/**
 * Interface that defines methods for getting properties of
 * a CDF variable.
 */
public interface VariableMetaData {

    /**
     *
     * @return
     */
    int getBlockingFactor();

    /**
     * Returns ByteBuffer containing uncompressed values converted to
     * a stream of numbers of the type specified by 'type' using the
     * specified byte ordering (specified by bo) for the specified range
     * of records.Original ordering of values (row majority) is
     * preserved.recordRange[0] specifies the first record, and recordRange[1] the
     * last
     * record.If 'preserve' is true, a Throwable is thrown if the conversion
     * to specified type will result in loss of precision.If 'preserve' is
     * false, compatible conversions will be made even if it results in loss
     * of precision.
     *
     * @param type
     * @param bo
     * @param recordRange
     * @param preserve
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    ByteBuffer getBuffer(Class type, int[] recordRange, boolean preserve, ByteOrder bo) throws Throwable;

    /**
     * Gets an array of VariableDataBuffer objects that provide location of
     * data for this variable if this variable is not compressed.This method
     * throws a Throwable if invoked for a compressed variable.getBuffer method of
     * VariableDataBuffer object returns a read only
     * ByteBuffer that contains data for this variable for a range of
     * records. getFirstRecord() and getLastRecord() define the
     * range of records.
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    VariableDataBuffer[] getDataBuffers() throws Throwable;

    /**
     *
     * @param raw
     *
     * @return
     *
     * @throws Throwable
     */
    VariableDataBuffer[] getDataBuffers(boolean raw) throws Throwable;

    /**
     * Gets the size of an item (defined as number of bytes needed to
     * represent the value of this variable at a point).
     *
     * @return
     */
    int getDataItemSize();

    /**
     * Gets the dimensions.
     *
     * @return
     */
    int[] getDimensions();

    /**
     * Returns effective dimensions
     *
     * @return
     */
    int[] getEffectiveDimensions();

    /**
     * Returns effective rank of this variable.Dimensions for which dimVarys is
     * false do not count.
     *
     * @return
     */
    int getEffectiveRank();

    /**
     * Return element count for this variable's dimensions.
     *
     * @return
     */
    Vector getElementCount();

    /**
     * Gets a list of regions that contain data for the variable.Each element of the
     * vector describes a region as an int[3] array.
     * Array elements are: record number of first point
     * in the region, record number of last point in the
     * region, and offset of the start of region.
     *
     * @return
     */
    VariableDataLocator getLocator();

    /**
     * Gets the name of this of this variable
     *
     * @return
     */
    String getName();

    /**
     * Gets the sequence number of the variable inside the CDF.
     *
     * @return
     */
    int getNumber();

    /**
     * Gets the number of elements (of type returned by getType()).
     *
     * @return
     */
    int getNumberOfElements();

    /**
     * Gets the number of values (size of time series)
     *
     * @return
     */
    int getNumberOfValues();

    /**
     * Gets an object that represents a padded instance.For variable of type
     * 'string', a String is returned;
     * For numeric data, a double[] is returned.
     * If the variable type is
     * long, a loss of precision may occur.
     *
     * @return
     */
    Object getPadValue();

    /**
     * Gets an object that represents a padded instance for a variable of
     * numeric type.A double[] is returned, unless the variable type is long and
     * preservePrecision is set to true;
     *
     * @param preservePrecision
     *
     * @return
     */
    Object getPadValue(boolean preservePrecision);

    /**
     * Returns record range for this variable
     *
     * @return
     */
    int[] getRecordRange();

    /**
     * Gets the type of values of the variable.Supported types are defined in the
     * CDF Internal Format Description
     *
     * @return
     */
    int getType();

    /**
     * Gets the dimensional variance.This determines the effective
     * dimensionality of values of the variable.
     *
     * @return
     */
    boolean[] getVarys();

    /**
     * returns whether conversion of this variable to type specified by
     * cl is supported while preserving precision.equivalent to isCompatible(Class
     * cl, true)
     *
     * @param cl
     *
     * @return
     */
    boolean isCompatible(Class cl);

    /**
     * returns whether conversion of this variable to type specified by
     * cl is supported under the given precision preserving constraint.
     *
     * @param cl
     * @param preserve
     *
     * @return
     */
    boolean isCompatible(Class cl, boolean preserve);

    /**
     * Determines whether the value of this variable is represented as
     * a compressed byte sequence in the CDF.
     *
     * @return
     */
    boolean isCompressed();

    /**
     * Shows whether one or more records (in the range returned by
     * getRecordRange()) are missing.
     *
     * @return
     */
    boolean isMissingRecords();

    /**
     *
     * @return
     */
    boolean isTypeR();

    /**
     * Return whether the missing record should be assigned the pad
     * value.
     *
     * @return
     */
    boolean missingRecordValueIsPad();

    /**
     * Return whether the missing record should be assigned the last
     * seen value.If none has been seen, pad value is assigned.
     *
     * @return
     */
    boolean missingRecordValueIsPrevious();

    /**
     * Determines whether the value of this variable is the same at
     * all time points.returns true if value may change, false otherwise
     *
     * @return
     */
    boolean recordVariance();

    /**
     * Determines whether the value of this variable is presented in
     * a row-major order in the CDF.
     *
     * @return
     */
    boolean rowMajority();
}
