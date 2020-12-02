package gov.nasa.gsfc.spdf.cdfj.files.dotcdf;

import static gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.NavigableSet;

import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.ADR;
import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.ADRV2;
import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.ADRV3;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.CDR;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.CDRV2;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.CDRV3;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.GDR;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.GDRV2;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.GDRV3;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DotCDRFiles {

    public static DotCDFFile readFile(Path dotCDFpath) {

        try (FileChannel dotCDFFileChannel = FileChannel.open(dotCDFpath)) {

            CDFMagicNumbers cdfMagicNumbers = CDFMagicNumbers.readMagicNumbers(dotCDFFileChannel);

            if (cdfMagicNumbers.isV3()) {

                CDRV3 cdr = readCdrV3(dotCDFFileChannel);

                GDRV3 gdr = readGdrV3(dotCDFFileChannel, cdr);

                NavigableSet<ADRV3> adrs = readAdrV3s(dotCDFFileChannel, gdr);

                return DotCDFFileV3.builder()
                        .cdfMagicNumbers(cdfMagicNumbers)
                        .cdr(cdr)
                        .gdr(gdr)
                        .adrs(adrs)
                        .build();
            }

            if (cdfMagicNumbers.isV2_6_V2_7() || cdfMagicNumbers.isV25()) {

                CDRV2 cdr = readCdrV2(dotCDFFileChannel);

                GDRV2 gdr = readGdrV2(dotCDFFileChannel, cdr);

                NavigableSet<ADRV2> adrs = readAdrV2s(dotCDFFileChannel, gdr);

                return DotCDFFileV2.builder()
                        .cdfMagicNumbers(cdfMagicNumbers)
                        .cdr(cdr)
                        .gdr(gdr)
                        .adrs(adrs)
                        .build();
            }

            throw new IllegalArgumentException("Magic Numbers " + cdfMagicNumbers.getMagicNumbersAsHexString()
                    + " from path " + dotCDFpath.toString() + " are of an unknown type.");

        }
        catch (IOException e) {
            throw new UncheckedIOException(" failed", e);
        }

    }

    public interface DotCDFFile {

        CDFMagicNumbers getCdfMagicNumbers();

        CDR getCdr();

        GDR getGdr();

        NavigableSet<? extends ADR> getAdrs();

    }

    @Value
    @NonFinal
    @SuperBuilder
    static abstract class AbstractDotCDFFile<CDR_TYPE extends CDR, GDR_TYPE extends GDR, ADR_TYPE extends ADR>
            implements DotCDFFile {

        CDFMagicNumbers cdfMagicNumbers;

        CDR_TYPE cdr;

        GDR_TYPE gdr;

        NavigableSet<ADR_TYPE> adrs;
    }

    @Value
    @SuperBuilder
    public static class DotCDFFileV2 extends AbstractDotCDFFile<CDRV2, GDRV2, ADRV2> {

    }

    @Value
    @SuperBuilder
    public static class DotCDFFileV3 extends AbstractDotCDFFile<CDRV3, GDRV3, ADRV3> {

    }

}
