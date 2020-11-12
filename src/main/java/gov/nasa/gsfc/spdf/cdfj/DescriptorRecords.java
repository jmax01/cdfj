package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Class DescriptorRecords.
 */
public final class DescriptorRecords {

    /**
     * The Interface DescriptorRecord.
     */
    interface DescriptorRecord {

        long getRecordSize();

        int getRecordType();
    }
}
