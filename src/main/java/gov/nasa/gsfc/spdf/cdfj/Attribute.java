package gov.nasa.gsfc.spdf.cdfj;

/**
 * CDF Attribute specification.
 */
public interface Attribute {

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Checks if is global.
     *
     * @return true, if is global
     */
    boolean isGlobal();
}
