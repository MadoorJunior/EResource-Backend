package cn.edu.njnu.pojo;

public class ResultFactory {
    public static Result buildSuccessResult(String message, Object data) {
        return buildResult(ResultCode.SUCCESS, message, data);
    }

    public static Result buildFailResult(String message) {
        return buildResult(ResultCode.FAIL, message, null);
    }

    public static Result buildResult(ResultCode code, String message, Object data) {
        return new Result(code.code, message, data);
    }

    public static Result buildResult(int code, String message, Object data) {
        return new Result(code, message, data);
    }
}
