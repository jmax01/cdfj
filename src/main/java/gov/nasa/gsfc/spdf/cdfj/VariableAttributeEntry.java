package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Class VariableAttributeEntry.
 *
 * @author nand
 */
public class VariableAttributeEntry extends AEDR {

    /** The Constant VARIABLE_ATTRIBUTE_RECORD_TYPE. */
    public static final int VARIABLE_ATTRIBUTE_RECORD_TYPE = 9;

    /**
     * Instantiates a new variable attribute entry.
     *
     * @param adr   the adr
     * @param type  the type
     * @param value the value
     */
    public VariableAttributeEntry(final ADR adr, final int type, final Object value) {
        super(adr, type, value);
    }

    /**
     * Instantiates a new variable attribute entry.
     *
     * @param adr   the adr
     * @param value the value
     */
    public VariableAttributeEntry(final ADR adr, final Object value) {
        this(adr, -1, value);
    }

    @Override
    public int getAEDRAttributeType() {
        return VARIABLE_ATTRIBUTE_RECORD_TYPE;
    }
}
