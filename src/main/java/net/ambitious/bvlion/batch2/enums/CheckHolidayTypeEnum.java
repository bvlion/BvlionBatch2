package net.ambitious.bvlion.batch2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CheckHolidayTypeEnum {
    HOME_HOLIDAY_CHECK(1),
    NORMAL_HOLIDAY_CHECK(2);

    private final int type;
}
