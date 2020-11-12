package gov.nasa.gsfc.spdf.cdfj;

/**
 * Specifes an attribute entry.
 */
public interface AttributeEntry {

    /**
     * Gets the attribute name.
     *
     * @return the attribute name
     */
    String getAttributeName();

    /**
     * Gets the number of elements.
     *
     * @return the number of elements
     */
    int getNumberOfElements();

    /**
     * Gets the type.
     *
     * @return the type
     */
    int getType();

    /**
     * Gets the value.
     *
     * @return the value
     */
    Object getValue();

    /**
     * Gets the variable number.
     *
     * @return the variable number
     */
    int getVariableNumber();

    /**
     * Checks if is long type.
     *
     * @return true, if is long type
     */
    boolean isLongType();

    /**
     * Checks if is same as.
     *
     * @param ae the ae
     *
     * @return true, if is same as
     */
    boolean isSameAs(AttributeEntry ae);

    /**
     * Checks if is string type.
     *
     * @return true, if is string type
     */
    boolean isStringType();
}
