package cn.edu.njnu.mapper;

import cn.edu.njnu.pojo.Folder;
import cn.edu.njnu.pojo.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface FavoriteMapper {
    List<Map> folder(String username);
    ArrayList<Resource> collection(String folderID);
    ArrayList<Map> collectionStr(String folderID);
    ArrayList<Map> key(String folderID);
    ArrayList<Map> goal(String folderID);
    boolean createFolder(String id, String name, String introduction, String username, long date);
    boolean updateFolder(String id, String name, String introduction);
    Folder queryFolder(String id);
    boolean putInFolder(int resourceID, String folderID, long date);
    boolean putGoal(int goal, String folderID, long date);
    boolean putKey(int key, String folderID, long date);
    Map queryCollection(int resourceID, String folderID);
    boolean putInFolderStr(String content, String folderID, long date);
    Map queryCollectionStr(String content, String folderID);
    int number(String folderID);
    boolean deleteFolder(String folderID);
    boolean deleteResource(String folderID);
    //delete
    boolean delFolderResource(int resourceID, String folderID);
    boolean delFolderContent(String content, String folderID);
    boolean delFolderGoal(int goal, String folderID);
    boolean delFolderKey(int key, String folderID);
    boolean delMulti(String folderID, List<Integer> list);

    ArrayList<Map> queryCurrent(HashMap<String, Object> condition);
    ArrayList<Map> yiyou(HashMap<String, Object> condition);
}
