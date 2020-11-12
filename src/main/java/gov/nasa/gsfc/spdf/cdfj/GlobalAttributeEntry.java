package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Class GlobalAttributeEntry.
 *
 * @author nand
 */
public class GlobalAttributeEntry extends AEDR {

    /** The Constant GLOBAL_ATTRIBUTE_RECORD_TYPE. */
    public static final int GLOBAL_ATTRIBUTE_RECORD_TYPE = 5;

    /**
     * Instantiates a new global attribute entry.
     *
     * @param adr   the adr
     * @param type  the type
     * @param value the value
     */
    public GlobalAttributeEntry(final ADR adr, final int type, final Object value) {
        super(adr, type, value);
    }

    /**
     * Instantiates a new global attribute entry.
     *
     * @param adr   the adr
     * @param value the value
     */
    public GlobalAttributeEntry(final ADR adr, final Object value) {
        this(adr, -1, value);
    }

    @Override
    public int getAEDRAttributeType() {
        return GLOBAL_ATTRIBUTE_RECORD_TYPE;
    }
}
