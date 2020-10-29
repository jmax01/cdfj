package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public interface OutputOption {

    /**
     *
     * @param name
     * @param compression
     */
    void add(String name, boolean compression);

    /**
     *
     * @return
     */
    String[] getNames();

    /**
     *
     * @param name
     *
     * @return
     */
    boolean hasVariable(String name);

    /**
     *
     * @param name
     *
     * @return
     */
    boolean isCompressed(String name);

    /**
     *
     * @param rowMajority
     */
    void setRowMajority(boolean rowMajority);
}
