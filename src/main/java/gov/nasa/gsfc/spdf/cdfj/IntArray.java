package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author nand
 */
public class IntArray extends AArray {

    /**
     *
     * @param o
     *
     * @throws Throwable
     */
    public IntArray(Object o) throws Throwable {
        super(o);
    }

    /**
     *
     * @param o
     * @param bln
     *
     * @throws Throwable
     */
    public IntArray(Object o, boolean majority) throws Throwable {
        super(o, majority);
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
     * create a byte buffer of a compatible type.
     *
     * @param ignore
     */
    @Override
    public ByteBuffer buffer(Class<?> cl, int ignore) throws Throwable {

        if (((cl != Integer.TYPE) && (cl != Short.TYPE))) {
            throw new Throwable("Only int and short targets supported");
        }

        if (this.dim > 4) {
            throw new Throwable("Rank > 4 not supported");
        }

        int elementSize = (cl == Short.TYPE) ? 2 : 4;
        ByteBuffer buf = allocate(elementSize);

        if (cl == Short.TYPE) {
            return doShort(buf);
        }

        return doInt(buf);
    }

    ByteBuffer doInt(ByteBuffer buf) {
        int[] _dim = this.aa.getDimensions();
        IntBuffer _buf = buf.asIntBuffer();

        switch (this.dim) {
            case 1:
                int[] data = (int[]) this.o;
                _buf.put(data);
                return buf;
            case 2:
                int[][] data2 = (int[][]) this.o;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                int[][][] data3 = (int[][][]) this.o;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            _buf.put(data3[i][j]);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                _buf.put(data3[i][j][k]);
                            }

                        }

                    }

                }
                return buf;
            case 4:
                int[][][][] data4 = (int[][][][]) this.o;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                _buf.put(data4[i][j][k]);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    _buf.put(data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                return buf;
        }

        return null;
    }

    ByteBuffer doShort(ByteBuffer buf) {
        int[] _dim = this.aa.getDimensions();
        short[] temp = null;
        ShortBuffer _buf = buf.asShortBuffer();

        switch (this.dim) {
            case 1:
                int[] data = (int[]) this.o;
                temp = new short[data.length];
                for (int i = 0; i < data.length; i++) {
                    temp[i] = (short) data[i];
                }
                _buf.put(temp);
                return buf;
            case 2:
                int[][] data2 = (int[][]) this.o;
                temp = new short[_dim[1]];
                for (int i = 0; i < _dim[0]; i++) {
                    int[] di = data2[i];

                    for (int j = 0; j < _dim[1]; j++) {
                        temp[j] = (short) di[j];
                    }

                    _buf.put(temp);
                }
                return buf;
            case 3:
                int[][][] data3 = (int[][][]) this.o;
                if (this.rowMajority) {
                    temp = new short[_dim[2]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            int[] di = data3[i][j];

                            for (int k = 0; k < _dim[2]; k++) {
                                temp[k] = (short) di[k];
                            }

                            _buf.put(temp);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                _buf.put((short) data3[i][j][k]);
                            }

                        }

                    }

                }
                return buf;
            case 4:
                int[][][][] data4 = (int[][][][]) this.o;
                if (this.rowMajority) {
                    temp = new short[_dim[3]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                int[] di = data4[i][j][k];

                                for (int l = 0; l < _dim[3]; l++) {
                                    temp[l] = (short) di[l];
                                }

                                _buf.put(temp);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    _buf.put((short) data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                return buf;
        }

        return null;
    }
}
