package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 *
 * @author nand
 */
public class ByteArray extends AArray {

    /**
     *
     * @param o
     *
     * @throws Throwable
     */
    public ByteArray(Object o) throws Throwable {
        super(o);
    }

    /**
     *
     * @param o
     * @param bln
     *
     * @throws Throwable
     */
    public ByteArray(Object o, boolean rowMajority) throws Throwable {
        super(o, rowMajority);
    }

    /**
     *
     * @return
     */
    @Override
    public Object array() {

        switch (this.dim) {
            case 1:
                return this.o;
            case 2:
                return this.o;
            case 3:
                return this.o;
            case 4:
                return this.o;
        }

        return null;
    }

    /**
     *
     * @param cl
     * @param ignore
     *
     * @return
     *
     * @throws Throwable
     */
    @Override
    public ByteBuffer buffer(Class<?> cl, int ignore) throws Throwable {

        if (cl != Byte.TYPE) {
            throw new Throwable("Only byte targets supported");
        }

        if (this.dim > 4) {
            throw new Throwable("Rank > 4 not supported");
        }

        ByteBuffer buf = allocate(1);
        int[] _dim = this.aa.getDimensions();

        switch (this.dim) {
            case 1:
                byte[] data = (byte[]) this.o;
                buf.put(data);
                buf.flip();
                return buf;
            case 2:
                byte[][] data2 = (byte[][]) this.o;
                for (int i = 0; i < _dim[0]; i++) {
                    buf.put(data2[i]);
                }
                buf.flip();
                return buf;
            case 3:
                byte[][][] data3 = (byte[][][]) this.o;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            buf.put(data3[i][j]);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                buf.put(data3[i][j][k]);
                            }

                        }

                    }

                }
                buf.flip();
                return buf;
            case 4:
                byte[][][][] data4 = (byte[][][][]) this.o;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                buf.put(data4[i][j][k]);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    buf.put(data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                buf.flip();
                return buf;
        }

        return null;
    }
}
