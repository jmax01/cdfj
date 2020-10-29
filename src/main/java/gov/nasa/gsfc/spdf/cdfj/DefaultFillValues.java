package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public class DefaultFillValues {

    static Number[] fillValues = new Number[50];
    static {
        fillValues[1] = -128;
        fillValues[2] = -32_768;
        fillValues[4] = -2_147_483_648;
        fillValues[8] = -9_223_372_036_854_775_808L;
        fillValues[11] = 255;
        fillValues[12] = 65_535;
        fillValues[14] = 4_294_967_295L;
        fillValues[44] = new Float(-1.0E31);
        fillValues[45] = -1.0E31;
        fillValues[31] = -1.0E31;
        fillValues[32] = -1.0E31;
        fillValues[33] = -9_223_372_036_854_775_808L;
    }

    /**
     *
     * @param type
     *
     * @return
     */
    public static Object value(int type) {
        return fillValues[type];
    }
}
