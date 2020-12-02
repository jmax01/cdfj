package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.fields.NameFields.*;

import gov.nasa.gsfc.spdf.cdfj.fields.NameFields;

/**
 * The Interface CDF2.
 *
 * @author nand
 */
public interface CDF2 extends CDFCore {

    /**
     * The max name field size.
     *
     * @deprecated use {@link NameFields#NAME_FIELD_SIZE_V2_IN_BYTES}
     */
    @Deprecated
    int MAX_STRING_SIZE = NameFields.NAME_FIELD_SIZE_V2_IN_BYTES;

    /** The agr edrhead offset. */
    int AGR_EDRHEAD_OFFSET = 12;

    /** The az edrhead offset. */
    int AZ_EDRHEAD_OFFSET = 36;

    /** The r dimsizes offset. */
    int R_DIMSIZES_OFFSET = 60;

    /** The cdf version. */
    int CDF_VERSION = 2;

    /** The offset next vdr. */
    int OFFSET_NEXT_VDR = 8;

    /** The offset next adr. */
    int OFFSET_NEXT_ADR = 8;
    // attribute

    /** The attr offset name. */
    int ATTR_OFFSET_NAME = 52;

    /** The offset next aedr. */
    int OFFSET_NEXT_AEDR = 8;

    /** The offset scope. */
    int OFFSET_SCOPE = 16;

    /** The offset entrynum. */
    int OFFSET_ENTRYNUM = 20;

    /** The attr offset datatype. */
    int ATTR_OFFSET_DATATYPE = 16;

    /** The attr offset num elements. */
    int ATTR_OFFSET_NUM_ELEMENTS = 24;

    /** The offset value. */
    int OFFSET_VALUE = 48;
    // variable

    /** The var offset datatype. */
    int VAR_OFFSET_DATATYPE = 12;

    /** The offset maxrec. */
    int OFFSET_MAXREC = 16;
    /*
     * public final int VAR_OFFSET_NAME = 64;
     * public final int OFFSET_zNumDims = VAR_OFFSET_NAME + MAX_STRING_SIZE;
     * public final int VAR_OFFSET_NUM_ELEMENTS = 48;
     * public final int OFFSET_NUM = 52;
     */

    /** The offset first vxr. */

    int OFFSET_FIRST_VXR = 20;

    /** The offset flags. */
    int OFFSET_FLAGS = 28;

    /** The offset srecords. */
    int OFFSET_SRECORDS = 32;

    /** The offset records. */
    int OFFSET_RECORDS = 8;

    /** The offset blocking factor. */
    int OFFSET_BLOCKING_FACTOR = 60;
    // data

    /** The offset next vxr. */
    int OFFSET_NEXT_VXR = 8;

    /** The offset nentries. */
    int OFFSET_NENTRIES = 12;

    /** The offset nused. */
    int OFFSET_NUSED = 16;

    /** The offset first. */
    int OFFSET_FIRST = 20;

    /** The offset record type. */
    int OFFSET_RECORD_TYPE = 4;
    // compressed

    /** The offset cdata. */
    int OFFSET_CDATA = 16;

    /** The offset csize. */
    int OFFSET_CSIZE = 12;

    @Override
    default int maxNameFieldSize() {
        return NAME_FIELD_SIZE_V2_IN_BYTES;
    }

    /**
     * Record size field java type.
     *
     * @return the class
     */
    @Override
    default Class<Integer> recordSizeFieldJavaType() {
        return Integer.class;
    }

    /**
     * Offset field java type.
     *
     * @return the class
     */
    @Override
    default Class<Integer> offsetFieldJavaType() {
        return Integer.class;
    }

    /**
     * Record size field size.
     *
     * @return the int
     */
    @Override
    default int recordSizeFieldSize() {
        return Integer.BYTES;
    }

    /**
     * Offset field size.
     *
     * @return the int
     */
    @Override
    default int offsetFieldSize() {
        return Integer.BYTES;
    }
}
