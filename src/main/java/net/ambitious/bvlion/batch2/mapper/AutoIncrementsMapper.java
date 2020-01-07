package net.ambitious.bvlion.batch2.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface AutoIncrementsMapper {
    int nextValue(@Param("type") int type);

    int randomValue(@Param("type") int type);

    void insert(@Param("value") int value, @Param("type") int type);
}
