package gov.nasa.gsfc.spdf.cdfj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * CDFFactory creates an instance of CDFImpl from a CDF source.
 * The source CDF can be a file, a byte array, or a URL.
 */
public final class CDFFactory {

    static final Logger LOGGER = CDFLogging.newLogger(CDFFactory.class);

    /** The Constant CDF_V3_MAGIC_NUMBER_1. */
    public static final int CDF_V3_MAGIC_NUMBER_1 = 0xCDF3_0001;

    /** The Constant CDF_V3_MAGIC_NUMBER_1_AS_STRING. */
    public static final String CDF_V3_MAGIC_NUMBER_1_AS_STRING = Integer.toHexString(CDF_V3_MAGIC_NUMBER_1);

    /** The Constant CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2. */
    public static final int CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2 = 0x0000_ffff;

    /** The Constant CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2_AS_STRING. */
    public static final String CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2_AS_STRING = Integer
            .toHexString(CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2);

    /** The Constant CDF_V3_COMPRESSED_MAGIC_NUMBER_2. */
    public static final int CDF_V3_COMPRESSED_MAGIC_NUMBER_2 = 0xCCCC_0001;

    /** The Constant CDF_V3_COMPRESSED_MAGIC_NUMBER_2_AS_STRING. */
    public static final String CDF_V3_COMPRESSED_MAGIC_NUMBER_2_AS_STRING = Integer
            .toHexString(CDF_V3_COMPRESSED_MAGIC_NUMBER_2);

    /** The Constant CDF_V3_MAGIC_NUMBER_UNCOMPRESSED. */
    public static final long CDF_V3_MAGIC_NUMBER_UNCOMPRESSED = (((long) CDF_V3_MAGIC_NUMBER_1) << 32)
            + CDF_V3_UNCOMPRESSED_MAGIC_NUMBER_2;

    /** The Constant CDF_V3_MAGIC_NUMBER_UNCOMPRESSED_AS_STRING. */
    public static final String CDF_V3_MAGIC_NUMBER_UNCOMPRESSED_AS_STRING = Long
            .toHexString(CDF_V3_MAGIC_NUMBER_UNCOMPRESSED);

    /** The Constant CDF_V3_MAGIC_NUMBER_COMPRESSED. */
    public static final long CDF_V3_MAGIC_NUMBER_COMPRESSED = (((long) CDF_V3_MAGIC_NUMBER_1) << 32)
            + CDF_V3_COMPRESSED_MAGIC_NUMBER_2;

    /** The Constant CDF_V3_MAGIC_NUMBER_COMPRESSED_AS_STRING. */
    public static final String CDF_V3_MAGIC_NUMBER_COMPRESSED_AS_STRING = Long
            .toHexString(CDF_V3_MAGIC_NUMBER_COMPRESSED);

    /** The Constant CDF_V2_6_V2_7_MAGIC_NUMBER_1. */
    public static final int CDF_V2_6_V2_7_MAGIC_NUMBER_1 = 0xCDF26002;

    /** The Constant CDF_V2_6_V2_7_UNCOMPRESSED_MAGIC_NUMBER_2. */
    public static final int CDF_V2_6_V2_7_UNCOMPRESSED_MAGIC_NUMBER_2 = 0x0000_ffff;

    /** The Constant CDF_V2_6_V2_7_COMPRESSED_MAGIC_NUMBER_2. */
    public static final int CDF_V2_6_V2_7_COMPRESSED_MAGIC_NUMBER_2 = 0xCCCC_0001;

    /** The Constant CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED. */
    public static final long CDF_V2_6_V2_7_MAGIC_NUMBER_UNCOMPRESSED = (((long) CDF_V2_6_V2_7_MAGIC_NUMBER_1) << 32)
            + CDF_V2_6_V2_7_UNCOMPRESSED_MAGIC_NUMBER_2;

    /** The Constant CDF_V2_6_V2_7_MAGIC_NUMBER_COMPRESSED. */
    public static final long CDF_V2_6_V2_7_MAGIC_NUMBER_COMPRESSED = (((long) CDF_V2_6_V2_7_MAGIC_NUMBER_1) << 32)
            + CDF_V2_6_V2_7_COMPRESSED_MAGIC_NUMBER_2;

    /** The Constant CDF_v2_5_MAGIC_NUMBER_1. */
    public static final int CDF_v2_5_MAGIC_NUMBER_1 = 0x0000_ffff;

    /** The Constant CDF_v2_5_MAGIC_NUMBER_2. */
    public static final int CDF_v2_5_MAGIC_NUMBER_2 = 0x0000_ffff;

    /** The Constant CDF_v2_5_MAGIC_NUMBER. */
    public static final long CDF_v2_5_MAGIC_NUMBER = (((long) CDF_v2_5_MAGIC_NUMBER_1) << 32) + CDF_v2_5_MAGIC_NUMBER_2;

    /** The Constant CDF3_MAGIC. */
    public static final long CDF3_MAGIC = (0xcdf3L << 48) + (0x0001L << 32) + 0x0000_ffff;

    /** The Constant CDF3_COMPRESSED_MAGIC. */
    public static final long CDF3_COMPRESSED_MAGIC = (0xcdf3L << 48) + (0x0001L << 32) + 0x0000_0000_cccc_0001L;

    /** The Constant CDF2_MAGIC. */
    public static final long CDF2_MAGIC = (0xcdf2L << 48) + (0x0001L << 32) + 0x0000_ffff;

    /** The Constant CDF2_MAGIC_DOT5. */
    public static final long CDF2_MAGIC_DOT5 = (0x0000_ffffL << 32) + 0x0000_ffff;

    static final Map<CDFCore, String> cdfMap = Collections.synchronizedMap(new WeakHashMap<>());

    static volatile Long maxMappedMemory;

    private CDFFactory() {}

    /**
     * Clean.
     */
    public static void clean() {

        if (maxMappedMemory != null) {

            if (mappedMemoryUsed() > maxMappedMemory) {
                System.gc();
            }

        }

    }

    /**
     * creates CDFImpl object from a file.
     *
     * @param fname the fname
     *
     * @return the cdf
     */
    public static CDFImpl getCDF(final String fname) {
        return getCDF(fname, false);
    }

    /**
     * Sets the max mapped memory.
     *
     * @param value the new max mapped memory
     */
    public static void setMaxMappedMemory(final long value) {

        if (maxMappedMemory != null) {

            if (maxMappedMemory > value) {
                return;
            }

        }

        maxMappedMemory = value;
    }

    static CDFImpl getCDF(final byte[] ba, final CDFSource cdfSource) {
        ByteBuffer buf;

        synchronized (ba) {
            buf = ByteBuffer.allocateDirect(ba.length);
            buf.put(ba);
        }

        buf.flip();
        return getVersion(buf, cdfSource);
    }

    static CDFImpl getCDF(final ByteBuffer buf) {
        ByteBuffer rbuf;

        synchronized (buf) {
            ByteBuffer _buf = ByteBuffer.allocateDirect(buf.remaining());
            _buf.put(buf);
            _buf.position(0);
            rbuf = _buf.asReadOnlyBuffer();
            rbuf.order(buf.order());
        }

        return getVersion(rbuf, new CDFFactory.ByteBufferCDFSource());
    }

    static CDFImpl getCDF(final String fname, final boolean option) {
        clean();

        File file = new File(fname);

        final String _fname = file.getPath();

        ByteBuffer buf;

        try (final FileInputStream fis = new FileInputStream(file); final FileChannel ch = fis.getChannel()) {
            buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(_fname + " could not be found", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file:  " + _fname, e);
        }

        CDFImpl cdf = getVersion(buf, new FileCDFSource(_fname));
        cdf.setOption(option ? new AccceptMissingRecords() : new RejectMissingRecords());
        cdfMap.put(cdf, _fname);
        return cdf;
    }

    static CDFImpl getCDF(final URL url) {

        URLConnection con;

        try {
            con = new CDFUrl(url).openConnection();
        } catch (IOException e) {
            throw new UncheckedIOException("failed to open URLConnection: " + url, e);
        }

        int remaining = con.getContentLength();

        try (InputStream is = con.getInputStream()) {

            byte[] ba = new byte[remaining];
            int offset = 0;

            while (remaining > 0) {
                int got = is.read(ba, offset, remaining);
                offset += got;
                remaining -= got;
            }

            final String _url = url.toString();
            return getCDF(ba, new UrlCDFSource(_url));

        } catch (IOException e) {
            throw new UncheckedIOException("failed to read from url: " + url, e);
        }

    }

    static CDFImpl getVersion(final ByteBuffer buf, final CDFSource cdfSource) {
        LongBuffer lbuf = buf.asLongBuffer();
        long magic = lbuf.get();

        if (magic == CDF3_MAGIC) {
            return new CDF3Impl(buf, cdfSource);
        }

        if (magic == CDF3_COMPRESSED_MAGIC) {
            ByteBuffer mbuf = uncompressed(buf, 3);
            return new CDF3Impl(mbuf, cdfSource);
        }

        if (magic == CDF2_MAGIC_DOT5) {
            int release = buf.getInt(24);
            return new CDF2Impl(buf, release, cdfSource);
        }

        ShortBuffer sbuf = buf.asShortBuffer();

        if (sbuf.get() == (short) 0xcdf2) {

            if (sbuf.get() == (short) 0x6002) {
                short x = sbuf.get();

                if (x == 0) {

                    if (sbuf.get() == -1) {
                        return new CDF2Impl(buf, 6);
                    }

                } else {

                    if ((x == (short) 0xcccc) && (sbuf.get() == 1)) {
                        // is compressed - positioned at CCR
                        ByteBuffer mbuf = uncompressed(buf, 2);
                        return new CDF2Impl(mbuf, 6);
                    }

                }

            }

        }

        throw new IllegalArgumentException("CDF version not supported, magic numbers are " + Long.toHexString(magic));
    }

    static ByteBuffer uncompressed(final ByteBuffer buf, final int version) {
        int DATA_OFFSET = 8 + 20;

        if (version == 3) {
            DATA_OFFSET = 8 + 32;
        }

        byte[] ba;
        int offset;
        int len = buf.getInt(8) - 20;

        if (version == 3) {
            len = (int) (buf.getLong(8) - 32);
        }

        int ulen = buf.getInt(8 + 12);

        if (version == 3) {
            ulen = (int) (buf.getLong(8 + 20));
        }

        byte[] udata = new byte[ulen + 8];
        buf.get(udata, 0, 8); // copy the magic words

        if (!buf.hasArray()) { // read data into byte array
            ba = new byte[len];
            buf.position(DATA_OFFSET);
            buf.get(ba);
            offset = 0;
        } else {
            ba = buf.array();
            offset = DATA_OFFSET;
        }

        int n = 0;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba, offset, len);
            GZIPInputStream gz = new GZIPInputStream(bais);
            int toRead = udata.length - 8;
            int off = 8;

            while (toRead > 0) {
                n = gz.read(udata, off, toRead);

                if (n == -1) {
                    break;
                }

                off += n;
                toRead -= n;
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "uncompressed failed");
            return null;
        }

        if (n < 0) {
            return null;
        }

        return ByteBuffer.wrap(udata);
    }

    private static long mappedMemoryUsed() {

        if (cdfMap.isEmpty()) {
            return 0;
        }

        return cdfMap.keySet()
                .stream()
                .filter(CDFImpl.class::isInstance)
                .map(CDFImpl.class::cast)
                .map(CDFImpl::getBuffer)
                .mapToInt(ByteBuffer::limit)
                .sum();

    }

    /**
     * The Class CDFSource.
     */
    public static class CDFSource {

        private final String name;

        /**
         * Instantiates a new CDF source.
         */
        @Deprecated
        public CDFSource() {
            this.name = null;
        }

        /**
         * Instantiates a new CDF source.
         *
         * @param name the name
         */
        public CDFSource(final String name) {
            this.name = name;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Checks if is byte array.
         *
         * @return true, if is byte array
         */
        public boolean isByteArray() {
            return false;
        }

        /**
         * Checks if is byte buffer.
         *
         * @return true, if is byte buffer
         */
        public boolean isByteBuffer() {
            return false;
        }

        /**
         * Checks if is file.
         *
         * @return true, if is file
         */
        public boolean isFile() {
            return false;
        }

        /**
         * Checks if is url.
         *
         * @return true, if is url
         */
        public boolean isURL() {
            return false;
        }
    }

    /** The Constant FileCDFSource. */
    public static final class FileCDFSource extends CDFSource {

        /**
         * File CDF source.
         *
         * @param name the name
         */
        public FileCDFSource(final String name) {
            super(name);

        }

        /**
         * Checks if is file.
         *
         * @return true, if is file
         */
        @Override
        public boolean isFile() {
            return true;
        }
    }

    /**
     * The Class ProcessingOption.
     */
    public static class ProcessingOption {

        String missingRecordsOption() {
            return "reject";
        }
    }

    /**
     * The Class RejectMissingRecords.
     */
    public static class RejectMissingRecords extends ProcessingOption {

        @Override
        String missingRecordsOption() {
            return "reject";
        }
    }

    /**
     * The Class AccceptMissingRecords.
     */
    public static class AccceptMissingRecords extends ProcessingOption {

        @Override
        String missingRecordsOption() {
            return "accept";
        }
    }

    /**
     * The Class UrlCDFSource.
     */
    public static final class UrlCDFSource extends CDFSource {

        /**
         * Instantiates a new url CDF source.
         *
         * @param name the name
         */
        public UrlCDFSource(final String name) {
            super(name);

        }

        @Override
        public boolean isURL() {
            return true;
        }
    }

    /**
     * The Class ByteArrayCDFSource.
     */
    public static final class ByteArrayCDFSource extends CDFSource {

        /**
         * Instantiates a new url CDF source.
         */
        public ByteArrayCDFSource() {
            super("byte[]");

        }

        /**
         * Checks if is byte array.
         *
         * @return true, if is byte array
         */
        @Override
        public boolean isByteArray() {
            return true;
        }

    }

    /**
     * The Class ByteBufferCDFSource.
     */
    public static final class ByteBufferCDFSource extends CDFSource {

        /**
         * Instantiates a new url CDF source.
         */
        public ByteBufferCDFSource() {
            super("ByteBuffer");

        }

        /**
         * Checks if is byte array.
         *
         * @return true, if is byte array
         */
        @Override
        public boolean isByteBuffer() {
            return true;
        }

    }
}
