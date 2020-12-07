package gov.nasa.gsfc.spdf.cdfj.records;

import static gov.nasa.gsfc.spdf.cdfj.records.RecordReaders.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import gov.nasa.gsfc.spdf.cdfj.*;
import gov.nasa.gsfc.spdf.cdfj.fields.*;
import gov.nasa.gsfc.spdf.cdfj.fields.CDFDataTypes;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.VariableDecriptorRecords.VDRV2Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.VariableDecriptorRecords.VDRV3Impl.*;
import lombok.*;
import lombok.experimental.*;

/**
 * The Class VariableDecriptorRecords.
 */
@UtilityClass
public class VariableDecriptorRecords {

    public static final int Z_VDR_RECORD_TYPE = CDFRecordTypes.Z_VDR_RECORD_TYPE_INTERNAL_VALUE;

    public static final int R_VDR_RECORD_TYPE = CDFRecordTypes.R_VDR_RECORD_TYPE_INTERNAL_VALUE;

    /**
     * The record dimension variance of this variable. Set indicates TRUE, the record has dimension variance.
     * Clear indicates a FALSE the record does not have dimension variance.
     */
    public static final int HAS_DIMENSION_VARIANCE = 0x00000001;

    /**
     * Whether or not a pad value is specified for this variable. Set indicates that a
     * pad value has been specified. Clear indicates that a pad value has not been
     * specified.
     */
    public static final int HAS_PAD_VALUE = 0x00000002;

    /**
     * Whether or not a compression method might be applied to this variable data.
     * Set indicates that a compression is chosen by the user and the data might be
     * compressed, depending on the data size and content. If the compressed data
     * becomes larger than its uncompressed data, no compression is applied and the
     * data are stored as uncompressed, even the compression bit is set. The
     * compressed data is stored in Compressed Variable Value Record (CVVR)
     * while uncompressed data go into Variable Value Record (VVR). Clear
     * indicates that a compression will not be used. The CPRorSPRoffset field
     * described below provides the offset of the Compressed Parameters Record if
     * this compression bit is set and the compression used.
     */
    public static final int IS_COMPRESSED = 0x00000004;

    /** The Constant DIM_VARIANCE_IS_TRUE. */
    public static final int DIM_VARIANCE_IS_TRUE = -1;

    public static NavigableSet<VDRV2> readRVdrV2s(FileChannel dotCDFFileChannel, GDRV2 gdrv2) throws IOException {

        int vdrOffset = gdrv2.getRVDRHead();

        if (0 == vdrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<VDRV2> vdrs = new TreeSet<>();

        while (vdrOffset != 0) {
            ByteBuffer zvdrBuffer = readV2Record(dotCDFFileChannel, vdrOffset);
            VDRV2 vdr = vdrv2(zvdrBuffer, dotCDFFileChannel, gdrv2::getRNumDims);
            vdrs.add(vdr);
            vdrOffset = vdr.getVdrNext();
        }

        return vdrs;
    }

    public static NavigableSet<VDRV2> readZVdrV2s(FileChannel dotCDFFileChannel, GDRV2 gdrv2) throws IOException {

        int vdrOffset = gdrv2.getZVDRHead();

        if (0 == vdrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<VDRV2> vdrs = new TreeSet<>();

        while (vdrOffset != 0) {
            ByteBuffer zvdrBuffer = readV2Record(dotCDFFileChannel, vdrOffset);
            VDRV2 vdr = vdrv2(zvdrBuffer, dotCDFFileChannel, gdrv2::getRNumDims);
            vdrs.add(vdr);
            vdrOffset = vdr.getVdrNext();
        }

        return vdrs;
    }

    public static NavigableSet<VDRV3> readRVdrV3s(FileChannel dotCDFFileChannel, GDRV3 gdrv3) throws IOException {

        Long vdrOffset = gdrv3.getRVDRHead();

        if (0 == vdrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<VDRV3> vdrs = new TreeSet<>();

        while (vdrOffset != 0) {
            ByteBuffer zvdrBuffer = readV3Record(dotCDFFileChannel, vdrOffset);
            VDRV3 vdr = vdrv3(zvdrBuffer, dotCDFFileChannel, gdrv3::getRNumDims);
            vdrs.add(vdr);
            vdrOffset = vdr.getVdrNext();
        }

        return vdrs;
    }

    /**
     * Read Z vdr V 3 s.
     *
     * @param dotCDFFileChannel the dot CDF file channel
     * @param gdrv3             the gdrv 3
     * 
     * @return the navigable set
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static NavigableSet<VDRV3> readZVdrV3s(FileChannel dotCDFFileChannel, GDRV3 gdrv3) throws IOException {

        Long vdrOffset = gdrv3.getZVDRHead();

        if (0 == vdrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<VDRV3> vdrs = new TreeSet<>();

        while (vdrOffset != 0) {
            ByteBuffer zvdrBuffer = readV3Record(dotCDFFileChannel, vdrOffset);
            VDRV3 vdr = vdrv3(zvdrBuffer, dotCDFFileChannel, gdrv3::getRNumDims);
            vdrs.add(vdr);
            vdrOffset = vdr.getVdrNext();
        }

        return vdrs;
    }

    /**
     * VDRv 2.
     *
     * @param source            the source
     * @param dotCDFFileChannel the dot CDF file channel
     *
     * @return the VDRv2
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static final VDRV2 vdrv2(ByteBuffer source, FileChannel dotCDFFileChannel, IntSupplier rNumDims) {

        VDRV2ImplBuilder<?, ?> builder = VDRV2Impl.builder()
                .recordSize(source.getInt());

        int recordType = source.getInt();

        switch (recordType) {
            case R_VDR_RECORD_TYPE:
            case Z_VDR_RECORD_TYPE:

                break;
            default:
                throw new IllegalArgumentException("The supplied bytebuffer does not contain a VDR record. Record Type "
                        + "was, " + recordType + ", should be " + R_VDR_RECORD_TYPE + " or " + Z_VDR_RECORD_TYPE);
        }

        builder.recordType(recordType);
        int vdrNext = source.getInt();
        int dataType = source.getInt();
        int maxRec = source.getInt();
        int vxrHead = source.getInt();
        int vxrTail = source.getInt();
        int flags = source.getInt();
        int sRecords = source.getInt();
        int rfuB = source.getInt();
        int rfuC = source.getInt();
        int rfuF = source.getInt();
        int numElems = source.getInt();
        int num = source.getInt();
        int cprOrSprOffset = source.getInt();

        int blockingFactor = source.getInt();
        String name = NameFields.readV2NameField(source);

        builder.vdrNext(vdrNext)
                .dataType(dataType)
                .maxRec(maxRec)
                .vxrHead(vxrHead)
                .vxrTail(vxrTail)
                .flags(flags)
                .sRecords(sRecords)
                .rfuB(rfuB)
                .rfuC(rfuC)
                .rfuF(rfuF)
                .numElems(numElems)
                .num(num)
                .cprOrSprOffset(cprOrSprOffset)
                .blockingFactor(blockingFactor)
                .name(name);

        if (Z_VDR_RECORD_TYPE == recordType) {
            int zNumDims = source.getInt();
            builder.zNumDims(zNumDims);
            int[] zDimSizes = new int[zNumDims];
            source.asIntBuffer()
                    .get(zDimSizes);
            builder.zDimSizes(zDimSizes);
            int[] dimVarys = new int[zNumDims];
            source.position(source.position() + (zNumDims * Integer.BYTES))
                    .asIntBuffer()
                    .get(dimVarys);
            builder.dimVarys(dimVarys);
        }

        if (R_VDR_RECORD_TYPE == recordType) {

            int numDims = rNumDims.getAsInt();
            int[] dimVarys = new int[numDims];
            source.asIntBuffer()
                    .get(dimVarys);
            builder.dimVarys(dimVarys)
                    .rNumDims(numDims);
            source.position(source.position() + (numDims * Integer.BYTES));

        }

        if ((flags & HAS_PAD_VALUE) != 0) {
            int padValueSize = numElems * CDFDataTypes.sizeInBytes(dataType);

            byte[] padValueAsBytes = new byte[padValueSize];
            source.get(padValueAsBytes);

            builder.padValue(padValueAsBytes);
        } else {
            builder.padValue(new byte[0]);
        }

        return builder.build();
    }

    /**
     * VDRv 3.
     *
     * @param source            the source
     * @param dotCDFFileChannel the dot CDF file channel
     *
     * @return the VDRv3
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static final VDRV3 vdrv3(ByteBuffer source, FileChannel dotCDFFileChannel, IntSupplier rNumDims) {

        VDRV3ImplBuilder<?, ?> builder = VDRV3Impl.builder()
                .recordSize(source.getLong());

        int recordType = source.getInt();

        switch (recordType) {
            case R_VDR_RECORD_TYPE:
            case Z_VDR_RECORD_TYPE:

                break;
            default:
                throw new IllegalArgumentException("The supplied bytebuffer does not contain a VDR record. Record Type "
                        + "was, " + recordType + ", should be " + R_VDR_RECORD_TYPE + " or " + Z_VDR_RECORD_TYPE);
        }

        builder.recordType(recordType);

        long vdrNext = source.getLong();
        int dataType = source.getInt();
        int maxRec = source.getInt();
        long vxrHead = source.getLong();
        long vxrTail = source.getLong();
        int flags = source.getInt();
        int sRecords = source.getInt();
        int rfuB = source.getInt();
        int rfuC = source.getInt();
        int rfuF = source.getInt();
        int numElems = source.getInt();
        int num = source.getInt();
        long cprOrSprOffset = source.getLong();
        int blockingFactor = source.getInt();

        String name = NameFields.readV3NameField(source);

        builder.vdrNext(vdrNext)
                .dataType(dataType)
                .maxRec(maxRec)
                .vxrHead(vxrHead)
                .vxrTail(vxrTail)
                .flags(flags)
                .sRecords(sRecords)
                .rfuB(rfuB)
                .rfuC(rfuC)
                .rfuF(rfuF)
                .numElems(numElems)
                .num(num)
                .cprOrSprOffset(cprOrSprOffset)
                .blockingFactor(blockingFactor)
                .name(name);

        if (Z_VDR_RECORD_TYPE == recordType) {
            int zNumDims = source.getInt();
            builder.zNumDims(zNumDims);
            int[] zDimSizes = new int[zNumDims];
            source.asIntBuffer()
                    .get(zDimSizes);
            builder.zDimSizes(zDimSizes);
            int[] dimVarys = new int[zNumDims];

            source.position(source.position() + (zNumDims * Integer.BYTES))
                    .asIntBuffer()
                    .get(dimVarys);
            builder.dimVarys(dimVarys);
        }

        if (R_VDR_RECORD_TYPE == recordType) {

            int numDims = rNumDims.getAsInt();
            int[] dimVarys = new int[numDims];
            source.asIntBuffer()
                    .get(dimVarys);
            builder.dimVarys(dimVarys);
            source.position(source.position() + (numDims * Integer.BYTES));
        }

        if ((flags & HAS_PAD_VALUE) != 0) {
            int padValueSize = numElems * CDFDataTypes.sizeInBytes(dataType);

            byte[] padValueAsBytes = new byte[padValueSize];
            source.get(padValueAsBytes);

            builder.padValue(padValueAsBytes);
        } else {
            builder.padValue(new byte[0]);
        }

        return builder.build();
    }

    /**
     * The Interface VDR.
     */
    public interface VDR extends CDFRecord, Comparable<VDR> {

        static final Comparator<VDR> BY_VDR_TYPE_BY_NUM = Comparator.comparing(VDR::getRecordType)
                .thenComparing(VDR::getNum);

        /**
         * The file offset of the next VDR.
         *
         * @return the next VDR
         */
        Number getVdrNext();

        /**
         * The data type of this entry.
         *
         * @return data type of this entry.
         */
        int getDataType();

        /**
         * The maximum record number physically written to this variable.
         * <p>
         * This is the last written record number.
         * <p>
         * More records might be allocated after this record so future written record(s)
         * can be in contiguous form to eliminate the potential data fragmentation.
         * <p>
         * Variable records are numbered beginning at zero (0).
         * <p>
         * If no records have been written to this variable, this field will contain negative one (-1).
         *
         * @return the max rec
         */
        int getMaxRec();

        /**
         * The file offset of the first Variable Index Record (VXR).
         * <p>
         * VXRs are used in single-file CDFs
         * to store the locations of Variable Value Records (VVRs).
         * <p>
         * VVRs are used to store variable records in single-file CDFs.
         * <p>
         * VXRs are described in Section 2.7 and VVRs are described in Section 2.8.
         * <p>
         * The first VXR contains the file offset of the next VXR and so on.
         * <p>
         * The last VXR contains a file offset of 0x00000000 for the file offset of the next VXR (to indicate the end of
         * the VXRs).
         * <p>
         * In single-file CDFs, if no records have been written to this variable, this field will contain a file offset
         * of 0x0000000000000000.
         * <p>
         * For multi-file CDFs variable records are stored in separate files and this field will always contain a file
         * offset of 0x00000000.
         *
         * @return the VXR head offset
         */
        Number getVxrHead();

        /**
         * The file offset of the last VXR.
         * <p>
         * See the VXRhead field above for a description of VXRs.
         *
         * @return the VXT tail offset
         */
        Number getVxrTail();

        /**
         * Boolean flags, one per bit, describing some aspect of this variable.
         * <p>
         * Bit numbering is described in Chapter 5.
         * <p>
         * The meaning of each bit is as follows...
         * <p>
         * 0: The record variance of this variable. Set indicates a TRUE record variance. Clear indicates a FALSE record
         * variance
         * <p>
         * <p>
         * 1: Whether or not a pad value is specified for this variable.
         * Set indicates that a pad value has been specified.
         * Clear indicates that a pad value has not been specified.
         * The PadValue field described below is only present if a pad value has been specified.
         * <p>
         * <p>
         * 2: Whether or not a compression method might be applied to this variable data.
         * Set indicates that a compression is chosen by the user and the data might be compressed, depending on the
         * data size and content.
         * If the compressed data becomes larger than its uncompressed data, no compression is applied and the data are
         * stored as uncompressed, even the compression bit is set.
         * The compressed data is stored in Compressed Variable Value Record (CVVR) while uncompressed data go into
         * Variable Value Record (VVR).
         * Clear indicates that a compression will not be used.
         * The CPRorSPRoffset field described below provides the offset of the Compressed Parameters Record if this
         * compression bit is set and the compression used
         * <p>
         * <p>
         * 3-31: Reserved for future use. These bits are always clear.
         * 
         * @return the flags
         */
        int getFlags();

        /**
         * True, this record has dimension variance
         *
         * @return true, this record has dimension variance
         */
        default boolean hasDimensionVariance() {
            return (getFlags() & HAS_DIMENSION_VARIANCE) != 0;
        }

        /**
         * True, this record does not have dimension variance
         *
         * @return true, this record does not have dimension variance
         */
        default boolean doesNotHaveDimensionVariance() {
            return (getFlags() & HAS_DIMENSION_VARIANCE) == 0;
        }

        /**
         * True, this record has a pad value
         *
         * @return true, this record has a pad value
         */
        default boolean hasPadValue() {
            return (getFlags() & HAS_PAD_VALUE) != 0;
        }

        /**
         * True, this record does not have a pad value
         *
         * @return true, this record does not have a pad value
         */
        default boolean doesNotHavePadValue() {
            return (getFlags() & HAS_PAD_VALUE) == 0;
        }

        /**
         * Checks if the variable record is compressed.
         *
         * @return true, if is compressed
         */
        default boolean isCompressed() {
            return (getFlags() & IS_COMPRESSED) != 0;
        }

        /**
         * Checks if the variable record is not compressed.
         *
         * @return true, if is not compressed
         */
        default boolean isNotCompressed() {
            return (getFlags() & IS_COMPRESSED) == 0;
        }

        /**
         * Type of sparse records: no sparserecords, padded sparserecords (using the default/defined
         * pad value), or previous sparserecords (using the last written value).
         * <p>
         * When reading a record(s) from a variable with sparserecords that is not written, data value(s) will be
         * returned based on the type of sparse records.
         * <p>
         * In this case, a non-zero, but positive status code will be returned, indicating a virtual record(s) is
         * involved.
         * <p>
         * A variable with sparserecords tends to be less efficient than a variable of non-sparserecords.
         * <p>
         * Try to limit the number of sparserecords if possible.
         *
         * @return the sparse records
         */
        int getSRecords();

        /**
         * Reserved for future used. Always set to zero (0).
         *
         * @return the rfu B
         */
        int getRfuB();

        /**
         * Reserved for future used. Always set to zero (-1).
         *
         * @return the rfu C
         */
        int getRfuC();

        /**
         * Reserved for future used. Always set to zero (-1).
         *
         * @return the rfu F
         */
        int getRfuF();

        /**
         * The number of elements of the data type (specified by the DataType field) for this variable at each value.
         * <p>
         * For character type, i.e., CDF_CHAR or CDF_UCHAR, it’s the length of the string.
         * <p>
         * For numeric type, it’s the number of items, which is 1 for most cases.
         * <p>
         * However, it can be multiple items.
         * <p>
         * This field can not be zero (0) or less.
         *
         * @return the num elems
         */
        int getNumElems();

        /**
         * This variable's number.
         * <p>
         * Variables are numbered beginning with zero (0).
         * <p>
         * Note that rVariables and zVariables are each numbered beginning with zero (0) and are considered two separate
         * groups of variables.
         *
         * @return the variable num
         */
        int getNum();

        /**
         * <p>
         * CPR/SPR offset depending on bits set in 'Flags' and compression used.
         * <p>
         * If neither compression nor sparse arrays, set to 0xFFFFFFFFFFFFFFFF.
         *
         * @return the CPR or SPR offset
         */
        Number getCprOrSprOffset();

        /**
         * The blocking factor for this variable
         * 
         * @return the blocking factor
         */
        int getBlockingFactor();

        /**
         * The name of this variable.
         * <p>
         * This field is 64 or 256 in length.
         * <p>
         * If the number of characters in the name is less than full length of the field, a NUL character (0x00) will be
         * used to terminate
         * the string.
         * <p>
         * In that case, the characters beyond the NUL-terminator (up to the size of this field) are undefined.
         *
         * @return the name
         */
        String getName();

        /**
         * The number of dimensions for this zVariable. This field will not be present if this is an
         * rVDR (rVariable).
         *
         * @return the number of dimensions if this is a zVariable
         */
        int getZNumDims();

        /**
         * The number of dimensions for this zVariable. This field will not be present if this is an
         * rVDR (rVariable).
         *
         * @return the number of dimensions if this is a zVariable
         */
        default OptionalInt zNumDims() {

            return isZVariable() ? OptionalInt.of(getZNumDims()) : OptionalInt.empty();

        }

        /**
         * Zero or more contiguous dimension sizes for this zVariable depending on the value of the
         * zNumDims field.
         * <p>
         * This field will not be present if this is an rVDR (rVariable).
         *
         * @return the size of dimensions if this is a zVariable
         */
        int[] getZDimSizes();

        /**
         * Zero or more contiguous dimension variances.
         * <p>
         * If this is an rVDR, the number of dimension variances will correspond to the value of the rNumDims field of
         * the GDR.
         * <p>
         * If this is a zVDR, the number of dimension variances will correspond to the value of the zNumDims field in
         * this zVDR.
         * <p>
         * A value of negative one (-1) indicates a TRUE dimension variance and a value of zero (0) indicates a FALSE
         * dimension variance.
         *
         * @return the dim varys
         */
        int[] getDimVarys();

        /**
         * Dim varys.
         *
         * @return the list
         */
        default List<Boolean> dimVarys() {

            return Collections.unmodifiableList(Arrays.stream(getDimVarys())
                    .mapToObj(d -> d == DIM_VARIANCE_IS_TRUE)
                    .collect(Collectors.toList()));
        }

        /**
         * The variable's pad value.
         * <p>
         * If bit 1 of the Flags field of this VDR is clear, then a pad value has not been specified for this variable
         * and this field will not be present.
         * <p>
         * If a pad value has been specified, the size of this field depends on the number of elements and the size of
         * the data type.
         * <p>
         * The encoding of the elements depends on the encoding of the CDF (which is contained in the Encoding field of
         * the CDR).
         *
         * @return the pad value
         */
        byte[] getPadValue();

        /**
         * The variable's pad value.
         *
         * @return The variable's pad value or empty if this variable doesn't have a pad value.
         */
        default Optional<byte[]> padValue() {

            return this.hasPadValue() ? Optional.of(getPadValue()) : Optional.empty();

        }

        /**
         * the number of dimensions if this is an rVariable, from {@link GDR#getRDimSizes()}.
         *
         * @return the number of dimensions if this is an rVariable
         */
        int getRNumDims();

        /**
         * the number of dimensions if this is an rVariable, from {@link GDR#getRDimSizes()}.
         *
         * @return the number of dimensions if this is an rVariable
         */
        default OptionalInt rNumDims() {
            return getRNumDims() == 0 ? OptionalInt.empty() : OptionalInt.of(getRNumDims());
        }

        /**
         * Checks if is z variable.
         *
         * @return true, if is z variable
         */
        default boolean isZVariable() {
            return getRecordType() == Z_VDR_RECORD_TYPE;
        }

        /**
         * Checks if is r variable.
         *
         * @return true, if is r variable
         */
        default boolean isRVariable() {
            return getRecordType() == R_VDR_RECORD_TYPE;
        }

        /**
         * Compare to.
         *
         * @param that the that
         * 
         * @return the int
         */
        @Override
        default int compareTo(VDR that) {

            return BY_VDR_TYPE_BY_NUM.compare(this, that);
        }
    }

    /**
     * The Interface VDRV2.
     */
    public interface VDRV2 extends VDR, CDFV2LinkedRecord<VDRV2> {

        static Comparator<VDRV2> BY_VDR_NEXT = Comparator.comparing(VDRV2::getVdrNext);

        @Override
        default Integer getNextRecordOffset() {
            return getVdrNext();
        }

        @Override
        default Comparator<VDRV2> getNextRecordOffsetComparator() {
            return BY_VDR_NEXT;
        }

        @Override
        Integer getVdrNext();

        @Override
        Integer getVxrHead();

        @Override
        Integer getVxrTail();

        @Override
        Integer getCprOrSprOffset();
    }

    /**
     * The Interface VDRV3.
     */
    public interface VDRV3 extends VDR, CDFV3LinkedRecord<VDRV3> {

        static Comparator<VDRV3> BY_VDR_NEXT = Comparator.comparing(VDRV3::getVdrNext);

        @Override
        default Long getNextRecordOffset() {
            return getVdrNext();
        }

        @Override
        default Comparator<VDRV3> getNextRecordOffsetComparator() {
            return BY_VDR_NEXT;
        }

        @Override
        Long getVdrNext();

        @Override
        Long getVxrHead();

        @Override
        Long getVxrTail();

        @Override
        Long getCprOrSprOffset();
    }

    @Value
    @NonFinal
    @SuperBuilder(toBuilder = true)
    abstract static class AbstractVDR<RECORD_SIZE_FIELD_TYPE extends Number, OFFSET_FIELD_TYPE extends Number>
            implements VDR {

        /**
         * The size in bytes of this VDR (including this field).
         */
        RECORD_SIZE_FIELD_TYPE recordSize;

        int recordType;

        /**
         * The file offset of the next VDR.
         *
         * @return the next VDR
         */
        OFFSET_FIELD_TYPE vdrNext;

        /**
         * The data type of this entry.
         *
         */
        int dataType;

        /**
         * The maximum record number physically written to this variable.
         * <p>
         * This is the last written record number.
         * <p>
         * More records might be allocated after this record so future written record(s)
         * can be in contiguous form to eliminate the potential data fragmentation.
         * <p>
         * Variable records are numbered beginning at zero (0).
         * <p>
         * If no records have been written to this variable, this field will contain negative one (-1).
         */
        int maxRec;

        /**
         * The file offset of the first Variable Index Record (VXR).
         * <p>
         * VXRs are used in single-file CDFs
         * to store the locations of Variable Value Records (VVRs).
         * <p>
         * VVRs are used to store variable records in single-file CDFs.
         * <p>
         * VXRs are described in Section 2.7 and VVRs are described in Section 2.8.
         * <p>
         * The first VXR contains the file offset of the next VXR and so on.
         * <p>
         * The last VXR contains a file offset of 0x00000000 for the file offset of the next VXR (to indicate the end of
         * the VXRs).
         * <p>
         * In single-file CDFs, if no records have been written to this variable, this field will contain a file offset
         * of 0x0000000000000000.
         * <p>
         * For multi-file CDFs variable records are stored in separate files and this field will always contain a file
         * offset of 0x00000000.
         *
         */
        OFFSET_FIELD_TYPE vxrHead;

        /**
         * The file offset of the last VXR.
         * <p>
         * See the VXRhead field above for a description of VXRs.
         */
        OFFSET_FIELD_TYPE vxrTail;

        /**
         * Boolean flags, one per bit, describing some aspect of this variable.
         * <p>
         * Bit numbering is described in Chapter 5.
         * <p>
         * The meaning of each bit is as follows...
         * <p>
         * 0: The record variance of this variable. Set indicates a TRUE record variance. Clear indicates a FALSE record
         * variance
         * <p>
         * <p>
         * 1: Whether or not a pad value is specified for this variable.
         * Set indicates that a pad value has been specified.
         * Clear indicates that a pad value has not been specified.
         * The PadValue field described below is only present if a pad value has been specified.
         * <p>
         * <p>
         * 2: Whether or not a compression method might be applied to this variable data.
         * Set indicates that a compression is chosen by the user and the data might be compressed, depending on the
         * data size and content.
         * If the compressed data becomes larger than its uncompressed data, no compression is applied and the data are
         * stored as uncompressed, even the compression bit is set.
         * The compressed data is stored in Compressed Variable Value Record (CVVR) while uncompressed data go into
         * Variable Value Record (VVR).
         * Clear indicates that a compression will not be used.
         * The CPRorSPRoffset field described below provides the offset of the Compressed Parameters Record if this
         * compression bit is set and the compression used
         * <p>
         * <p>
         * 3-31: Reserved for future use. These bits are always clear.
         * 
         */
        int flags;

        /**
         * Type of sparse records: no sparserecords, padded sparserecords (using the default/defined
         * pad value), or previous sparserecords (using the last written value).
         * <p>
         * When reading a record(s) from a variable with sparserecords that is not written, data value(s) will be
         * returned based on the type of sparse records.
         * <p>
         * In this case, a non-zero, but positive status code will be returned, indicating a virtual record(s) is
         * involved.
         * <p>
         * A variable with sparserecords tends to be less efficient than a variable of non-sparserecords.
         * <p>
         * Try to limit the number of sparserecords if possible.
         */
        int sRecords;

        /**
         * Reserved for future used. Always set to zero (0).
         */
        int rfuB;

        /**
         * Reserved for future used. Always set to zero (-1).
         */
        int rfuC;

        /**
         * Reserved for future used. Always set to zero (-1).
         */
        int rfuF;

        /**
         * The number of elements of the data type (specified by the DataType field) for this variable at each value.
         * <p>
         * For character type, i.e., CDF_CHAR or CDF_UCHAR, it’s the length of the string.
         * <p>
         * For numeric type, it’s the number of items, which is 1 for most cases.
         * <p>
         * However, it can be multiple items.
         * <p>
         * This field can not be zero (0) or less.
         */
        int numElems;

        /**
         * This variable's number.
         * <p>
         * Variables are numbered beginning with zero (0).
         * <p>
         * Note that rVariables and zVariables are each numbered beginning with zero (0) and are considered two separate
         * groups of variables.
         */
        int num;

        /**
         * <p>
         * CPR/SPR offset depending on bits set in 'Flags' and compression used.
         * <p>
         * If neither compression nor sparse arrays, set to 0xFFFFFFFFFFFFFFFF.
         *
         * @return the CPR or SPR offset
         */
        OFFSET_FIELD_TYPE cprOrSprOffset;

        /**
         * The blocking factor for this variable
         * 
         * @return the blocking factor
         */
        int blockingFactor;

        /**
         * The name of this variable.
         * <p>
         * This field is 64 or 256 in length.
         * <p>
         * If the number of characters in the name is less than full length of the field, a NUL character (0x00) will be
         * used to terminate
         * the string.
         * <p>
         * In that case, the characters beyond the NUL-terminator (up to the size of this field) are undefined.
         *
         * @return the name
         */
        String name;

        /**
         * The number of dimensions for this zVariable. This field will not be present if this is an
         * rVDR (rVariable).
         *
         * @return the number of dimensions if this is a zVariable
         */
        int zNumDims;

        /**
         * Zero or more contiguous dimension sizes for this zVariable depending on the value of the
         * zNumDims field.
         * <p>
         * This field will not be present if this is an rVDR (rVariable).
         *
         * @return the size of dimensions if this is a zVariable
         */
        int[] zDimSizes;

        /**
         * Zero or more contiguous dimension variances.
         * <p>
         * If this is an rVDR, the number of dimension variances will correspond to the value of the rNumDims field of
         * the GDR.
         * <p>
         * If this is a zVDR, the number of dimension variances will correspond to the value of the zNumDims field in
         * this zVDR.
         * <p>
         * A value of negative one (-1) indicates a TRUE dimension variance and a value of zero (0) indicates a FALSE
         * dimension variance.
         *
         * @return the dim varys
         */
        int[] dimVarys;

        /**
         * The variable's pad value.
         * <p>
         * If bit 1 of the Flags field of this VDR is clear, then a pad value has not been specified for this variable
         * and this field will not be present.
         * <p>
         * If a pad value has been specified, the size of this field depends on the number of elements and the size of
         * the data type.
         * <p>
         * The encoding of the elements depends on the encoding of the CDF (which is contained in the Encoding field of
         * the CDR).
         *
         * @return the pad value
         */
        byte[] padValue;

        int rNumDims;

    }

    @Value
    @SuperBuilder(toBuilder = true)
    public static class VDRV2Impl extends AbstractVDR<Integer, Integer> implements VDRV2 {

    }

    @Value
    @SuperBuilder(toBuilder = true)
    public static class VDRV3Impl extends AbstractVDR<Long, Long> implements VDRV3 {

    }
}
