package net.ambitious.bvlion.batch2.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimerDateEnumTest {

    @Test
    public void columnNameTest() {
        assertEquals(TimerDateEnum.columnName(0), "");
        assertEquals(TimerDateEnum.columnName(1), "sun_started_flag");
        assertEquals(TimerDateEnum.columnName(2), "mon_started_flag");
        assertEquals(TimerDateEnum.columnName(3), "tue_started_flag");
        assertEquals(TimerDateEnum.columnName(4), "wed_started_flag");
        assertEquals(TimerDateEnum.columnName(5), "thu_started_flag");
        assertEquals(TimerDateEnum.columnName(6), "fri_started_flag");
        assertEquals(TimerDateEnum.columnName(7), "sat_started_flag");
    }
}