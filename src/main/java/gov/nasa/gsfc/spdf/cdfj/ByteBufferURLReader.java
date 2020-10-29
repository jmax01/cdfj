package gov.nasa.gsfc.spdf.cdfj;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * @author nand
 */
public class ByteBufferURLReader {

    InputStream is;

    boolean eof = false;

    /**
     *
     */
    public int total;

    /**
     *
     */
    public int len = -1;

    Chunk chunk = new Chunk();

    byte[] block = this.chunk.getBlock();

    FileChannel cacheFileChannel;

    ByteBuffer buffer;

    /**
     *
     * @param url
     *
     * @throws IOException
     */
    public ByteBufferURLReader(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.connect();
        this.len = con.getContentLength();

        if (this.len >= 0) {
            this.chunk.setLength(this.len);
        }

        this.is = con.getInputStream();
        boolean gzipped = url.getPath().trim().endsWith(".gz");

        if (gzipped) {
            this.is = new GZIPInputStream(this.is);
        }

    }

    /**
     *
     * @param url
     * @param chunk
     *
     * @throws IOException
     */
    public ByteBufferURLReader(URL url, Chunk chunk) throws IOException {
        this(url);
        setChunk(chunk);
    }

    /**
     *
     * @param url
     * @param fc
     *
     * @throws IOException
     */
    public ByteBufferURLReader(URL url, FileChannel fileChannel) throws IOException {
        this(url);
        this.cacheFileChannel = fileChannel;
        this.buffer = this.chunk.allocateBuffer();
    }

    /**
     *
     * @param url
     * @param fc
     * @param chunk
     *
     * @throws IOException
     */
    public ByteBufferURLReader(URL url, FileChannel fileChannel, Chunk chunk) throws IOException {
        this(url, chunk);
        this.cacheFileChannel = fileChannel;
        this.buffer = chunk.allocateBuffer();
    }

    /**
     *
     * @return
     */
    public boolean endOfFile() {
        return this.eof;
    }

    /**
     *
     * @return
     *
     * @throws IOException
     */
    public ByteBuffer getBuffer() throws IOException {
        Vector<ByteBuffer> buffers = new Vector<>();

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
     *
     * @return
     *
     * @throws IOException
     */
    public ByteBuffer read() throws IOException {
        ByteBuffer buf = this.chunk.allocateBuffer();
        _read(buf);
        return buf;
    }

    /**
     *
     * @param chunk
     */
    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        this.block = chunk.getBlock();
    }

    /**
     *
     * @throws IOException
     */
    public void transfer() throws IOException {
        _read(this.buffer);
        this.cacheFileChannel.write(this.buffer);
    }

    private void _read(ByteBuffer buffer) throws IOException {
        int count = 0;
        int n;
        buffer.position(0);
        buffer.limit(buffer.capacity());

        if (buffer.capacity() < (2 * this.block.length)) {

            for (int i = 0; i < buffer.capacity(); i++) {
                n = this.is.read();

                if (n == -1) {
                    throw new IOException("Premature end of data");
                }

                buffer.put((byte) n);
            }

            if ((n = this.is.read(this.block)) != -1) {
                throw new IOException("Unread data remains");
            }

            count = buffer.capacity();
            this.total = count;
            this.eof = true;
        } else {

            while ((n = this.is.read(this.block)) != -1) {
                buffer.put(this.block, 0, n);
                this.total += n;
                count += n;

                if (this.len == buffer.capacity()) {
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

            buffer.limit(buffer.position());
        }

        buffer.position(0);
    }

    /**
     *
     */
    public static class Chunk {

        int chunkSize = 1_024 * 1_024;

        int blockSize = 64 * 1_024;

        int len = -1;

        /**
         *
         */
        public Chunk() {
        }

        /**
         *
         * @param i
         * @param i1
         *
         * @throws Throwable
         */
        public Chunk(int blockSize, int chunkSize) throws Throwable {

            if (chunkSize < blockSize) {
                throw new Throwable("Chunk size must be >= block size");
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

        void setLength(int length) {
            this.len = length;
        }
    }
}
