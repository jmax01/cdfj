package gov.nasa.gsfc.spdf.cdfj.records;

import gov.nasa.gsfc.spdf.cdfj.records.CDFRecordFields.IntegerCDFRecordField;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecordFields.LongRecordSizeField;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecordFields.RecordSizeField;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecordFields.RecordTypeField;

public final class CDFRecords {

    /**
     * CDF Descriptor Record.
     * <p>
     * General information about the CDF (see Section 2.2)
     */
    public static final int CDR_RECORD_TYPE_INTERNAL_VALUE = 1;

    /**
     * Global Descriptor Record.
     * <p>
     * Additional general information about the CDF (see
     * Section 2.3).
     *
     */
    public static final int GDR_RECORD_TYPE_INTERNAL_VALUE = 2;

    /**
     * rVariable Descriptor Record.
     * <p>
     * Information about an rVariable (see Section 2.6).
     */
    public static final int rVDR_RECORD_TYPE_INTERNAL_VALUE = 3;

    /**
     * Attribute Descriptor Record.
     * <p>
     * Information about an attribute (see Section 2.4).
     */
    public static final int ADR_RECORD_TYPE_INTERNAL_VALUE = 4;

    /**
     * Attribute g/rEntry Descriptor Record.
     * <p>
     * Information about a gEntry or rEntry of an attribute (see Section 2.5).
     */
    public static final int A_GR_EDR_RECORD_TYPE_INTERNAL_VALUE = 5;

    /**
     * Variable Index Record.
     * <p>
     * Indexing information for a variable (see Section 2.7).
     */
    public static final int VXR_RECORD_TYPE_INTERNAL_VALUE = 6;

    /**
     * Variable Values Record.
     * <p>
     * One or more variable records (see Section 2.8).
     */
    public static final int VVR_RECORD_TYPE_INTERNAL_VALUE = 7;

    /**
     * zVariable Descriptor Record.
     * <p>
     * Information about a zVariable (see Section 2.6).
     */
    public static final int Z_VDR_RECORD_TYPE_INTERNAL_VALUE = 8;

    /**
     * Attribute zEntry Descriptor Record.
     * <p>
     * Information about a zEntry of an attribute (see Section 2.5).
     */
    public static final int A_Z_EDR_RECORD_TYPE_INTERNAL_VALUE = 9;

    /**
     * Compressed CDF Record.
     * <p>
     * Information about a compressed CDF/variable (see Section 2.9).
     */
    public static final int CCR_RECORD_TYPE_INTERNAL_VALUE = 10;

    /**
     * Compression Parameters Record.
     * <p>
     * Information about the compression used for a CDF/variable (see Section 2.10).
     */
    public static final int CPR_RECORD_TYPE_INTERNAL_VALUE = 11;

    /**
     * Sparseness Parameters Record.
     * <p>
     * Information about the specified sparseness array (see Section 2.11).
     */
    public static final int SPR_RECORD_TYPE_INTERNAL_VALUE = 12;

    /**
     * Compressed Variable Values Record.
     * <p>
     * Information for the compressed CDF/variable (see Section 2.12).
     */
    public static final int CVVR_RECORD_TYPE_INTERNAL_VALUE = 13;

    /**
     * Unused Internal Record.
     * <p>
     * An internal record not currently being used (see Section 2.13).
     */
    public static final int UIR_RECORD_TYPE_INTERNAL_VALUE = -1;

    interface CDFRecord {

        RecordSizeField<?> getRecordSizeField();

        RecordTypeField getRecordTypeField();

    }

    interface CDFRecordV3 extends CDFRecord {

        @Override
        LongRecordSizeField getRecordSizeField();

    }

    interface CDFDescriptorRecord {

        RecordSizeField<?> getRecordSizeField();

        RecordTypeField getRecordTypeField();

        IntegerCDFRecordField getVersionField();

    }

    interface VersionField extends IntegerCDFRecordField {

    }
}
