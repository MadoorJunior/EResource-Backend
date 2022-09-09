package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Comment;
import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/e-resource/api")
@CrossOrigin
public class CommentController extends BaseController{

    @Autowired
    private CommentService commentService;

    @GetMapping("/v1.0/public/comment")
    public Result comment(@RequestParam Map<String, Object> resourceIDMap){
        return commentService.comment(resourceIDMap);
    }

    @PostMapping("/v1.0/public/addComment")
    public Result addComment(@RequestBody Comment comment){
        return commentService.addComment(comment);
    }
}
