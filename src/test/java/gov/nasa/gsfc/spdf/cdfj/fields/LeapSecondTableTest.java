package gov.nasa.gsfc.spdf.cdfj.fields;

import static gov.nasa.gsfc.spdf.cdfj.fields.LeapSecondTable.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;

import gov.nasa.gsfc.spdf.cdfj.fields.LeapSecondTable.*;

class LeapSecondTableTest {

    @Test
    void testRows() {

        NavigableSet<Row> rows = DEFAULT_LEAP_SECOND_TABLE.rows();

        assertEquals(42, rows.size());
        Row first = rows.first();
        assertEquals(LocalDate.of(1960, 1, 1), first.leapSecondDate());
        assertEquals(1417818, first.leapSecondsAsMicros());
        // assertEquals(1417, first.leapSecondsAsMillis());
        // assertEquals(1417818, first.drift1AsMicros());
        // assertEquals(1417, first.drift1AsMillis());
        Row ls201711 = rows.stream()
                .filter(r -> r.leapSecondDate()
                        .equals(LocalDate.of(2017, 1, 1)))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        assertEquals(LocalDate.of(2017, 1, 1), ls201711.leapSecondDate());
        assertEquals(37_000_000, ls201711.leapSecondsAsMicros());
        assertEquals(37_000, ls201711.leapSecondsAsMillis());
        assertEquals(0, ls201711.drift1AsMicros());
        assertEquals(0, ls201711.drift1AsMillis());
        assertEquals(0, ls201711.drift2AsMicros());
        assertEquals(0, ls201711.drift2AsMillis());
    }

}
