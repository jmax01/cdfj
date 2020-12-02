package gov.nasa.gsfc.spdf.cdfj;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.logging.*;

import gov.nasa.gsfc.spdf.cdfj.CDFException.*;
import gov.nasa.gsfc.spdf.cdfj.CDFFactory.*;
import gov.nasa.gsfc.spdf.cdfj.files.dotcdf.*;
import lombok.extern.java.*;

/**
 * ReaderFactory creates an instance of CDFReader from a CDF source.
 * Uses array backed ByteBuffer for CDFReader.
 * The source CDF can be a file, or a URL.
 */
@Log
public final class ReaderFactory {

    /**
     * creates CDFReader object from a file using array backed ByteBuffer.
     */
    static final int preamble = 3_000;

    private ReaderFactory() {}

    /**
     * Gets the reader.
     *
     * @param fname the fname
     *
     * @return the reader
     *
     * @throws ReaderError the reader error
     */
    @SuppressWarnings("resource")
    public static CDFReader getReader(final String fname) throws CDFException.ReaderError {

        return new CDFReader(fileChannelCDF(fname));

    }

    @SuppressWarnings("resource")
    private static CDFImpl fileChannelCDF(final String fname) throws ReaderError {

        RandomAccessFile raf = null;

        FileChannel fileChannel = null;

        try {
            File file = new File(fname);
            raf = new RandomAccessFile(file, "r");
        }
        catch (FileNotFoundException e) {

            throw new CDFException.ReaderError("Failed to read file" + fname, e);
        }

        try {

            long len = raf.length();

            if (len > preamble) {
                len = preamble;
            }

            byte[] ba = new byte[(int) len];

            raf.readFully(ba);

            ByteBuffer buf = ByteBuffer.wrap(ba);

            fileChannel = raf.getChannel();

            return getVersion(buf, fileChannel, new CDFFactory.FileCDFSource(fname));

        }
        catch (IOException | RuntimeException e) {

            if (fileChannel != null) {

                try {
                    fileChannel.close();
                }
                catch (IOException e1) {
                    LOGGER.log(Level.WARNING, e1, () -> "Failed to close file channel" + fname);
                }

            }

            try {
                raf.close();
            }
            catch (IOException ioe) {
                LOGGER.log(Level.WARNING, ioe, () -> "Failed to close file" + fname);
            }

            throw new CDFException.ReaderError("I/O Error reading " + fname, e);

        }

    }

    public static CDFReader getReader(ByteBuffer byteBuffer) {

        return new CDFReader(getVersion(byteBuffer, new CDFFactory.ByteBufferCDFSource()));

    }

    /**
     * Gets the reader.
     *
     * @param fname the fname
     * @param map   the map
     *
     * @return the reader
     *
     * @throws ReaderError the reader error
     */
    @SuppressWarnings("resource")
    public static CDFReader getReader(final String fname, final boolean map) throws CDFException.ReaderError {

        File file = new File(fname);

        return new CDFReader(toCDf(fname, file));

    }

    private static CDFImpl toCDf(final String fname, final File file) throws ReaderError {

        int len = (int) file.length();
        byte[] ba = new byte[len];
        int rem = len;

        try (FileInputStream fis = new FileInputStream(file)) {
            int n = 0;

            while (rem > 0) {
                len = fis.read(ba, n, rem);
                n += len;
                rem -= len;
            }

        }
        catch (FileNotFoundException e) {
            throw new CDFException.ReaderError("File, " + fname + " not found", e);
        }
        catch (IOException e) {
            throw new CDFException.ReaderError("Failed to read file, " + fname, e);
        }

        return CDFFactory.getCDF(ba, new CDFFactory.FileCDFSource(fname));

    }

    /**
     * creates CDFReader object from a URL using array backed ByteBuffer.
     *
     * @param url the url
     *
     * @return the reader
     *
     * @throws ReaderError the reader error
     */
    @SuppressWarnings("resource")
    public static CDFReader getReader(final URL url) throws CDFException.ReaderError {

        return new CDFReader(toCdf(url));

    }

    private static CDFImpl toCdf(final URL url) throws ReaderError {

        try {

            URLConnection con = new CDFUrl(url).openConnection();

            int remaining = con.getContentLength();

            byte[] ba = new byte[remaining];

            try (InputStream is = con.getInputStream()) {

                int offset = 0;

                while (remaining > 0) {
                    int got = is.read(ba, offset, remaining);
                    offset += got;
                    remaining -= got;
                }

            }

            ((HttpURLConnection) con).disconnect();
            ByteBuffer buf = ByteBuffer.wrap(ba);
            return CDFFactory.getVersion(buf, new CDFFactory.ByteArrayCDFSource());
        }
        catch (RuntimeException e) {
            throw new CDFException.ReaderError("I/O Error reading " + url, e);
        }
        catch (IOException e) {
            throw new UncheckedIOException("getReader failed", e);
        }

    }

    static CDFImpl getVersion(final ByteBuffer buf, final ByteBufferCDFSource byteBufferCDFSource) {
        LongBuffer lbuf = buf.asLongBuffer();

        long magic = lbuf.get(0);

        if (magic == CDFFactory.CDF3_MAGIC) {
            return new CDF3Impl(buf, byteBufferCDFSource);
        }

        if (magic == CDFFactory.CDF3_COMPRESSED_MAGIC) {
            ByteBuffer mbuf = CDFFactory.uncompressed(buf, 3);
            return new CDF3Impl(mbuf, byteBufferCDFSource);
        }

        if (magic == CDFMagicNumbers.CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED) {
            int release = buf.getInt(24);
            return new CDF2Impl(buf, release, byteBufferCDFSource);
        }

        if (magic == CDFFactory.CDF2_MAGIC_DOT5) {
            int release = buf.getInt(24);
            return new CDF2Impl(buf, release, byteBufferCDFSource);
        }

        // ShortBuffer sbuf = buf.asShortBuffer();
        //
        // if (sbuf.get() == (short) 0xcdf2) {
        //
        // if (sbuf.get() == (short) 0x6002) {
        // short x = sbuf.get();
        //
        // if (x == 0) {
        //
        // if (sbuf.get() == -1) {
        // return new CDF2Impl(buf, 6);
        // }
        //
        // } else {
        //
        // if ((x == (short) 0xcccc) && (sbuf.get() == 1)) {
        // // is compressed - positioned at CCR
        // ByteBuffer mbuf = CDFFactory.uncompressed(buf, 2);
        // return new CDF2Impl(mbuf, 6, ch);
        // }
        //
        // }
        //
        // }
        //
        // }

        return null;
    }

    static CDFImpl getVersion(final ByteBuffer buf, final FileChannel ch, final FileCDFSource fileCDFSource) {
        LongBuffer lbuf = buf.asLongBuffer();
        long magic = lbuf.get();

        if (magic == CDFFactory.CDF3_MAGIC) {
            return new CDF3Impl(buf, ch, fileCDFSource);
        }

        if (magic == CDFFactory.CDF3_COMPRESSED_MAGIC) {
            ByteBuffer mbuf = CDFFactory.uncompressed(buf, 3);
            return new CDF3Impl(mbuf, fileCDFSource);
        }

        if (magic == CDFFactory.CDF2_MAGIC_DOT5) {
            int release = buf.getInt(24);
            return new CDF2Impl(buf, release, ch, fileCDFSource);
        }

        ShortBuffer sbuf = buf.asShortBuffer();

        if (sbuf.get() == (short) 0xcdf2) {

            if (sbuf.get() == (short) 0x6002) {
                short x = sbuf.get();

                if (x == 0) {

                    if (sbuf.get() == -1) {
                        return new CDF2Impl(buf, 6, ch);
                    }

                } else {

                    if ((x == (short) 0xcccc) && (sbuf.get() == 1)) {
                        // is compressed - positioned at CCR
                        ByteBuffer mbuf = CDFFactory.uncompressed(buf, 2);
                        return new CDF2Impl(mbuf, 6, ch);
                    }

                }

            }

        }

        return null;
    }
}
