package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 *
 * @author nand
 */
public abstract class AArray {

    ArrayAttribute aa;

    Object o;

    int dim;

    boolean rowMajority = true;

    /**
     *
     * @param o
     *
     * @throws Throwable
     */
    public AArray(Object o) throws Throwable {
        this(o, true);
    }

    /**
     *
     * @param o
     * @param bln
     *
     * @throws Throwable
     */
    public AArray(Object o, boolean rowMajority) throws Throwable {
        Class<?> cl = o.getClass();

        if (!cl.isArray()) {
            throw new Throwable("AArray: Object " + o + " is not an array");
        }

        this.o = o;
        this.aa = new ArrayAttribute(o);
        this.dim = this.aa.getDimensions().length;
        this.rowMajority = rowMajority;
    }

    /**
     *
     * @param o
     *
     * @return
     *
     * @throws Throwable
     */
    public static Object getPoint(Object o) throws Throwable {
        ArrayAttribute aa = new ArrayAttribute(o);
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

        Array.set(a, 0, o);
        return a;
    }

    /**
     *
     * @return
     */
    public abstract Object array();

    /**
     *
     * @return
     *
     * @throws Throwable
     */
    public ByteBuffer buffer() throws Throwable {

        if (this.aa.getType() == String.class) {
            throw new Throwable("Invalid call for String type");
        }

        return buffer(this.aa.getType(), 0);
    }

    /**
     *
     * @param cl
     *
     * @return
     *
     * @throws Throwable
     */
    public ByteBuffer buffer(Class<?> cl) throws Throwable {
        return buffer(cl, 0);
    }

    /**
     *
     * @param cl
     * @param size
     *
     * @return
     *
     * @throws Throwable
     */
    public abstract ByteBuffer buffer(Class<?> cl, int size) throws Throwable;

    /**
     *
     * @param size
     *
     * @return
     *
     * @throws Throwable
     */
    public ByteBuffer buffer(int size) throws Throwable {

        if (this.aa.getType() == String.class) {
            return buffer(String.class, size);
        }

        return buffer();
    }

    /**
     *
     * @return
     */
    public int[] getDimensions() {
        return this.aa.getDimensions();
    }

    /**
     *
     * @param dimensions
     *
     * @return
     */
    public boolean validateDimensions(int[] dimensions) {
        return Arrays.equals(dimensions, this.aa.getDimensions());
    }

    ByteBuffer allocate(int elementSize) {
        int size = elementSize;
        int[] _dim = this.aa.getDimensions();

        for (int j : _dim) {
            size *= j;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(size);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf;
    }
}
