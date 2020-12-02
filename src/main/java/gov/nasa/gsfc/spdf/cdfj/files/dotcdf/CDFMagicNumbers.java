package gov.nasa.gsfc.spdf.cdfj.files.dotcdf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import lombok.Value;
import lombok.experimental.NonFinal;

public interface CDFMagicNumbers {

    public static final int CDF_COMPRESSED_MAGIC_NUMBER_2 = 0xCCCC_0001;

    public static final int CDF_UNCOMPRESSED_MAGIC_NUMBER_2 = 0x0000_ffff;

    public static final int CDF_V3_MAGIC_NUMBER_1 = 0xCDF3_0001;

    public static final String CDF_V3_MAGIC_NUMBER_1_AS_STRING = Integer.toHexString(CDF_V3_MAGIC_NUMBER_1);

    public static final int CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2 = 0x0000_ffff;

    public static final String CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2_AS_STRING = Integer
            .toHexString(CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2);

    public static final String CDF_V3_COMPRESSED_MAGIC_NUMBER_2_AS_STRING = Integer
            .toHexString(CDF_COMPRESSED_MAGIC_NUMBER_2);

    public static final long CDF_V3_MAGIC_NUMBER_UNCOMPRESSED = (((long) CDF_V3_MAGIC_NUMBER_1) << 32)
            + CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2;

    public static final String CDF_V3_MAGIC_NUMBER_UNCOMPRESSED_AS_STRING = Long
            .toHexString(CDF_V3_MAGIC_NUMBER_UNCOMPRESSED);

    public static final long CDF_V3_MAGIC_NUMBER_COMPRESSED = (((long) CDF_V3_MAGIC_NUMBER_1) << 32)
            | (CDF_COMPRESSED_MAGIC_NUMBER_2 & 0xffffffffL);

    public static final String CDF_V3_MAGIC_NUMBER_COMPRESSED_AS_STRING = Long
            .toHexString(CDF_V3_MAGIC_NUMBER_COMPRESSED);

    public static final int CDF_V2_6_V2_7_MAGIC_NUMBER_1 = 0xCDF26002;

    public static final long CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED = (((long) CDF_V2_6_V2_7_MAGIC_NUMBER_1) << 32)
            + CDF_UNCOMPRESSED_MAGIC_NUMBER_2;

    public static final long CDF_V2_6_V2_7_MAGIC_NUMBER_COMPRESSED = (((long) CDF_V2_6_V2_7_MAGIC_NUMBER_1) << 32)
            | (CDF_COMPRESSED_MAGIC_NUMBER_2 & 0xffffffffL);

    public static final int CDF_V2_5_MAGIC_NUMBER_1 = 0x0000_ffff;

    public static final int CDF_V2_5_MAGIC_NUMBER_2 = 0x0000_ffff;

    public static final long CDF_V2_5_MAGIC_NUMBER = (((long) CDF_V2_5_MAGIC_NUMBER_1) << 32) + CDF_V2_5_MAGIC_NUMBER_2;

    public static final CDFMagicNumbersV2_5 CDF_MAGIC_NUMBERS_V2_5 = new CDFMagicNumbersV2_5();

    public static final CDFMagicNumbersV2_6_V2_7Compressed CDF_MAGIC_NUMBERS_V2_6_V2_7_COMPRESSED = new CDFMagicNumbersV2_6_V2_7Compressed();

    public static final CDFMagicNumbersV2_6_V2_7Uncompressed CDF_MAGIC_NUMBERS_V2_6_V2_7_UNCOMPRESSED = new CDFMagicNumbersV2_6_V2_7Uncompressed();

    public static final CDFMagicNumbersV3Compressed CDF_MAGIC_NUMBERS_V3_COMPRESSED = new CDFMagicNumbersV3Compressed();

    public static final CDFMagicNumbersV3Uncompressed CDF_MAGIC_NUMBERS_V3_UNCOMPRESSED = new CDFMagicNumbersV3Uncompressed();

    /**
     * Read magic numbers as long.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * 
     * @return the long
     */
    static CDFMagicNumbers readMagicNumbers(final FileChannel dotCDFFileChannel) {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            dotCDFFileChannel.read(buffer, 0);
            return readMagicNumbers(buffer);
        }
        catch (IOException e) {
            throw new UncheckedIOException("readMagicNumbersAsLong failed", e);
        }

    }

    static CDFMagicNumbers readMagicNumbers(ByteBuffer source) {

        int magicNumber1 = source.getInt(0);
        int magicNumber2 = source.getInt(4);

        return buildMagicNumbers(magicNumber1, magicNumber2);

    }

    static CDFMagicNumbers buildMagicNumbers(int magicNumber1, int magicNumber2) {

        switch (magicNumber1) {

            case CDF_V2_5_MAGIC_NUMBER_1: {

                if (CDF_UNCOMPRESSED_MAGIC_NUMBER_2 == magicNumber2) {
                    return CDF_MAGIC_NUMBERS_V2_5;
                }

                throw new IllegalArgumentException("readMagicNumbers failed for magic numbers "
                        + Integer.toHexString(magicNumber1) + " " + Integer.toHexString(magicNumber2));
            }
            case CDF_V2_6_V2_7_MAGIC_NUMBER_1: {

                switch (magicNumber2) {
                    case CDF_UNCOMPRESSED_MAGIC_NUMBER_2: {
                        return CDF_MAGIC_NUMBERS_V2_6_V2_7_UNCOMPRESSED;
                    }
                    case CDF_COMPRESSED_MAGIC_NUMBER_2: {
                        return CDF_MAGIC_NUMBERS_V2_6_V2_7_COMPRESSED;
                    }
                    default:
                        throw new IllegalArgumentException("readMagicNumbers failed for magic numbers "
                                + Integer.toHexString(magicNumber1) + " " + Integer.toHexString(magicNumber2));
                }

            }
            case CDF_V3_MAGIC_NUMBER_1: {

                switch (magicNumber2) {
                    case CDF_UNCOMPRESSED_MAGIC_NUMBER_2: {
                        return CDF_MAGIC_NUMBERS_V3_UNCOMPRESSED;
                    }
                    case CDF_COMPRESSED_MAGIC_NUMBER_2: {
                        return CDF_MAGIC_NUMBERS_V3_COMPRESSED;
                    }
                    default:
                        throw new IllegalArgumentException("readMagicNumbers failed for magic numbers "
                                + Integer.toHexString(magicNumber1) + " " + Integer.toHexString(magicNumber2));

                }

            }
            default:
                throw new IllegalArgumentException("readMagicNumbers failed for magic numbers "
                        + Integer.toHexString(magicNumber1) + " " + Integer.toHexString(magicNumber2));
        }

    }

    int getMagicNumber1();

    default String getMagicNumber1AsHexString() {
        return Integer.toHexString(getMagicNumber1());
    }

    int getMagicNumber2();

    default String getMagicNumber2AsHexString() {
        return Integer.toHexString(getMagicNumber2());
    }

    long getMagicNumbersAsLong();

    default String getMagicNumbersAsHexString() {
        return Long.toHexString(getMagicNumbersAsLong());
    }

    default boolean isCompressed() {
        return CDF_COMPRESSED_MAGIC_NUMBER_2 == getMagicNumber2();
    }

    default boolean isNotCompressed() {
        return CDF_UNCOMPRESSED_MAGIC_NUMBER_2 == getMagicNumber2();
    }

    default boolean isV2_6_V2_7() {
        return CDF_V2_6_V2_7_MAGIC_NUMBER_1 == getMagicNumber1();
    }

    default boolean isNotV2_6_V2_7() {
        return CDF_V2_6_V2_7_MAGIC_NUMBER_1 != getMagicNumber1();
    }

    default boolean isV25() {
        return CDF_V2_5_MAGIC_NUMBER_1 == getMagicNumber1();
    }

    default boolean isNotV25() {
        return CDF_V2_5_MAGIC_NUMBER_1 != getMagicNumber1();
    }

    default boolean isV3() {
        return CDF_V3_MAGIC_NUMBER_1 == getMagicNumber1();
    }

    default boolean isNotV3() {
        return CDF_V3_MAGIC_NUMBER_1 != getMagicNumber1();
    }

    public interface IsCompressedMagicNumber extends CDFMagicNumbers {}

    public interface IsUncompressedMagicNumber extends CDFMagicNumbers {}

    @Value
    @NonFinal
    static abstract class AbstractCDFMagicNumbers implements CDFMagicNumbers {

        final int magicNumber1;

        final int magicNumber2;

    }

    @Value
    public static class CDFMagicNumbersV2_5 extends AbstractCDFMagicNumbers {

        public CDFMagicNumbersV2_5() {
            super(CDF_V2_5_MAGIC_NUMBER_1, CDF_V2_5_MAGIC_NUMBER_2);
        }

        @Override
        public long getMagicNumbersAsLong() {
            return CDF_V2_5_MAGIC_NUMBER;
        }
    }

    public interface CDFMagicNumbersV2_6_V2_7 extends CDFMagicNumbers {}

    public interface CDFMagicNumbersV3 extends CDFMagicNumbers {}

    @Value
    public static class CDFMagicNumbersV2_6_V2_7Compressed extends AbstractCDFMagicNumbers
            implements CDFMagicNumbersV2_6_V2_7 {

        public CDFMagicNumbersV2_6_V2_7Compressed() {
            super(CDF_V2_6_V2_7_MAGIC_NUMBER_1, CDF_COMPRESSED_MAGIC_NUMBER_2);
        }

        @Override
        public long getMagicNumbersAsLong() {
            return CDF_V2_6_V2_7_MAGIC_NUMBER_COMPRESSED;
        }
    }

    @Value
    public static class CDFMagicNumbersV2_6_V2_7Uncompressed extends AbstractCDFMagicNumbers
            implements CDFMagicNumbersV2_6_V2_7 {

        public CDFMagicNumbersV2_6_V2_7Uncompressed() {
            super(CDF_V2_6_V2_7_MAGIC_NUMBER_1, CDF_UNCOMPRESSED_MAGIC_NUMBER_2);
        }

        @Override
        public long getMagicNumbersAsLong() {
            return CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED;
        }
    }

    @Value
    public static class CDFMagicNumbersV3Compressed extends AbstractCDFMagicNumbers implements CDFMagicNumbersV3 {

        public CDFMagicNumbersV3Compressed() {
            super(CDF_V3_MAGIC_NUMBER_1, CDF_COMPRESSED_MAGIC_NUMBER_2);
        }

        @Override
        public long getMagicNumbersAsLong() {
            return CDF_V3_MAGIC_NUMBER_COMPRESSED;
        }
    }

    @Value
    public static class CDFMagicNumbersV3Uncompressed extends AbstractCDFMagicNumbers implements CDFMagicNumbersV3 {

        public CDFMagicNumbersV3Uncompressed() {
            super(CDF_V3_MAGIC_NUMBER_1, CDF_UNCOMPRESSED_MAGIC_NUMBER_2);
        }

        @Override
        public long getMagicNumbersAsLong() {
            return CDF_V3_MAGIC_NUMBER_UNCOMPRESSED;
        }
    }
}