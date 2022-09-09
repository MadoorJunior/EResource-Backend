package cn.edu.njnu.service;

import cn.edu.njnu.filter.RedisBloomFilter;
import cn.edu.njnu.mapper.RecordMapper;
import cn.edu.njnu.mapper.ResourceMapper;
import cn.edu.njnu.mapper.UserMapper;
import cn.edu.njnu.mapper.XApiMapper;
import cn.edu.njnu.pojo.Resource;
import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.jndi.dns.ResourceRecord;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.shiro.SecurityUtils;
import org.checkerframework.checker.units.qual.A;
import org.neo4j.driver.internal.async.HandshakeHandler;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;

@Service
public class EntityService {
    @Autowired
    private XApiMapper xApiMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Autowired
    private ResourceService resourceService;

    private static Driver driver;

    @Autowired
    public EntityService(Driver driver) {
        EntityService.driver = driver;
    }

    @PostConstruct
    public void initRedisBloomFilter(){
//        Session session = driver.session();
//        //根据用户输入在neo4j中查找对应节点
//        StatementResult result = session.run(
//                "MATCH (a:concept) RETURN a.name as name",
//                parameters( ) );
//        while ( result.hasNext() )
//        {
//            Record record = result.next();
//            redisBloomFilter.put("知识点", record.get("name").asString());
//        }
//        session.close();
    }

    public JSONArray getRelatedEntity(String entityName, Session session, String mainEntityName){
        StatementResult result = session.run( "MATCH (a:concept) -[k:相关关系]-> (m:concept) where a.name = { name } and m.name<>{mainEntity}" +
                        "RETURN m.name AS name limit 3",
                parameters( "name", entityName, "mainEntity", mainEntityName) );
        JSONArray relatedEntity = new JSONArray();
        while ( result.hasNext() )
        {
            Record record = result.next();
            relatedEntity.add(record.get( "name" ).asString());
        }
        relatedEntity.add(mainEntityName);
        return relatedEntity;
    }

    public Result getRelatedEntity(Map<String, Object> keywordMap){
        String[] keyword = ((String) keywordMap.get("keyword")).split("#");
//        Driver driver = createDrive();
        Session session = driver.session();//已关
        JSONArray resArray = new JSONArray();
        if (keyword.length > 1) {
            String Cypher = "with [";
            for (String entityName : keyword){
                Cypher += "\"" + entityName + "\",";
            }
            Cypher = Cypher.substring(0, Cypher.length()-1) + "] as l match p=shortestpath((n:concept)-[*]->(m:concept)) where n.name in l and m.name in l and n.name<>m.name return p as path ";
            Cypher = Cypher + "limit " + (2*keyword.length);
            StatementResult result = session.run( Cypher, parameters(  ) );
            Map<Long, Value> nodesMap = new HashMap<>();
            HashMap<String, ArrayList<String>> relationshipMap = new HashMap<>();
            while ( result.hasNext() )
            {
                Record record = result.next();
                List<Value> values = record.values();
                for (Value value : values) {
                    if (value.type().name().equals("PATH")) {
                        Path p = value.asPath();
                        Iterable<Node> nodes = p.nodes();
                        for (Node node : nodes) {
                            if (!nodesMap.containsKey(node.id())){
                                nodesMap.put(node.id(), node.get("name"));
                            }
                        }
                        Iterable<Relationship> relationships = p.relationships();
                        for (Relationship relationship : relationships) {
                            String entityName = nodesMap.get(relationship.startNodeId()).asString();
                            String related = nodesMap.get(relationship.endNodeId()).asString();
                            ArrayList<String> relateNode;
                            if (!relationshipMap.containsKey(entityName)){
                                relateNode = new ArrayList<>();
                            }
                            else {
                                relateNode = relationshipMap.get(entityName);
                            }
                            if (!relateNode.contains(related)){
                                relateNode.add(related);
                            }
                            relationshipMap.put(entityName, relateNode);
                        }
                    }
                }
            }
            Iterator iter = relationshipMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                ArrayList<String> val = (ArrayList<String>) entry.getValue();
                if (Cypher.contains(key)){
                    JSONObject similarEntity = new JSONObject();
                    similarEntity.put("entityName", key);
                    JSONArray relatedEntity = new JSONArray();
                    for (String entity:val){
                        relatedEntity.add(entity);
                    }
                    similarEntity.put("relatedEntity", relatedEntity);
                    resArray.add(similarEntity);
                }
            }
//            JSONObject similarEntity = new JSONObject();
//            similarEntity.put("relatedEntity", getRelatedEntity(entityName, session, entityName));
//            similarEntity.put("entityName", entityName);
//            resArray.add(similarEntity);
        }
        else {
            StatementResult result = session.run( "MATCH (a:concept)-[]->(n:concept) where a.name = {name} " +
                            "RETURN n.name AS name limit 4",
                    parameters( "name",  keyword[0]  ) );
            if (!result.hasNext()){
                return ResultFactory.buildFailResult("未查询到相关知识点");
            }
            ArrayList<String> mainEntityRelated = new ArrayList<>();
            while ( result.hasNext() )
            {
                Record record = result.next();
                String entityName = record.get( "name" ).asString();
                mainEntityRelated.add(entityName);
                JSONObject similarEntity = new JSONObject();
                similarEntity.put("relatedEntity", getRelatedEntity(entityName, session, keyword[0]));
                similarEntity.put("entityName", entityName);
                resArray.add(similarEntity);
            }
            JSONObject mainEntity = new JSONObject();
            mainEntity.put("relatedEntity", mainEntityRelated);
            mainEntity.put("entityName", keyword[0]);
            resArray.add(mainEntity);
        }
//        driver.close();
        session.close();
        return ResultFactory.buildSuccessResult("查询成功", resArray);
    }

    //新的查寻接口 2022-5-26
    public Result queryEntityNew(Map<String, Object> keywordMap){
        //获取用户输入的内容
        String keyword = (String) keywordMap.get("keyword");
        Map<String, Object> entityAndResources = filterAndQuery(keyword);
        JSONArray entityArray = (JSONArray) entityAndResources.get("entity");
        List<Resource> resourceList = (List<Resource>) entityAndResources.get("resource");;
        JSONObject result = new JSONObject();
        result.put("total", resourceList.size());
        int pages = resourceList.size()%10==0?resourceList.size()/10:resourceList.size()/10+1;
        result.put("pages", pages);
        result.put("entity", entityArray);
        result.put("resources", resourceList);
        return ResultFactory.buildSuccessResult("查询成功", result);
    }

    //获取该关键词的知识点列表 2022-5-26
    public Map filterAndQuery(String keyword){
//        System.out.println(keyword);
        Map<String, Object> entityAndResources = new HashMap<>();
        JSONArray entityArray = new JSONArray();
        Set<Integer> resourceIdSet = new LinkedHashSet<>(); //保证id有序且不重复
        if(redisBloomFilter.mightContain("知识点", keyword)){
            System.out.print("未分词");
            JSONObject entityProperties = getEntityProperties(keyword);
            if (entityProperties!=null) entityArray.add(entityProperties);
            resourceIdSet.addAll(getResourceIdByEntityAndContent(keyword));
        }
        //terms列表，元素就是拆分出来的词以及词性
        for(Term term : ToAnalysis.parse(keyword).getTerms()){
            String word = term.getName();		//分词的内容
            System.out.print(word+",词性为"+term.getNatureStr()+",");
            //分词结果跟原词相同或者布隆过滤器不包含的就略过
            if(word.equals(keyword)||!redisBloomFilter.mightContain("知识点", word)){
                System.out.println("被过滤");
            }
            else {
                JSONObject entityProperties = getEntityProperties(word);
                if (entityProperties!=null) entityArray.add(entityProperties);
                resourceIdSet.addAll(getResourceIdByEntityAndContent(word));
            }
        }
        List<Resource> resourceList = new ArrayList<>();
        // 这一步是为了保证查出来的资源顺序跟前面id一样，放弃了时间效率
        for (Integer integer : resourceIdSet) {
            resourceList.add(resourceMapper.queryResourceByID(integer));
        }
        entityAndResources.put("entity", entityArray);
        entityAndResources.put("resource", resourceList);
        System.out.println("总资源个数:"+resourceList.size());
        System.out.println("--------------------------------");
        return entityAndResources;
    }

    //过滤词性 暂时用不到
    public boolean filterPartOfSpeech(String natureStr){
        //只关注名词、动词、形容词、副词
        Set<String> expectedNature = new HashSet<String>() {{
            add("n");add("q");add("v");
            add("vd");add("vn");add("vf");
            add("vx");add("vi");
            add("nt");add("nz");add("nw");add("nl");
            add("ng");add("wh");
            add("en");add("l");
        }};
        return expectedNature.contains(natureStr);
    }

    //获取知识点的属性、重难点 2022-5-26
    public JSONObject getEntityProperties(String entityName){
        Session session = driver.session();//已关
        StatementResult result = session.run(
                "MATCH (a:concept) where a.name = {name} " +
                        "RETURN properties(a) AS props",
                parameters( "name", entityName) );
        session.close();
        JSONObject entityProperties = new JSONObject();
        if (result.hasNext()){
            Record record = result.next();
            entityProperties.put("goalAndKey", goalAndKey(entityName));
            entityProperties.put("entityName", entityName);
            entityProperties.put("properties", record.get( "props" ).asMap());
            return entityProperties;
        }else{
            return null;
        }
    }

    public Set<Integer> getResourceIdByEntityAndContent(String word){
        Set<Integer> idSet = new LinkedHashSet<>();
        idSet.addAll(getResourceIdByEntity(word));
        idSet.addAll(getResourceIdByContent(word));
        System.out.println("资源个数:"+idSet.size());
        return idSet;
    }

    //从图谱中获取与知识点节点直接相连的资源节点id
    public Set<Integer> getResourceIdByEntity(String entity){
        Session session = driver.session();//已关
        Set<Integer> idSet = new LinkedHashSet<>();
        //根据用户输入在neo4j中查找对应节点
        StatementResult result = session.run(
                "MATCH (n:resource)-[r]->(a:concept) where a.name = {name} " +
                        "RETURN n.id as id order by r.weight desc",
                parameters( "name", entity) );
        while ( result.hasNext() )
        {
            Record idRecord = result.next();
            idSet.add(idRecord.get("id").asInt());
        }
        session.close();
        return idSet;
    }

    //从图谱中获取与知识点节点直接相连的资源节点id
    public Set<Integer> getResourceIdByContent(String content){
        Set<Integer> idSet = resourceMapper.queryResourceIdByContent(content);
        return idSet;
    }

    public Result queryEntity(Map<String, Object> keywordMap) {
        System.out.println(keywordMap);
        String browser = (String) keywordMap.get("browser");
        String OS = (String) keywordMap.get("OS");
        String ipAddress = (String) keywordMap.get("ipAddress");
        long browseDate = System.currentTimeMillis();
        //获取用户输入的内容
        String keyword = (String) keywordMap.get("keyword");
        String content = keyword;
        //向数据库添加用户浏览记录
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        int from = Integer.parseInt((String)keywordMap.getOrDefault("from", 0));
        if (username!=null){
            int userId = userMapper.queryUserByName(username).getUserId();
            recordMapper.addEntityRecord(userId,browseDate,keyword, browser, OS, ipAddress);
            if (from==1){
                xApiMapper.addClickEntity(userId,browseDate,keyword);
            }
        }
        int period = Integer.parseInt((String) keywordMap.getOrDefault("period", "0"));
        int subject = Integer.parseInt((String) keywordMap.getOrDefault("subject", "0"));

        int sort = 0;  //0默认，1最热，2最新
        int type = 0;  //0全部
        if (keywordMap.containsKey("sort")){
            sort = Integer.parseInt((String) keywordMap.get("sort"));
        }
        if (keywordMap.containsKey("type")){
            type = Integer.parseInt((String) keywordMap.get("type"));
        }
        int page = Integer.parseInt( (String) keywordMap.get("page") );
        int perPage = Integer.parseInt( (String) keywordMap.get("perPage") );
        if (keyword==null){
            return resourceService.getResourcesByPeriodSubject(subject, period, sort, type, page, perPage);
        }
        if (keyword.equals("小学") || keyword.equals("初中") || keyword.equals("高中")){
            return resourceService.getResourcesByGrade(subject, keyword, sort, type, page, perPage);
        }

        //根据用户输入与资源名进行匹配
        ArrayList<Resource> resourceNameList =
                (ArrayList<Resource>) redisTemplate.opsForValue().get("content_"+sort+"_"+type+"_"+content);
        if (resourceNameList == null){
            resourceNameList = resourceMapper.queryResourceByContent(content, sort, type);
            redisTemplate.opsForValue().set("content_"+sort+"_"+type+"_"+content, resourceNameList);
            redisTemplate.expire(content+"_"+sort+"_"+type, 100, TimeUnit.MINUTES);
        }
        Set<Integer> idSet = new HashSet<Integer>(){
            {
                add(-1);//避免空列表导致报错
            }
        };
        //获取资源id，避免之后在neo4j中重复查找
        for (Resource single:resourceNameList){
            int id = single.getId();
            //获取到的资源id加入列表，之后获取详细信息用
            idSet.add(id);
        }
        //与neo4j建立连接
        Session session = driver.session();//已关
        //根据用户输入在neo4j中查找对应节点
        StatementResult result = session.run( "MATCH (a:concept) where a.name = {name} " +
                        "RETURN properties(a) AS props",
                parameters( "name", keyword) );

        Record record = null;
        if (!result.hasNext()){
            //精确匹配没有查到则进行模糊查找，结果限制个数为1
            String keyword1 = ".*" + keyword + ".*";
            StatementResult result1 = session.run( "MATCH (a:concept) where a.name =~ {name} " +
                            "RETURN properties(a) AS props limit 1",
                    parameters( "name", keyword1) );
            if (result1.hasNext()){
                record = result1.next();
            }
            else { //模糊查找依旧没有查找到，则对用户输入进行分词处理
                org.ansj.domain.Result ansjResult = ToAnalysis.parse(keyword); //封装的分词结果对象，包含一个terms列表
                List<Term> terms = ansjResult.getTerms(); //term列表，元素就是拆分出来的词以及词性
                for(Term term:terms){
                    String fenci = term.getName();		//分词的内容
                    if(term.getNatureStr().equals("n")){   //分词的词性，如果是名词，尝试查找节点
                        StatementResult resultfenci = session.run( "MATCH (a:concept) where a.name = {name} " +
                                        "RETURN properties(a) AS props",
                                parameters( "name", fenci) );
                        if (resultfenci.hasNext()){ //若查找到知识点，跳出查找循环
                            record = resultfenci.next();
                            break;
                        }
                    }
                }
                if (record == null && resourceNameList.isEmpty()){  //分词后也未查到，并且根据资源名也没查到，则返回未查找到的结果
                    session.close();
                    return ResultFactory.buildFailResult("未查询到相关知识点");
                }
            }
        }
        else { //将查询结果赋值给record
            record = result.next();
        }

        JSONObject similarEntity = new JSONObject();

        if (record!=null){
            String entityName = record.get( "props" ).get( "name" ).asString();
            similarEntity.put("goalAndKey", goalAndKey(entityName));
            similarEntity.put("entityName", entityName);
            keyword = entityName;
            similarEntity.put("properties", record.get( "props" ).asMap());
            //如果keyword跟一开始用户的keyword不同，则再对资源名进行一次匹配
            if (!keyword.equals(content)){
                ArrayList<Resource> resourceNameList2 = resourceMapper.queryResourceByContent(keyword, sort, type);
                for (Resource single:resourceNameList2){
                    int id = single.getId();
                    idSet.add(id);
                }
                //根据知识点查找关联资源
                StatementResult resourceNode = session.run( "MATCH (m:resource)-[r]->(a:concept) where a.name = {name} " +
                                "RETURN m.id AS id order by r.weight",
                        parameters( "name", keyword) );
                while ( resourceNode.hasNext() )
                {
                    Record ResourceRecord = resourceNode.next();
                    int resourceID = ResourceRecord.get( "id" ).asInt();
                    idSet.add(resourceID);
                }
            }
        }
        else {
            //如果没有查到知识节点，则将重难点，知识名称，属性设置为null
            similarEntity.put("goalAndKey", null);
            similarEntity.put("entityName", null);
            similarEntity.put("properties", null);
        }

        //根据前面生成的idList从mysql中查资源
        List<Resource> resourceArrayList = resourceMapper.queryResourceByIDList(idSet,sort,type);
        List<Resource> resourceArrayListTmp = new ArrayList<>();
        for (Resource resource:resourceArrayList){
//            System.out.println(resource.getPeriod());
            if (period!=0 && !resource.getPeriod().equals(String.valueOf(period))){
                continue;
            }
            if (subject!=0&&resource.getSubject()!=subject){
                continue;
            }
            int resourceID = resource.getId();
            Resource resourceInRedis = (Resource) redisTemplate.opsForValue().get("resource_"+resourceID);
            if (resourceInRedis == null){
            //在neo4j中获取资源包含的知识点，生成list
//                StatementResult conceptNode = session.run( "MATCH (m:resource)-[r]->(a:concept) where m.id = {id} " +
//                                "RETURN a.name AS name order by r.tfidf",
//                        parameters( "id", resourceID) );
//                ArrayList<String> entityList = new ArrayList<>();
//                while ( conceptNode.hasNext() )
//                {
//                    Record entityRecord = conceptNode.next();
//                    String name = entityRecord.get( "name" ).asString();
//                    entityList.add(name);
//                }
                resource.setEntityList(null);
                //设置资源封面路径
                resource.setCover("/cover/" + resource.getId() + ".png");

                //extendID：资源在额外表中的id； tableID，资源额外属性存在哪张表里
                int extendID = resource.getTableResourceID();
                int tableID = resource.getTable();
                //根据资源类型获取资源属性
                switch (tableID) {
                    case 1:
                        Map bvideoInfo = resourceMapper.queryBvideo(extendID);
                        resource.setAid((String) bvideoInfo.get("aid"));
                        resource.setBvid((String) bvideoInfo.get("bvid"));
                        resource.setCid((String) bvideoInfo.get("cid"));
                        resource.setPage((int)bvideoInfo.get("page"));
                        break;
                    case 2:
                        Map documentInfo = resourceMapper.queryDocument(extendID);
                        resource.setUrl((String) documentInfo.get("url"));
                        resource.setViewUrl((String) documentInfo.get("view_url"));
                        break;
                    case 3:
                        Map videoInfo = resourceMapper.queryVideo(extendID);
                        resource.setUrl((String) videoInfo.get("url"));
                        break;
                }
                redisTemplate.opsForValue().set("resource_"+resourceID,resource);
                redisTemplate.expire("resource_"+resourceID, 100, TimeUnit.MINUTES);
            }
            else{
                BeanUtils.copyProperties(resourceInRedis, resource);
            }
            resourceArrayListTmp.add(resource);
        }

        //根据页码与每页个数获取资源
        resourceArrayList = resourceArrayListTmp;
        int totalEntity = resourceArrayList.size();
        int skip = (page-1)*perPage;
        JSONArray resourceTotal = new JSONArray();
        for (int i = skip;i<skip+perPage && i<totalEntity;i++){
            resourceTotal.add(resourceArrayList.get(i));
        }
        similarEntity.put("resources", resourceTotal);
        //建立返回json数据类型
        JSONObject resObject = new JSONObject();
        JSONArray resArray = new JSONArray();
        resArray.add(similarEntity);
        resObject.put("resources", resArray);
        resObject.put("total", totalEntity);
        resObject.put("pages", (int)Math.ceil(totalEntity * 1.0 / perPage));
        session.close();
        return ResultFactory.buildSuccessResult("查询成功", resObject);
    }

    //根据entity查找重难点,从mysql里面查
    public JSONArray goalAndKey(String entityName){
        JSONArray resArray = new JSONArray();
//        Driver driver = createDrive();
        Session session = driver.session();//已关
        StatementResult goalNode = session.run( "MATCH (m:GoalAndKey)-[r]->(a:concept) where a.name = {name} " +
                        "RETURN m.key, m.goal, m.id",
                parameters( "name", entityName) );
        while ( goalNode.hasNext() )
        {
            Record goalRecord = goalNode.next();
            String objectives = goalRecord.get( "m.goal" ).asString();
            String key = goalRecord.get( "m.key" ).asString();
            int id = goalRecord.get("m.id").asInt();
            JSONObject singleGK = new JSONObject();
            singleGK.put("objectives", objectives);
            singleGK.put("key", key);
            singleGK.put("resourceID", id);
            resArray.add(singleGK);
        }
        session.close();
//        driver.close();
        return resArray;
    }

    //根据用户浏览记录生成图谱
    public Result userGraph(int userID){
//        Driver driver = createDrive();
        Session session = driver.session();//已关
        HashMap<String, Integer> entityNumMap = new HashMap<>();
        HashMap<String, Integer> subjectMap = new HashMap<>();
        ArrayList<Map<String, Object>> recordMap = recordMapper.record(userID);
        int flag1 = 0;  //判断是否有搜索单独知识点
        int flag2 = 0;  //判断是否浏览过资源
        String neo4jNode = "MATCH (n:concept) where ";
        String neo4JMatch = "MATCH (m:resource)-[]->(n:concept) where ";
        for (Map singleRecord:recordMap){
            if (singleRecord.containsKey("entity_name")){
                String entityName = (String) singleRecord.get("entity_name");
                if (entityNumMap.containsKey(entityName)){
                    int currentNum = entityNumMap.get(entityName);
                    entityNumMap.put(entityName, currentNum + 1);
                }
                else {
                    entityNumMap.put(entityName, 1);
                }
                neo4jNode += "n.name = \'" + entityName + "\'" + " or ";
                flag1 = 1;
            }

            if (singleRecord.containsKey("resource_id")){
                int resourceID = (int)singleRecord.get("resource_id");
                neo4JMatch += "m.id =" + resourceID + " or ";
                flag2 = 1;
            }
        }
        if (flag1 == 1){
            neo4jNode = neo4jNode.substring(0, neo4jNode.length()-4);
            neo4jNode += " RETURN n.name as name, n.学科 as subject";
            StatementResult resNode = session.run(neo4jNode);
            while ( resNode.hasNext() )
            {
                Record nodeRecord = resNode.next();
                String entityName = nodeRecord.get( "name" ).asString();
                String subject = nodeRecord.get( "subject" ).asString();
                if (!subjectMap.containsKey(subject)){
                    subjectMap.put(subject, 1);
                }
                if (entityNumMap.containsKey(entityName)){
                    int currentNum = entityNumMap.get(entityName);
                    entityNumMap.put(entityName, currentNum + 1);
                }
                else {
                    entityNumMap.put(entityName, 1);
                }
            }
        }
        if (flag2 == 1){
            neo4JMatch = neo4JMatch.substring(0, neo4JMatch.length()-4);
            neo4JMatch += " RETURN n.name as name, n.学科 as subject";
            StatementResult resEntity = session.run(neo4JMatch);
            while ( resEntity.hasNext() )
            {
                Record nodeRecord = resEntity.next();
                String entityName = nodeRecord.get( "name" ).asString();
                String subject = nodeRecord.get( "subject" ).asString();
                if (!subjectMap.containsKey(subject)){
                    subjectMap.put(subject, 1);
                }
                if (entityNumMap.containsKey(entityName)){
                    int currentNum = entityNumMap.get(entityName);
                    entityNumMap.put(entityName, currentNum + 1);
                }
                else {
                    entityNumMap.put(entityName, 1);
                }
            }
        }
        String sql =  "MATCH (n:concept) where (";
        for (String key : entityNumMap.keySet()) {
            sql += "n.name = \'" + key + "\'" + " or ";
        }
        sql = sql.substring(0, sql.length() - 4);
        JSONArray resArray = new JSONArray();
        for (String subjectKey : subjectMap.keySet()) {
            JSONObject subjectObject = new JSONObject();
            subjectObject.put("subject", subjectKey);
            String subjectSql = sql + ") and n.学科 = \'" + subjectKey + "\'" + " RETURN n.name as name";
            StatementResult subjectNode = session.run(subjectSql);
            JSONArray node = new JSONArray();
            while ( subjectNode.hasNext() )
            {
                JSONObject nodeInfo = new JSONObject();
                Record subjectNodeRecord = subjectNode.next();
                String entityName = subjectNodeRecord.get( "name" ).asString();
                nodeInfo.put("entityName", entityName);
                ArrayList<String> connectNode = new ArrayList<>();
                ArrayList<String> disconnect = new ArrayList<>();
                int disconnectNum = 0;
                StatementResult relatedNode = session.run("MATCH (n:concept)-[]->(m:concept) where n.name = {nodeName}" +
                        "RETURN m.name as name",
                        parameters("nodeName", entityName));
                while ( relatedNode.hasNext() )
                {
                    Record relatedNodeRecord = relatedNode.next();
                    String relatedEntityName = relatedNodeRecord.get("name").asString();
                    if (entityNumMap.containsKey(relatedEntityName)){
                        connectNode.add(relatedEntityName);
                    }
                    else {
                        if (disconnectNum < 4){ //防止disconnect太多了
                            disconnect.add(relatedEntityName);
                            disconnectNum++;
                        }
                    }
                }
                nodeInfo.put("connect", connectNode);
                nodeInfo.put("disconnect", disconnect);
                node.add(nodeInfo);
            }
            subjectObject.put("node", node);
            resArray.add(subjectObject);
        }
        session.close();
//        driver.close();
        return ResultFactory.buildSuccessResult("success", resArray);
    }
}
