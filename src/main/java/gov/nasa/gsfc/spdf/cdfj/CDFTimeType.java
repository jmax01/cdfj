package gov.nasa.gsfc.spdf.cdfj;

import java.util.Hashtable;

/**
 * CDF Time types
 */
public final class CDFTimeType {

    /**
     * EPOCH Type
     */
    public static final CDFTimeType EPOCH = new CDFTimeType(31);

    /**
     * EPOCH16 Type
     */
    public static final CDFTimeType EPOCH16 = new CDFTimeType(32);

    /**
     * TT2000 Type
     */
    public static final CDFTimeType TT2000 = new CDFTimeType(33);

    static Hashtable<String, CDFTimeType> ht = new Hashtable<>();
    static {
        ht.put("epoch", EPOCH);
        ht.put("epoch16", EPOCH16);
        ht.put("tt2000", TT2000);
    }

    int _type;

    private CDFTimeType(int _type) {
        this._type = _type;
    }

    /**
     * Returns CDFTimeType of the named time type.
     *
     * @param s
     *
     * @return
     */
    public static CDFTimeType getType(String s) {
        return ht.get(s.toLowerCase());
    }

    /**
     *
     * @return
     */
    public int getValue() {
        return this._type;
    }
}
