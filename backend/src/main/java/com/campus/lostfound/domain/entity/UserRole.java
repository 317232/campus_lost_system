package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_roles")
public class UserRole implements Serializable {
    @TableId
    private Long id;

    private Long userId;

    private Long roleId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private LocalDateTime createTime;
}
