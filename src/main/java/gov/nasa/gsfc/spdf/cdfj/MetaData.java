package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteOrder;
import java.util.Vector;

/**
 * Abstract base class for GenericReader.
 * Provides methods to access CDF properties, global attributes, variable
 * properties and attributes.
 */
public abstract class MetaData {

    CDFImpl thisCDF;

    /**
     * Returns whether there is a variable with the given name.
     *
     * @param varName
     *
     * @return
     */
    public final boolean existsVariable(String varName) {
        return (varName != null) && (this.thisCDF.getVariable(varName) != null);
    }

    /**
     * Returns value of the first entry for the named global attribute.
     * <p>
     * For a character string attribute, a String[] is returned
     * For a numeric attribute, a long[] is returned for long type;
     * double[] is returned for all other numeric types.
     * <p>
     * This method is deprecated. Use {@link #getGlobalAttribute(String atr)
     * getGlobalAttribute(String atr)} method to extract all entries.
     *
     * @param atr
     *
     * @return
     */
    public final Object getAttribute(String atr) {
        return this.thisCDF.getAttribute(atr);
    }

    /**
     * Returns value of the named attribute for specified variable.For a character
     * string attribute, a Vector of String is returned.For a numeric attribute, a
     * Vector of size 1 is returned.The
     * single element of the Vector is a long[] if attribute's type is long;
     * For all other numeric types, the element is a double[].
     *
     * @param varName
     * @param aname
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final Object getAttribute(String varName, String aname) throws CDFException.ReaderError {
        return this.thisCDF.getAttribute(varName, aname);
    }

    /**
     * Returns list of {@link AttributeEntry AttributeEntry} objects for the
     * named global attribute.
     *
     * @param aname
     *
     * @return
     *
     * @throws gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError
     */
    public final Vector<AttributeEntry> getAttributeEntries(String aname) throws CDFException.ReaderError {

        try {
            return this.thisCDF.getAttributeEntries(aname);
        } catch (Throwable th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns list of {@link AttributeEntry AttributeEntry} objects for
     * the named attribute for the named variable.
     *
     * @param varName
     * @param aname
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final Vector<AttributeEntry> getAttributeEntries(String varName, String aname)
            throws CDFException.ReaderError {
        return this.thisCDF.getAttributeEntries(varName, aname);
    }

    /**
     * Returns the blocking factor used to compress this variable.See the CDF User's
     * Guide for details.
     *
     * @param varName
     *
     * @return
     *
     * @throws gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError
     */
    public final int getBlockingFactor(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getBlockingFactor();
    }

    /**
     * Returns ByteOrder.LITTLE_ENDIAN, or ByteOrder.BIG_ENDIAN depending
     * the CDF encoding
     *
     * @return
     */
    public final ByteOrder getByteOrder() {
        return this.thisCDF.getByteOrder();
    }

    /**
     * Returns size of a data item for the given variable.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int getDataItemSize(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getDataItemSize();
    }

    /**
     * Returns dimensions the given variable.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int[] getDimensions(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getDimensions();
    }

    /**
     * Returns effective dimensions of the given variable.Dimensions for which
     * dimVarys is false are ignored.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int[] getEffectiveDimensions(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        Variable var = this.thisCDF.getVariable(varName);
        return var.getEffectiveDimensions();
    }

    /**
     * Returns effective rank of this variable.Dimensions for which dimVarys is
     * false do not count.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int getEffectiveRank(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getEffectiveRank();
    }

    /**
     * Return element count for this variable's dimensions.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final Vector<Integer> getElementCount(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getElementCount();
    }

    /**
     * Returns {@link GlobalAttribute GlobalAttribute} object for
     * the named global attribute.
     *
     * @param atr
     *
     * @return
     *
     * @throws gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError
     */
    public final GlobalAttribute getGlobalAttribute(String atr) throws CDFException.ReaderError {

        try {
            return this.thisCDF.getGlobalAttribute(atr);
        } catch (Throwable th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Identifies the leap second table used in creating this CDF.Returns the id of
     * the last leap second in the leap second table.
     * Leap second id is an integer = year*10000 + month*100 + day, where
     * year, month and day refer to the day following the leap second. Until
     * 2015, leap second has been added at the end of December, or June. Thus
     * leapSecondId is either (10000*year + 101), or (10000*year + 701).
     *
     * @return
     */
    public final int getLastLeapSecondId() {
        return this.thisCDF.lastLeapSecondId;
    }

    /**
     * Returns given variable's number property.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int getNumber(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getNumber();
    }

    /**
     * Returns given variable's 'number of elements' property.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int getNumberOfElements(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getNumberOfElements();
    }

    /**
     * Returns 'number of values' property of the given variable.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int getNumberOfValues(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getNumberOfValues();
    }

    /**
     * Returns 'pad value' property of the given variable.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final Object getPadValue(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getPadValue();
    }

    /**
     * Returns 'pad value' property of the given variable subject to the given
     * precision preservation constraint.
     *
     * @param varName
     * @param preservePrecision
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final Object getPadValue(String varName, boolean preservePrecision) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        Variable var = this.thisCDF.getVariable(varName);
        return var.getPadValue(preservePrecision);
    }

    /**
     * Returns record range for this variable
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int[] getRecordRange(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getRecordRange();
    }

    /**
     * Returns the name of the time variable for the given variable.
     *
     * @param varName variable name
     *
     * @return String
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final String getTimeVariableName(String varName) throws Throwable {

        if (!existsVariable(varName)) {
            throw new Throwable("CDF does not hava a variable named " + varName);
        }

        String tname = userTimeVariableName(varName);

        if (tname != null) {
            return tname;
        }

        // assume istp convention
        Variable var = this.thisCDF.getVariable(varName);
        String vname = var.getName();
        Vector v = (Vector) this.thisCDF.getAttribute(vname, "DEPEND_0");

        if (v.size() > 0) {
            tname = (String) v.elementAt(0);
        }

        if (tname == null) {

            if (!vname.equals("Epoch")) {

                if (this.thisCDF.getVariable("Epoch") != null) {
                    tname = "Epoch";
                    System.out.println("Variable " + vname + " has no DEPEND_0" + " attribute. Variable named Epoch "
                            + "assumed to be the right time variable");
                } else {
                    throw new Throwable("Time variable not found for " + vname);
                }

            } else {
                throw new Throwable("Variable named Epoch has no DEPEND_0 " + "attribute.");
            }

        }

        return tname;
    }

    /**
     * Returns CDF type of the variable.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final int getType(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getType();
    }

    /**
     * Returns names of variables in the CDF
     *
     * @return
     */
    public final String[] getVariableNames() {
        return this.thisCDF.getVariableNames();
    }

    /**
     * returns variable names of a given VAR_TYPE in a String[]
     *
     * @param type
     *
     * @return
     */
    public final String[] getVariableNames(String type) {
        return this.thisCDF.getVariableNames(type);
    }

    /**
     * Returns 'varys' property of the given variable.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean[] getVarys(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).getVarys();
    }

    /**
     * Returns number of entries for the named global attribute.
     *
     * @param atr
     *
     * @return
     *
     * @throws gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError
     */
    public final int globalAttributeEntryCount(String atr) throws CDFException.ReaderError {
        return getGlobalAttribute(atr).getEntryCount();
    }

    /**
     * Returns names of global attributes.
     *
     * @return
     */
    public final String[] globalAttributeNames() {
        return this.thisCDF.globalAttributeNames();
    }

    /**
     * returns whether conversion of this variable to type specified by
     * cl is supported while preserving precision.
     *
     * @param varName
     * @param cl
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean isCompatible(String varName, Class cl) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        try {
            return this.thisCDF.getVariable(varName).isCompatible(cl);
        } catch (Throwable th) {
            throw new CDFException.ReaderError(th.getMessage());
        }

    }

    /**
     * Returns whether conversion of this variable to type specified by
     * cl is supported under the given precision preserving constraint.
     *
     * @param varName
     * @param preserve
     * @param cl
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean isCompatible(String varName, Class cl, boolean preserve) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        Variable var = this.thisCDF.getVariable(varName);
        return var.isCompatible(cl, preserve);
    }

    /**
     * Returns whether the values of the variable are represented in a
     * compressed form in the CDF.For variables declared to be compressed, CDF
     * specification allows
     * the values to be stored in uncompressed form if the latter results in
     * a smaller size.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean isCompressed(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).isCompressed();
    }

    /**
     * Shows whether one or more records (in the range returned by
     * getRecordRange()) are missing.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean isMissingRecords(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).isMissingRecords();
    }

    /**
     * Returns whether the given variable represents time.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean isTimeType(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        Variable var = this.thisCDF.getVariable(varName);
        int type = var.getType();
        boolean isTimeType = (CDFTimeType.EPOCH.getValue() == type);
        isTimeType |= (CDFTimeType.EPOCH16.getValue() == type);
        isTimeType |= (CDFTimeType.TT2000.getValue() == type);
        return isTimeType;
    }

    /**
     * Returns whether a variable of type r-variable..See the CDF User's Guide for
     * details.
     *
     * @param varName
     *
     * @return
     *
     * @throws gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError
     */
    public final boolean isTypeR(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).isTypeR();
    }

    /**
     * Return whether the missing record should be assigned the pad
     * value.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean missingRecordValueIsPad(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        Variable var = this.thisCDF.getVariable(varName);
        return var.missingRecordValueIsPad();
    }

    /**
     * Return whether the missing record should be assigned the last
     * seen value.If none has been seen, pad value is assigned.
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean missingRecordValueIsPrevious(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        Variable var = this.thisCDF.getVariable(varName);
        return var.missingRecordValueIsPrevious();
    }

    /**
     * Returns an indication of the record varying property of a variable.
     *
     * @param varName
     *
     * @return false if variable has a constant value for this CDF.
     *
     * @throws CDFException.ReaderError if variable does not exist
     */
    public final boolean recordVariance(String varName) throws CDFException.ReaderError {

        if (!existsVariable(varName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + varName);
        }

        return this.thisCDF.getVariable(varName).recordVariance();
    }

    /**
     * Returns whether the arrays are stored in row major order in the source
     *
     * @return
     */
    public final boolean rowMajority() {
        return this.thisCDF.rowMajority();
    }

    /**
     *
     * @param varName
     *
     * @return
     *
     * @throws CDFException.ReaderError
     */
    public abstract String userTimeVariableName(String varName) throws CDFException.ReaderError;

    /**
     * Returns names of variable attributes.
     *
     * @param name
     *
     * @return
     */
    public final String[] variableAttributeNames(String name) {
        return this.thisCDF.variableAttributeNames(name);
    }
}
