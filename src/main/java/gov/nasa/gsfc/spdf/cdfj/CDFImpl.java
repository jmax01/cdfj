package gov.nasa.gsfc.spdf.cdfj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/* public */ abstract class CDFImpl implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7106016786475171626L;

    /**
     * CDF constants
     */
    public static final int GDR_RECORD = 2;

    public static final int FLAGS_MAJORITY_MASK = 0x01;

    public static final int ROW_MAJOR = 1;

    public static final int VXR_RECORD_TYPE = 6;

    public static final int VVR_RECORD_TYPE = 7;

    public static final int CVVR_RECORD_TYPE = 13;

    public static final String STRINGDELIMITER = "\\N ";

    private static final Logger LOGGER = Logger.getLogger("cdfj.cdfimpl");

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

    long GDROffset;

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

    protected String[] varNames;

    protected Hashtable variableTable;

    private HashMap<Integer, CDFVariable> ivariableTable;

    Hashtable attributeTable;

    protected CDFCore thisCDF;

    protected CDFFactory.CDFSource source;

    protected CDFFactory.ProcessingOption processingOption;

    protected CDFImpl(ByteBuffer buf) {
        this.buf = buf;
    }

    public static Object getNumberAttribute(int type, int nelement, ByteBuffer vbuf, ByteOrder byteOrder) {
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
                    value[i] = (n >= 0 ? n : longInt + n);
                }

            }

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.out.println("getNumberAttribute: " + vbuf);
            System.out.println("type: " + type);
            ex.printStackTrace();
            return null;
        }

        if (longType) {
            return lvalue;
        }

        return value;
    }

    public static TargetAttribute targetAttributeInstance(boolean p, boolean c) {
        return new TargetAttribute(p, c);
    }

    public void extractBytes(int bufOffset, byte[] ba, int offset, int len) {
        this.buf.duplicate().position(bufOffset).get(ba, offset, len);
    }

    public Object get(String varName) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (DataTypes.isStringType(var.getType())) {
            VDataContainer.CString container = var.getStringContainer(null);
            container.run();
            StringArray sa = (StringArray) container.asArray();
            return sa.array();
        }

        VDataContainer.CDouble container = var.getDoubleContainer(null, false);
        container.run();
        DoubleArray da = container.asArray();
        return da.array();
    }

    public Object get(String varName, int element) throws Throwable {
        return get(varName, new int[] { element });
    }

    public Object get(String varName, int index0, int index1) throws Throwable {
        DoubleVarContainer dbuf;
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (DataTypes.isStringType(var.getType())) {
            throw new Throwable("Function not supported for string variables");
        }

        dbuf = new DoubleVarContainer(this, var, null, false, ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf.asArrayElement(index0, index1);
    }

    public Object get(String varName, int first, int last, int element) throws Throwable {
        return get(varName, first, last, new int[] { element });
    }

    public Object get(String varName, int first, int last, int[] elements) throws Throwable {
        DoubleVarContainer dbuf = getRangeBuffer(varName, first, last);
        return dbuf.asArrayElement(elements);
    }

    public Object get(String varName, int[] elements) throws Throwable {
        DoubleVarContainer dbuf;
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (DataTypes.isStringType(var.getType())) {
            throw new Throwable("Function not supported for string variables");
        }

        dbuf = new DoubleVarContainer(this, var, null, false, ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf.asArrayElement(elements);
    }

    public double[] get1D(String varName) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (!var.isCompatible(Double.TYPE)) {
            throw new Throwable("Variable " + varName + " cannot be "
                    + "converted to double, or the conversion may result in loss of " + "precision. Use get1D("
                    + varName + ", Boolean.TRUE) for " + "string type. Otherwise use get1D(" + varName + ", false");
        }

        return var.asDoubleArray();
    }

    public Object get1D(String varName, boolean preserve) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        int type = var.getType();

        if (DataTypes.isStringType(type)) {
            return var.asByteArray(null);
        }

        if (preserve) {

            if (DataTypes.isLongType(type)) {
                return var.asLongArray(null);
            }

        }

        return var.asDoubleArray();
    }

    public byte[] get1D(String varName, Boolean stringType) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        int type = var.getType();

        if (!DataTypes.isStringType(type)) {
            throw new Throwable("Variable " + varName + " is not a string variable");
        }

        return var.asByteArray(null);
    }

    public Object get1D(String varName, int point) throws Throwable {
        return get1D(varName, point, -1);
    }

    /*
     * public double[] get1D(String varName, int first, int last, int[] stride)
     * throws Throwable {
     * DoubleVarContainer dbuf = getRangeBuffer(varName, first, last);
     * return dbuf.asSampledArray(stride);
     * }
     */
    public Object get1D(String varName, int first, int last) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        int type = var.getType();
        int[] range = (last >= 0) ? new int[] { first, last } : new int[] { first };

        if (DataTypes.isLongType(type)) {
            return var.asLongArray(range);
        }

        if (DataTypes.isStringType(type)) {
            return var.asByteArray(range);
        }

        return var.asDoubleArray(range);
    }

    /**
     * returns value of the named global attribute
     */
    public Object getAttribute(String atr) {

        if (this.attributeTable == null) {
            return null;
        }

        CDFAttribute a = (CDFAttribute) this.attributeTable.get(atr);

        if (a == null) {
            return null;
        }

        if (!a.isGlobal()) {
            return null;
        }

        if (a.gEntries.size() == 0) {
            return null;
        }

        AttributeEntry ae = (AttributeEntry) a.gEntries.elementAt(0);

        if (ae.isStringType()) {
            String[] sa = new String[a.gEntries.size()];

            for (int i = 0; i < a.gEntries.size(); i++) {
                ae = (AttributeEntry) a.gEntries.elementAt(i);
                sa[i] = (String) ae.getValue();
            }

            return sa;
        }

        return ae.getValue();
    }

    /**
     * returns value of the named attribute for the named variable
     */
    public Object getAttribute(String var, String atr) {
        CDFVariable c = (CDFVariable) this.variableTable.get(var);

        if (c == null) {
            return null;
        }

        Vector attrs = c.attributes;
        List values = new Vector();

        for (int i = 0; i < attrs.size(); i++) {
            AttributeEntry ae = (AttributeEntry) attrs.elementAt(i);

            if (ae.getAttributeName().equals(atr)) {
                values.add(ae.getValue());
            }

        }

        return values;
    }

    /**
     * returns list of AttributeEntry objects for the named global attribute.
     */
    public Vector getAttributeEntries(String atr) throws Throwable {

        if (this.attributeTable == null) {
            throw new Throwable("No attribute named " + atr);
        }

        final CDFAttribute a = (CDFAttribute) this.attributeTable.get(atr);

        if (a == null) {
            throw new Throwable("No attribute named " + atr);
        }

        if (!a.isGlobal()) {
            throw new Throwable("No global attribute named " + atr);
        }

        return a.gEntries;
    }

    /**
     * returns list of AttributeEntry objects for the named attribute
     * for the named variable.
     */
    public Vector getAttributeEntries(String var, String atr) {
        CDFVariable c = (CDFVariable) this.variableTable.get(var);

        if (c == null) {
            return null;
        }

        Vector attrs = c.attributes;
        Vector entries = new Vector();

        for (int i = 0; i < attrs.size(); i++) {
            AttributeEntry ae = (AttributeEntry) attrs.elementAt(i);

            if (ae.getAttributeName().equals(atr)) {
                entries.add(ae);
            }

        }

        return entries;
    }

    public byte[] getByteArray(String varName, int[] pt) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.asByteArray(pt);
    }

    /**
     * returns byte order of source CDF
     */
    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    public double[] getDoubleArray(String varName, int[] pt) throws Throwable {
        return getDoubleArray(varName, pt, true);
    }

    public double[] getDoubleArray(String varName, int[] pt, boolean preserve) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.asDoubleArray(preserve, pt);
    }

    public float[] getFloatArray(String varName, int[] pt) throws Throwable {
        return getFloatArray(varName, pt, true);
    }

    public float[] getFloatArray(String varName, int[] pt, boolean preserve) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.asFloatArray(preserve, pt);
    }

    /**
     * returns value of the named global attribute as GlobalAttribute
     * object.
     */
    public GlobalAttribute getGlobalAttribute(String atr) throws Throwable {

        if (this.attributeTable == null) {
            throw new Throwable("No attribute named " + atr);
        }

        final CDFAttribute a = (CDFAttribute) this.attributeTable.get(atr);

        if (a == null) {
            throw new Throwable("No attribute named " + atr);
        }

        if (!a.isGlobal()) {
            throw new Throwable("No global attribute named " + atr);
        }

        return new GlobalAttribute() {

            @Override
            public Object getEntry(int n) {

                if (n > a.gEntries.size()) {
                    return null;
                }

                if (n < 0) {
                    return null;
                }

                AttributeEntry ae = (AttributeEntry) a.gEntries.elementAt(n);
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
            public boolean isLongType(int n) throws Throwable {

                if ((n < 0) || (n > a.gEntries.size())) {
                    throw new Throwable("Invalid attribute index");
                }

                AttributeEntry ae = (AttributeEntry) a.gEntries.elementAt(n);
                return ae.isLongType();
            }

            @Override
            public boolean isStringType(int n) throws Throwable {

                if ((n < 0) || (n > a.gEntries.size())) {
                    throw new Throwable("Invalid attribute index");
                }

                AttributeEntry ae = (AttributeEntry) a.gEntries.elementAt(n);
                return ae.isStringType();
            }
        };
    }

    public int[] getIntArray(String varName, int[] pt) throws Throwable {
        return getIntArray(varName, pt, true);
    }

    public int[] getIntArray(String varName, int[] pt, boolean preserve) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.asIntArray(preserve, pt);
    }

    public Object getLong(String varName) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (!DataTypes.isLongType(var.getType())) {
            throw new Throwable("getLong method appropriate for " + "TT2000 and INT8 types. ");
        }

        VDataContainer.CLong container = var.getLongContainer(null);
        container.run();
        LongArray la = container.asArray();
        return la.array();
    }

    public long[] getLongArray(String varName, int[] pt) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.asLongArray(pt);
    }

    public double[] getOneD(String varName, boolean columnMajor) throws Throwable {
        CDFVariable var = (CDFVariable) getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (!var.isCompatible(Double.TYPE)) {
            throw new Throwable("Variable " + varName + " cannot be "
                    + "converted to double, or the conversion may result in loss of " + "precision. Use getOneD("
                    + varName + ", Boolean.TRUE) for " + "string type. Otherwise use get1D(" + varName + ", false");
        }

        TargetAttribute ta = new TargetAttribute(false, columnMajor);
        return var.asDoubleArray(ta, null);
    }

    public byte[] getOneD(String varName, Boolean stringType, boolean columnMajor) throws Throwable {
        CDFVariable var = (CDFVariable) getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        int type = var.getType();

        if (!DataTypes.isStringType(type)) {
            throw new Throwable("Variable " + varName + " is not a string variable");
        }

        return var.asByteArray(null, columnMajor);
    }

    // --- POINT
    public Object getPoint(String varName, int point) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (DataTypes.isStringType(var.getType())) {
            VDataContainer.CString container = var.getStringContainer(null);
            container.run();
            StringArray sa = (StringArray) container.asArray();
            return sa.array();
        }

        VDataContainer.CDouble dbuf = new DoubleVarContainer(this, var, new int[] { point }, false,
                ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf.asArray().array();
    }

    public Object getRange(String varName, int first, int last) throws Throwable {
        return getRange(varName, first, last, false);
    }

    // --- RANGE
    public Object getRange(String varName, int first, int last, boolean oned) throws Throwable {
        DoubleVarContainer dbuf = getRangeBuffer(varName, first, last);

        if (oned) {
            return dbuf.as1DArray();
        }

        return dbuf.asArray().array();
    }

    public Object getRange(String varName, int first, int last, int element) throws Throwable {
        return getRange(varName, first, last, new int[] { element });
    }

    public Object getRange(String varName, int first, int last, int[] elements) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (DataTypes.isStringType(var.getType())) {
            throw new Throwable("Function not supported for string variables");
        }

        DoubleVarContainer dbuf = getRangeBuffer(varName, first, last);
        return dbuf.asArrayElement(elements);
    }

    public Object getRangeOneD(String varName, int first, int last, boolean columnMajor) throws Throwable {
        DoubleVarContainer dbuf = getRangeBuffer(varName, first, last);
        return dbuf.asOneDArray(columnMajor);
    }

    public short[] getShortArray(String varName, int[] pt) throws Throwable {
        return getShortArray(varName, pt, true);
    }

    public short[] getShortArray(String varName, int[] pt, boolean preserve) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.asShortArray(preserve, pt);
    }

    public CDFFactory.CDFSource getSource() {
        return this.source;
    }

    public ByteBuffer getValueBuffer(long offset) {
        ByteBuffer bv = getRecord(offset);
        bv.position(this.offset_RECORDS);
        return bv;
    }

    public ByteBuffer getValueBuffer(long offset, int size, int number) {
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
            System.out.println(ex.toString() + " at offset " + offset);
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
     * the named variable
     */
    public Variable getVariable(String name) {
        return (Variable) this.variableTable.get(name);
    }

    /**
     * returns variable names in a String[]
     */
    public String[] getVariableNames() {
        String[] sa = new String[this.varNames.length];
        System.arraycopy(this.varNames, 0, sa, 0, sa.length);
        return sa;
    }

    /**
     * returns variable names of a given VAR_TYPE in a String[]
     */
    public String[] getVariableNames(String type) {
        Vector vars = new Vector();

        for (String varName : this.varNames) {
            Vector v = (Vector) getAttribute(varName, "VAR_TYPE");

            if (v == null) {
                continue;
            }

            if (v.size() == 0) {
                continue;
            }

            String s = (String) v.elementAt(0);

            if (s.equals(type)) {
                vars.add(varName);
            }

        }

        String[] sa = new String[vars.size()];

        for (int i = 0; i < sa.length; i++) {
            sa[i] = (String) vars.elementAt(i);
        }

        return sa;
    }

    /**
     * returns names of global attributes in a String[]
     */
    public String[] globalAttributeNames() {
        Vector vec = new Vector();

        if (this.attributeTable == null) {
            return new String[0];
        }

        Set set = this.attributeTable.keySet();
        Iterator iter = set.iterator();

        while (iter.hasNext()) {
            CDFAttribute attr = (CDFAttribute) this.attributeTable.get(iter.next());

            if (attr.isGlobal()) {
                vec.add(attr.name);
            }

        }

        String[] sa = new String[vec.size()];

        for (int i = 0; i < vec.size(); i++) {
            sa[i] = (String) vec.elementAt(i);
        }

        return sa;
    }

    public boolean isBigEndian() {
        return this.bigEndian;
    }

    public boolean isCompatible(String varName, Class cl) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        return var.isCompatible(cl);
    }

    /**
     * returns row majority of source CDF
     */
    public boolean rowMajority() {
        return ((this.flags & FLAGS_MAJORITY_MASK) == ROW_MAJOR);
    }

    /**
     * returns names of variable attributes in a String[]
     */
    public String[] variableAttributeNames(String name) {
        CDFVariable var = (CDFVariable) this.variableTable.get(name);

        if (var == null) {
            return null;
        }

        String[] sa = new String[var.attributes.size()];

        for (int i = 0; i < sa.length; i++) {
            AttributeEntry ae = (AttributeEntry) var.attributes.elementAt(i);
            sa[i] = ae.getAttributeName();
        }

        return sa;
    }

    /**
     * returns dimensions of the named variable.
     */
    public int[] variableDimensions(String name) {
        VariableMetaData var = (Variable) this.variableTable.get(name);

        if (var == null) {
            return null;
        }

        int[] dims = var.getDimensions();
        int[] ia = new int[dims.length];
        System.arraycopy(ia, 0, dims, 0, dims.length);
        return ia;
    }

    protected ByteBuffer getBuffer() {
        return this.buf;
    }

    protected ByteBuffer getRecord(long offset) {
        ByteBuffer _buf = this.buf.duplicate();
        _buf.position((int) offset);
        return _buf.slice();
    }

    protected int getRecordOffset() {
        return this.offset_RECORDS;
    }

    protected String getString(ByteBuffer _buf, int max) {
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

    protected abstract String getString(long offset);

    protected String getString(long offset, int max) {
        return getString(getRecord(offset), max);
    }

    protected abstract long longInt(ByteBuffer buf);

    protected abstract int lowOrderInt(ByteBuffer buf);

    protected abstract int lowOrderInt(ByteBuffer buf, int offset);

    protected void setBuffer(ByteBuffer b) {
        this.buf = b;
    }

    protected void setByteOrder(boolean _bigEndian) {
        this.byteOrder = (_bigEndian) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        setByteOrder(this.byteOrder);
    }

    protected void setByteOrder(ByteOrder bo) {
        this.bigEndian = bo.equals(ByteOrder.BIG_ENDIAN);
    }

    protected void setOption(CDFFactory.ProcessingOption option) {
        this.processingOption = option;
    }

    protected void setSource(CDFFactory.CDFSource source) {
        this.source = source;
    }

    /**
     * returns name to Variable map
     */
    protected Hashtable variables() {

        if (this.variableTable != null) {
            return this.variableTable;
        }

        LOGGER.entering("CDFImpl", "variables");
        int[] offsets = new int[] { (int) this.zVDRHead, (int) this.rVDRHead };
        String[] vtypes = { "z", "r" };
        Hashtable table = new Hashtable();
        HashMap<Integer, CDFVariable> ivariableTable = new HashMap<>();
        Vector v = new Vector();

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
                String name = cdfv.getName();
                v.add(name);
                ivariableTable.put(cdfv.number, cdfv);
                table.put(name, cdfv);

                if (next == 0) {
                    break;
                }

                offset = next;
                _buf = getRecord(offset);
            }

        }

        this.varNames = new String[v.size()];

        for (int i = 0; i < v.size(); i++) {
            this.varNames[i] = (String) v.elementAt(i);
        }

        this.variableTable = table;
        this.ivariableTable = ivariableTable;
        LOGGER.exiting("CDFImpl", "variables");
        return table;
    }

    /**
     * returns name to Attribute object map
     */
    Hashtable attributes() {

        if (this.attributeTable != null) {
            return this.attributeTable;
        }

        LOGGER.entering("CDFImpl", "attributes");
        long offset = this.ADRHead;

        if (offset == 0) {
            return null;
        }

        Hashtable table = new Hashtable();
        ByteBuffer _buf = getRecord(offset);

        while (true) {
            _buf.position(this.offset_NEXT_ADR);
            long next = longInt(_buf);
            CDFAttribute cdfa = new CDFAttribute(offset);

            if ((table.put(cdfa.getName(), cdfa)) != null) {
                System.out.println("possibly duplicate attribute " + cdfa.getName());
            }

            if (next == 0) {
                break;
            }

            offset = next;
            _buf = getRecord(offset);
        }

        this.attributeTable = table;
        LOGGER.exiting("CDFImpl", "attributes");
        return table;
    }

    /**
     * returns Variable object associated with a given type at a given number
     */
    Variable getCDFVariable(String vtype, int number) {
        CDFVariable var = this.ivariableTable.get(number);

        if (vtype.equals(var.vtype)) {
            return var;
        }

        throw new IllegalArgumentException("unsupported case, file must contain only zvariables or rvariables");
    }

    Object getFillValue(VariableMetaData var) {
        Vector fill = (Vector) getAttribute(var.getName(), "FILLVAL");
        int type = var.getType();

        if (fill.size() != 0) {

            if (fill.get(0).getClass().getComponentType() == Double.TYPE) {
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

    Object getPadValue(VariableMetaData var) {
        return var.getPadValue(true);
    }

    DoubleVarContainer getRangeBuffer(String varName, int first, int last) throws Throwable {
        Variable var = getVariable(varName);

        if (var == null) {
            throw new Throwable("No such variable: " + varName);
        }

        if (DataTypes.isStringType(var.getType())) {
            throw new Throwable("Function not supported for string variables");
        }

        int[] range = new int[] { first, last };
        DoubleVarContainer dbuf;
        dbuf = new DoubleVarContainer(this, var, range, false, ByteOrder.nativeOrder());
        dbuf.run();
        return dbuf;
    }

    /**
     * returns ByteBuffer containing count values for variable var starting at
     * CDF offset value offset.
     */
    ByteBuffer positionBuffer(VariableMetaData var, long offset, int count) {
        ByteBuffer bv;

        if (!var.isCompressed()) {
            bv = getValueBuffer(offset);
        } else {
            int size = var.getDataItemSize();
            bv = getValueBuffer(offset, size, count);
        }

        bv.order(getByteOrder());
        return bv;
    }

    /**
     * AttributeEntry class
     */
    public class CDFAttributeEntry implements AttributeEntry, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 3507692892215505019L;

        transient ByteBuffer _buf;

        int variableNumber;

        int type;

        int nelement;

        String attribute;

        String stringValue;

        String[] stringValues = null;

        Object value;

        public CDFAttributeEntry(ByteBuffer buf, String name) {
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
                    int begin = 0, count = 0;

                    while ((lastIndex = this.stringValue.indexOf(STRINGDELIMITER, begin)) != -1) {
                        this.stringValues[count] = this.stringValue.substring(begin, lastIndex);
                        begin += this.stringValues[count].length() + STRINGDELIMITER.length();
                        count++;
                    }

                    this.stringValues[count] = this.stringValue.substring(begin);
                } else {
                    this.stringValues = null;
                }

            } else {
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
            return (isStringType()) ? (this.stringValues != null ? this.stringValues : this.stringValue) : this.value;
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
        public boolean isSameAs(AttributeEntry ae) {

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

                    if (!newValue.getClass().isArray()) {
                        return false;
                    }

                    String[] oldStrings = this.stringValues;
                    String[] newStrings = (String[]) newValue;
                    return Arrays.equals(oldStrings, newStrings);
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
     * CDFVariable class
     */
    public class CDFVariable implements java.io.Serializable, Variable {

        /**
         *
         */
        private static final long serialVersionUID = 1512547111444704626L;

        int DIMENSION_VARIES = -1;

        public Vector attributes = new Vector();

        String name;

        public int number;

        String vtype;

        final int flags;

        int sRecords;

        int type;

        int numberOfElements;

        protected final int numberOfValues;

        public int[] dimensions;

        public boolean[] varies;

        public Object padValue;

        long offset;

        boolean completed = false;

        transient ByteBuffer _buf;

        int dataItemSize;

        int blockingFactor;

        DataLocator locator;

        boolean recordGap = false;

        public CDFVariable(long offset, String vtype) {
            this.offset = offset;
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

            if (vtype.equals("r")) {
                this.dimensions = CDFImpl.this.rDimSizes;
            }

            if (vtype.equals("z")) {
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
         * unsigned byte or char. Otherwise, throws Throwable
         */
        @Override
        public byte[] asByteArray() throws Throwable {
            return asByteArray(null);
        }

        /**
         * Returns this variable's values for a range of records as byte[] if
         * variable type is byte, unsigned byte or char. Otherwise, throws
         * Throwable
         */
        @Override
        public byte[] asByteArray(int[] pt) throws Throwable {
            VDataContainer.CByte container;

            if (ByteVarContainer.isCompatible(this.type, true)) {
                container = new ByteVarContainer(CDFImpl.this, this, pt);
                container.run();
                return container.as1DArray();
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "byte[].");
        }

        public byte[] asByteArray(int[] pt, boolean columnMajor) throws Throwable {
            VDataContainer.CByte container;

            if (ByteVarContainer.isCompatible(this.type, true)) {
                container = new ByteVarContainer(CDFImpl.this, this, pt);
                container.run();
                return container.asOneDArray(columnMajor);
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "byte[].");
        }

        /**
         * Returns this variable's values as double[].
         * If variable type cannot be cast to double, a Throwable is thrown.
         */
        @Override
        public double[] asDoubleArray() throws Throwable {
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
        public double[] asDoubleArray(boolean preserve, int[] pt) throws Throwable {
            TargetAttribute ta = new TargetAttribute(preserve, false);
            return asDoubleArray(ta, pt);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * double[].
         * If variable type cannot be cast to double, a Throwable is thrown.
         */
        @Override
        public double[] asDoubleArray(int[] pt) throws Throwable {
            return asDoubleArray(false, pt);
        }

        public double[] asDoubleArray(TargetAttribute tattr, int[] pt) throws Throwable {
            VDataContainer.CDouble container;

            try {
                container = getDoubleContainer(pt, tattr.preserve, ByteOrder.nativeOrder());
            } catch (Throwable th) {
                throw new Throwable("Variable " + getName() + " cannot return " + "double[].");
            }

            container.run();
            return container.asOneDArray(tattr.columnMajor);
        }

        /**
         * Returns this variable's values as float[].
         * If variable type cannot be cast to float, a Throwable is thrown.
         */
        @Override
        public float[] asFloatArray() throws Throwable {
            return asFloatArray(false, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * float[].
         * If variable type cannot be cast to float, a Throwable is thrown.
         * If preserve is true, a Throwable is thrown for variables of type
         * double, long or int to signal possible loss of precision.
         */
        @Override
        public float[] asFloatArray(boolean preserve, int[] pt) throws Throwable {
            VDataContainer.CFloat container;

            try {
                container = getFloatContainer(pt, preserve, ByteOrder.nativeOrder());
            } catch (Throwable th) {
                throw new Throwable("Variable " + getName() + " cannot return " + "float[].");
            }

            container.run();
            return container.as1DArray();
        }

        /**
         * Returns this variable's values for the specified range of records as
         * float[].
         * If variable type cannot be cast to float, a Throwable is thrown.
         */
        @Override
        public float[] asFloatArray(int[] pt) throws Throwable {
            return asFloatArray(false, pt);
        }

        /**
         * Returns this variable's values as int[] for variables of type
         * int, short or unsigned short, byte or unsigned byte.
         * throws Throwable for variables of other types.
         */
        @Override
        public int[] asIntArray() throws Throwable {
            return asIntArray(true, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * int[] for variables of type int, short or unsigned short, byte or
         * unsigned byte, or unsigned int (only if preserve is false).
         * throws Throwable for variables of other types.
         */
        @Override
        public int[] asIntArray(boolean preserve, int[] pt) throws Throwable {
            VDataContainer.CInt container;

            try {
                container = getIntContainer(pt, preserve, ByteOrder.nativeOrder());
            } catch (Throwable th) {
                throw new Throwable("Variable " + getName() + " cannot return " + "int[].");
            }

            container.run();
            return container.as1DArray();
        }

        /**
         * Returns this variable's values for the specified range of records as
         * int[] for variables of type int, short or unsigned short, byte or
         * unsigned byte.
         * throws Throwable for variables of other types.
         */
        @Override
        public int[] asIntArray(int[] pt) throws Throwable {
            return asIntArray(true, pt);
        }

        /**
         * Returns this variable's values as long[] for variables of type long.
         * throws Throwable for variables of other types.
         */
        @Override
        public long[] asLongArray() throws Throwable {
            return asLongArray(false, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * long[] for variables of type long.
         * throws Throwable for variables of other types.
         */
        @Override
        public long[] asLongArray(int[] pt) throws Throwable {
            return asLongArray(false, pt);
        }

        /**
         * Returns this variable's values as long[] for variables of type long.
         * throws Throwable for variables of other types.
         */
        @Override
        public short[] asShortArray() throws Throwable {
            return asShortArray(true, null);
        }

        /**
         * Returns this variable's values for the specified range of records as
         * short[] for variables of type short, byte or unsigned byte, or
         * unsigned short (only if preserve is false).
         * throws Throwable for variables of other types.
         */
        @Override
        public short[] asShortArray(boolean preserve, int[] pt) throws Throwable {
            VDataContainer.CShort container;

            try {
                container = getShortContainer(pt, preserve, ByteOrder.nativeOrder());
            } catch (Throwable th) {
                throw new Throwable("Variable " + getName() + " cannot return " + "short[].");
            }

            container.run();
            return container.as1DArray();
        }

        /**
         * Returns this variable's values for the specified range of records as
         * short[] for variables of type short, byte or unsigned byte.
         * throws Throwable for variables of other types.
         */
        @Override
        public short[] asShortArray(int[] pt) throws Throwable {
            return asShortArray(true, pt);
        }

        /**
         * returns blocking factor used in compression
         */
        @Override
        public int getBlockingFactor() {
            return this.blockingFactor;
        }

        public ByteBuffer getBuffer() throws Throwable {
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
        public ByteBuffer getBuffer(Class cl, int[] recordRange, boolean preserve, ByteOrder bo) throws Throwable {

            if (!this.completed) {
                complete();
            }

            if (cl == Byte.TYPE) {
                VDataContainer.CByte container;
                container = new ByteVarContainer(CDFImpl.this, this, recordRange);
                container.run();
                return container.getBuffer();
            }

            if (cl == Double.TYPE) {
                VDataContainer.CDouble container;

                if (DoubleVarContainer.isCompatible(this.type, preserve)) {
                    container = new DoubleVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Float.TYPE) {
                VDataContainer.CFloat container;

                if (FloatVarContainer.isCompatible(this.type, preserve)) {
                    container = new FloatVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Integer.TYPE) {
                VDataContainer.CInt container;

                if (IntVarContainer.isCompatible(this.type, preserve)) {
                    container = new IntVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Short.TYPE) {
                VDataContainer.CShort container;

                if (ShortVarContainer.isCompatible(this.type, preserve)) {
                    container = new ShortVarContainer(CDFImpl.this, this, recordRange, preserve, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            if (cl == Long.TYPE) {
                VDataContainer.CLong container;

                if (LongVarContainer.isCompatible(this.type, preserve)) {
                    container = new LongVarContainer(CDFImpl.this, this, recordRange, bo);
                    container.run();
                    return container.getBuffer();
                }

            }

            throw new Throwable("Inconsistent constraints for " + "this variable");
        }

        public ByteBuffer getBuffer(int[] recordRange) throws Throwable {
            return getBuffer(Double.TYPE, recordRange, false, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CByte getByteContainer(int[] pt) throws Throwable {

            if (ByteVarContainer.isCompatible(this.type, true)) {
                return new ByteVarContainer(CDFImpl.this, this, pt);
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.CByte.");
        }

        @Override
        public CDFImpl getCDF() {
            return CDFImpl.this;
        }

        @Override
        public VariableDataBuffer[] getDataBuffers() throws Throwable {
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
        public VariableDataBuffer[] getDataBuffers(boolean raw) throws Throwable {

            if (!this.completed) {
                complete();
            }

            if (!raw) {

                if ((this.flags & 4) != 0) {
                    throw new Throwable("Function not " + "supported for compressed variables ");
                }

            }

            long[][] locations = this.locator.getLocations();
            List dbufs = new Vector();
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
                dbufs.add(new VariableDataBuffer(first, last, bbuf, compressed));
            }

            VariableDataBuffer[] vdbuf = new VariableDataBuffer[dbufs.size()];
            dbufs.toArray(vdbuf);
            return vdbuf;
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

        /**
         * Gets the dimensions.
         */
        @Override
        public int[] getDimensions() {
            int[] ia = new int[this.dimensions.length];
            System.arraycopy(this.dimensions, 0, ia, 0, this.dimensions.length);
            return ia;
        }

        @Override
        public VDataContainer.CDouble getDoubleContainer(int[] pt, boolean preserve) throws Throwable {
            return getDoubleContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CDouble getDoubleContainer(int[] pt, boolean preserve, ByteOrder bo) throws Throwable {

            if (DoubleVarContainer.isCompatible(this.type, preserve)) {
                return new DoubleVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.CDouble.");
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

        /**
         * Return element count for this variable's dimensions.
         */
        @Override
        public Vector getElementCount() {
            int[] dimensions = getDimensions();
            Vector ecount = new Vector();

            for (int i = 0; i < dimensions.length; i++) {

                if (getVarys()[i]) {
                    ecount.add(dimensions[i]);
                }

            }

            return ecount;
        }

        @Override
        public VDataContainer.CFloat getFloatContainer(int[] pt, boolean preserve) throws Throwable {
            return getFloatContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CFloat getFloatContainer(int[] pt, boolean preserve, ByteOrder bo) throws Throwable {

            if (FloatVarContainer.isCompatible(this.type, preserve)) {
                return new FloatVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.Float.");
        }

        @Override
        public VDataContainer.CInt getIntContainer(int[] pt, boolean preserve) throws Throwable {
            return getIntContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CInt getIntContainer(int[] pt, boolean preserve, ByteOrder bo) throws Throwable {

            if (IntVarContainer.isCompatible(this.type, preserve)) {
                return new IntVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.CInt.");
        }

        /**
         * Gets a list of regions that contain data for the variable.
         * Each element of the vector describes a region as an int[3] array.
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
        public VDataContainer.CLong getLongContainer(int[] pt) throws Throwable {
            return getLongContainer(pt, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CLong getLongContainer(int[] pt, ByteOrder bo) throws Throwable {

            if (LongVarContainer.isCompatible(this.type, true)) {
                return new LongVarContainer(CDFImpl.this, this, pt, ByteOrder.nativeOrder());
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.CLong.");
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
        public Object getPadValue(boolean preservePrecision) {

            if (this.padValue == null) {
                return null;
            }

            if (DataTypes.isStringType(this.type)) {
                return this.padValue;
            }

            if (this.padValue.getClass().getComponentType() == Long.TYPE) {
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
        public VDataContainer.CShort getShortContainer(int[] pt, boolean preserve) throws Throwable {
            return getShortContainer(pt, preserve, ByteOrder.nativeOrder());
        }

        @Override
        public VDataContainer.CShort getShortContainer(int[] pt, boolean preserve, ByteOrder bo) throws Throwable {

            if (ShortVarContainer.isCompatible(this.type, preserve)) {
                return new ShortVarContainer(CDFImpl.this, this, pt, preserve, ByteOrder.nativeOrder());
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.CShort.");
        }

        @Override
        public VDataContainer.CString getStringContainer(int[] pt) throws Throwable {

            if (StringVarContainer.isCompatible(this.type, true)) {
                return new StringVarContainer(CDFImpl.this, this, pt);
            }

            throw new Throwable("Variable " + getName() + " cannot return " + "VDataContainer.CString.");
        }

        /**
         * returns type of values of this variable
         */
        @Override
        public int getType() {
            return this.type;
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
        public boolean isCompatible(Class cl) {
            return BaseVarContainer.isCompatible(getType(), true, cl);
        }

        /**
         * returns whether conversion of this variable to type specified by
         * cl is supported under the given precision preserving constraint.
         */
        @Override
        public boolean isCompatible(Class cl, boolean preserve) {
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
            return (this.vtype.equals("r"));
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
         * returns whether pad value is specified for this variable
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

        long[] asLongArray(boolean preserve, int[] pt) throws Throwable {
            VDataContainer.CLong container;

            try {
                container = getLongContainer(pt, ByteOrder.nativeOrder());
            } catch (Throwable th) {
                throw new Throwable("Variable " + getName() + " cannot return " + "long[].");
            }

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
     * DataLocator
     */
    public class DataLocator implements VariableDataLocator, java.io.Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 713466264682521709L;

        private int numberOfValues;

        private boolean compressed;

        protected Vector locations = new Vector();

        protected DataLocator(ByteBuffer b, int n, boolean compr) {
            this.numberOfValues = n;
            this.compressed = compr;
            b.position(CDFImpl.this.offset_FIRST_VXR);
            long offset = longInt(b);
            ByteBuffer bx = getRecord(offset);
            Vector v = _getLocations(bx);
            registerNodes(bx, v);
        }

        @Override
        public long[][] getLocations() {
            long[][] loc = new long[this.locations.size()][3];

            for (int i = 0; i < this.locations.size(); i++) {
                long[] ia = (long[]) this.locations.elementAt(i);
                loc[i][0] = ia[0];
                loc[i][1] = ia[1];
                loc[i][2] = ia[2];
            }

            return loc;
        }

        public Vector getLocationsAsVector() {
            Vector _locations = new Vector();
            long[][] loc = getLocations();

            for (int i = 0; i < this.locations.size(); i++) {
                _locations.add(loc[i]);
            }

            return _locations;
        }

        public boolean isReallyCompressed() {
            return this.compressed;
        }

        Vector _getLocations(ByteBuffer bx) {
            Vector locations = new Vector();

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
                    locations.add(new long[] { first, last, off });
                }

                if (next == 0) {
                    break;
                }

                bx = getRecord(next);
            }

            return locations;
        }

        void registerNodes(ByteBuffer bx, Vector v) {

            if (this.compressed) {
            }

            for (int i = 0; i < v.size(); i++) {
                long[] loc = (long[]) v.elementAt(i);
                ByteBuffer bb = getRecord(loc[2]);

                if (bb.getInt(CDFImpl.this.offset_RECORD_TYPE) == VXR_RECORD_TYPE) {
                    Vector vin = _getLocations(bb);
                    registerNodes(bb, vin);
                } else {
                    this.locations.add(loc);
                }

            }

        }
    }

    /**
     * CDFAttribute class
     */
    /* public */ class CDFAttribute implements java.io.Serializable, Attribute {

        /**
         *
         */
        private static final long serialVersionUID = -2085580948842565139L;

        String name;

        int scope;

        int num;

        Vector zEntries = new Vector();

        Vector gEntries = new Vector();

        public CDFAttribute(long offset) {
            this.name = getString(offset + CDFImpl.this.offset_ATTR_NAME);
            LOGGER.log(Level.FINER, "new attribute {0} at {1}", new Object[] { this.name, offset });
            ByteBuffer _buf = getRecord(offset);
            _buf.position(CDFImpl.this.offset_SCOPE);
            this.scope = _buf.getInt();
            this.num = _buf.getInt();
            _buf.position(CDFImpl.this.offset_AgrEDRHead);
            long n = longInt(_buf);

            if (n > 0) {
                this.gEntries = getAttributeEntries(n);
                LOGGER.log(Level.FINEST, "link attr {0} to {1} gEntries",
                        new Object[] { this.name, this.gEntries.size() });

                if ((this.scope == 2) || (this.scope == 4)) { // variable scope
                    linkToVariables(this.gEntries, "r");
                }

            }

            _buf.position(CDFImpl.this.offset_AzEDRHead);
            n = longInt(_buf);

            if (n > 0) {
                this.zEntries = getAttributeEntries(n);
                LOGGER.log(Level.FINEST, "link attr {0} to {1} zEntries",
                        new Object[] { this.name, this.zEntries.size() });
                linkToVariables(this.zEntries, "z");
            }

        }

        /**
         * returns attribute entries
         */
        public Vector getAttributeEntries(long offset) {

            if (offset == 0) {
                return null;
            }

            Vector list = new Vector();
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

            return list;
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
        public void linkToVariables(Vector entries, String type) {

            for (int e = 0; e < entries.size(); e++) {
                AttributeEntry ae = (AttributeEntry) entries.elementAt(e);
                CDFVariable var = (CDFVariable) getCDFVariable(type, ae.getVariableNumber());

                if (var == null) {
                    System.out.println("An attribute entry for " + ae.getAttributeName() + " of type " + type
                            + " links to variable number " + ae.getVariableNumber() + ".");
                    System.out.println("Variable whose number is " + ae.getVariableNumber() + " was not found.");
                } else {
                    var.attributes.add(ae);
                }

            }

        }
    }

    static class TargetAttribute {

        public final boolean preserve;

        public final boolean columnMajor;

        TargetAttribute(boolean p, boolean c) {
            this.preserve = p;
            this.columnMajor = c;
        }
    }
}
