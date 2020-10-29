package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public class Stride {

    int[] stride;

    int nv;

    /**
     *
     * @param ints
     */
    public Stride(int[] stride) {

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
     *
     * @return
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
     *
     * @param nv
     *
     * @return
     */
    public int getStride(int nv) {
        this.nv = nv;
        return getStride();
    }
}
