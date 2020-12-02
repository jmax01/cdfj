package gov.nasa.gsfc.spdf.cdfj;

/**
 * Interface that defines methods for getting attributes, variable
 * characteristics, and data from a generic CDF.
 */
public interface CDFCore extends CDFMeta {

    /**
     * Record size field java type.
     *
     * @return the class<? extends number>
     */
    Class<? extends Number> recordSizeFieldJavaType();

    /**
     * Offset field java type.
     *
     * @return the class<? extends number>
     */
    Class<? extends Number> offsetFieldJavaType();

    /**
     * Record size field size.
     *
     * @return the int
     */
    int recordSizeFieldSize();

    /**
     * Offset field size.
     *
     * @return the int
     */
    int offsetFieldSize();

    /**
     * Returns a byte[] containing value of the given variable for the
     * specified range of points.
     * <p>
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the byte array
     */
    byte[] getByteArray(String variableName, int[] pt);

    /**
     * Returns a double[] containing value of the given variable for the
     * specified range of records.
     * <p>
     * same as getDoubleArray(variableName, pt, true).
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the double array
     */
    double[] getDoubleArray(String variableName, int[] pt);

    /**
     * Returns a double[] containing value of the given variable for the
     * specified range of records, optionally accepting loss of precision.
     * <p>
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the double array
     */
    double[] getDoubleArray(String variableName, int[] pt, boolean preserve);

    /**
     * Returns a float[] containing value of the given variable for the
     * specified range of records.
     * <p>
     * same as getFloatArray(variableName, pt, true).
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the float array
     */
    float[] getFloatArray(String variableName, int[] pt);

    /**
     * Returns a float[] containing value of the given variable for the
     * specified range of records, optionally accepting loss of precision.
     * <p>
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the float array
     */
    float[] getFloatArray(String variableName, int[] pt, boolean preserve);

    /**
     * returns a int[] containing value of the given variable for the
     * specified range of records.
     * <p>
     * same as getIntArray(variableName, pt, true).
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the int array
     */
    int[] getIntArray(String variableName, int[] pt);

    /**
     * returns a int[] containing value of the given variable for the
     * specified range of records, optionally accepting loss of precision.
     * <p>
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the int array
     */
    int[] getIntArray(String variableName, int[] pt, boolean preserve);

    /**
     * returns a long[] containing value of the given variable for the
     * specified range of records.
     * <p>
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the long array
     */
    long[] getLongArray(String variableName, int[] pt);

    /**
     * Gets the one D.
     *
     * @param variableName the variable name
     * @param columnMajor  the column major
     *
     * @return the one D
     * @
     */
    Object getOneD(String variableName, boolean columnMajor);

    /**
     * Gets the range one D.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param columnMajor  the column major
     *
     * @return the range one D
     * @
     */
    Object getRangeOneD(String variableName, int first, int last, boolean columnMajor);

    /**
     * returns a short[] containing value of the given variable for the
     * specified range of records.
     * <p>
     * same as getShortArray(variableName, pt, true).
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the short array
     */
    short[] getShortArray(String variableName, int[] pt);

    /**
     * returns a short[] containing value of the given variable for the
     * specified range of records, optionally accepting loss of precision.
     * <p>
     * If pt is null, all available records are returned.
     * If pt.length is 1, only the pt[0] record is returned.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the short array
     */
    short[] getShortArray(String variableName, int[] pt, boolean preserve);

    /**
     * Gets the source.
     *
     * @return the source
     */
    CDFFactory.CDFSource getSource();

    /**
     * Gets the variable.
     *
     * @param variableName the variable name
     *
     * @return the variable
     */
    @Override
    Variable getVariable(String variableName);

    /**
     * Max name field size.
     *
     * @return the int
     */
    int maxNameFieldSize();

    String readNameField(long offset);

}
