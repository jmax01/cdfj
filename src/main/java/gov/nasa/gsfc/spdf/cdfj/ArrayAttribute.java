package gov.nasa.gsfc.spdf.cdfj;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Class ArrayAttribute.
 *
 * @author nand
 */
public class ArrayAttribute {

    final CopyOnWriteArrayList<Integer> dim = new CopyOnWriteArrayList<>();

    final Class<?> cl;

    Object data;

    /**
     * Instantiates a new array attribute.
     *
     * @param data the data must be an array
     */
    public ArrayAttribute(final Object data) {

        Class<?> clazz = data.getClass();

        if (!clazz.isArray()) {
            throw new IllegalArgumentException("AArray: Object " + data + " is not an array");
        }

        this.data = data;

        while (clazz.isArray()) {

            clazz = clazz.getComponentType();

            if (clazz.isPrimitive()) {

                if (Double.TYPE.equals(clazz)) {
                    this.dim.add(((double[]) this.data).length);
                    break;
                }

                if (Float.TYPE.equals(clazz)) {
                    this.dim.add(((float[]) this.data).length);
                    break;
                }

                if (Integer.TYPE.equals(clazz)) {
                    this.dim.add(((int[]) this.data).length);
                    break;
                }

                if (Byte.TYPE.equals(clazz)) {
                    this.dim.add(((byte[]) this.data).length);
                    break;
                }

                if (Short.TYPE.equals(clazz)) {
                    this.dim.add(((short[]) this.data).length);
                    break;
                }

                if (Long.TYPE.equals(clazz)) {
                    this.dim.add(((long[]) this.data).length);
                    break;
                }

            }

            Object[] _o = (Object[]) this.data;
            this.data = _o[0];
            this.dim.add(_o.length);
        }

        this.cl = clazz;
    }

    /**
     * Gets the dimensions.
     *
     * @return the dimensions
     */
    public int[] getDimensions() {
        int[] ia = new int[this.dim.size()];

        for (int i = 0; i < ia.length; i++) {
            ia[i] = (this.dim.get(i));
        }

        return ia;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Class<?> getType() {
        return this.cl;
    }

    /**
     * To double array.
     *
     * @param arrayToPopulate the da
     */
    public void toDoubleArray(final double[] arrayToPopulate) {

        if (this.cl == Double.TYPE) {

            double[] din = (double[]) this.data;

            if (arrayToPopulate.length == din.length) {
                System.arraycopy(din, 0, arrayToPopulate, 0, din.length);
                return;
            }

            throw new IllegalArgumentException("Length of the receiver array does not match length.");
        }

        throw new IllegalArgumentException("Method not appropriate for objects of type " + this.cl);
    }

    /**
     * To long array.
     *
     * @param arrayToPopulate the array to populate
     */
    public void toLongArray(final long[] arrayToPopulate) {

        if (this.cl == Long.TYPE) {
            long[] lin = (long[]) this.data;

            if (arrayToPopulate.length == lin.length) {
                System.arraycopy(lin, 0, arrayToPopulate, 0, lin.length);
                return;
            }

            throw new IllegalArgumentException("Length of the receiver array does not match length.");
        }

        throw new IllegalArgumentException("Method not appropriate for objects of type " + this.cl);
    }

    /**
     * To string array.
     *
     * @param arrayToPopulate the array to populate
     */
    public void toStringArray(final String[] arrayToPopulate) {

        if (this.cl == String.class) {
            String[] sin = (String[]) this.data;

            if (arrayToPopulate.length == sin.length) {
                System.arraycopy(sin, 0, arrayToPopulate, 0, sin.length);
                return;
            }

            throw new IllegalArgumentException("Length of the receiver array does not match length.");
        }

        throw new IllegalArgumentException("Method not appropriate for objects of type " + this.cl);
    }
}
