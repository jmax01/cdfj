package gov.nasa.gsfc.spdf.cdfj;
import java.util.*;

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
     * @throws Throwable
     */
    public ArrayAttribute(Object data) throws Throwable {
        cl = data.getClass();
        if (!cl.isArray()) throw new Throwable("AArray: Object " + data +
            " is not an array");
        o = data;
        while (cl.isArray()) {
            cl = cl.getComponentType();
            if (cl.isPrimitive()) {            
                if (cl == Double.TYPE) {
                    dim.add(((double[])o).length);
                    break;
                }
                if (cl == Float.TYPE) {
                    dim.add(((float[])o).length);
                    break;
                }
                if (cl == Integer.TYPE) {
                    dim.add(((int[])o).length);
                    break;
                }
                if (cl == Byte.TYPE) {
                    dim.add(((byte[])o).length);
                    break;
                }
                if (cl == Short.TYPE) {
                    dim.add(((short[])o).length);
                    break;
                }
                if (cl == Long.TYPE) {
                    dim.add(((long[])o).length);
                    break;
                }
            } 
            Object[] _o = (Object[])o;
            o = _o[0];
            dim.add(_o.length);                
        }
    }

    /**
     *
     * @return
     */
    public Class<?> getType() {return cl;}

    /**
     *
     * @return
     */
    public int[] getDimensions() {
        int[] ia = new int[dim.size()];
        for (int i = 0; i < ia.length; i++) {
            ia[i] = (dim.get(i));
        }
        return ia;
    }

    /**
     *
     * @param sa
     * @throws Throwable
     */
    public  void toStringArray(String[] sa) throws Throwable {
        if (cl == String.class) {
            String[] sin = (String[]) o;
            if (sa.length == sin.length) {
                System.arraycopy(sin, 0, sa, 0, sin.length);
                return;
            }
            throw new Throwable("Length of the receiver array does not " +
            "match length.");
        }
        throw new Throwable("Method not appropriate for objects of type " + cl);
    }

    /**
     *
     * @param la
     * @throws Throwable
     */
    public void  toLongArray(long[] la) throws Throwable {
        if (cl == Long.TYPE) {
            long[] lin = (long[]) o;
            if (la.length == lin.length) {
                System.arraycopy(lin, 0, la, 0, lin.length);
                return;
            }
            throw new Throwable("Length of the receiver array does not " +
            "match length.");
        }
        throw new Throwable("Method not appropriate for objects of type " + cl);
    }

    /**
     *
     * @param da
     * @throws Throwable
     */
    public void  toDoubleArray(double[] da) throws Throwable {
        if (cl == Double.TYPE) {
            double[] din = (double[]) o;
            if (da.length == din.length) {
                System.arraycopy(din, 0, da, 0, din.length);
                return;
            }
            throw new Throwable("Length of the receiver array does not " +
            "match length.");
        }
        throw new Throwable("Method not appropriate for objects of type " + cl);
    }
}
