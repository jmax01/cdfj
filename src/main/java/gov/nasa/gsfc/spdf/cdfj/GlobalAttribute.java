package gov.nasa.gsfc.spdf.cdfj;

/**
 * Global Attribute specification.
 */
public interface GlobalAttribute extends Attribute {

    /**
     * returns nth entry for this global attribute.if entry type is string, a String
     * is returned.if entry type is long, a long[] is returned.
     * In other cases a double[] is returned
     *
     * @param n
     *
     * @return
     */
    Object getEntry(int n);

    /**
     * returns count of entries for this global attribute.
     *
     * @return
     */
    int getEntryCount();

    /**
     *
     * @return
     */
    int getNum();

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
