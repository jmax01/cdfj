package gov.nasa.gsfc.spdf.cdfj;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * The Class ByteBufferURLReader.
 *
 * @author nand
 */
public class ByteBufferURLReader {

    InputStream is;

    boolean eof = false;

    private int total;

    private int len = -1;

    Chunk chunk = new Chunk();

    byte[] block = this.chunk.getBlock();

    FileChannel cacheFileChannel;

    ByteBuffer buffer;

    /**
     * Instantiates a new byte buffer URL reader.
     *
     * @param url the url
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBufferURLReader(final URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.connect();
        this.len = con.getContentLength();

        if (this.len >= 0) {
            this.chunk.setLength(this.len);
        }

        this.is = con.getInputStream();
        boolean gzipped = url.getPath()
                .trim()
                .endsWith(".gz");

        if (gzipped) {
            this.is = new GZIPInputStream(this.is);
        }

    }

    /**
     * Instantiates a new byte buffer URL reader.
     *
     * @param url   the url
     * @param chunk the chunk
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBufferURLReader(final URL url, final Chunk chunk) throws IOException {
        this(url);
        setChunk(chunk);
    }

    /**
     * Instantiates a new byte buffer URL reader.
     *
     * @param url         the url
     * @param fileChannel the file channel
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBufferURLReader(final URL url, final FileChannel fileChannel) throws IOException {
        this(url);
        this.cacheFileChannel = fileChannel;
        this.buffer = this.chunk.allocateBuffer();
    }

    /**
     * Instantiates a new byte buffer URL reader.
     *
     * @param url         the url
     * @param fileChannel the file channel
     * @param chunk       the chunk
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBufferURLReader(final URL url, final FileChannel fileChannel, final Chunk chunk) throws IOException {
        this(url, chunk);
        this.cacheFileChannel = fileChannel;
        this.buffer = chunk.allocateBuffer();
    }

    /**
     * End of file.
     *
     * @return true, if successful
     */
    public boolean endOfFile() {
        return this.eof;
    }

    /**
     * Gets the buffer.
     *
     * @return the buffer
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBuffer getBuffer() throws IOException {

        List<ByteBuffer> buffers = new ArrayList<>();

        while (!this.eof) {

            if (this.cacheFileChannel == null) {
                buffers.add(read());
            } else {
                transfer();
            }

        }

        if (this.cacheFileChannel != null) {
            long pos = this.cacheFileChannel.position();
            FileChannel.MapMode mode = FileChannel.MapMode.READ_ONLY;
            return this.cacheFileChannel.map(mode, 0L, pos);
        }

        if (buffers.size() == 1) {
            ByteBuffer _buf = buffers.get(0);
            return _buf.asReadOnlyBuffer();
        }

        int size = 0;

        for (ByteBuffer _buf : buffers) {
            size += _buf.remaining();
        }

        ByteBuffer ball = ByteBuffer.allocateDirect(size);

        for (ByteBuffer _buf : buffers) {
            ball.put(_buf);
        }

        ball.position(0);
        return ball.asReadOnlyBuffer();
    }

    /**
     * Read.
     *
     * @return the byte buffer
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBuffer read() throws IOException {
        ByteBuffer buf = this.chunk.allocateBuffer();
        _read(buf);
        return buf;
    }

    /**
     * Sets the chunk.
     *
     * @param chunk the new chunk
     */
    public void setChunk(final Chunk chunk) {
        this.chunk = chunk;
        this.block = chunk.getBlock();
    }

    /**
     * Transfer.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void transfer() throws IOException {
        _read(this.buffer);
        this.cacheFileChannel.write(this.buffer);
    }

    private void _read(final ByteBuffer buff) throws IOException {
        int count = 0;
        int n;
        buff.position(0);
        buff.limit(buff.capacity());

        if (buff.capacity() < (2 * this.block.length)) {

            for (int i = 0; i < buff.capacity(); i++) {
                n = this.is.read();

                if (n == -1) {
                    throw new IOException("Premature end of data");
                }

                buff.put((byte) n);
            }

            if ((n = this.is.read(this.block)) != -1) {
                throw new IOException("Unread data remains");
            }

            count = buff.capacity();
            this.total = count;
            this.eof = true;
        } else {

            while ((n = this.is.read(this.block)) != -1) {
                buff.put(this.block, 0, n);
                this.total += n;
                count += n;

                if (this.len == buff.capacity()) {
                    continue;
                }

                if (count >= this.chunk.chunkSize) {
                    break;
                }

            }

            if (n == -1) {
                this.eof = true;
            }

            if (this.eof && (this.len >= 0)) {

                if (this.total != this.len) {
                    throw new IOException("Mismatched length " + this.total + " expected: " + this.len);
                }

            }

            buff.limit(buff.position());
        }

        buff.position(0);
    }

    /**
     * The Class Chunk.
     */
    public static class Chunk {

        private static final int DEFAULT_BLOCK_SIZE = 64 * 1_024;

        private static final int DEFAULT_CHUNK_SIZE = 1_024 * 1_024;

        final int chunkSize;

        final int blockSize;

        int len = -1;

        /**
         * Instantiates a new chunk.
         */
        public Chunk() {
            this.chunkSize = DEFAULT_CHUNK_SIZE;
            this.blockSize = DEFAULT_BLOCK_SIZE;
        }

        /**
         * Instantiates a new chunk.
         *
         * @param blockSize the block size
         * @param chunkSize the chunk size must be greater that blockSize
         */
        public Chunk(final int blockSize, final int chunkSize) {

            // FIXME: Message does not match logic
            if (chunkSize < blockSize) {
                throw new IllegalArgumentException(
                        "chunkSize(" + chunkSize + ") must be >= blockSize(" + blockSize + ')');
            }

            this.blockSize = blockSize;
            this.chunkSize = chunkSize;
        }

        ByteBuffer allocateBuffer() {

            int bufsize = this.chunkSize + this.blockSize;

            if (this.len < 0) {
                return ByteBuffer.allocateDirect(bufsize);
            }

            if (this.len > bufsize) {
                return ByteBuffer.allocateDirect(bufsize);
            }

            return ByteBuffer.allocateDirect(this.len);
        }

        byte[] getBlock() {
            return new byte[this.blockSize];
        }

        void setLength(final int length) {
            this.len = length;
        }
    }
}
