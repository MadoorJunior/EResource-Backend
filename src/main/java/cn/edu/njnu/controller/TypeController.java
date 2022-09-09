package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/e-resource/api")
public class TypeController extends BaseController {

    @Autowired
    private TypeService typeService;

    @GetMapping("/v1.0/public/classification")
    public Result classification(){
        return typeService.classification();
    }

    @GetMapping("/v1.0/public/subject")
    public Result getSubject(){
        return typeService.getSubject();
    }
}
