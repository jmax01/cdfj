package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Class DefaultPadValues.
 *
 * @author nand
 */
// FIXME: Not used internally
public final class DefaultPadValues {

    static final Number[] padValues = new Number[50];
    static {
        padValues[1] = -127;
        padValues[2] = -32_767;
        padValues[4] = -2_147_483_647;
        padValues[8] = -9_223_372_036_854_775_807L;
        padValues[11] = 254;
        padValues[12] = 65_534;
        padValues[14] = 4_294_967_294L;
        padValues[44] = -1.0E30F;
        padValues[45] = -1.0E30D;
        padValues[31] = 0.0D;
        padValues[32] = 0.0D;
        padValues[33] = -9_223_372_036_854_775_807L;
    }

    private DefaultPadValues() {
    }

    /**
     * Value.
     *
     * @param type the type
     *
     * @return the object
     */
    public static Object value(final int type) {
        return padValues[type];
    }
}
