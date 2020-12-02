package gov.nasa.gsfc.spdf.cdfj.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public final class MavenTestSupport {

    /** The Maven project.build.directory usually target or target-ide as a string */
    public static final String PROJECT_BUILD_DIRECTORY_PATH_AS_STRING = System.getProperty("project.build.directory",
            "target");

    /** The Maven project.build.directory usually target or target-ide as a path */
    public static final Path PROJECT_BUILD_DIRECTORY_PATH = Paths.get(PROJECT_BUILD_DIRECTORY_PATH_AS_STRING);

    /** The Constant project.build.directory for cdfj test output ${project.build.directory}/cdfj */
    public static final Path CDFJ_BUILD_DIRECTORY_PATH = PROJECT_BUILD_DIRECTORY_PATH.resolveSibling("cdfj");

    /**
     * Retrieves cdf file urls from site.
     *
     * @param url the url from which to retrieve the cdf file urls
     * 
     * @return the list of cdf file urls
     */
    public static List<URL> retrieveCdfFileUrlsFromSite(String url) {

        try {
            Document doc = Jsoup.connect(url)
                    .get();

            List<URL> urls = doc.select("a[href]")
                    .parallelStream()
                    .map(anchor -> anchor.attr("href"))
                    .filter(href -> href.endsWith(".cdf"))
                    .map(cdfHref -> url + cdfHref)
                    .map(cdfFileUrlAsString -> {

                        try {
                            return new URL(cdfFileUrlAsString);
                        }
                        catch (MalformedURLException e) {
                            throw new UncheckedIOException("retrieveCdfFileUrlsFromSite failed for url: "
                                    + cdfFileUrlAsString + " from " + url, e);

                        }

                    })
                    .collect(Collectors.toList());
            return Collections.unmodifiableList(urls);
        }
        catch (IOException e) {
            throw new UncheckedIOException("retrieveCdfFileUrlsFromSite failed for url: " + url, e);
        }

    }

    /**
     * Downloads cdf files from the supplied url to the target directory.
     *
     * @param url             the url from which to retrieve the cdf file urls
     * @param targetDirectory the target directory to save the files
     * @param overwrite       if true existing files will be overwritten
     * 
     * @return the list
     */
    public static List<Path> downloadCdfFiles(String url, Path targetDirectory, boolean overwrite) {

        return Collections.unmodifiableList(retrieveCdfFileUrlsFromSite(url).stream()
                .map(cdfFileUrl -> downloadCdfFile(cdfFileUrl, targetDirectory, overwrite))
                .collect(Collectors.toList()));

    }

    /**
     * Download a cdf file.
     *
     * @param cdfFileUrl the cdf file url
     * @param filename   the filename
     * 
     * @return the path
     */
    public static Path downloadCdfFile(URL cdfFileUrl, Path targetDirectory, boolean overwrite) {

        Path filenamePath = Paths.get(cdfFileUrl.getPath())
                .getFileName();

        Path targetCdfFilePath = targetDirectory.resolve(filenamePath);

        if (overwrite || !Files.exists(targetCdfFilePath)) {

            try {
                Files.createDirectories(targetDirectory);

            }
            catch (IOException e) {
                LOGGER.error("Failed to create cdf target directory {}", targetDirectory, e);
                throw new UncheckedIOException(e);
            }

            try (InputStream cdfFileUrlInputStream = cdfFileUrl.openStream();
                    ReadableByteChannel readableByteChannel = Channels.newChannel(cdfFileUrlInputStream)) {

                String cdfFilenameAsString = targetCdfFilePath.getFileName()
                        .toString();

                Path tmp = Files.createTempFile(targetCdfFilePath.getParent(), "~", cdfFilenameAsString + ".tmp");

                try (FileOutputStream fileOutputStream = new FileOutputStream(tmp.toFile());
                        FileChannel fileChannel = fileOutputStream.getChannel();) {

                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                }

                return Files.move(tmp, tmp.resolveSibling(cdfFilenameAsString));

            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }

        return targetCdfFilePath;

    }
}
