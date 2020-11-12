package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Interface OutputOption.
 *
 * @author nand
 */
public interface OutputOption {

    /**
     * Adds the.
     *
     * @param name        the name
     * @param compression the compression
     */
    void add(String name, boolean compression);

    /**
     * Gets the names.
     *
     * @return the names
     */
    String[] getNames();

    /**
     * Checks for variable.
     *
     * @param name the name
     *
     * @return true, if successful
     */
    boolean hasVariable(String name);

    /**
     * Checks if is compressed.
     *
     * @param name the name
     *
     * @return true, if is compressed
     */
    boolean isCompressed(String name);

    /**
     * Sets the row majority.
     *
     * @param rowMajority the new row majority
     */
    void setRowMajority(boolean rowMajority);
}
