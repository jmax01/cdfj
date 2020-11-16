package gov.nasa.gsfc.spdf.cdfj;

import java.io.File;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError;
import gov.nasa.gsfc.spdf.cdfj.CDFException.WriterError;

/**
 * Extends GenericWriter with methods to include user selected data from CDFs.
 */
public class CDFWriter extends GenericWriter {

    static final Logger anonymousLogger = Logger.getAnonymousLogger();

    @SuppressWarnings("hiding")
    static final Logger LOGGER = CDFLogging.newLogger(CDFWriter.class);

    static final String[] DO_NOT_CHECK_LIST_GLOBAL_AS_ARRAY = { "Logical_file_id", "Generation_date",
            "Software_version" };

    static final Collection<String> doNotCheckListGlobal = new CopyOnWriteArrayList<>(
            DO_NOT_CHECK_LIST_GLOBAL_AS_ARRAY);

    Map<String, Map<String, Object>> cdfVariablesByName = new ConcurrentHashMap<>();

    Map<String, Object> globalAttributes = new ConcurrentHashMap<>();

    SelectedVariableCollection vcol = new Selector();

    /**
     * Constructs a of given row majority.
     *
     * @param targetMajority the target majority
     */
    public CDFWriter(final boolean targetMajority) {
        super(targetMajority);
    }

    /**
     * Constructs a populated
     * with data from the given {@link GenericReader GenericReader}.
     *
     * @param cdf the cdf
     *
     * @throws WriterError the writer error
     */
    public CDFWriter(final GenericReader cdf) throws CDFException.WriterError {
        super(cdf.rowMajority());

        try {
            _addCDF(cdf);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th);
        } catch (ReaderError e) {
            throw new CDFException.WriterError(e);
        }

    }

    /**
     * Constructs a column major populated
     * with data from the given CDF file.
     *
     * @param fname the fname
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String fname) throws CDFException.WriterError, CDFException.ReaderError {
        super(false);
        GenericReader cdf = getFileReader(fname);

        try {
            _addCDF(cdf);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with data from the given file.
     *
     * @param fname          the fname
     * @param targetMajority the target majority
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String fname, final boolean targetMajority)
            throws CDFException.WriterError, CDFException.ReaderError {
        super(targetMajority);
        GenericReader cdf = getFileReader(fname);

        try {
            _addCDF(cdf);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with selected variables, and variables they depend on, from
     * the given file.
     *
     * @param fname          the fname
     * @param targetMajority the target majority
     * @param col            the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String fname, final boolean targetMajority, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        super(targetMajority);
        GenericReader cdf = getFileReader(fname);

        _addCDF(cdf, variableNames(cdf, col));

    }

    /**
     * Constructs a column major populated with
     * selected variables, and variables they depend on, from the given file.
     *
     * @param fname the fname
     * @param col   the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String fname, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        super(false);
        GenericReader cdf = getFileReader(fname);

        _addCDF(cdf, variableNames(cdf, col));

    }

    /**
     * Constructs a column major populated
     * with data from the given files.
     *
     * @param files the files
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String[] files) throws CDFException.WriterError, CDFException.ReaderError {
        this(files[0]);

        for (int i = 1; i < files.length; i++) {
            addCDF(files[i]);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with data from the given files.
     *
     * @param files          the files
     * @param targetMajority the target majority
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String[] files, final boolean targetMajority)
            throws CDFException.WriterError, CDFException.ReaderError {
        this(files[0], targetMajority);

        for (int i = 1; i < files.length; i++) {
            addCDF(files[i]);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with selected variables, and variables they depend on, from
     * the given files.
     *
     * @param files          the files
     * @param targetMajority the target majority
     * @param col            the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String[] files, final boolean targetMajority, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        this(files[0], targetMajority, col);

        for (int i = 1; i < files.length; i++) {
            addCDF(files[i]);
        }

    }

    /**
     * Constructs a column major populated with
     * selected variables, and variables they depend on, from the given files.
     *
     * @param files the files
     * @param col   the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final String[] files, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        this(files[0], col);

        for (int i = 1; i < files.length; i++) {
            addCDF(files[i]);
        }

    }

    /**
     * Constructs a column major populated
     * with data from the given URL.
     *
     * @param url the url
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL url) throws CDFException.WriterError, CDFException.ReaderError {
        super(false);
        GenericReader genericReader = new GenericReader(url);

        try {
            _addCDF(genericReader);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with data from the given URL.
     *
     * @param url            the url
     * @param targetMajority the target majority
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL url, final boolean targetMajority)
            throws CDFException.WriterError, CDFException.ReaderError {
        super(targetMajority);
        GenericReader genericReader = new GenericReader(url);

        try {
            _addCDF(genericReader);
        } catch (RuntimeException th) {
            throw new CDFException.WriterError(th);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with selected variables, and variables they depend on, from
     * the given URL.
     *
     * @param url            the url
     * @param targetMajority the target majority
     * @param col            the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL url, final boolean targetMajority, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        super(targetMajority);
        GenericReader cdf = null;

        try {
            cdf = new GenericReader(url);
        } catch (CDFException.ReaderError th) {
            throw new CDFException.WriterError("init CDFWriter failed for " + url, th);
        }

        try {
            _addCDF(cdf, variableNames(cdf, col));
        } catch (CDFException.ReaderError | CDFException.WriterError th) {
            throw new CDFException.WriterError("init CDFWriter failed for " + url, th);
        }

    }

    /**
     * Constructs a column major populated with
     * selected variables, and variables they depend on, from the given URL.
     *
     * @param url the url
     * @param col the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL url, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        super(false);
        GenericReader cdf = null;

        try {
            cdf = new GenericReader(url);
        } catch (CDFException.ReaderError th) {
            throw new CDFException.WriterError("init CDFWriter failed for " + url, th);
        }

        try {
            _addCDF(cdf, variableNames(cdf, col));
        } catch (CDFException.ReaderError | CDFException.WriterError th) {
            throw new CDFException.WriterError("init CDFWriter failed for " + url, th);
        }

    }

    /**
     * Constructs a column major populated
     * with data from the given array of URLs.
     *
     * @param urls the urls
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL[] urls) throws CDFException.WriterError, CDFException.ReaderError {
        this(urls[0]);

        for (int i = 1; i < urls.length; i++) {
            addCDF(urls[i]);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with data from the given array of URLs.
     *
     * @param urls           the urls
     * @param targetMajority the target majority
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL[] urls, final boolean targetMajority)
            throws CDFException.WriterError, CDFException.ReaderError {
        this(urls[0], targetMajority);

        for (int i = 1; i < urls.length; i++) {
            addCDF(urls[i]);
        }

    }

    /**
     * Constructs a of specified row majority,
     * populated with selected variables, and variables they depend on, from
     * the given array of URLs.
     *
     * @param urls           the urls
     * @param targetMajority the target majority
     * @param col            the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL[] urls, final boolean targetMajority, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        this(urls[0], targetMajority, col);

        for (int i = 1; i < urls.length; i++) {
            addCDF(urls[i]);
        }

    }

    /**
     * Constructs a column major populated with
     * selected variables, and variables they depend on, from the given
     * array of URLs.
     *
     * @param urls the urls
     * @param col  the col
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public CDFWriter(final URL[] urls, final SelectedVariableCollection col)
            throws CDFException.WriterError, CDFException.ReaderError {
        this(urls[0], col);

        for (int i = 1; i < urls.length; i++) {
            addCDF(urls[i]);
        }

    }

    /**
     * Adds an attribute to the list of 'not to be monitored' global attributes.
     *
     * @param aname the aname
     */
    public static void addToDoNotCheckList(final String aname) {

        if (doNotCheckListGlobal.contains(aname)) {
            return;
        }

        doNotCheckListGlobal.add(aname);
    }

    /**
     * Removes an attribute from the list 'not to be monitored' global
     * attributes.
     *
     * @param aname the aname
     */
    public static void removeFromDoNotCheckList(final String aname) {

        if (!doNotCheckListGlobal.contains(aname)) {
            return;
        }

        doNotCheckListGlobal.remove(aname);
    }

    /**
     * Returns a new instance of the {@link SelectedVariableCollection
     * SelectedVariableCollection}.
     *
     * @return the selected variable collection
     */
    public static SelectedVariableCollection selectorInstance() {
        return new Selector();
    }

    /**
     * Sets Level for the default anonymous Logger for this class.If a logger has
     * been set via a call to {@link #setLogger(Logger)},
     * this method has no effect
     *
     * @param newLevel the new logger level
     */
    public static void setLoggerLevel(final Level newLevel) {

        if (Objects.equals(LOGGER, anonymousLogger)) {
            LOGGER.setLevel(newLevel);
        }

    }

    @SuppressWarnings("unchecked")
    static List<String> getDependent(final GenericReader cdf, final String vname) {

        String[] variableAttributeNames = cdf.variableAttributeNames(vname);

        if (variableAttributeNames == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(Arrays.stream(variableAttributeNames)
                .filter(attributeName -> attributeName.startsWith("DEPEND_"))
                .map(attributeName -> ((List<String>) cdf.getAttribute(vname, attributeName)).get(0))
                .collect(Collectors.toList()));

    }

    /**
     * Adds previously selected variables, and variables they depend on, from
     * the given {@link GenericReader GenericReader}..
     *
     * @param cdf the cdf
     *
     * @throws ReaderError the reader error
     * @throws WriterError the writer error
     */
    public void addCDF(final GenericReader cdf) throws CDFException.ReaderError, CDFException.WriterError {
        checkLastLeapSecondId(cdf);
        checkGlobalAttributes(cdf);
        List<String> timeVariableList = getTimeVariableList(cdf);
        String[] vnames = this.vcol.getNames();

        for (String vn : vnames) {

            Map<String, Object> map = this.cdfVariablesByName.get(vn);

            if (((Boolean) map.get("variance"))) {

                if (timeVariableList.contains(vn)) {
                    DataContainer dc = this.dataContainers.get(vn);

                    if (cdf.getNumberOfValues(vn) > 0) {
                        Object firstTime;

                        try {

                            if (cdf.isCompatible(vn, Double.TYPE)) {
                                firstTime = cdf.getOneDArray(vn, "double", new int[] { 0, 0 }, true, !this.rowMajority);
                            } else {
                                firstTime = cdf.getOneDArray(vn, "long", new int[] { 0, 0 }, true, !this.rowMajority);
                            }

                        } catch (CDFException.ReaderError th) {
                            throw new CDFException.WriterError("addCDF failed", th);
                        }

                        if (!dc.timeOrderOK(firstTime)) {
                            throw new CDFException.WriterError("Time Backup -Time of first record for variable " + vn
                                    + " of CDF " + cdf.thisCDF.getSource()
                                            .getName()
                                    + " starts before the end of previous CDF");
                        }

                    }

                }

                if (cdf.getNumberOfValues(vn) > 0) {
                    copyVariableData(cdf, vn);
                }

            }

        }

    }

    /**
     * Adds previously selected variables, and variables they depend on, from
     * the given file.
     *
     * @param fname the fname
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public void addCDF(final String fname) throws CDFException.WriterError, CDFException.ReaderError {
        GenericReader cdf = getFileReader(fname);
        addCDF(cdf);
    }

    /**
     * Adds previously selected variables, and variables they depend on, from
     * the given URL.
     *
     * @param url the url
     *
     * @throws WriterError the writer error
     * @throws ReaderError the reader error
     */
    public void addCDF(final URL url) throws CDFException.WriterError, CDFException.ReaderError {
        GenericReader cdf = null;

        try {
            cdf = new GenericReader(url);
        } catch (CDFException.ReaderError th) {
            throw new CDFException.WriterError("addCDF failed for " + url, th);
        }

        addCDF(cdf);
    }

    /**
     * Returns names of 'not to be monitored' global attributes.
     *
     * @return the string[]
     */
    public String[] attributesInDoNotCheckList() {

        return doNotCheckListGlobal.toArray(new String[0]);

    }

    /**
     * sets a Logger for this class.
     *
     * @param _logger the new logger
     */
    public void setLogger(final Logger _logger) {

        if (_logger == null) {
            return;
        }

        synchronized (this) {

        }

    }

    void checkGlobalAttributes(final GenericReader cdf) {

        String[] globalAttributeNames = cdf.globalAttributeNames();

        for (String globalAttributeName : globalAttributeNames) {

            @SuppressWarnings("unchecked")
            List<AttributeEntry> globalAttributeEntries = (List<AttributeEntry>) this.globalAttributes
                    .get(globalAttributeName);

            List<AttributeEntry> attributeEntries2 = cdf.attributeEntries(globalAttributeName);

            for (AttributeEntry attributeEntry : attributeEntries2) {

                boolean found = false;

                for (AttributeEntry globalAttributeEntry : globalAttributeEntries) {

                    found = globalAttributeEntry.isSameAs(attributeEntry);

                    if (found) {
                        break;
                    }

                }

                if (!found) {

                    if (!doNotCheckListGlobal.contains(globalAttributeName)) {
                        LOGGER.log(Level.FINE,
                                "Global attribute entry for attribute {0} not in base, or differs from the value in base.",
                                globalAttributeName);
                    }

                }

            }

        }

    }

    void checkLastLeapSecondId(final GenericReader cdf) throws CDFException.WriterError {

        if (this.leapSecondLastUpdated == -1) {
            this.leapSecondLastUpdated = cdf.getLastLeapSecondId();
        } else {

            if (this.leapSecondLastUpdated != cdf.getLastLeapSecondId()) {
                throw new CDFException.WriterError("LastLeapSecondId " + cdf.getLastLeapSecondId()
                        + " does not match previously found " + this.leapSecondLastUpdated);
            }

        }

    }

    void copyGlobalAttributes(final GenericReader cdf) throws CDFException.WriterError {

        for (String globalAttributeName : cdf.globalAttributeNames()) {

            List<AttributeEntry> _attributeEntries = cdf.attributeEntries(globalAttributeName);

            this.globalAttributes.put(globalAttributeName, _attributeEntries);

            for (AttributeEntry attributeEntry : _attributeEntries) {

                addGlobalAttributeEntry(globalAttributeName, SupportedTypes.cdfType(attributeEntry.getType()),
                        attributeEntry.getValue());
            }

        }

    }

    void copyVariableAttributes(final GenericReader cdf, final String vn)
            throws CDFException.ReaderError, CDFException.WriterError {

        boolean compressed = this.vcol.isCompressed(vn);

        SparseRecordOption sro = this.vcol.getSparseRecordOption(vn);

        CDFDataType ctype = SupportedTypes.cdfType(cdf.getType(vn));

        Map<String, Object> vmap = new ConcurrentHashMap<>();
        vmap.put("ctype", ctype);
        vmap.put("compressed", compressed);
        vmap.put("dimensions", cdf.getDimensions(vn));
        vmap.put("varys", cdf.getVarys(vn));
        vmap.put("variance", cdf.recordVariance(vn));
        vmap.put("padValue", cdf.getPadValue(vn, true));
        vmap.put("numberOfElements", cdf.getNumberOfElements(vn));

        // GenericReader returns EPOCH16 as a 1 dim variable
        int[] dims = cdf.getDimensions(vn);

        boolean[] varys = cdf.getVarys(vn);

        if (ctype == CDFDataType.EPOCH16) {
            dims = new int[0];
            varys = new boolean[0];
        }

        try {
            defineVariable(vn, ctype, /* cdf.getDimensions(vn) */dims, /* cdf.getVarys(vn) */varys,
                    cdf.recordVariance(vn), compressed, cdf.getPadValue(vn, true), cdf.getNumberOfElements(vn), sro);
        } catch (CDFException.ReaderError | CDFException.WriterError ex) {

            throw new CDFException.WriterError("Failed to define " + vn, ex);
        }

        Map<String, Object> amap = new ConcurrentHashMap<>();
        String[] anames = cdf.variableAttributeNames(vn);

        for (String aname : anames) {

            List<AttributeEntry> entries = cdf.attributeEntries(vn, aname);

            amap.put(aname, entries);

            AttributeEntry entry = entries.get(0);

            ctype = SupportedTypes.cdfType(entry.getType());

            setVariableAttributeEntry(vn, aname, ctype, entry.getValue());

            for (int e = 1; e < entries.size(); e++) {
                entry = entries.get(e);
                ctype = SupportedTypes.cdfType(cdf.getType(vn));
                addVariableAttributeEntry(vn, aname, ctype, entry.getValue());
            }

        }

        vmap.put("amap", amap);

        this.cdfVariablesByName.put(vn, vmap);
    }

    void copyVariableData(final GenericReader cdf, final String vn)
            throws CDFException.ReaderError, CDFException.WriterError {

        if ((cdf.getByteOrder() == ByteOrder.LITTLE_ENDIAN) && (cdf.rowMajority() == this.rowMajority)) {
            VariableDataBuffer[] dbufs = null;

            try {
                Variable variable = cdf.thisCDF.getVariable(vn);
                dbufs = variable.getDataBuffers(true);
            } catch (RuntimeException th) {
                throw new CDFException.ReaderError("copyVariableData failed for " + vn, th);
            }

            for (VariableDataBuffer dbuf : dbufs) {
                addBuffer(vn, dbuf);
            }

        } else {
            VDataContainer _container = null;

            try {
                _container = getContainer(cdf, vn);
            } catch (RuntimeException th) {
                throw new CDFException.ReaderError("copyVariableData for " + vn, th);
            }

            _container.run();
            int[] rr = { 0, cdf.getNumberOfValues(vn) - 1, 1 };
            DataContainer container = this.dataContainers.get(vn);

            if (container != null) {
                int _last = container.getLastRecord();

                if (_last >= 0) {
                    _last++;
                    rr[0] += _last;
                    rr[1] += _last;
                }

            }

            if (cdf.rowMajority() == this.rowMajority) {
                addData(vn, _container.getBuffer(), rr);
            } else {
                addOneD(vn, _container.asOneDArray(!this.rowMajority), rr, true);
            }

        }

    }

    VDataContainer getContainer(final GenericReader rdr, final String variableName) throws ReaderError {
        VDataContainer container = null;
        CDFDataType ctype = SupportedTypes.cdfType(rdr.getType(variableName));
        Variable variable = rdr.thisCDF.getVariable(variableName);
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;

        if ((ctype == CDFDataType.INT1) || (ctype == CDFDataType.UINT1)) {
            container = variable.getByteContainer(null);
        }

        if (ctype == CDFDataType.INT2) {
            container = variable.getShortContainer(null, true, order);
        }

        if (ctype == CDFDataType.INT4) {
            container = variable.getIntContainer(null, true, order);
        }

        if (ctype == CDFDataType.UINT2) {
            container = variable.getShortContainer(null, false, order);
        }

        if (ctype == CDFDataType.UINT4) {
            container = variable.getIntContainer(null, false, order);
        }

        if (ctype == CDFDataType.FLOAT) {
            container = variable.getFloatContainer(null, true, order);
        }

        if ((ctype == CDFDataType.DOUBLE) || (ctype == CDFDataType.EPOCH)
            || (ctype == CDFDataType.EPOCH16)) {
            container = variable.getDoubleContainer(null, true, order);
        }

        if ((ctype == CDFDataType.TT2000) || (ctype == CDFDataType.INT8)) {
            container = variable.getLongContainer(null, order);
        }

        if (ctype == CDFDataType.CHAR) {
            container = variable.getStringContainer(null);
        }

        return container;
    }

    GenericReader getFileReader(final String fname) throws CDFException.ReaderError, WriterError {
        GenericReader cdf = null;
        File file = new File(fname);

        if (!file.exists()) {
            throw new CDFException.ReaderError("file " + fname + " does not exist.");
        }

        try {
            long size = file.length();

            if (size > Integer.MAX_VALUE) {
                cdf = ReaderFactory.getReader(fname);
            } else {

                if (isWindows()) {
                    return ReaderFactory.getReader(fname, true);
                }

                cdf = new GenericReader(fname);
            }

        } catch (CDFException.ReaderError th) {
            throw new CDFException.WriterError("addCDF for filename " + fname, th);
        }

        return cdf;
    }

    String[] getSelected(final GenericReader cdf, final SelectedVariableCollection col)
            throws CDFException.ReaderError {

        List<String> selected = new ArrayList<>();

        for (String name : col.getNames()) {

            LOGGER.log(Level.FINE, "requested: {0}", name);

            if (!hasVariable(cdf, name)) {
                LOGGER.log(Level.FINE, "{0} not found in original. ignoring.", name);
                continue;
            }

            if (!selected.contains(name)) {
                selected.add(name);
                this.vcol.add(name, col.isCompressed(name), col.getSparseRecordOption(name));
            }

            for (String dependentVariable : getDependent(cdf, name)) {

                if (selected.contains(dependentVariable)) {
                    continue;
                }

                selected.add(dependentVariable);

                boolean compressed = cdf.isCompressed(dependentVariable);

                SparseRecordOption sro = sparseRecordOption(cdf, name);

                if (col.hasVariable(dependentVariable)) {
                    compressed = col.isCompressed(dependentVariable);
                    sro = col.getSparseRecordOption(name);
                }

                this.vcol.add(dependentVariable, compressed, sro);
                LOGGER.log(Level.FINE, "added: {0}", dependentVariable);
            }

        }

        if (selected.isEmpty()) {
            LOGGER.fine("No valid variables selected.");
            return new String[0];
        }

        return selected.toArray(new String[0]);
    }

    List<String> getTimeVariableList(final GenericReader cdf) {

        List<String> list = new ArrayList<>();
        String[] vnames = this.vcol.getNames();

        for (String vname : vnames) {
            String tvar;

            try {
                tvar = cdf.getTimeVariableName(vname);
            } catch (RuntimeException th) {
                LOGGER.log(Level.SEVERE, "getTimeVariableName failed", th);
                tvar = null;
            }

            if (tvar != null) {
                list.add(tvar);
            }

        }

        return list;
    }

    String getTimeVariableName(final GenericReader cdf, final String vname) {
        String tvar;

        try {
            tvar = cdf.getTimeVariableName(vname);
        } catch (RuntimeException th) {
            LOGGER.fine(th.toString());
            tvar = null;
        }

        return tvar;
    }

    boolean hasVariable(final GenericReader cdf, final String vname) {
        String[] vnames = cdf.getVariableNames();

        for (String vname1 : vnames) {

            if (vname.equals(vname1)) {
                return true;
            }

        }

        return false;
    }

    boolean isTimeType(final int type) {
        boolean isTimeType = (CDFTimeType.EPOCH.getValue() == type);
        isTimeType |= (CDFTimeType.EPOCH16.getValue() == type);
        isTimeType |= (CDFTimeType.TT2000.getValue() == type);
        return isTimeType;
    }

    SparseRecordOption sparseRecordOption(final GenericReader cdf, final String vname) throws CDFException.ReaderError {

        if (cdf.missingRecordValueIsPad(vname)) {
            return SparseRecordOption.PADDED;
        }

        if (cdf.missingRecordValueIsPrevious(vname)) {
            return SparseRecordOption.PREVIOUS;
        }

        return SparseRecordOption.NONE;
    }

    void validateVariableProperties(final GenericReader cdf, final String vn) throws ReaderError {

        Map<String, Object> vmap = this.cdfVariablesByName.get(vn);

        cdf.isCompressed(vn);

        boolean failed = (vmap.get("ctype") != SupportedTypes.cdfType(cdf.getType(vn)));

        // if (!failed) failed = ((boolean)vmap.get("compressed") != compressed);
        if (!failed) {
            failed = !Arrays.equals((int[]) vmap.get("dimensions"), cdf.getDimensions(vn));
        }

        if (!failed) {
            failed = !Arrays.equals((boolean[]) vmap.get("varys"), cdf.getVarys(vn));
        }

        if (!failed) {
            failed = (((Boolean) vmap.get("variance")) != cdf.recordVariance(vn));
        }

        if (!failed) {
            failed = (((Integer) vmap.get("numberOfElements")) != cdf.getNumberOfElements(vn));
        }

        // vmap.put("padValue", cdf.getPadValue(vn, true));
        if (failed) {
            throw new IllegalStateException("Properties of variable " + vn + "do not match.");
        }

    }

    String[] variableNames(final GenericReader cdf, final SelectedVariableCollection col)
            throws CDFException.ReaderError {
        String[] vnames;

        if (col == null) {
            vnames = cdf.getVariableNames();

            for (String vname : vnames) {
                this.vcol.add(vname, cdf.isCompressed(vname), sparseRecordOption(cdf, vname));
            }

        } else {
            vnames = getSelected(cdf, col);
        }

        return vnames;
    }

    private void _addCDF(final GenericReader cdf) throws ReaderError, WriterError {
        String[] vnames = cdf.getVariableNames();

        for (String vname : vnames) {
            this.vcol.add(vname, cdf.isCompressed(vname));
        }

        _addCDF(cdf, vnames);
    }

    private void _addCDF(final GenericReader cdf, final String[] vnames)
            throws CDFException.WriterError, CDFException.ReaderError {
        checkLastLeapSecondId(cdf);
        copyGlobalAttributes(cdf);
        addGlobalAttributeEntry("cdfj_source", cdf.getSource());

        for (String vn : vnames) {
            copyVariableAttributes(cdf, vn);
        }

        for (String vname : vnames) {
            String tvar;

            if (!cdf.recordVariance(vname)) {
                continue;
            }

            if (cdf.isTimeType(vname)) {
                continue;
            }

            try {
                tvar = cdf.getTimeVariableName(vname);
            } catch (RuntimeException th) {
                LOGGER.log(Level.WARNING, th, () -> "Variable name, " + vname + ", is not a Time Variable Name");
                tvar = null;
            }

            if (tvar != null) {
                DataContainer dc = this.dataContainers.get(vname);

                if (cdf.getNumberOfValues(vname) == cdf.getNumberOfValues(tvar)) {
                    dc.setTimeContainer(this.dataContainers.get(tvar));
                }

            }

        }

        for (String vname : vnames) {

            if (cdf.getNumberOfValues(vname) == 0) {
                this.dataContainers.get(vname)
                        .addPhantomEntry();
            } else {
                copyVariableData(cdf, vname);
            }

            this.vcol.add(vname, cdf.isCompressed(vname), sparseRecordOption(cdf, vname));
        }

    }

    static class Selector implements SelectedVariableCollection {

        Map<String, Boolean> map = new HashMap<>();

        Map<String, SparseRecordOption> smap = new HashMap<>();

        @Override
        public void add(final String vname, final boolean compression) {
            this.map.put(vname, compression);
        }

        @Override
        public void add(final String vname, final boolean compression, final SparseRecordOption opt) {
            add(vname, compression);
            this.smap.put(vname, opt);
        }

        @Override
        public String[] getNames() {

            return this.map.keySet()
                    .toArray(new String[0]);

        }

        @Override
        public SparseRecordOption getSparseRecordOption(final String name) {

            if (this.smap.get(name) == null) {
                return SparseRecordOption.PADDED;
            }

            return this.smap.get(name);
        }

        @Override
        public boolean hasVariable(final String name) {
            return (this.map.get(name) != null);
        }

        @Override
        public boolean isCompressed(final String name) {
            return this.map.get(name);
        }
    }
}
