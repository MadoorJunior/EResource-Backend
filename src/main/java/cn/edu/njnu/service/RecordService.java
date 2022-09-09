package cn.edu.njnu.service;

import cn.edu.njnu.mapper.RecordMapper;
import cn.edu.njnu.mapper.ResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class RecordService {

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    public void addRecord(Map recordMap){
        String browser = (String) recordMap.get("browser");
        String OS = (String) recordMap.get("OS");
        String ipAddress = (String) recordMap.get("ipAddress");
        long browseDate = System.currentTimeMillis();
        if (recordMap.containsKey("resourceID")){
            int resourceID = (int) recordMap.get("resourceID");
            int browse = resourceMapper.queryResourceByID(resourceID).getBrowse() + 1;
            resourceMapper.updateBrowse(browse, resourceID);
        }
        if (recordMap.containsKey("userId")){
            int userID = (int) recordMap.get("userId");
            if (recordMap.containsKey("entityName")){
                String entityName = (String) recordMap.get("entityName");
                recordMapper.addEntityRecord(userID, browseDate, entityName, browser, OS, ipAddress);
            }
            if (recordMap.containsKey("resourceID")){
                int resourceID = (int) recordMap.get("resourceID");
                recordMapper.addResourceRecord(userID, browseDate, resourceID, browser, OS, ipAddress);
            }
        }
    }
}
