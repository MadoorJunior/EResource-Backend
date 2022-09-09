package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/e-resource/api")
public class AuthenticationController extends BaseController{
    @GetMapping("/v1.0/public/authentication")
    public Result authentication(){
        Subject subject = SecurityUtils.getSubject();
        String message;
        if (!subject.isAuthenticated()) {
            message = "需要登录";
            return ResultFactory.buildFailResult(message);
        } else {
            message = "身份认证成功";
            return ResultFactory.buildSuccessResult(message, null);
        }
    }
}
