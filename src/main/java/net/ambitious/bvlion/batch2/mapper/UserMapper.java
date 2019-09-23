package net.ambitious.bvlion.batch2.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface UserMapper {
    int fcmUpdate(@Param("user") String user, @Param("fcm_id") String fcmId);

    void userModeUpdate(@Param("user") String user, @Param("mode") int mode);

    int userCount();

    List<String> otherFcmTokens(@Param("user") String user);

    List<String> allUsers();
}
