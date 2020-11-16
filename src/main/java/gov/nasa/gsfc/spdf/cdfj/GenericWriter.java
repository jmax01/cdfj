package gov.nasa.gsfc.spdf.cdfj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import gov.nasa.gsfc.spdf.cdfj.CDFException.WriterError;

/**
 * Base class for creating a version 3.6 CDF.
 * Derived class CDFWriter provides methods for creating a CDF
 * which includes user selected data from existing CDFs.
 */
public class GenericWriter {

    LinkedHashMap<String, ADR> attributes = new LinkedHashMap<>();

    LinkedHashMap<String, CopyOnWriteArrayList<AEDR>> attributeEntries = new LinkedHashMap<>();

    LinkedHashMap<String, VDR> variableDescriptors = new LinkedHashMap<>();

    int leapSecondLastUpdated = -1;

    CDR cdr = new CDR();

    GDR gdr = new GDR();

    /** The row majority. */
    public final boolean rowMajority;

    LinkedHashMap<String, DataContainer> dataContainers = new LinkedHashMap<>();

    boolean needDigest = false;

    /**
     * Constructs a column major GenericWriter.
     */
    public GenericWriter() {
        this(true);
    }

    /**
     * Constructs a GenericWriter of specified row majority.
     *
     * @param rowMajority the row majority
     */
    public GenericWriter(final boolean rowMajority) {
        this.rowMajority = rowMajority;
    }

    /**
     * Adds data to a variable.same as addData(String name, Object data, false)
     *
     * @param name the name
     * @param data the data
     *
     * @throws WriterError the writer error
     *
     * @see #addData(String name, Object data, boolean relax)
     */
    public void addData(final String name, final Object data) throws CDFException.WriterError {
        DataContainer container = this.dataContainers.get(name);

        if (container == null) {
            throw new CDFException.WriterError("Variable " + name + " is not defined.");
        }

        try {
            container.addData(data, null, false, false);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds data to a variable.
     *
     * @param name  name of the variable.
     * @param data  an array of type compatible with the type of variable.
     * @param relax relevant for unsigned data types, CDF_UINT1,
     *              CDF_UINT2 and CDF_UINT4 only,specifies that values in
     *              data array can be interpreted as unsigned.
     *
     * @throws WriterError the writer error
     *
     * @see #addData(String name, Object data, int[] recordRange, boolean relax)
     *      for more details.
     */
    public void addData(final String name, final Object data, final boolean relax) throws CDFException.WriterError {
        DataContainer container = this.dataContainers.get(name);

        if (container == null) {
            throw new CDFException.WriterError("Variable " + name + " is not defined.");
        }

        try {
            container.addData(data, null, false, relax);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds data for a specified record range to a variable.
     *
     * @param name        name of the variable.
     * @param data        an array of type compatible with the type of variable, or
     *                    a ByteOrder.LITTLE_ENDIAN ByteBuffer containing data to be
     *                    added.
     * @param recordRange int[2] containing record range. If data is an
     *                    array, or variable is to be saved uncompressed,
     *                    recordRange may be null,
     *                    in which case range is assumed to be follow last record
     *                    added.
     *
     * @throws WriterError the writer error
     *
     * @see #addData(String name, Object data, int[] recordRange, boolean relax)
     */
    public void addData(final String name, final Object data, final int[] recordRange) throws CDFException.WriterError {
        DataContainer container = this.dataContainers.get(name);

        if (container == null) {
            throw new CDFException.WriterError("Variable " + name + " is not defined.");
        }

        try {
            container.addData(data, recordRange, false, false);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds data for a specified record range to a variable.
     *
     * @param name        name of the variable.
     * @param data        an array of type compatible with the type of variable, or
     *                    a ByteOrder.LITTLE_ENDIAN ByteBuffer containing data to be
     *                    added.
     * @param recordRange int[2] containing record range. If data is an
     *                    array, or variable is to be saved uncompressed,
     *                    recordRange may be null,
     *                    in which case range is assumed to be follow last record
     *                    added.
     * @param relax       relevant for unsigned data types, CDF_UINT1,
     *                    CDF_UINT2 and CDF_UINT4 only,specifies that values in
     *                    data array can be interpreted as unsigned.
     *                    <table summary="">
     *                    <tr>
     *                    <td>CDF Type of Variable</td>
     *                    <td>Type of Array</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT8, TT2000, UINT4</td>
     *                    <td>long</td>
     *                    </tr>
     *                    <tr>
     *                    <td>UINT4</td>
     *                    <td>int, if relax = true, long otherwise</td>
     *                    </tr>
     *                    <tr>
     *                    <td>DOUBLE, EPOCH, EPOCH16</td>
     *                    <td>double</td>
     *                    </tr>
     *                    <tr>
     *                    <td>FLOAT</td>
     *                    <td>float or double</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT4</td>
     *                    <td>int</td>
     *                    </tr>
     *                    <tr>
     *                    <td>UINT2</td>
     *                    <td>short, if relax = true, int otherwise</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT2</td>
     *                    <td>short</td>
     *                    </tr>
     *                    <tr>
     *                    <td>UINT1</td>
     *                    <td>byte, if relax = true, short otherwise</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT1</td>
     *                    <td>byte</td>
     *                    </tr>
     *                    <tr>
     *                    <td>CHAR</td>
     *                    <td>String</td>
     *                    </tr>
     *                    </table>
     *                    data must contain an integral number of points. If data is
     *                    an array,
     *                    rank of the array must be 1 greater than the rank of the
     *                    variable, and the dimensions after the first must match
     *                    the
     *                    variable dimensions in number and order.
     *                    If data is a ByteBuffer, contents must conform to the row
     *                    majority
     *                    of this GenericWriter. If the variable is to be stored as
     *                    compressed,
     *                    then the buffer should contain compressed data, and record
     *                    range must
     *                    be specified.
     *
     * @throws WriterError the writer error
     */

    public void addData(final String name, final Object data, final int[] recordRange, final boolean relax)
            throws CDFException.WriterError {
        DataContainer container = this.dataContainers.get(name);

        if (container == null) {
            throw new CDFException.WriterError("Variable " + name + " is not defined.");
        }

        try {
            container.addData(data, recordRange, false, relax);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds a global attribute entry of specified type..
     *
     * @param name     name of the attribute
     * @param dataType {@link CDFDataType CDFDataType} desired
     * @param value    array or wrapped scalar value to assign to attribute
     *
     * @throws WriterError the writer error
     */
    public void addGlobalAttributeEntry(final String name, final CDFDataType dataType, final Object value)
            throws CDFException.WriterError {
        ADR adr = getAttribute(name, true);
        CopyOnWriteArrayList<AEDR> values = this.attributeEntries.get(name);

        if (values == null) {
            values = new CopyOnWriteArrayList<>();
            this.attributeEntries.put(name, values);
        }

        GlobalAttributeEntry gae;
        int type = (dataType == null) ? -1 : dataType.getValue();

        try {
            gae = new GlobalAttributeEntry(adr, type, value);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

        gae.setNum(adr.ngrEntries);
        adr.mAXgrEntry = adr.ngrEntries;
        adr.ngrEntries++;
        values.add(gae);
    }

    /**
     * Adds a global attribute entry.
     *
     * @param name  name of the attribute
     * @param value array or wrapped scalar value to assign to attribute
     *
     * @throws WriterError the writer error
     */
    public void addGlobalAttributeEntry(final String name, final Object value) throws CDFException.WriterError {
        addGlobalAttributeEntry(name, null, value);
    }

    /**
     * Adds an NRV record of string type.
     *
     * @param name  the name
     * @param value the value
     *
     * @throws WriterError the writer error
     */
    public void addNRVString(final String name, final CharSequence value) throws CDFException.WriterError {
        addNRVVariable(name, CDFDataType.CHAR, new int[0], value.length(), value);
    }

    /**
     * Adds a NRV record of the given type and dimensions.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param size     the size
     * @param value    the value
     *
     * @throws WriterError the writer error
     */
    public void addNRVVariable(final String name, final CDFDataType dataType, final int[] dim, final int size,
            final Object value) throws CDFException.WriterError {

        if ((dataType != CDFDataType.CHAR) && (size > 1)) {
            throw new CDFException.WriterError("incompatible size for type " + dataType);
        }

        boolean[] varys = new boolean[dim.length];

        Arrays.fill(varys, true);

        defineVariable(name, dataType, dim, varys, false, false, null, size);

        if ((dim.length > 0) || (dataType == CDFDataType.EPOCH16)) {

            try {
                addData(name, AArray.getPoint(value));
            } catch (RuntimeException th) {
                throw new CDFException.WriterError(th.getMessage());
            }

        } else {
            dispatch(name, value);
        }

    }

    /**
     * Adds a NRV record of the given numeric type and dimension.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param value    the value
     *
     * @throws WriterError the writer error
     */
    public void addNRVVariable(final String name, final CDFDataType dataType, final int[] dim, final Object value)
            throws CDFException.WriterError {

        if (dataType == CDFDataType.CHAR) {
            throw new CDFException.WriterError(
                    "Invalid method for string type. Use addNRVVariable(name, dataType, dim, size, value)");
        }

        addNRVVariable(name, dataType, dim, 1, value);
    }

    /**
     * Adds a scalar NRV record of the given numeric type.
     *
     * @param name     the name
     * @param dataType the data type
     * @param value    the value
     *
     * @throws WriterError the writer error
     */
    public void addNRVVariable(final String name, final CDFDataType dataType, final Object value)
            throws CDFException.WriterError {
        addNRVVariable(name, dataType, new int[0], 1, value);
    }

    /**
     * Adds data (represented as a one dimensional array) to a variable.same as
     * addOneD(String name, Object data, null, false)
     *
     * @param name the name
     * @param data the data
     *
     * @throws WriterError the writer error
     *
     * @see #addOneD(String name, Object data, int[] recordRange, boolean relax)
     */
    public void addOneD(final String name, final Object data) throws CDFException.WriterError {
        DataContainer container = getContainer(name, data);

        try {
            container.addData(data, null, true, false);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds data (represented as a one dimensional array) to a variable.same as
     * addOneD(String name, Object data, null, boolean relax)
     *
     * @param name  the name
     * @param data  the data
     * @param relax the relax
     *
     * @throws WriterError the writer error
     *
     * @see #addOneD(String name, Object data, int[] recordRange, boolean relax)
     */
    public void addOneD(final String name, final Object data, final boolean relax) throws CDFException.WriterError {
        DataContainer container = getContainer(name, data);

        try {
            container.addData(data, null, true, relax);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds data (represented as a one dimensional array)
     * for a specified record range to a variable.same as addOneD(String name,
     * Object data, int[] recordRange, false)
     *
     * @param name        the name
     * @param data        the data
     * @param recordRange the record range
     *
     * @throws WriterError the writer error
     *
     * @see #addOneD(String name, Object data, int[] recordRange, boolean relax)
     */
    public void addOneD(final String name, final Object data, final int[] recordRange) throws CDFException.WriterError {
        DataContainer container = getContainer(name, data);

        try {
            container.addData(data, recordRange, true, false);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Adds data (represented as a one dimensional array)
     * for a specified record range to a variable.
     *
     * @param name        name of the variable.
     * @param data        a one dimensional array of a type compatible with
     *                    the type of variable.
     * @param recordRange int[2] containing record range. May be null, in
     *                    which case range is assumed to be follow last record
     *                    added.
     * @param relax       relevant for unsigned data types, CDF_UINT1,
     *                    CDF_UINT2 and CDF_UINT4 only,specifies that values in
     *                    data array can be interpreted as unsigned.
     *                    <table summary="">
     *                    <tr>
     *                    <td>CDF Type of Variable</td>
     *                    <td>Type of Array</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT8, TT2000, UINT4</td>
     *                    <td>long</td>
     *                    </tr>
     *                    <tr>
     *                    <td>UINT4</td>
     *                    <td>int, if relax = true, long otherwise</td>
     *                    </tr>
     *                    <tr>
     *                    <td>DOUBLE, EPOCH, EPOCH16</td>
     *                    <td>double</td>
     *                    </tr>
     *                    <tr>
     *                    <td>FLOAT</td>
     *                    <td>float or double</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT4</td>
     *                    <td>int</td>
     *                    </tr>
     *                    <tr>
     *                    <td>UINT2</td>
     *                    <td>short, if relax = true, int otherwise</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT2</td>
     *                    <td>short</td>
     *                    </tr>
     *                    <tr>
     *                    <td>UINT1</td>
     *                    <td>byte, if relax = true, short otherwise</td>
     *                    </tr>
     *                    <tr>
     *                    <td>INT1</td>
     *                    <td>byte</td>
     *                    </tr>
     *                    <tr>
     *                    <td>CHAR</td>
     *                    <td>String</td>
     *                    </tr>
     *                    </table>
     *                    data must contain an integral number of points, and its
     *                    contents must conform to the row majority
     *                    of this GenericWriter.
     *
     * @throws WriterError the writer error
     */
    public void addOneD(final String name, final Object data, final int[] recordRange, final boolean relax)
            throws CDFException.WriterError {
        DataContainer container = getContainer(name, data);

        try {
            container.addData(data, recordRange, true, relax);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    /**
     * Sets the value of a given attribute for a variable.This method creates a new
     * value for the given attribute.
     * If an entry exists, new value is added to the existing entry
     * if both are of String type.
     *
     * @param vname    name of the variable
     * @param aname    name of the attribute
     * @param dataType {@link CDFDataType CDFDataType} desired
     * @param value    array of primitives, or String value to assign to
     *                 attribute
     *
     * @throws WriterError the writer error
     */
    public void addVariableAttributeEntry(final String vname, final String aname, final CDFDataType dataType,
            final Object value) throws CDFException.WriterError {

        VDR vdesc = this.variableDescriptors.get(vname);

        if (vdesc == null) {
            throw new CDFException.WriterError("Variable " + vname + " has not been defined.");
        }

        List<VariableAttributeEntry> currentEntries = findVariableAttributeEntries(vname, aname);

        if (currentEntries.isEmpty()) {

            if (!this.attributeEntries.containsKey(aname)) {
                this.attributeEntries.put(aname, new CopyOnWriteArrayList<>());
            }

        } else {

            if (value.getClass() != String.class) {
                int _type = currentEntries.get(currentEntries.size() - 1).dataType;

                if (DataTypes.isStringType(_type)) {
                    throw new CDFException.WriterError("Only String values can be added");
                }

            }

        }

        ADR adr = getAttribute(aname, false);
        VariableAttributeEntry vae;
        int type = (dataType == null) ? -1 : dataType.getValue();

        try {
            vae = new VariableAttributeEntry(adr, type, value);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

        vae.setNum(vdesc.getNum());
        this.attributeEntries.get(aname)
                .add(vae);

        if (vdesc.getNum() > adr.mAXzEntry) {
            adr.mAXzEntry = vdesc.getNum();
        }

        adr.nzEntries++;
    }

    /**
     * Sets the value of a given attribute for a variable.This method creates a new
     * value for the given attribute.
     * If an entry exists, new value is added to the existing entry
     * if both are of String type.
     *
     * @param vname name of the variable
     * @param aname name of the attribute
     * @param value array of primitives, or String value to assign to
     *              attribute
     *
     * @throws WriterError the writer error
     */
    public void addVariableAttributeEntry(final String vname, final String aname, final Object value)
            throws CDFException.WriterError {
        addVariableAttributeEntry(vname, aname, null, value);
    }

    /**
     * Defines a compressed variable of string type with given dimensions.
     *
     * @param name the name
     * @param dim  the dim
     * @param size the size
     *
     * @throws WriterError the writer error
     */
    public void defineCompressedStringVariable(final String name, final int[] dim, final int size)
            throws CDFException.WriterError {
        defineCompressedVariable(name, CDFDataType.CHAR, dim, size);
    }

    /**
     * Defines a time series of the named variable of specified data type and
     * the time variable of specified name and type.Variable's data is compressed
     * before it is stored
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param tname    the tname
     * @param timeType the time type
     *
     * @throws WriterError the writer error
     */
    public void defineCompressedTimeSeries(final String name, final CDFDataType dataType, final int[] dim,
            final String tname, final CDFTimeType timeType) throws CDFException.WriterError {
        defineTimeSeries(name, dataType, dim, tname, timeType, true);
    }

    /**
     * Defines a compressed variable of specified numeric data type and
     * dimensions.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     *
     * @throws WriterError the writer error
     */
    public void defineCompressedVariable(final String name, final CDFDataType dataType, final int[] dim)
            throws CDFException.WriterError {
        boolean[] varys = new boolean[dim.length];
        Arrays.fill(varys, true);
        defineCompressedVariable(name, dataType, dim, 1);
    }

    /**
     * Defines a compressed variable of string type with given dimensions.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param size     the size
     *
     * @throws WriterError the writer error
     */
    public void defineCompressedVariable(final String name, final CDFDataType dataType, final int[] dim, final int size)
            throws CDFException.WriterError {
        boolean[] varys = new boolean[dim.length];
        Arrays.fill(varys, true);
        defineVariable(name, dataType, dim, varys, true, true, null, size);
    }

    /**
     * Defines a NRV record of the given type and dimensions.Parameter size is
     * ignored for variables of numeric types.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param size     the size
     *
     * @throws WriterError the writer error
     */
    public void defineNRVVariable(final String name, final CDFDataType dataType, final int[] dim, final int size)
            throws CDFException.WriterError {
        boolean[] varys = new boolean[dim.length];
        Arrays.fill(varys, true);
        int _size = (dataType == CDFDataType.CHAR) ? size : 1;
        defineVariable(name, dataType, dim, varys, false, false, null, _size);
    }

    /**
     * Defines a variable of string type using default
     * sparse record option.same as:
     * defineStringVariable(String name, int[] dim,
     * boolean[] varys, boolean recordVariance, boolean compressed,
     * Object pad, int size, SparseRecordOption.NONE)
     *
     * @param name           the name
     * @param dim            the dim
     * @param varys          the varys
     * @param recordVariance the record variance
     * @param compressed     the compressed
     * @param pad            the pad
     * @param size           the size
     *
     * @throws WriterError the writer error
     *
     * @see #defineStringVariable(String name, int[] dim,
     *      boolean[] varys, boolean recordVariance, boolean compressed,
     *      Object pad, int size, SparseRecordOption option)
     */
    public void defineStringVariable(final String name, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad, final int size)
            throws CDFException.WriterError {
        defineVariable(name, CDFDataType.CHAR, dim, varys, recordVariance, compressed, pad, size,
                SparseRecordOption.NONE);
    }

    /**
     * Defines a variable of string type using given
     * sparse record option.
     *
     * @param name           the name
     * @param dim            dimensions
     * @param varys          dimension variance
     * @param recordVariance the record variance
     * @param compressed     whether the values will be saved in compressed form
     * @param pad            array or wrapped scalar value to assign to use as pad
     * @param size           length of character string
     * @param option         {@link SparseRecordOption sparse record option}
     *
     * @throws WriterError the writer error
     */
    public void defineStringVariable(final String name, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad, final int size,
            final SparseRecordOption option) throws CDFException.WriterError {
        defineVariable(name, CDFDataType.CHAR, dim, varys, recordVariance, compressed, pad, size, option);
    }

    /**
     * Defines a named variable of string data type.
     *
     * @param name the name
     * @param dim  the dim
     * @param size the size
     *
     * @throws WriterError the writer error
     */
    public void defineStringVariable(final String name, final int[] dim, final int size)
            throws CDFException.WriterError {
        defineVariable(name, CDFDataType.CHAR, dim, size);
    }

    /**
     * Defines a time series for a new variable of specified data type.Variable's
     * times are contained in a time variable named Epoch which
     * must have been created before this method is called.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     *
     * @throws WriterError the writer error
     */
    public void defineTimeSeries(final String name, final CDFDataType dataType, final int[] dim)
            throws CDFException.WriterError {
        defineTimeSeries(name, dataType, dim, "Epoch");
    }

    /**
     * Defines a time series of the named variable of specified data type
     * and the time variable of specified name.The named time variable must have
     * been defined before this method
     * is called.Name of the time variable is assigned to the DEPEND_0
     * attribute.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param tname    the tname
     *
     * @throws WriterError the writer error
     */
    public void defineTimeSeries(final String name, final CDFDataType dataType, final int[] dim, final String tname)
            throws CDFException.WriterError {
        defineVariable(name, dataType, dim);
        this.variableDescriptors.get(name);
        VDR tvdr = this.variableDescriptors.get(tname);

        if (tvdr == null) {
            throw new CDFException.WriterError("TimeVariable " + tname + " does not exist.");
        }

        addVariableAttributeEntry(name, "DEPEND_0", tname);
    }

    /**
     * Defines a time series of a new named variable of specified data type
     * and the time variable of specified name and type.
     *
     * @param name       the name
     * @param dataType   the data type
     * @param dim        the dim
     * @param tname      the tname
     * @param timeType   the time type
     * @param compressed the compressed
     *
     * @throws WriterError the writer error
     */
    public void defineTimeSeries(final String name, final CDFDataType dataType, final int[] dim, final String tname,
            final CDFTimeType timeType, final boolean compressed) throws CDFException.WriterError {

        if (!compressed) {
            defineVariable(name, dataType, dim);
        }

        if (compressed) {
            defineCompressedVariable(name, dataType, dim);
        }

        this.variableDescriptors.get(name);
        VDR tvdr = this.variableDescriptors.get(tname);

        if (tvdr != null) {
            throw new CDFException.WriterError("TimeVariable " + tname + " already exists.");
        }

        defineTimeVariable(tname, timeType);
        addVariableAttributeEntry(name, "DEPEND_0", tname);
    }

    /**
     * Defines a time variable of the specified {@link CDFTimeType time type}.
     *
     * @param name     the name
     * @param timeType the time type
     *
     * @throws WriterError the writer error
     */
    public void defineTimeVariable(final String name, final CDFTimeType timeType) throws CDFException.WriterError {
        defineVariable(name, CDFDataType.getType(timeType), new int[0]);
    }

    /**
     * Defines a named variable of specified numeric data type.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     *
     * @throws WriterError the writer error
     */
    public void defineVariable(final String name, final CDFDataType dataType, final int[] dim)
            throws CDFException.WriterError {
        boolean[] varys = new boolean[dim.length];
        Arrays.fill(varys, true);
        defineVariable(name, dataType, dim, 1);
    }

    /**
     * Defines a variable of specified numeric data type using default
     * sparse record option.
     *
     * @param name           the name
     * @param dataType       {@link CDFDataType data tpe}
     * @param dim            dimensions
     * @param varys          dimension variance
     * @param recordVariance the record variance
     * @param compressed     whether the values will be saved in compressed form
     * @param pad            array or wrapped scalar value to assign to use as pad
     *
     * @throws WriterError the writer error
     */
    public void defineVariable(final String name, final CDFDataType dataType, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad) throws CDFException.WriterError {
        defineVariable(name, dataType, dim, varys, recordVariance, compressed, pad, 1, SparseRecordOption.NONE);
    }

    /**
     * Defines a variable of string type using given
     * sparse record option.
     *
     * @param name           the name
     * @param dataType       the data type
     * @param dim            dimensions
     * @param varys          dimension variance
     * @param recordVariance the record variance
     * @param compressed     whether the values will be saved in compressed form
     * @param pad            array or wrapped scalar value to assign to use as pad
     * @param size           length of character string
     *
     * @throws WriterError the writer error
     */
    public void defineVariable(final String name, final CDFDataType dataType, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad, final int size)
            throws CDFException.WriterError {

        defineVariable(name, dataType, dim, varys, recordVariance, compressed, pad, size, SparseRecordOption.NONE);
    }

    /**
     * Defines a new variable.
     *
     * @param name           Variable name
     * @param dataType       {@link CDFDataType data type} of the variable
     * @param dim            dimension
     * @param varys          the varys
     * @param recordVariance the record variance
     * @param compressed     whether the variable data appears in compressed
     *                       form in the CDF
     * @param pad            Object to use as a pad value - a Number object
     *                       for a numeric variable, a String for a character
     *                       variable
     * @param size           length of charater string for character variable,
     *                       Must be 1 for numeric type variable
     * @param option         {@link SparseRecordOption sparse record option}
     *
     * @throws WriterError the writer error
     */
    public void defineVariable(final String name, final CDFDataType dataType, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad, final int size,
            final SparseRecordOption option) throws CDFException.WriterError {

        synchronized (dim) {
            int[] _dim = new int[dim.length];
            System.arraycopy(dim, 0, _dim, 0, dim.length);
        }

        synchronized (varys) {
            boolean[] _varys = new boolean[varys.length];
            System.arraycopy(varys, 0, _varys, 0, varys.length);
        }

        if (dataType == CDFDataType.EPOCH16) {

            if (dim.length > 0) {
                throw new CDFException.WriterError("Only scalar variables of type EPOCH16 are supported.");
            }

        }

        VDR vdr = this.variableDescriptors.get(name);

        if (vdr != null) {
            throw new CDFException.WriterError("Variable " + name + " exists already.");
        }

        Object _pad = null;

        if (pad != null) {
            Class<?> cl = pad.getClass();

            if (!cl.isArray()) {
                _pad = java.lang.reflect.Array.newInstance(cl, 1);
                java.lang.reflect.Array.set(_pad, 0, pad);
            } else {
                _pad = pad;
            }

        }

        try {
            vdr = new VDR(name, dataType.getValue(), dim, varys, recordVariance, compressed, _pad, size, option);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

        vdr.setNum(this.variableDescriptors.size());
        this.variableDescriptors.put(name, vdr);
        DataContainer dc = new DataContainer(vdr, this.rowMajority);
        this.dataContainers.put(name, dc);
    }

    /**
     * Defines a variable of specified numeric data type using given
     * sparse record option.
     *
     * @param name           the name
     * @param dataType       {@link CDFDataType data tpe}
     * @param dim            dimensions
     * @param varys          dimension variance
     * @param recordVariance the record variance
     * @param compressed     whether the values will be saved in compressed form
     * @param pad            array or wrapped scalar value to assign to use as pad
     * @param option         {@link SparseRecordOption sparse record option}
     *
     * @throws WriterError the writer error
     */
    public void defineVariable(final String name, final CDFDataType dataType, final int[] dim, final boolean[] varys,
            final boolean recordVariance, final boolean compressed, final Object pad, final SparseRecordOption option)
            throws CDFException.WriterError {
        defineVariable(name, dataType, dim, varys, recordVariance, compressed, pad, 1, option);
    }

    /**
     * Defines a named variable of specified numeric data type and dimensions.
     *
     * @param name     the name
     * @param dataType the data type
     * @param dim      the dim
     * @param size     the size
     *
     * @throws WriterError the writer error
     */
    public void defineVariable(final String name, final CDFDataType dataType, final int[] dim, final int size)
            throws CDFException.WriterError {

        if ((dataType != CDFDataType.CHAR) && (size > 1)) {
            throw new CDFException.WriterError("incompatible size for type " + dataType);
        }

        boolean[] varys = new boolean[dim.length];
        Arrays.fill(varys, true);
        defineVariable(name, dataType, dim, varys, true, false, null, size);
    }

    /**
     * Returns whether the time variable has been defined for a variable.
     *
     * @param name the name
     *
     * @return true, if successful
     *
     * @throws WriterError the writer error
     */
    public boolean hasTimeVariable(final String name) throws CDFException.WriterError {
        VDR vdr = this.variableDescriptors.get(name);

        if (vdr == null) {
            throw new CDFException.WriterError("Variable " + name + " has not been defined yet.");
        }

        return (findVariableAttributeEntries(name, "DEPEND_0").isEmpty());
    }

    /**
     * Sets the last Leap Second Id.
     * This value is intended to allow applications that read the
     * created CDF to validate TT2000 times in the CDF.
     *
     * @param n integer = year*10000 + month*100 + day, where year
     *          month and day refer to the day following the leap second.
     *          A 0 value for n asserts that applications accept the
     *          validity of TT2000 times. n = -1 implies lastLeapSecondId=20120701,
     *          which is the default for CDF versions prior to 3.6.
     */
    public void setLastLeapSecondId(final int n) {
        this.leapSecondLastUpdated = n;
    }

    /**
     * Prescribes whether an MD5 digest is to be included in
     * the output file.
     *
     * @param need the new MD 5 needed
     */
    public void setMD5Needed(final boolean need) {
        this.needDigest = need;
    }

    /**
     * Sets the value of a given attribute for a variable.
     *
     * @param vname    name of the variable
     * @param aname    name of the attribute
     * @param dataType {@link CDFDataType CDFDataType} desired
     * @param value    array of primitives, or String value to assign to
     *                 attribute
     *                 Overwrites previous value, if any
     *
     * @throws WriterError the writer error
     */
    public void setVariableAttributeEntry(final String vname, final String aname, final CDFDataType dataType,
            final Object value) throws CDFException.WriterError {
        List<VariableAttributeEntry> entries = findVariableAttributeEntries(vname, aname);

        if (!entries.isEmpty()) {

            if (!(value.getClass()
                    .isArray())) {

                // attributeEntries.get(aname).remove(entries.get(0));
                // } else {
                if (value.getClass() != String.class) {
                    throw new CDFException.WriterError("Value should be numeric array or a String.");
                }

            }

            for (VariableAttributeEntry entry : entries) {
                this.attributeEntries.get(aname)
                        .remove(entry);
            }

            ADR adr = getAttribute(aname, false);
            adr.nzEntries--;
        }

        addVariableAttributeEntry(vname, aname, dataType, value);
    }

    /**
     * Sets the value of a given attribute for a variable.
     *
     * @param vname name of the variable
     * @param aname name of the attribute
     * @param value array of primitives, or String value to assign to
     *              attribute
     *
     * @throws WriterError the writer error
     */
    public void setVariableAttributeEntry(final String vname, final String aname, final Object value)
            throws CDFException.WriterError {
        setVariableAttributeEntry(vname, aname, null, value);
    }

    /**
     * Writes CDF to a file.
     *
     * @param fname the fname
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(final String fname) throws IOException, java.io.FileNotFoundException {
        List<AEDR> vec = this.attributeEntries.get("cdfj_source");

        if (vec != null) {

            if (new String(vec.get(0).values).equals(fname)) {
                System.out.println("overwriting " + fname);
                write(fname, true);
                return;
            }

        }

        write(fname, false);
    }

    /**
     * Write.
     *
     * @param fname     the fname
     * @param overwrite the overwrite
     *
     * @return true, if successful
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public boolean write(final String fname, final boolean overwrite) throws IOException, java.io.FileNotFoundException {

        if (this.leapSecondLastUpdated != -1) {
            this.gdr.setLeapSecondLastUpdated(this.leapSecondLastUpdated);
        }

        long len = getSize();

        if (this.needDigest) {
            len += 32;
        }

        if (len > Integer.MAX_VALUE) {

            try (RandomAccessFile raf = new RandomAccessFile(new File(fname), "rw");
                    FileChannel channel = raf.getChannel()) {
                write(channel, len);
                return true;
            }

        }

        ByteBuffer obuf;

        if (isWindows()) {
            obuf = ByteBuffer.allocate((int) len);
        } else {

            if (overwrite) {
                obuf = ByteBuffer.allocateDirect((int) len);
            } else {

                try (RandomAccessFile raf = new RandomAccessFile(new File(fname), "rw");
                        FileChannel channel = raf.getChannel()) {
                    obuf = channel.map(FileChannel.MapMode.READ_WRITE, 0L, len);
                }

            }

        }

        this.cdr.setRowMajority(this.rowMajority);
        this.cdr.setMD5Needed(this.needDigest);
        obuf.put(this.cdr.get());
        // need gdrbuf for insertion of pointers later
        this.gdr.position = obuf.position();
        obuf.position((int) (this.gdr.position + this.gdr.getSize()));
        // assemble attributes
        Set<String> atset = this.attributes.keySet();
        Iterator<String> ait = atset.iterator();
        boolean first = true;
        ADR lastADR = null;

        while (ait.hasNext()) {
            ADR adr = this.attributes.get(ait.next());
            String name = adr.name;
            // if (adr.scope != 1) continue;
            adr.position = obuf.position();
            obuf.position((int) (adr.position + adr.getSize()));
            List<AEDR> vec = this.attributeEntries.get(name);

            for (int i = 0; i < vec.size(); i++) {
                AEDR ae = vec.get(i);
                ae.position = obuf.position();

                if (i == 0) {

                    if (adr.scope == 1) {
                        adr.setAgrEDRHead(ae.position);
                    } else {
                        adr.setAzEDRHead(ae.position);
                    }

                } else {
                    vec.get(i - 1)
                            .setAEDRNext(ae.position);
                }

                obuf.position(obuf.position() + ae.getSize());
            }

            if (first) {
                this.gdr.setADRHead(adr.position);
                first = false;
            } else {

                if (lastADR != null) {
                    lastADR.setADRNext(adr.position);
                }

            }

            lastADR = adr;
        }
        // obuf.limit(obuf.position());

        // write attributes
        ait = atset.iterator();

        while (ait.hasNext()) {
            ADR adr = this.attributes.get(ait.next());
            String name = adr.name;
            obuf.position((int) adr.position);
            obuf.put(adr.get());
            List<AEDR> vec = this.attributeEntries.get(name);

            for (AEDR ae : vec) {
                obuf.position((int) ae.position);
                obuf.put(ae.get());
            }

        }

        Set<String> dcset = this.dataContainers.keySet();
        Iterator<String> dcit = dcset.iterator();
        ByteBuffer cbuf = obuf;

        while (dcit.hasNext()) {
            DataContainer dc = this.dataContainers.get(dcit.next());
            cbuf = dc.update(cbuf);
        }

        obuf.position((int) this.gdr.position);
        this.gdr.setEof(obuf.limit());
        this.gdr.setNumAttr(this.attributes.size());
        this.gdr.setNzVars(this.dataContainers.size());
        obuf.put(this.gdr.get());
        ByteBuffer digest = null;

        if (this.needDigest) {
            obuf.position(0);
            digest = getDigest(obuf);
        }

        if (digest != null) {
            cbuf.put(digest);
        }

        if (isWindows()) {
            writeWin(fname, obuf);
        } else {

            if (overwrite) {

                try (RandomAccessFile raf = new RandomAccessFile(new File(fname), "rw");
                        FileChannel channel = raf.getChannel()) {
                    obuf.position(0);
                    channel.write(obuf);
                    channel.force(true);
                }

            }

        }

        return true;
    }

    void addBuffer(final String name, final VariableDataBuffer data) throws CDFException.WriterError {
        DataContainer container = this.dataContainers.get(name);

        if (container == null) {
            throw new CDFException.WriterError("Variable " + name + " is not defined.");
        }

        try {
            container.addData(data.getBuffer(), new int[] { data.getFirstRecord(), data.getLastRecord() }, false,
                    false);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

    }

    void dispatch(final String name, final Object value) throws CDFException.WriterError {
        Class<?> cl = value.getClass();

        if (cl == String.class) {
            addData(name, new String[] { (String) value });
            return;
        }

        Number num = (Number) value;

        if (cl == Byte.class) {
            addData(name, new byte[] { num.byteValue() });
            return;
        }

        if (cl == Short.class) {
            addData(name, new short[] { num.shortValue() });
            return;
        }

        if (cl == Integer.class) {
            addData(name, new int[] { num.intValue() });
            return;
        }

        if (cl == Double.class) {
            addData(name, new double[] { num.doubleValue() });
            return;
        }

        if (cl == Float.class) {
            addData(name, new float[] { num.floatValue() });
            return;
        }

        if (cl == Long.class) {
            addData(name, new long[] { num.longValue() });
            return;
        }

        throw new CDFException.WriterError("Unrecognized type " + cl);
    }

    /**
     * Returns AttributeEntry collection for the given variable and
     * attribute.
     *
     * @param vname name of the variable
     * @param aname name of the attribute
     */
    List<VariableAttributeEntry> findVariableAttributeEntries(final String vname, final String aname)
            throws CDFException.WriterError {

        VDR vdesc = this.variableDescriptors.get(vname);

        if (vdesc == null) {
            throw new CDFException.WriterError("Variable " + vname + " has not been defined.");
        }

        List<VariableAttributeEntry> result = new CopyOnWriteArrayList<>();

        Iterable<AEDR> entries = this.attributeEntries.get(aname);

        if (entries == null) {
            return result;
        }

        for (AEDR entry : entries) {

            if (entry instanceof VariableAttributeEntry) {

                VariableAttributeEntry vae = (VariableAttributeEntry) entry;

                if (vae.getNum() == vdesc.getNum()) {
                    result.add(vae);
                }

            }

        }

        return result;
    }

    ADR getAttribute(final String name, final boolean global) {
        return getAttribute(name, global, true);
    }

    ADR getAttribute(final String name, final boolean global, final boolean create) {
        ADR adr = this.attributes.get(name);

        if (adr != null) {
            return this.attributes.get(name);
        }

        if (!create) {
            return null;
        }

        adr = new ADR();
        adr.setScope((global) ? 1 : 2);
        adr.name = name;
        int anumber = this.attributes.size();
        adr.setNum(anumber);
        this.attributes.put(name, adr);
        return adr;
    }

    DataContainer getContainer(final String name, final Object data) throws CDFException.WriterError {
        ArrayAttribute aa = null;

        try {
            aa = new ArrayAttribute(data);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th.getMessage());
        }

        if (aa.getDimensions().length != 1) {
            throw new CDFException.WriterError("data must be a 1 dimensional array. ");
        }

        DataContainer container = this.dataContainers.get(name);

        if (container == null) {
            throw new CDFException.WriterError("Variable " + name + " is not defined.");
        }

        return container;
    }

    ByteBuffer getDigest(final ByteBuffer obuf) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsa) {
            nsa.printStackTrace();
            return null;
        }

        int pos = obuf.position();
        byte[] ba = new byte[1_024 * 1_024];

        while (obuf.remaining() > 0) {
            int csize = obuf.remaining();

            if (csize > ba.length) {
                csize = ba.length;
            }

            obuf.get(ba, 0, csize);
            md.update(ba, 0, csize);
        }

        obuf.position(pos);
        return ByteBuffer.wrap(md.digest());
    }

    void getDigest(final SeekableByteChannel channel) throws IOException {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsa) {
            nsa.printStackTrace();
            return;
        }

        // System.out.println(channel.position());
        byte[] ba = new byte[1_024 * 1_024];
        ByteBuffer buf = ByteBuffer.wrap(ba);
        long remaining = channel.size();
        channel.position(0);

        while (remaining > 0) {
            long csize = remaining;

            if (csize > ba.length) {
                csize = ba.length;
            }

            buf.position(0);
            buf.limit((int) csize);
            int trans = channel.read(buf);

            if (trans == -1) {
                throw new IOException("Unexpected end of data");
            }

            md.update(ba, 0, trans);
            remaining -= trans;
        }

        // System.out.println(channel.position());
        // channel.position(channel.size());
        channel.write(ByteBuffer.wrap(md.digest()));
        // System.out.println(channel.size());
    }

    long getSize() {
        long size = this.cdr.getSize();
        size += this.gdr.getSize();
        Set<String> atset = this.attributes.keySet();
        Iterator<String> ait = atset.iterator();

        while (ait.hasNext()) {
            ADR adr = this.attributes.get(ait.next());
            size += adr.getSize();
        }

        Set<String> ateset = this.attributeEntries.keySet();
        Iterator<String> aeit = ateset.iterator();

        while (aeit.hasNext()) {
            Iterable<AEDR> vec = this.attributeEntries.get(aeit.next());

            for (AEDR aedr : vec) {
                size += aedr.getSize();
            }

        }

        Set<String> dcset = this.dataContainers.keySet();
        Iterator<String> dcit = dcset.iterator();
        boolean first = true;
        DataContainer lastContainer = null;

        while (dcit.hasNext()) {
            DataContainer dc = this.dataContainers.get(dcit.next());
            dc.position = size;

            if (first) {
                this.gdr.setZVDRHead(size);
                first = false;
            } else {
                lastContainer.getVDR()
                        .setVDRNext(dc.position);
            }

            lastContainer = dc;
            size += dc.getSize();
        }

        return size;
    }

    HashMap<String, VDR> getVariableDescriptors() {
        return this.variableDescriptors;
    }

    /*
     * byte[] ba = new byte[chunkSize];
     * int toWrite = buf.remaining();
     * int written = 0;
     * while (written < toWrite) {
     * int n = toWrite - written;
     * if (n > chunkSize) n = chunkSize;
     * buf.get(ba, 0, n);
     * fos.write(ba, 0, n);
     * written += n;
     * }
     * }
     * static int chunkSize = 64*1024;
     */
    boolean isWindows() {
        return (System.getProperty("os.name")
                .toLowerCase()
                .startsWith("win"));
    }

    void write(final FileChannel channel, final long len) throws IOException {
        this.cdr.setRowMajority(this.rowMajority);
        this.cdr.setMD5Needed(this.needDigest);
        channel.write(this.cdr.get());
        // need gdrbuf for insertion of pointers later
        this.gdr.position = channel.position();
        channel.position(this.gdr.position + this.gdr.getSize());
        // assemble attributes
        Set<String> atset = this.attributes.keySet();
        Iterator<String> ait = atset.iterator();
        boolean first = true;
        ADR lastADR = null;

        while (ait.hasNext()) {
            ADR adr = this.attributes.get(ait.next());
            String name = adr.name;
            // if (adr.scope != 1) continue;
            adr.position = channel.position();
            channel.position(adr.position + adr.getSize());
            List<AEDR> vec = this.attributeEntries.get(name);

            for (int i = 0; i < vec.size(); i++) {
                AEDR ae = vec.get(i);
                ae.position = channel.position();

                if (i == 0) {

                    if (adr.scope == 1) {
                        adr.setAgrEDRHead(ae.position);
                    } else {
                        adr.setAzEDRHead(ae.position);
                    }

                } else {
                    vec.get(i - 1)
                            .setAEDRNext(ae.position);
                }

                channel.position(channel.position() + ae.getSize());
            }

            if (first) {
                this.gdr.setADRHead(adr.position);
                first = false;
            } else {

                if (lastADR != null) {
                    lastADR.setADRNext(adr.position);
                }

            }

            lastADR = adr;
        }

        // write attributes
        ait = atset.iterator();

        while (ait.hasNext()) {
            ADR adr = this.attributes.get(ait.next());
            String name = adr.name;
            channel.position(adr.position);
            channel.write(adr.get());

            Iterable<AEDR> vec = this.attributeEntries.get(name);

            for (AEDR ae : vec) {
                channel.position(ae.position);
                channel.write(ae.get());
            }

        }

        Set<String> dcset = this.dataContainers.keySet();
        Iterator<String> dcit = dcset.iterator();

        while (dcit.hasNext()) {
            DataContainer dc = this.dataContainers.get(dcit.next());
            dc.update(channel);
        }

        channel.position(this.gdr.position);
        this.gdr.setEof(channel.size());
        this.gdr.setNumAttr(this.attributes.size());
        this.gdr.setNzVars(this.dataContainers.size());
        channel.write(this.gdr.get());
        channel.position(channel.size());

        if (this.needDigest) {
            getDigest(channel);
        }

    }

    void writeWin(final String fname, final ByteBuffer buf) throws IOException, java.io.FileNotFoundException {

        try (FileOutputStream fos = new FileOutputStream(fname)) {
            byte[] ba = buf.array();
            fos.write(ba);
            // System.out.println("finished writing to " + fname);
        }

    }
}
