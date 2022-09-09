package cn.edu.njnu.mapper;

import cn.edu.njnu.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.tomcat.jni.OS;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Mapper
@Repository
public interface RecordMapper {
    void addEntityRecord(int userID, long browseDate, String entityName, String browser, String OS, String ipAddress);
    void addResourceRecord(int userID, long browseDate, int resourceID, String browser, String OS, String ipAddress);

    ArrayList<Map<String, Object>> record(int userID);
    ArrayList<Map<String, Object>> resourceRecord(int userID);
}
