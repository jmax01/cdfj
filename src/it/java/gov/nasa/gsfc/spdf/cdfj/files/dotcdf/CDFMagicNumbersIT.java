package gov.nasa.gsfc.spdf.cdfj.files.dotcdf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gov.nasa.gsfc.spdf.cdfj.test.*;

class CDFMagicNumbersIT {

    @BeforeAll
    static void downloadNasaCdfTestFiles() {
        NasaCdfTestFiles.downloadNasaCdfTestFiles(false);
    }

    @Test
    void testReadMagicNumbersFileChannel() throws IOException {

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)

                .forEach(p -> {

                    try (FileChannel fc = FileChannel.open(p)) {

                        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
                        fc.read(byteBuffer, 0);
                        long mn = byteBuffer.getLong(0);
                        CDFMagicNumbers readMagicNumbers = CDFMagicNumbers.readMagicNumbers(fc);
                        assertEquals(mn, readMagicNumbers.getMagicNumbersAsLong());
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(" failed", e);
                    }

                });

    }

}
