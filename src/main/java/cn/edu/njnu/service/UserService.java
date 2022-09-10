package cn.edu.njnu.service;

import cn.edu.njnu.dto.UserRegisterDto;
import cn.edu.njnu.mapper.RecordMapper;
import cn.edu.njnu.mapper.ResourceMapper;
import cn.edu.njnu.mapper.UserMapper;
import cn.edu.njnu.pojo.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.neo4j.driver.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.neo4j.driver.v1.Values.parameters;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private static Driver driver;


    public Integer getUserPermission(Integer userId){
        return userMapper.queryUserByID(userId).getUserType();
    }
    @Autowired
    public UserService(Driver driver) {
        UserService.driver = driver;
    }

    public User getByName(String username) {
        return userMapper.queryUserByName(username);
    }

    public User getByEmail(String userEmail) {
        return userMapper.queryUserByEmail(userEmail);
    }

    public User getByNameNoPassword(String username) {
        return userMapper.queryUserByNameNP(username);
    }

    public boolean isExist(String username, String userEmail) {
        User user = getByName(username);
        if (user == null) {
            user = getByEmail(userEmail);
        }
        return null != user;
    }

    public void addUser(User user) {
        userMapper.addUser(user);
    }

    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    public boolean register(UserRegisterDto user){
        boolean exist = isExist(user.getUsername(), user.getEmail());
        if (exist) {
            return false;
        }

        HashMap<String, String> securityInfo = generateSaltAndPassword(user.getPassword());
        User pojoUser = new User();
        pojoUser.setSalt(securityInfo.get("salt"))
                .setUsername(user.getUsername())
                .setUserEmail(user.getEmail())
                .setUserPassword(securityInfo.get("password"))
                .setUserType(user.getUserType())
                .setPeriod(user.getPeriod())
                .setGrade(user.getGrade())
                .setAvatar("default-avatar.jpg")
                .setSchool(user.getSchool());
        addUser(pojoUser);
        int userID = userMapper.queryUserByName(pojoUser.getUsername()).getUserId();
        Session session = driver.session();//已关
        session.run( "create (n:user { id: {userID} }) return n;",
                parameters( "userID", userID) );
        session.close();
        return true;
    }
    public boolean register(User user) {
        boolean exist = isExist(user.getUsername(), user.getUserEmail());
        if (exist) {
            return false;
        }

        HashMap<String, String> securityInfo = generateSaltAndPassword(user.getUserPassword());
        String salt = securityInfo.get("salt");
        String encodedPassword = securityInfo.get("password");
        // 储存用户信息
        user.setSalt(salt);
        user.setUserPassword(encodedPassword);
        user.setUserType(0);
        user.setAvatar("default-avatar.jpg");
        addUser(user);
        int userID = userMapper.queryUserByName(user.getUsername()).getUserId();
        Session session = driver.session();//已关
        session.run( "create (n:user { id: {userID} }) return n;",
                parameters( "userID", userID) );
        session.close();
        return true;
    }

    public HashMap<String, String> generateSaltAndPassword(String password) {
        HashMap<String, String> securityInfo = new HashMap<String, String>();
        // 生成盐
        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        // 设置Hash算法迭代次数
        int times = 2;
        // 得到Hash后的密码
        String encodedPassword = new SimpleHash("md5", password, salt, times).toString();
        securityInfo.put("salt", salt);
        securityInfo.put("password", encodedPassword);
        return securityInfo;
    }

    public Result modifyUserInfo(User requestUser) {
        User queryUser = getByEmail(requestUser.getUserEmail());
        if (null != queryUser && !queryUser.getUsername().equals(requestUser.getUsername())) {
            return ResultFactory.buildFailResult("邮箱已被注册");
        }

        HashMap<String, String> securityInfo = generateSaltAndPassword(requestUser.getUserPassword());
        String salt = securityInfo.get("salt");
        String encodedPassword = securityInfo.get("password");
        // 储存用户信息
        requestUser.setSalt(salt);
        requestUser.setUserPassword(encodedPassword);
        updateUser(requestUser);
        return ResultFactory.buildSuccessResult("修改成功", null);
    }


    //推荐算法 UPDATE 2021-4-7
    public Result relatedUser(){
        List<Map> userIdList = userMapper.browseUserID();
        JSONArray resArray = new JSONArray();
        int entityNum = 0;
        for(Map userIDMap : userIdList){
            int userID = (int) userIDMap.get("user_id");
            JSONObject userRecord = new JSONObject();
            userRecord.put("id", userID);
            List<Map> record = userMapper.browseRecord(userID);
//            List<Map> record = userMapper.entityRecord(userID);
            if (record.size()==0){ //如果用户没有记录就跳过
                continue;
            }
            JSONArray resourceList = new JSONArray();
            Session session = driver.session();//已关
            for (Map singleRecord : record){
                int resourceID = (int) singleRecord.get("resource_id");
                resourceList.add(resourceID);
//                String entityName = (String) singleRecord.get("entity_name");
//                StatementResult result = session.run( "MATCH (a:concept) where a.name = {name} " +
//                                "RETURN ID(a) as ID",
//                        parameters( "name", entityName) );
//                if (result.hasNext()){
//                    Record recordID = result.next();
//                    int entityID = recordID.get( "ID" ).asInt();
//                    resourceList.add(entityID);
//                }
            }
            session.close();
            userRecord.put("record", resourceList);
            entityNum = resourceList.size() > entityNum ? resourceList.size() : entityNum;  //获取最大列数
            if (resourceList.size()!=0){
                resArray.add(userRecord);
            }

        }
        int row = resArray.size();
        int col = 1 + entityNum;  //数组的列数=用户ID（1个）+记录数
        int[][] s = new int[row][col];
        for (int i = 0; i < resArray.size(); i++)
        {
            int userID = (int) resArray.getJSONObject(i).get("id");
            s[i][0] = userID;
            List<Integer> resourceList = (List<Integer>) resArray.getJSONObject(i).get("record");
            for (int j = 0; j < resourceList.size(); j++){
                int resourceID = resourceList.get(j);
                s[i][j+1] = resourceID;
            }
        }
        return relatedUser(s,s.length,col); //col 是数组的列数
    }
    public Result relatedUser(int[][]user_item,int N,int col){
        Map<Integer, Integer> userID = new HashMap<Integer, Integer>();
        //辅助存储每一个用户的用户ID映射
        Map<Integer, Integer> idUser = new HashMap<Integer, Integer>();
        //辅助存储每一个ID对应的用户映射
        Map<String, Double> user_user = new HashMap<String, Double>();//用户与用户的相似度
        int[][] sparseMatrix = new int[N][N];
        //建立用户稀疏矩阵，用于用户相似度计算【相似度矩阵】
        Map<Integer, Integer> userItemLength = new HashMap<Integer, Integer>();
        //存储每一个用户对应的不同资源总数 eg: A 3
        Map<Integer, Set<Integer>> itemUserCollection = new HashMap<Integer, Set<Integer>>();
        //建立资源到用户的倒排表 eg: a A B
        Set<Integer> items = new HashSet<Integer>();//set是无序的 且不能重复
        //辅助存储资源集合
        for (int i = 0; i < N ; i++){
            //获取非0长度
            int length = 0;
            for (int k = 0; k < col ; k++){
                if (user_item[i][k]==0){
                    break;
                }
                length++;
            }
            //依次处理N个用户 输入数据 以空格间隔  A a b d
            userItemLength.put(user_item[i][0], length-1);//A用户 喜欢3个产品
            //eg: A 3
            userID.put(user_item[i][0], i);  //15 0 ,16 1
            //用户ID与稀疏矩阵建立对应关系
            idUser.put(i, user_item[i][0]);// 0 15 ,1 16
            for (int j = 1; j < length; j++){ //j= a b d
                if(items.contains(user_item[i][j])){
                    //如果资源集合中已经包含对应的物品--用户映射，直接添加对应的用户
                    itemUserCollection.get(user_item[i][j]).add(user_item[i][0]);//将A添加到 a b d 中
                } else{
                    //否则创建对应资源--用户集合映射
                    items.add(user_item[i][j]);
                    itemUserCollection.put(user_item[i][j], new HashSet<Integer>());
                    //创建资源--用户倒排关系
                    itemUserCollection.get(user_item[i][j]).add(user_item[i][0]);
                }
            }
        }
        //计算相似度矩阵【稀疏】
        Set<Map.Entry<Integer, Set<Integer>>> entrySet = itemUserCollection.entrySet();
        Iterator<Map.Entry<Integer, Set<Integer>>> iterator = entrySet.iterator();
        while(iterator.hasNext()){//遍历itemUserCollection
            Set<Integer> commonUsers = iterator.next().getValue();
            for (Integer user_u : commonUsers) {
                for (Integer user_v : commonUsers) {
                    if(user_u.equals(user_v)){
                        continue;
                    }
                    sparseMatrix[userID.get(user_u)][userID.get(user_v)] += 1;
                    //计算用户u与用户v都有正反馈的资源总数
                }
            }
        }
        Session session = driver.session();//已关
        session.run("MATCH (a:user)-[r]->(b:user) delete r ");
        session.run("MATCH (a:user) delete a ");
        for (int k = 0; k <userItemLength.size() ; k++) { //1-3
            int recommendUserId =idUser.get(k);
            JSONArray userArray = new JSONArray();  //存用户id与权重，便于后续排序
            for (int j = 0; j <sparseMatrix.length; j++) {   //遍历每一行
                if (k!= j) {
                    double xsduser=sparseMatrix[k][j] / Math.sqrt(userItemLength.get(idUser.get(j)) * userItemLength.get(idUser.get(k)));
                    System.out.println("用户id"+recommendUserId+ "--" +"用户id"+idUser.get(j) + "相似度:" +(0.58566926484624622+xsduser));
                    String temp=recommendUserId+" "+idUser.get(j);
                    user_user.put(temp,xsduser);
                    JSONObject userWeight = new JSONObject();
                    userWeight.put("userID", idUser.get(j));
                    userWeight.put("weight", xsduser);
                    userArray.add(userWeight);
                    StatementResult result_1 = session.run( "MATCH (a:user) where a.id = {id} " +
                                    "RETURN a.id",
                            parameters( "id", recommendUserId) );

                    if (!result_1.hasNext()){
                        session.run( "CREATE (a:user { id: {id} })",
                                parameters( "id", recommendUserId) );
                    }
                    StatementResult result_2 = session.run( "MATCH (a:user) where a.id = {id} " +
                                    "RETURN a.id",
                            parameters( "id", idUser.get(j)) );
                    if (!result_2.hasNext()){
                        session.run( "CREATE (a:user { id: {id} })",
                                parameters( "id", idUser.get(j)) );
                    }
//                    session.run("MATCH (a:user)-[r]->(b:user) " +
//                            "WHERE a.id = " + idUser.get(j) + " AND b.id = " + recommendUserId
//                            + " delete r");
                    session.run("MATCH (a:user), (b:user) " +
                            "WHERE a.id = " + idUser.get(j) + " AND b.id = " + recommendUserId
                            + " CREATE (a)-[:similarity{weight:" + xsduser + "}]->(b)");

                }//相同的个数/各自喜欢的个数乘机开根号
            }
            int userLength = userArray.size();
            double[] weightList = new double[userLength];  //用户权重数组
            int[] userList = new int[userLength];  //用户ID数组
            for (int i = 0;i < userLength;i++){  //循环，根据权重对用户ID进行排序
                int uid = (int) userArray.getJSONObject(i).get("userID");
                double weight = (double) userArray.getJSONObject(i).get("weight");
                for (int j = 0; j < userLength; j++){
                    if (weight > weightList[j]){
                        for (int l = userLength-1;l>j;l--){
                            weightList[l] = weightList[l-1];
                            userList[l] = userList[l-1];
                        }
                        weightList[j]=weight;
                        userList[j] = uid;
                        break;
                    }
                }
            }
            String list = new String();  //生成相关用户字符串
            int userNum = 10;  //只保存最相似的十个用户ID
            userNum = userNum >= userLength ? userLength : userNum;  //判断个数是否够10个
            for (int ui = 0; ui < userNum; ui++){
                list += userList[ui] + "#";
            }
            userMapper.updateRelated(recommendUserId,list);
        }
        session.close();
        return ResultFactory.buildSuccessResult("相似用户更新成功",null);
    }
    public Result recommend(Map<String, Object> userIDMap) {
        int recommendUserID = Integer.parseInt((String)userIDMap.get("userId"));
        ArrayList<Integer> userList = new ArrayList<Integer>();
        userList.add(recommendUserID);
        JSONArray resArray = new JSONArray();
        Session session = driver.session();//已关
        StatementResult userResult = session.run( "MATCH (n:user)-[r]->(m:user) where n.id={userID} and r.weight <> 0 " +
                        "RETURN m.id as ID order by r.weight desc LIMIT 10",
                parameters( "userID", recommendUserID) );
        while (userResult.hasNext()){
            Record recordID = userResult.next();
            int userID = recordID.get( "ID" ).asInt();
            userList.add(userID);
        }
        if(userList.size()==1){
            List<Map> record = userMapper.browseRecord(recommendUserID);
            List<Map> entityRecord = userMapper.entityRecord(recommendUserID);
            JSONArray res = new JSONArray();
            ArrayList<Resource> resourceList = resourceMapper.queryHot2();
            ArrayList<Integer> quchong = new ArrayList<>();
            if (record.size()!=0){
                for(Map recordMap:record){
                    int resourceID = (int) recordMap.get("resource_id");
                    StatementResult result = session.run( "MATCH (a:resource)-[r]->(b:resource) where a.id = {resourceID} " +
                                    "RETURN b.id as ID order by r.weight desc",
                            parameters( "resourceID", resourceID) );
                    while (result.hasNext()){
                        Record recordID = result.next();
                        int weightID = recordID.get( "ID" ).asInt();

                        if (!quchong.contains(weightID)){
                            res.add(resourceMapper.queryResourceByID(weightID));
                            quchong.add(weightID);
                        }

                        if(res.size()==8){
                            return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                        }
                    }
                }
                for(Resource resource:resourceList){
                    res.add(resource);
                    if (res.size()==8){
                        return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                    }
                }
            }
            else if (entityRecord.size()!=0){
                for(Map recordMap:entityRecord){
                    String entityName = (String) recordMap.get("entity_name");
                    StatementResult result = session.run( "MATCH (a:resource)-[r]->(b:concept) where a.id = {entityName} " +
                                    "RETURN a.id as ID order by r.weight desc",
                            parameters( "entityName", entityName) );
                    while (result.hasNext()){
                        Record recordID = result.next();
                        int weightID = recordID.get( "ID" ).asInt();
                        if (!res.contains(resourceMapper.queryResourceByID(weightID))){
                            res.add(resourceMapper.queryResourceByID(weightID));
                        }
                        if(res.size()==8){
                            return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                        }
                    }
                }
                for(Resource resource:resourceList){
                    res.add(resource);
                    if (res.size()==8){
                        return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                    }
                }
            }
            else {
                return ResultFactory.buildSuccessResult("获取推荐资源成功", resourceList);
            }
        }
        int col = 0;
        for(int i=0;i < userList.size();i++){
            int userID = userList.get(i);
            JSONObject userRecord = new JSONObject();
            userRecord.put("id", userID);
            List<Map> record = userMapper.browseRecord(userID);
            if (record.size()==0){
                continue;
            }
            JSONArray resourceList = new JSONArray();
            for (Map singleRecord : record){
                int resourceID = (int) singleRecord.get("resource_id");
                resourceList.add(resourceID);
            }
            userRecord.put("record", resourceList);
            col = resourceList.size() > col ? resourceList.size() : col;
            resArray.add(userRecord);
        }
        int row = resArray.size();
        int[][] s = new int[row][col + 1];
        for (int i = 0; i < resArray.size(); i++)
        {
            int userID = (int) resArray.getJSONObject(i).get("id");
            s[i][0] = userID;
            List<Integer> resourceList = (List<Integer>) resArray.getJSONObject(i).get("record");
            for (int j = 0; j < resourceList.size(); j++){
                int resourceID = resourceList.get(j);
                s[i][j+1] = resourceID;
            }
        }
        session.close();
        return userxsd(s,s.length,recommendUserID);
    }

    public Result userxsd(int[][]user_item,int N,int recommendID) {   //1 4 5 6 7 （第一个代表用户的id 后面的代表浏览过的资源id）        N个用户
        /**
         * 输入用户-->物品条目 一个用户对应多个物品
         * 用户ID 物品ID集合
         * 1  2 3
         * 2  2 3 4
         * 3  2 4 5 7
         */
        Map<Integer, Integer> userID = new HashMap<Integer, Integer>();
        //辅助存储每一个用户的用户ID映射
        Map<Integer, Integer> idUser = new HashMap<Integer, Integer>();
        //辅助存储每一个ID对应的用户映射
        Map<String, Double> user_user = new HashMap<String, Double>();//用户与用户的相似度
        Map<String, Double> user_entity = new HashMap<String, Double>();//资源——用户的推荐度
        int[][] sparseMatrix = new int[N][N];
        //建立用户稀疏矩阵，用于用户相似度计算【相似度矩阵】
        Map<Integer, Integer> userItemLength = new HashMap<Integer, Integer>();
        //存储每一个用户对应的不同资源总数 eg: A 3
        Map<Integer, Set<Integer>> itemUserCollection = new HashMap<Integer, Set<Integer>>();
        //建立资源到用户的倒排表 eg: a A B
        Set<Integer> items = new HashSet<Integer>();//set是无序的 且不能重复
        //辅助存储资源集合
        for (int i = 0; i < N ; i++){
            //获取非0长度
            int length = 0;
            for (int k = 0; k < N ; k++){
                if (user_item[i][k]!=0){
                    length++;
                    continue;
                }
                break;
            }
            //依次处理N个用户 输入数据 以空格间隔  A a b d
            userItemLength.put(user_item[i][0], length-1);//A用户 喜欢3个产品
            //eg: A 3
            userID.put(user_item[i][0], i);  //15 0 ,16 1
            //用户ID与稀疏矩阵建立对应关系
            idUser.put(i, user_item[i][0]);// 0 15 ,1 16
            for (int j = 1; j < length; j++){ //j= a b d
                if(items.contains(user_item[i][j])){
                    //如果资源集合中已经包含对应的物品--用户映射，直接添加对应的用户
                    itemUserCollection.get(user_item[i][j]).add(user_item[i][0]);//将A添加到 a b d 中
                } else{
                    //否则创建对应资源--用户集合映射
                    items.add(user_item[i][j]);
                    itemUserCollection.put(user_item[i][j], new HashSet<Integer>());
                    //创建资源--用户倒排关系
                    itemUserCollection.get(user_item[i][j]).add(user_item[i][0]);
                }
            }
        }
        //计算相似度矩阵【稀疏】
        Set<Map.Entry<Integer, Set<Integer>>> entrySet = itemUserCollection.entrySet();
        Iterator<Map.Entry<Integer, Set<Integer>>> iterator = entrySet.iterator();
        while(iterator.hasNext()){//遍历itemUserCollection
            Set<Integer> commonUsers = iterator.next().getValue();
            for (Integer user_u : commonUsers) {
                for (Integer user_v : commonUsers) {
                    if(user_u.equals(user_v)){
                        continue;
                    }
                    sparseMatrix[userID.get(user_u)][userID.get(user_v)] += 1;
                    //计算用户u与用户v都有正反馈的资源总数
                }
            }
        }
        //计算用户之间的相似度【余弦相似性】
        JSONArray resourceArray = new JSONArray();
        for (int k = 0; k <userItemLength.size() ; k++) { //1-3
            int recommendUserId =idUser.get(k);
            if (recommendID != recommendUserId){
                continue;
            }
            for (int j = 0; j <sparseMatrix.length; j++) {   //遍历每一行
                if (k!= j) {
                    double xsduser=sparseMatrix[k][j] / Math.sqrt(userItemLength.get(idUser.get(j)) * userItemLength.get(idUser.get(k)));
                    String temp=recommendUserId+" "+idUser.get(j);
                    user_user.put(temp,xsduser);

                }//相同的个数/各自喜欢的个数乘机开根号
            }
            //计算指定用户recommendUser的资源推荐度
            JSONArray userArray = new JSONArray();  //存资源id与权重，便于后续排序
            for (Integer item : items) {
                //遍历每一件资源
                Set<Integer> users = itemUserCollection.get(item);
                //得到购买当前资源的所有用户集合
                if (!users.contains(recommendUserId)) {
                    //如果被推荐用户没有浏览当前资源，则进行推荐度计算
                    double itemRecommendDegree = 0.0;
                    for (Integer user : users) {
                        double tjd=sparseMatrix[userID.get(recommendUserId)][userID.get(user)] / Math.sqrt(userItemLength.get(recommendUserId) * userItemLength.get(user));
                        itemRecommendDegree += tjd;
                        //推荐度计算
                        String temp=recommendUserId+" "+item;
                        user_entity.put(temp,itemRecommendDegree);
                    }
                    JSONObject userWeight = new JSONObject();
                    userWeight.put("resourceID", item);
                    userWeight.put("weight", itemRecommendDegree);
                    userArray.add(userWeight);
                }
            }
            int userLength = userArray.size();
            double[] weightList = new double[userLength];
            int[] userList = new int[userLength];
            for (int i = 0;i < userLength;i++){
                weightList[i] = (double) userArray.getJSONObject(i).get("weight");
                userList[i] = (int) userArray.getJSONObject(i).get("resourceID");
            }
            for (int i = 0;i < userLength-1;i++){
                for (int j = i+1; j < userLength; j++){
                    if (weightList[i] < weightList[j]){
                        double tempWeight = weightList[i];
                        weightList[i] = weightList[j];
                        weightList[j] = tempWeight;
                        int tempID = userList[i];
                        userList[i] = userList[j];
                        userList[j] = tempID;
                    }
                }
            }
            ArrayList<Integer> resourceIDList = new ArrayList<>();
            for (int i = 0;i < userLength;i++){
                if (weightList[i]!=0){
                    Resource resource = (Resource) redisTemplate.opsForValue().get("resource_"+userList[i]);
                    if (resource == null){
                        resource = resourceMapper.queryResourceByID(userList[i]);
                        redisTemplate.opsForValue().set("resource_"+userList[i], resource);
                        redisTemplate.expire("resource_"+userList[i], 100, TimeUnit.MINUTES);
                        resourceArray.add(resource);
                    }
                    resourceIDList.add(userList[i]);
                }
            }
            ArrayList<Map<String, Object>> resourceRecord = recordMapper.resourceRecord(recommendUserId);
            Session session = driver.session();//已关
            for (Map<String, Object> resourceMap : resourceRecord){
                int resourceID = (int) resourceMap.get("resource_id");
                StatementResult resourceResult = session.run( "MATCH (n:resource)-[r]->(m:resource) where n.id = {resourceID} and r.weight > 0.5" +
                                " RETURN m.id as ID order by r.weight desc LIMIT 2",
                        parameters( "resourceID", resourceID) );
                while (resourceResult.hasNext()){
                    Record recordID = resourceResult.next();
                    int recommendResourceID = recordID.get( "ID" ).asInt();
                    Resource resource = (Resource) redisTemplate.opsForValue().get("resource_"+recommendResourceID);
                    if (resource == null) {
                        resource = resourceMapper.queryResourceByID(recommendResourceID);
                        redisTemplate.opsForValue().set("resource_"+recommendResourceID, resource);
                        redisTemplate.expire("resource_"+recommendResourceID, 100, TimeUnit.MINUTES);
                    }

                    if (resourceIDList.contains(recommendResourceID)){
                        continue;
                    }
                    resourceArray.add(resource);
                    resourceIDList.add(recommendResourceID);
                }
                if (resourceArray.size()>80){
                    break;
                }
            }
            session.close();
            if (resourceArray.size()<80){
                ArrayList<Resource> resourceList = resourceMapper.queryHot2();
                for (Resource resource:resourceList){
                    resourceArray.add(resource);
                }
            }
            if (resourceArray.size()> 80){
                break;
            }
        }
        int size = resourceArray.size();
        if (resourceArray.size()>80){
            while (resourceArray.size()!=80){
                resourceArray.remove(80);
            }
        }
        if (resourceArray.size()>8){
            while (resourceArray.size()!=8){
                resourceArray.remove(8);
            }
        }
        return ResultFactory.buildSuccessResult("成功查询推荐资源", resourceArray);
    }

    public Result recommendMore(Map<String, Object> userIDMap) {
        int recommendUserID = Integer.parseInt((String)userIDMap.get("userId"));
        ArrayList<Integer> userList = new ArrayList<Integer>();
        userList.add(recommendUserID);
        JSONArray resArray = new JSONArray();
        Session session = driver.session();//已关
        StatementResult userResult = session.run( "MATCH (n:user)-[r]->(m:user) where n.id={userID} and r.weight <> 0 " +
                        "RETURN m.id as ID order by r.weight desc LIMIT 10",
                parameters( "userID", recommendUserID) );
        while (userResult.hasNext()){
            Record recordID = userResult.next();
            int userID = recordID.get( "ID" ).asInt();
            userList.add(userID);
        }
        if(userList.size()==1){
            List<Map> record = userMapper.browseRecord(recommendUserID);
            List<Map> entityRecord = userMapper.entityRecord(recommendUserID);
            JSONArray res = new JSONArray();
            ArrayList<Resource> resourceList = resourceMapper.queryHot2();
            ArrayList<Integer> quchong = new ArrayList<>();
            if (record.size()!=0){
                for(Map recordMap:record){
                    int resourceID = (int) recordMap.get("resource_id");
                    StatementResult result = session.run( "MATCH (a:resource)-[r]->(b:resource) where a.id = {resourceID} " +
                                    "RETURN b.id as ID order by r.weight desc",
                            parameters( "resourceID", resourceID) );
                    while (result.hasNext()){
                        Record recordID = result.next();
                        int weightID = recordID.get( "ID" ).asInt();

                        if (!quchong.contains(weightID)){
                            res.add(resourceMapper.queryResourceByID(weightID));
                            quchong.add(weightID);
                        }

                        if(res.size()==8){
                            return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                        }
                    }
                }
                for(Resource resource:resourceList){
                    res.add(resource);
                    if (res.size()==8){
                        return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                    }
                }
            }
            else if (entityRecord.size()!=0){
                for(Map recordMap:entityRecord){
                    String entityName = (String) recordMap.get("entity_name");
                    StatementResult result = session.run( "MATCH (a:resource)-[r]->(b:concept) where a.id = {entityName} " +
                                    "RETURN a.id as ID order by r.weight desc",
                            parameters( "entityName", entityName) );
                    while (result.hasNext()){
                        Record recordID = result.next();
                        int weightID = recordID.get( "ID" ).asInt();
                        if (!res.contains(resourceMapper.queryResourceByID(weightID))){
                            res.add(resourceMapper.queryResourceByID(weightID));
                        }
                        if(res.size()==8){
                            return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                        }
                    }
                }
                for(Resource resource:resourceList){
                    res.add(resource);
                    if (res.size()==8){
                        return ResultFactory.buildSuccessResult("获取推荐资源成功", res);
                    }
                }
            }
            else {
                return ResultFactory.buildSuccessResult("获取推荐资源成功", resourceList);
            }
        }
        int col = 0;
        for(int i=0;i < userList.size();i++){
            int userID = userList.get(i);
            JSONObject userRecord = new JSONObject();
            userRecord.put("id", userID);
            List<Map> record = userMapper.browseRecord(userID);
            if (record.size()==0){
                continue;
            }
            JSONArray resourceList = new JSONArray();
            for (Map singleRecord : record){
                int resourceID = (int) singleRecord.get("resource_id");
                resourceList.add(resourceID);
            }
            userRecord.put("record", resourceList);
            col = resourceList.size() > col ? resourceList.size() : col;
            resArray.add(userRecord);
        }
        int row = resArray.size();
        int[][] s = new int[row][col + 1];
        for (int i = 0; i < resArray.size(); i++)
        {
            int userID = (int) resArray.getJSONObject(i).get("id");
            s[i][0] = userID;
            List<Integer> resourceList = (List<Integer>) resArray.getJSONObject(i).get("record");
            for (int j = 0; j < resourceList.size(); j++){
                int resourceID = resourceList.get(j);
                s[i][j+1] = resourceID;
            }
        }
        session.close();
        return userxsdMore(s,s.length,recommendUserID);
    }

    public Result userxsdMore(int[][]user_item,int N,int recommendID) {   //1 4 5 6 7 （第一个代表用户的id 后面的代表浏览过的资源id）        N个用户
        /**
         * 输入用户-->物品条目 一个用户对应多个物品
         * 用户ID 物品ID集合
         * 1  2 3
         * 2  2 3 4
         * 3  2 4 5 7
         */
        Map<Integer, Integer> userID = new HashMap<Integer, Integer>();
        //辅助存储每一个用户的用户ID映射
        Map<Integer, Integer> idUser = new HashMap<Integer, Integer>();
        //辅助存储每一个ID对应的用户映射
        Map<String, Double> user_user = new HashMap<String, Double>();//用户与用户的相似度
        Map<String, Double> user_entity = new HashMap<String, Double>();//资源——用户的推荐度
        int[][] sparseMatrix = new int[N][N];
        //建立用户稀疏矩阵，用于用户相似度计算【相似度矩阵】
        Map<Integer, Integer> userItemLength = new HashMap<Integer, Integer>();
        //存储每一个用户对应的不同资源总数 eg: A 3
        Map<Integer, Set<Integer>> itemUserCollection = new HashMap<Integer, Set<Integer>>();
        //建立资源到用户的倒排表 eg: a A B
        Set<Integer> items = new HashSet<Integer>();//set是无序的 且不能重复
        //辅助存储资源集合
        for (int i = 0; i < N ; i++){
            //获取非0长度
            int length = 0;
            for (int k = 0; k < N ; k++){
                if (user_item[i][k]!=0){
                    length++;
                    continue;
                }
                break;
            }
            //依次处理N个用户 输入数据 以空格间隔  A a b d
            userItemLength.put(user_item[i][0], length-1);//A用户 喜欢3个产品
            //eg: A 3
            userID.put(user_item[i][0], i);  //15 0 ,16 1
            //用户ID与稀疏矩阵建立对应关系
            idUser.put(i, user_item[i][0]);// 0 15 ,1 16
            for (int j = 1; j < length; j++){ //j= a b d
                if(items.contains(user_item[i][j])){
                    //如果资源集合中已经包含对应的物品--用户映射，直接添加对应的用户
                    itemUserCollection.get(user_item[i][j]).add(user_item[i][0]);//将A添加到 a b d 中
                } else{
                    //否则创建对应资源--用户集合映射
                    items.add(user_item[i][j]);
                    itemUserCollection.put(user_item[i][j], new HashSet<Integer>());
                    //创建资源--用户倒排关系
                    itemUserCollection.get(user_item[i][j]).add(user_item[i][0]);
                }
            }
        }
        //计算相似度矩阵【稀疏】
        Set<Map.Entry<Integer, Set<Integer>>> entrySet = itemUserCollection.entrySet();
        Iterator<Map.Entry<Integer, Set<Integer>>> iterator = entrySet.iterator();
        while(iterator.hasNext()){//遍历itemUserCollection
            Set<Integer> commonUsers = iterator.next().getValue();
            for (Integer user_u : commonUsers) {
                for (Integer user_v : commonUsers) {
                    if(user_u.equals(user_v)){
                        continue;
                    }
                    sparseMatrix[userID.get(user_u)][userID.get(user_v)] += 1;
                    //计算用户u与用户v都有正反馈的资源总数
                }
            }
        }
        //计算用户之间的相似度【余弦相似性】
        JSONArray resourceArray = new JSONArray();
        for (int k = 0; k <userItemLength.size() ; k++) { //1-3
            int recommendUserId =idUser.get(k);
            if (recommendID != recommendUserId){
                continue;
            }
            for (int j = 0; j <sparseMatrix.length; j++) {   //遍历每一行
                if (k!= j) {
                    double xsduser=sparseMatrix[k][j] / Math.sqrt(userItemLength.get(idUser.get(j)) * userItemLength.get(idUser.get(k)));
                    String temp=recommendUserId+" "+idUser.get(j);
                    user_user.put(temp,xsduser);

                }//相同的个数/各自喜欢的个数乘机开根号
            }
            //计算指定用户recommendUser的资源推荐度
            JSONArray userArray = new JSONArray();  //存资源id与权重，便于后续排序
            for (Integer item : items) {
                //遍历每一件资源
                Set<Integer> users = itemUserCollection.get(item);
                //得到购买当前资源的所有用户集合
                if (!users.contains(recommendUserId)) {
                    //如果被推荐用户没有浏览当前资源，则进行推荐度计算
                    double itemRecommendDegree = 0.0;
                    for (Integer user : users) {
                        double tjd=sparseMatrix[userID.get(recommendUserId)][userID.get(user)] / Math.sqrt(userItemLength.get(recommendUserId) * userItemLength.get(user));
                        itemRecommendDegree += tjd;
                        //推荐度计算
                        String temp=recommendUserId+" "+item;
                        user_entity.put(temp,itemRecommendDegree);
                    }
                    JSONObject userWeight = new JSONObject();
                    userWeight.put("resourceID", item);
                    userWeight.put("weight", itemRecommendDegree);
                    userArray.add(userWeight);
                }
            }
            int userLength = userArray.size();
            double[] weightList = new double[userLength];
            int[] userList = new int[userLength];
            for (int i = 0;i < userLength;i++){
                weightList[i] = (double) userArray.getJSONObject(i).get("weight");
                userList[i] = (int) userArray.getJSONObject(i).get("resourceID");
            }
            for (int i = 0;i < userLength-1;i++){
                for (int j = i+1; j < userLength; j++){
                    if (weightList[i] < weightList[j]){
                        double tempWeight = weightList[i];
                        weightList[i] = weightList[j];
                        weightList[j] = tempWeight;
                        int tempID = userList[i];
                        userList[i] = userList[j];
                        userList[j] = tempID;
                    }
                }
            }
            ArrayList<Integer> resourceIDList = new ArrayList<>();
            for (int i = 0;i < userLength;i++){
                if (weightList[i]!=0){
                    Resource resource = (Resource) redisTemplate.opsForValue().get("resource_"+userList[i]);
                    if (resource == null){
                        resource = resourceMapper.queryResourceByID(userList[i]);
                        redisTemplate.opsForValue().set("resource_"+userList[i], resource);
                        redisTemplate.expire("resource_"+userList[i], 100, TimeUnit.MINUTES);
                        resourceArray.add(resource);
                    }
                    resourceIDList.add(userList[i]);
                }
            }
            ArrayList<Map<String, Object>> resourceRecord = recordMapper.resourceRecord(recommendUserId);
            Session session = driver.session();//已关
            for (Map<String, Object> resourceMap : resourceRecord){
                int resourceID = (int) resourceMap.get("resource_id");
                StatementResult resourceResult = session.run( "MATCH (n:resource)-[r]->(m:resource) where n.id = {resourceID} and r.weight > 0.5" +
                                " RETURN m.id as ID order by r.weight desc LIMIT 2",
                        parameters( "resourceID", resourceID) );
                while (resourceResult.hasNext()){
                    Record recordID = resourceResult.next();
                    int recommendResourceID = recordID.get( "ID" ).asInt();
                    Resource resource = (Resource) redisTemplate.opsForValue().get("resource_"+recommendResourceID);
                    if (resource == null) {
                        resource = resourceMapper.queryResourceByID(recommendResourceID);
                        redisTemplate.opsForValue().set("resource_"+recommendResourceID, resource);
                        redisTemplate.expire("resource_"+recommendResourceID, 100, TimeUnit.MINUTES);
                    }

                    if (resourceIDList.contains(recommendResourceID)){
                        continue;
                    }
                    resourceArray.add(resource);
                    resourceIDList.add(recommendResourceID);
                }
                if (resourceArray.size()>20){
                    break;
                }
            }
            session.close();
            if (resourceArray.size()<20){
                ArrayList<Resource> resourceList = resourceMapper.queryHot2();
                for (Resource resource:resourceList){
                    resourceArray.add(resource);
                }
            }
            if (resourceArray.size()> 20){
                break;
            }
        }

        if (resourceArray.size()>20){
            while (resourceArray.size()!=20){
                resourceArray.remove(20);
            }
        }
        return ResultFactory.buildSuccessResult("成功查询推荐资源", resourceArray);
    }
}
