package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public interface CDF2 extends CDFCore {

    /**
     *
     */
    int MAX_STRING_SIZE = 64;

    /**
     *
     */
    int AGR_EDRHEAD_OFFSET = 12;

    /**
     *
     */
    int AZ_EDRHEAD_OFFSET = 36;

    /**
     *
     */
    int R_DIMSIZES_OFFSET = 60;

    /**
     *
     */
    int CDF_VERSION = 2;

    /**
     *
     */
    int OFFSET_NEXT_VDR = 8;

    /**
     *
     */
    int OFFSET_NEXT_ADR = 8;
    // attribute

    /**
     *
     */
    int ATTR_OFFSET_NAME = 52;

    /**
     *
     */
    int OFFSET_NEXT_AEDR = 8;

    /**
     *
     */
    int OFFSET_SCOPE = 16;

    /**
     *
     */
    int OFFSET_ENTRYNUM = 20;

    /**
     *
     */
    int ATTR_OFFSET_DATATYPE = 16;

    /**
     *
     */
    int ATTR_OFFSET_NUM_ELEMENTS = 24;

    /**
     *
     */
    int OFFSET_VALUE = 48;
    // variable

    /**
     *
     */
    int VAR_OFFSET_DATATYPE = 12;

    /**
     *
     */
    int OFFSET_MAXREC = 16;
    /*
     * public final int VAR_OFFSET_NAME = 64;
     * public final int OFFSET_zNumDims = VAR_OFFSET_NAME + MAX_STRING_SIZE;
     * public final int VAR_OFFSET_NUM_ELEMENTS = 48;
     * public final int OFFSET_NUM = 52;
     */

    /**
     *
     */

    int OFFSET_FIRST_VXR = 20;

    /**
     *
     */
    int OFFSET_FLAGS = 28;

    /**
     *
     */
    int OFFSET_SRECORDS = 32;

    /**
     *
     */
    int OFFSET_RECORDS = 8;

    /**
     *
     */
    int OFFSET_BLOCKING_FACTOR = 60;
    // data

    /**
     *
     */
    int OFFSET_NEXT_VXR = 8;

    /**
     *
     */
    int OFFSET_NENTRIES = 12;

    /**
     *
     */
    int OFFSET_NUSED = 16;

    /**
     *
     */
    int OFFSET_FIRST = 20;

    /**
     *
     */
    int OFFSET_RECORD_TYPE = 4;
    // compressed

    /**
     *
     */
    int OFFSET_CDATA = 16;

    /**
     *
     */
    int OFFSET_CSIZE = 12;
}
