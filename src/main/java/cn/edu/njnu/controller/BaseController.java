package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultCode;
import cn.edu.njnu.pojo.ResultFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class BaseController {
    /**
     * @description: 统一异常处理
     * @author Madoor
     * @date 2022/9/10 16:31     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder stringBuilder = new StringBuilder("校验失败");
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            stringBuilder.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage()).append(",");
        }
        String msg=stringBuilder.toString();
        return ResultFactory.buildFailResult(msg);

    }
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleConstraintViolationException(ConstraintViolationException ex){
        return ResultFactory.buildFailResult("参数校验错误");
    }
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
        log.info(ex.toString());
        return ResultFactory.buildResult(ResultCode.INTERNAL_SERVER_ERROR,"INTERNAL_SERVER_ERROR",null);
    }

}
