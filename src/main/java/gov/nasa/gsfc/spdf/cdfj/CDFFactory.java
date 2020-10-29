package gov.nasa.gsfc.spdf.cdfj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;

/**
 * CDFFactory creates an instance of CDFImpl from a CDF source.
 * The source CDF can be a file, a byte array, or a URL.
 */
public final class CDFFactory {

    /**
     *
     */
    public static final long CDF3_MAGIC = (0xcdf3L << 48) + (0x0001L << 32) + 0x0000_ffff;

    /**
     *
     */
    public static final long CDF3_COMPRESSED_MAGIC = (0xcdf3L << 48) + (0x0001L << 32) + 0x0000_0000_cccc_0001L;

    /**
     *
     */
    public static final long CDF2_MAGIC = (0xcdf2L << 48) + (0x0001L << 32) + 0x0000_ffff;

    /**
     *
     */
    public static final long CDF2_MAGIC_DOT5 = (0x0000_ffffL << 32) + 0x0000_ffff;

    static Map cdfMap = Collections.synchronizedMap(new WeakHashMap());

    static Long maxMappedMemory;

    private CDFFactory() {
    }

    /**
     *
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
     * @param fname
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    public static CDFImpl getCDF(String fname) throws Throwable {
        return getCDF(fname, false);
    }

    /**
     *
     * @param value
     */
    public static void setMaxMappedMemory(long value) {

        if (maxMappedMemory != null) {

            if (maxMappedMemory > value) {
                return;
            }

        }

        maxMappedMemory = value;
    }

    /**
     * creates CDFImpl object from a byte array.
     */
    static CDFImpl getCDF(byte[] ba) throws Throwable {
        ByteBuffer buf;

        synchronized (ba) {
            buf = ByteBuffer.allocateDirect(ba.length);
            buf.put(ba);
        }

        buf.flip();
        return getVersion(buf);
    }

    static CDFImpl getCDF(ByteBuffer buf) throws Throwable {
        ByteBuffer rbuf;

        synchronized (buf) {
            ByteBuffer _buf = ByteBuffer.allocateDirect(buf.remaining());
            _buf.put(buf);
            _buf.position(0);
            rbuf = _buf.asReadOnlyBuffer();
            rbuf.order(buf.order());
        }

        return getVersion(rbuf);
    }

    static CDFImpl getCDF(final String fname, final boolean option) throws Throwable {
        clean();
        File file = new File(fname);
        final String _fname = file.getPath();
        ByteBuffer buf;

        try (FileInputStream fis = new FileInputStream(file)) {
            FileChannel ch = fis.getChannel();
            buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        }

        CDFImpl cdf = getVersion(buf);
        cdf.setOption(new ProcessingOption() {
        });
        cdf.setSource(new CDFSource() {

            @Override
            public String getName() {
                return _fname;
            }

            @Override
            public boolean isFile() {
                return true;
            }
        });
        cdfMap.put(cdf, _fname);
        return cdf;
    }

    /**
     * creates CDFImpl object from a URL.
     */
    static CDFImpl getCDF(URL url) throws Throwable {
        final String _url = url.toString();
        URLConnection con = new CDFUrl(url).openConnection();
        int remaining = con.getContentLength();
        InputStream is = con.getInputStream();
        byte[] ba = new byte[remaining];
        int offset = 0;

        while (remaining > 0) {
            int got = is.read(ba, offset, remaining);
            offset += got;
            remaining -= got;
        }

        CDFImpl cdf = getCDF(ba);
        cdf.setSource(new CDFSource() {

            @Override
            public String getName() {
                return _url;
            }

            @Override
            public boolean isFile() {
                return false;
            }
        });
        return cdf;
    }

    static CDFImpl getVersion(ByteBuffer buf) throws Throwable {
        LongBuffer lbuf = buf.asLongBuffer();
        long magic = lbuf.get();

        if (magic == CDF3_MAGIC) {
            return new CDF3Impl(buf);
        }

        if (magic == CDF3_COMPRESSED_MAGIC) {
            ByteBuffer mbuf = uncompressed(buf, 3);
            return new CDF3Impl(mbuf);
        }

        if (magic == CDF2_MAGIC_DOT5) {
            int release = buf.getInt(24);
            return new CDF2Impl(buf, release);
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

        return null;
    }

    static ByteBuffer uncompressed(ByteBuffer buf, int version) {
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
            System.out.println(ex.toString());
            return null;
        }

        if (n < 0) {
            return null;
        }

        return ByteBuffer.wrap(udata);
    }

    private static long mappedMemoryUsed() {

        if (cdfMap.size() == 0) {
            return 0;
        }

        Set set = cdfMap.keySet();
        Iterator it = set.iterator();
        long size = 0;

        while (it.hasNext()) {
            size += ((CDFImpl) it.next()).getBuffer().limit();
        }

        return size;
    }

    /**
     *
     */
    public static class CDFSource {

        /**
         *
         * @return
         */
        public String getName() {
            return "";
        }

        /**
         *
         * @return
         */
        public boolean isByteArray() {
            return false;
        }

        /**
         *
         * @return
         */
        public boolean isByteBuffer() {
            return false;
        }

        /**
         *
         * @return
         */
        public boolean isFile() {
            return false;
        }

        /**
         *
         * @return
         */
        public boolean isURL() {
            return false;
        }
    }

    /**
     *
     */
    public static class ProcessingOption {

        String missingRecordsOption() {
            return "reject";
        }
    }
}
