package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

@TableId(type = IdType.AUTO)
    private Long id;

    @TableField("student_no")
    private String studentNo; // 学号，唯一身份标识

    private String name; // 姓名

    @TableField("password_hash")
    private String passwordHash; // 密码哈希值，需求明确密码需要加密存储 [cite: 479]

    private String phone; // 手机号
    
    private String email; // 邮箱
    
    private String major; // 专业
    
    @TableField("avatar_url")
    private String avatarUrl; // 头像地址

    private String status; // 状态：ACTIVE/INACTIVE
}
