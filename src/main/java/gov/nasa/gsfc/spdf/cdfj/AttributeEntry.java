package gov.nasa.gsfc.spdf.cdfj;

/**
 * Specifes an attribute entry.
 */
public interface AttributeEntry {

    /**
     *
     * @return
     */
    String getAttributeName();

    /**
     *
     * @return
     */
    int getNumberOfElements();

    /**
     *
     * @return
     */
    int getType();

    /**
     *
     * @return
     */
    Object getValue();

    /**
     *
     * @return
     */
    int getVariableNumber();

    /**
     *
     * @return
     */
    boolean isLongType();

    /**
     *
     * @param ae
     *
     * @return
     */
    boolean isSameAs(AttributeEntry ae);

    /**
     *
     * @return
     */
    boolean isStringType();
}
