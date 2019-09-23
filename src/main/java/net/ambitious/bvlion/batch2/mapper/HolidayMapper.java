package net.ambitious.bvlion.batch2.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface HolidayMapper {

    boolean isSetHoliday();

    boolean isHoliday();

    int nextDayStatusCheck();

    void setHoliday(@Param("date") String date, @Param("holiday") int holiday);
}
