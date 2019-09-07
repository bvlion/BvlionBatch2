package net.ambitious.bvlion.batch2.mapper;

import net.ambitious.bvlion.batch2.entity.RealtimeSettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface RealtimeSettingMapper {
    RealtimeSettingEntity selectRealtimeSetting();

    void updateAirconMode(@Param("mode") int mode, @Param("temp") float temp);

    void updateMonitoringMode(@Param("mode") int mode);
}
