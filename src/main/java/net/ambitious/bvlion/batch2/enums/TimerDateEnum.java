package net.ambitious.bvlion.batch2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Calendar;

@RequiredArgsConstructor
public enum TimerDateEnum {

	SUNDAY(Calendar.SUNDAY, "sun_started_flag"),
	MONDAY(Calendar.MONDAY, "mon_started_flag"),
	TUESDAY(Calendar.TUESDAY, "tue_started_flag"),
	WEDNESDAY(Calendar.WEDNESDAY, "wed_started_flag"),
	THURSDAY(Calendar.THURSDAY, "thu_started_flag"),
	FRIDAY(Calendar.FRIDAY, "fri_started_flag"),
	SATURDAY(Calendar.SATURDAY, "sat_started_flag");

	private final int week;

	@Getter
	private final String columnName;

	public static String columnName(int week) {
		return Arrays.stream(values())
				.filter(value -> value.week == week)
				.map(value -> value.columnName)
				.findFirst().orElse("");
	}
}
