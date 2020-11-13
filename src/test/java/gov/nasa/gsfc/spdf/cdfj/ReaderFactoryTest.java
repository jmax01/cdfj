package gov.nasa.gsfc.spdf.cdfj;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError;
import lombok.Value;

class ReaderFactoryTest {

    static final Logger LOGGER = LogManager.getLogger();

    static final String PROJECT_BUILD_DIRECTORY_PATH_AS_STRING = System.getProperty("project.build.directory",
            "target");

    static final Path CDFJ_BUILD_PATH = Path.of(PROJECT_BUILD_DIRECTORY_PATH_AS_STRING, "cdfj");

    static final File CDFJ_BUILD_DIRECTORY = createCdfjDir();

    private static File createCdfjDir() {

        try {
            return Files.createDirectories(CDFJ_BUILD_PATH)
                    .toFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    // @Test
    // void testMagicNumbers() {
    // // assertEquals(CDF2_MAGIC, CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED);
    // assertEquals(CDF3_COMPRESSED_MAGIC, CDF_V3_MAGIC_NUMBER_COMPRESSED);
    // }

    @BeforeAll
    static void downloadSamples() throws IOException {

        String cdfTestFilesUrlAsString = "https://cdaweb.gsfc.nasa.gov/pub/software/cdf/cdf_test_files/";
        Document doc = Jsoup.connect(cdfTestFilesUrlAsString)
                .get();
        LOGGER.info("doc {}", doc);

        doc.select("a[href]")
                .parallelStream()
                .map(anchor -> anchor.attr("href"))
                .filter(href -> href.endsWith(".cdf"))
                .map(cdfHref -> cdfTestFilesUrlAsString + cdfHref)
                .map(cdfFileUrlAsString -> {

                    try {
                        return new URL(cdfFileUrlAsString);
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }

                })
                .forEach(cdfFileUrl -> {

                    String filename = Paths.get(cdfFileUrl.getPath())
                            .getFileName()
                            .toString();

                    if (!Files.exists(CDFJ_BUILD_DIRECTORY.toPath()
                            .resolve(filename))) {

                        try (InputStream cdfFileUrlInputStream = cdfFileUrl.openStream();
                                ReadableByteChannel readableByteChannel = Channels.newChannel(cdfFileUrlInputStream)) {

                            Path tmp = Files.createTempFile(CDFJ_BUILD_DIRECTORY.toPath(), "~", filename + ".tmp");

                            try (FileOutputStream fileOutputStream = new FileOutputStream(tmp.toFile());
                                    FileChannel fileChannel = fileOutputStream.getChannel();) {

                                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                            }

                            Files.move(tmp, tmp.resolveSibling(filename));

                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }

                    }

                });

    }

    @Test
    void testGetReaderString() throws IOException {
        Files.list(ReaderFactoryTest.CDFJ_BUILD_PATH)
                .map(CDFMagicNumbers::from)
                .peek(mn -> LOGGER.info("{}", mn))
                .sorted(CDFMagicNumbers.COMP)
                // .filter(CDFMagicNumbers::isV3)
                .forEach(fname -> {
                    LOGGER.info("{}", fname);

                    try (CDFReader reader = ReaderFactory.getReader(fname.getFilePath()
                            .toString())) {

                        LOGGER.info("Variable Names {}", Arrays.toString(reader.getVariableNames()));
                        LOGGER.info("GlobalAttributeNames Names {}", Arrays.toString(reader.globalAttributeNames()));
                        LOGGER.info("All Attribute Names {}", Arrays.toString(reader.allAttributeNames()));
                    } catch (ReaderError | IOException e1) {

                        LOGGER.error("{}", fname, e1);
                    }

                });

    }

    @Test
    void testGetReaderStringBoolean() {
        fail("Not yet implemented");
    }

    @Test
    void testGetReaderURL() {
        fail("Not yet implemented");
    }

    @Test
    void testGetVersion() {
        fail("Not yet implemented");
    }

    @Value
    static class CDFMagicNumbers {

        static final Comparator<CDFMagicNumbers> COMP = Comparator.comparing(CDFMagicNumbers::getMagicNumber1AsHex)
                .thenComparing(CDFMagicNumbers::getMagicNumber2AsHex)
                .thenComparing(CDFMagicNumbers::getFilePath);

        Path filePath;

        String magicNumber1AsHex;

        String magicNumber2AsHex;

        static CDFMagicNumbers from(Path filePath) {

            try (FileChannel fc = FileChannel.open(filePath)) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(8);
                fc.read(byteBuffer);
                return CDFMagicNumbers.from(filePath, byteBuffer.getInt(0), byteBuffer.getInt(4));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }

        static CDFMagicNumbers from(Path filePath, int mn1, int mn2) {
            return new CDFMagicNumbers(filePath, Integer.toHexString(mn1), Integer.toHexString(mn2));
        }

        boolean isV3() {
            return CDFFactory.CDF_V3_MAGIC_NUMBER_1_AS_STRING.equals(magicNumber1AsHex);
        }
    }
}
