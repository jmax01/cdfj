package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteOrder;
import java.util.Vector;

/**
 * Interface that defines methods for getting attributes, variable
 * characteristics
 */
public interface CDFMeta {

    /**
     * Returns values of a numeric variable whose values can be converted
     * to double without loss of precision as double[].If variable is non-numeric,
     * or of type long a Throwable is thrown.
     *
     * @param varName
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    double[] get1D(String varName) throws Throwable;

    /**
     * Returns value of a variable as a one dimensional array.returns, as double[],
     * values of a numeric variable whose values can be
     * converted to double without loss of precision;
     * returns, as byte[], values of a string variable;
     * returns, as long[], values of a long variable for preserve = true,
     * and as double[] otherwise.
     *
     * @param varName
     * @param preserve
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    Object get1D(String varName, boolean preserve) throws Throwable;

    /**
     * Returns values of a string variable as byte[].If variable is numeric, a
     * Throwable is thrown.
     *
     * @param varName
     * @param stringType
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    byte[] get1D(String varName, Boolean stringType) throws Throwable;

    /**
     * Returns value of 1 dimensional variable at the specified point.
     *
     * @param varName
     * @param point
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    Object get1D(String varName, int point) throws Throwable;

    /**
     * Returns values of 1 dimensional variable for the specified
     * range of points.
     *
     * @param varName name of the variable
     * @param first   record number of first point of range
     * @param last    record number of first point of range
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    Object get1D(String varName, int first, int last) throws Throwable;

    /**
     * Returns value of the named global attribute.
     * <p>
     * For a character string attribute, a Vector of String is returned
     * For a numeric attribute, a long[] is returned for long type;
     * double[] is returned for all other numeric types.
     *
     * @param atr
     *
     * @return
     */
    Object getAttribute(String atr);

    /**
     * Returns value of the named attribute for specified variable.
     * <p>
     * For a character string attribute, a String[] is returned
     * For a numeric attribute, a long[] is returned for long type;
     * double[] is returned for all other numeric types.
     *
     * @param vname
     * @param aname
     *
     * @return
     */
    Object getAttribute(String vname, String aname);

    /**
     *
     * @param attribute
     *
     * @return
     *
     * @throws Throwable
     */
    Vector getAttributeEntries(String attribute) throws Throwable;

    /**
     *
     * @param vname
     * @param attribute
     *
     * @return
     */
    Vector getAttributeEntries(String vname, String attribute);

    /**
     * Returns ByteOrder.LITTLE_ENDIAN, or ByteOrder.BIG_ENDIAN depending
     * the CDF encoding
     *
     * @return
     */
    ByteOrder getByteOrder();

    /**
     * Returns the {@link GlobalAttribute} object for the named global
     * attribute.
     *
     * @param atr
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    GlobalAttribute getGlobalAttribute(String atr) throws Throwable;

    /**
     * Returns the object that implements the {@link Variable} interface for
     * the named variable
     *
     * @param name
     *
     * @return
     */
    VariableMetaData getVariable(String name);

    /**
     * Returns names of variables in the CDF
     *
     * @return
     */
    String[] getVariableNames();

    /**
     * Returns names of variables of given VAR_TYPE in the CDF
     *
     * @param type
     *
     * @return
     */
    String[] getVariableNames(String type);

    /**
     * Returns names of global attributes.
     *
     * @return
     */
    String[] globalAttributeNames();

    /**
     * Returns whether value of the given variable can be cast to the
     * specified type without loss of precision
     *
     * @param vname
     * @param cl
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    boolean isCompatible(String vname, Class cl) throws Throwable;

    /**
     * Returns whether the arrays are stored in row major order in the source
     *
     * @return
     */
    boolean rowMajority();

    /**
     * Returns names of attributes of the given variable.
     *
     * @param name
     *
     * @return
     */
    String[] variableAttributeNames(String name);
}
