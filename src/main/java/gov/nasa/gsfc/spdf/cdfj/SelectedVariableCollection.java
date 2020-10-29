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
     * @param name
     * @param compression
     */
    void add(String name, boolean compression);

    /**
     * Add a variable to the output with specified compression and
     * specified setting for {@link SparseRecordOption SparseRecordOption}.
     *
     * @param name
     * @param opt
     * @param compression
     */
    void add(String name, boolean compression, SparseRecordOption opt);

    /**
     * Returns a list of variable selected.
     *
     * @return
     */
    String[] getNames();

    /**
     * Returns {@link SparseRecordOption SparseRecordOption} chosen for
     * the given variable.
     *
     * @param name
     *
     * @return
     */
    SparseRecordOption getSparseRecordOption(String name);

    /**
     * Returns whather a given variable is in the list of variable selected.
     *
     * @param name
     *
     * @return
     */
    boolean hasVariable(String name);

    /**
     * Returns whether compression was chosen for the variable.
     *
     * @param name
     *
     * @return
     */
    boolean isCompressed(String name);
}
