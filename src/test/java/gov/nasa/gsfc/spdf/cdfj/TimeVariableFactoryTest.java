package gov.nasa.gsfc.spdf.cdfj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class TimeVariableFactoryTest {

    @Test
    void test() {
        assertEquals(8.64e7, TimeVariableFactory.MILLISECONDS_IN_A_DAY_AS_DOUBLE);
        assertEquals(42_184, TimeVariableFactory.DIFF_TIA_UTC_JANUARY_1_1972);

    }

}
