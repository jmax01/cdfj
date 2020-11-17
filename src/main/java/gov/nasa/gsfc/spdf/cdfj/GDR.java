package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * The GDR contains general information about the CDF (as does the CDR).
 * <p>
 * All dotCDF files contain a single Global Descriptor Record (GDR) at the file
 * offset contained in the GDRoffset field of the CDR (described in Section
 * 2.2).
 *
 * @author nand
 */
public class GDR {

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
    final int rfuE = GDR_RFU_E_DEFAULT;

    /**
     * Zero or more contiguous rVariable dimension sizes depending on the value of
     * the rNumDims field described above.
     * <p>
     * Signed 4-byte integers, big-endian byte ordering within each.
     */
    int rDimSizes;

    // FIXME: Why is this here;
    long position;

    /**
     * Gets the populated ByteBuffer.
     *
     * @return the byte buffer
     */
    public ByteBuffer get() {

        ByteBuffer bb = ByteBuffer.allocate(GDR_FIXED_SEGMENT_SIZE)
                .putLong(GDR_FIXED_SEGMENT_SIZE)
                .putInt(GDR_RECORD_TYPE)
                .putLong(this.rVDRHead)
                .putLong(this.zVDRHead)
                .putLong(this.adrHead)
                .putLong(this.eof)
                .putInt(this.nrVars)
                .putInt(this.numAttr)
                .putInt(this.rMaxRec)
                .putInt(this.rNumDims)
                .putInt(this.nzVars)
                .putLong(this.uirHead)
                .putInt(this.rfuC)
                .putInt(this.leapSecondLastUpdated)
                .putInt(this.rfuE);
        bb.position(0);
        return bb;

    }

    /**
     * Gets the size.
     *
     * @return the size {@value #GDR_FIXED_SEGMENT_SIZE}
     */
    @SuppressWarnings("static-method")
    public int getSize() {
        return GDR_FIXED_SEGMENT_SIZE;
    }

    /**
     * Sets the file offset of the first Attribute Descriptor Record (ADR).
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
     * @param adrHead the new ADR head
     */
    public void setADRHead(final long adrHead) {
        this.adrHead = adrHead;
    }

    /**
     * Sets the end-of-file (EOF) position in the dotCDF file.
     * <p>
     * This is the file offset of the byte that is one beyond the last byte of the
     * last internal record.
     * <p>
     * (This value is also the total number of bytes used in the dotCDF file.)
     * <p>
     * Prior to CDF V2.1, this field is undefined.
     * <p>
     * Signed 8-byte integer, big-endian byte ordering.
     *
     * @param eof the new eof
     */
    public void setEof(final long eof) {
        this.eof = eof;
    }

    /**
     * Sets the leap second last updated.
     *
     * @param leapSecondLastUpdated the new last leap second id
     *
     * @deprecated use {@link #setLeapSecondLastUpdated(int)}
     */
    @Deprecated
    public void setLastLeapSecondId(final int leapSecondLastUpdated) {
        this.leapSecondLastUpdated = leapSecondLastUpdated;
    }

    /**
     * Sets the leap second last updated.
     *
     * @param leapSecondLastUpdated the new leap second last updated
     */
    public void setLeapSecondLastUpdated(final int leapSecondLastUpdated) {
        this.leapSecondLastUpdated = leapSecondLastUpdated;
    }

    /**
     * Sets the number of attributes in the CDF.
     * <p>
     * This will correspond to the number of
     * ADRs in the dotCDF file.
     * <p>
     * Signed 4-byte integer, big-endian byte ordering.
     *
     * @param numAttr the new num attr
     */
    public void setNumAttr(final int numAttr) {
        this.numAttr = numAttr;
    }

    /**
     * Sets the number of zVariables in the CDF.
     * <p>
     * This will correspond to the number of zVDRs in the
     * dotCDF file.
     * <p>
     * Prior to CDF V2.2 this value will always be zero (0).
     * <p>
     * Signed 4-byte integer, big-endian byte ordering.
     *
     * @param nzVars the new nz vars
     */
    public void setNzVars(final int nzVars) {
        this.nzVars = nzVars;
    }

    /**
     * Sets the file offset of the first zVariable Descriptor Record (zVDR).
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
     * @param zVDRHead the new ZVDR head
     */
    public void setZVDRHead(final long zVDRHead) {
        this.zVDRHead = zVDRHead;
    }
}
