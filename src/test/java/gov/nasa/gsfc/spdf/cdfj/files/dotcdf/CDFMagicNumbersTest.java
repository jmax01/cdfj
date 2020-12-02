package gov.nasa.gsfc.spdf.cdfj.files.dotcdf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

class CDFMagicNumbersTest {

    static Path createFile(long magicnumber) throws IOException {

        Path tmpFilePath = Files.createTempFile("~", "cdf.tmp");

        try (FileChannel fc = FileChannel.open(tmpFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putLong(0, magicnumber);
            fc.write(bb);
        }

        return tmpFilePath;
    }

    static void assertMagicNumber(long magicnumber, boolean isCompressed, boolean isV3, boolean isV2_6_V2_7,
            boolean isV2_5) throws IOException {

        Path filePath = createFile(magicnumber);

        int magicNumber1 = (int) (magicnumber >> 32);
        int magicNumber2 = (int) magicnumber;

        try (FileChannel dotCDFFileChannel = FileChannel.open(filePath)) {
            CDFMagicNumbers magicNumbers = CDFMagicNumbers.readMagicNumbers(dotCDFFileChannel);
            assertEquals(isCompressed, magicNumbers.isCompressed());
            assertEquals(!isCompressed, magicNumbers.isNotCompressed());
            assertEquals(isV3, magicNumbers.isV3());
            assertEquals(!isV3, magicNumbers.isNotV3());
            assertEquals(isV2_6_V2_7, magicNumbers.isV2_6_V2_7());
            assertEquals(!isV2_6_V2_7, magicNumbers.isNotV2_6_V2_7());
            assertEquals(isV2_5, magicNumbers.isV25());
            assertEquals(!isV2_5, magicNumbers.isNotV25());
            assertEquals(magicnumber, magicNumbers.getMagicNumbersAsLong());
            assertEquals(Long.toHexString(magicnumber), magicNumbers.getMagicNumbersAsHexString());
            assertEquals(magicNumber1, magicNumbers.getMagicNumber1());
            assertEquals(magicNumber2, magicNumbers.getMagicNumber2());
            assertEquals(Integer.toHexString(magicNumber1), magicNumbers.getMagicNumber1AsHexString());
            assertEquals(Integer.toHexString(magicNumber2), magicNumbers.getMagicNumber2AsHexString());

        }

    }

    @Test
    void testReadMagicNumbersFileChannel() throws IOException {

        assertMagicNumber(CDFMagicNumbers.CDF_V2_5_MAGIC_NUMBER, false, false, false, true);
        assertMagicNumber(CDFMagicNumbers.CDF_V2_6_V2_7_MAGIC_NUMBER_COMPRESSED, true, false, true, false);
        assertMagicNumber(CDFMagicNumbers.CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED, false, false, true, false);
        assertMagicNumber(CDFMagicNumbers.CDF_V3_MAGIC_NUMBER_COMPRESSED, true, true, false, false);
        assertMagicNumber(CDFMagicNumbers.CDF_V3_MAGIC_NUMBER_UNCOMPRESSED, false, true, false, false);
    }

    @Test
    void testBuildMagicNumbersFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            CDFMagicNumbers.buildMagicNumbers(0, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            CDFMagicNumbers.buildMagicNumbers(CDFMagicNumbers.CDF_V2_5_MAGIC_NUMBER_1, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            CDFMagicNumbers.buildMagicNumbers(CDFMagicNumbers.CDF_V2_6_V2_7_MAGIC_NUMBER_1, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            CDFMagicNumbers.buildMagicNumbers(CDFMagicNumbers.CDF_V3_MAGIC_NUMBER_1, 0);
        });
    }
}
