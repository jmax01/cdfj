package gov.nasa.gsfc.spdf.cdfj.records;

import static gov.nasa.gsfc.spdf.cdfj.records.RecordReaders.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import gov.nasa.gsfc.spdf.cdfj.*;
import gov.nasa.gsfc.spdf.cdfj.fields.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.CDRV2Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.CDRV3Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecords.*;
import lombok.*;
import lombok.experimental.*;

/**
 * The Class CDFDecriptorRecords.
 */
@UtilityClass
public class CDFDecriptorRecords {

    /** The Constant CDR_RECORD_OFFSET. */
    public static final int CDR_RECORD_OFFSET = 8;

    /**
     * The value 1, which identifies this as the GDR.
     * <p>
     * Signed 4-byte integer, big-endian byte ordering.
     */
    public static final int CDR_RECORD_TYPE = CDFRecordTypes.CDR_RECORD_TYPE_INTERNAL_VALUE;

    /** Prior to CDF V2.5, the copyright consisted of 1945 characters (bytes). */
    public static final int COPYRIGHT_V2_4_FIELD_SIZE = 1945;

    /**
     * Since the release of CDF V2.5, the copyright field has been reduced to 256 characters (bytes)
     */
    public static final int COPYRIGHT_V2_5_UP_FIELD_SIZE = 256;

    /**
     * Signed 4-byte integer, big-endian byte ordering.
     * Reserved for future use. Always set to zero (0).
     */
    public static final int RFU_A_FIELD_DEFAULT_VALUE = 0;

    /**
     * Signed 4-byte integer, big-endian byte ordering.
     * Reserved for future use. Always set to zero (0).
     */
    public static final int RFU_B_FIELD_DEFAULT_VALUE = 0;

    /**
     * Signed 4-byte integer, big-endian byte ordering.
     * Reserved for future use. Always set to negative one (-1).
     */
    public static final int RFU_E_FIELD_DEFAULT_VALUE = -1;

    /**
     * A value of one (1) indicates the
     * file was created directly by Java without the use of the C library.
     */
    public static final int PURE_JAVA_IDENTIFIER_VALUE = 1;

    /** A value of negative one (-1) means the file was created by the normal way through the CDF’s C-based library. */
    public static final int C_LIB_IDENTIFIER_VALUE = -1;

    /**
     * A values of two (2) indicates
     * that the file was created by Python without the use of the C library.
     */
    public static final int PURE_PYTHON_IDENTIFIER_VALUE = 2;

    /**
     * The majority of variable values within a variable record. Variable records are
     * described in Chapter 4. Set indicates row-majority. Clear indicates column-majority.
     */
    public static final int IS_ROW_MAJORITY_FLAG_SET = 0x00000001;

    /** The file format of the CDF. Set indicates single-file. Clear indicates multi-file */
    public static final int IS_SINGLE_FILE_FLAG_SET = 0x00000002;

    /** The checksum of the CDF. Set indicates a checksum method is used. */
    public static final int HAS_CHECKSUM_FLAG_SET = 0x00000004;

    /**
     * The MD5 checksum method indicator. Set indicates MD5 method is used for
     * the checksum. Bit 2 must be set.
     */
    public static final int IS_MD5_CHECKSUM_FLAG_SET = 0x00000008;

    /** Reserved for another checksum method bit 2 must be set and bit 3 clear. */
    public static final int IS_NON_MD5_CHECKSUM_FLAG_SET = 0x000000010;

    /**
     * Read cdr V 2.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * 
     * @return the cdrv2
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static CDRV2 readCdrV2(FileChannel dotCDFFileChannel) throws IOException {

        ByteBuffer recordAsBuffer = readV2Record(dotCDFFileChannel, CDR_RECORD_OFFSET);

        return cdrv2(recordAsBuffer);

    }

    /**
     * Cdrv 2.
     *
     * @param source the source
     * 
     * @return the cdrv2
     */
    public static final CDRV2 cdrv2(ByteBuffer source) {

        int recordSize = source.getInt();

        CDRV2ImplBuilder<?, ?> builder = CDRV2Impl.builder()
                .recordSize(recordSize);

        int recordType = source.getInt();

        if (CDR_RECORD_TYPE != recordType) {
            throw new IllegalArgumentException("The supplied bytebuffer does not contain a CDR record. Record Type "
                    + "was, " + recordType + ", should be " + CDR_RECORD_TYPE);
        }

        builder.gdrOffset(source.getInt())
                .version(source.getInt());
        int release = source.getInt();

        builder.release(release)
                .encoding(source.getInt())
                .flags(source.getInt())
                .rfuA(source.getInt())
                .rfuB(source.getInt())
                .increment(source.getInt())
                .identifier(source.getInt())
                .rfuE(source.getInt());

        int copyrightFieldSize = recordSize == 304 ? COPYRIGHT_V2_5_UP_FIELD_SIZE : COPYRIGHT_V2_4_FIELD_SIZE;

        String copyright = FieldReaders.readNullTerminatedString(source, source.position(), copyrightFieldSize);

        return builder.copyright(copyright)
                .build();

    }

    /**
     * Read cdr V 3.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * 
     * @return the cdrv3
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static CDRV3 readCdrV3(FileChannel dotCDFFileChannel) throws IOException {

        ByteBuffer recordAsBuffer = readV3Record(dotCDFFileChannel, CDR_RECORD_OFFSET);

        return cdrv3(recordAsBuffer);

    }

    /**
     * Cdrv 3.
     *
     * @param source the source
     * 
     * @return the cdrv3
     */
    public static final CDRV3 cdrv3(ByteBuffer source) {

        CDRV3ImplBuilder<?, ?> builder = CDRV3Impl.builder()
                .recordSize(source.getLong());

        int recordType = source.getInt();

        if (CDR_RECORD_TYPE != recordType) {
            throw new IllegalArgumentException("The supplied bytebuffer does not contain a CDR record. Record Type "
                    + "was, " + recordType + ", should be " + CDR_RECORD_TYPE);
        }

        builder.gdrOffset(source.getLong())
                .version(source.getInt())
                .release(source.getInt())
                .encoding(source.getInt())
                .flags(source.getInt())
                .rfuA(source.getInt())
                .rfuB(source.getInt())
                .increment(source.getInt())
                .identifier(source.getInt())
                .rfuE(source.getInt());

        int copyrightFieldSize = COPYRIGHT_V2_5_UP_FIELD_SIZE;

        String copyright = FieldReaders.readNullTerminatedString(source, source.position(), copyrightFieldSize);

        return builder.copyright(copyright)
                .build();

    }

    /**
     * The Interface CDR.
     */
    public interface CDR extends CDFRecord {

        /**
         * The value 1, which identifies this as the GDR.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the record type
         */
        @Override
        default int getRecordType() {
            return CDR_RECORD_TYPE;
        }

        /**
         * The file offset of the Global Descriptor Record.
         *
         * @return the gdr offset
         */
        Number getGdrOffset();

        /**
         * The version of the CDF distribution (library) that created this CDF. CDF distributions are
         * identified with four values: version, release, increment, and sub-increment. For example,
         * CDF V2.5.8a is CDF version 2, release 5, and increment 8, sub-increment ‘a’. Note that the
         * sub-increment is not stored in a CDF.
         *
         * @return the version
         */
        int getVersion();

        /**
         * The release of the CDF distribution that created this CDF. See the Version field above.
         *
         * @return the release
         */
        int getRelease();

        /**
         * The data encoding for attribute entry and variable values. Section 5.3 describes the
         * supported data encodings and their corresponding internal values.
         *
         * @return the encoding
         */
        int getEncoding();

        /**
         * Boolean flags, one per bit, describing some aspect of the CDF. Bit numbering is described in
         * Chapter 5.
         * <p>
         * The meaning of each bit is as follows...
         * <p>
         * 0: The majority of variable values within a variable record. Variable records are
         * described in Chapter 4. Set indicates row-majority. Clear indicates column-majority.
         * <p>
         * 1: The file format of the CDF. Set indicates single-file. Clear indicates multi-file.
         * <p>
         * 2: The checksum of the CDF. Set indicates a checksum method is used.
         * <p>
         * 3: The MD5 checksum method indicator. Set indicates MD5 method is used for
         * the checksum. Bit 2 must be set.
         * <p>
         * 4: Reserved for another checksum method. Bit 2 must be set and bit 3 must be
         * clear .
         * <p>
         * 5-31: Reserved for future use. These bits are always clear .
         *
         * @return the flags
         */
        int getFlags();

        /**
         * The majority of variable values within a variable record are row.
         *
         * @return true, if is row majority
         */
        default boolean isRowMajority() {
            return (getFlags() & IS_ROW_MAJORITY_FLAG_SET) != 0;
        }

        /**
         * The majority of variable values within a variable record are column.
         *
         * @return true, if is column majority
         */
        default boolean isColumnMajority() {
            return (getFlags() & IS_ROW_MAJORITY_FLAG_SET) == 0;
        }

        /**
         * True if this is a single file CDF.
         *
         * @return true, if is single file
         */
        default boolean isSingleFile() {
            return (getFlags() & IS_SINGLE_FILE_FLAG_SET) != 0;
        }

        /**
         * True if this is a multi-file CDF.
         *
         * @return true, if is multi file
         */
        default boolean isMultiFile() {
            return (getFlags() & IS_SINGLE_FILE_FLAG_SET) == 0;
        }

        /**
         * True if the file has a checksum.
         *
         * @return true, if the file has a checksum.
         */
        default boolean hasChecksum() {
            return (getFlags() & HAS_CHECKSUM_FLAG_SET) != 0;
        }

        /**
         * True if the file doesn't have a checksum.
         *
         * @return true, f the file doesn't have a checksum.
         */
        default boolean doesNotHaveChecksum() {
            return (getFlags() & HAS_CHECKSUM_FLAG_SET) == 0;
        }

        /**
         * True if the file has an md5 checksum.
         *
         * @return true, if the file has an md5 checksum.
         */
        default boolean isMd5Checksum() {
            return ((getFlags() & IS_MD5_CHECKSUM_FLAG_SET) != 0) && hasChecksum();
        }

        /**
         * True if the file has a non-md5 checksum.
         *
         * @return true, if the file has a non-md5 checksum.
         */
        default boolean isNonMd5Checksum() {
            return ((getFlags() & IS_NON_MD5_CHECKSUM_FLAG_SET) != 0) && hasChecksum();
        }

        /**
         * Signed 4-byte integer, big-endian byte ordering.
         * Reserved for future use. Always set to zero (0).
         *
         * @return the rfu A
         */
        int getRfuA();

        /**
         * Signed 4-byte integer, big-endian byte ordering.
         * Reserved for future use. Always set to zero (0).
         *
         * @return the rfu B
         */
        int getRfuB();

        /**
         * The increment of the CDF distribution that created this CDF. See the Version field above.
         * Prior to CDF V2.1 this field was always set to zero (0).
         *
         * @return the increment
         */
        int getIncrement();

        /**
         * This field indicates how the file was created.
         * <p>
         * A value of negative one (-1) means the file was
         * created by the normal way through the CDF’s C-based library.
         * <p>
         * It has a value of one (1) if the
         * file was created directly by Java without the use of the library.
         * <p>
         * A values of two (2) indicates
         * that the file was created by Python without the use of the library.
         *
         * @return the identifier
         */
        default int getIdentifier() {
            return PURE_JAVA_IDENTIFIER_VALUE;
        }

        /**
         * Signed 4-byte integer, big-endian byte ordering.
         * Reserved for future use. Always set to negative one (-1).
         *
         * @return the rfu E
         */
        int getRfuE();

        /**
         *
         * The CDF copyright notice.10 This consists of a string of characters containing one or more
         * lines of text with each line of text separated by a newline character (0x0A). If the total
         * number of characters in the copyright is less than the size of this field, a NUL character
         * (0x00) will be used to terminate the string. In that case, the characters beyond the NULterminator
         * (up to the size of this field) are undefined. This field may be one of two sizes.
         * Prior to CDF V2.5, this field consisted of 1945 characters (bytes).11 Since the release of
         * CDF V2.5, this field has been reduced to 256 characters (bytes).
         *
         * @return the copyright stripped of NUL characters
         */
        String getCopyright();

        /**
         * Checks if is pure java created.
         *
         * @return true, if is pure java created
         */
        default boolean isPureJavaCreated() {
            return PURE_JAVA_IDENTIFIER_VALUE == getIdentifier();
        }

        /**
         * Checks if is pure java created.
         *
         * @return true, if is pure java created
         */
        default boolean isCLibCreated() {
            return C_LIB_IDENTIFIER_VALUE == getIdentifier();
        }

        /**
         * Checks if is pure python created.
         *
         * @return true, if is pure python created
         */
        default boolean isPurePythonCreated() {
            return PURE_PYTHON_IDENTIFIER_VALUE == getIdentifier();
        }

        /**
         * Gets the complete version string.
         *
         * @return the complete version string
         */
        default String getCompleteVersionString() {
            return "V" + this.getVersion() + "." + getRelease() + "." + getIncrement();
        }
    }

    /**
     * The Interface CDRV2.
     */
    public interface CDRV2 extends CDR, CDFV2Record {

        /**
         * Gets the gdr offset.
         *
         * @return the gdr offset
         */
        @Override
        Integer getGdrOffset();
    }

    /**
     * The Interface CDRV3.
     */
    public interface CDRV3 extends CDR, CDFV3Record {

        /**
         * Gets the gdr offset.
         *
         * @return the gdr offset
         */
        @Override
        Long getGdrOffset();
    }

    @Value
    @NonFinal
    @SuperBuilder(toBuilder = true)
    abstract static class AbstractCDR<RECORD_SIZE_FIELD_TYPE extends Number, OFFSET_FIELD_TYPE extends Number>
            implements CDR {

        final RECORD_SIZE_FIELD_TYPE recordSize;

        @Builder.Default
        final int recordType = CDR_RECORD_TYPE;

        final OFFSET_FIELD_TYPE gdrOffset;

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

    /**
     * The Class CDRV2Impl.
     */
    @Value

    /**
     * The Class CDRV2ImplBuilder.
     *
     * @param <C> the generic type
     * @param <B> the generic type
     */
    @SuperBuilder(toBuilder = true)
    public static class CDRV2Impl extends AbstractCDR<Integer, Integer> implements CDRV2 {

    }

    /**
     * The Class CDRV3Impl.
     */
    @Value

    /**
     * The Class CDRV3ImplBuilder.
     *
     * @param <C> the generic type
     * @param <B> the generic type
     */
    @SuperBuilder(toBuilder = true)
    public static class CDRV3Impl extends AbstractCDR<Long, Long> implements CDRV3 {

    }

}
