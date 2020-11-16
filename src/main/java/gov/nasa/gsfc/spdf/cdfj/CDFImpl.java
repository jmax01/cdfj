package gov.nasa.gsfc.spdf.cdfj;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import lombok.ToString;

abstract class CDFImpl implements CDFCore, java.io.Serializable, Closeable {

    private static final long serialVersionUID = -7106016786475171626L;

    /** CDF constants. */
    public static final int GDR_RECORD = 2;

    /** The Constant FLAGS_MAJORITY_MASK. */
    public static final int FLAGS_MAJORITY_MASK = 0x01;

    /** The Constant ROW_MAJOR. */
    public static final int ROW_MAJOR = 1;

    /** The Constant VXR_RECORD_TYPE. */
    public static final int VXR_RECORD_TYPE = 6;

    /** The Constant VVR_RECORD_TYPE. */
    public static final int VVR_RECORD_TYPE = 7;

    /** The Constant CVVR_RECORD_TYPE. */
    public static final int CVVR_RECORD_TYPE = 13;

    /** The Constant STRINGDELIMITER. */
    public static final String STRINGDELIMITER = "\\N ";

    static final Logger LOGGER = CDFLogging.newLogger(CDFImpl.class);

    transient volatile FileChannel fileChannel;

    /**
     * CDF offsets
     */
    int offset_NEXT_VDR;

    int offset_NEXT_ADR;

    int offset_ATTR_NAME;

    int offset_SCOPE;

    int offset_AgrEDRHead;

    int offset_AzEDRHead;

    int offset_NEXT_AEDR;

    int offset_ENTRYNUM;

    int offset_ATTR_DATATYPE;

    int offset_ATTR_NUM_ELEMENTS;

    int offset_VALUE;

    int offset_VAR_NAME;

    int offset_VAR_NUM_ELEMENTS;

    int offset_NUM;

    int offset_FLAGS;

    int offset_sRecords;

    int offset_BLOCKING_FACTOR;

    int offset_VAR_DATATYPE;

    int offset_zNumDims;

    int offset_FIRST_VXR;

    int offset_NEXT_VXR;

    int offset_NENTRIES;

    int offset_NUSED;

    int offset_FIRST;

    int offset_RECORD_TYPE;

    int offset_RECORDS;

    int offset_CSIZE;

    int offset_CDATA;

    /**
     * CDF metadata
     */
    int version;

    int release;

    int encoding;

    int flags;

    int increment;

    transient ByteOrder byteOrder;

    boolean bigEndian;

    /**
     * Extracted from GDR
     */
    long rVDRHead;

    long zVDRHead;

    long ADRHead;

    int numberOfRVariables;

    int numberOfAttributes;

    int numberOfZVariables;

    int[] rDimSizes;

    int lastLeapSecondId;

    transient ByteBuffer buf;

    /** The variable names. */
    protected String[] variableNames;

    /** The variable table. */
    private Map<String, CDFVariable> cdfVariablesByName;

    private Map<Integer, CDFVariable> cdfZVariablesByNumber;

    private Map<Integer, CDFVariable> cdfRVariablesByNumber;

    private Map<String, CDFAttribute> cdfAttributesByName;

    /** The this CDF. */
    protected CDFCore thisCDF;

    /** The source. */
    protected CDFFactory.CDFSource source;

    /** The processing option. */
    protected CDFFactory.ProcessingOption processingOption;

    /**
     * Instantiates a new CDF impl.
     *
     * @param buf the buf
     */
    protected CDFImpl(final ByteBuffer buf) {
        this.buf = buf;

    }

    /**
     * Instantiates a new CDF impl.
     *
     * @param buf         the buf
     * @param fileChannel the file channel
     */
    protected CDFImpl(final ByteBuffer buf, final FileChannel fileChannel) {
        this.buf = buf;
        this.fileChannel = fileChannel;

    }

    /**
     * Gets the number attribute.
     *
     * @param type      the type
     * @param nelement  the nelement
     * @param vbuf      the vbuf
     * @param byteOrder the byte order
     *
     * @return the number attribute
     */
    public static Object getNumberAttribute(final int type, final int nelement, final ByteBuffer vbuf,
            final ByteOrder byteOrder) {
        ByteBuffer vbufLocal = vbuf.duplicate();
        vbufLocal.order(byteOrder);
        int ne = nelement;

        if (type == DataTypes.EPOCH16) {
            ne = 2 * nelement;
        }

        long[] lvalue = null;
        double[] value = null;
        long longInt = DataTypes.longInt[type];
        boolean longType = false;

        try {

            if ((type > 20) || (type < 10)) {

                if (DataTypes.typeCategory[type] == DataTypes.LONG) {
                    lvalue = new long[ne];
                    longType = true;
                } else {
                    value = new double[ne];
                }

                for (int i = 0; i < ne; i++) {
                    Number num = (Number) DataTypes.method[type].invoke(vbufLocal);

                    if (!longType) {
                        value[i] = num.doubleValue();
                    }

                    if (longType) {
                        lvalue[i] = num.longValue();
                    }

                }

            } else {
                value = new double[ne];

                for (int i = 0; i < nelement; i++) {
                    Number num = (Number) DataTypes.method[type].invoke(vbufLocal);
                    int n = num.intValue();
                    value[i] = ((n >= 0) ? n : (longInt + n));
                }

            }

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.out.println("getNumberAttribute: " + vbuf);
            System.out.println("type: " + type);
            ex.printStackTrace();
            LOGGER.log(Level.SEVERE, ex, () -> "Failed to get number attributes for type, " + type + " nelement");
            return null;
        }

        if (longType) {
            return lvalue;
        }

        return value;
    }

    /**
     * Target attribute instance.
     *
     * @param p the p
     * @param c the c
     *
     * @return the target attribute
     */
    public static TargetAttribute targetAttributeInstance(final boolean p, final boolean c) {
        return new TargetAttribute(p, c);
    }

    @Override
    public List<AttributeEntry> attributeEntries(final String attributeName) {

        if (this.cdfAttributesByName == null) {
            throw new IllegalStateException("No attributes " + attributeName);
        }

        final CDFAttribute a = this.cdfAttributesByName.get(attributeName);

        if (a == null) {
            throw new IllegalArgumentException("No attribute named " + attributeName);
        }

        if (!a.isGlobal()) {
            throw new IllegalArgumentException("No global attribute named " + attributeName);
        }

        return a.gEntries;
    }

    @Override
    public List<AttributeEntry> attributeEntries(final String variableName, final String attributeName) {

        CDFVariable cdfVariable = this.cdfVariablesByName.get(variableName);

        if (cdfVariable == null) {
            return null;
        }

        return Collections.unmodifiableList(cdfVariable.getAttributes()
                .stream()
                .filter(ae -> ae.getAttributeName()
                        .equals(attributeName))
                .collect(Collectors.toList()));
    }

    @Override
    public void close() throws IOException {

        if ((this.fileChannel != null) && this.fileChannel.isOpen()) {

            synchronized (this.fileChannel) {

                if ((this.fileChannel != null) && this.fileChannel.isOpen()) {
                    this.fileChannel.close();
                }

            }

        }

    }

    /**
     * Extract bytes.
     *
     * @param bufOffset the buf offset
     * @param ba        the ba
     * @param offset    the offset
     * @param len       the len
     */
    public void extractBytes(final int bufOffset, final byte[] ba, final int offset, final int len) {
        this.buf.duplicate()
                .position(bufOffset)
                .get(ba, offset, len);
    }

    /**
     * Find variable by name.
     *
     * @param name the name
     *
     * @return the optional
     */
    public Optional<Variable> findVariableByName(final String name) {
        return Optional.ofNullable(this.cdfVariablesByName.get(name));
    }

    /**
     * Gets the.
     *
     * @param variableName the variable name
     *
     * @return the object
     */
    public Object get(final String variableName) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (DataTypes.isStringType(variable.getType())) {
            VDataContainer.CString container = variable.getStringContainer(null);
            container.run();
            StringArray sa = (StringArray) container.asArray();
            return sa.array();
        }

        VDataContainer.CDouble container = variable.getDoubleContainer(null, false);
        container.run();
        DoubleArray da = container.asArray();
        return da.array();
    }

    /**
     * Gets the.
     *
     * @param variableName the variable name
     * @param element      the element
     *
     * @return the object
     */
    public Object get(final String variableName, final int element) {
        return get(variableName, new int[] { element });
    }

    /**
     * Gets the.
     *
     * @param variableName the variable name
     * @param index0       the index 0
     * @param index1       the index 1
     *
     * @return the object
     */
    public Object get(final String variableName, final int index0, final int index1) {

        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (DataTypes.isStringType(variable.getType())) {
            throw new IllegalArgumentException(
                    "Function not supported for string variables of type " + variable.getType());
        }

        DoubleVarContainer dbuf = new DoubleVarContainer(this, variable, null, false, ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf.asArrayElement(index0, index1);
    }

    /**
     * Gets the.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param element      the element
     *
     * @return the object
     */
    public Object get(final String variableName, final int first, final int last, final int element) {
        return get(variableName, first, last, new int[] { element });
    }

    /**
     * Gets the.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param elements     the elements
     *
     * @return the object
     */
    public Object get(final String variableName, final int first, final int last, final int[] elements) {
        DoubleVarContainer dbuf = getRangeBuffer(variableName, first, last);
        return dbuf.asArrayElement(elements);
    }

    /**
     * Gets the.
     *
     * @param variableName the variable name
     * @param elements     the elements
     *
     * @return the object
     */
    public Object get(final String variableName, final int[] elements) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (DataTypes.isStringType(variable.getType())) {
            throw new IllegalArgumentException(
                    "Function not supported for string variables of type " + variable.getType());
        }

        DoubleVarContainer dbuf = new DoubleVarContainer(this, variable, null, false, ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf.asArrayElement(elements);
    }

    /**
     * Gets the 1d.
     *
     * @param variableName the variable name
     *
     * @return the 1d
     */
    @Override
    public double[] get1D(final String variableName) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (!variable.isCompatible(Double.TYPE)) {
            throw new IllegalArgumentException("Variable " + variableName + " cannot be "
                    + "converted to double, or the conversion may result in loss of precision. Use get1D("
                    + variableName + ", Boolean.TRUE) for string type. Otherwise use get1D(" + variableName
                    + ", false");
        }

        return variable.asDoubleArray();
    }

    /**
     * Gets the 1d.
     *
     * @param variableName the variable name
     * @param preserve     the preserve
     *
     * @return the 1d
     */
    @Override
    public Object get1D(final String variableName, final boolean preserve) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        int type = variable.getType();

        if (DataTypes.isStringType(type)) {
            return variable.asByteArray(null);
        }

        if (preserve) {

            if (DataTypes.isLongType(type)) {
                return variable.asLongArray(null);
            }

        }

        return variable.asDoubleArray();
    }

    /**
     * Gets the 1d.
     *
     * @param variableName the variable name
     * @param stringType   the string type
     *
     * @return the 1d
     */
    @Override
    public byte[] get1D(final String variableName, final Boolean stringType) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        int type = variable.getType();

        if (!DataTypes.isStringType(type)) {
            throw new IllegalArgumentException("Variable " + variableName + " is not a string variable");
        }

        return variable.asByteArray(null);
    }

    /**
     * Gets the 1d.
     *
     * @param variableName the variable name
     * @param point        the point
     *
     * @return the 1d
     */
    @Override
    public Object get1D(final String variableName, final int point) {
        return get1D(variableName, point, -1);
    }

    /**
     * Gets the 1d.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     *
     * @return the 1d
     */
    /*
     * public double[] get1D(String variableName, int first, int last, int[] stride)
     * {
     * DoubleVarContainer dbuf = getRangeBuffer(variableName, first, last);
     * return dbuf.asSampledArray(stride);
     * }
     */
    @Override
    public Object get1D(final String variableName, final int first, final int last) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        int type = variable.getType();
        int[] range = (last >= 0) ? new int[] { first, last } : new int[] { first };

        if (DataTypes.isLongType(type)) {
            return variable.asLongArray(range);
        }

        if (DataTypes.isStringType(type)) {
            return variable.asByteArray(range);
        }

        return variable.asDoubleArray(range);
    }

    /**
     * returns value of the named global attribute.
     *
     * @param atr the atr
     *
     * @return the attribute
     */
    @Override
    public Object getAttribute(final String atr) {

        if (this.cdfAttributesByName == null) {
            return null;
        }

        CDFAttribute a = this.cdfAttributesByName.get(atr);

        if (a == null) {
            return null;
        }

        if (!a.isGlobal()) {
            return null;
        }

        if (a.gEntries.isEmpty()) {
            return null;
        }

        AttributeEntry ae = a.gEntries.get(0);

        if (ae.isStringType()) {

            String[] sa = new String[a.gEntries.size()];

            for (int i = 0; i < a.gEntries.size(); i++) {

                ae = a.gEntries.get(i);

                sa[i] = (String) ae.getValue();
            }

            return sa;
        }

        return ae.getValue();
    }

    @Override
    public Object getAttribute(final String variableName, final String atr) {

        CDFVariable cdfVariable = this.cdfVariablesByName.get(variableName);

        if (cdfVariable == null) {
            return null;
        }

        return cdfVariable.getAttributes()
                .stream()
                .filter(ae -> ae.getAttributeName()
                        .equals(atr))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    @Deprecated
    @Override
    public Vector getAttributeEntries(final String attributeName) {
        return new Vector<>(attributeEntries(attributeName));
    }

    /**
     * returns list of AttributeEntry objects for the named attribute
     * for the named variable.
     *
     * @param variableName the variable name
     *
     * @return the attribute entries or null
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public Vector<AttributeEntry> getAttributeEntries(final String variableName, final String attributeName) {
        List<AttributeEntry> attributeEntries = attributeEntries(variableName, attributeName);

        return (attributeEntries == null) ? null : new Vector<>(attributeEntries);

    }

    /**
     * Gets the byte array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the byte array
     */
    @Override
    public byte[] getByteArray(final String variableName, final int[] pt) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.asByteArray(pt);
    }

    /**
     * returns byte order of source CDF.
     *
     * @return the byte order
     */
    @Override
    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    /**
     * Gets the double array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the double array
     */
    @Override
    public double[] getDoubleArray(final String variableName, final int[] pt) {
        return getDoubleArray(variableName, pt, true);
    }

    /**
     * Gets the double array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the double array
     */
    @Override
    public double[] getDoubleArray(final String variableName, final int[] pt, final boolean preserve) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.asDoubleArray(preserve, pt);
    }

    /**
     * Gets the float array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the float array
     */
    @Override
    public float[] getFloatArray(final String variableName, final int[] pt) {
        return getFloatArray(variableName, pt, true);
    }

    /**
     * Gets the float array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the float array
     */
    @Override
    public float[] getFloatArray(final String variableName, final int[] pt, final boolean preserve) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.asFloatArray(preserve, pt);
    }

    /**
     * returns value of the named global attribute as GlobalAttribute
     * object.
     *
     * @param atr the atr
     *
     * @return the global attribute
     */
    @Override
    public GlobalAttribute getGlobalAttribute(final String atr) {

        if (this.cdfAttributesByName == null) {
            throw new IllegalStateException("No attribute named " + atr);
        }

        final CDFAttribute a = this.cdfAttributesByName.get(atr);

        if (a == null) {
            throw new IllegalArgumentException("No attribute named " + atr);
        }

        if (!a.isGlobal()) {
            throw new IllegalArgumentException("No global attribute named " + atr);
        }

        return new GlobalAttribute() {

            @Override
            public Object getEntry(final int n) {

                if (n > a.gEntries.size()) {
                    return null;
                }

                if (n < 0) {
                    return null;
                }

                AttributeEntry ae = a.gEntries.get(n);
                return ae.getValue();
            }

            @Override
            public int getEntryCount() {
                return a.gEntries.size();
            }

            @Override
            public String getName() {
                return a.getName();
            }

            @Override
            public int getNum() {
                return a.num;
            }

            @Override
            public boolean isGlobal() {
                return true;
            }

            @Override
            public boolean isLongType(final int n) {

                if ((n < 0) || (n > a.gEntries.size())) {
                    throw new IllegalArgumentException("Invalid attribute index " + n);
                }

                AttributeEntry ae = a.gEntries.get(n);
                return ae.isLongType();
            }

            @Override
            public boolean isStringType(final int n) {

                if ((n < 0) || (n > a.gEntries.size())) {
                    throw new IllegalArgumentException("Invalid attribute index ");
                }

                AttributeEntry ae = a.gEntries.get(n);
                return ae.isStringType();
            }
        };
    }

    /**
     * Gets the int array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the int array
     */
    @Override
    public int[] getIntArray(final String variableName, final int[] pt) {
        return getIntArray(variableName, pt, true);
    }

    /**
     * Gets the int array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the int array
     */
    @Override
    public int[] getIntArray(final String variableName, final int[] pt, final boolean preserve) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.asIntArray(preserve, pt);
    }

    /**
     * Gets the long.
     *
     * @param variableName the variable name
     *
     * @return the long
     */
    public Object getLong(final String variableName) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (!DataTypes.isLongType(variable.getType())) {
            throw new IllegalArgumentException("getLong method appropriate for TT2000 and INT8 types. ");
        }

        VDataContainer.CLong container = variable.getLongContainer(null);
        container.run();
        LongArray la = container.asArray();
        return la.array();
    }

    /**
     * Gets the long array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the long array
     */
    @Override
    public long[] getLongArray(final String variableName, final int[] pt) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.asLongArray(pt);
    }

    /**
     * Gets the one D.
     *
     * @param variableName the variable name
     * @param columnMajor  the column major
     *
     * @return the one D
     */
    @Override
    public double[] getOneD(final String variableName, final boolean columnMajor) {
        CDFVariable variable = (CDFVariable) getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (!variable.isCompatible(Double.TYPE)) {
            throw new IllegalArgumentException("Variable " + variableName + " cannot be "
                    + "converted to double, or the conversion may result in loss of precision. Use getOneD("
                    + variableName + ", Boolean.TRUE) for string type. Otherwise use get1D(" + variableName
                    + ", false");
        }

        TargetAttribute ta = new TargetAttribute(false, columnMajor);
        return variable.asDoubleArray(ta, null);
    }

    /**
     * Gets the one D.
     *
     * @param variableName the variable name
     * @param stringType   the string type
     * @param columnMajor  the column major
     *
     * @return the one D
     */
    public byte[] getOneD(final String variableName, final Boolean stringType, final boolean columnMajor) {
        CDFVariable variable = (CDFVariable) getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        int type = variable.getType();

        if (!DataTypes.isStringType(type)) {
            throw new IllegalArgumentException("Variable " + variableName + " is not a string variable");
        }

        return variable.asByteArray(null, columnMajor);
    }

    /**
     * Gets the point.
     *
     * @param variableName the variable name
     * @param point        the point
     *
     * @return the point
     */
    // --- POINT
    public Object getPoint(final String variableName, final int point) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (DataTypes.isStringType(variable.getType())) {
            VDataContainer.CString container = variable.getStringContainer(null);
            container.run();
            StringArray sa = (StringArray) container.asArray();
            return sa.array();
        }

        VDataContainer dbuf = new DoubleVarContainer(this, variable, new int[] { point }, false,
                ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf.asArray()
                .array();
    }

    /**
     * Gets the range.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     *
     * @return the range
     */
    public Object getRange(final String variableName, final int first, final int last) {
        return getRange(variableName, first, last, false);
    }

    /**
     * Gets the range.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param oned         the oned
     *
     * @return the range
     */
    // --- RANGE
    public Object getRange(final String variableName, final int first, final int last, final boolean oned) {
        DoubleVarContainer dbuf = getRangeBuffer(variableName, first, last);

        if (oned) {
            return dbuf.as1DArray();
        }

        return dbuf.asArray()
                .array();
    }

    /**
     * Gets the range.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param element      the element
     *
     * @return the range
     */
    public Object getRange(final String variableName, final int first, final int last, final int element) {
        return getRange(variableName, first, last, new int[] { element });
    }

    /**
     * Gets the range.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param elements     the elements
     *
     * @return the range
     */
    public Object getRange(final String variableName, final int first, final int last, final int[] elements) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (DataTypes.isStringType(variable.getType())) {
            throw new IllegalArgumentException("Function not supported for string variables");
        }

        DoubleVarContainer dbuf = getRangeBuffer(variableName, first, last);
        return dbuf.asArrayElement(elements);
    }

    /**
     * Gets the range one D.
     *
     * @param variableName the variable name
     * @param first        the first
     * @param last         the last
     * @param columnMajor  the column major
     *
     * @return the range one D
     */
    @Override
    public Object getRangeOneD(final String variableName, final int first, final int last, final boolean columnMajor) {
        DoubleVarContainer dbuf = getRangeBuffer(variableName, first, last);
        return dbuf.asOneDArray(columnMajor);
    }

    /**
     * Gets the short array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     *
     * @return the short array
     */
    @Override
    public short[] getShortArray(final String variableName, final int[] pt) {
        return getShortArray(variableName, pt, true);
    }

    /**
     * Gets the short array.
     *
     * @param variableName the variable name
     * @param pt           the pt
     * @param preserve     the preserve
     *
     * @return the short array
     */
    @Override
    public short[] getShortArray(final String variableName, final int[] pt, final boolean preserve) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.asShortArray(preserve, pt);
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    @Override
    public CDFFactory.CDFSource getSource() {
        return this.source;
    }

    /**
     * Gets the string.
     *
     * @param offset the offset
     *
     * @return the string
     */
    public String getString(final long offset) {

        if (this.fileChannel == null) {
            return getString(offset, maxStringSize());
        }

        ByteBuffer _buf = getRecord(offset, maxStringSize());

        return getString(_buf, maxStringSize());
    }

    /**
     * Gets the value buffer.
     *
     * @param offset the offset
     *
     * @return the value buffer
     */
    public ByteBuffer getValueBuffer(final long offset) {
        ByteBuffer bv = getRecord(offset);
        bv.position(this.offset_RECORDS);
        return bv;
    }

    /**
     * Gets the value buffer.
     *
     * @param offset the offset
     * @param size   the size
     * @param number the number
     *
     * @return the value buffer
     */
    public ByteBuffer getValueBuffer(final long offset, final int size, final int number) {
        ByteBuffer bv = getRecord(offset);

        if (bv.getInt(this.offset_RECORD_TYPE) == VVR_RECORD_TYPE) {
            /*
             * System.out.println("Encountered uncompressed instead of " +
             * " compressed at offset " + offset);
             */
            bv.position(this.offset_RECORDS);
            return bv;
        }

        int clen = lowOrderInt(bv, this.offset_CSIZE);
        byte[] work = new byte[clen];
        bv.position(this.offset_CDATA);
        bv.get(work);
        byte[] udata = new byte[size * number];
        int n = 0;

        try {
            GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(work));
            int toRead = udata.length;
            int off = 0;

            while (toRead > 0) {
                n = gz.read(udata, off, toRead);

                if (n == -1) {
                    break;
                }

                off += n;
                toRead -= n;
            }

        } catch (IOException ex) {
            System.out.println(ex + " at offset " + offset);
            System.out.println("Trying to get data as uncompressed");
            return getValueBuffer(offset);
        }

        if (n < 0) {
            return null;
        }

        return ByteBuffer.wrap(udata);
    }

    /**
     * returns the object that implements the Variable interface for
     * the named variable.
     *
     * @param name the name
     *
     * @return the variable or null;
     */
    @Override
    public Variable getVariable(final String name) {
        return this.cdfVariablesByName.get(name);
    }

    /**
     * Gets the variable by name.
     *
     * @param name the name
     *
     * @return the variable by name
     */
    public Optional<Variable> getVariableByName(final String name) {
        return Optional.ofNullable(this.cdfVariablesByName.get(name));
    }

    /**
     * returns variable names in a String[].
     *
     * @return the variable names
     */
    @Override
    public String[] getVariableNames() {
        String[] sa = new String[this.variableNames.length];
        System.arraycopy(this.variableNames, 0, sa, 0, sa.length);
        return sa;
    }

    /**
     * returns variable names of a given VAR_TYPE in a String[].
     *
     * @param type the type
     *
     * @return the variable names
     */
    // FIXME: move to istp dedicated package or library
    @Override
    public String[] getVariableNames(final String type) {

        Collection<String> vars = new ArrayList<>();

        for (String variableName : this.variableNames) {

            @SuppressWarnings("unchecked")
            List<String> v = (List<String>) getAttribute(variableName, "VAR_TYPE");

            if ((v == null) || v.isEmpty()) {
                continue;
            }

            String s = v.get(0);

            if (s.equals(type)) {
                vars.add(variableName);
            }

        }

        return vars.toArray(String[]::new);
    }

    /**
     * returns names of global attributes in a String[].
     *
     * @return the string[]
     */
    @Override
    public String[] allAttributeNames() {

        return this.cdfAttributesByName.values()
                .stream()
                .map(CDFAttribute::getName)
                .toArray(String[]::new);

    }

    /**
     * returns names of global attributes in a String[].
     *
     * @return the string[]
     */
    @Override
    public String[] globalAttributeNames() {

        return this.cdfAttributesByName.values()
                .stream()
                .filter(CDFAttribute::isGlobal)
                .map(CDFAttribute::getName)
                .toArray(String[]::new);

    }

    /**
     * Checks if is big endian.
     *
     * @return true, if is big endian
     */
    public boolean isBigEndian() {
        return this.bigEndian;
    }

    /**
     * Checks if is compatible.
     *
     * @param variableName the variable name
     * @param cl           the cl
     *
     * @return true, if is compatible
     */
    @Override
    public boolean isCompatible(final String variableName, final Class<?> cl) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        return variable.isCompatible(cl);
    }

    /**
     * returns row majority of source CDF.
     *
     * @return true, if successful
     */
    @Override
    public boolean rowMajority() {
        return ((this.flags & FLAGS_MAJORITY_MASK) == ROW_MAJOR);
    }

    @Override
    public String[] variableAttributeNames(final String variableName) {

        CDFVariable variable = this.cdfVariablesByName.get(variableName);

        if (variable == null) {
            return null;
        }

        return variable.getAttributes()
                .stream()
                .map(AttributeEntry::getAttributeName)
                .toArray(String[]::new);
    }

    /**
     * returns dimensions of the named variable.
     *
     * @param name the name
     *
     * @return the int[]
     */
    public int[] variableDimensions(final String name) {
        VariableMetaData variable = this.cdfVariablesByName.get(name);

        if (variable == null) {
            return null;
        }

        int[] dims = variable.getDimensions();
        int[] ia = new int[dims.length];
        System.arraycopy(ia, 0, dims, 0, dims.length);
        return ia;
    }

    /**
     * Gets the buffer.
     *
     * @return the buffer
     */
    protected ByteBuffer getBuffer() {
        return this.buf;
    }

    abstract int readRecordSizeFieldAsInt(ByteBuffer recordSizeFieldByteBuffer);

    /**
     * Gets the record.
     *
     * @param offset the offset
     *
     * @return the record
     */
    protected ByteBuffer getRecord(final long offset) {

        if (this.fileChannel == null) {
            ByteBuffer _buf = this.buf.duplicate();
            _buf.position((int) offset);
            return _buf.slice();
        }

        ByteBuffer recordSizeFieldByteBuffer = ByteBuffer.allocate(recordSizeFieldSize());

        synchronized (this.fileChannel) {

            try {
                this.fileChannel.position(offset);
                this.fileChannel.read(recordSizeFieldByteBuffer);
                recordSizeFieldByteBuffer.position(0);
                int size = readRecordSizeFieldAsInt(recordSizeFieldByteBuffer);
                return getRecord(offset, size);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to read from file channel", e);
                return null;
            }

        }

    }

    /**
     * Gets the record.
     *
     * @param offset the offset
     * @param size   the size
     *
     * @return the record
     */
    protected ByteBuffer getRecord(final long offset, final int size) {

        ByteBuffer bb = ByteBuffer.allocate(size);

        synchronized (this.fileChannel) {

            try {
                this.fileChannel.position(offset);

                int got = this.fileChannel.read(bb);

                if (got != size) {

                    LOGGER.log(Level.SEVERE,
                            () -> String.format("Failed to get record at offset %s, needed %s bytes, got %s bytes.",
                                    offset, size, got));

                    return null;
                }

                bb.position(0);
                return bb;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to read from file channel", e);
                return null;
            }

        }

    }

    /**
     * Gets the record offset.
     *
     * @return the record offset
     */
    protected int getRecordOffset() {
        return this.offset_RECORDS;
    }

    /**
     * Gets the string.
     *
     * @param _buf the buf
     * @param max  the max
     *
     * @return the string
     */
    protected String getString(final ByteBuffer _buf, final int max) {

        byte[] ba = new byte[max];

        int i = 0;

        for (; i < max; i++) {
            ba[i] = _buf.get();

            if (ba[i] == 0) {
                break;
            }

        }

        return new String(ba, 0, i);
    }

    /**
     * Gets the string.
     *
     * @param offset the offset
     * @param max    the max
     *
     * @return the string
     */
    protected String getString(final long offset, final int max) {
        return getString(getRecord(offset), max);
    }

    /**
     * Long int.
     *
     * @param buf the buf
     *
     * @return the long
     */
    protected abstract long longInt(ByteBuffer byteBuffer);

    /**
     * Low order int.
     *
     * @param buf the buf
     *
     * @return the int
     */
    protected abstract int lowOrderInt(ByteBuffer byteBuffer);

    /**
     * Low order int.
     *
     * @param buf    the buf
     * @param offset the offset
     *
     * @return the int
     */
    protected abstract int lowOrderInt(ByteBuffer byteBuffer, int offset);

    /**
     * Sets the buffer.
     *
     * @param b the new buffer
     */
    protected void setBuffer(final ByteBuffer b) {
        this.buf = b;
    }

    /**
     * Sets the byte order.
     *
     * @param _bigEndian the new byte order
     */
    protected void setByteOrder(final boolean _bigEndian) {
        this.byteOrder = (_bigEndian) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        setByteOrder(this.byteOrder);
    }

    /**
     * Sets the byte order.
     *
     * @param bo the new byte order
     */
    protected void setByteOrder(final ByteOrder bo) {
        this.bigEndian = bo.equals(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Sets the option.
     *
     * @param option the new option
     */
    protected void setOption(final CDFFactory.ProcessingOption option) {
        this.processingOption = option;
    }

    /**
     * Sets the source.
     *
     * @param source the new source
     */
    protected void setSource(final CDFFactory.CDFSource source) {
        this.source = source;
    }

    /**
     * Variables.
     *
     * @return the map
     */
    protected Map<String, CDFVariable> variables() {

        if (this.cdfVariablesByName != null) {
            return this.cdfVariablesByName;
        }

        this.cdfVariablesByName = new HashMap<>();

        this.cdfRVariablesByNumber = new HashMap<>();

        this.cdfZVariablesByNumber = new HashMap<>();

        int[] offsets = { (int) this.zVDRHead, (int) this.rVDRHead };

        String[] vtypes = { "z", "r" };

        Collection<String> variableNamesAsList = new CopyOnWriteArrayList<>();

        for (int vtype = 0; vtype < 2; vtype++) {

            long offset = offsets[vtype];

            if (offset == 0) {
                continue;
            }

            ByteBuffer _buf = getRecord(offset);

            while (true) {
                _buf.position(this.offset_NEXT_VDR);

                // int next = lowOrderInt(_buf);

                long next = longInt(_buf);

                CDFVariable cdfv = new CDFVariable(offset, vtypes[vtype]);

                String variableName = cdfv.getName();

                variableNamesAsList.add(variableName);

                if (cdfv.isTypeR()) {
                    this.cdfRVariablesByNumber.put(cdfv.number, cdfv);
                } else {
                    this.cdfZVariablesByNumber.put(cdfv.number, cdfv);
                }

                this.cdfVariablesByName.put(variableName, cdfv);

                if (next == 0) {
                    break;
                }

                offset = next;
                _buf = getRecord(offset);
            }

        }

        this.variableNames = variableNamesAsList.toArray(String[]::new);

        LOGGER.exiting("CDFImpl", "variables");
        return this.cdfVariablesByName;
    }

    /**
     * returns name to Attribute object map
     */
    Map<String, CDFAttribute> attributes() {

        if (this.cdfAttributesByName != null) {
            return this.cdfAttributesByName;
        }

        this.cdfAttributesByName = new HashMap<>();

        LOGGER.entering("CDFImpl", "attributes");

        long offset = this.ADRHead;

        if (offset == 0) {
            return null;
        }

        ByteBuffer _buf = getRecord(offset);

        while (true) {

            _buf.position(this.offset_NEXT_ADR);

            long next = longInt(_buf);

            CDFAttribute cdfa = new CDFAttribute(offset);

            String attributeName = cdfa.getName();

            if ((this.cdfAttributesByName.put(attributeName, cdfa)) != null) {
                LOGGER.log(Level.WARNING, "Duplicate attribute: {0}", attributeName);
            }

            if (next == 0) {
                break;
            }

            offset = next;
            _buf = getRecord(offset);
        }

        LOGGER.exiting("CDFImpl", "attributes");
        return this.cdfAttributesByName;
    }

    /**
     * returns Variable object associated with a given type at a given number
     */
    Variable getCDFVariable(final String vtype, final int number) {

        if (vtype.charAt(0) == 'z') {
            CDFVariable var = this.cdfZVariablesByNumber.get(number);
            return var;
        } else {
            CDFVariable var = this.cdfRVariablesByNumber.get(number);
            return var;
        }

    }

    Object getFillValue(final VariableMetaData variableMetaData) {

        @SuppressWarnings("unchecked")
        List<Object> fill = (List<Object>) getAttribute(variableMetaData.getName(), "FILLVAL");

        int type = variableMetaData.getType();

        if (!fill.isEmpty()) {

            if (fill.get(0)
                    .getClass()
                    .getComponentType() == Double.TYPE) {
                double dfill = ((double[]) fill.get(0))[0];

                if (DataTypes.typeCategory[type] == DataTypes.LONG) {
                    return new long[] { 0L, (long) dfill };
                }

                return new double[] { 0, dfill };
            }

            long lfill = ((long[]) fill.get(0))[0];

            if (DataTypes.typeCategory[type] == DataTypes.LONG) {
                return new long[] { 0L, lfill };
            }

            return new double[] { 0, lfill };
        }

        if (DataTypes.typeCategory[type] == DataTypes.LONG) {
            return new long[] { Long.MIN_VALUE, 0L };
        }

        return new double[] { Double.NEGATIVE_INFINITY, 0 };
    }

    Object getPadValue(final VariableMetaData variableMetaData) {
        return variableMetaData.getPadValue(true);
    }

    DoubleVarContainer getRangeBuffer(final String variableName, final int first, final int last) {
        Variable variable = getVariable(variableName);

        if (variable == null) {
            throw new IllegalArgumentException("No such variable: " + variableName);
        }

        if (DataTypes.isStringType(variable.getType())) {
            throw new IllegalArgumentException("Function not supported for string variables, " + variable.getType());
        }

        int[] range = { first, last };
        DoubleVarContainer dbuf = new DoubleVarContainer(this, variable, range, false, ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf;
    }

    /**
     * returns ByteBuffer containing count values for variable variable starting at
     * CDF offset value offset.
     */
    ByteBuffer positionBuffer(final VariableMetaData variableMetaData, final long offset, final int count) {
        ByteBuffer bv;

        if (!variableMetaData.isCompressed()) {
            bv = getValueBuffer(offset);
        } else {
            int size = variableMetaData.getDataItemSize();
            bv = getValueBuffer(offset, size, count);
        }

        bv.order(getByteOrder());
        return bv;
    }

    /**
     * AttributeEntry class.
     */
    @ToString(exclude = "_buf")
    public class CDFAttributeEntry implements AttributeEntry, Serializable {

        private static final long serialVersionUID = 3507692892215505019L;

        transient ByteBuffer _buf;

        final int variableNumber;

        final int type;

        final int nelement;

        final String attribute;

        final String stringValue;

        final String[] stringValues;

        final Object value;

        /**
         * Instantiates a new CDF attribute entry.
         *
         * @param buf  the buf
         * @param name the name
         */
        public CDFAttributeEntry(final ByteBuffer buf, final String name) {
            this.attribute = name;
            this._buf = buf.duplicate();
            this._buf.position(CDFImpl.this.offset_ENTRYNUM);
            this.variableNumber = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_ATTR_DATATYPE);
            this.type = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_ATTR_NUM_ELEMENTS);
            this.nelement = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_VALUE);

            if (this.type > 50) {
                byte[] ba = new byte[this.nelement];
                int i = 0;

                for (; i < this.nelement; i++) {
                    ba[i] = this._buf.get();

                    if (ba[i] == 0) {
                        break;
                    }

                }

                this.stringValue = new String(ba, 0, i);
                this._buf.position(CDFImpl.this.offset_ATTR_NUM_ELEMENTS + 4);
                int numStrings = this._buf.getInt();

                if (numStrings > 1) {
                    this.stringValues = new String[numStrings];
                    int lastIndex = 0;
                    int begin = 0;
                    int count = 0;

                    while ((lastIndex = this.stringValue.indexOf(STRINGDELIMITER, begin)) != -1) {
                        this.stringValues[count] = this.stringValue.substring(begin, lastIndex);
                        begin += this.stringValues[count].length() + STRINGDELIMITER.length();
                        count++;
                    }

                    this.stringValues[count] = this.stringValue.substring(begin);
                } else {
                    this.stringValues = null;
                }

                this.value = null;

            } else {
                this.stringValues = null;
                this.stringValue = null;
                this.value = getNumberAttribute(this.type, this.nelement, this._buf, CDFImpl.this.byteOrder);
            }

        }

        @Override
        public String getAttributeName() {
            return this.attribute;
        }

        @Override
        public int getNumberOfElements() {
            return this.nelement;
        }

        @Override
        public int getType() {
            return this.type;
        }

        @Override
        public Object getValue() {
            return (isStringType()) ? ((this.stringValues != null) ? this.stringValues : this.stringValue) : this.value;
        }

        @Override
        public int getVariableNumber() {
            return this.variableNumber;
        }

        @Override
        public boolean isLongType() {
            return (DataTypes.typeCategory[this.type] == DataTypes.LONG);
        }

        @Override
        public boolean isSameAs(final AttributeEntry ae) {

            if (getType() != ae.getType()) {
                return false;
            }

            if (getNumberOfElements() != ae.getNumberOfElements()) {
                return false;
            }

            if (isStringType() != ae.isStringType()) {
                return false;
            }

            if (isStringType()) {

                if (this.stringValues != null) {
                    Object newValue = ae.getValue();

                    if (!newValue.getClass()
                            .isArray()) {
                        return false;
                    }

                    String[] newStrings = (String[]) newValue;
                    return Arrays.equals(this.stringValues, newStrings);
                }

                return (this.stringValue.equals(ae.getValue()));
            }

            if (isLongType() != ae.isLongType()) {
                return false;
            }

            if (isLongType()) {
                return Arrays.equals((long[]) this.value, (long[]) ae.getValue());
            }

            return Arrays.equals((double[]) this.value, (double[]) ae.getValue());
        }

        @Override
        public boolean isStringType() {
            return DataTypes.isStringType(this.type);
        }
    }

    /**
     * CDFVariable class.
     */
    @ToString
    public class CDFVariable implements java.io.Serializable, Variable {

        private static final long serialVersionUID = 1512547111444704626L;

        private final int DIMENSION_VARIES = -1;

        final List<AttributeEntry> attributes = new CopyOnWriteArrayList<>();

        private final String name;

        private final int number;

        private final String vtype;

        private final int flags;

        private final int sRecords;

        private final int type;

        private final int numberOfElements;

        private final int numberOfValues;

        private int[] dimensions;

        private boolean[] varies;

        private Object padValue;

        private boolean completed = false;

        private final transient ByteBuffer _buf;

        private int dataItemSize;

        private final int blockingFactor;

        private DataLocator locator;

        private boolean recordGap = false;

        /**
         * Instantiates a new CDF variable.
         *
         * @param offset the offset
         * @param vtype  the vtype
         */
        public CDFVariable(final long offset, final String vtype) {
            this.vtype = vtype;
            this._buf = getRecord(offset);
            this.name = getString(offset + CDFImpl.this.offset_VAR_NAME);
            this._buf.position(CDFImpl.this.offset_VAR_NUM_ELEMENTS);
            this.numberOfElements = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_NUM);
            this.number = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_FLAGS);
            this.flags = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_sRecords);
            this.sRecords = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_BLOCKING_FACTOR);
            this.blockingFactor = this._buf.getInt();
            this._buf.position(CDFImpl.this.offset_VAR_DATATYPE);
            this.type = this._buf.getInt();
            this.numberOfValues = this._buf.getInt() + 1;
            this._buf.position(CDFImpl.this.offset_zNumDims);

            if ("r".equals(vtype)) {
                this.dimensions = CDFImpl.this.rDimSizes;
            }

            if ("z".equals(vtype)) {
                this.dimensions = new int[this._buf.getInt()];

                for (int i = 0; i < this.dimensions.length; i++) {
                    this.dimensions[i] = this._buf.getInt();
                }

            }

            this.varies = new boolean[this.dimensions.length];

            for (int i = 0; i < this.dimensions.length; i++) {
                this.varies[i] = (this._buf.getInt() == this.DIMENSION_VARIES);
            }

            if (this.type == DataTypes.EPOCH16) {
                this.dimensions = new int[] { 2 };
            }

            if (this.type == DataTypes.EPOCH16) {
                this.varies = new boolean[] { true };
            }

            this.dataItemSize = DataTypes.size[this.type];
            // PadValue immediately follows DimVarys
            this.padValue = null;
            int padValueSize = getDataItemSize() / this.dataItemSize;
            Object _padValue = DataTypes.defaultPad(this.type);

            if (DataTypes.isStringType(this.type)) {
                byte[] ba = new byte[this.numberOfElements];

                if (padValueSpecified()) {
                    this._buf.get(ba);

                    for (int i = 0; i < this.numberOfElements; i++) {

                        if (ba[i] <= 0) {
                            ba[i] = 0x20;
                        }

                    }

                } else {

                    for (int i = 0; i < this.numberOfElements; i++) {
                        ba[i] = ((Byte) _padValue);
                    }

                }

                _padValue = new String(ba);
                String[] sa = new String[padValueSize];

                for (int i = 0; i < padValueSize; i++) {
                    sa[i] = (String) _padValue;
                }

                this.padValue = sa;
            } else {

                if (padValueSpecified()) {
                    _padValue = getNumberAttribute(this.type, 1, this._buf, CDFImpl.this.byteOrder);
                }

                if (DataTypes.isLongType(this.type)) {
                    long[] lpad = new long[padValueSize];

                    if (padValueSpecified()) {
                        lpad[0] = ((long[]) _padValue)[0];
                    } else {
                        lpad[0] = ((Long) _padValue);
                    }

                    for (int i = 1; i < padValueSize; i++) {
                        lpad[i] = lpad[0];
                    }

                    this.padValue = lpad;
                } else {
                    double[] dpad = new double[padValueSize];

                    if (padValueSpecified()) {
                        dpad[0] = ((double[]) _padValue)[0];
                    } else {
                        dpad[0] = ((Double) _padValue);
                    }

                    for (int i = 1; i < padValueSize; i++) {
                        dpad[i] = dpad[0];
                    }

                    this.padValue = dpad;
                }

            }

            // ignore numberOfElements for numeric data types
            if (DataTypes.isStringType(this.type)) {
                this.dataItemSize *= this.numberOfElements;
            }

        }

        /**
         * Returns this variable's values as byte[] if variable type is byte,
         * unsigned byte or char. Otherwise,
         */
        @Override
        public byte[] asByteArray() {
            return asByteArray(null);
        }

        /**
         * Returns this variable's values for a range of records as byte[] if
         * variable type is byte, unsigned byte or char. Otherwise, throws
         *
         * @param pt the pt
         *
         * @return the byte[]
         */
        @Override
        public byte[] asByteArray(final int[] pt) {

            if (ByteVarContainer.isCompatible(this.type, true)) {

                VDataContainer.CByte container = new ByteVarContainer(CDFImpl.this, this, pt);
                container.run();
                return container.as1DArray();
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return a byte[].");
        }

        /**
         * As byte array.
         *
         * @param pt          the pt
         * @param columnMajor the column major
         *
         * @return the byte[]
         */
        public byte[] asByteArray(final int[] pt, final boolean columnMajor) {

            if (ByteVarContainer.isCompatible(this.type, true)) {
                VDataContainer.CByte container = new ByteVarContainer(CDFImpl.this, this, pt);
                container.run();
                return container.asOneDArray(columnMajor);
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return a byte[].");
        }

        /**
         * Returns this variable's values as double[].
         * If variable type cannot be cast to double, a Throwable is thrown.
         */
        @Override
        public double[] asDoubleArray() {
            return asDoubleArray(false, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * double[].
         * If variable type cannot be cast to double, a Throwable is thrown.
         * If preserve is true, a Throwable is thrown for variables of type long
         * to signal possible loss of precision.
         */
        @Override
        public double[] asDoubleArray(final boolean preserve, final int[] pt) {
            TargetAttribute ta = new TargetAttribute(preserve, false);
            return asDoubleArray(ta, pt);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * double[].
         * If variable type cannot be cast to double, a Throwable is thrown.
         */
        @Override
        public double[] asDoubleArray(final int[] pt) {
            return asDoubleArray(false, pt);
        }

        /**
         * As double array.
         *
         * @param targetAttribute the target attribute
         * @param pt              the pt
         *
         * @return the double[]
         */
        public double[] asDoubleArray(final TargetAttribute targetAttribute, final int[] pt) {

            try {
                VDataContainer.CDouble container = getDoubleContainer(pt, targetAttribute.preserve,
                        ByteOrder.nativeOrder());
                container.run();
                return container.asOneDArray(targetAttribute.columnMajor);
            } catch (RuntimeException e) {
                throw new UnsupportedOperationException("Variable " + getName() + " cannot return a double[].", e);
            }

        }

        /**
         * Returns this variable's values as float[].
         * If variable type cannot be cast to float, a Throwable is thrown.
         */
        @Override
        public float[] asFloatArray() {
            return asFloatArray(false, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * float[].
         * If variable type cannot be cast to float, a Throwable is thrown.
         * If preserve is true, a Throwable is thrown for variables of type
         * double, long or int to signal possible loss of precision.
         *
         * @param preserve the preserve
         * @param pt       the pt
         *
         * @return the float[]
         */
        @Override
        public float[] asFloatArray(final boolean preserve, final int[] pt) {

            VDataContainer.CFloat container = getFloatContainer(pt, preserve, ByteOrder.nativeOrder());
            container.run();
            return container.as1DArray();

        }

        /**
         * Returns this variable's values for the specified range of records as
         * float[].
         * If variable type cannot be cast to float, a Throwable is thrown.
         */
        @Override
        public float[] asFloatArray(final int[] pt) {
            return asFloatArray(false, pt);
        }

        /**
         * Returns this variable's values as int[] for variables of type
         * int, short or unsigned short, byte or unsigned byte.
         * for variables of other types.
         */
        @Override
        public int[] asIntArray() {
            return asIntArray(true, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * int[] for variables of type int, short or unsigned short, byte or
         * unsigned byte, or unsigned int (only if preserve is false).
         * for variables of other types.
         */
        @Override
        public int[] asIntArray(final boolean preserve, final int[] pt) {
            VDataContainer.CInt container = getIntContainer(pt, preserve, ByteOrder.nativeOrder());
            container.run();
            return container.as1DArray();
        }

        /**
         * Returns this variable's values for the specified range of records as
         * int[] for variables of type int, short or unsigned short, byte or
         * unsigned byte.
         * for variables of other types.
         */
        @Override
        public int[] asIntArray(final int[] pt) {
            return asIntArray(true, pt);
        }

        /**
         * Returns this variable's values as long[] for variables of type long.
         * for variables of other types.
         */
        @Override
        public long[] asLongArray() {
            return asLongArray(null);
        }

        /**
         * Returns this variable's values as long[] for variables of type long.
         * for variables of other types.
         */
        @Override
        public short[] asShortArray() {
            return asShortArray(true, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * short[] for variables of type short, byte or unsigned byte, or
         * unsigned short (only if preserve is false).
         * for variables of other types.
         */
        @Override
        public short[] asShortArray(final boolean preserve, final int[] pt) {
            VDataContainer.CShort container = getShortContainer(pt, preserve, ByteOrder.nativeOrder());
            container.run();
            return container.as1DArray();
        }

        /**
         * Returns this variable's values for the specified range of records as
         * short[] for variables of type short, byte or unsigned byte.
         * for variables of other types.
         */
        @Override
        public short[] asShortArray(final int[] pt) {
            return asShortArray(true, pt);
        }

        /**
         * Gets the attributes.
         *
         * @return the attributes
         */
        public final List<AttributeEntry> getAttributes() {
            return this.attributes;
        }

        /**
         * returns blocking factor used in compression
         */
        @Override
        public int getBlockingFactor() {
            return this.blockingFactor;
        }

        /**
         * Gets the buffer.
         *
         * @return the buffer
         */
        public ByteBuffer getBuffer() {
            return getBuffer(Double.TYPE, null, false, ByteOrder.nativeOrder());
        }

        /**
         * Returns ByteBuffer containing uncompressed values converted to
         * a stream of numbers of the type specified by 'type' using the
         * specified byte ordering (specified by bo) for the specified range
         * of records. Original ordering of values (row majority) is preserved.
         * recordRange[0] specifies the first record, and recordRange[1] the
         * last record. If 'preserve' is true, a Throwable is thrown if the
         * conversion to specified type will result in loss of precision.
         * If 'preserve' is * false, compatible conversions will be made even
         * if it results in loss of precision.
         */
        @Override
        public ByteBuffer getBuffer(final Class<?> cl, final int[] recordRange, final boolean preserve,
                final ByteOrder bo) {

            if (!this.completed) {
                complete();
            }

            if (cl == Byte.TYPE) {
                VDataContainer container = new ByteVarContainer(CDFImpl.this, this, recordRange);
                container.run();
                return container.getBuffer();
            }

            if (cl == Double.TYPE) {

                if (DoubleVarContainer.isCompatible(this.type, preserve)) {
                    VDataContainer container = new DoubleVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Float.TYPE) {

                if (FloatVarContainer.isCompatible(this.type, preserve)) {
                    VDataContainer container = new FloatVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Integer.TYPE) {

                if (IntVarContainer.isCompatible(this.type, preserve)) {
                    VDataContainer container = new IntVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Short.TYPE) {

                if (ShortVarContainer.isCompatible(this.type, preserve)) {
                    VDataContainer container = new ShortVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Long.TYPE) {

                if (LongVarContainer.isCompatible(this.type, preserve)) {
                    VDataContainer container = new LongVarContainer(CDFImpl.this, this, recordRange, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            throw new IllegalStateException("Inconsistent constraints for this variable");
        }

        /**
         * Gets the buffer.
         *
         * @param recordRange the record range
         *
         * @return the buffer
         */
        public ByteBuffer getBuffer(final int[] recordRange) {
            return getBuffer(Double.TYPE, recordRange, false, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CByte getByteContainer(final int[] pt) {

            if (ByteVarContainer.isCompatible(this.type, true)) {
                return new ByteVarContainer(CDFImpl.this, this, pt);
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.CByte.");
        }

        @Override
        public CDFImpl getCDF() {
            return CDFImpl.this;
        }

        @Override
        public VariableDataBuffer[] getDataBuffers() {
            return getDataBuffers(false);
        }

        /**
         * Gets an array of VariableDataBuffer objects that provide location of
         * data for this variable if this variable is not compressed.
         * This method throws a Throwable if invoked for a compressed variable.
         * getBuffer method of VariableDataBuffer object returns a read only
         * ByteBuffer that contains data for this variable for a range of
         * records. getFirstRecord() and getLastRecord() define the
         * range of records.
         */
        @Override
        public VariableDataBuffer[] getDataBuffers(final boolean raw) {

            if (!this.completed) {
                complete();
            }

            if (!raw) {

                if ((this.flags & 4) != 0) {
                    throw new UnsupportedOperationException("Function not supported for compressed variables ");
                }

            }

            long[][] locations = this.locator.getLocations();
            Collection<VariableDataBuffer> variableDataBUffers = new ArrayList<>();
            int size = getDataItemSize();

            for (long[] location : locations) {
                int first = (int) location[0];
                int last = (int) location[1];
                ByteBuffer bv = getRecord(location[2]);
                int clen = ((last - first) + 1) * size;
                // System.out.println("uclen: " + clen);
                boolean compressed = false;

                if (!isCompressed() || (bv.getInt(CDFImpl.this.offset_RECORD_TYPE) == VVR_RECORD_TYPE)) {
                    bv.position(CDFImpl.this.offset_RECORDS);
                } else {
                    compressed = true;
                    bv.position(CDFImpl.this.offset_CDATA);
                    clen = lowOrderInt(bv, CDFImpl.this.offset_CSIZE);
                    // System.out.println("clen: " + clen);
                }

                ByteBuffer bbuf = bv.slice();
                bbuf.order(getByteOrder());
                bbuf.limit(clen);
                variableDataBUffers.add(new VariableDataBuffer(first, last, bbuf, compressed));
            }

            return variableDataBUffers.toArray(VariableDataBuffer[]::new);
        }

        /**
         * returns size of value of this variable
         */
        @Override
        public int getDataItemSize() {
            int size = this.dataItemSize;

            for (int i = 0; i < this.dimensions.length; i++) {

                if (this.varies[i]) {
                    size *= this.dimensions[i];
                }

            }

            return size;
        }

        @Override
        public int[] getDimensions() {
            int[] ia = new int[this.dimensions.length];
            System.arraycopy(this.dimensions, 0, ia, 0, this.dimensions.length);
            return ia;
        }

        @Override
        public VDataContainer.CDouble getDoubleContainer(final int[] pt, final boolean preserve) {
            return getDoubleContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CDouble getDoubleContainer(final int[] pt, final boolean preserve, final ByteOrder bo) {

            if (DoubleVarContainer.isCompatible(this.type, preserve)) {
                return new DoubleVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.CDouble.");
        }

        /**
         * Returns effective dimensions
         */
        @Override
        public int[] getEffectiveDimensions() {
            int rank = getEffectiveRank();

            if (rank == 0) {
                return new int[0];
            }

            int[] edim = new int[rank];
            int n = 0;

            for (int i = 0; i < this.dimensions.length; i++) {

                if (!this.varies[i]) {
                    continue;
                }

                if (this.dimensions[i] == 1) {
                    continue;
                }

                edim[n++] = this.dimensions[i];
            }

            return edim;
        }

        /**
         * returns effective rank
         */
        @Override
        public int getEffectiveRank() {

            int rank = 0;

            for (int i = 0; i < this.dimensions.length; i++) {

                if (!this.varies[i]) {
                    continue;
                }

                if (this.dimensions[i] == 1) {
                    continue;
                }

                rank++;
            }

            return rank;
        }

        @Deprecated
        @Override
        public Vector<Integer> getElementCount() {

            return Arrays.stream(getDimensions())
                    .mapToObj(Integer::valueOf)
                    .collect(Collectors.toCollection(Vector::new));
        }

        @Override
        public VDataContainer.CFloat getFloatContainer(final int[] pt, final boolean preserve) {
            return getFloatContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CFloat getFloatContainer(final int[] pt, final boolean preserve, final ByteOrder bo) {

            if (FloatVarContainer.isCompatible(this.type, preserve)) {
                return new FloatVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.Float.");
        }

        @Override
        public VDataContainer.CInt getIntContainer(final int[] pt, final boolean preserve) {
            return getIntContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CInt getIntContainer(final int[] pt, final boolean preserve, final ByteOrder bo) {

            if (IntVarContainer.isCompatible(this.type, preserve)) {
                return new IntVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.CInt.");
        }

        /**
         * Gets a list of regions that contain data for the variable.
         * Each element of the CopyOnWriteArrayList describes a region as an int[3]
         * array.
         * Array elements are: record number of first point
         * in the region, record number of last point in the
         * region, and offset of the start of region.
         */
        @Override
        public VariableDataLocator getLocator() {

            if (!this.completed) {
                complete();
            }

            return this.locator;
        }

        @Override
        public VDataContainer.CLong getLongContainer(final int[] pt) {
            return getLongContainer(pt, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CLong getLongContainer(final int[] pt, final ByteOrder bo) {

            if (LongVarContainer.isCompatible(this.type, true)) {
                return new LongVarContainer(CDFImpl.this, this, pt, ByteOrder.nativeOrder());
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.CLong.");
        }

        /**
         * Gets the name of this of this variable
         */
        @Override
        public String getName() {
            return this.name;
        }

        /**
         * Gets the sequence number of the variable inside the CDF.
         */
        @Override
        public int getNumber() {
            return this.number;
        }

        /**
         * returns number of elements in the value of this variable
         */
        @Override
        public int getNumberOfElements() {
            return this.numberOfElements;
        }

        /**
         * returns number of values
         */
        @Override
        public int getNumberOfValues() {
            return this.numberOfValues;
        }

        /**
         * returns pad value
         */
        @Override
        public Object getPadValue() {

            if (this.padValue == null) {
                return null;
            }

            if (DataTypes.isStringType(this.type)) {
                return this.padValue;
            }

            return getPadValue(false);
        }

        /**
         * Gets an object that represents a padded instance for a variable of
         * numeric type.
         * A double[] is returned, unless the variable type is long and
         * preservePrecision is set to true;
         */
        @Override
        public Object getPadValue(final boolean preservePrecision) {

            if (this.padValue == null) {
                return null;
            }

            if (DataTypes.isStringType(this.type)) {
                return this.padValue;
            }

            if (this.padValue.getClass()
                    .getComponentType() == Long.TYPE) {
                long[] ltemp = (long[]) this.padValue;

                if (preservePrecision) {
                    long[] la = new long[ltemp.length];
                    System.arraycopy(ltemp, 0, la, 0, ltemp.length);
                    return la;
                }

                double[] dtemp = new double[ltemp.length];

                for (int i = 0; i < ltemp.length; i++) {
                    dtemp[i] = ltemp[i];
                }

                return dtemp;
            }

            double[] dtemp = (double[]) this.padValue;
            double[] da = new double[dtemp.length];
            System.arraycopy(dtemp, 0, da, 0, dtemp.length);
            return da;
        }

        /**
         * Returns record range for this variable
         */
        @Override
        public int[] getRecordRange() {

            if (!this.completed) {
                complete();
            }

            if (this.locator == null) {
                return null;
            }

            long[][] locations = this.locator.getLocations();
            return new int[] { (int) locations[0][0], (int) locations[locations.length - 1][1] };
        }

        @Override
        public VDataContainer.CShort getShortContainer(final int[] pt, final boolean preserve) {
            return getShortContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CShort getShortContainer(final int[] pt, final boolean preserve, final ByteOrder bo) {

            if (ShortVarContainer.isCompatible(this.type, preserve)) {
                return new ShortVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.CShort.");
        }

        @Override
        public VDataContainer.CString getStringContainer(final int[] pt) {

            if (StringVarContainer.isCompatible(this.type, true)) {
                return new StringVarContainer(CDFImpl.this, this, pt);
            }

            throw new UnsupportedOperationException("Variable " + getName() + " cannot return VDataContainer.CString.");
        }

        /**
         * returns type of values of this variable
         */
        @Override
        public int getType() {
            return this.type;
        }

        /**
         * Gets the variable type.
         *
         * @return the variable type
         */
        public final String getVariableType() {
            return this.vtype;
        }

        /**
         * Gets the dimensional variance. This determines the effective
         * dimensionality of values of the variable.
         */
        @Override
        public boolean[] getVarys() {

            boolean[] ba = new boolean[this.varies.length];

            System.arraycopy(this.varies, 0, ba, 0, this.varies.length);

            return ba;
        }

        /**
         * returns whether conversion of this variable to type specified by
         * cl is supported while preserving precision.
         * equivalent to isCompatible(Class cl, true)
         */
        @Override
        public boolean isCompatible(final Class<?> cl) {
            return BaseVarContainer.isCompatible(getType(), true, cl);
        }

        /**
         * returns whether conversion of this variable to type specified by
         * cl is supported under the given precision preserving constraint.
         */
        @Override
        public boolean isCompatible(final Class<?> cl, final boolean preserve) {
            return BaseVarContainer.isCompatible(getType(), preserve, cl);
        }

        /**
         * returns whether variable values have been compressed
         */
        @Override
        public boolean isCompressed() {

            if (!this.completed) {
                complete();
            }

            return (this.locator != null) && this.locator.isReallyCompressed();
        }

        /**
         * Shows whether one or more records (in the range returned by
         * getRecordRange()) are missing.
         */
        @Override
        public boolean isMissingRecords() {

            if (!this.completed) {
                complete();
            }

            return this.recordGap;
        }

        @Override
        public boolean isTypeR() {
            return ("r".equals(this.vtype));
        }

        /**
         * Return whether the missing record should be assigned the pad
         * value.
         */
        @Override
        public boolean missingRecordValueIsPad() {
            return (this.sRecords == 1);
        }

        /**
         * Return whether the missing record should be assigned the last
         * seen value. If none has been seen, pad value is assigned.
         */
        @Override
        public boolean missingRecordValueIsPrevious() {
            return (this.sRecords == 2);
        }

        /**
         * returns whether pad value is specified for this variable.
         *
         * @return true, if successful
         */
        public boolean padValueSpecified() {
            return ((this.flags & 2) != 0);
        }

        /**
         * returns whether value of this variable can vary from record to record
         */
        @Override
        public boolean recordVariance() {
            return ((this.flags & 1) != 0);
        }

        /**
         * returns whether row major ordering is in use
         */
        @Override
        public boolean rowMajority() {
            return CDFImpl.this.rowMajority();
        }

        /**
         * Returns this variable's values for the specified range of records as
         * long[] for variables of type long.
         * for variables of other types.
         */
        @Override
        public long[] asLongArray(final int[] pt) {

            VDataContainer.CLong container = getLongContainer(pt, ByteOrder.nativeOrder());
            container.run();

            return container.as1DArray();
        }

        void checkContinuity() {

            if (this.numberOfValues == 0) {
                return;
            }

            long[][] locations = this.locator.getLocations();
            long last = locations[0][0] - 1;

            for (long[] location : locations) {

                if (location[0] != (last + 1)) {
                    this.recordGap = true;
                    break;
                }

                last = location[1];
            }

            if (this.recordGap) {

                if (this.sRecords == 0) {
                    System.out.println("Variable " + this.name + " is missing "
                            + "records. This is not consistent with sRecords = 0");
                }

            }

        }

        synchronized void complete() {

            if (this.completed) {
                return;
            }

            if (this.numberOfValues > 0) {
                this.locator = new DataLocator(this._buf, this.numberOfValues, ((this.flags & 4) != 0));
                checkContinuity();
            }

            this.completed = true;
        }

        boolean isComplete() {
            return this.completed;
        }
    }

    /**
     * DataLocator.
     */
    public class DataLocator implements VariableDataLocator, java.io.Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 713466264682521709L;

        private final int numberOfValues;

        private final boolean compressed;

        /** The locations. */
        protected CopyOnWriteArrayList<long[]> locations = new CopyOnWriteArrayList<>();

        /**
         * Instantiates a new data locator.
         *
         * @param b     the b
         * @param n     the n
         * @param compr the compr
         */
        protected DataLocator(final ByteBuffer b, final int n, final boolean compr) {
            this.numberOfValues = n;
            this.compressed = compr;
            b.position(CDFImpl.this.offset_FIRST_VXR);
            long offset = longInt(b);
            ByteBuffer bx = getRecord(offset);
            List<long[]> locs = _getLocations(bx);
            registerNodes(locs);
        }

        @Override
        public long[][] getLocations() {
            long[][] loc = new long[this.locations.size()][3];

            for (int i = 0; i < this.locations.size(); i++) {
                long[] ia = this.locations.get(i);
                loc[i][0] = ia[0];
                loc[i][1] = ia[1];
                loc[i][2] = ia[2];
            }

            return loc;
        }

        /**
         * Gets the locations as list.
         *
         * @return the locations as as unmodifiable list
         */
        public List<long[]> getLocationsAsList() {

            return Collections.unmodifiableList(Arrays.stream(getLocations())
                    .collect(Collectors.toList()));

        }

        /**
         * Gets the locations as vector.
         *
         * @return the locations as vector
         *
         * @deprecated use {@link #getLocationsAsList()}
         */
        @Deprecated
        public Vector<long[]> getLocationsAsVector() {
            Vector<long[]> _locations = new Vector<>();
            long[][] loc = getLocations();

            for (int i = 0; i < this.locations.size(); i++) {
                _locations.add(loc[i]);
            }

            return _locations;
        }

        /**
         * Checks if is really compressed.
         *
         * @return true, if is really compressed
         */
        public boolean isReallyCompressed() {
            return this.compressed;
        }

        List<long[]> _getLocations(ByteBuffer bx) {

            List<long[]> locs = new CopyOnWriteArrayList<>();

            while (true) {
                bx.position(CDFImpl.this.offset_NEXT_VXR);
                long next = longInt(bx);
                bx.position(CDFImpl.this.offset_NENTRIES);
                int nentries = bx.getInt();
                bx.position(CDFImpl.this.offset_NUSED);
                int nused = bx.getInt();
                bx.position(CDFImpl.this.offset_FIRST);
                ByteBuffer bf = bx.slice();
                bx.position(CDFImpl.this.offset_FIRST + (nentries * 4));
                ByteBuffer bl = bx.slice();
                bx.position(CDFImpl.this.offset_FIRST + (2 * nentries * 4));
                ByteBuffer bo = bx.slice();

                for (int entry = 0; entry < nused; entry++) {
                    long first = bf.getInt();
                    long last = bl.getInt();

                    if (last > (this.numberOfValues - 1)) {
                        last = (this.numberOfValues - 1);
                    }

                    long off = longInt(bo);
                    locs.add(new long[] { first, last, off });
                }

                if (next == 0) {
                    break;
                }

                bx = getRecord(next);
            }

            return locs;
        }

        void registerNodes(final Iterable<long[]> locs) {

            for (long[] loc : locs) {

                ByteBuffer bb = getRecord(loc[2]);

                if (bb.getInt(CDFImpl.this.offset_RECORD_TYPE) == VXR_RECORD_TYPE) {

                    List<long[]> locs2 = _getLocations(bb);

                    registerNodes(locs2);

                } else {
                    this.locations.add(loc);
                }

            }

        }
    }

    /**
     * CDFAttribute class
     */
    @ToString
    class CDFAttribute implements java.io.Serializable, Attribute {

        private static final long serialVersionUID = -2085580948842565139L;

        final String name;

        final int scope;

        final int num;

        final List<AttributeEntry> zEntries;

        final List<AttributeEntry> gEntries;

        CDFAttribute(final long offset) {

            this.name = getString(offset + CDFImpl.this.offset_ATTR_NAME);

            LOGGER.log(Level.FINER, "new attribute {0} at {1}", new Object[] { this.name, offset });

            ByteBuffer _buf = getRecord(offset);

            _buf.position(CDFImpl.this.offset_SCOPE);

            this.scope = _buf.getInt();

            this.num = _buf.getInt();

            _buf.position(CDFImpl.this.offset_AgrEDRHead);

            long n = longInt(_buf);

            if (n > 0) {
                this.gEntries = attributeEntries(n);
                LOGGER.log(Level.FINEST, "link attr {0} to {1} gEntries",
                        new Object[] { this.name, this.gEntries.size() });

                if ((this.scope == 2) || (this.scope == 4)) { // variable scope
                    linkToVariables(this.gEntries, "r");
                }

            } else {
                this.gEntries = Collections.emptyList();
            }

            _buf.position(CDFImpl.this.offset_AzEDRHead);
            n = longInt(_buf);

            if (n > 0) {
                this.zEntries = attributeEntries(n);
                LOGGER.log(Level.FINEST, "link attr {0} to {1} zEntries",
                        new Object[] { this.name, this.zEntries.size() });
                linkToVariables(this.zEntries, "z");
            } else {
                this.zEntries = Collections.emptyList();
            }

        }

        /**
         * Attribute entries.
         *
         * @param offset the offset
         *
         * @return an unmodifiable list of entries or null if offset==0
         */
        public List<AttributeEntry> attributeEntries(final long offset) {

            if (offset == 0) {
                return null;
            }

            List<AttributeEntry> list = new ArrayList<>();

            ByteBuffer _buf = getRecord(offset);

            while (true) {

                _buf.position(CDFImpl.this.offset_NEXT_AEDR);

                long next = longInt(_buf);

                _buf.position(0);

                AttributeEntry ae = new CDFAttributeEntry(_buf, this.name);

                list.add(ae);

                if (next == 0) {
                    break;
                }

                _buf = getRecord(next);
            }

            return Collections.unmodifiableList(list);
        }

        /**
         * returns attribute entries
         *
         * @deprecated use {@link #attributeEntries(long)}
         */
        @SuppressWarnings("rawtypes")
        @Deprecated
        public Vector getAttributeEntries(final long offset) {
            List<AttributeEntry> attributeEntries = attributeEntries(offset);

            return (attributeEntries == null) ? null : new Vector<>(attributeEntries);
        }

        /**
         * returns name of the attribute
         */
        @Override
        public String getName() {
            return this.name;
        }

        /**
         * is this a global attribute?
         */
        @Override
        public boolean isGlobal() {
            return ((this.scope != 2) && (this.scope != 4));
        }

        /**
         * link variable attribute entries to the appropriate variable
         */
        public void linkToVariables(final Iterable<? extends AttributeEntry> attributeEntries, final String type) {

            for (AttributeEntry attributeEntry : attributeEntries) {

                CDFVariable variable = (CDFVariable) getCDFVariable(type, attributeEntry.getVariableNumber());

                if (variable == null) {

                    LOGGER.log(Level.WARNING,
                            "An attribute entry for {0} of type, {1}, links to variable number {2} that was not found",
                            new Object[] { attributeEntry.getAttributeName(), type,
                                    attributeEntry.getVariableNumber() });

                } else {
                    variable.attributes.add(attributeEntry);
                }

            }

        }
    }

    static class TargetAttribute {

        public final boolean preserve;

        public final boolean columnMajor;

        TargetAttribute(final boolean p, final boolean c) {
            this.preserve = p;
            this.columnMajor = c;
        }
    }

}
