package gov.nasa.gsfc.spdf.cdfj;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * The Class CDFLogging.
 */
public final class CDFLogging {

    private CDFLogging() {

    }

    /**
     * New configured logger.
     *
     * @param clazz the clazz, may not be null
     *
     * @return the logger
     */
    public static Logger newLogger(final Class<?> clazz) {

        // logger.addHandler(H);

        return Logger.getLogger(Objects.requireNonNull(clazz, "clazz must not be null")
                .getCanonicalName());
    }

}
