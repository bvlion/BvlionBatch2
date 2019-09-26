package net.ambitious.bvlion.batch2.mapper;

import net.ambitious.bvlion.batch2.entity.TimerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface TimerDataMapper {
	List<TimerEntity> selectExecTimerSetting(@Param("exec_started_flag") String execStartedFlagName, @Param("do_exec_time") String doExecTime);
}
