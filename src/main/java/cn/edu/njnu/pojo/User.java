package cn.edu.njnu.pojo;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int userId;
    private String username;
    @JsonAlias("email")
    private String userEmail;
    private int userType;
    @JsonAlias("password")
    private String userPassword;
    private String salt;
    private int period;
    private int grade;
    private String school;
    private String avatar;
}
