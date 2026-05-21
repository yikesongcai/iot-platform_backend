package com.atchensong.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("users")
@ToString
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
/*
* @Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
*/
