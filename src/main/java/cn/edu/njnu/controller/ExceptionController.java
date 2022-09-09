package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultCode;
import cn.edu.njnu.pojo.ResultFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@RestController
public class ExceptionController extends BaseController{
    /**
     * @description: 权限不足统一拦截
     * @author Madoor
     * @date 2022/9/8 17:22
     * */
    @RequestMapping("/filterException")
    public Result filterException(HttpServletRequest request) throws Exception {
        return ResultFactory.buildResult(ResultCode.UNAUTHORIZED,"NO_PERMISSION",null);
    }
}
