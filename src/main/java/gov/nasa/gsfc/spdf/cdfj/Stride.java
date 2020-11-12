package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Class Stride.
 *
 * @author nand
 */
public class Stride {

    int[] stride;

    int nv;

    /**
     * Instantiates a new stride.
     *
     * @param stride the stride
     */
    public Stride(final int[] stride) {

        if (stride.length == 0) {
            this.stride = null;
            return;
        }

        if (stride.length == 1) {
            this.stride = new int[] { stride[0] };
        } else {
            this.stride = new int[] { stride[0], stride[1] };
        }

    }

    /**
     * Gets the stride.
     *
     * @return the stride
     */
    public int getStride() {
        int _stride = 1;

        if (this.stride != null) {

            if (this.stride[0] > 0) {
                _stride = this.stride[0];
            } else {

                if (this.nv > this.stride[1]) {
                    _stride = (this.nv / this.stride[1]);

                    if ((_stride * this.stride[1]) < this.nv) {
                        _stride++;
                    }

                }

            }

        }

        return _stride;
    }

    /**
     * Gets the stride.
     *
     * @param nv the nv
     *
     * @return the stride
     */
    public int getStride(final int nv) {
        this.nv = nv;
        return getStride();
    }
}
