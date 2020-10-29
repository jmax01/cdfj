package gov.nasa.gsfc.spdf.cdfj;

import java.util.Vector;

/**
 *
 * @author nand
 */
public class ArrayAttribute {

    Vector<Integer> dim = new Vector<>();

    Class<?> cl;

    Object o;

    /**
     *
     * @param o
     *
     * @throws Throwable
     */
    public ArrayAttribute(Object data) throws Throwable {
        this.cl = data.getClass();

        if (!this.cl.isArray()) {
            throw new Throwable("AArray: Object " + data + " is not an array");
        }

        this.o = data;

        while (this.cl.isArray()) {
            this.cl = this.cl.getComponentType();

            if (this.cl.isPrimitive()) {

                if (this.cl == Double.TYPE) {
                    this.dim.add(((double[]) this.o).length);
                    break;
                }

                if (this.cl == Float.TYPE) {
                    this.dim.add(((float[]) this.o).length);
                    break;
                }

                if (this.cl == Integer.TYPE) {
                    this.dim.add(((int[]) this.o).length);
                    break;
                }

                if (this.cl == Byte.TYPE) {
                    this.dim.add(((byte[]) this.o).length);
                    break;
                }

                if (this.cl == Short.TYPE) {
                    this.dim.add(((short[]) this.o).length);
                    break;
                }

                if (this.cl == Long.TYPE) {
                    this.dim.add(((long[]) this.o).length);
                    break;
                }

            }

            Object[] _o = (Object[]) this.o;
            this.o = _o[0];
            this.dim.add(_o.length);
        }

    }

    /**
     *
     * @return
     */
    public int[] getDimensions() {
        int[] ia = new int[this.dim.size()];

        for (int i = 0; i < ia.length; i++) {
            ia[i] = (this.dim.get(i));
        }

        return ia;
    }

    /**
     *
     * @return
     */
    public Class<?> getType() {
        return this.cl;
    }

    /**
     *
     * @param da
     *
     * @throws Throwable
     */
    public void toDoubleArray(double[] da) throws Throwable {

        if (this.cl == Double.TYPE) {
            double[] din = (double[]) this.o;

            if (da.length == din.length) {
                System.arraycopy(din, 0, da, 0, din.length);
                return;
            }

            throw new Throwable("Length of the receiver array does not " + "match length.");
        }

        throw new Throwable("Method not appropriate for objects of type " + this.cl);
    }

    /**
     *
     * @param la
     *
     * @throws Throwable
     */
    public void toLongArray(long[] la) throws Throwable {

        if (this.cl == Long.TYPE) {
            long[] lin = (long[]) this.o;

            if (la.length == lin.length) {
                System.arraycopy(lin, 0, la, 0, lin.length);
                return;
            }

            throw new Throwable("Length of the receiver array does not " + "match length.");
        }

        throw new Throwable("Method not appropriate for objects of type " + this.cl);
    }

    /**
     *
     * @param sa
     *
     * @throws Throwable
     */
    public void toStringArray(String[] sa) throws Throwable {

        if (this.cl == String.class) {
            String[] sin = (String[]) this.o;

            if (sa.length == sin.length) {
                System.arraycopy(sin, 0, sa, 0, sin.length);
                return;
            }

            throw new Throwable("Length of the receiver array does not " + "match length.");
        }

        throw new Throwable("Method not appropriate for objects of type " + this.cl);
    }
}
