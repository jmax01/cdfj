package gov.nasa.gsfc.spdf.cdfj;

/**
 * Specifies the selection and options for the aggregated CDF.
 */
public interface SelectedVariableCollection {

    /**
     * Add a variable to the output with specified compression and
     * default specification for {@link SparseRecordOption SparseRecordOption},
     * (PAD).
     *
     * @param name        the name
     * @param compression the compression
     */
    void add(String name, boolean compression);

    /**
     * Add a variable to the output with specified compression and
     * specified setting for {@link SparseRecordOption SparseRecordOption}.
     *
     * @param name        the name
     * @param compression the compression
     * @param opt         the opt
     */
    void add(String name, boolean compression, SparseRecordOption opt);

    /**
     * Returns a list of variable selected.
     *
     * @return the names
     */
    String[] getNames();

    /**
     * Returns {@link SparseRecordOption SparseRecordOption} chosen for
     * the given variable.
     *
     * @param name the name
     *
     * @return the sparse record option
     */
    SparseRecordOption getSparseRecordOption(String name);

    /**
     * Returns whather a given variable is in the list of variable selected.
     *
     * @param name the name
     *
     * @return true, if successful
     */
    boolean hasVariable(String name);

    /**
     * Returns whether compression was chosen for the variable.
     *
     * @param name the name
     *
     * @return true, if is compressed
     */
    boolean isCompressed(String name);
}
