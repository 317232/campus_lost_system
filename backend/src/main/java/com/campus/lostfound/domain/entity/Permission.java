package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("permissions")
public class Permission implements Serializable {
    private Long id;
    @TableField("perm_name")
    private String name;
    @TableField("perm_code")
    private String code;
    private String description;
    private String resourceType;
    private String resourcePath;
    private String action;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField(exist = false)
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private LocalDateTime createTime;
}
