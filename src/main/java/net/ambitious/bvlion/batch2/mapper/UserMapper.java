package net.ambitious.bvlion.batch2.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface UserMapper {
    int fcmUpdate(@Param("user") String user);

    void userModeUpdate(@Param("user") String user, @Param("ip") String ip, @Param("mode") int mode);

    void homeIpUpdate(@Param("ip") String ip);

    int userCount();

    List<String> fcmSendUsers(@Param("user") String user);

    List<String> allUsers();

    int userInHome(@Param("user") String user);
}
