package gov.nasa.gsfc.spdf.cdfj;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError;

/**
 * GenericReader extends MetaData class with methods to access variable
 * data. Data access methods of this class do not require a detailed knowledge
 * of the structure of CDF. Derived class CDFReader extends this class with
 * methods to access
 * time series.
 */
public class GenericReader extends MetaData {

    private static final Logger LOGGER = Logger.getLogger("cdfj.genericreader");

    static final Map<String, Class<?>> SUPPORTED_CLASSES_BY_NAME = Collections
            .unmodifiableMap(initSupportedClassesByName());

    static {
        initSupportedClassesByName();
    }

    private ThreadGroup tgroup;

    private final Map<String, ThreadMapEntry> threadNameEntriesByThreadName = new ConcurrentHashMap<>();

    /**
     * Constructs a reader for the given CDF file.
     *
     * @param cdfFile the cdf file
     *
     * @throws ReaderError the reader error
     */
    public GenericReader(final String cdfFile) throws CDFException.ReaderError {
        LOGGER.entering("GenericReader", "constructor", cdfFile);
        File _file = new File(cdfFile);

        if (!_file.exists()) {
            throw new CDFException.ReaderError(cdfFile + " does not exist.");
        }

        if (_file.length() > Integer.MAX_VALUE) {
            throw new CDFException.ReaderError("Size of file " + cdfFile + " exceeds "
                    + "Integer.MAX_VALUE. If data for individual variables is less "
                    + "than this limit, you can use ReaderFactory.getReader(fileName) to get a "
                    + "GenericReader instance for this file.");
        }

        try {
            this.thisCDF = CDFFactory.getCDF(cdfFile);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

        LOGGER.exiting("GenericReader", "constructor");

    }

    /**
     * Constructs a reader for the given CDF URL.
     *
     * @param url the url
     *
     * @throws ReaderError the reader error
     */
    public GenericReader(final URL url) throws CDFException.ReaderError {

        try {
            this.thisCDF = CDFFactory.getCDF(url);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    protected GenericReader(final CDFImpl cdfImpl) {
        this.thisCDF = cdfImpl;
    }

    private static boolean coreNeeded(final VariableMetaData var) {
        return var.isMissingRecords();
    }

    private static boolean coreNeeded(final VariableMetaData var, final int[] range) {
        int[] available = var.getRecordRange();

        if (range.length == 1) {
            return (range[0] < available[0]) || var.isMissingRecords();
        }

        return (range[0] < available[0]) || (range[1] > available[1])
            || var.isMissingRecords();
    }

    private static Map<String, Class<?>> initSupportedClassesByName() {

        Map<String, Class<?>> supportedClassesByName = new HashMap<>();
        supportedClassesByName.put("long", Long.TYPE);
        supportedClassesByName.put("double", Double.TYPE);
        supportedClassesByName.put("float", Float.TYPE);
        supportedClassesByName.put("int", Integer.TYPE);
        supportedClassesByName.put("short", Short.TYPE);
        supportedClassesByName.put("byte", Byte.TYPE);
        supportedClassesByName.put("string", String.class);
        return supportedClassesByName;
    }

    /**
     * Returns all available values for the given scalar variable.
     * For variable of type long, loss of precision may occur.
     *
     * @param variableName variable name
     *
     * @return a double array
     *
     * @throws ReaderError the reader error
     */
    public final double[] asDouble0(final String variableName) throws CDFException.ReaderError {

        try {
            int ndim = getEffectiveDimensions(variableName).length;

            if (ndim != 0) {
                throw new CDFException.ReaderError("Use asDouble" + ndim + "(" + variableName + ") for " + ndim
                        + "-dimensional variable " + variableName);
            }

            Object o = get(variableName);
            double[] da;
            ArrayAttribute aa = new ArrayAttribute(o);

            if (aa.getType() == Long.TYPE) {
                long[] la = (long[]) o;
                da = new double[la.length];

                for (int i = 0; i < la.length; i++) {
                    da[i] = la[i];
                }

            } else {
                da = (double[]) o;
            }

            return da;
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns all available values for the given variable of rank 1.
     * For variable of type long, loss of precision may occur.
     *
     * @param variableName variable name
     *
     * @return a double[][]
     *
     * @throws ReaderError the reader error
     */
    public final double[][] asDouble1(final String variableName) throws CDFException.ReaderError {

        try {
            int ndim = getEffectiveDimensions(variableName).length;

            if (ndim != 1) {
                throw new CDFException.ReaderError("Use asDouble" + ndim + "(" + variableName + ") for " + ndim
                        + "-dimensional variable " + variableName);
            }

            return (double[][]) get(variableName);
        } catch (CDFException.ReaderError th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns all available values for the given variable of rank 2.
     * For variable of type long, loss of precision may occur.
     *
     * @param variableName variable name
     *
     * @return a double[][][]
     *
     * @throws ReaderError the reader error
     */
    public final double[][][] asDouble2(final String variableName) throws CDFException.ReaderError {

        try {
            int ndim = getEffectiveDimensions(variableName).length;

            if (ndim != 2) {
                throw new CDFException.ReaderError("Use asDouble" + ndim + "(" + variableName + ") for " + ndim
                        + "-dimensional variable " + variableName);
            }

            return (double[][][]) get(variableName);
        } catch (CDFException.ReaderError th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns all available values for the given variable of rank 3.
     * For variable of type long, loss of precision may occur.
     *
     * @param variableName variable name
     *
     * @return a double[][][][]
     *
     * @throws ReaderError the reader error
     */
    public final double[][][][] asDouble3(final String variableName) throws CDFException.ReaderError {

        try {
            int ndim = getEffectiveDimensions(variableName).length;

            if (ndim != 3) {
                throw new CDFException.ReaderError("Use asDouble" + ndim + "(" + variableName + ") for " + ndim
                        + "-dimensional variable " + variableName);
            }

            return (double[][][][]) get(variableName);
        } catch (CDFException.ReaderError th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns all available values for the given variable.
     *
     * @param variableName variable name
     *
     * @return a double array of dimension appropriate to the variable.
     *         <p>
     *         Type of object returned depends on the number of varying dimensions
     *         and type of the CDF variable.<br>
     *         For a numeric variable,
     *         a double[], double[][], double[][][], or double[][][][] object is
     *         returned for scalar, one-dimensional, two-dimensional, or
     *         three-dimensional variable, respectively.
     *         </p>
     *         <p>
     *         For a character string variable, a String[] or a String[][]
     *         object is returned for a scalar or one-dimensional variable.
     *         </p>
     *
     * @throws ReaderError the reader error
     *
     * @see #getOneD(String variableName, boolean columnMajor)
     */
    public final Object get(final String variableName) throws CDFException.ReaderError {
        Variable var = this.thisCDF.getVariable(variableName);

        if (var == null) {
            throw new CDFException.ReaderError("No such variable " + variableName);
        }

        try {
            Method method = Extractor.getMethod(var, "Series");

            if ((method == null) || coreNeeded(var)) {
                return this.thisCDF.get(variableName);
            }

            return method.invoke(null, this.thisCDF, var);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new CDFException.ReaderError("Failed to get variable " + variableName, e);
        }

    }

    /**
     * Returns data extracted by the named thread as ByteBuffer.
     * <p>
     * After this method returns the ByteBuffer, threadName is forgotten.
     *
     * @param threadName the thread name
     *
     * @return the buffer
     *
     * @throws ReaderError the reader error
     */
    public final ByteBuffer getBuffer(final String threadName) throws ReaderError {

        if (threadFinished(threadName)) {

            synchronized (this.threadNameEntriesByThreadName) {

                VDataContainer container = this.threadNameEntriesByThreadName.get(threadName)
                        .getContainer();

                try {
                    return container.getBuffer();

                } catch (RuntimeException e) {
                    throw new CDFException.ReaderError("Failed to get buffer for threadname " + threadName, e);
                } finally {
                    this.threadNameEntriesByThreadName.remove(threadName);

                }

            }

        }

        throw new CDFException.ReaderError("Thread " + threadName + " is working");
    }

    /**
     * Returns specified data as ByteBuffer of specified type.Order of the
     * ByteBuffer is 'native'.Data is organized according to
     * storage model of the variable returned by rowMajority().
     * A DirectBuffer
     * is allocated.
     *
     * @param variableName variable name
     * @param targetType   desired type of extracted data
     * @param recordRange  the record range
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     *
     * @return the buffer
     *
     * @throws ReaderError the reader error
     */
    public final ByteBuffer getBuffer(final String variableName, final String targetType, final int[] recordRange,
            final boolean preserve) throws CDFException.ReaderError {
        return getBuffer(variableName, targetType, recordRange, preserve, true);
    }

    /**
     * Returns specified data as ByteBuffer of specified type.Order of the
     * ByteBuffer is 'native'.Data is organized according to
     * storage model of the variable returned by rowMajority().
     *
     * @param variableName variable name
     * @param targetType   desired type of extracted data
     * @param recordRange  the record range
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     * @param useDirect    specifies whether a DirectBuffer should be used.
     *                     if set to false, an array backed buffer will be
     *                     allocated.
     *
     * @return the buffer
     *
     * @throws ReaderError the reader error
     */
    public final ByteBuffer getBuffer(final String variableName, final String targetType, final int[] recordRange,
            final boolean preserve, final boolean useDirect) throws CDFException.ReaderError {
        Class<?> type;

        try {
            type = getContainerClass(targetType);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

        if (!isCompatible(variableName, type, preserve)) {
            throw new CDFException.ReaderError(
                    "Requested type " + targetType + " not compatible with preserve = " + preserve);
        }

        VDataContainer container = null;

        try {
            container = getContainer(variableName, type, recordRange, preserve, ByteOrder.nativeOrder());
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

        container.setDirect(useDirect);
        container.run();
        return container.getBuffer();
    }

    /**
     * Gets the buffer.
     *
     * @param variableName the var name
     * @param targetType   the target type
     * @param recordRange  the record range
     * @param preserve     the preserve
     * @param buffer       the buffer
     *
     * @return the buffer
     *
     * @throws ReaderError the reader error
     */
    public final ByteBuffer getBuffer(final String variableName, final String targetType, final int[] recordRange,
            final boolean preserve, final ByteBuffer buffer) throws CDFException.ReaderError {
        VDataContainer container = null;

        try {
            Class<?> type = getContainerClass(targetType);
            container = getContainer(variableName, type, recordRange, preserve, ByteOrder.nativeOrder());
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

        container.setUserBuffer(buffer);
        container.run();
        return container.getBuffer();
    }

    /**
     * Gets the buffer capacity.
     *
     * @param variableName the var name
     * @param targetType   the target type
     * @param recordRange  the record range
     *
     * @return the buffer capacity
     *
     * @throws ReaderError the reader error
     */
    public final int getBufferCapacity(final String variableName, final String targetType, final int[] recordRange)
            throws CDFException.ReaderError {
        VDataContainer container = null;

        try {
            Class<?> type = getContainerClass(targetType);
            container = getContainer(variableName, type, recordRange, false, ByteOrder.nativeOrder());
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

        return container.getCapacity();
    }

    /*
     * public final double[] getOneD(String variableName, int first, int last,
     * int[] stride) throws CDFException.ReaderError {
     * Variable var = thisCDF.getVariable(variableName);
     * if (var == null) throw new CDFException.ReaderError(
     * "No such variable " + variableName);
     * try {
     * return thisCDF.get1D(variableName, first, last, stride);
     * } catch (RuntimeException th) {
     * throw new CDFException.ReaderError(th.getMessage());
     * }
     * }
     */
    /**
     * Returns values of the specified component of 1 dimensional
     * variable of numeric types other than INT8 or TT2000.
     *
     * @param variableName variable name
     * @param component    component
     *
     * @return the copy on write array list component
     *
     * @throws ReaderError the reader error
     *
     * @see #get(String variableName)
     */
    public final double[] getVectorComponent(final String variableName, final int component)
            throws CDFException.ReaderError {
        checkType(variableName);

        if (getEffectiveRank(variableName) != 1) {
            throw new CDFException.ReaderError(variableName + " is not a CopyOnWriteArrayList.");
        }

        try {
            Variable var = this.thisCDF.getVariable(variableName);
            Method method = Extractor.getMethod(var, "Element");

            if ((method == null) || coreNeeded(var)) {
                return (double[]) this.thisCDF.get(variableName, component);
            }

            return (double[]) method.invoke(null, this.thisCDF, var, component);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new CDFException.ReaderError("Failed to get variable " + variableName, e);
        }

    }

    /**
     * Returns value of the specified component of 1 dimensional
     * variable of numeric types other than INT8 or TT2000.
     *
     * @param variableName variable name
     * @param components   array containing components to be extracted
     *
     * @return the copy on write array list components
     *
     * @throws ReaderError the reader error
     *
     * @see #get(String variableName)
     */
    public final double[][] getVectorComponents(final String variableName, final int[] components) throws ReaderError {
        checkType(variableName);

        if (getEffectiveRank(variableName) != 1) {
            throw new CDFException.ReaderError(variableName + " is not a CopyOnWriteArrayList.");
        }

        try {
            Variable var = this.thisCDF.getVariable(variableName);
            Method method = Extractor.getMethod(var, "Elements");

            if ((method == null) || coreNeeded(var)) {
                return (double[][]) this.thisCDF.get(variableName, components);
            }

            return (double[][]) method.invoke(null, this.thisCDF, var, components);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new CDFException.ReaderError("Failed to get variable " + variableName, e);
        }

    }

    /**
     * Returns all available values for the given long type variable.
     *
     * @param variableName variable name
     *
     * @return a long array of dimension appropriate to the variable.
     *         <p>
     *         Type of object returned depends on the number of varying dimensions.
     *         <br>
     *         For a numeric variable,
     *         a long[], long[][], long[][][], or long[][][][] object is
     *         returned for scalar, one-dimensional, two-dimensional, or
     *         three-dimensional variable, respectively.
     *         </p>
     *
     * @throws ReaderError the reader error
     *
     * @see #getOneD(String variableName, boolean columnMajor)
     */
    public final Object getLong(final String variableName) throws CDFException.ReaderError {
        Variable var = this.thisCDF.getVariable(variableName);

        if (var == null) {
            throw new CDFException.ReaderError("No such variable " + variableName);
        }

        try {
            return this.thisCDF.getLong(variableName);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns one dimensional representation of the values of a numeric
     * variable whose type is not INT8 or TT2000.
     *
     * @param variableName variable name
     * @param columnMajor  specifies whether the returned array conforms
     *                     to a columnMajor storage mode, i.e. the first index of a
     *                     multi
     *                     dimensional array varies the fastest.
     *
     * @return a double[] that represents values
     *         of multi-dimensional arrays stored in a manner prescribed by the
     *         columnMajor parameter.
     *
     * @throws ReaderError the reader error
     *
     * @see #get(String variableName)
     */
    public final double[] getOneD(final String variableName, final boolean columnMajor)
            throws CDFException.ReaderError {
        Variable var = this.thisCDF.getVariable(variableName);

        if (var == null) {
            throw new CDFException.ReaderError("No such variable " + variableName);
        }

        if (getNumberOfValues(variableName) == 0) {
            return new double[0];
        }

        try {
            return this.thisCDF.getOneD(variableName, columnMajor);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns data extracted by the named thread as a one dimensional
     * array, organized according to specified row majority..
     *
     * @param threadName  the thread name
     * @param columnMajor the column major
     *
     * @return the one D array
     *
     * @throws ReaderError the reader error
     */
    public final Object getOneDArray(final String threadName, final boolean columnMajor)
            throws CDFException.ReaderError {

        if (threadFinished(threadName)) {

            synchronized (this.threadNameEntriesByThreadName) {
                VDataContainer container = this.threadNameEntriesByThreadName.get(threadName)
                        .getContainer();
                // System.out.println("getOneDArray: " + container);
                Object array = null;

                try {
                    array = container.asOneDArray(columnMajor);
                } catch (RuntimeException th) {
                    throw new CDFException.ReaderError(th.getMessage());
                }

                this.threadNameEntriesByThreadName.remove(threadName);
                return array;
            }

        }

        throw new CDFException.ReaderError("Thread " + threadName + " is working");
    }

    /**
     * Returns specified data as a one dimensional
     * array, organized according to specified row majority..
     *
     * @param variableName variable name
     * @param targetType   desired type of extracted data
     * @param recordRange  the record range
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     * @param columnMajor  specifies whether the returned array conforms
     *                     to a columnMajor storage mode, i.e. the first index of a
     *                     multi
     *                     dimensional array varies the fastest.
     *
     * @return the one D array
     *
     * @throws ReaderError the reader error
     */
    public final Object getOneDArray(final String variableName, final String targetType, final int[] recordRange,
            final boolean preserve, final boolean columnMajor) throws CDFException.ReaderError {
        VDataContainer container = null;

        try {
            Class<?> type = getContainerClass(targetType);
            container = getContainer(variableName, type, recordRange, preserve, ByteOrder.nativeOrder());
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

        container.run();
        return container.asOneDArray(columnMajor);
    }

    /**
     * Returns values for a range of records for the given numeric variable
     * of rank &lt;&#61; 2.
     *
     * @param variableName variable name
     * @param first        first record of range
     * @param last         last record of range
     *
     * @return a double array of dimension appropriate to the variable.
     *         <p>
     *         Type of object returned depends on the number of varying dimensions
     *         and type of the CDF variable.<br>
     *         For a numeric variable,
     *         a double[], double[][], double[][][] object is
     *         returned for scalar, one-dimensional, or two-dimensional
     *         variable, respectively.
     *         </p>
     *
     * @throws ReaderError the reader error
     *
     * @see #getRangeOneD(String variableName, int first, int last,
     *      boolean columnMajor)
     */
    public final Object getRange(final String variableName, final int first, final int last)
            throws CDFException.ReaderError {
        Variable var = this.thisCDF.getVariable(variableName);

        if (var == null) {
            throw new CDFException.ReaderError("No such variable " + variableName);
        }

        try {
            Method method = Extractor.getMethod(var, "Range");

            if ((method == null) || coreNeeded(var)) {
                return this.thisCDF.getRange(variableName, first, last);
            }

            return method.invoke(null, this.thisCDF, var, first, last);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new CDFException.ReaderError("Failed to get variable " + variableName, e);
        }

    }

    /**
     * Returns values, in the specified record range, of the specified
     * component of 1 dimensional * variable of numeric types other than
     * INT8 or TT2000.
     *
     * @param variableName variable name
     * @param first        first record of range
     * @param last         last record of range
     * @param component    component
     *
     * @return the range for component
     *
     * @throws ReaderError the reader error
     *
     * @see #getRange(String variableName, int first, int last)
     */
    public final double[] getRangeForComponent(final String variableName, final int first, final int last,
            final int component) throws CDFException.ReaderError {
        checkType(variableName);

        if (getEffectiveRank(variableName) != 1) {
            throw new CDFException.ReaderError(variableName + " is not a CopyOnWriteArrayList.");
        }

        try {
            Variable var = this.thisCDF.getVariable(variableName);
            Method method = Extractor.getMethod(var, "RangeForElement");

            if ((method == null) || coreNeeded(var, new int[] { first, last })) {
                return (double[]) this.thisCDF.getRange(variableName, first, last, component);
            }

            return (double[]) method.invoke(null, this.thisCDF, var, first, last, component);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new CDFException.ReaderError("Failed to get variable " + variableName, e);
        }

    }

    /**
     * Returns values, in the specified record range, of specified
     * components of 1 dimensional variable of numeric types other than
     * INT8 or TT2000.
     *
     * @param variableName variable name
     * @param first        first record of range
     * @param last         last record of range
     * @param components   components
     *
     * @return the range for components
     *
     * @throws ReaderError the reader error
     *
     * @see #getRange(String variableName, int first, int last)
     */
    public final double[][] getRangeForComponents(final String variableName, final int first, final int last,
            final int[] components) throws CDFException.ReaderError {
        checkType(variableName);

        if (getEffectiveRank(variableName) != 1) {
            throw new CDFException.ReaderError(variableName + " is not a CopyOnWriteArrayList.");
        }

        try {
            Variable var = this.thisCDF.getVariable(variableName);
            Method method = Extractor.getMethod(var, "RangeForElements");

            if ((method == null) || coreNeeded(var)) {
                return (double[][]) this.thisCDF.get(variableName, first, last, components);
            }

            return (double[][]) method.invoke(null, this.thisCDF, var, first, last, components);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new CDFException.ReaderError("Failed to get variable " + variableName, e);
        }

    }

    /**
     * Returns one dimensional representation of the values
     * for a range of records of a numeric
     * variable whose type is not INT8 or TT2000.
     *
     * @param variableName variable name
     * @param first        first record of range
     * @param last         last record of range
     * @param columnMajor  specifies whether the returned array conforms
     *                     to a columnMajor storage mode, i.e. the first index of a
     *                     multi
     *                     dimensional array varies the fastest.
     *
     * @return a double[] that represents values
     *         of multi-dimensional arrays stored in a manner prescribed by the
     *         columnMajor parameter.
     *
     * @throws ReaderError the reader error
     *
     * @see #getRange(String variableName, int first, int last)
     */
    public final double[] getRangeOneD(final String variableName, final int first, final int last,
            final boolean columnMajor) throws CDFException.ReaderError {
        Variable var = this.thisCDF.getVariable(variableName);

        if (var == null) {
            throw new CDFException.ReaderError("No such variable " + variableName);
        }

        try {
            return (double[]) this.thisCDF.getRangeOneD(variableName, first, last, columnMajor);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns sampled values of a numeric variable as one dimensional
     * array of specified type.Data for records is organized according to the
     * storage model of the
     * variable (as returned by rowMajority()).
     *
     * @param variableName variable name
     * @param first        the first
     * @param last         the last
     * @param stride       array of length 1 where value specifies stride
     * @param type         desired type of extracted data - one of
     *                     the following: "long", "double", "float", "int", "short",
     *                     or "byte"
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     *
     * @return the sampled
     *
     * @throws ReaderError the reader error
     */
    public Object getSampled(final String variableName, final int first, final int last, final int stride,
            final String type, final boolean preserve) throws CDFException.ReaderError {

        try {
            BaseVarContainer container = getRangeContainer(variableName, new int[] { first, last }, type, preserve);
            int[] _stride = (stride > 0) ? new int[] { stride } : new int[] { -1, -stride };
            return container.asSampledArray(new Stride(_stride));
        } catch (ReaderError e) {
            throw new CDFException.ReaderError("getSampled failed", e);
        }

    }

    /**
     * Returns sampled values of a numeric variable as one dimensional
     * array of specified type and storage model.
     *
     * @param variableName variable name
     * @param range        the range
     * @param stride       array of length 1 where value specifies stride
     * @param type         desired type of extracted data - one of
     *                     the following: "long", "double", "float", "int", "short",
     *                     or "byte"
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     * @param columnMajor  specifies whether the returned array conforms
     *                     to a columnMajor storage mode, i.e. the first index of a
     *                     multi
     *                     dimensional array varies the fastest.
     *
     * @return the sampled
     *
     * @throws ReaderError the reader error
     */
    public Object getSampled(final String variableName, final int[] range, final int stride, final String type,
            final boolean preserve, final boolean columnMajor) throws CDFException.ReaderError {

        try {
            BaseVarContainer container = getRangeContainer(variableName, range, type, preserve);
            int[] _stride = (stride > 0) ? new int[] { stride } : new int[] { -1, -stride };
            return container.asOneDArray(columnMajor, new Stride(_stride));
        } catch (ReaderError e) {
            throw new CDFException.ReaderError(e);
        }

    }

    /**
     * Returns the name of the source CDF.
     *
     * @return the source
     */
    public final String getSource() {
        return this.thisCDF.getSource()
                .getName();
    }

    /**
     * Returns whether a variable is a List.
     *
     * @param variableName the var name
     *
     * @return true, if is copy on write array list
     *
     * @throws ReaderError the reader error
     */
    public final boolean isVector(final String variableName) throws CDFException.ReaderError {
        return getEffectiveRank(variableName) == 1;
    }

    /**
     * Checks if the supplied variable name is a List.
     *
     * @param variableName the variable name
     *
     * @return true, if is a list
     *
     * @throws ReaderError the reader error
     */
    public final boolean isList(final String variableName) {
        return 1 == getVariableEffectiveRank(variableName).orElse(-1);
    }

    /**
     * Returns whether a variable is scalar.
     *
     * @param variableName the var name
     *
     * @return true, if is scalar
     *
     * @throws ReaderError the reader error
     */
    public final boolean isScalar(final String variableName) throws CDFException.ReaderError {
        return getEffectiveRank(variableName) == 0;
    }

    /**
     * Source is file.
     *
     * @return true, if successful
     */
    public final boolean sourceIsFile() {
        return this.thisCDF.getSource()
                .isFile();
    }

    /**
     * Starts a new thread to extract specified data.
     *
     * @param variableName variable name
     * @param targetType   desired type of extracted data - one of
     *                     the following: long, double, float, int, short,
     *                     byte or string
     * @param recordRange  the record range
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     *
     * @return Name of the thread. Methods to ascertain the
     *         availability and to retrieve require this name.
     *
     * @throws ReaderError the reader error
     *
     * @see #threadFinished(String threadName)
     * @see #getOneDArray(String threadName, boolean columnMajor)
     * @see #getBuffer(String threadName)
     */
    public final String startContainerThread(final String variableName, final String targetType,
            final int[] recordRange, final boolean preserve) throws CDFException.ReaderError {

        try {
            return startContainerThread(variableName, targetType, recordRange, preserve, ByteOrder.nativeOrder());
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns whether the named thread (started via this object) has
     * finished.
     *
     * @param threadName the thread name
     *
     * @return true, if successful
     *
     * @throws ReaderError the reader error
     */
    public final boolean threadFinished(final String threadName) throws CDFException.ReaderError {
        Thread thread = this.threadNameEntriesByThreadName.get(threadName)
                .getThread();

        if (thread == null) {
            throw new CDFException.ReaderError("Invalid thread name " + threadName);
        }

        return (thread.getState() == Thread.State.TERMINATED);
    }

    /**
     * Returns the name of the user supplied time variable for
     * the given variable.
     * For CDF that does not conform to ISTP specification for identifying
     * time variable associated with a variable, applications need to
     * override this method via a subclass. Default implementation assumes
     * ISTP compliance, and returns null.
     *
     * @param variableName variable name
     *
     * @return String user supplied name, or null if none
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    // FIXME: Always returns null
    @Override
    public String userTimeVariableName(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return null;
    }

    void checkType(final String variableName) throws CDFException.ReaderError {

        Variable variable = this.thisCDF.getVariable(variableName);

        if (variable == null) {
            throw new CDFException.ReaderError("No such variable " + variableName);
        }

        int type = variable.getType();

        if (DataTypes.typeCategory[type] == DataTypes.LONG) {
            throw new CDFException.ReaderError(
                    "This method cannot be used for variables of type long. Use the get methods for the "
                            + "variable and the associated time variable. ");
        }

    }

    VDataContainer getContainer(final String variableName, final Class<?> type, final int[] recordRange,
            final boolean preserve, final ByteOrder bo) {

        Variable variable = this.thisCDF.getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable " + variableName);
        }

        if (type == Double.TYPE) {
            return variable.getDoubleContainer(recordRange, preserve, bo);
        }

        if (type == Float.TYPE) {
            return variable.getFloatContainer(recordRange, preserve, bo);
        }

        if (type == Long.TYPE) {
            return variable.getLongContainer(recordRange, bo);
        }

        if (type == Integer.TYPE) {
            return variable.getIntContainer(recordRange, preserve, bo);
        }

        if (type == Short.TYPE) {
            return variable.getShortContainer(recordRange, preserve, bo);
        }

        if (type == Byte.TYPE) {
            return variable.getByteContainer(recordRange);
        }

        if (type == String.class) {
            return variable.getStringContainer(recordRange);
        }

        throw new IllegalArgumentException("Variable, " + variableName + ", has an unsupported type: " + type + ".");
    }

    Class<?> getContainerClass(final String supportedClassTypeName) {
        Class<?> cl = SUPPORTED_CLASSES_BY_NAME.get(supportedClassTypeName.toLowerCase());

        if (cl == null) {
            throw new IllegalArgumentException("The type, " + supportedClassTypeName + " , is not supported");
        }

        return cl;
    }

    BaseVarContainer getRangeContainer(final String variableName, final int[] range, final String type,
            final boolean preserve) throws ReaderError {

        if (!existsVariable(variableName)) {
            throw new IllegalArgumentException("File does not hava a variable named " + variableName);
        }

        int varType = getType(variableName);

        if (DataTypes.isStringType(varType)) {
            throw new IllegalArgumentException("Function not supported for string variables");
        }

        Class<?> cl = SUPPORTED_CLASSES_BY_NAME.get(type);

        if (cl == null) {
            throw new IllegalArgumentException("Invalid type " + type);
        }

        Variable var = this.thisCDF.getVariable(variableName);

        if ("float".equals(type)) {
            FloatVarContainer container = new FloatVarContainer(this.thisCDF, var, range, preserve);
            container.run();
            return container;
        }

        if ("double".equals(type)) {
            DoubleVarContainer container = new DoubleVarContainer(this.thisCDF, var, range, preserve);
            container.run();
            return container;
        }

        if ("int".equals(type)) {
            IntVarContainer container = new IntVarContainer(this.thisCDF, var, range, preserve);
            container.run();
            return container;
        }

        if ("short".equals(type)) {
            ShortVarContainer container = new ShortVarContainer(this.thisCDF, var, range, preserve);
            container.run();
            return container;
        }

        if ("byte".equals(type)) {
            ByteVarContainer container = new ByteVarContainer(this.thisCDF, var, range);
            container.run();
            return container;
        }

        if ("long".equals(type)) {
            LongVarContainer container = new LongVarContainer(this.thisCDF, var, range);
            container.run();
            return container;
        }

        throw new IllegalArgumentException("Could not find containter for type " + type);
        /*
         * String pkg = getClass().getPackage().getName();
         * String cname = pkg + "." + (type.substring(0,1)).toUpperCase() +
         * type.substring(1) + "VarContainer";
         * Class cclass = Class.forName(cname);
         * Constructor ccons;
         * if (type == "byte") {
         * ccons = cclass.getConstructor(
         * new Class[]{thisCDF.getClass(), Class.forName(pkg + ".Variable"),
         * range.getClass()});
         * } else {
         * ccons = cclass.getConstructor(
         * new Class[]{thisCDF.getClass(), Class.forName(pkg + ".Variable"),
         * range.getClass(), Boolean.TYPE});
         * }
         * container = (BaseVarContainer)
         * ccons.newInstance(thisCDF, thisCDF.getVariable(variableName), range,
         * preserve);
         */

    }

    void setup() {
        this.tgroup = new ThreadGroup(Integer.toHexString(hashCode()));
    }

    /**
     * Starts a new thread to extract specified data.
     *
     * @param variableName variable name
     * @param targetType   desired type of extracted data
     * @param recordRange
     * @param preserve     specifies whether the target must preserve
     *                     precision. if false, possible loss of precision
     *                     is deemed acceptable.
     * @param bo           ByteOrder for target ByteBuffer. ByteOrder
     *                     other than the native order may be specified
     *                     if the application requires it.
     *
     * @return Name of the container's thread. Methods to ascertain the
     *         availability and to retrieve require this name.
     *
     *
     * @see #threadFinished(String threadName)
     *
     * @see #getOneDArray(String threadName, boolean columnMajor)
     * @see #getBuffer(String threadName)
     */

    String startContainerThread(final String variableName, final String targetType, final int[] recordRange,
            final boolean preserve, final java.nio.ByteOrder bo) {

        String tname = threadName(variableName, targetType, recordRange, preserve, bo);

        Class<?> type = getContainerClass(targetType);

        VDataContainer container = getContainer(variableName, type, recordRange, preserve, bo);

        if (this.tgroup == null) {
            setup();
        }

        Thread thread = new Thread(this.tgroup, container, tname);
        thread.start();

        this.threadNameEntriesByThreadName.put(tname, new ThreadMapEntry(container, thread));

        return tname;
    }

    String threadName(final String variableName, final String type, final int[] recordRange, final boolean preserve,
            final java.nio.ByteOrder bo) {
        StringBuilder sb = new StringBuilder(variableName + "_" + type + "_");

        if (recordRange == null) {
            sb.append("null_");
        } else {
            sb.append(recordRange[0])
                    .append("_")
                    .append(recordRange[1]);
            sb.append("_");
        }

        sb.append(preserve)
                .append("_")
                .append(Math.random())
                .append("_")
                .append(bo);
        return sb.toString();
    }

    static class ThreadMapEntry {

        VDataContainer container;

        Thread thread;

        ThreadMapEntry(final VDataContainer container, final Thread thread) {
            this.container = container;
            this.thread = thread;
        }

        VDataContainer getContainer() {
            return this.container;
        }

        Thread getThread() {
            return this.thread;
        }
    }
}
