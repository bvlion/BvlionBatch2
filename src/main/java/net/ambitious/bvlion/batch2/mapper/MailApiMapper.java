package net.ambitious.bvlion.batch2.mapper;

import net.ambitious.bvlion.batch2.entity.MailApiEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MailApiMapper {
    List<MailApiEntity> selectTarget();
}
