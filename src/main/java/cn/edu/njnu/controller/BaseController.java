package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.ResultCode;
import cn.edu.njnu.pojo.ResultFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class BaseController {
    @ExceptionHandler(ShiroException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object shiroException(HttpServletRequest request, Exception ex){
        log.info(ex.getMessage());
        return ResultFactory.buildResult(ResultCode.UNAUTHORIZED,ex.getMessage(),null);
    }
    //定义ExceptionHandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex){
        log.info(ex.getMessage());
        return ResultFactory.buildResult(ResultCode.INTERNAL_SERVER_ERROR,ex.getMessage(),null);
    }

}
