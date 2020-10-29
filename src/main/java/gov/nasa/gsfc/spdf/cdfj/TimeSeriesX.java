package gov.nasa.gsfc.spdf.cdfj;

/**
 *
 * @author nand
 */
public interface TimeSeriesX extends TimeSeries {

    /**
     *
     * @return
     */
    boolean isColumnMajor();

    /**
     *
     * @return
     */
    boolean isOneD();
}
