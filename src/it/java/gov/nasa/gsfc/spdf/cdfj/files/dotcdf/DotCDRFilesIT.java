package gov.nasa.gsfc.spdf.cdfj.files.dotcdf;

import static gov.nasa.gsfc.spdf.cdfj.fields.CDFDataTypes.*;
import static gov.nasa.gsfc.spdf.cdfj.fields.DateTimeFields.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;

import gov.nasa.gsfc.spdf.cdfj.*;
import gov.nasa.gsfc.spdf.cdfj.fields.CDFDataTypes;
import gov.nasa.gsfc.spdf.cdfj.files.dotcdf.DotCDRFiles.*;
import gov.nasa.gsfc.spdf.cdfj.records.AttributeDescriptorRecords.ADR;
import gov.nasa.gsfc.spdf.cdfj.test.*;

class DotCDRFilesIT {

    @BeforeAll
    static void downloadNasaCdfTestFiles() {
        NasaCdfTestFiles.downloadNasaCdfTestFiles(false);
    }

    @Test
    void testReadFile() throws IOException {

        NavigableSet<String> names = new TreeSet<>();

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)

                // .filter(p -> p.toString()
                // .contains("a1_k0_mpa_20050804_v02.cdf"))
                .forEach(dotCDFpath -> {

                    System.out.println(dotCDFpath);
                    DotCDFFile dotCDFFile = DotCDRFiles.readFile(dotCDFpath);
                    names.add(dotCDFFile.getCdfMagicNumbers()
                            .getMagicNumbersAsHexString() + " " + dotCDFpath.getFileName());

                    dotCDFFile.getAdrs()
                            .stream()
                            .flatMap(adr -> Stream.concat(adr.getREntries()
                                    .stream(),
                                    Stream.concat(adr.getGEntries()
                                            .stream(),
                                            adr.getZEntries()
                                                    .stream())))
                            .filter(aedr -> aedr.getNumElems() > 1)
                            .filter(aedr -> CDFDataTypes.CDF_UCHAR_INTERNAL_VALUE == aedr.getDataType()
                                || CDFDataTypes.CDF_CHAR_INTERNAL_VALUE == aedr.getDataType())
                            .map(aedr -> aedr.getValue())
                            .map(byte[].class::cast)
                            .map(valAsBytes -> new String(valAsBytes, StandardCharsets.US_ASCII))
                            .forEach(System.out::println);

                    dotCDFFile.getRVdrs()
                            .stream()
                            .forEach(System.out::println);

                    dotCDFFile.getZVdrs()
                            .stream()
                            .forEach(System.out::println);
                });

    }

    @Test
    void testReadFile2() throws IOException {

        NavigableSet<String> names = new TreeSet<>();

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)

                .forEach(dotCDFpath -> {

                    DotCDFFile dotCDFFile = DotCDRFiles.readFile(dotCDFpath);
                    names.add(dotCDFFile.getCdfMagicNumbers()
                            .getMagicNumbersAsHexString() + " " + dotCDFpath.getFileName());

                    dotCDFFile.getAdrs()
                            .stream()
                            .flatMap(adr -> Stream.concat(adr.getREntries()
                                    .stream(),
                                    Stream.concat(adr.getGEntries()
                                            .stream(),
                                            adr.getZEntries()
                                                    .stream())))
                            .filter(ader -> CDFDataTypes.is2ByteUnsignedInteger(ader.getDataType()))
                            .peek(aedr -> System.out.println(aedr.getNumElems()))
                            .map(aedr -> toShortArray((byte[]) aedr.getValue(), aedr.getNumElems()))
                            .flatMapToInt(sa -> IntStream.range(0, sa.length)
                                    .map(i -> sa[i]))
                            .mapToObj(i -> Integer.toUnsignedString(i) + " " + Integer.toString(i))
                            .forEach(System.out::println);
                });

        names.stream()
                .forEach(System.out::println);

    }

    @Test
    void testReadFile4Real() throws IOException {

        NavigableSet<String> names = new TreeSet<>();

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)

                .forEach(dotCDFpath -> {

                    DotCDFFile dotCDFFile = DotCDRFiles.readFile(dotCDFpath);
                    names.add(dotCDFFile.getCdfMagicNumbers()
                            .getMagicNumbersAsHexString() + " " + dotCDFpath.getFileName());

                    Map<Integer, ADR> byNum = dotCDFFile.getAdrs()
                            .stream()
                            .collect(Collectors.toMap(ADR::getNum, Function.identity()));

                    dotCDFFile.getAdrs()
                            .stream()
                            // .peek(adr -> System.out.println(adr.getName()))
                            .flatMap(adr -> Stream.concat(adr.getREntries()
                                    .stream(),
                                    Stream.concat(adr.getGEntries()
                                            .stream(),
                                            adr.getZEntries()
                                                    .stream())))
                            .filter(ader -> CDFDataTypes.is4ByteSinglePrecisionType(ader.getDataType()))
                            .peek(aedr -> System.out.println(byNum.get(aedr.getAttrNum())
                                    .getName() + " " + aedr.getNumElems() + " " + aedr.getDataType()))
                            .map(aedr -> toFloatArray((byte[]) aedr.getValue(), aedr.getNumElems()))
                            .map(Arrays::toString)
                            // .flatMapToDouble(sa -> IntStream.range(0, sa.length)
                            // .mapToDouble(i -> (double) sa[i]))
                            .forEach(System.out::println);
                });

        names.stream()
                .forEach(System.out::println);

    }

    @Test
    void testReadFileEpochDates() throws IOException {

        NavigableSet<String> names = new TreeSet<>();

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)

                .forEach(dotCDFpath -> {

                    DotCDFFile dotCDFFile = DotCDRFiles.readFile(dotCDFpath);
                    names.add(dotCDFFile.getCdfMagicNumbers()
                            .getMagicNumbersAsHexString() + " " + dotCDFpath.getFileName());

                    dotCDFFile.getGdr()
                            .leapSecondLastUpdatedAsLocalDate()
                            .ifPresent(d -> System.out.println("\n****** " + d + "\n "));

                    Map<Integer, ADR> byNum = dotCDFFile.getAdrs()
                            .stream()
                            .collect(Collectors.toMap(ADR::getNum, Function.identity()));

                    System.out.println("File: " + dotCDFpath + " Version" + " " + dotCDFFile.getCdr()
                            .getCompleteVersionString());

                    dotCDFFile.getAdrs()
                            .stream()
                            // .peek(adr -> System.out.println(adr.getName()))
                            .flatMap(adr -> Stream.concat(adr.getREntries()
                                    .stream(),
                                    Stream.concat(adr.getGEntries()
                                            .stream(),
                                            adr.getZEntries()
                                                    .stream())))
                            .filter(ader -> CDF_EPOCH_INTERNAL_VALUE == ader.getDataType())
                            .peek(aedr -> System.out.println(byNum.get(aedr.getAttrNum())
                                    .getName() + " " + aedr.getNumElems() + " " + aedr.getDataType()))
                            .map(aedr -> toInstantCDFEpoch((byte[]) aedr.getValue(), aedr.getNumElems()))

                            // .flatMapToDouble(sa -> IntStream.range(0, sa.length)
                            // .mapToDouble(i -> (double) sa[i]))
                            .forEach(System.out::println);
                });

        names.stream()
                .forEach(System.out::println);

    }

    @Test
    void testReadFileEpoch16Dates() throws IOException {

        NavigableSet<String> names = new TreeSet<>();

        Files.list(NasaCdfTestFiles.NASA_TEST_CDF_FILES_DIRECTORY_PATH)

                .forEach(dotCDFpath -> {

                    DotCDFFile dotCDFFile = DotCDRFiles.readFile(dotCDFpath);
                    names.add(dotCDFFile.getCdfMagicNumbers()
                            .getMagicNumbersAsHexString() + " " + dotCDFpath.getFileName());

                    dotCDFFile.getGdr()
                            .leapSecondLastUpdatedAsLocalDate()
                            .ifPresent(d -> System.out.println("\n****** " + d + "\n "));

                    Map<Integer, ADR> byNum = dotCDFFile.getAdrs()
                            .stream()
                            .collect(Collectors.toMap(ADR::getNum, Function.identity()));

                    System.out.println("File: " + dotCDFpath + " Version" + " " + dotCDFFile.getCdr()
                            .getCompleteVersionString());

                    dotCDFFile.getAdrs()
                            .stream()
                            // .peek(adr -> System.out.println(adr.getName()))
                            .flatMap(adr -> Stream.concat(adr.getREntries()
                                    .stream(),
                                    Stream.concat(adr.getGEntries()
                                            .stream(),
                                            adr.getZEntries()
                                                    .stream())))
                            .filter(ader -> CDF_EPOCH16_INTERNAL_VALUE == ader.getDataType())
                            .peek(aedr -> System.out.println(byNum.get(aedr.getAttrNum())
                                    .getName() + " " + aedr.getNumElems() + " " + aedr.getDataType()))
                            .map(aedr -> toInstantFromCDFEpoch16((byte[]) aedr.getValue(), aedr.getNumElems()))

                            // .flatMapToDouble(sa -> IntStream.range(0, sa.length)
                            // .mapToDouble(i -> (double) sa[i]))
                            .forEach(System.out::println);
                });

        names.stream()
                .forEach(System.out::println);

    }

    @Test
    void time() {
        OffsetDateTime zero = OffsetDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        System.out.println(OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
        System.out.println(zero.until(OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC), ChronoUnit.MILLIS));
        System.out.println(TimeVariableFactory.JANUARY_1_1970_LONG);
    }
}
