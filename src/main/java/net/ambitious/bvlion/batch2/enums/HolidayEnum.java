package net.ambitious.bvlion.batch2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HolidayEnum {
	明日は休み(1),
	明日は仕事(2);

	private final int status;
}
