package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Interface VariableAttribute.
 *
 * @author nand
 */
public interface VariableAttribute extends Attribute {

    /**
     * returns nth entry for this variable attribute.if entry type is string, a
     * String or String[] is returned.if entry type is long, a long[] is returned.
     * In other cases a double[] is returned
     *
     * @param n the n
     *
     * @return the entry
     */
    Object getEntry(int n);

    /**
     * returns count of entries for this variable attribute.
     *
     * @return the entry count
     */
    int getEntryCount();

    /**
     * returns whether nth entry is of type long.A Throwable is thrown for invalid
     * entry number
     *
     * @param n the n
     *
     * @return true, if is long type
     */
    boolean isLongType(int n);

    /**
     * returns whether nth entry is of type string.A Throwable is thrown for invalid
     * entry number
     *
     * @param n the n
     *
     * @return true, if is string type
     */
    boolean isStringType(int n);
}
