package cn.edu.njnu.controller;

import cn.edu.njnu.filter.RedisBloomFilter;
import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.service.EntityService;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

@RestController
@RequestMapping("/e-resource/api")
@CrossOrigin
public class EntityController extends BaseController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @GetMapping("/v1.0/public/relatedEntity")
    public Result relatedEntity(@RequestParam Map<String, Object> keywordMap){
        return entityService.getRelatedEntity(keywordMap);
    }

    @GetMapping("/v1.0/public/queryEntity")
    public Result queryEntity(@RequestParam Map<String, Object> keywordMap){

        return entityService.queryEntity(keywordMap);
    }

    @GetMapping("/v1.0/public/queryEntityNew")
    public Result queryEntityNew(@RequestParam Map<String, Object> keywordMap){

        return entityService.queryEntityNew(keywordMap);
    }

    @GetMapping("/v1.0/private/userGraph/{userID}")
    public Result userGraph(@PathVariable(name = "userID") int userID){
        return entityService.userGraph(userID);
    }
}
