package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("role_permissions")
public class RolePermission implements Serializable {
    @TableId
    private Long id;

    private Long roleId;

    private Long permissionId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
