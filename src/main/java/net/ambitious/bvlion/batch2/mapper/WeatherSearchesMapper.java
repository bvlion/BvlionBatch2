package net.ambitious.bvlion.batch2.mapper;

import net.ambitious.bvlion.batch2.entity.WeatherSearchesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface WeatherSearchesMapper {
	List<WeatherSearchesEntity> selectWeatherSearchList();
}
