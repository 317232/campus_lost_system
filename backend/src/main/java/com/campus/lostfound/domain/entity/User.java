package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    @TableField("student_no")
    private String studentNo;

    private String name;

    @TableField("password_hash")
    private String passwordHash;

    private String phone;

    private String email;

    private String major;

    @TableField("avatar_url")
    private String avatarUrl;

    private String status;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}
