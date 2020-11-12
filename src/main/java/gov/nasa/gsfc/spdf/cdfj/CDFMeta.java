package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Vector;

/**
 * Interface that defines methods for getting attributes, variable
 * characteristics.
 */
public interface CDFMeta {

    /**
     * Attribute entries.
     *
     * @param attributeName the attribute name
     *
     * @return the list
     */
    List<AttributeEntry> attributeEntries(String attributeName);

    /**
     * Attribute entries.
     *
     * @param variableName  the variable name
     * @param attributeName the attribute name
     *
     * @return the list
     */
    List<AttributeEntry> attributeEntries(String variableName, String attributeName);

    /**
     * Returns values of a numeric variable whose values can be converted
     * to double without loss of precision as double[].If variable is non-numeric,
     * or of type long a Throwable is thrown.
     *
     * @param variableName the variable name
     *
     * @return the 1d
     */
    double[] get1D(String variableName);

    /**
     * Returns value of a variable as a one dimensional array.returns, as double[],
     * values of a numeric variable whose values can be
     * converted to double without loss of precision;
     * returns, as byte[], values of a string variable;
     * returns, as long[], values of a long variable for preserve = true,
     * and as double[] otherwise.
     *
     * @param variableName the variable name
     * @param preserve     the preserve
     *
     * @return the 1d
     */
    Object get1D(String variableName, boolean preserve);

    /**
     * Returns values of a string variable as byte[].If variable is numeric, a
     * Throwable is thrown.
     *
     * @param variableName the variable name
     * @param stringType   the string type
     *
     * @return the 1d
     */
    byte[] get1D(String variableName, Boolean stringType);

    /**
     * Returns value of 1 dimensional variable at the specified point.
     *
     * @param variableName the variable name
     * @param point        the point
     *
     * @return the 1d
     */
    Object get1D(String variableName, int point);

    /**
     * Returns values of 1 dimensional variable for the specified
     * range of points.
     *
     * @param variableName name of the variable
     * @param first        record number of first point of range
     * @param last         record number of first point of range
     *
     * @return the 1d
     */
    Object get1D(String variableName, int first, int last);

    /**
     * Returns value of the named global attribute.
     * <p>
     * For a character string attribute, a Vector of String is returned
     * For a numeric attribute, a long[] is returned for long type;
     * double[] is returned for all other numeric types.
     *
     * @param atr the atr
     *
     * @return the attribute
     */
    Object getAttribute(String atr);

    /**
     * Returns value of the named attribute for specified variable.
     * <p>
     * For a character string attribute, a String[] is returned
     * For a numeric attribute, a long[] is returned for long type;
     * double[] is returned for all other numeric types.
     *
     * @param vname the vname
     * @param aname the aname
     *
     * @return the attribute
     */
    Object getAttribute(String vname, String aname);

    /**
     * Gets the attribute entries.
     *
     * @param attribute the attribute
     *
     * @return the attribute entries
     *
     * @deprecated use {@link #attributeEntries(String)}
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    Vector getAttributeEntries(String attribute);

    /**
     * Gets the attribute entries.
     *
     * @param vname     the vname
     * @param attribute the attribute
     *
     * @return the attribute entries
     *
     * @deprecated use {@link #attributeEntries(String, String)}
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    Vector getAttributeEntries(String vname, String attribute);

    /**
     * Returns ByteOrder.LITTLE_ENDIAN, or ByteOrder.BIG_ENDIAN depending
     * the CDF encoding
     *
     * @return the byte order
     */
    ByteOrder getByteOrder();

    /**
     * Returns the {@link GlobalAttribute} object for the named global
     * attribute.
     *
     * @param atr the atr
     *
     * @return the global attribute
     */
    GlobalAttribute getGlobalAttribute(String atr);

    /**
     * Returns the object that implements the {@link Variable} interface for
     * the named variable.
     *
     * @param name the name
     *
     * @return the variable
     */
    VariableMetaData getVariable(String name);

    /**
     * Returns names of variables in the CDF.
     *
     * @return the variable names
     */
    String[] getVariableNames();

    /**
     * Returns names of variables of given VAR_TYPE in the CDF.
     *
     * @param type the type
     *
     * @return the variable names
     */
    String[] getVariableNames(String type);

    /**
     * Returns names of global attributes.
     *
     * @return the string[]
     */
    String[] globalAttributeNames();

    /**
     * Returns whether value of the given variable can be cast to the
     * specified type without loss of precision.
     *
     * @param vname the vname
     * @param cl    the cl
     *
     * @return true, if is compatible
     */
    boolean isCompatible(String vname, Class<?> cl);

    /**
     * Returns whether the arrays are stored in row major order in the source.
     *
     * @return true, if successful
     */
    boolean rowMajority();

    /**
     * Returns names of attributes of the given variable.
     *
     * @param variableName the variable name
     *
     * @return the string[] or null
     */
    String[] variableAttributeNames(String variableName);

    /**
     * returns names of all attributes in a String[].
     *
     * @return the string[]
     */
    String[] allAttributeNames();
}
