package gov.nasa.gsfc.spdf.cdfj.records;

import static gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.RecordReaders.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.function.*;

import gov.nasa.gsfc.spdf.cdfj.fields.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecords.*;
import lombok.*;
import lombok.experimental.*;

/**
 * The Class AttributeEntryDescriptorRecords.
 */
@UtilityClass
public class AttributeEntryDescriptorRecords {

    static final String AEDR_STRING_DELIMITER = "\\N ";

    /**
     * Reads G and R adrV2 records.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param agredOffset       the agred offset
     * @param nGrEntries        the n gr entries
     * @param scope             the scope
     * @param addGlobal         the add global
     * @param addREntry         the add R entry
     * 
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void readGrAedrV2s(FileChannel dotCDFFileChannel, Integer agredOffset, int nGrEntries, int scope,
            Consumer<? super AEDRV2> addGlobal, Consumer<? super AEDRV2> addREntry) throws IOException {

        Integer offset = agredOffset;

        for (int i = 0; i < nGrEntries || offset != 0; i++) {
            ByteBuffer adrBuffer = readV2Record(dotCDFFileChannel, offset);
            AEDRV2 aedrv = aedrv2(adrBuffer, scope);
            offset = aedrv.getAedrNext();

            switch (scope) {
                case ADR_GLOBAL_SCOPE:
                case ADR_GLOBAL_SCOPE_ASSUMED:
                    addGlobal.accept(aedrv);
                    break;
                case ADR_VARIABLE_SCOPE:
                case ADR_VARIABLE_SCOPE_ASSUMED:
                    addREntry.accept(aedrv);
                    break;
                default:
                    throw new IllegalArgumentException("Scope, " + scope + ", is not a valid scope ");
            }

        }

    }

    /**
     * Read Z aedr V 2 s.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param azEDRhead         the az ED rhead
     * @param nZEntries         the n Z entries
     * @param scope             the scope
     * @param addZEntry         the add Z entry
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void readZAedrV2s(FileChannel dotCDFFileChannel, Integer azEDRhead, int nZEntries, int scope,
            Consumer<? super AEDRV2> addZEntry) throws IOException {

        Integer offset = azEDRhead;

        for (int i = 0; i < nZEntries || offset != 0; i++) {
            ByteBuffer adrBuffer = readV2Record(dotCDFFileChannel, offset);
            AEDRV2 aedr = aedrv2(adrBuffer, scope);
            offset = aedr.getAedrNext();
            addZEntry.accept(aedr);
        }

    }

    /**
     * Aedrv 2.
     *
     * @param source the source
     * @param scope  the scope
     * 
     * @return the aedrv2
     */
    public static final AEDRV2 aedrv2(ByteBuffer source, int scope) {

        int recordSize = source.getInt();

        int recordType = source.getInt();

        int aedrNext = source.getInt();

        int attrNum = source.getInt();

        int dataType = source.getInt();

        int num = source.getInt();

        int numElems = source.getInt();

        int numStrings = source.getInt();

        int rfuB = source.getInt();

        int rfuC = source.getInt();

        int rfuD = source.getInt();

        int rfuE = source.getInt();

        int elementSize = CDFDataTypes.sizeInBytes(dataType);

        int valueFieldSize = elementSize * numElems;

        int posPlusValueFieldSize = source.position() + valueFieldSize;

        if (posPlusValueFieldSize != recordSize) {
            throw new IllegalArgumentException("got " + posPlusValueFieldSize + " was " + recordSize);
        }

        byte[] valueInBytes = new byte[valueFieldSize];
        source.get(valueInBytes);

        return AEDRV2.builder()
                .recordSize(recordSize)
                .recordType(recordType)
                .aedrNext(aedrNext)
                .attrNum(attrNum)
                .dataType(dataType)
                .num(num)
                .numElems(numElems)
                .numStrings(numStrings)
                .rfuB(rfuB)
                .rfuC(rfuC)
                .rfuD(rfuD)
                .rfuE(rfuE)
                .value(valueInBytes)
                .build();

    }

    /**
     * Read adr V 2 s.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param agredOffset       the agred offset
     * @param nGrEntries        the n gr entries
     * @param scope             the scope
     * @param addGlobal         the add global
     * @param addREntry         the add R entry
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void readGrAedrV3s(FileChannel dotCDFFileChannel, long agredOffset, int nGrEntries, int scope,
            Consumer<? super AEDRV3> addGlobal, Consumer<? super AEDRV3> addREntry) throws IOException {

        long offset = agredOffset;

        for (int i = 0; i < nGrEntries || offset != 0; i++) {
            ByteBuffer adrBuffer = readV3Record(dotCDFFileChannel, offset);
            AEDRV3 aedrv = aedrv3(adrBuffer, scope);
            offset = aedrv.getAedrNext();

            switch (scope) {
                case ADR_GLOBAL_SCOPE:
                case ADR_GLOBAL_SCOPE_ASSUMED:
                    addGlobal.accept(aedrv);
                    break;
                case ADR_VARIABLE_SCOPE:
                case ADR_VARIABLE_SCOPE_ASSUMED:
                    addREntry.accept(aedrv);
                    break;
                default:
                    throw new IllegalArgumentException("Scope, " + scope + ", is not a valid scope ");
            }

        }

    }

    /**
     * Read Z aedr V 3 s.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param azEDRhead         the az ED rhead
     * @param maxZEntry         the max Z entry
     * @param scope             the scope
     * @param addZEntry         the add Z entry
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void readZAedrV3s(FileChannel dotCDFFileChannel, long azEDRhead, int maxZEntry, int scope,
            Consumer<? super AEDRV3> addZEntry) throws IOException {

        long offset = azEDRhead;

        for (int i = 0; i < maxZEntry || offset != 0; i++) {
            ByteBuffer adrBuffer = readV3Record(dotCDFFileChannel, offset);
            AEDRV3 aedr = aedrv3(adrBuffer, scope);
            offset = aedr.getAedrNext();
            addZEntry.accept(aedr);
        }

    }

    /**
     * Aedrv 3.
     *
     * @param source the source
     * @param scope  the scope
     * 
     * @return the aedrv3
     */
    public static final AEDRV3 aedrv3(ByteBuffer source, int scope) {

        long recordSize = source.getLong();

        int recordType = source.getInt();

        long aedrNext = source.getLong();

        int attrNum = source.getInt();

        int dataType = source.getInt();

        int num = source.getInt();

        int numElems = source.getInt();

        int numStrings = source.getInt();

        int rfuB = source.getInt();

        int rfuC = source.getInt();

        int rfuD = source.getInt();

        int rfuE = source.getInt();

        int elementSize = CDFDataTypes.sizeInBytes(dataType);

        int valueFieldSize = elementSize * numElems;

        int posPlusValueFieldSize = source.position() + valueFieldSize;

        if (posPlusValueFieldSize != recordSize) {
            throw new IllegalArgumentException("got " + posPlusValueFieldSize + " was " + recordSize);
        }

        byte[] valueInBytes = new byte[valueFieldSize];
        source.get(valueInBytes);

        return AEDRV3.builder()
                .recordSize(recordSize)
                .recordType(recordType)
                .aedrNext(aedrNext)
                .attrNum(attrNum)
                .dataType(dataType)
                .num(num)
                .numElems(numElems)
                .numStrings(numStrings)
                .rfuB(rfuB)
                .rfuC(rfuC)
                .rfuD(rfuD)
                .rfuE(rfuE)
                .value(valueInBytes)
                .build();

    }

    /**
     * Either the value 5 which identifies this as an AgrEDR or the value 9 if
     * an AzEDR. Because zEntries were not supported until CDF V2.2, prior to
     * CDF V2.2 AzEDRs will not occur in a dotCDF file.
     * <p>
     * Signed 4-byte integer, big-endian byte ordering.
     */
    // public static final int AEDR_RECORD_TYPE = CDFRecordTypes.AEDR_RECORD_TYPE_INTERNAL_VALUE;

    public interface AEDR extends CDFRecord, Comparable<AEDR> {

        static final Comparator<AEDR> BY_ATTR_NUM_BY_NUM_BY_DATA_TYPE = Comparator.comparing(AEDR::getAttrNum)
                .thenComparing(AEDR::getNum)
                .thenComparing(AEDR::getDataType);

        /**
         * The file offset of the next AEDR. Beginning with CDF V2.1 the last AEDR
         * will contain a file offset of 0x0000000000000000 in this field (to indicate the end of the AEDRS)
         *
         * @return the aedr next
         */
        Number getAedrNext();

        /**
         * The attribute number to which this entry corresponds. Attributes are numbered beginning
         * with zero (0).
         *
         * @return the attr num
         */
        int getAttrNum();

        /**
         * Gets the data type.
         *
         * @return the data type
         */
        int getDataType();

        /**
         * This entry's number: an entry number in a global attribute, or the variable number for an
         * rVariable or zVariable in a variable attribute.
         * 
         * <p>
         * Entries are numbered beginning with zero(0).
         *
         * @return the num
         */
        int getNum();

        /**
         * The number of elements of the data type (specified by the DataType field)
         * for this entry.
         * <p>
         * For character type, i.e., CDF_CHAR or CDF_UCHAR, it’s the length of the
         * string. For numeric type, it’s the number of items, which is 1 for most cases.
         * <p>
         * However, it can be multiple items.
         * <p>
         * This field can not be zero (0) or less.
         *
         * @return the num elems
         */
        int getNumElems();

        /**
         * The number of strings in the Value field.
         * <p>
         * This applies only for string-type data from variable
         * entry.
         * <p>
         * Strings are delimited by “\\N “, a three-character string, in the Value field.
         * <p>
         * This field shows the number of strings concatenated into a single one at the Value field.
         * <p>
         * A value of 0 (from pre-3.7.0) or 1 indicates that the Value field contains a single string.
         * <p>
         * For non-string data, it should be 0.
         *
         * @return the num strings
         */
        int getNumStrings();

        /**
         * Reserved for future used. Always set to zero (0).
         *
         * @return the rfu B
         */
        int getRfuB();

        /**
         * Reserved for future used. Always set to zero (0).
         *
         * @return the rfu C
         */
        int getRfuC();

        /**
         * Reserved for future used. Always set to negative one (-1).
         *
         * @return the rfu D
         */
        int getRfuD();

        /**
         * Reserved for future used. Always set to negative one (-1).
         *
         * @return the rfu E
         */
        int getRfuE();

        /**
         * This entry's value.
         * <p>
         * This consists of the number of elements (specified by the NumElems field) of the data type (specified by the
         * DataType field).
         * <p>
         * This can be thought of as a 1-dimensional array of values (stored contiguously).
         * <p>
         * The size of this field is the product of the number of elements and the size in bytes of each element.
         * <p>
         * The encoding of the elements depends on the data encoding of the CDF (which is contained in the Encoding
         * field of the CDR).
         * <p>
         * The possible encodings are described in Section 5.3.
         *
         * @return the value
         */
        Object getValue();

        /**
         * Compare to.
         *
         * @param that the that
         * 
         * @return the int
         */
        @Override
        default int compareTo(AEDR that) {

            return BY_ATTR_NUM_BY_NUM_BY_DATA_TYPE.compare(this, that);
        }
    }

    @Value
    @NonFinal
    @SuperBuilder(toBuilder = true)
    abstract static class AbstractAEDR<RECORD_SIZE_FIELD_TYPE extends Number, OFFSET_FIELD_TYPE extends Number, VALUE_TYPE>
            implements AEDR {

        /**
         * The size in bytes of this GDR (including this field).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        RECORD_SIZE_FIELD_TYPE recordSize;

        int recordType;

        /**
         * The file offset of the next AEDR. Beginning with CDF V2.1 the last AEDR
         * will contain a file offset of 0x0000000000000000 in this field (to indicate the end of the AEDRS)
         *
         * @return the aedr next
         */
        OFFSET_FIELD_TYPE aedrNext;

        /**
         * The attribute number to which this entry corresponds. Attributes are numbered beginning
         * with zero (0).
         *
         * @return the attr num
         */
        int attrNum;

        /**
         * Gets the data type.
         *
         * @return the data type
         */
        int dataType;

        /**
         * Gets the num.
         *
         * @return the num
         */
        int num;

        /**
         * Gets the num elems.
         *
         * @return the num elems
         */
        int numElems;

        /**
         * Gets the num strings.
         *
         * @return the num strings
         */
        int numStrings;

        /**
         * Gets the rfu B.
         *
         * @return the rfu B
         */
        int rfuB;

        /**
         * Gets the rfu C.
         *
         * @return the rfu C
         */
        int rfuC;

        /**
         * Gets the rfu D.
         *
         * @return the rfu D
         */
        int rfuD;

        /**
         * Gets the rfu E.
         *
         * @return the rfu E
         */
        int rfuE;

        /**
         * Gets the value.
         *
         * @return the value
         */
        VALUE_TYPE value;
    }

    /**
     * The Class AEDRV2.
     */
    @Value
    @SuperBuilder(toBuilder = true)
    public static class AEDRV2 extends AbstractAEDR<Integer, Integer, Object> implements CDFV2LinkedRecord<AEDRV2> {

        static final Comparator<AEDRV2> BY_AEDR_NEXT = Comparator.comparing(AEDRV2::getNextRecordOffset);

        @Override
        public Integer getNextRecordOffset() {
            int next = getAedrNext();
            return next <= 0 ? Integer.MAX_VALUE : next;
        }

        @Override
        public Comparator<AEDRV2> getNextRecordOffsetComparator() {
            return BY_AEDR_NEXT;

        }
    }

    /**
     * The Class AEDRV3.
     */
    @Value
    @SuperBuilder(toBuilder = true)
    public static class AEDRV3 extends AbstractAEDR<Long, Long, Object> implements CDFV3LinkedRecord<AEDRV3> {

        static final Comparator<AEDRV3> BY_AEDR_NEXT = Comparator.comparing(AEDRV3::getNextRecordOffset);

        @Override
        public Long getNextRecordOffset() {
            return getAedrNext();
        }

        @Override
        public Comparator<AEDRV3> getNextRecordOffsetComparator() {
            return BY_AEDR_NEXT;

        }

    }

}
