package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Interface that defines methods for getting properties of
 * a CDF variable.
 */
public interface VariableMetaData {

    /**
     * Gets the blocking factor.
     *
     * @return the blocking factor
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
     * @param type        the type
     * @param recordRange the record range
     * @param preserve    the preserve
     * @param bo          the bo
     *
     * @return the buffer
     */
    ByteBuffer getBuffer(Class<?> type, int[] recordRange, boolean preserve, ByteOrder bo);

    /**
     * Gets an array of VariableDataBuffer objects that provide location of
     * data for this variable if this variable is not compressed.This method
     * throws a Throwable if invoked for a compressed variable.getBuffer method of
     * VariableDataBuffer object returns a read only
     * ByteBuffer that contains data for this variable for a range of
     * records. getFirstRecord() and getLastRecord() define the
     * range of records.
     *
     * @return the data buffers
     */
    VariableDataBuffer[] getDataBuffers();

    /**
     * Gets the data buffers.
     *
     * @param raw the raw
     *
     * @return the data buffers
     * @
     */
    VariableDataBuffer[] getDataBuffers(boolean raw);

    /**
     * Gets the size of an item (defined as number of bytes needed to
     * represent the value of this variable at a point).
     *
     * @return the data item size
     */
    int getDataItemSize();

    /**
     * Return element count for this variable's dimensions.
     *
     * @return the dimension element counts as an unmodifiable list
     */
    default List<Integer> getDimensionElementCounts() {

        int[] dimensions = getDimensions();

        List<Integer> dimensionElementCounts = new ArrayList<>();

        for (int i = 0; i < dimensions.length; i++) {

            if (getVarys()[i]) {
                dimensionElementCounts.add(dimensions[i]);
            }

        }

        return Collections.unmodifiableList(dimensionElementCounts);
    }

    /**
     * Gets the dimensions.
     *
     * @return the dimensions
     */
    int[] getDimensions();

    /**
     * Returns effective dimensions.
     *
     * @return the effective dimensions
     */
    int[] getEffectiveDimensions();

    /**
     * Returns effective rank of this variable.Dimensions for which dimVarys is
     * false do not count.
     *
     * @return the effective rank
     */
    int getEffectiveRank();

    /**
     * Return element count for this variable's dimensions.
     *
     * @return the element count
     *
     * @deprecated Use {@link #getElements()}
     */
    @Deprecated
    Vector<Integer> getElementCount();

    /**
     * Gets a list of regions that contain data for the variable.Each element of the
     * CopyOnWriteArrayList describes a region as an int[3] array.
     * Array elements are: record number of first point
     * in the region, record number of last point in the
     * region, and offset of the start of region.
     *
     * @return the locator
     */
    VariableDataLocator getLocator();

    /**
     * Gets the name of this of this variable.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the sequence number of the variable inside the CDF.
     *
     * @return the number
     */
    int getNumber();

    /**
     * Gets the number of elements (of type returned by getType()).
     *
     * @return the number of elements
     */
    int getNumberOfElements();

    /**
     * Gets the number of values (size of time series).
     *
     * @return the number of values
     */
    int getNumberOfValues();

    /**
     * Gets an object that represents a padded instance.For variable of type
     * 'string', a String is returned;
     * For numeric data, a double[] is returned.
     * If the variable type is
     * long, a loss of precision may occur.
     *
     * @return the pad value
     */
    Object getPadValue();

    /**
     * Gets an object that represents a padded instance for a variable of
     * numeric type.A double[] is returned, unless the variable type is long and
     * preservePrecision is set to true;
     *
     * @param preservePrecision the preserve precision
     *
     * @return the pad value
     */
    Object getPadValue(boolean preservePrecision);

    /**
     * Returns record range for this variable.
     *
     * @return the record range
     */
    int[] getRecordRange();

    /**
     * Gets the type of values of the variable.Supported types are defined in the
     * CDF Internal Format Description
     *
     * @return the type
     */
    int getType();

    /**
     * Gets the dimensional variance.This determines the effective
     * dimensionality of values of the variable.
     *
     * @return the varys
     */
    boolean[] getVarys();

    /**
     * returns whether conversion of this variable to type specified by
     * cl is supported while preserving precision.equivalent to isCompatible(Class
     * cl, true)
     *
     * @param cl the cl
     *
     * @return true, if is compatible
     */
    boolean isCompatible(Class<?> cl);

    /**
     * returns whether conversion of this variable to type specified by
     * cl is supported under the given precision preserving constraint.
     *
     * @param cl       the cl
     * @param preserve the preserve
     *
     * @return true, if is compatible
     */
    boolean isCompatible(Class<?> cl, boolean preserve);

    /**
     * Determines whether the value of this variable is represented as
     * a compressed byte sequence in the CDF.
     *
     * @return true, if is compressed
     */
    boolean isCompressed();

    /**
     * Shows whether one or more records (in the range returned by
     * getRecordRange()) are missing.
     *
     * @return true, if is missing records
     */
    boolean isMissingRecords();

    /**
     * Checks if is type R.
     *
     * @return true, if is type R
     */
    boolean isTypeR();

    /**
     * Return whether the missing record should be assigned the pad
     * value.
     *
     * @return true, if successful
     */
    boolean missingRecordValueIsPad();

    /**
     * Return whether the missing record should be assigned the last
     * seen value.If none has been seen, pad value is assigned.
     *
     * @return true, if successful
     */
    boolean missingRecordValueIsPrevious();

    /**
     * Determines whether the value of this variable is the same at
     * all time points.returns true if value may change, false otherwise
     *
     * @return true, if successful
     */
    boolean recordVariance();

    /**
     * Determines whether the value of this variable is presented in
     * a row-major order in the CDF.
     *
     * @return true, if successful
     */
    boolean rowMajority();
}
