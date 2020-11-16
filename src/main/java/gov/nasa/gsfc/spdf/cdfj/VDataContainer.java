package gov.nasa.gsfc.spdf.cdfj;

// import gov.nasa.gsfc.spdf.common.*;
import java.nio.ByteBuffer;

/**
 * Data Container for a variable.
 */
public interface VDataContainer extends Runnable {

    /**
     * Returns the one dimensional array representation.
     *
     * @return the object
     */
    Object as1DArray();

    /**
     * As array.
     *
     * @return the a array
     * @
     */
    AArray asArray();

    /**
     * As one D array.
     *
     * @param cmtarget the cmtarget
     *
     * @return the object
     */
    Object asOneDArray(boolean cmtarget);

    /**
     * Returns ByteBuffer for this container.
     *
     * @return the buffer
     */
    ByteBuffer getBuffer();

    /**
     * Gets the capacity.
     *
     * @return the capacity
     */
    int getCapacity();

    /**
     * Returns range of records in this container.
     *
     * @return the record range
     */
    int[] getRecordRange();

    /**
     * Returns the {@link Variable Variable} for this container.
     *
     * @return the variable
     */
    Variable getVariable();

    /**
     * Sets the direct.
     *
     * @param direct the new direct
     */
    void setDirect(boolean direct);

    /**
     * Sets the user buffer.
     *
     * @param buffer the buffer
     *
     * @return true, if successful
     */
    boolean setUserBuffer(ByteBuffer buffer);

    /**
     * Byte Data Container.
     */
    interface CByte extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the byte[]
         */
        @Override
        byte[] as1DArray();

        /**
         * As one D array.
         *
         * @return the byte[]
         */
        byte[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the byte[]
         */
        @Override
        byte[] asOneDArray(boolean cmtarget);
    }

    /**
     * Double Data Container.
     */
    interface CDouble extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the double[]
         */
        @Override
        double[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         *
         * @return the double array
         */
        @Override
        DoubleArray asArray();

        /**
         * As one D array.
         *
         * @return the double[]
         */
        double[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the double[]
         */
        @Override
        double[] asOneDArray(boolean cmtarget);
    }

    /**
     * Float Data Container.
     */
    interface CFloat extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the float[]
         */
        @Override
        float[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         *
         * @return the float array
         */
        @Override
        FloatArray asArray();

        /**
         * As one D array.
         *
         * @return the float[]
         */
        float[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the float[]
         */
        @Override
        float[] asOneDArray(boolean cmtarget);
    }

    /**
     * Int Data Container.
     */
    interface CInt extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the int[]
         */
        @Override
        int[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         *
         * @return the int array
         */
        @Override
        IntArray asArray();

        /**
         * As one D array.
         *
         * @return the int[]
         */
        int[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the int[]
         */
        @Override
        int[] asOneDArray(boolean cmtarget);
    }

    /**
     * Long Data Container.
     */
    interface CLong extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the long[]
         */
        @Override
        long[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         *
         * @return the long array
         */
        @Override
        LongArray asArray();

        /**
         * As one D array.
         *
         * @return the long[]
         */
        long[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the long[]
         */
        @Override
        long[] asOneDArray(boolean cmtarget);
    }

    /**
     * Short Data Container.
     */
    interface CShort extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the short[]
         */
        @Override
        short[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         *
         * @return the short array
         */
        @Override
        ShortArray asArray();

        /**
         * As one D array.
         *
         * @return the short[]
         */
        short[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the short[]
         */
        @Override
        short[] asOneDArray(boolean cmtarget);
    }

    /**
     * String Data Container.
     */
    interface CString extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return the byte[]
         */
        @Override
        byte[] as1DArray();

        /**
         * As one D array.
         *
         * @return the byte[]
         */
        byte[] asOneDArray();

        /**
         * As one D array.
         *
         * @param cmtarget the cmtarget
         *
         * @return the byte[]
         */
        @Override
        byte[] asOneDArray(boolean cmtarget);
    }
}
