package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * The Class AArray.
 *
 * @author nand
 */
public abstract class AArray {

    final ArrayAttribute attributeArray;

    final Object data;

    final int dimensions;

    final boolean rowMajority;

    /**
     * Instantiates a new a array.
     *
     * @param data the data
     */
    public AArray(final Object data) {
        this(data, true);
    }

    /**
     * Instantiates a new a array.
     *
     * @param data        the data
     * @param rowMajority the row majority
     */
    public AArray(final Object data, final boolean rowMajority) {

        Class<?> cl = data.getClass();

        if (!cl.isArray()) {
            throw new IllegalArgumentException("AArray: Object " + cl + " is not an array");
        }

        this.data = data;
        this.attributeArray = new ArrayAttribute(data);
        this.dimensions = this.attributeArray.getDimensions().length;
        this.rowMajority = rowMajority;
    }

    /**
     * Gets the point.
     *
     * @param data the data
     *
     * @return the point
     */
    public static Object getPoint(final Object data) {

        ArrayAttribute aa = new ArrayAttribute(data);

        int[] dim = aa.getDimensions();

        Object a = null;

        if (dim.length == 1) {
            a = Array.newInstance(aa.getType(), 1, dim[0]);
        }

        if (dim.length == 2) {
            a = Array.newInstance(aa.getType(), 1, dim[0], dim[1]);
        }

        if (dim.length == 3) {
            a = Array.newInstance(aa.getType(), 1, dim[0], dim[1], dim[2]);
        }

        if (dim.length == 4) {
            a = Array.newInstance(aa.getType(), 1, dim[0], dim[1], dim[2], dim[3]);
        }

        if (a == null) {
            return null;
        }

        Array.set(a, 0, data);
        return a;
    }

    /**
     * Array.
     *
     * @return the object
     */
    public final Object array() {

        switch (this.dimensions) {
            case 1:
            case 2:
            case 3:
            case 4:
                return this.data;
            default:
                throw new IllegalStateException("Rank > 4 not supported");

        }

    }

    /**
     * Buffer.
     *
     * @return the byte buffer
     */
    public ByteBuffer buffer() {

        if (this.attributeArray.getType() == String.class) {
            throw new IllegalStateException("Invalid call for String type");
        }

        return buffer(this.attributeArray.getType(), 0);
    }

    /**
     * Buffer.
     *
     * @param cl the cl
     *
     * @return the byte buffer
     */
    public ByteBuffer buffer(final Class<?> cl) {
        return buffer(cl, 0);
    }

    /**
     * Buffer.
     *
     * @param cl   the cl
     * @param size the size
     *
     * @return the byte buffer
     */
    public abstract ByteBuffer buffer(Class<?> cl, int size);

    /**
     * Buffer.
     *
     * @param size the size
     *
     * @return the byte buffer
     */
    public ByteBuffer buffer(final int size) {

        if (this.attributeArray.getType() == String.class) {
            return buffer(String.class, size);
        }

        return buffer();
    }

    /**
     * Gets the dimensions.
     *
     * @return the dimensions
     */
    public int[] getDimensions() {
        return this.attributeArray.getDimensions();
    }

    /**
     * Validate dimensions.
     *
     * @param dimensions the dimensions
     *
     * @return true, if successful
     */
    public boolean validateDimensions(final int[] _dimensions) {
        return Arrays.equals(_dimensions, this.attributeArray.getDimensions());
    }

    ByteBuffer allocate(final int elementSize) {
        int size = elementSize;
        int[] _dim = this.attributeArray.getDimensions();

        for (int j : _dim) {
            size *= j;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(size);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf;
    }
}
