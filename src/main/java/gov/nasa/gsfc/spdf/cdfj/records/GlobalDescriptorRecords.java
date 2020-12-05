package gov.nasa.gsfc.spdf.cdfj.records;

import static gov.nasa.gsfc.spdf.cdfj.records.RecordReaders.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import gov.nasa.gsfc.spdf.cdfj.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.GDRV2Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.GDRV3Impl.*;
import lombok.*;
import lombok.experimental.*;

/**
 * The Class GlobalDescriptorRecords.
 */
@UtilityClass
public class GlobalDescriptorRecords {

    /**
     * The value 2, which identifies this as the GDR.
     * <p>
     * Signed 4-byte integer, big-endian byte ordering.
     */
    public static final int GDR_RECORD_TYPE = CDFRecordTypes.GDR_RECORD_TYPE_INTERNAL_VALUE;

    /** The Constant GDR_NO_R_VARIABLE_RECORDS_VALUE. */
    public static final int GDR_NO_R_VARIABLE_RECORDS_VALUE = -1;

    /**
     * The Constant GDR_RFU_C_DEFAULT.
     */
    public static final int GDR_RFU_C_DEFAULT = 0;

    /** The Constant GDR_RFU_E_DEFAULT. */
    public static final int GDR_RFU_E_DEFAULT = -1;

    /** The Constant GDR_R_MAX_REC_NO_RECORDS_VALUE. */
    public static final int GDR_R_MAX_REC_NO_RECORDS_VALUE = -1;

    /** The Constant GDR_NR_VARS_DEFAULT_VALUE. */
    public static final int GDR_NR_VARS_DEFAULT_VALUE = 0;

    /** The Constant GDR_R_VDR_HEAD_V2_2_EMPTY_DEFAULT. */
    public static final int GDR_R_VDR_HEAD_V2_2_EMPTY_DEFAULT = 0x0000000000000000;

    /**
     * Read gdr V 2.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param cdrv2 the cdrv 2
     * @return the gdrv2
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GDRV2 readGdrV2(FileChannel dotCDFFileChannel, CDRV2 cdrv2) throws IOException {

        ByteBuffer gdr = readV2Record(dotCDFFileChannel, cdrv2.getGdrOffset());

        return gdrv2(gdr);
    }

    /**
     * Gdrv 2.
     *
     * @param source the source
     * @return the gdrv2
     */
    public static final GDRV2 gdrv2(ByteBuffer source) {

        GDRV2ImplBuilder<?, ?> builder = GDRV2Impl.builder()
                .recordSize(source.getInt());

        int recordType = source.getInt();

        if (GDR_RECORD_TYPE != recordType) {
            throw new IllegalArgumentException("The supplied bytebuffer does not contain a GDR record. Record Type "
                    + "was, " + recordType + ", should be " + GDR_RECORD_TYPE);
        }

        builder.rVDRHead(source.getInt())
                .zVDRHead(source.getInt())
                .adrHead(source.getInt())
                .eof(source.getInt())
                .nrVars(source.getInt())
                .numAttr(source.getInt())
                .rMaxRec(source.getInt());

        int rNumDims = source.getInt();

        builder.rNumDims(rNumDims)
                .nzVars(source.getInt())
                .uirHead(source.getInt())
                .rfuC(source.getInt())
                .leapSecondLastUpdated(source.getInt())
                .rfuE(source.getInt());

        for (int i = 0; i < rNumDims; i++) {
            builder.rDimSize(source.getInt());
        }

        return builder.build();

    }

    /**
     * Read gdr V 3.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param cdrv3             the cdrv 3
     * 
     * @return the gdrv3
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GDRV3 readGdrV3(FileChannel dotCDFFileChannel, CDRV3 cdrv3) throws IOException {

        ByteBuffer gdr = readV3Record(dotCDFFileChannel, cdrv3.getGdrOffset());

        return gdrv3(gdr);
    }

    /**
     * Gdrv 3.
     *
     * @param source the source
     * @return the gdrv3
     */
    public static final GDRV3 gdrv3(ByteBuffer source) {

        GDRV3ImplBuilder<?, ?> builder = GDRV3Impl.builder()
                .recordSize(source.getLong());

        int recordType = source.getInt();

        if (GDR_RECORD_TYPE != recordType) {
            throw new IllegalArgumentException("The supplied bytebuffer does not contain a GDR record. Record Type "
                    + "was, " + recordType + ", should be " + GDR_RECORD_TYPE);
        }

        builder.rVDRHead(source.getLong())
                .zVDRHead(source.getLong())
                .adrHead(source.getLong())
                .eof(source.getLong())
                .nrVars(source.getInt())
                .numAttr(source.getInt())
                .rMaxRec(source.getInt());

        int rNumDims = source.getInt();

        builder.rNumDims(rNumDims)
                .nzVars(source.getInt())
                .uirHead(source.getLong())
                .rfuC(source.getInt())
                .leapSecondLastUpdated(source.getInt())
                .rfuE(source.getInt());

        for (int i = 0; i < rNumDims; i++) {
            builder.rDimSize(source.getInt());
        }

        return builder.build();
    }

    /**
     * The Interface GDR.
     */
    public interface GDR extends CDFRecord {

        /**
         * The value 2, which identifies this as the GDR.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the record type
         */
        @Override
        default int getRecordType() {
            return GDR_RECORD_TYPE;
        }

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
         *
         * @return the RVDR head
         */
        Number getRVDRHead();

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
         *
         * @return the ZVDR head
         */
        Number getZVDRHead();

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
         *
         * @return the adr head
         */
        Number getAdrHead();

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
         *
         * @return the eof
         */
        Number getEof();

        /**
         * The number of rVariables in the CDF.
         * <p>
         * This will correspond to the number of rVDRs in the dotCDF file.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the nr vars
         */
        int getNrVars();

        /**
         * The number of attributes in the CDF.
         * <p>
         * This will correspond to the number of
         * ADRs in the dotCDF file.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the num attr
         */
        int getNumAttr();

        /**
         * The maximum rVariable record number in the CDF.
         * <p>
         * Note that variable record numbers are numbered beginning with zero (0).
         * <p>
         * If no rVariable records exist, this value will be negative one (-1).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the r max rec
         */
        int getRMaxRec();

        /**
         * Checks for R variable records.
         *
         * @return true, if successful
         */
        default boolean hasRVariableRecords() {
            return GDR_NO_R_VARIABLE_RECORDS_VALUE == getRMaxRec();
        }

        /**
         * The number of dimensions for rVariables.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the r num dims
         */
        int getRNumDims();

        /**
         * The number of zVariables in the CDF.
         * <p>
         * This will correspond to the number of zVDRs in the
         * dotCDF file.
         * <p>
         * Prior to CDF V2.2 this value will always be zero (0).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the NZ vars
         */
        int getNzVars();

        /**
         * The file offset of the first Unused Internal Record (UIR).
         * <p>
         * The first UIR contains the file offset of the next UIR and so on.
         * <p>
         * The last UIR contains a file offset of 0x0000000000000000 for the file
         * offset of the next UIR (indicating the end of the UIRs).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         *
         * @return the uir head
         */
        Number getUirHead();

        /**
         * Reserved for future use.
         * <p>
         * Always set to zero (0).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the rfu C
         */
        default int getRfuC() {
            return GDR_RFU_C_DEFAULT;
        }

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
         *
         * @return the leap second last updated
         */
        int getLeapSecondLastUpdated();

        /**
         * Gets the leap second last updated as local date.
         *
         * @return the leap second last updated as local date
         */
        default Optional<LocalDate> leapSecondLastUpdatedAsLocalDate() {

            int leapSecondLastUpdated = getLeapSecondLastUpdated();

            if (leapSecondLastUpdated > 0) {

                String leapSecondLastUpdatedAsYYYYMMDD = Integer.toString(leapSecondLastUpdated);

                return Optional.of(LocalDate.parse(leapSecondLastUpdatedAsYYYYMMDD, DateTimeFormatter.BASIC_ISO_DATE));

            }

            return Optional.empty();

        }

        /**
         * Reserved for future use.
         * <p>
         * Always set to zero (-1).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the rfu E
         */
        default int getRfuE() {
            return GDR_RFU_E_DEFAULT;
        }

        /**
         * Zero or more contiguous rVariable dimension sizes depending on the value of
         * the rNumDims field described above.
         * <p>
         * Signed 4-byte integers, big-endian byte ordering within each.
         *
         * @return the r dim sizes
         */
        List<Integer> getRDimSizes();

    }

    /**
     * The Interface GDRV2.
     */
    public interface GDRV2 extends GDR, CDFV2Record {

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
         *
         * @return the RVDR head
         */
        @Override
        Integer getRVDRHead();

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
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the ZVDR head
         */
        @Override
        Integer getZVDRHead();

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
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the adr head
         */
        @Override
        Integer getAdrHead();

        /**
         * The file offset of the first Unused Internal Record (UIR).
         * <p>
         * The first UIR contains the file offset of the next UIR and so on.
         * <p>
         * The last UIR contains a file offset of 0x0000000000000000 for the file
         * offset of the next UIR (indicating the end of the UIRs).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the uir head
         */
        @Override
        Integer getUirHead();

    }

    /**
     * The Interface GDRV3.
     */
    public interface GDRV3 extends GDR, CDFV3Record {

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
         *
         * @return the RVDR head
         */
        @Override
        Long getRVDRHead();

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
         *
         * @return the ZVDR head
         */
        @Override
        Long getZVDRHead();

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
         *
         * @return the adr head
         */
        @Override
        Long getAdrHead();

        /**
         * The file offset of the first Unused Internal Record (UIR).
         * <p>
         * The first UIR contains the file offset of the next UIR and so on.
         * <p>
         * The last UIR contains a file offset of 0x0000000000000000 for the file
         * offset of the next UIR (indicating the end of the UIRs).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         *
         * @return the uir head
         */
        @Override
        Long getUirHead();

    }

    @Value
    @SuperBuilder(toBuilder = true)
    @NonFinal
    abstract static class AbstractGDR<RECORD_SIZE_FIELD_TYPE extends Number, OFFSET_FIELD_TYPE extends Number>
            implements GDR {

        /**
         * The size in bytes of this GDR (including this field).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        RECORD_SIZE_FIELD_TYPE recordSize;

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
        OFFSET_FIELD_TYPE rVDRHead;

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
        OFFSET_FIELD_TYPE zVDRHead;

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
        OFFSET_FIELD_TYPE adrHead;

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
        OFFSET_FIELD_TYPE eof;

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
        OFFSET_FIELD_TYPE uirHead;

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

        @Singular
        List<Integer> rDimSizes;
    }

    /**
     * The Class GDRV2Impl.
     */
    @Value
    @NonFinal
    
    /**
     * The Class GDRV2ImplBuilder.
     *
     * @param <C> the generic type
     * @param <B> the generic type
     */
    @SuperBuilder(toBuilder = true)
    public static class GDRV2Impl extends AbstractGDR<Integer, Integer> implements GDRV2 {

    }

    /**
     * The Class GDRV3Impl.
     */
    @Value
    @NonFinal
    
    /**
     * The Class GDRV3ImplBuilder.
     *
     * @param <C> the generic type
     * @param <B> the generic type
     */
    @SuperBuilder(toBuilder = true)
    public static class GDRV3Impl extends AbstractGDR<Long, Long> implements GDRV3 {

    }

}
