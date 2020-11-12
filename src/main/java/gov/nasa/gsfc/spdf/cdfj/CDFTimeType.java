package gov.nasa.gsfc.spdf.cdfj;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CDF Time types.
 */
public final class CDFTimeType {

    /** The Constant CDF_EPOCH_TIME_TYPE_NAME. */
    public static final String CDF_EPOCH_TIME_TYPE_NAME = "epoch";

    /** The Constant CDF_EPOCH16_TIME_TYPE_NAME. */
    public static final String CDF_EPOCH16_TIME_TYPE_NAME = "epoch16";

    /** The Constant CDF_TT2000_TIME_TYPE_NAME. */
    public static final String CDF_TT2000_TIME_TYPE_NAME = "tt2000";

    /** EPOCH Type. */
    public static final CDFTimeType EPOCH = new CDFTimeType(31);

    /** EPOCH16 Type. */
    public static final CDFTimeType EPOCH16 = new CDFTimeType(32);

    /** TT2000 Type. */
    public static final CDFTimeType TT2000 = new CDFTimeType(33);

    static final Map<String, CDFTimeType> CDF_TIME_TYPES_BY_NAME = Collections.unmodifiableMap(new HashMap<>());

    int _type;

    private CDFTimeType(final int _type) {
        this._type = _type;
    }

    /**
     * Gets the type.
     *
     * @param name the name
     *
     * @return the type
     */
    public static CDFTimeType getType(final String name) {
        return CDF_TIME_TYPES_BY_NAME.get(name.toLowerCase());
    }

    static Map<String, CDFTimeType> initCDFTimeTypesByName() {
        Map<String, CDFTimeType> cdfTimeTypesByName = new HashMap<>();
        cdfTimeTypesByName.put(CDF_EPOCH_TIME_TYPE_NAME, EPOCH);
        cdfTimeTypesByName.put(CDF_EPOCH16_TIME_TYPE_NAME, EPOCH16);
        cdfTimeTypesByName.put(CDF_TT2000_TIME_TYPE_NAME, TT2000);
        return cdfTimeTypesByName;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return this._type;
    }
}
