package gov.nasa.gsfc.spdf.cdfj;

import java.nio.*;
import java.util.*;
import java.util.stream.*;

import gov.nasa.gsfc.spdf.cdfj.CDFException.*;
import lombok.extern.java.*;

/**
 * Abstract base class for GenericReader.
 * Provides methods to access CDF properties, global attributes, variable
 * properties and attributes.
 */
@Log
public abstract class MetaData {

    CDFImpl thisCDF;

    /**
     * Returns list of {@link AttributeEntry AttributeEntry} objects for
     * the named attribute for the named variable.
     *
     *
     * @param attributeName the attribute name
     *
     * @return the list attribute entries
     */
    public final List<AttributeEntry> attributeEntries(final String attributeName) {
        return this.thisCDF.attributeEntries(attributeName);
    }

    /**
     * Returns list of {@link AttributeEntry AttributeEntry} objects for
     * the named attribute for the named variable.
     *
     * @param variableName  the variable name
     * @param attributeName the attribute name
     *
     * @return the list or null
     */
    public final List<AttributeEntry> attributeEntries(final String variableName, final String attributeName) {
        return this.thisCDF.attributeEntries(variableName, attributeName);
    }

    /**
     * Returns whether there is a variable with the given name.
     *
     * @param variableName the var name
     *
     * @return true, if successful
     */
    public final boolean existsVariable(final String variableName) {
        return (variableName != null) && (this.thisCDF.getVariable(variableName) != null);
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
     * @param atr the atr
     *
     * @return the attribute
     */
    public final Object getAttribute(final String atr) {
        return this.thisCDF.getAttribute(atr);
    }

    /**
     * Returns value of the named attribute for specified variable.
     * <p>
     * For a character string attribute, a Vector of String is
     * returned.
     * <p>
     * For a numeric attribute, a Vector of size 1 is returned.
     * <p>
     * The single element of the Vector is a long[] if attribute's
     * type is long;
     * <p>
     * For all other numeric types, the element is a double[].
     *
     * @param variableName the var name
     * @param aname        the aname
     *
     * @return the attribute
     */
    public final Object getAttribute(final String variableName, final String aname) {
        return this.thisCDF.getAttribute(variableName, aname);
    }

    /**
     * Returns list of {@link AttributeEntry AttributeEntry} objects for the
     * named global attribute.
     *
     * @param attributeName the attribute name
     *
     * @return the attribute entries
     *
     * @throws ReaderError the reader error
     *
     * @deprecated use {@link #attributeEntries(String)}
     */
    @Deprecated
    public final Vector<AttributeEntry> getAttributeEntries(final String attributeName)
            throws CDFException.ReaderError {

        try {
            return this.attributeEntries(attributeName)
                    .stream()
                    .collect(Collectors.toCollection(Vector::new));
        }
        catch (RuntimeException ex) {
            throw new CDFException.ReaderError("Failed to retrieve attribute entries for " + attributeName, ex);
        }

    }

    /**
     * Returns list of {@link AttributeEntry AttributeEntry} objects for
     * the named attribute for the named variable.
     *
     * @param variableName  the variable name
     * @param attributeName the attribute name
     *
     * @return the attribute entries or null
     *
     * @deprecated Use {@link #attributeEntries(String, String)}
     */
    @Deprecated
    public final Vector<AttributeEntry> getAttributeEntries(final String variableName, final String attributeName) {

        List<AttributeEntry> attributeEntries = this.attributeEntries(variableName, attributeName);

        return (attributeEntries == null) ? null
                : attributeEntries.stream()
                        .collect(Collectors.toCollection(Vector::new));
    }

    /**
     * Returns the blocking factor used to compress this variable.See the CDF User's
     * Guide for details.
     *
     * @param variableName the var name
     *
     * @return the blocking factor
     *
     * @throws ReaderError the reader error
     */
    public final int getBlockingFactor(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getBlockingFactor();
    }

    /**
     * Returns ByteOrder.LITTLE_ENDIAN, or ByteOrder.BIG_ENDIAN depending
     * the CDF encoding
     *
     * @return the byte order
     */
    public final ByteOrder getByteOrder() {
        return this.thisCDF.getByteOrder();
    }

    /**
     * Returns size of a data item for the given variable.
     *
     * @param variableName the var name
     *
     * @return the data item size
     *
     * @throws ReaderError the reader error
     */
    public final int getDataItemSize(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getDataItemSize();
    }

    /**
     * Gets the dimension element counts.
     *
     * @param variableName the variable name
     *
     * @return the dimension element counts
     */
    public final List<Integer> getDimensionElementCounts(final String variableName) {
        return this.thisCDF.getVariable(variableName)
                .getDimensionElementCounts();
    }

    /**
     * Returns dimensions the given variable.
     *
     * @param variableName the var name
     *
     * @return the dimensions
     *
     * @throws ReaderError the reader error
     */
    public final int[] getDimensions(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getDimensions();
    }

    /**
     * Returns effective dimensions of the given variable.Dimensions for which
     * dimVarys is false are ignored.
     *
     * @param variableName the var name
     *
     * @return the effective dimensions
     *
     * @throws ReaderError the reader error
     */
    public final int[] getEffectiveDimensions(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        Variable variable = this.thisCDF.getVariable(variableName);
        return variable.getEffectiveDimensions();
    }

    /**
     * Returns effective rank of this variable.Dimensions for which dimVarys is
     * false do not count.
     *
     * @param variableName the var name
     *
     * @return the effective rank
     *
     * @throws ReaderError the reader error
     */
    public final int getEffectiveRank(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getEffectiveRank();
    }

    /**
     * Returns effective rank of this variable.Dimensions for which dimVarys is
     * false do not count.
     *
     * @param variableName the var name
     *
     * @return the effective rank
     */
    public final OptionalInt getVariableEffectiveRank(final String variableName) {

        return this.thisCDF.getVariableByName(variableName)
                .map(Variable::getEffectiveRank)
                .map(OptionalInt::of)
                .orElseGet(OptionalInt::empty);
    }

    /**
     * Return element count for this variable's dimensions.
     *
     * @param variableName the var name
     *
     * @return the element count
     *
     * @throws ReaderError the reader error
     *
     * @deprecated
     */
    @Deprecated
    public final Vector<Integer> getElementCount(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return getDimensionElementCounts(variableName).stream()
                .collect(Collectors.toCollection(Vector::new));
    }

    /**
     * Returns {@link GlobalAttribute GlobalAttribute} object for
     * the named global attribute.
     *
     * @param atr the atr
     *
     * @return the global attribute
     *
     * @throws ReaderError the reader error
     */
    public final GlobalAttribute getGlobalAttribute(final String atr) throws CDFException.ReaderError {

        try {
            return this.thisCDF.getGlobalAttribute(atr);
        }
        catch (RuntimeException th) {
            throw new CDFException.ReaderError("getGlobalAttribute failed for " + atr, th);
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
     * @return the last leap second id
     */
    public final int getLastLeapSecondId() {
        return this.thisCDF.lastLeapSecondId;
    }

    /**
     * Returns given variable's number property.
     *
     * @param variableName the var name
     *
     * @return the number
     *
     * @throws ReaderError the reader error
     */
    public final int getNumber(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getNumber();
    }

    /**
     * Returns given variable's 'number of elements' property.
     *
     * @param variableName the var name
     *
     * @return the number of elements
     *
     * @throws ReaderError the reader error
     */
    public final int getNumberOfElements(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getNumberOfElements();
    }

    /**
     * Returns 'number of values' property of the given variable.
     *
     * @param variableName the var name
     *
     * @return the number of values
     *
     * @throws ReaderError the reader error
     */
    public final int getNumberOfValues(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getNumberOfValues();
    }

    /**
     * Returns 'pad value' property of the given variable.
     *
     * @param variableName the var name
     *
     * @return the pad value
     *
     * @throws ReaderError the reader error
     */
    public final Object getPadValue(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getPadValue();
    }

    /**
     * Returns 'pad value' property of the given variable subject to the given
     * precision preservation constraint.
     *
     * @param variableName      the var name
     * @param preservePrecision the preserve precision
     *
     * @return the pad value
     *
     * @throws ReaderError the reader error
     */
    public final Object getPadValue(final String variableName, final boolean preservePrecision)
            throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        Variable variable = this.thisCDF.getVariable(variableName);
        return variable.getPadValue(preservePrecision);
    }

    /**
     * Returns record range for this variable.
     *
     * @param variableName the var name
     *
     * @return the record range
     *
     * @throws ReaderError the reader error
     */
    public final int[] getRecordRange(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getRecordRange();
    }

    /**
     * Returns the name of the time variable for the given variable.
     *
     * @param variableName variable name
     *
     * @return String
     */
    // FIXME: This method relies on userTimeVariableName which is broken
    public final String getTimeVariableName(final String variableName) {

        if (!existsVariable(variableName)) {
            throw new IllegalArgumentException("CDF does not hava a variable named " + variableName);
        }

        try {
            String tname = userTimeVariableName(variableName);

            if (tname != null) {
                return tname;
            }

        }
        catch (ReaderError e) {
            throw new IllegalArgumentException("CDF does not hava a variable named " + variableName, e);
        }

        // assume istp convention
        Variable variable = this.thisCDF.getVariable(variableName);
        String vname = variable.getName();

        List<String> v = (List<String>) this.thisCDF.getAttribute(vname, "DEPEND_0");

        String tname = null;

        if (!v.isEmpty()) {
            tname = v.get(0);
        }

        if (tname == null) {

            if (!"Epoch".equals(vname)) {

                if (this.thisCDF.getVariable("Epoch") != null) {
                    tname = "Epoch";
                    LOGGER.fine("Variable " + vname + " has no DEPEND_0 attribute. Variable named Epoch "
                            + "assumed to be the right time variable");
                } else {
                    throw new IllegalArgumentException("Time variable not found for " + vname);
                }

            } else {
                throw new IllegalArgumentException("Variable named Epoch has no DEPEND_0 attribute.");
            }

        }

        return tname;
    }

    /**
     * Returns CDF type of the variable.
     *
     * @param variableName the var name
     *
     * @return the type
     *
     * @throws ReaderError the reader error
     */
    public final int getType(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getType();
    }

    /**
     * Returns names of variables in the CDF.
     *
     * @return the variable names
     */
    public final String[] getVariableNames() {
        return this.thisCDF.getVariableNames();
    }

    /**
     * returns variable names of a given VAR_TYPE in a String[].
     *
     * @param type the type
     *
     * @return the variable names
     */
    public final String[] getVariableNames(final String type) {
        return this.thisCDF.getVariableNames(type);
    }

    /**
     * Returns 'varys' property of the given variable.
     *
     * @param variableName the var name
     *
     * @return the varys
     *
     * @throws ReaderError the reader error
     */
    public final boolean[] getVarys(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .getVarys();
    }

    /**
     * Returns number of entries for the named global attribute.
     *
     * @param atr the atr
     *
     * @return the int
     *
     * @throws ReaderError the reader error
     */
    public final int globalAttributeEntryCount(final String atr) throws CDFException.ReaderError {
        return getGlobalAttribute(atr).getEntryCount();
    }

    /**
     * Returns names of global attributes.
     *
     * @return the string[]
     */
    public final String[] globalAttributeNames() {
        return this.thisCDF.globalAttributeNames();
    }

    /**
     * Returns names of global attributes.
     *
     * @return the string[]
     */
    public final String[] allAttributeNames() {
        return this.thisCDF.allAttributeNames();
    }

    /**
     * returns whether conversion of this variable to type specified by
     * cl is supported while preserving precision.
     *
     * @param variableName the var name
     * @param cl           the cl
     *
     * @return true, if is compatible
     *
     * @throws ReaderError the reader error
     */
    public final boolean isCompatible(final String variableName, final Class<?> cl) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        try {
            return this.thisCDF.getVariable(variableName)
                    .isCompatible(cl);
        }
        catch (RuntimeException th) {
            throw new CDFException.ReaderError("isCompatible failed for " + variableName + " class " + cl, th);
        }

    }

    /**
     * Returns whether conversion of this variable to type specified by
     * cl is supported under the given precision preserving constraint.
     *
     * @param variableName the var name
     * @param cl           the cl
     * @param preserve     the preserve
     *
     * @return true, if is compatible
     *
     * @throws ReaderError the reader error
     */
    public final boolean isCompatible(final String variableName, final Class<?> cl, final boolean preserve)
            throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        Variable variable = this.thisCDF.getVariable(variableName);
        return variable.isCompatible(cl, preserve);
    }

    /**
     * Returns whether the values of the variable are represented in a
     * compressed form in the CDF.For variables declared to be compressed, CDF
     * specification allows
     * the values to be stored in uncompressed form if the latter results in
     * a smaller size.
     *
     * @param variableName the var name
     *
     * @return true, if is compressed
     *
     * @throws ReaderError the reader error
     */
    public final boolean isCompressed(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .isCompressed();
    }

    /**
     * Shows whether one or more records (in the range returned by
     * getRecordRange()) are missing.
     *
     * @param variableName the var name
     *
     * @return true, if is missing records
     *
     * @throws ReaderError the reader error
     */
    public final boolean isMissingRecords(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .isMissingRecords();
    }

    /**
     * Returns whether the given variable represents time.
     *
     * @param variableName the var name
     *
     * @return true, if is time type
     *
     * @throws ReaderError the reader error
     */
    public final boolean isTimeType(final String variableName) throws CDFException.ReaderError {

        return this.thisCDF.findVariableByName(variableName)
                .map(variable -> {

                    int type = variable.getType();

                    return (CDFTimeType.EPOCH.getValue() == type) || (CDFTimeType.EPOCH16.getValue() == type)
                        || (CDFTimeType.TT2000.getValue() == type);
                })
                .orElseThrow(() -> new CDFException.ReaderError("CDF does not hava a variable named " + variableName));

    }

    /**
     * Returns whether a variable of type r-variable..See the CDF User's Guide for
     * details.
     *
     * @param variableName the var name
     *
     * @return true, if is type R
     *
     * @throws ReaderError the reader error
     */
    public final boolean isTypeR(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .isTypeR();
    }

    /**
     * Return whether the missing record should be assigned the pad
     * value.
     *
     * @param variableName the var name
     *
     * @return true, if successful
     *
     * @throws ReaderError the reader error
     */
    public final boolean missingRecordValueIsPad(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        Variable variable = this.thisCDF.getVariable(variableName);
        return variable.missingRecordValueIsPad();
    }

    /**
     * Return whether the missing record should be assigned the last
     * seen value.If none has been seen, pad value is assigned.
     *
     * @param variableName the var name
     *
     * @return true, if successful
     *
     * @throws ReaderError the reader error
     */
    public final boolean missingRecordValueIsPrevious(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        Variable variable = this.thisCDF.getVariable(variableName);
        return variable.missingRecordValueIsPrevious();
    }

    /**
     * Returns an indication of the record varying property of a variable.
     *
     * @param variableName the var name
     *
     * @return false if variable has a constant value for this CDF.
     *
     * @throws ReaderError the reader error
     */
    public final boolean recordVariance(final String variableName) throws CDFException.ReaderError {

        if (!existsVariable(variableName)) {
            throw new CDFException.ReaderError("CDF does not hava a variable named " + variableName);
        }

        return this.thisCDF.getVariable(variableName)
                .recordVariance();
    }

    /**
     * Returns whether the arrays are stored in row major order in the source.
     *
     * @return true, if successful
     */
    public final boolean rowMajority() {
        return this.thisCDF.rowMajority();
    }

    /**
     * User time variable name.
     *
     * @param variableName the var name
     *
     * @return the string
     *
     * @throws ReaderError the reader error
     */
    public abstract String userTimeVariableName(String variableName) throws CDFException.ReaderError;

    /**
     * Returns names of attributes of the given variable.
     *
     * @param variableName the variable name
     *
     * @return the string[] or null
     */
    public final String[] variableAttributeNames(final String variableName) {
        return this.thisCDF.variableAttributeNames(variableName);
    }
}
