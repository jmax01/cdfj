package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public class DefaultPadValues {

    static Number[] padValues = new Number[50];
    static {
        padValues[1] = -127;
        padValues[2] = -32_767;
        padValues[4] = -2_147_483_647;
        padValues[8] = -9_223_372_036_854_775_807L;
        padValues[11] = 254;
        padValues[12] = 65_534;
        padValues[14] = 4_294_967_294L;
        padValues[44] = new Float(-1.0E30);
        padValues[45] = -1.0E30;
        padValues[31] = new Double(0);
        padValues[32] = new Double(0);
        padValues[33] = -9_223_372_036_854_775_807L;
    }

    /**
     *
     * @param type
     *
     * @return
     */
    public static Object value(int type) {
        return padValues[type];
    }
}
