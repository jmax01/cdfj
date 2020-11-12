package gov.nasa.gsfc.spdf.cdfj;

import gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError;

/**
 * Time series specification for one-dimensional representation of values.
 * TimeSeriesOneD objects are returned by the getTimeSeriesOneD method of
 * CDFReader.
 */
public interface TimeSeriesOneD {

    /**
     *
     */
    boolean ONED = true;

    /**
     * Returns time instant model used to derive times returned
     * by {@link #getTimes() getTimes()}.
     *
     * @return the time instant model
     */
    TimeInstantModel getTimeInstantModel();

    /**
     * Returns times according to the
     * {@link TimeInstantModel time instant model}
     * returned by {@link #getTimeInstantModel() getTimeInstantModel()}.
     *
     * @return the times
     *
     * @throws ReaderError the reader error
     *
     * @see CDFReader#timeModelInstance()
     */
    double[] getTimes() throws CDFException.ReaderError;

    /**
     * Returns one dimensional representation of the values of the variable
     * at times returned by getTimes().
     * <p>
     * Returned array represents multi dimensional arrays in a manner
     * determined by the value returned by {@link #isColumnMajor()
     * isColumnMajor()} method.
     * </p>
     *
     * @return the values
     *
     * @throws ReaderError the reader error
     */
    double[] getValues() throws CDFException.ReaderError;

    /**
     * Returns whether the array returned by getValues() is to be
     * interpreted as having the first index of variable's dimension varying
     * the fastest, as in IDL.
     *
     * @return true, if is column major
     */
    boolean isColumnMajor();
}
