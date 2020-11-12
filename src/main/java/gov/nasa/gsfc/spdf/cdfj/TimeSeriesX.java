package gov.nasa.gsfc.spdf.cdfj;

/**
 * The Interface TimeSeriesX.
 *
 * @author nand
 */
public interface TimeSeriesX extends TimeSeries {

    /**
     * Checks if is column major.
     *
     * @return true, if is column major
     */
    boolean isColumnMajor();

    /**
     * Checks if is one D.
     *
     * @return true, if is one D
     */
    boolean isOneD();
}
