package gov.nasa.gsfc.spdf.cdfj;

// import gov.nasa.gsfc.spdf.common.*;
import java.nio.ByteBuffer;

/**
 * Data Container for a variable
 */
public interface VDataContainer extends Runnable {

    /**
     * Returns the one dimensional array representation.
     *
     * @return
     */
    Object as1DArray();

    /**
     *
     * @return
     *
     * @throws Throwable
     */
    AArray asArray() throws Throwable;

    /**
     *
     * @param cmtarget
     *
     * @return
     */
    Object asOneDArray(boolean cmtarget);

    /**
     * Returns ByteBuffer for this container.
     *
     * @return
     */
    ByteBuffer getBuffer();

    /**
     *
     * @return
     */
    int getCapacity();

    /**
     * Returns range of records in this container.
     *
     * @return
     */
    int[] getRecordRange();

    /**
     * Returns the {@link Variable Variable} for this container.
     *
     * @return
     */
    Variable getVariable();

    /**
     *
     * @param direct
     */
    void setDirect(boolean direct);

    /**
     *
     * @param buffer
     *
     * @return
     */
    boolean setUserBuffer(ByteBuffer buffer);

    /**
     * Byte Data Container.
     */
    public interface CByte extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        byte[] as1DArray();

        /**
         *
         * @return
         */
        byte[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        byte[] asOneDArray(boolean cmtarget);

        /**
         * Returns the multi dimensional array representation.
         */
        // public ByteArray asArray() throws Throwable ;
    }

    /**
     * Double Data Container.
     */
    public interface CDouble extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        double[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         */
        @Override
        DoubleArray asArray() throws Throwable;

        /**
         *
         * @return
         */
        double[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        double[] asOneDArray(boolean cmtarget);
    }

    /**
     * Float Data Container.
     */
    public interface CFloat extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        float[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         */
        @Override
        FloatArray asArray() throws Throwable;

        /**
         *
         * @return
         */
        float[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        float[] asOneDArray(boolean cmtarget);
    }

    /**
     * Int Data Container.
     */
    public interface CInt extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        int[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         */
        @Override
        IntArray asArray() throws Throwable;

        /**
         *
         * @return
         */
        int[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        int[] asOneDArray(boolean cmtarget);
    }

    /**
     * Long Data Container.
     */
    public interface CLong extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        long[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         */
        @Override
        LongArray asArray() throws Throwable;

        /**
         *
         * @return
         */
        long[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        long[] asOneDArray(boolean cmtarget);
    }

    /**
     * Short Data Container.
     */
    public interface CShort extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        short[] as1DArray();

        /**
         * Returns the multi dimensional array representation.
         */
        @Override
        ShortArray asArray() throws Throwable;

        /**
         *
         * @return
         */
        short[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        short[] asOneDArray(boolean cmtarget);
    }

    /**
     * String Data Container.
     */
    public interface CString extends VDataContainer {

        /**
         * Returns the one dimensional array representation.
         *
         * @return
         */
        @Override
        byte[] as1DArray();

        /**
         *
         * @return
         */
        byte[] asOneDArray();

        /**
         *
         * @param cmtarget
         *
         * @return
         */
        @Override
        byte[] asOneDArray(boolean cmtarget);

        /**
         * Returns the multi dimensional array representation.
         */
        // public StringArray asArray() throws Throwable ;
    }
}
