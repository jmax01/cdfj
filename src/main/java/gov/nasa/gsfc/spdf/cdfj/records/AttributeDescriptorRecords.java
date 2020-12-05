package gov.nasa.gsfc.spdf.cdfj.records;

import static gov.nasa.gsfc.spdf.cdfj.records.AttributeEntryDescriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.RecordReaders.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import gov.nasa.gsfc.spdf.cdfj.*;
import gov.nasa.gsfc.spdf.cdfj.fields.*;
import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.ADRV2Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.ADRV3Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.AttributeEntryDescriptorRecords.AEDR;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.*;
import lombok.*;
import lombok.experimental.*;

/**
 * The Class AttributeDescriptorRecords.
 */
@UtilityClass
public class AttributeDescriptorRecords {

    /**
     * The value 4, which identifies this as the GDR.
     * <p>
     * Signed 4-byte integer, big-endian byte ordering.
     */
    public static final int ADR_RECORD_TYPE = CDFRecordTypes.ADR_RECORD_TYPE_INTERNAL_VALUE;

    /** The Attribute is Global scope. */
    public static final int ADR_GLOBAL_SCOPE = 1;

    /** The Attribute is Variable scope. */
    public static final int ADR_VARIABLE_SCOPE = 2;

    /** The Attribute is Global scope assumed. */
    public static final int ADR_GLOBAL_SCOPE_ASSUMED = 3;

    /** The Attribute is Variable scope assumed. */
    public static final int ADR_VARIABLE_SCOPE_ASSUMED = 4;

    /**
     * Read adr V 2 s.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param gdrv2             the gdrv 2
     * 
     * @return the list
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static NavigableSet<ADRV2> readAdrV2s(FileChannel dotCDFFileChannel, GDRV2 gdrv2) throws IOException {

        Integer adrOffset = gdrv2.getAdrHead();

        if (0 == adrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<ADRV2> adrs = new TreeSet<>();

        while (adrOffset != 0) {
            ByteBuffer adrBuffer = readV2Record(dotCDFFileChannel, adrOffset);
            ADRV2 adrv2 = adrv2(adrBuffer, dotCDFFileChannel);
            adrs.add(adrv2);
            adrOffset = adrv2.getAdrNext();
        }

        return adrs;
    }

    /**
     * Adrv 2.
     *
     * @param source            the source
     * @param dotCDFFileChannel the dot CDF file channel
     * 
     * @return the adrv2
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static final ADRV2 adrv2(ByteBuffer source, FileChannel dotCDFFileChannel) throws IOException {

        ADRV2ImplBuilder<?, ?> builder = ADRV2Impl.builder()
                .recordSize(source.getInt());

        int recordType = source.getInt();

        if (ADR_RECORD_TYPE != recordType) {
            throw new IllegalArgumentException("The supplied bytebuffer does not contain a ADR record. Record Type "
                    + "was, " + recordType + ", should be " + ADR_RECORD_TYPE);
        }

        int adrNext = source.getInt();
        int agrEdrhead = source.getInt();
        int scope = source.getInt();
        int num = source.getInt();

        int nGrEntries = source.getInt();
        int maxGrEntry = source.getInt();
        int rfua = source.getInt();
        int azEdrHead = source.getInt();
        int nzEntries = source.getInt();
        int maxZEntry = source.getInt();
        int rfuE = source.getInt();

        readGrAedrV2s(dotCDFFileChannel, agrEdrhead, nGrEntries, scope, builder::gEntry, builder::rEntry);

        readZAedrV2s(dotCDFFileChannel, azEdrHead, nzEntries, scope, builder::zEntry);

        builder.adrNext(adrNext)
                .agrEdrhead(agrEdrhead)
                .scope(scope)
                .num(num)
                .nGrEntries(nGrEntries)
                .maxGrEntry(maxGrEntry)
                .rfuA(rfua)
                .azEdrHead(azEdrHead)
                .nzEntries(nzEntries)
                .maxZEntry(maxZEntry)
                .rfuE(rfuE);

        String name = NameFields.readV3NameField(source, source.position());

        return builder.name(name)
                .build();

    }

    /**
     * Read adr V 3 s.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param gdrv3             the gdrv 3
     * 
     * @return the list
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static NavigableSet<ADRV3> readAdrV3s(FileChannel dotCDFFileChannel, GDRV3 gdrv3) throws IOException {

        Long adrOffset = gdrv3.getAdrHead();

        if (0 == adrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<ADRV3> adrs = new TreeSet<>();

        while (adrOffset != 0) {
            ByteBuffer adrBuffer = readV3Record(dotCDFFileChannel, adrOffset);
            ADRV3 adrv3 = adrv3(adrBuffer, dotCDFFileChannel);
            adrs.add(adrv3);
            adrOffset = adrv3.getAdrNext();
        }

        return adrs;
    }

    /**
     * Adrv 3.
     *
     * @param source            the source
     * @param dotCDFFileChannel the dot CDF file channel
     * 
     * @return the adrv3
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static final ADRV3 adrv3(ByteBuffer source, FileChannel dotCDFFileChannel) throws IOException {

        ADRV3ImplBuilder<?, ?> builder = ADRV3Impl.builder()
                .recordSize(source.getLong());

        int recordType = source.getInt();

        if (ADR_RECORD_TYPE != recordType) {
            throw new IllegalArgumentException("The supplied bytebuffer does not contain a ADR record. Record Type "
                    + "was, " + recordType + ", should be " + ADR_RECORD_TYPE);
        }

        long adrNext = source.getLong();
        long agrEdrhead = source.getLong();
        int scope = source.getInt();
        int num = source.getInt();

        int nGrEntries = source.getInt();
        int maxGrEntry = source.getInt();
        int rfua = source.getInt();
        long azEdrHead = source.getLong();
        int nzEntries = source.getInt();
        int maxZEntry = source.getInt();
        int rfuE = source.getInt();

        readGrAedrV3s(dotCDFFileChannel, agrEdrhead, nGrEntries, scope, builder::gEntry, builder::rEntry);

        readZAedrV3s(dotCDFFileChannel, azEdrHead, nzEntries, scope, builder::zEntry);

        builder.adrNext(adrNext)
                .agrEdrhead(agrEdrhead)
                .scope(scope)
                .num(num)
                .nGrEntries(nGrEntries)
                .maxGrEntry(maxGrEntry)
                .rfuA(rfua)
                .azEdrHead(azEdrHead)
                .nzEntries(nzEntries)
                .maxZEntry(maxZEntry)
                .rfuE(rfuE);

        String name = NameFields.readV3NameField(source, source.position());

        return builder.name(name)
                .build();

    }

    /**
     * The Interface ADR.
     */
    public interface ADR extends CDFRecord, Comparable<ADR> {

        static final Comparator<ADR> BY_NUM_BY_NAME_BY_SCOPE = Comparator.comparing(ADR::getNum)
                .thenComparing(ADR::getName)
                .thenComparing(ADR::isGlobalScope)
                .thenComparing(ADR::isVariableScope);

        /**
         * The file offset of the next ADR.
         * <p>
         * Beginning with CDF V2.1 the last ADR will
         * contain a file offset of 0x0000000000000000 in this field (to indicate the
         * end of the ADRs).
         * <p>
         * Prior to CDF V2.1 this file offset is undefined in the last ADR.
         *
         * @return the adr next
         */
        Number getAdrNext();

        /**
         * The file offset of the first Attribute g/rEntry Descriptor Record (AgrEDR)
         * for this attribute.
         * <p>
         * The first AgrEDR contains a file offset to the next AgrEDR and so on.
         * An AgrEDR will exist for each g/rEntry for this attribute. This field will
         * contain
         * 0x0000000000000000 if the attribute has no g/rEntries. Beginning with CDF
         * V2.1 the last AgrEDR will contain a file
         * offset of 0x0000000000000000 for the file offset of the next AgrEDR (to
         * indicate the end of 11the AgrEDRs).
         * <p>
         * Prior to CDF V2.1 the “next AgrEDR" file offset in the last AgrEDR is
         * undefined.
         * <p>
         * Note that the term g/rEntry is used to refer to an entry that may be either
         * a
         * gEntry or an rEntry.
         * <p>
         * The type of entry described by an AgrEDR depends on the scope of the
         * corresponding attribute.
         * <p>
         * AgrEDRs of a global-scoped attribute describe gEntries.
         * <p>
         * AgrEDRs of a variable-scoped attribute describe rEntries.
         *
         * @return the agr edrhead
         */
        Number getAgrEdrhead();

        /**
         * The intended scope of this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * The following internal values are valid:
         * <p>
         * 1 Global scope.
         * <p>
         * 2 Variable scope.
         * <p>
         * 3 Global scope assumed.
         * <p>
         * 4 Variable scope assumed.
         * <p>
         * Note that assumed scopes only exist prior to CDF V2.5.
         *
         * @return the scope
         */
        int getScope();

        /**
         * Checks if is variable scope.
         *
         * @return true, if is variable scope
         */
        default boolean isVariableScope() {

            switch (getScope()) {
                case ADR_VARIABLE_SCOPE:
                case ADR_VARIABLE_SCOPE_ASSUMED:
                    return true;
                default:
                    return false;
            }

        }

        /**
         * Checks if is not variable scope.
         *
         * @return true, if is not variable scope
         */
        default boolean isNotVariableScope() {
            return !isVariableScope();
        }

        /**
         * Checks if is global scope.
         *
         * @return true, if is global scope
         */
        default boolean isGlobalScope() {

            switch (getScope()) {
                case ADR_GLOBAL_SCOPE:
                case ADR_GLOBAL_SCOPE_ASSUMED:
                    return true;
                default:
                    return false;
            }

        }

        /**
         * Checks if is not global scope.
         *
         * @return true, if is not global scope
         */
        default boolean isNotGlobalScope() {

            return !isGlobalScope();
        }

        /**
         * This attribute's number.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * Attributes are numbered beginning with zero (0)
         *
         * @return the num
         */
        int getNum();

        /**
         * The number of g/rEntries for this attribute.
         *
         * @return the n gr entries
         */
        int getNGrEntries();

        /**
         * The maximum numbered g/rEntry for this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * g/rEntries are numbered beginning with zero (0).
         * <p>
         * If there are no g/rEntries, this field will contain negative one (-1).
         *
         * @return the max gr entry
         */
        int getMaxGrEntry();

        /**
         * Reserved for future used. Always set to zero (0).
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the rfu A
         */
        int getRfuA();

        /**
         * The file offset of the first Attribute zEntry Descriptor Record (AzEDR) for
         * this attribute.
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         * <p>
         * The first AzEDR contains a file offset to the next AzEDR and so on.
         * <p>
         * An AzEDR will exist for each zEntry for this attribute.
         * <p>
         * This field will contain 0x0000000000000000 if this attribute has no
         * zEntries.
         * <p>
         * The last AzEDR will contain a file offset of 0x0000000000000000 for the
         * file
         * offset of the next AzEDR (to indicate the end of the AzEDRs).
         *
         * @return the az edr head
         */
        Number getAzEdrHead();

        /**
         * The number of zEntries for this attribute.
         * <p>
         * Prior to CDF V2.2 this field will always contain a
         * value of zero (0).
         *
         * @return the nz entries
         */
        int getNzEntries();

        /**
         * The maximum numbered zEntry for this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * zEntries are numbered beginning with zero (0).
         * <p>
         * Prior to CDF V2.2 this field will always contain a value of negative one
         * (-1).
         *
         * @return the max Z entry
         */
        int getMaxZEntry();

        /**
         * Reserved for future used. Always set to negative one (-1).
         * Signed 4-byte integer, big-endian byte ordering.
         *
         * @return the rfu e
         */
        int getRfuE();

        /**
         * <p>
         * The name of this attribute.
         * <p>
         * Character string, ASCII character set.
         * <p>
         * This field is always 64 in V2.* or V3.* 256 bytes in length.
         * <p>
         * If the number of characters in the name is less than 64 in V2.* or V3.* 256 , a NUL character
         * (0x00) will be used to terminate the string.
         * <p>
         * In that case, the characters beyond the NUL-terminator (up to the size of
         * this field) are undefined.
         *
         * @return the name
         */
        String getName();

        /**
         * Compare to.
         *
         * @param that the that
         * 
         * @return the int
         */
        @Override
        default int compareTo(ADR that) {

            return BY_NUM_BY_NAME_BY_SCOPE.compare(this, that);
        }

        /**
         * Gets the g entries.
         *
         * @return the g entries
         */
        NavigableSet<AEDR> getGEntries();

        /**
         * Gets the r entries.
         *
         * @return the r entries
         */
        NavigableSet<AEDR> getREntries();

        /**
         * Gets the z entries.
         *
         * @return the z entries
         */
        NavigableSet<AEDR> getZEntries();
    }

    /**
     * The Interface ADRV2.
     */
    public interface ADRV2 extends ADR, CDFV2LinkedRecord<ADRV2> {

        static Comparator<ADRV2> BY_ADR_NEXT = Comparator.comparing(ADRV2::getAdrNext);

        /**
         * Gets the next record offset.
         *
         * @return the next record offset
         */
        @Override
        default Integer getNextRecordOffset() {
            return getAdrNext();
        }

        /**
         * Gets the next record offset comparator.
         *
         * @return the next record offset comparator
         */
        @Override
        default Comparator<ADRV2> getNextRecordOffsetComparator() {
            return BY_ADR_NEXT;

        }

        /**
         * The file offset of the next ADR.
         * <p>
         * Beginning with CDF V2.1 the last ADR will
         * contain a file offset of 0x0000000000000000 in this field (to indicate the
         * end of the ADRs).
         * <p>
         * Prior to CDF V2.1 this file offset is undefined in the last ADR.
         *
         * @return the adr next
         */
        @Override
        Integer getAdrNext();

        /**
         * The file offset of the first Attribute g/rEntry Descriptor Record (AgrEDR)
         * for this attribute.
         * <p>
         * The first AgrEDR contains a file offset to the next AgrEDR and so on.
         * An AgrEDR will exist for each g/rEntry for this attribute. This field will
         * contain
         * 0x0000000000000000 if the attribute has no g/rEntries. Beginning with CDF
         * V2.1 the last AgrEDR will contain a file
         * offset of 0x0000000000000000 for the file offset of the next AgrEDR (to
         * indicate the end of 11the AgrEDRs).
         * <p>
         * Prior to CDF V2.1 the “next AgrEDR" file offset in the last AgrEDR is
         * undefined.
         * <p>
         * Note that the term g/rEntry is used to refer to an entry that may be either
         * a
         * gEntry or an rEntry.
         * <p>
         * The type of entry described by an AgrEDR depends on the scope of the
         * corresponding attribute.
         * <p>
         * AgrEDRs of a global-scoped attribute describe gEntries.
         * <p>
         * AgrEDRs of a variable-scoped attribute describe rEntries.
         *
         * @return the agr edrhead
         */
        @Override
        Integer getAgrEdrhead();

        /**
         * The file offset of the first Attribute zEntry Descriptor Record (AzEDR) for
         * this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * The first AzEDR contains a file offset to the next AzEDR and so on.
         * <p>
         * An AzEDR will exist for each zEntry for this attribute.
         * <p>
         * This field will contain 0x0000000000000000 if this attribute has no
         * zEntries.
         * <p>
         * The last AzEDR will contain a file offset of 0x0000000000000000 for the
         * file
         * offset of the next AzEDR (to indicate the end of the AzEDRs).
         *
         * @return the az edr head
         */
        @Override
        Integer getAzEdrHead();

    }

    /**
     * The Interface ADRV3.
     */
    public interface ADRV3 extends ADR, CDFV3LinkedRecord<ADRV3> {

        static Comparator<ADRV3> BY_ADR_NEXT = Comparator.comparing(ADRV3::getAdrNext);

        /**
         * Gets the next record offset.
         *
         * @return the next record offset
         */
        @Override
        default Long getNextRecordOffset() {
            return getAdrNext();
        }

        /**
         * Gets the next record offset comparator.
         *
         * @return the next record offset comparator
         */
        @Override
        default Comparator<ADRV3> getNextRecordOffsetComparator() {
            return BY_ADR_NEXT;

        }

        /**
         * The file offset of the next ADR.
         * <p>
         * Beginning with CDF V2.1 the last ADR will
         * contain a file offset of 0x0000000000000000 in this field (to indicate the
         * end of the ADRs).
         * <p>
         * Prior to CDF V2.1 this file offset is undefined in the last ADR.
         *
         * @return the adr next
         */
        @Override
        Long getAdrNext();

        /**
         * The file offset of the first Attribute g/rEntry Descriptor Record (AgrEDR)
         * for this attribute.
         * <p>
         * The first AgrEDR contains a file offset to the next AgrEDR and so on.
         * An AgrEDR will exist for each g/rEntry for this attribute. This field will
         * contain
         * 0x0000000000000000 if the attribute has no g/rEntries. Beginning with CDF
         * V2.1 the last AgrEDR will contain a file
         * offset of 0x0000000000000000 for the file offset of the next AgrEDR (to
         * indicate the end of 11the AgrEDRs).
         * <p>
         * Prior to CDF V2.1 the “next AgrEDR" file offset in the last AgrEDR is
         * undefined.
         * <p>
         * Note that the term g/rEntry is used to refer to an entry that may be either
         * a
         * gEntry or an rEntry.
         * <p>
         * The type of entry described by an AgrEDR depends on the scope of the
         * corresponding attribute.
         * <p>
         * AgrEDRs of a global-scoped attribute describe gEntries.
         * <p>
         * AgrEDRs of a variable-scoped attribute describe rEntries.
         *
         * @return the agr edrhead
         */
        @Override
        Long getAgrEdrhead();

        /**
         * The file offset of the first Attribute zEntry Descriptor Record (AzEDR) for
         * this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * The first AzEDR contains a file offset to the next AzEDR and so on.
         * <p>
         * An AzEDR will exist for each zEntry for this attribute.
         * <p>
         * This field will contain 0x0000000000000000 if this attribute has no
         * zEntries.
         * <p>
         * The last AzEDR will contain a file offset of 0x0000000000000000 for the
         * file
         * offset of the next AzEDR (to indicate the end of the AzEDRs).
         *
         * @return the az edr head
         */
        @Override
        Long getAzEdrHead();

    }

    @Value
    @NonFinal
    @SuperBuilder(toBuilder = true)
    abstract static class AbstractADR<RECORD_SIZE_FIELD_TYPE extends Number, OFFSET_FIELD_TYPE extends Number>
            implements ADR {

        /**
         * The size in bytes of this GDR (including this field).
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         */
        RECORD_SIZE_FIELD_TYPE recordSize;

        @Builder.Default
        int recordType = ADR_RECORD_TYPE;

        /**
         * The file offset of the next ADR.
         * <p>
         * Beginning with CDF V2.1 the last ADR will
         * contain a file offset of 0x0000000000000000 in this field (to indicate the
         * end of the ADRs).
         * <p>
         * Prior to CDF V2.1 this file offset is undefined in the last ADR.
         */
        OFFSET_FIELD_TYPE adrNext;

        /**
         * The file offset of the first Attribute g/rEntry Descriptor Record (AgrEDR)
         * for this attribute.
         * <p>
         * The first AgrEDR contains a file offset to the next AgrEDR and so on.
         * An AgrEDR will exist for each g/rEntry for this attribute. This field will
         * contain
         * 0x0000000000000000 if the attribute has no g/rEntries. Beginning with CDF
         * V2.1 the last AgrEDR will contain a file
         * offset of 0x0000000000000000 for the file offset of the next AgrEDR (to
         * indicate the end of 11the AgrEDRs).
         * <p>
         * Prior to CDF V2.1 the “next AgrEDR" file offset in the last AgrEDR is
         * undefined.
         * <p>
         * Note that the term g/rEntry is used to refer to an entry that may be either
         * a
         * gEntry or an rEntry.
         * <p>
         * The type of entry described by an AgrEDR depends on the scope of the
         * corresponding attribute.
         * <p>
         * AgrEDRs of a global-scoped attribute describe gEntries.
         * <p>
         * AgrEDRs of a variable-scoped attribute describe rEntries.
         *
         * @return the agr edrhead
         */
        OFFSET_FIELD_TYPE agrEdrhead;

        /**
         * The intended scope of this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * The following internal values are valid:
         * <p>
         * 1 Global scope.
         * <p>
         * 2 Variable scope.
         * <p>
         * 3 Global scope assumed.
         * <p>
         * 4 Variable scope assumed.
         * <p>
         * Note that assumed scopes only exist prior to CDF V2.5.
         */
        int scope;

        /**
         * This attribute's number.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * Attributes are numbered beginning with zero (0)
         */
        int num;

        /**
         * The number of g/rEntries for this attribute.
         */
        int nGrEntries;

        /**
         * The maximum numbered g/rEntry for this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * g/rEntries are numbered beginning with zero (0).
         * <p>
         * If there are no g/rEntries, this field will contain negative one (-1).
         */
        int maxGrEntry;

        /**
         * Reserved for future used. Always set to zero (0).
         * Signed 4-byte integer, big-endian byte ordering.
         */
        int rfuA;

        /**
         * The file offset of the first Attribute zEntry Descriptor Record (AzEDR) for
         * this attribute.
         * <p>
         * Signed 8-byte integer, big-endian byte ordering.
         * <p>
         * The first AzEDR contains a file offset to the next AzEDR and so on.
         * <p>
         * An AzEDR will exist for each zEntry for this attribute.
         * <p>
         * This field will contain 0x0000000000000000 if this attribute has no
         * zEntries.
         * <p>
         * The last AzEDR will contain a file offset of 0x0000000000000000 for the
         * file
         * offset of the next AzEDR (to indicate the end of the AzEDRs).
         */
        OFFSET_FIELD_TYPE azEdrHead;

        /**
         * The number of zEntries for this attribute.
         * <p>
         * Prior to CDF V2.2 this field will always contain a
         * value of zero (0).
         */
        int nzEntries;

        /**
         * The maximum numbered zEntry for this attribute.
         * <p>
         * Signed 4-byte integer, big-endian byte ordering.
         * <p>
         * zEntries are numbered beginning with zero (0).
         * <p>
         * Prior to CDF V2.2 this field will always contain a value of negative one
         * (-1).
         */
        int maxZEntry;

        /**
         * Reserved for future used. Always set to negative one (-1).
         * Signed 4-byte integer, big-endian byte ordering.
         */
        int rfuE;

        /**
         * <p>
         * The name of this attribute.
         * <p>
         * Character string, ASCII character set.
         * <p>
         * This field is always 64 in V2.* or V3.* 256 bytes in length.
         * <p>
         * If the number of characters in the name is less than 64 in V2.* or V3.* 256 , a NUL character
         * (0x00) will be used to terminate the string.
         * <p>
         * In that case, the characters beyond the NUL-terminator (up to the size of
         * this field) are undefined.
         */
        String name;

        @Singular
        NavigableSet<AEDR> gEntries;

        @Singular
        NavigableSet<AEDR> rEntries;

        @Singular
        NavigableSet<AEDR> zEntries;

    }

    /**
     * The Class ADRV2Impl.
     */
    @Value
    @SuperBuilder(toBuilder = true)
    public static class ADRV2Impl extends AbstractADR<Integer, Integer> implements ADRV2 {

    }

    /**
     * The Class ADRV3Impl.
     */
    @Value
    @SuperBuilder(toBuilder = true)
    public static class ADRV3Impl extends AbstractADR<Long, Long> implements ADRV3 {

    }
}
