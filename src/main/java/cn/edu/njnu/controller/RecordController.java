package cn.edu.njnu.controller;

import cn.edu.njnu.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/e-resource/api")
public class RecordController extends BaseController {

    @Autowired
    private RecordService recordService;

    @PostMapping("/v1.0/public/record")
    public void addRecord(@RequestBody Map<String, Object> recordMap){
        recordService.addRecord(recordMap);
    }
}
