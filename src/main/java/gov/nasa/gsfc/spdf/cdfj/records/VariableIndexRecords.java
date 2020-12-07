package gov.nasa.gsfc.spdf.cdfj.records;

import static gov.nasa.gsfc.spdf.cdfj.records.RecordReaders.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.stream.*;

import gov.nasa.gsfc.spdf.cdfj.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.VariableDecriptorRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.VariableIndexRecords.VXRV2Impl.*;
import gov.nasa.gsfc.spdf.cdfj.records.VariableIndexRecords.VXRV3Impl.*;
import lombok.*;
import lombok.experimental.*;

@UtilityClass
public class VariableIndexRecords {

    public static final int VXR_RECORD_TYPE = CDFRecordTypes.VXR_RECORD_TYPE_INTERNAL_VALUE;

    public static NavigableSet<VXRV2> readVXRV2s(FileChannel dotCDFFileChannel, VDRV2 vdr) throws IOException {

        int vxrOffset = vdr.getVxrHead();

        if (0 == vxrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<VXRV2> vxrs = new TreeSet<>();

        while (vxrOffset != 0) {
            ByteBuffer zvdrBuffer = readV2Record(dotCDFFileChannel, vxrOffset);
            VXRV2 vxr = vxrv2(zvdrBuffer, dotCDFFileChannel);
            vxrs.add(vxr);
            vxrOffset = vxr.getVxrNext();
        }

        return vxrs;
    }

    public static NavigableSet<VXRV3> readRVdrV3s(FileChannel dotCDFFileChannel, VDRV3 vdr3) throws IOException {

        long vxrOffset = vdr3.getVxrHead();

        if (0 == vxrOffset) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<VXRV3> vxrs = new TreeSet<>();

        while (vxrOffset != 0) {
            ByteBuffer zvdrBuffer = readV3Record(dotCDFFileChannel, vxrOffset);
            VXRV3 vxr = vxrv3(zvdrBuffer, dotCDFFileChannel);
            vxrs.add(vxr);
            vxrOffset = vxr.getVxrNext();
        }

        return vxrs;
    }

    public static final VXRV2 vxrv2(ByteBuffer source, FileChannel dotCDFFileChannel) {

        VXRV2ImplBuilder<?, ?> builder = VXRV2Impl.builder()
                .recordSize(source.getInt());

        int recordType = source.getInt();
        builder.recordType(recordType);
        int vxrNext = source.getInt();
        int nEntries = source.getInt();
        int nUsedEntries = source.getInt();
        int[] firsts = new int[nEntries];

        source.asIntBuffer()
                .get(firsts);

        source.position(source.position() + (nEntries * Integer.BYTES));

        builder.vxrNext(vxrNext)
                .nEntries(nEntries)
                .nUsedEntries(nUsedEntries)
                .first(firsts);

        int[] lasts = new int[nEntries];

        source.asIntBuffer()
                .get(lasts);

        source.position(source.position() + (nEntries * Integer.BYTES));

        int[] offsets = new int[nEntries];

        source.asIntBuffer()
                .get(offsets);

        source.position(source.position() + (nEntries * Integer.BYTES));

        List<Integer> offsetsAsList = Arrays.stream(offsets)
                .boxed()
                .collect(Collectors.toList());

        return builder.offsets(offsetsAsList)
                .build();

    }

    public static final VXRV3 vxrv3(ByteBuffer source, FileChannel dotCDFFileChannel) {

        VXRV3ImplBuilder<?, ?> builder = VXRV3Impl.builder()
                .recordSize(source.getLong());

        int recordType = source.getInt();
        builder.recordType(recordType);
        long vxrNext = source.getLong();
        int nEntries = source.getInt();
        int nUsedEntries = source.getInt();
        int[] firsts = new int[nEntries];

        source.asIntBuffer()
                .get(firsts);

        source.position(source.position() + (nEntries * Integer.BYTES));

        builder.vxrNext(vxrNext)
                .nEntries(nEntries)
                .nUsedEntries(nUsedEntries)
                .first(firsts);

        int[] lasts = new int[nEntries];

        source.asIntBuffer()
                .get(lasts);

        source.position(source.position() + (nEntries * Integer.BYTES));

        long[] offsets = new long[nEntries];

        source.asLongBuffer()
                .get(offsets);

        source.position(source.position() + (nEntries * Integer.BYTES));

        List<Long> offsetsAsList = Arrays.stream(offsets)
                .boxed()
                .collect(Collectors.toList());

        return builder.offsets(offsetsAsList)
                .build();

    }

    public interface VXR extends CDFRecord {

        /**
         * The file offset of the next VXR. The last VXR will contain a file offset of
         * 0x0000000000000000 in this field (to indicate the end of the VXRs).
         *
         * @return the next VDR
         */
        Number getVxrNext();

        /**
         * The number of index entries in this VXR. This is the maximum number of
         * VVRs that may be indexed using this VXR
         *
         * @return the n entries
         */
        int getNEntries();

        /**
         * The number of index entries actually used in this VXR.
         *
         * @return the n used entries
         */
        int getNUsedEntries();

        /**
         * This is a contiguous array of variable record numbers with each record number being the first variable record
         * in the corresponding VVR or lower level VXRs.
         * <p>
         * The size of this array depends on the value of the Nentries field.
         * <p>
         * The nth entry in this array corresponds to the nth entry in the Last and Offset fields.
         * <p>
         * Unused entries in this array contain 0xFFFFFFFF.
         * <p>
         * Note that variable records are numbered beginning with zero (0).
         *
         * @return the first
         */
        int[] getFirst();

        /**
         * This is a contiguous array of variable record numbers with each record number being the last variable record
         * in the corresponding VVR or lower level VXRs.
         * <p>
         * The size of this array depends on the value of the Nentries field.
         * <p>
         * The nth entry in this array corresponds to the nth entry in the First and Offset fields.
         * <p>
         * Unused entries in this array contain 0xFFFFFFFF.
         * <p>
         * Note that variable records are numbered beginning with zero (0).
         *
         * @return the last
         */
        int[] getLast();

        /**
         * This is a contiguous array of file offsets with each being the file offset of the corresponding VVR, CVVR or
         * a lower level of VXR.
         * <p>
         * If the offset is pointing to a VXR, the prior, corresponding first/last fields are the record range this VXR
         * tree will hold.
         * <p>
         * The size of this array depends on the value of the Nentries field.
         * <p>
         * The nth entry in this array corresponds to the nth entry in the First and Last fields.
         * <p>
         * Unused entries in this array contain 0xFFFFFFFFFFFFFFFF.
         *
         * @return the offset
         */
        List<? extends Number> getOffsets();
    }

    public interface VXRV2 extends VXR, CDFV2LinkedRecord<VXRV2> {

        static Comparator<VXRV2> BY_VXR_NEXT = Comparator.comparing(VXRV2::getVxrNext);

        @Override
        default Integer getNextRecordOffset() {
            return getVxrNext();
        }

        @Override
        default Comparator<VXRV2> getNextRecordOffsetComparator() {
            return BY_VXR_NEXT;
        }

        @Override
        Integer getVxrNext();

        @Override
        List<Integer> getOffsets();

        default int compareTo(VXRV2 that) {
            return BY_VXR_NEXT.compare(this, that);
        }
    }

    public interface VXRV3 extends VXR, CDFV3LinkedRecord<VXRV3> {

        static Comparator<VXRV3> BY_VXR_NEXT = Comparator.comparing(VXRV3::getVxrNext);

        @Override
        default Long getNextRecordOffset() {
            return getVxrNext();
        }

        @Override
        default Comparator<VXRV3> getNextRecordOffsetComparator() {
            return BY_VXR_NEXT;
        }

        @Override
        Long getVxrNext();

        @Override
        List<Long> getOffsets();

        default int compareTo(VXRV3 that) {
            return BY_VXR_NEXT.compare(this, that);
        }
    }

    @Value
    @NonFinal
    @SuperBuilder(toBuilder = true)
    abstract static class AbstractVXR<RECORD_SIZE_FIELD_TYPE extends Number, OFFSET_FIELD_TYPE extends Number>
            implements VXR {

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
        OFFSET_FIELD_TYPE vxrNext;

        /**
         * The number of index entries in this VXR. This is the maximum number of
         * VVRs that may be indexed using this VXR
         */
        int nEntries;

        /**
         * The number of index entries actually used in this VXR.
         */
        int nUsedEntries;

        /**
         * This is a contiguous array of variable record numbers with each record number being the first variable record
         * in the corresponding VVR or lower level VXRs.
         * <p>
         * The size of this array depends on the value of the Nentries field.
         * <p>
         * The nth entry in this array corresponds to the nth entry in the Last and Offset fields.
         * <p>
         * Unused entries in this array contain 0xFFFFFFFF.
         * <p>
         * Note that variable records are numbered beginning with zero (0).
         */
        int[] first;

        /**
         * This is a contiguous array of variable record numbers with each record number being the last variable record
         * in the corresponding VVR or lower level VXRs.
         * <p>
         * The size of this array depends on the value of the Nentries field.
         * <p>
         * The nth entry in this array corresponds to the nth entry in the First and Offset fields.
         * <p>
         * Unused entries in this array contain 0xFFFFFFFF.
         * <p>
         * Note that variable records are numbered beginning with zero (0).
         *
         */
        int[] last;

        /**
         * This is a contiguous array of file offsets with each being the file offset of the corresponding VVR, CVVR or
         * a lower level of VXR.
         * <p>
         * If the offset is pointing to a VXR, the prior, corresponding first/last fields are the record range this VXR
         * tree will hold.
         * <p>
         * The size of this array depends on the value of the Nentries field.
         * <p>
         * The nth entry in this array corresponds to the nth entry in the First and Last fields.
         * <p>
         * Unused entries in this array contain 0xFFFFFFFFFFFFFFFF.
         */
        @Singular
        List<OFFSET_FIELD_TYPE> offsets;
    }

    @Value
    @SuperBuilder(toBuilder = true)
    public static class VXRV2Impl extends AbstractVXR<Integer, Integer> implements VXRV2 {

    }

    @Value
    @SuperBuilder(toBuilder = true)
    public static class VXRV3Impl extends AbstractVXR<Long, Long> implements VXRV3 {

    }
}
