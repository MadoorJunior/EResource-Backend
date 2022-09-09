package cn.edu.njnu.mapper;

import cn.edu.njnu.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserMapper {
    // 根据用户名查找用户
    User queryUserByName(String username);
    User queryUserByNameNP(String username);
    // 根据邮箱查找用户
    User queryUserByEmail(String userEmail);
    // 添加用户
    void addUser(User user);
    // 更新用户信息
    void updateUser(User user);
    //获取用户ID
    List<Map> queryUserID();
    //获取用户ID
    List<Map> browseUserID();
    //浏览记录
    List<Map> browseRecord(int user_id);
    List<Map> entityRecord(int user_id);
    void updateRelated(int user_id, String related_user);
    String queryRelatedUser(int user_id);
    User queryUserByID(int user_id);

}
