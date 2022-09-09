package cn.edu.njnu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RoleMapper {
    String getRoleName(Integer roleId);
    String getRoleNameByUserName(String userName);
}
