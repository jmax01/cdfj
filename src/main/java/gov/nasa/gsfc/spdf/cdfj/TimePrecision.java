package gov.nasa.gsfc.spdf.cdfj;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class TimePrecision.
 */
public final class TimePrecision {

    /** The Constant MILLISECOND. */
    public static final TimePrecision MILLISECOND = new TimePrecision(0);

    /** The Constant MICROSECOND. */
    public static final TimePrecision MICROSECOND = new TimePrecision(1);

    /** The Constant NANOSECOND. */
    public static final TimePrecision NANOSECOND = new TimePrecision(2);

    /** The Constant PICOSECOND. */
    public static final TimePrecision PICOSECOND = new TimePrecision(3);

    static final Map<String, TimePrecision> TIME_PRECISIONS_BY_NAME = timePrecisionsByName();

    private static Map<String, TimePrecision> timePrecisionsByName() {
        Map<String, TimePrecision> _timePrecisionsByName = new HashMap<>();
        _timePrecisionsByName.put("millisecond", MILLISECOND);
        _timePrecisionsByName.put("microsecond", MICROSECOND);
        _timePrecisionsByName.put("nanosecond", NANOSECOND);
        _timePrecisionsByName.put("picosecond", PICOSECOND);
        return _timePrecisionsByName;
    }

    static final int TIME_PRECISION_SHORT_NAME_MIN_LENGTH = 3;

    int precision;

    private TimePrecision(final int precision) {
        this.precision = precision;
    }

    /**
     * Gets the precision by logn or short name.
     *
     * @param timePrecisionName the time precision name
     *
     * @return the precision or null
     */
    public static TimePrecision getPrecision(final String timePrecisionName) {

        int len = timePrecisionName.length();

        if (len < TIME_PRECISION_SHORT_NAME_MIN_LENGTH) {
            return null;
        }

        return TIME_PRECISIONS_BY_NAME.entrySet()
                .stream()
                .filter(e -> e.getKey()
                        .startsWith(timePrecisionName))
                .map(Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return this.precision;
    }
}
