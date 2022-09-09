package cn.edu.njnu.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    // 响应码
    private int code;
    // 响应状态消息
    private String message;
    // 响应结果对象
    private Object data;
}
