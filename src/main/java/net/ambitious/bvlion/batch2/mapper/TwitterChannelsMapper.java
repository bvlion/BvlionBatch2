package net.ambitious.bvlion.batch2.mapper;

import net.ambitious.bvlion.batch2.entity.TwitterChannelsDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface TwitterChannelsMapper {
	List<TwitterChannelsDataEntity> selectEnableChannels(@Param("jorudan_flag") int jorudanFlag);
}
