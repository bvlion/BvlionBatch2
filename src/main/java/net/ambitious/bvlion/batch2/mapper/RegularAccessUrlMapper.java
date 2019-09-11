package net.ambitious.bvlion.batch2.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface RegularAccessUrlMapper {
	List<String> regularAccessUrls();
}
