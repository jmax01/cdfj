package gov.nasa.gsfc.spdf.cdfj;

import java.util.HashMap;
import java.util.Map;

/**
 * Sparse Record Option Definition class.
 */
public final class SparseRecordOption {

    /**
     * NONE option - CDF is not missing any records.
     */
    public static final SparseRecordOption NONE = new SparseRecordOption(0);

    /**
     * PADDED option - Pad value is returned for a missing record.
     */
    public static final SparseRecordOption PADDED = new SparseRecordOption(1);

    /**
     * PREVIOUS option - Previous value is returned for a missing record.
     * If no previous value, then a pad value is returned.
     */
    public static final SparseRecordOption PREVIOUS = new SparseRecordOption(2);

    static final Map<String, SparseRecordOption> SPARSE_RECORD_OPTIONS_BY_NAME = sparseRecordOptionsByName();

    private static Map<String, SparseRecordOption> sparseRecordOptionsByName() {
        Map<String, SparseRecordOption> sparseRecordOptionsByName = new HashMap<>();
        sparseRecordOptionsByName.put("none", NONE);
        sparseRecordOptionsByName.put("padded", PADDED);
        sparseRecordOptionsByName.put("previous", PREVIOUS);
        return sparseRecordOptionsByName;
    }

    final int option;

    private SparseRecordOption(final int option) {
        this.option = option;
    }

    /**
     * Returns SparseRecordOption object for the named option.
     *
     * @param s the s
     *
     * @return the option
     */
    public static SparseRecordOption getOption(final String s) {
        return SPARSE_RECORD_OPTIONS_BY_NAME.get(s.toLowerCase());
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return this.option;
    }
}
