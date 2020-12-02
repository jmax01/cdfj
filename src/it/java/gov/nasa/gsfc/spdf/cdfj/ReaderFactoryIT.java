package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.files.dotcdf.CDFMagicNumbers.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.*;

import gov.nasa.gsfc.spdf.cdfj.CDFException.*;
import gov.nasa.gsfc.spdf.cdfj.fields.CDFDataTypes;
import gov.nasa.gsfc.spdf.cdfj.test.*;
import lombok.*;

class ReaderFactoryIT {

    static final Logger LOGGER = LogManager.getLogger();

    @BeforeAll
    static void downloadNasaCdfTestFiles() {
        NasaCdfTestFiles.downloadNasaCdfTestFiles(false);
    }

    // @Test
    // void testGetReaderStringViaByteBuffer() throws IOException {
    //
    // Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)
    // .map(CDFMagicNumbers::from)
    // .peek(mn -> LOGGER.info("{}", mn))
    // .sorted(CDFMagicNumbers.COMP)
    // //
    // // .filter(cm -> cm.getFilePath()
    // // .toString()
    // // .contains("c1_waveform"))
    // .forEach(fname -> {
    // LOGGER.info("{}", fname);
    //
    // Path filePath = fname.getFilePath();
    //
    // try (FileChannel fc = FileChannel.open(filePath);) {
    //
    // ByteBuffer byteBuffer = ByteBuffer.allocate((int) fc.size());
    // fc.read(byteBuffer);
    // byteBuffer.position(0);
    //
    // try (CDFReader reader = ReaderFactory.getReader(byteBuffer)) {
    //
    // LOGGER.info("Variable Names {}", Arrays.toString(reader.getVariableNames()));
    // LOGGER.info("GlobalAttributeNames Names {}",
    // Arrays.toString(reader.globalAttributeNames()));
    // LOGGER.info("All Attribute Names {}", Arrays.toString(reader.allAttributeNames()));
    // }
    //
    // }
    // catch (IOException e1) {
    //
    // throw new RuntimeException(e1);
    // }
    //
    // });
    //
    // }

    @Test
    void testGetReaderString() throws IOException {

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)
                .map(CDFMagicNumbers::from)
                .peek(mn -> LOGGER.info("{}", mn))
                .sorted(CDFMagicNumbers.COMP)
                //
                // .filter(cm -> cm.getFilePath()
                // .toString()
                // .contains("c1_waveform"))
                .forEach(fname -> {
                    LOGGER.info("{}", fname);

                    try (CDFReader reader = ReaderFactory.getReader(fname.getFilePath()
                            .toString())) {

                        reader.cdfAttributesByName()
                                .values()
                                .stream()
                                .flatMap(c -> Stream.concat(c.gEntries.stream(), c.zEntries.stream()))
                                .filter(a -> CDFDataTypes.isTemporalType(a.getType()))
                                .forEach(a -> {

                                    Object value = a.getValue();

                                    if (value instanceof double[]) {
                                        double[] asDoubleArray = (double[]) value;
                                        System.out.println(a.getAttributeName() + " " + Arrays.toString(asDoubleArray));
                                    } else {
                                        System.out.println(a.getAttributeName() + " " + value + " " + value.getClass());
                                    }

                                });

                        LOGGER.info("Variable Names {}", Arrays.toString(reader.getVariableNames()));
                        LOGGER.info("GlobalAttributeNames Names {}", Arrays.toString(reader.globalAttributeNames()));
                        LOGGER.info("All Attribute Names {}", Arrays.toString(reader.allAttributeNames()));
                    }
                    catch (ReaderError | IOException e1) {

                        throw new RuntimeException(e1);
                    }

                });

    }

    @Value
    static class CDFMagicNumbers {

        static final Comparator<CDFMagicNumbers> COMP = Comparator.comparing(CDFMagicNumbers::getMagicNumber1AsHex)
                .thenComparing(CDFMagicNumbers::getMagicNumber2AsHex)
                .thenComparing(CDFMagicNumbers::getFilePath);

        Path filePath;

        String magicNumber1AsHex;

        String magicNumber2AsHex;

        static CDFMagicNumbers from(final Path filePath) {

            try (FileChannel fc = FileChannel.open(filePath)) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(8);
                fc.read(byteBuffer);
                return CDFMagicNumbers.from(filePath, byteBuffer.getInt(0), byteBuffer.getInt(4));
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }

        static CDFMagicNumbers from(final Path filePath, final int mn1, final int mn2) {
            return new CDFMagicNumbers(filePath, Integer.toHexString(mn1), Integer.toHexString(mn2));
        }

        boolean isV3() {
            return CDF_V3_MAGIC_NUMBER_1_AS_STRING.equals(this.magicNumber1AsHex);
        }
    }

}
