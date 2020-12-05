package gov.nasa.gsfc.spdf.cdfj.records;

import java.util.Comparator;

import lombok.experimental.UtilityClass;

/**
 * The Class CDFRecords.
 */
@UtilityClass
public class CDFRecords {

    interface CDFRecord {

        /**
         * The size in bytes of this CDF record (including this field).
         */
        Number getRecordSize();

        int getRecordType();
    }

    interface CDFLinkedRecord<OFFSET_TYPE extends Number & Comparable<OFFSET_TYPE>, SELF extends CDFLinkedRecord<OFFSET_TYPE, SELF>> {

        OFFSET_TYPE getNextRecordOffset();

        Comparator<SELF> getNextRecordOffsetComparator();

        @SuppressWarnings("unchecked")
        default int compareByNextRecordOffset(SELF that) {
            return getNextRecordOffsetComparator().compare((SELF) this, that);
        }
    }

    interface CDFV2Record extends CDFRecord {

        /**
         * The size in bytes of this CDF record (including this field).
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         */
        @Override
        Integer getRecordSize();

    }

    interface CDFV2LinkedRecord<SELF extends CDFV2LinkedRecord<SELF>>
            extends CDFLinkedRecord<Integer, SELF>, CDFV2Record {}

    interface CDFV3Record extends CDFRecord {

        /**
         * The size in bytes of this CDF record (including this field).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        @Override
        Long getRecordSize();

    }

    interface CDFV3LinkedRecord<SELF extends CDFV3LinkedRecord<SELF>>
            extends CDFLinkedRecord<Long, SELF>, CDFV3Record {}

}
