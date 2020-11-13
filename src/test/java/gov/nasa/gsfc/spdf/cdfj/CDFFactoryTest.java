package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.CDFFactory.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import gov.nasa.gsfc.spdf.cdfj.CDFFactoryTest.ADR.ADRBuilder;
import gov.nasa.gsfc.spdf.cdfj.CDFFactoryTest.CDFDescriptorRecordV3.CDFDescriptorRecordV3Builder;
import gov.nasa.gsfc.spdf.cdfj.CDFFactoryTest.GDR.GDRBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;

class CDFFactoryTest {

    static final Logger LOGGER = LogManager.getLogger();

    @Test
    void testCompressed() throws IOException {

        String fileName = "target/test-classes/bigcdf_compressed.cdf";

        Path filePath = Path.of(fileName);

        try (FileChannel fc = FileChannel.open(filePath)) {

            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            fc.read(byteBuffer);
            byteBuffer.position(0);

            int first4Bytes = byteBuffer.getInt();
            int second4Bytes = byteBuffer.getInt();

            LOGGER.info("{}", Integer.toHexString(first4Bytes));
            LOGGER.info("{}", Integer.toHexString(second4Bytes));
            //
            // ByteBuffer byteBuffer2 = ByteBuffer.allocate(12);
            // fc.read(byteBuffer2);
            // byteBuffer2.position(0);
            // LOGGER.info("{}", byteBuffer2.getLong());
            // LOGGER.info("{}", byteBuffer2.getInt());
            // LOGGER.info("{}", CDFFactory.CDF3_MAGIC);

        }

    }

    @Test
    void test() throws IOException {

        String fileName = "target/test-classes/mms1_fpi_fast_sitl_20150801132440_v0.0.0.cdf";

        Path filePath = Path.of(fileName);

        try (FileChannel fc = FileChannel.open(filePath)) {

            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            fc.read(byteBuffer);
            byteBuffer.position(0);

            int first4Bytes = byteBuffer.getInt();
            assertEquals(CDF_V3_MAGIC_NUMBER_1, first4Bytes);
            int second4Bytes = byteBuffer.getInt();
            assertEquals(CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2, second4Bytes);

            LOGGER.info("{}", Integer.toHexString(first4Bytes));
            LOGGER.info("{}", Integer.toHexString(second4Bytes));
            //
            // ByteBuffer byteBuffer2 = ByteBuffer.allocate(12);
            // fc.read(byteBuffer2);
            // byteBuffer2.position(0);
            // LOGGER.info("{}", byteBuffer2.getLong());
            // LOGGER.info("{}", byteBuffer2.getInt());
            // LOGGER.info("{}", CDFFactory.CDF3_MAGIC);

        }

    }

    @Value
    static class CDFMagicNumbers {

        static final Comparator<CDFMagicNumbers> COMP = Comparator.comparing(CDFMagicNumbers::getMagicNumber1AsHex)
                .thenComparing(CDFMagicNumbers::getMagicNUmber2AsHex)
                .thenComparing(CDFMagicNumbers::getFilePath);

        Path filePath;

        String magicNumber1AsHex;

        String magicNUmber2AsHex;

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
    }

    @Test
    void allFiles() {

        try {
            Files.list(ReaderFactoryTest.CDFJ_BUILD_PATH)
                    .map(CDFMagicNumbers::from)
                    .sorted(CDFMagicNumbers.COMP)
                    .forEach(cm -> LOGGER.info("Filename: {} Magic Number 1: {} Magic Number 2: {}", cm.getFilePath()
                            .getFileName(), cm.getMagicNumber1AsHex(), cm.getMagicNUmber2AsHex()));

        } catch (IOException e) {
            throw new UncheckedIOException(e);

        }

    }

    @Test
    void test2() throws IOException {
        // String fileName = "target/test-classes/tha_l2_scm_20160831_v01.cdf";
        String fileName = ReaderFactoryTest.CDFJ_BUILD_PATH.resolve("c1_waveform_wbd_200202080940_v01_subset.cdf")
                .toString();
        Path filePath = Path.of(fileName);

        try (FileChannel fc = FileChannel.open(filePath)) {

            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            fc.read(byteBuffer);
            byteBuffer.position(0);

            long first8Bytes = byteBuffer.getLong();
            LOGGER.info("{}", Long.toHexString(first8Bytes));
            assertEquals(CDF_V3_MAGIC_NUMBER_UNCOMPRESSED, first8Bytes);
            assertEquals(CDF_V3_MAGIC_NUMBER_UNCOMPRESSED, CDFFactory.CDF3_MAGIC);

            ByteBuffer cdfrsdrbb = ByteBuffer.allocate(8);
            fc.read(cdfrsdrbb);
            long cdfrsbb = cdfrsdrbb.getLong(0);
            LOGGER.info("cdfrsbb {}", cdfrsbb);
            CDFDescriptorRecordV3Builder builder = CDFDescriptorRecordV3.builder()
                    .recordSize(cdfrsbb);

            ByteBuffer cdfdrbb = ByteBuffer.allocate(((int) cdfrsbb) - 8);

            fc.read(cdfdrbb);
            cdfdrbb.position(0);

            builder.recordType(cdfdrbb.getInt())
                    .gdrOffset(cdfdrbb.getLong())
                    .version(cdfdrbb.getInt())
                    .release(cdfdrbb.getInt())
                    .encoding(cdfdrbb.getInt())
                    .flags(cdfdrbb.getInt())
                    .rfuA(cdfdrbb.getInt())
                    .rfuB(cdfdrbb.getInt())
                    .increment(cdfdrbb.getInt())
                    .identifier(cdfdrbb.getInt())
                    .rfuE(cdfdrbb.getInt());
            //
            // builder.recordType(cdfdrbb.getInt(8))
            // .gdrOffset(cdfdrbb.getLong(12))
            // .version(cdfdrbb.getInt(20))
            // .release(cdfdrbb.getInt(24))
            // .encoding(cdfdrbb.getInt(28))
            // .flags(cdfdrbb.getInt(32))
            // .rfuA(cdfdrbb.getInt(36))
            // .rfuB(cdfdrbb.getInt(40))
            // .increment(cdfdrbb.getInt(44))
            // .identifier(cdfdrbb.getInt(48))
            // .rfuE(cdfdrbb.getInt(52));

            byte[] copyright = new byte[256];
            cdfdrbb.get(copyright);

            CDFDescriptorRecordV3 cdr = builder.copyright(new String(copyright, StandardCharsets.US_ASCII))
                    .build();

            LOGGER.info("{}", cdr);

            LOGGER.info("{}", fc.position());

            ByteBuffer gdrRsBB = ByteBuffer.allocate(8);
            fc.read(gdrRsBB);
            gdrRsBB.position(0);
            LOGGER.info("fcp {}", fc.position());

            long gdrLength = gdrRsBB.getLong(0);

            LOGGER.info("gdrLength {}", gdrLength);

            ByteBuffer gdrBB = ByteBuffer.allocate(((int) gdrLength) - 8);
            fc.read(gdrBB);
            gdrBB.position(0);
            LOGGER.info("fcp {}", fc.position());

            GDRBuilder gdrBuilder = GDR.builder()
                    .recordSize(gdrLength);

            gdrBuilder.recordType(gdrBB.getInt())
                    .rVDRHead(gdrBB.getLong())
                    .zVDRHead(gdrBB.getLong())
                    .adrHead(gdrBB.getLong())
                    .eof(gdrBB.getLong())
                    .nrVars(gdrBB.getInt())
                    .numAttr(gdrBB.getInt())
                    .rMaxRec(gdrBB.getInt());

            int rNumDims = gdrBB.getInt();
            gdrBuilder.rNumDims(rNumDims)
                    .nzVars(gdrBB.getInt())
                    .uirHead(gdrBB.getLong())
                    .rfuC(gdrBB.getInt())
                    .leapSecondLastUpdated(gdrBB.getInt())
                    .rfuE(gdrBB.getInt());

            for (int i = 0; i < rNumDims; i++) {
                gdrBuilder.rDimSize(gdrBB.getInt());
            }

            GDR gdr = gdrBuilder.build();
            LOGGER.info("{}", gdr);
            LOGGER.info("fcp {}", fc.position());

            List<ADR> adrs = readAdrs(fc, gdr);

            LOGGER.info("{}", adrs);
            // int second4Bytes = byteBuffer.getInt();
            // assertEquals(CDFFactory.CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2, second4Bytes);

            // LOGGER.info("{}", Integer.toHexString(second4Bytes));
            //
            // ByteBuffer byteBuffer2 = ByteBuffer.allocate(12);
            // fc.read(byteBuffer2);
            // byteBuffer2.position(0);
            // LOGGER.info("{}", byteBuffer2.getLong());
            // LOGGER.info("{}", byteBuffer2.getInt());
            // LOGGER.info("{}", CDFFactory.CDF3_MAGIC);

        }

    }

    static List<ADR> readAdrs(FileChannel fc, GDR gdr) throws IOException {

        long nextAdr = gdr.getAdrHead();

        List<ADR> adrs = new ArrayList<>();

        while (nextAdr > 0) {

            ADR adr = readAdr(fc, nextAdr);
            nextAdr = adr.getAdrNext();
            adrs.add(adr);
        }

        return adrs;
    }

    static ADR readAdr(FileChannel fc, long adrHead) throws IOException {

        fc.position(adrHead);

        ByteBuffer adrRsBB = ByteBuffer.allocate(8);
        fc.read(adrRsBB);
        adrRsBB.position(0);
        long adrRecordSize = adrRsBB.getLong();

        ADRBuilder adrBuilder = ADR.builder()
                .recordSize(adrRecordSize);
        fc.position(adrHead);

        ByteBuffer adrBB = ByteBuffer.allocate((int) adrRecordSize);
        fc.read(adrBB);
        adrBB.position(0);

        adrBuilder.recordType(adrBB.getInt(ADR.ADR_RECORD_TYPE_OFFSET))
                .adrNext(adrBB.getLong(ADR.ADR_ADR_NEXT_FIELD_OFFSET))
                .agrEdrhead(adrBB.getLong(ADR.ADR_AGR_EDR_HEAD_FIELD_OFFSET))
                .scope(adrBB.getInt(ADR.ADR_SCOPE_FIELD_OFFSET))
                .num(adrBB.getInt(ADR.ADR_NUM_FIELD_OFFSET))
                .ngrEntries(adrBB.getInt(ADR.ADR_NGR_ENTRIES_FIELD_OFFSET))
                .maxGrEntry(adrBB.getInt(ADR.ADR_MAX_GR_ENTRY_FIELD_OFFSET))
                .rfuA(adrBB.getInt(ADR.ADR_RFUA_FIELD_OFFSET))
                .azEdrHead(adrBB.getLong(ADR.ADR_AZ_EDR_HEAD_FIELD_OFFSET))
                .nzEntries(adrBB.getInt(ADR.ADR_NZ_ENTRIES_FIELD_OFFSET))
                .maxZEntry(adrBB.getInt(ADR.ADR_MAX_Z_ENTRIES_FIELD_OFFSET))
                .rfuE(adrBB.getInt(ADR.ADR_RFUE_FIELD_OFFSET));

        byte[] adrName = new byte[256];
        adrBB.get(ADR.ADR_NUM_FIELD_OFFSET, adrName);
        ADR adr = adrBuilder.name(new String(adrName, StandardCharsets.US_ASCII))
                .build();
        return adr;
    }

    interface CDFRecord<RECORD_SIZE extends Number, OFFSET_SIZE extends Number> {

        RECORD_SIZE getRecordSize();

        int getRecordType();

        Class<OFFSET_SIZE> offsetSize();
    }

    interface CDFDecriptorRecord<RECORD_SIZE extends Number, OFFSET_SIZE extends Number> {

        int RECORD_SIZE_FIELD_SIZE = 8;

        int RECORD_TYPE_FIELD_SIZE = 4;

        int GDR_OFFSET_FIELD_SIZE = 8;

        int VERSION_FIELD_SIZE = 4;

        int RELEASE_FIELD_SIZE = 4;

        int ENCODING_FIELD_SIZE = 4;

        int FLAGS_FIELD_SIZE = 4;

        int RFU_A_FIELD_SIZE = 4;

        int RFU_B_FIELD_SIZE = 4;

        int INCREMENT_FIELD_SIZE = 4;

        int IDENTIFIER_FIELD_SIZE = 4;

        int RFU_E_FIELD_SIZE = 4;

        int COPYRIGHT_V2_4_FIELD_SIZE = 1945;

        int COPYRIGHT_V2_5_UP_FIELD_SIZE = 256;

        RECORD_SIZE getRecordSize();

        int getRecordType();

        OFFSET_SIZE getGdrOffset();

        int getVersion();

        int getRelease();

        int getEncoding();

        int getFlags();

        int getRfuA();

        int getRfuB();

        int getIncrement();

        int getIdentifier();

        int getRfuE();

        String getCopyright();
    }

    @Value
    @NonFinal
    static abstract class AbstractCDFDecriptorRecord<RECORD_SIZE extends Number, OFFSET_SIZE extends Number>
            implements CDFDecriptorRecord<RECORD_SIZE, OFFSET_SIZE> {

        final RECORD_SIZE recordSize;

        final int recordType;

        final OFFSET_SIZE gdrOffset;

        final int version;

        final int release;

        final int encoding;

        final int flags;

        final int rfuA;

        final int rfuB;

        final int increment;

        final int identifier;

        final int rfuE;

        final String copyright;
    }

    @Value
    static class CDFDescriptorRecordV3 extends AbstractCDFDecriptorRecord<Long, Long> {

        @Builder
        public CDFDescriptorRecordV3(long recordSize, int recordType, Long gdrOffset, int version, int release,
                int encoding, int flags, int rfuA, int rfuB, int increment, int identifier, int rfuE,
                String copyright) {

            super(recordSize, recordType, gdrOffset, version, release, encoding, flags, rfuA, rfuB, increment,
                    identifier, rfuE, copyright);

        }

    }

    @Value
    static class CDFDescriptorRecordV2_6V2_7 extends AbstractCDFDecriptorRecord<Integer, Integer> {

        @Builder
        public CDFDescriptorRecordV2_6V2_7(int recordSize, int recordType, int gdrOffset, int version, int release,
                int encoding, int flags, int rfuA, int rfuB, int increment, int identifier, int rfuE,
                String copyright) {

            super(recordSize, recordType, gdrOffset, version, release, encoding, flags, rfuA, rfuB, increment,
                    identifier, rfuE, copyright);

        }

    }

    @Value
    @Builder
    static class GDR {

        /** The Constant GDR_NR_VARS_DEFAULT_VALUE. */
        public static final int GDR_NR_VARS_DEFAULT_VALUE = 0;

        /**
         * The value 2, which identifies this as the GDR.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        public static final int GDR_RECORD_TYPE = CDFRecordTypes.GDR_RECORD_TYPE_INTERNAL_VALUE;

        /**
         * The Constant GDR_RECORD_SIZE_OFFSET.
         */
        public static final int GDR_RECORD_SIZE_OFFSET = 0;

        /**
         * The Constant GDR_RECORD_SIZE_FIELD_LENGTH.
         */
        public static final int GDR_RECORD_SIZE_FIELD_LENGTH = 8;

        /**
         * The Constant GDR_RECORD_TYPE_OFFSET.
         */
        public static final int GDR_RECORD_TYPE_OFFSET = GDR_RECORD_SIZE_OFFSET + GDR_RECORD_SIZE_FIELD_LENGTH;

        /**
         * The Constant GDR_RECORD_TYPE_FIELD_LENGTH.
         */
        public static final int GDR_RECORD_TYPE_FIELD_LENGTH = 4;

        /**
         * The Constant GDR_R_VDR_HEAD_OFFSET.
         */
        public static final int GDR_R_VDR_HEAD_OFFSET = GDR_RECORD_TYPE_OFFSET + GDR_RECORD_TYPE_FIELD_LENGTH;

        /**
         * The Constant GDR_R_VDR_HEAD_FIELD_LENGTH.
         */
        public static final int GDR_R_VDR_HEAD_FIELD_LENGTH = 8;

        /** The Constant GDR_R_VDR_HEAD_V2_2_EMPTY_DEFAULT. */
        public static final long GDR_R_VDR_HEAD_V2_2_EMPTY_DEFAULT = 0x0000000000000000;

        /**
         * The Constant GDR_Z_VDR_HEAD_OFFSET.
         */
        public static final int GDR_Z_VDR_HEAD_OFFSET = GDR_R_VDR_HEAD_OFFSET + GDR_R_VDR_HEAD_FIELD_LENGTH;

        /**
         * The Constant GDR_Z_VDR_HEAD_FIELD_LENGTH.
         */
        public static final int GDR_Z_VDR_HEAD_FIELD_LENGTH = 8;

        /**
         * The Constant GDR_ADR_HEAD_OFFSET.
         */
        public static final int GDR_ADR_HEAD_OFFSET = GDR_Z_VDR_HEAD_OFFSET + GDR_Z_VDR_HEAD_FIELD_LENGTH;

        /**
         * The Constant GDR_ADR_HEAD_FIELD_LENGTH.
         */
        public static final int GDR_ADR_HEAD_FIELD_LENGTH = 8;

        /**
         * The Constant GDR_EOF_OFFSET.
         */
        public static final int GDR_EOF_OFFSET = GDR_ADR_HEAD_OFFSET + GDR_ADR_HEAD_FIELD_LENGTH;

        /**
         * The Constant GDR_EOF_FIELD_LENGTH.
         */
        public static final int GDR_EOF_FIELD_LENGTH = 8;

        /**
         * The Constant GDR_NR_VARS_OFFSET.
         */
        public static final int GDR_NR_VARS_OFFSET = GDR_EOF_OFFSET + GDR_EOF_FIELD_LENGTH;

        /**
         * The Constant GDR_NR_VARS_FIELD_LENGTH.
         */
        public static final int GDR_NR_VARS_FIELD_LENGTH = 4;

        /**
         * The Constant GDR_ATTR_NUM_OFFSET.
         */
        public static final int GDR_ATTR_NUM_OFFSET = GDR_NR_VARS_OFFSET + GDR_NR_VARS_FIELD_LENGTH;

        /**
         * The Constant GDR_ATTR_NUM_FIELD_LENGTH.
         */
        public static final int GDR_ATTR_NUM_FIELD_LENGTH = 4;

        /**
         * The Constant GDR_R_MAX_REC_OFFSET.
         */
        public static final int GDR_R_MAX_REC_OFFSET = GDR_ATTR_NUM_OFFSET + GDR_ATTR_NUM_FIELD_LENGTH;

        /**
         * The Constant GDR_R_MAX_REC_FIELD_LENGTH.
         */
        public static final int GDR_R_MAX_REC_FIELD_LENGTH = 4;

        /** The Constant GDR_R_MAX_REC_NO_RECORDS_VALUE. */
        public static final int GDR_R_MAX_REC_NO_RECORDS_VALUE = -1;

        /**
         * The Constant GDR_R_NUM_DIMS_OFFSET.
         */
        public static final int GDR_R_NUM_DIMS_OFFSET = GDR_R_MAX_REC_OFFSET + GDR_R_MAX_REC_FIELD_LENGTH;

        /**
         * The Constant GDR_R_NUM_DIMS_FIELD_LENGTH.
         */
        public static final int GDR_R_NUM_DIMS_FIELD_LENGTH = 4;

        /**
         * The Constant GDR_NZ_VARS_OFFSET.
         */
        public static final int GDR_NZ_VARS_OFFSET = GDR_R_NUM_DIMS_OFFSET + GDR_R_NUM_DIMS_FIELD_LENGTH;

        /**
         * The Constant GDR_NZ_VARS_FIELD_LENGTH.
         */
        public static final int GDR_NZ_VARS_FIELD_LENGTH = 4;

        /**
         * The Constant GDR_UIR_HEAD_OFFSET.
         */
        public static final int GDR_UIR_HEAD_OFFSET = GDR_NZ_VARS_OFFSET + GDR_NZ_VARS_FIELD_LENGTH;

        /**
         * The Constant GDR_UIR_HEAD_FIELD_LENGTH.
         */
        public static final int GDR_UIR_HEAD_FIELD_LENGTH = 8;

        /**
         * The Constant GDR_RFU_C_OFFSET.
         */
        public static final int GDR_RFU_C_OFFSET = GDR_UIR_HEAD_OFFSET + GDR_UIR_HEAD_FIELD_LENGTH;

        /**
         * The Constant GDR_RFU_C_FIELD_LENGTH.
         */
        public static final int GDR_RFU_C_FIELD_LENGTH = 4;

        /**
         * The Constant GDR_RFU_C_DEFAULT.
         */
        public static final int GDR_RFU_C_DEFAULT = 0;

        /**
         * The Constant GDR_LEAP_SECOND_LAST_UPDATED_OFFSET.
         */
        public static final int GDR_LEAP_SECOND_LAST_UPDATED_OFFSET = GDR_RFU_C_OFFSET + GDR_RFU_C_FIELD_LENGTH;

        /**
         * The Constant GDR_LEAP_SECOND_LAST_UPDATED_LENGTH.
         */
        public static final int GDR_LEAP_SECOND_LAST_UPDATED_LENGTH = 4;

        /**
         * The Constant GDR_RFU_E_OFFSET.
         */
        public static final int GDR_RFU_E_OFFSET = GDR_LEAP_SECOND_LAST_UPDATED_OFFSET
                + GDR_LEAP_SECOND_LAST_UPDATED_LENGTH;

        /**
         * The Constant GDR_RFU_E_FIELD_LENGTH.
         */
        public static final int GDR_RFU_E_FIELD_LENGTH = 4;

        /** The Constant GDR_RFU_E_DEFAULT. */
        public static final int GDR_RFU_E_DEFAULT = -1;

        /** The Constant GDR_FIXED_SEGMENT_SIZE. */
        public static final int GDR_FIXED_SEGMENT_SIZE = GDR_RFU_E_OFFSET + GDR_RFU_E_FIELD_LENGTH;

        /**
         * The size in bytes of this GDR (including this field).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        long recordSize;

        @Builder.Default
        int recordType = GDR_RECORD_TYPE;

        /**
         * The file offset of the first rVariable Descriptor Record (rVDR).
         * <p>
         * The first rVDR contains a file offset to the next rVDR and so on.
         * <p>
         * An rVDR will exist for each rVariable in the CDF.
         * <p>
         * This field will contain 0x0000000000000000 if the CDF contains no rVariables.
         * <p>
         * Beginning with CDF V2.1 the last rVDR will contain a file offset of
         * 0x0000000000000000 for the file offset of the next rVDR (to indicate the end
         * of the rVDRs).
         * <p>
         * Prior to CDF V2.1 the “next VDR” file offset in the last rVDR is undefined.
         * <p>
         * rVDRs are described in Section 2.6.
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        @Builder.Default
        long rVDRHead = GDR_R_VDR_HEAD_V2_2_EMPTY_DEFAULT;

        /**
         * The file offset of the first zVariable Descriptor Record (zVDR).
         * <p>
         * The first zVDR contains a file offset to the next zVDR and so on.
         * <p>
         * A zVDR will exist for each zVariable in the CDF.
         * <p>
         * Because zVariables were not supported by CDF until CDF V2.2, prior to CDF
         * V2.2 this field is undefined.
         * <p>
         * Beginning with CDF V2.2 this field will contain either a file offset to the
         * first zVDR or 0x0000000000000000 if the CDF contains no zVariables.
         * <p>
         * The last zVDR will always contain 0x0000000000000000 for the file offset of
         * the next zVDR (to indicate the end of the zVDRs).
         * <p>
         * zVDRs are described in Section 2.6.
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        long zVDRHead;

        /**
         * The file offset of the first Attribute Descriptor Record (ADR).
         * <p>
         * The first ADR contains a file offset to the next ADR and so on.
         * <p>
         * An ADR will exist for each attribute in the CDF.
         * <p>
         * This field will contain 0x0000000000000000 if the CDF contains no attributes.
         * <p>
         * Beginning with CDF V2.1 the last ADR will contain a file offset of
         * 0x0000000000000000 for the file offset of the next ADR (to indicate the end
         * of the ADRs).
         * <p>
         * Prior to CDF V2.1 the “next ADR" file offset in the last ADR is undefined.
         * <p>
         * ADRs are described in Section 2.4.
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        long adrHead;

        /**
         * The end-of-file (EOF) position in the dotCDF file.
         * <p>
         * This is the file offset of the byte that is one beyond the last byte of the
         * last internal record.
         * <p>
         * (This value is also the total number of bytes used in the dotCDF file.) Prior
         * to CDF V2.1, this field is undefined.
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        long eof;

        /**
         * The number of rVariables in the CDF.
         * <p>
         * This will correspond to the number of rVDRs in the dotCDF file.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        @Builder.Default
        int nrVars = GDR_NR_VARS_DEFAULT_VALUE;

        /**
         * The number of attributes in the CDF.
         * <p>
         * This will correspond to the number of
         * ADRs in the dotCDF file.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        int numAttr;

        /**
         * The maximum rVariable record number in the CDF.
         * <p>
         * Note that variable record numbers are numbered beginning with zero (0).
         * <p>
         * If no rVariable records exist, this value will be negative one (-1).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        @Builder.Default
        int rMaxRec = GDR_R_MAX_REC_NO_RECORDS_VALUE;

        /**
         * The number of dimensions for rVariables.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        int rNumDims;

        /**
         * The number of zVariables in the CDF.
         * <p>
         * This will correspond to the number of zVDRs in the
         * dotCDF file.
         * <p>
         * Prior to CDF V2.2 this value will always be zero (0).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        int nzVars;

        /**
         * The file offset of the first Unused Internal Record (UIR).
         * <p>
         * The first UIR contains the file offset of the next UIR and so on.
         * <p>
         * The last UIR contains a file offset of 0x0000000000000000 for the file
         * offset of the next UIR (indicating the end of the UIRs).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        long uirHead;

        /**
         * Reserved for future use.
         * <p>
         * Always set to zero (0).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        @Builder.Default
        final int rfuC = GDR_RFU_C_DEFAULT;

        /**
         * The date of the last entry in the leap second table (in YYYYMMDD form).
         * <p>
         * It is negative one (-1) for the previous version.
         * <p>
         * A value of zero (0) is also accepted, which means a CDF was not created based
         * on a leap second table.
         * <p>
         * This field is applicable to CDFs with CDF_TIME_TT2000 data type.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        int leapSecondLastUpdated;

        /**
         * Reserved for future use.
         * <p>
         * Always set to zero (-1).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        @Builder.Default
        final int rfuE = GDR_RFU_E_DEFAULT;

        /**
         * Zero or more contiguous rVariable dimension sizes depending on the value of
         * the rNumDims field described above.
         * <p>
         * Signed 4-byte integers, big-endian byte ordering within each.
         */
        @Singular
        List<Integer> rDimSizes;

    }

    @Value
    @Builder
    static class ADR {

        /** The Constant ADR_RECORD_TYPE_ID. */
        public static final int ADR_RECORD_TYPE_ID = 4;

        /** The Constant ADR_RECORD_SIZE_FIELD_OFFSET. */
        public static final int ADR_RECORD_SIZE_FIELD_OFFSET = 0;

        /** The Constant ADR_RECORD_SIZE_FIELD_BYTE_LEN. */
        public static final int ADR_RECORD_SIZE_FIELD_LENGTH = 8;

        /** The Constant ADR_RECORD_TYPE_FIELD_OFFSET. */
        public static final int ADR_RECORD_TYPE_OFFSET = ADR_RECORD_SIZE_FIELD_OFFSET + ADR_RECORD_SIZE_FIELD_LENGTH;

        /** The Constant ADR_RECORD_TYPE_FIELD_BYTE_LEN. */
        public static final int ADR_RECORD_TYPE_FIELD_LENGTH = 4;

        /** The Constant ADR_ADR_NEXT_FIELD_OFFSET. */
        public static final int ADR_ADR_NEXT_FIELD_OFFSET = ADR_RECORD_TYPE_OFFSET + ADR_RECORD_TYPE_FIELD_LENGTH;

        /** The Constant ADR_ADR_NEXT_FIELD_LENGTH. */
        public static final int ADR_ADR_NEXT_FIELD_LENGTH = 8;

        /** The Constant ADR_AGR_EDR_HEAD_FIELD_OFFSET. */
        public static final int ADR_AGR_EDR_HEAD_FIELD_OFFSET = ADR_ADR_NEXT_FIELD_OFFSET + ADR_ADR_NEXT_FIELD_LENGTH;

        /** The Constant ADR_AGR_EDR_HEAD_FIELD_LENGTH. */
        public static final int ADR_AGR_EDR_HEAD_FIELD_LENGTH = 8;

        /** The Constant ADR_SCOPE_FIELD_OFFSET. */
        public static final int ADR_SCOPE_FIELD_OFFSET = ADR_AGR_EDR_HEAD_FIELD_OFFSET + ADR_AGR_EDR_HEAD_FIELD_LENGTH;

        /** The Constant ADR_SCOPE_FIELD_LENGTH. */
        public static final int ADR_SCOPE_FIELD_LENGTH = 4;

        /** The Constant ADR_GLOBAL_SCOPE. */
        public static final int ADR_GLOBAL_SCOPE = 1;

        /** The Constant ADR_VARIABLE_SCOPE. */
        public static final int ADR_VARIABLE_SCOPE = 2;

        /** The Constant ADR_GLOBAL_SCOPE_ASSUMED. */
        public static final int ADR_GLOBAL_SCOPE_ASSUMED = 3;

        /** The Constant ADR_VARIABLE_SCOPE_ASSUMED. */
        public static final int ADR_VARIABLE_SCOPE_ASSUMED = 4;

        /** The Constant ADR_NUM_FIELD_OFFSET. */
        public static final int ADR_NUM_FIELD_OFFSET = ADR_SCOPE_FIELD_OFFSET + ADR_SCOPE_FIELD_LENGTH;

        /** The Constant ADR_NUM_FIELD_LENGTH. */
        public static final int ADR_NUM_FIELD_LENGTH = 4;

        /** The Constant ADR_NGR_ENTRIES_FIELD_OFFSET. */
        public static final int ADR_NGR_ENTRIES_FIELD_OFFSET = ADR_NUM_FIELD_OFFSET + ADR_NUM_FIELD_LENGTH;

        /** The Constant ADR_NGR_ENTRIES_FIELD_LENGTH. */
        public static final int ADR_NGR_ENTRIES_FIELD_LENGTH = 4;

        /** The Constant ADR_MAX_GR_ENTRY_FIELD_OFFSET. */
        public static final int ADR_MAX_GR_ENTRY_FIELD_OFFSET = ADR_NGR_ENTRIES_FIELD_OFFSET
                + ADR_NGR_ENTRIES_FIELD_LENGTH;

        /** The Constant ADR_MAX_GR_ENTRY_FIELD_LENGTH. */
        public static final int ADR_MAX_GR_ENTRY_FIELD_LENGTH = 4;

        /** The Constant ADR_RFUA_FIELD_OFFSET. */
        public static final int ADR_RFUA_FIELD_OFFSET = ADR_MAX_GR_ENTRY_FIELD_OFFSET + ADR_MAX_GR_ENTRY_FIELD_LENGTH;

        /** The Constant ADR_RFUA_FIELD_LENGTH. */
        public static final int ADR_RFUA_FIELD_LENGTH = 4;

        /** The Constant ADR_AZ_EDR_HEAD_FIELD_OFFSET. */
        public static final int ADR_AZ_EDR_HEAD_FIELD_OFFSET = ADR_RFUA_FIELD_OFFSET + ADR_RFUA_FIELD_LENGTH;

        /** The Constant ADR_AZ_EDR_HEAD_FIELD_LENGTH. */
        public static final int ADR_AZ_EDR_HEAD_FIELD_LENGTH = 8;

        /** The Constant ADR_AZ_EDR_HEAD_FIELD_OFFSET. */
        public static final int ADR_NZ_ENTRIES_FIELD_OFFSET = ADR_AZ_EDR_HEAD_FIELD_OFFSET
                + ADR_AZ_EDR_HEAD_FIELD_LENGTH;

        /** The Constant ADR_AZ_EDR_HEAD_FIELD_LENGTH. */
        public static final int ADR_NZ_ENTRIES_FIELD_LENGTH = 4;

        /** The Constant ADR_MAX_Z_ENTRIES_FIELD_OFFSET. */
        public static final int ADR_MAX_Z_ENTRIES_FIELD_OFFSET = ADR_NZ_ENTRIES_FIELD_OFFSET
                + ADR_NZ_ENTRIES_FIELD_LENGTH;

        /** The Constant ADR_MAX_Z_ENTRIES_FIELD_LENGTH. */
        public static final int ADR_MAX_Z_ENTRIES_FIELD_LENGTH = 4;

        /** The Constant ADR_RFUE_FIELD_OFFSET. */
        public static final int ADR_RFUE_FIELD_OFFSET = ADR_MAX_Z_ENTRIES_FIELD_OFFSET + ADR_MAX_Z_ENTRIES_FIELD_LENGTH;

        /** The Constant ADR_RFUE_FIELD_LENGTH. */
        public static final int ADR_RFUE_FIELD_LENGTH = 4;

        /** The Constant ADR_NAME_FIELD_OFFSET. */
        public static final int ADR_NAME_FIELD_OFFSET = ADR_RFUE_FIELD_OFFSET + ADR_RFUE_FIELD_LENGTH;

        /** The Constant ADR_NAME_FIELD_LENGTH. */
        public static final int ADR_NAME_FIELD_LENGTH = 256;

        /** The Constant ADR_RECORD_SIZE. */
        public static final int ADR_RECORD_SIZE = ADR_NAME_FIELD_OFFSET + ADR_NAME_FIELD_LENGTH;

        long recordSize;

        int recordType;

        long adrNext;

        long agrEdrhead;

        int scope;

        int num;

        int ngrEntries;

        int maxGrEntry;

        int rfuA;

        long azEdrHead;

        int nzEntries;

        int maxZEntry;

        int rfuE;

        String name;

        // /**
        // * Gets the.
        // *
        // * @return the byte buffer
        // */
        //
        // public ByteBuffer get() {
        // this.record.position(0);
        // this.record.putLong(ADR_RECORD_SIZE);
        // this.record.putInt(ADR_RECORD_TYPE_ID);
        // this.record.putLong(this.aDRNext);
        // this.record.putLong(this.agrEDRHead);
        // this.record.putInt(this.scope);
        // this.record.putInt(this.num);
        // this.record.putInt(this.ngrEntries);
        // this.record.putInt(this.maxGrEntry);
        // this.record.putInt(this.rfuA);
        // this.record.putLong(this.azEDRHead);
        // this.record.putInt(this.nzEntries);
        // this.record.putInt(this.mAXzEntry);
        // this.record.putInt(this.rfuE);
        //
        // // should be replaced by this.record.put(this.paddedName);
        // this.record.put(getNameBytes(this.name));
        //
        // this.record.position(0);
        //
        // return this.record;
        // }
        //
        // /**
        // * Gets the name bytes.
        // *
        // * @param s the s
        // *
        // * @return the name bytes
        // *
        // */
        // public byte[] getNameBytes(final String s) {
        //
        // final byte[] padded = new byte[ADR_NAME_FIELD_LENGTH];
        //
        // final byte[] bs = s.getBytes(StandardCharsets.US_ASCII);
        //
        // int i = 0;
        //
        // for (; i < bs.length; i++) {
        // padded[i] = bs[i];
        // }
        //
        // // This is unneeded as byte defaults to 0
        // for (; i < padded.length; i++) {
        // padded[i] = 0;
        // }
        //
        // return padded;
        // }
        //
        // /**
        // * Gets the size.
        // *
        // * @return the size
        // */
        // // FIXME: This is a constant
        // public int getSize() {
        // return this.record.limit();
        // }
        //
        // /**
        // * Sets the ADR next.
        // * <p>
        // * The file offset of the next ADR.
        // * <p>
        // * Signed 8-byte integer, big-endian byte ordering.
        // * <p>
        // * Beginning with CDF V2.1 the last ADR will
        // * contain a file offset of 0x0000000000000000 in this field (to indicate the
        // * end of the ADRs).
        // * <p>
        // * Prior to CDF V2.1 this file offset is undefined in the last ADR.
        // *
        // * @param aDRNext the new ADR next
        // */
        // public void setADRNext(final long aDRNext) {
        // this.aDRNext = aDRNext;
        // }
        //
        // /**
        // * Sets the agr EDR head.
        // * <p>
        // * The file offset of the first Attribute g/rEntry Descriptor Record (AgrEDR)
        // * for this attribute.
        // * <p>
        // * Signed 8-byte integer, big-endian byte ordering.
        // * <p>
        // * The first AgrEDR contains a file offset to the next AgrEDR and so on.
        // * An AgrEDR will exist for each g/rEntry for this attribute. This field will
        // * contain
        // * 0x0000000000000000 if the attribute has no g/rEntries. Beginning with CDF
        // * V2.1 the last AgrEDR will contain a file
        // * offset of 0x0000000000000000 for the file offset of the next AgrEDR (to
        // * indicate the end of 11the AgrEDRs).
        // * <p>
        // * Prior to CDF V2.1 the “next AgrEDR" file offset in the last AgrEDR is
        // * undefined.
        // * <p>
        // * Note that the term g/rEntry is used to refer to an entry that may be either
        // a
        // * gEntry or an rEntry.
        // * <p>
        // * The type of entry described by an AgrEDR depends on the scope of the
        // * corresponding attribute.
        // * <p>
        // * AgrEDRs of a global-scoped attribute describe gEntries.
        // * <p>
        // * AgrEDRs of a variable-scoped attribute describe rEntries.
        // *
        // * @param agrEDRHead the new agr EDR head
        // */
        // public void setAgrEDRHead(final long agrEDRHead) {
        // this.agrEDRHead = agrEDRHead;
        // }
        //
        // /**
        // * Sets the az EDR head.
        // * <p>
        // * The file offset of the first Attribute zEntry Descriptor Record (AzEDR) for
        // * this attribute.
        // * <p>
        // * Signed 8-byte integer, big-endian byte ordering.
        // * <p>
        // * The first AzEDR contains a file offset to the next AzEDR and so on.
        // * <p>
        // * An AzEDR will exist for each zEntry for this attribute.
        // * <p>
        // * This field will contain 0x0000000000000000 if this attribute has no
        // zEntries.
        // * <p>
        // * The last AzEDR will contain a file offset of 0x0000000000000000 for the
        // file
        // * offset of the next AzEDR (to indicate the end of the AzEDRs).
        // *
        // * @param azEDRHead the new az EDR head
        // */
        // public void setAzEDRHead(final long azEDRHead) {
        // this.azEDRHead = azEDRHead;
        // }
        //
        // /**
        // * Sets the Max gr entry.
        // * <p>
        // * The maximum numbered g/rEntry for this attribute.
        // * <p>
        // * Signed 4-byte integer, big-endian byte ordering.
        // * <p>
        // * g/rEntries are numbered beginning with zero (0).
        // * <p>
        // * If there are no g/rEntries, this field will contain negative one (-1).
        // *
        // * @param mAXgrEntry the new MA xgr entry
        // */
        // public void setMAXgrEntry(final int mAXgrEntry) {
        // this.maxGrEntry = mAXgrEntry;
        // }
        //
        // /**
        // * Sets the Max z entry.
        // * <p>
        // * The maximum numbered zEntry for this attribute.
        // * <p>
        // * Signed 4-byte integer, big-endian byte ordering.
        // * <p>
        // * zEntries are numbered beginning with zero (0).
        // * <p>
        // * Prior to CDF V2.2 this field will always contain a value of negative one
        // * (-1).
        // *
        // * @param mAXzEntry the new MA xz entry
        // */
        // public void setMAXzEntry(final int mAXzEntry) {
        // this.mAXzEntry = mAXzEntry;
        // }
        //
        // /**
        // * Sets the name.
        // * <p>
        // * The name of this attribute.
        // * <p>
        // * Character string, ASCII character set.
        // * <p>
        // * This field is always 256 bytes in length.
        // * <p>
        // * If the number of characters in the name is less than 256, a NUL character
        // * (0x00) will be used to terminate the string.
        // * <p>
        // * In that case, the characters beyond the NUL-terminator (up to the size of
        // * this field) are undefined.
        // *
        // * @param name the new name, may not be null.
        // *
        // * @throws IllegalArgumentException if the name is longer than
        // * {@link ADR_NAME_LENGTH}.
        // */
        // public void setName(final String name) {
        //
        // Objects.requireNonNull(name, "name cannot be null.");
        //
        // byte[] nameAsBytes = name.getBytes(StandardCharsets.US_ASCII);
        //
        // int nameLength = nameAsBytes.length;
        //
        // if (nameLength > ADR_NAME_FIELD_LENGTH) {
        // throw new IllegalArgumentException("ADR names cannot exceed " +
        // ADR_NAME_FIELD_LENGTH
        // + " bytes. The supplied name, " + name + " is " + nameLength + " bytes
        // long.");
        // }
        //
        // this.name = name;
        //
        // @SuppressWarnings("hiding")
        // final byte[] paddedName = new byte[256];
        //
        // System.arraycopy(nameAsBytes, 0, paddedName, 0, nameLength);
        //
        // this.paddedName = paddedName;
        // }
        //
        // /**
        // * Sets the ngr entries.
        // * <p>
        // * The number of g/rEntries for this attribute.
        // * <p>
        // * Signed 4-byte integer, big-endian byte ordering.
        // *
        // *
        // * @param ngrEntries the new ngr entries
        // */
        // public void setNgrEntries(final int ngrEntries) {
        // this.ngrEntries = ngrEntries;
        // }
        //
        // /**
        // * Sets the num.
        // * <p>
        // * This attribute's number.
        // * <p>
        // * Signed 4-byte integer, big-endian byte ordering.
        // * <p>
        // * Attributes are numbered beginning with zero (0)
        // *
        // * @param num the new num
        // */
        // public void setNum(final int num) {
        // this.num = num;
        // }
        //
        // /**
        // * Sets the nz entries.
        // * <p>
        // * The number of zEntries for this attribute.
        // * <p>
        // * Signed 4-byte integer, big-endian byte ordering.
        // * <p>
        // * Prior to CDF V2.2 this field will always contain a value of zero (0).
        // *
        // * @param nzEntries the new nz entries
        // */
        // public void setNzEntries(final int nzEntries) {
        // this.nzEntries = nzEntries;
        // }
        //
        // /**
        // * Sets the scope.
        // * <p>
        // * The intended scope of this attribute.
        // * <p>
        // * Signed 4-byte integer, big-endian byte ordering.
        // * <p>
        // * The following internal values are valid:
        // * <p>
        // * 1 Global scope.
        // * <p>
        // * 2 Variable scope.
        // * <p>
        // * 3 Global scope assumed.
        // * <p>
        // * 4 Variable scope assumed.
        // *
        // * @param scope the new scope
        // */
        // public void setScope(final int scope) {
        //
        // switch (scope) {
        // case ADR_GLOBAL_SCOPE:
        // case ADR_VARIABLE_SCOPE:
        // case ADR_GLOBAL_SCOPE_ASSUMED:
        // case ADR_VARIABLE_SCOPE_ASSUMED:
        // break;
        // default:
        // throw new IllegalArgumentException(
        // "Scope, " + scope + ", is not valid. Scope must be 1, 2, 3, or 4.");
        //
        // }
        //
        // this.scope = scope;
        // }
    }

}
