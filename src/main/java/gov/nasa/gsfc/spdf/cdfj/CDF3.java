package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Interface CDF3.
 *
 * @author nand
 */
public interface CDF3 extends CDFCore {

    /** The max string size. */
    int MAX_STRING_SIZE = 256;

    /** The agr edrhead offset. */
    int AGR_EDRHEAD_OFFSET = 20;

    /** The az edrhead offset. */
    int AZ_EDRHEAD_OFFSET = 48;

    /** The r dimsizes offset. */
    int R_DIMSIZES_OFFSET = 84;

    /** The cdf version. */
    int CDF_VERSION = 3;

    /** The offset next vdr. */
    int OFFSET_NEXT_VDR = 12;

    /** The offset next adr. */
    int OFFSET_NEXT_ADR = 12;

    /** The attr offset name. */
    int ATTR_OFFSET_NAME = 68;

    /** The offset next aedr. */
    int OFFSET_NEXT_AEDR = 12;

    /** The offset scope. */
    int OFFSET_SCOPE = 28;

    /** The offset entrynum. */
    int OFFSET_ENTRYNUM = 28;

    /** The attr offset datatype. */
    int ATTR_OFFSET_DATATYPE = 24;

    /** The attr offset num elements. */
    int ATTR_OFFSET_NUM_ELEMENTS = 32;

    /** The offset value. */
    int OFFSET_VALUE = 56;

    /** The var offset datatype. */
    int VAR_OFFSET_DATATYPE = 20;

    /** The offset maxrec. */
    int OFFSET_MAXREC = 24;

    /** The var offset name. */
    int VAR_OFFSET_NAME = 84;

    /** The offset z numdims. */
    int OFFSET_Z_NUMDIMS = VAR_OFFSET_NAME + 256;

    /** The var offset num elements. */
    int VAR_OFFSET_NUM_ELEMENTS = 64;

    /** The offset num. */
    int OFFSET_NUM = 68;

    /** The offset first vxr. */
    int OFFSET_FIRST_VXR = 28;

    /** The offset flags. */
    int OFFSET_FLAGS = 44;

    /** The offset srecords. */
    int OFFSET_SRECORDS = 48;

    /** The offset records. */
    int OFFSET_RECORDS = 12;

    /** The offset blocking factor. */
    int OFFSET_BLOCKING_FACTOR = 80;

    /** The offset next vxr. */
    int OFFSET_NEXT_VXR = 12;

    /** The offset nentries. */
    int OFFSET_NENTRIES = 20;

    /** The offset nused. */
    int OFFSET_NUSED = 24;

    /** The offset first. */
    int OFFSET_FIRST = 28;

    /** The offset record type. */
    int OFFSET_RECORD_TYPE = 8;

    /** The offset cdata. */
    int OFFSET_CDATA = 24;

    /** The offset csize. */
    int OFFSET_CSIZE = 16;

    /**
     * Max string size.
     *
     * @return the int
     */
    @Override
    default int maxStringSize() {
        return MAX_STRING_SIZE;
    }
}
