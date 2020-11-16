package gov.nasa.gsfc.spdf.cdfj.records;

public class CDFRecordFields {

    interface CDFRecordField {

        int getOffsetInRecord();

        Object getValue();

    }

    interface FixedSizeCDFRecordField<FIELD_SIZE extends Number> extends CDFRecordField {

        int getFieldSize();

        Class<FIELD_SIZE> getJavaType();

    }

    interface LongCDFREcordField extends FixedSizeCDFRecordField<Long> {

        @Override
        default Class<Long> getJavaType() {
            return Long.class;
        }

        @Override
        default int getFieldSize() {
            return Long.BYTES;
        }

        @Override
        Long getValue();
    }

    interface IntegerCDFRecordField extends FixedSizeCDFRecordField<Integer> {

        @Override
        default Class<Integer> getJavaType() {
            return Integer.class;
        }

        @Override
        default int getFieldSize() {
            return Integer.BYTES;
        }
    }

    interface OffsetCDFREcordField<FIELD_SIZE extends Number> extends FixedSizeCDFRecordField<FIELD_SIZE> {

    }

    interface LongOffsetCDFREcordField extends OffsetCDFREcordField<Long>, LongCDFREcordField {

    }

    interface IntegerOffsetCDFREcordField extends OffsetCDFREcordField<Integer>, IntegerCDFRecordField {

    }

    interface VariableSizedCDFRecordField extends CDFRecordField {

    }

    interface RecordSizeField<FIELD_SIZE extends Number> extends FixedSizeCDFRecordField<FIELD_SIZE> {

        @Override
        default int getOffsetInRecord() {
            return 0;
        }

    }

    interface LongRecordSizeField extends RecordSizeField<Long>, LongCDFREcordField {

    }

    interface IntegerRecordSizeField extends RecordSizeField<Integer>, IntegerCDFRecordField {

    }

    interface RecordTypeField extends IntegerCDFRecordField {

        @Override
        Integer getValue();
    }
}
