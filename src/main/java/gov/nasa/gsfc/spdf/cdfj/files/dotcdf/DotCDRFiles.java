package gov.nasa.gsfc.spdf.cdfj.files.dotcdf;

import static gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.*;
import static gov.nasa.gsfc.spdf.cdfj.records.VariableDecriptorRecords.*;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.CDFDecriptorRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.GlobalDescriptorRecords.*;
import gov.nasa.gsfc.spdf.cdfj.records.VariableDecriptorRecords.*;
import lombok.*;
import lombok.experimental.*;

@UtilityClass
public class DotCDRFiles {

    public static DotCDFFile readFile(Path dotCDFpath) {

        try (FileChannel dotCDFFileChannel = FileChannel.open(dotCDFpath)) {

            CDFMagicNumbers cdfMagicNumbers = CDFMagicNumbers.readMagicNumbers(dotCDFFileChannel);

            if (cdfMagicNumbers.isV3()) {

                CDRV3 cdr = readCdrV3(dotCDFFileChannel);

                GDRV3 gdr = readGdrV3(dotCDFFileChannel, cdr);

                NavigableSet<ADRV3> adrs = readAdrV3s(dotCDFFileChannel, gdr);

                NavigableSet<VDRV3> zVdrs = readZVdrV3s(dotCDFFileChannel, gdr);

                NavigableSet<VDRV3> rVdrs = readRVdrV3s(dotCDFFileChannel, gdr);

                return DotCDFFileV3.builder()
                        .cdfMagicNumbers(cdfMagicNumbers)
                        .cdr(cdr)
                        .gdr(gdr)
                        .adrs(adrs)
                        .zVdrs(zVdrs)
                        .rVdrs(rVdrs)
                        .build();
            }

            if (cdfMagicNumbers.isV2_6_V2_7() || cdfMagicNumbers.isV25()) {

                CDRV2 cdr = readCdrV2(dotCDFFileChannel);

                GDRV2 gdr = readGdrV2(dotCDFFileChannel, cdr);

                NavigableSet<ADRV2> adrs = readAdrV2s(dotCDFFileChannel, gdr);

                NavigableSet<VDRV2> zVdrs = readZVdrV2s(dotCDFFileChannel, gdr);

                NavigableSet<VDRV2> rVdrs = readRVdrV2s(dotCDFFileChannel, gdr);

                return DotCDFFileV2.builder()
                        .cdfMagicNumbers(cdfMagicNumbers)
                        .cdr(cdr)
                        .gdr(gdr)
                        .adrs(adrs)
                        .zVdrs(zVdrs)
                        .rVdrs(rVdrs)
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

        NavigableSet<? extends VDR> getRVdrs();

        NavigableSet<? extends VDR> getZVdrs();
    }

    @Value
    @NonFinal
    @SuperBuilder
    static abstract class AbstractDotCDFFile<CDR_TYPE extends CDR, GDR_TYPE extends GDR, ADR_TYPE extends ADR, VDR_TYPE extends VDR>
            implements DotCDFFile {

        CDFMagicNumbers cdfMagicNumbers;

        CDR_TYPE cdr;

        GDR_TYPE gdr;

        NavigableSet<ADR_TYPE> adrs;

        NavigableSet<VDR_TYPE> rVdrs;

        NavigableSet<VDR_TYPE> zVdrs;
    }

    @Value
    @SuperBuilder
    public static class DotCDFFileV2 extends AbstractDotCDFFile<CDRV2, GDRV2, ADRV2, VDRV2> {

    }

    @Value
    @SuperBuilder
    public static class DotCDFFileV3 extends AbstractDotCDFFile<CDRV3, GDRV3, ADRV3, VDRV3> {

    }

}
