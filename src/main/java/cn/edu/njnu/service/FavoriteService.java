package cn.edu.njnu.service;

import cn.edu.njnu.mapper.FavoriteMapper;
import cn.edu.njnu.mapper.ResourceMapper;
import cn.edu.njnu.mapper.UserMapper;
import cn.edu.njnu.mapper.XApiMapper;
import cn.edu.njnu.pojo.Folder;
import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.checkerframework.checker.units.qual.A;
import org.neo4j.driver.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.neo4j.driver.v1.Values.parameters;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private XApiMapper xApiMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    private static Driver driver;

    @Autowired
    public FavoriteService(Driver driver) {
        FavoriteService.driver = driver;
    }

    //生成8位id
    public static  String getUUID()
    {
        String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
                "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
                "6", "7", "8", "9"};
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x24]);
        }
        return shortBuffer.toString();
    }

    //根据username获取收藏夹
    public Result favorite(Map<String, Object> infoMap){
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        JSONObject resObject = new JSONObject();
        JSONArray resArray = new JSONArray();
        List<Map> folderList = favoriteMapper.folder(username);
        for (Map folder:folderList){
            JSONObject singleFolder = new JSONObject();
            String folderID = (String) folder.get("id");
            String folderName = (String) folder.get("name");
            String introduction = (String) folder.get("introduction");
            singleFolder.put("folderID", folderID);
            singleFolder.put("folderName", folderName);
            singleFolder.put("introduction", introduction);
            singleFolder.put("resourceNum", favoriteMapper.number(folderID));
            resArray.add(singleFolder);
        }
        resObject.put("folders", resArray);
        HashMap<String, Object> condition = new HashMap<>();
        condition.put("username", username);
        ArrayList<String> currentFolder = new ArrayList<>();
        if (infoMap.size() == 0){
            resObject.put("currentFolder", currentFolder);
            return ResultFactory.buildSuccessResult("收藏夹获取成功", resArray);
        }
        else if (infoMap.containsKey("resourceID")){
            int resourceID = Integer.parseInt((String) infoMap.get("resourceID"));
            condition.put("resourceID", resourceID);
            ArrayList<Map> folderID = favoriteMapper.queryCurrent(condition);
            for (Map folder:folderID){
                currentFolder.add((String) folder.get("id"));
            }
            resObject.put("currentFolder", currentFolder);
        }
        else if (infoMap.containsKey("content")){
            String content = (String) infoMap.get("content");
            condition.put("content", content);
            ArrayList<Map> folderID = favoriteMapper.queryCurrent(condition);
            for (Map folder:folderID){
                currentFolder.add((String) folder.get("id"));
            }
            resObject.put("currentFolder", currentFolder);
        }
        else if (infoMap.containsKey("goal")){
            int goal = Integer.parseInt((String) infoMap.get("goal"));
            condition.put("goal", goal);
            ArrayList<Map> folderID = favoriteMapper.queryCurrent(condition);
            for (Map folder:folderID){
                currentFolder.add((String) folder.get("id"));
            }
            resObject.put("currentFolder", currentFolder);
        }
        else if (infoMap.containsKey("key")){
            int key = Integer.parseInt((String) infoMap.get("key"));
            condition.put("key", key);
            ArrayList<Map> folderID = favoriteMapper.queryCurrent(condition);
            for (Map folder:folderID){
                currentFolder.add((String) folder.get("id"));
            }
            resObject.put("currentFolder", currentFolder);
        }
        else {
            return ResultFactory.buildFailResult("获取失败");
        }
        return ResultFactory.buildSuccessResult("收藏夹获取成功", resObject);
    }

    //根据收藏夹ID获取资源
    public Result folderResource(String folderID){
        Session session = driver.session();//已关
        JSONObject folder = new JSONObject();
        folder.put("resources", favoriteMapper.collection(folderID));
        ArrayList<Map> contentMap = favoriteMapper.collectionStr(folderID);
        ArrayList<String> content = new ArrayList<>();
        if (contentMap.size()>0){
            for (Map singleContent:contentMap){
                if (singleContent!=null){
                    content.add((String) singleContent.get("content"));
                }
            }
        }
        folder.put("content", contentMap);

        ArrayList<Map> goalMap = favoriteMapper.goal(folderID);
        JSONArray goal = new JSONArray();

        if (goalMap.size()>0){
            for (Map singleGoal:goalMap){
                if (singleGoal!=null){
                    int collectionId = (int) singleGoal.get("collectionId");
                    int id = (int) singleGoal.get("goal");
                    StatementResult node = session.run( "MATCH (n:GoalAndKey) where id(n) = {id} " +
                                    "RETURN n.goal as goal",
                            parameters( "id", id) );
                    if ( node.hasNext() )
                    {
                        Record record = node.next();
                        String text = record.get( "goal" ).asString();
                        JSONObject goalObject = new JSONObject();
                        goalObject.put("id", id);
                        goalObject.put("text", text);
                        goalObject.put("collectionId", collectionId);
                        goal.add(goalObject);
                    }
                }
            }
        }
        folder.put("goal", goal);

        ArrayList<Map> keyMap = favoriteMapper.key(folderID);
        JSONArray key = new JSONArray();
        if (keyMap.size()>0){
            for (Map singleKey:keyMap){
                if (singleKey!=null){
                    int collectionId = (int) singleKey.get("collectionId");
                    int id = (int) singleKey.get("key");
                    StatementResult node = session.run( "MATCH (n:GoalAndKey) where id(n) = {id} " +
                                    "RETURN n.key as key",
                            parameters( "id", id) );
                    if ( node.hasNext() )
                    {
                        Record record = node.next();
                        String text = record.get( "key" ).asString();
                        JSONObject keyObject = new JSONObject();
                        keyObject.put("id", id);
                        keyObject.put("text", text);
                        keyObject.put("collectionId", collectionId);
                        key.add(keyObject);
                    }
                }

            }
        }

        folder.put("key", key);
        session.close();
        return ResultFactory.buildSuccessResult("获取资源成功", folder);
    }
    //用户创建收藏夹
    public Result createFolder(Map<String, Object> infoMap){
        String id = getUUID();
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        String name = (String) infoMap.get("name");
        String introduction = (String) infoMap.get("introduction");
        long date = System.currentTimeMillis();
        if (favoriteMapper.createFolder(id, name, introduction, username, date)){
            Folder folder = favoriteMapper.queryFolder(id);
            return ResultFactory.buildSuccessResult("创建成功",folder);
        }
        return ResultFactory.buildFailResult("创建失败");
    }
    //用户修改收藏夹
    public Result updateFolder(Map<String, Object> infoMap){
        String id = (String) infoMap.get("folderID");
        String name = (String) infoMap.get("name");
        String introduction = (String) infoMap.get("introduction");
        if (favoriteMapper.updateFolder(id, name, introduction)){
            Folder folder = favoriteMapper.queryFolder(id);
            return ResultFactory.buildSuccessResult("创建成功",folder);
        }
        return ResultFactory.buildFailResult("创建失败");
    }
    //根据收藏夹ID删除收藏夹
    public Result deleteFolder(String folderID){
        favoriteMapper.deleteResource(folderID);
        favoriteMapper.deleteFolder(folderID);
        return ResultFactory.buildSuccessResult("删除成功", null);
    }
    //资源加入资源包
    public Result putInFolder(Map<String, Object> IDMap){
        long date = System.currentTimeMillis();
        ArrayList<String> folderIDList = (ArrayList<String>) IDMap.get("addFolderID");
        ArrayList<String> delFolderIDList = (ArrayList<String>) IDMap.get("deleteFolderID");
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        int flag = 0; //判断是否添加成功
        if (IDMap.containsKey("resourceID")){
            int userId = userMapper.queryUserByName(username).getUserId();
            long browseDate = System.currentTimeMillis();

            int resourceID = (int) IDMap.get("resourceID");
            for (String folderID:delFolderIDList){
                favoriteMapper.delFolderResource(resourceID, folderID);
            }
            for (String folderID:folderIDList){
                HashMap<String, Object> condition = new HashMap<>();
                condition.put("username", username);
                condition.put("folderID", folderID);
                condition.put("resourceID", resourceID);
                if (favoriteMapper.yiyou(condition).size() == 0){
                    flag = 1;
                    favoriteMapper.putInFolder(resourceID, folderID, date);
                    String folderName = favoriteMapper.queryFolder(folderID).getName();
                    int resourceType = resourceMapper.queryResourceByID(resourceID).getResourceType();
                    int objectType = resourceType==1?2:3;
                    xApiMapper.addFavorite(userId,resourceID,folderName,objectType,browseDate);
                }
            }
        }
        else if (IDMap.containsKey("goal")){
            int goal = (int) IDMap.get("goal");
            for (String folderID:delFolderIDList){
                favoriteMapper.delFolderGoal(goal, folderID);
            }
            for (String folderID:folderIDList){
                HashMap<String, Object> condition = new HashMap<>();
                condition.put("username", username);
                condition.put("folderID", folderID);
                condition.put("goal", goal);
                if (favoriteMapper.yiyou(condition).size() == 0){
                    flag = 1;
                    favoriteMapper.putGoal(goal, folderID, date);
                }
            }
        }
        else if (IDMap.containsKey("key")){
            int key = (int) IDMap.get("key");
            for (String folderID:delFolderIDList){
                favoriteMapper.delFolderKey(key, folderID);
            }
            for (String folderID:folderIDList){
                HashMap<String, Object> condition = new HashMap<>();
                condition.put("username", username);
                condition.put("folderID", folderID);
                condition.put("key", key);
                if (favoriteMapper.yiyou(condition).size() == 0){
                    flag = 1;
                    favoriteMapper.putKey(key, folderID, date);
                }
            }
        }
        else {
            String content = (String) IDMap.get("content");
            for (String folderID:delFolderIDList){
                favoriteMapper.delFolderContent(content, folderID);
            }
            for (String folderID:folderIDList){
                HashMap<String, Object> condition = new HashMap<>();
                condition.put("username", username);
                condition.put("folderID", folderID);
                condition.put("content", content);
                if (favoriteMapper.yiyou(condition).size() == 0){
                    flag = 1;
                    favoriteMapper.putInFolderStr(content, folderID, date);
                }
            }
        }
        if (flag == 1){
            return ResultFactory.buildSuccessResult("添加成功", null);
        }
        else {
            return ResultFactory.buildFailResult("添加失败");
        }
    }
    //删除资源包资源
    public Result delSingle(Map<String, Object> IDMap){
        String folderID = (String) IDMap.get("folderID");
        int flag = 0; //判断是否添加成功
        if (IDMap.containsKey("resourceID")){
            int resourceID = (int) IDMap.get("resourceID");
            if (favoriteMapper.delFolderResource(resourceID, folderID)){
                flag = 1;
            }
        }
        else if (IDMap.containsKey("goal")){
            int goal = (int) IDMap.get("goal");
            if (favoriteMapper.delFolderGoal(goal, folderID)){
                flag = 1;
            }
        }
        else if (IDMap.containsKey("key")){
            int key = (int) IDMap.get("key");
            if (favoriteMapper.delFolderKey(key, folderID)){
                flag = 1;
            }
        }
        else {
            String content = (String) IDMap.get("content");
            if (favoriteMapper.delFolderContent(content, folderID)){
                flag = 1;
            }
        }
        if (flag == 1){
            return ResultFactory.buildSuccessResult("删除成功", null);
        }
        else {
            return ResultFactory.buildFailResult("删除失败");
        }
    }

    public Result delMulti(Map<String, Object> IDMap) {
        String folderID = (String) IDMap.get("folderID");
        List<Integer> resourceList = (List<Integer>) IDMap.get("resourceIDs");
        boolean result = favoriteMapper.delMulti(folderID, resourceList);
        return result?ResultFactory.buildSuccessResult("删除成功",null):
                ResultFactory.buildFailResult("删除失败");
    }
}
