package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public interface CDF3 extends CDFCore {

    /**
     *
     */
    int MAX_STRING_SIZE = 256;

    /**
     *
     */
    int AGR_EDRHEAD_OFFSET = 20;

    /**
     *
     */
    int AZ_EDRHEAD_OFFSET = 48;

    /**
     *
     */
    int R_DIMSIZES_OFFSET = 84;

    /**
     *
     */
    int CDF_VERSION = 3;

    /**
     *
     */
    int OFFSET_NEXT_VDR = 12;

    /**
     *
     */
    int OFFSET_NEXT_ADR = 12;
    // attribute

    /**
     *
     */
    int ATTR_OFFSET_NAME = 68;

    /**
     *
     */
    int OFFSET_NEXT_AEDR = 12;

    /**
     *
     */
    int OFFSET_SCOPE = 28;

    /**
     *
     */
    int OFFSET_ENTRYNUM = 28;

    /**
     *
     */
    int ATTR_OFFSET_DATATYPE = 24;

    /**
     *
     */
    int ATTR_OFFSET_NUM_ELEMENTS = 32;

    /**
     *
     */
    int OFFSET_VALUE = 56;
    // variable

    /**
     *
     */
    int VAR_OFFSET_DATATYPE = 20;

    /**
     *
     */
    int OFFSET_MAXREC = 24;

    /**
     *
     */
    int VAR_OFFSET_NAME = 84;

    /**
     *
     */
    int OFFSET_Z_NUMDIMS = VAR_OFFSET_NAME + 256;

    /**
     *
     */
    int VAR_OFFSET_NUM_ELEMENTS = 64;

    /**
     *
     */
    int OFFSET_NUM = 68;

    /**
     *
     */
    int OFFSET_FIRST_VXR = 28;

    /**
     *
     */
    int OFFSET_FLAGS = 44;

    /**
     *
     */
    int OFFSET_SRECORDS = 48;

    /**
     *
     */
    int OFFSET_RECORDS = 12;

    /**
     *
     */
    int OFFSET_BLOCKING_FACTOR = 80;
    // data

    /**
     *
     */
    int OFFSET_NEXT_VXR = 12;

    /**
     *
     */
    int OFFSET_NENTRIES = 20;

    /**
     *
     */
    int OFFSET_NUSED = 24;

    /**
     *
     */
    int OFFSET_FIRST = 28;

    /**
     *
     */
    int OFFSET_RECORD_TYPE = 8;
    // compressed

    /**
     *
     */
    int OFFSET_CDATA = 24;

    /**
     *
     */
    int OFFSET_CSIZE = 16;
}
