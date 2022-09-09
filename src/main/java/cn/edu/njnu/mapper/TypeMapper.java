package cn.edu.njnu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface TypeMapper {
    List<Map> period();
    List<Map> subject(String period);

    List<Map> simpleSubject();
}
