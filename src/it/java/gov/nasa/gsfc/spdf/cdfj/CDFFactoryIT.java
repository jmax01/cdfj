package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.files.dotcdf.CDFMagicNumbers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.CDRV3;
import gov.nasa.gsfc.spdf.cdfj.test.*;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords;
import gov.nasa.gsfc.spdf.cdfj.records.RecordReaders;

class CDFFactoryIT {

    static final Logger LOGGER = LogManager.getLogger();

    @BeforeAll
    static void downloadNasaCdfTestFiles() {
        NasaCdfTestFiles.downloadNasaCdfTestFiles(false);
    }

    @Test
    void test2() throws IOException {
        // String fileName = "target/test-classes/tha_l2_scm_20160831_v01.cdf";
        String fileName = NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH
                .resolve("c1_waveform_wbd_200202080940_v01_subset.cdf")
                .toString();
        Path filePath = Paths.get(fileName);

        try (FileChannel fc = FileChannel.open(filePath)) {

            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            fc.read(byteBuffer);
            byteBuffer.position(0);

            long first8Bytes = byteBuffer.getLong();
            LOGGER.info("{}", Long.toHexString(first8Bytes));
            assertEquals(CDF_V3_MAGIC_NUMBER_UNCOMPRESSED, first8Bytes);
            assertEquals(CDF_V3_MAGIC_NUMBER_UNCOMPRESSED, CDFFactory.CDF3_MAGIC);

            ByteBuffer cdfdrbb = RecordReaders.readV3Record(fc, 8);

            CDRV3 cdr = CDFDecriptorRecords.cdrv3(cdfdrbb);

            LOGGER.info("{}", cdr);

            LOGGER.info("{}", fc.position());

            ByteBuffer gdrBB = RecordReaders.readV3Record(fc, cdr.getGdrOffset());

            LOGGER.info("fcp {}", fc.position());

            GlobalDescriptorRecords.GDRV3 gdr = GlobalDescriptorRecords.gdrv3(gdrBB);

            LOGGER.info("{}", gdr);
            LOGGER.info("fcp {}", fc.position());

            // List<ADR> adrs = readAdrs(fc, gdr);
            //
            // LOGGER.info("{}", adrs);
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

    // static List<ADR> readAdrs(final FileChannel fc, final GlobalDescriptorRecords.GDRV3 gdr) throws IOException {
    //
    // long nextAdr = gdr.getAdrHead();
    //
    // List<ADR> adrs = new ArrayList<>();
    //
    // while (nextAdr > 0) {
    //
    // ADR adr = readAdr(fc, nextAdr);
    // nextAdr = adr.getAdrNext();
    // adrs.add(adr);
    // }
    //
    // return adrs;
    // }
    //
    // static ADR readAdr(final FileChannel fc, final long adrHead) throws IOException {
    //
    // fc.position(adrHead);
    //
    // ByteBuffer adrBB = RecordReaders.readV3Record(fc, adrHead);
    //
    // ADRBuilder adrBuilder = ADR.builder()
    // .recordSize(adrBB.getLong(0))
    // .recordType(adrBB.getInt(ADR.ADR_RECORD_TYPE_OFFSET))
    // .adrNext(adrBB.getLong(ADR.ADR_ADR_NEXT_FIELD_OFFSET))
    // .agrEdrhead(adrBB.getLong(ADR.ADR_AGR_EDR_HEAD_FIELD_OFFSET))
    // .scope(adrBB.getInt(ADR.ADR_SCOPE_FIELD_OFFSET))
    // .num(adrBB.getInt(ADR.ADR_NUM_FIELD_OFFSET))
    // .ngrEntries(adrBB.getInt(ADR.ADR_NGR_ENTRIES_FIELD_OFFSET))
    // .maxGrEntry(adrBB.getInt(ADR.ADR_MAX_GR_ENTRY_FIELD_OFFSET))
    // .rfuA(adrBB.getInt(ADR.ADR_RFUA_FIELD_OFFSET))
    // .azEdrHead(adrBB.getLong(ADR.ADR_AZ_EDR_HEAD_FIELD_OFFSET))
    // .nzEntries(adrBB.getInt(ADR.ADR_NZ_ENTRIES_FIELD_OFFSET))
    // .maxZEntry(adrBB.getInt(ADR.ADR_MAX_Z_ENTRIES_FIELD_OFFSET))
    // .rfuE(adrBB.getInt(ADR.ADR_RFUE_FIELD_OFFSET));
    //
    // String nameField = NameFields.readV3NameField(adrBB, ADR.ADR_NAME_FIELD_OFFSET);
    //
    // ADR adr = adrBuilder.name(nameField)
    // .build();
    //
    // return adr;
    // }

}
