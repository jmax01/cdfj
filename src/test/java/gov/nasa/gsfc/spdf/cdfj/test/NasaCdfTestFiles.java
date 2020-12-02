package gov.nasa.gsfc.spdf.cdfj.test;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class NasaCdfTestFiles {

    /** The uri of the NASA CDF Test files as a String. */
    public static final String NASA_CDF_TEST_FILES_URI_AS_STRING = "https://cdaweb.gsfc.nasa.gov/pub/software/cdf/cdf_test_files/";

    /** The uri of the NASA CDF Test files. */
    public static final URI NASA_CDF_TEST_FILES_URI = URI.create(NASA_CDF_TEST_FILES_URI_AS_STRING);

    public static final URL NASA_CDF_TEST_FILES_URL = toUrl(NASA_CDF_TEST_FILES_URI);

    public static final Path NASA_TEST_CDF_FILES_DIRECTORY_PATH = MavenTestSupport.CDFJ_BUILD_DIRECTORY_PATH
            .resolve("nasa");

    static URL toUrl(URI uri) {

        try {
            return NASA_CDF_TEST_FILES_URI.toURL();
        }
        catch (MalformedURLException e) {
            throw new UncheckedIOException("toUrl failed for " + uri, e);
        }

    }

    public static List<Path> downloadNasaCdfTestFiles(boolean overwrite) {

        synchronized (NASA_TEST_CDF_FILES_DIRECTORY_PATH) {

            return MavenTestSupport.downloadCdfFiles(NASA_CDF_TEST_FILES_URI_AS_STRING,
                    NASA_TEST_CDF_FILES_DIRECTORY_PATH, overwrite);
        }

    }
}
