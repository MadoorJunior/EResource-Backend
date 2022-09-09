package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultFactory;
import cn.edu.njnu.pojo.User;
import cn.edu.njnu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/e-resource/api")
@CrossOrigin
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @PatchMapping("/v1.0/user/{username}")
    public Result updateUser (@PathVariable(name = "username") String username, @RequestBody User requestUser) {
        requestUser.setUsername(username);
        return userService.modifyUserInfo(requestUser);
    }

    @PostMapping("/v1.0/public/relatedUser")
    public Result relatedUser(){
        return userService.relatedUser();
    }

    @GetMapping("/v1.0/public/recommendUser")
    public Result recommend(@RequestParam Map<String, Object> userIDMap){
        return userService.recommend(userIDMap);
    }

    @GetMapping("/v1.0/public/recommendMoreUser")
    public Result recommendMore(@RequestParam Map<String, Object> userIDMap){
        return userService.recommendMore(userIDMap);
    }

}
