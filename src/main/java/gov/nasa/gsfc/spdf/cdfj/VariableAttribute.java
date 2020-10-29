package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public interface VariableAttribute extends Attribute {

    /**
     * returns nth entry for this variable attribute.if entry type is string, a
     * String or String[] is returned.if entry type is long, a long[] is returned.
     * In other cases a double[] is returned
     *
     * @param n
     *
     * @return
     */
    Object getEntry(int n);

    /**
     * returns count of entries for this variable attribute.
     *
     * @return
     */
    int getEntryCount();

    /**
     * returns whether nth entry is of type long.A Throwable is thrown for invalid
     * entry number
     *
     * @param n
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    boolean isLongType(int n) throws Throwable;

    /**
     * returns whether nth entry is of type string.A Throwable is thrown for invalid
     * entry number
     *
     * @param n
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    boolean isStringType(int n) throws Throwable;
}
