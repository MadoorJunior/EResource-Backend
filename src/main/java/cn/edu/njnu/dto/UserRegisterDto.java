package cn.edu.njnu.dto;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * @description: 用户注册映射对象
 * @author Madoor
 * @date 2022/9/9 21:20 */
@Data
public class UserRegisterDto {
    @NotBlank(message = "用户名不能为空")
    private String username;


    @NotBlank(message ="密码不能为空")
    @Size(min = 8,max = 18,message = "长度不小于8，不超过18")
    private String password;

    private Integer grade=1;
    private Integer period=1;
    private String school;
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotNull(message = "请输入角色信息")
    private Integer userType;
}
