package gov.nasa.gsfc.spdf.cdfj.istpiacg;


/**
 * The Class IstpIacgVariableAttributes.
 */
public final class IstpIacgVariableAttributes {

    interface IstpIacgVariableAttribute {

        boolean isRequired();

        String getAttributeName();
    }
}
