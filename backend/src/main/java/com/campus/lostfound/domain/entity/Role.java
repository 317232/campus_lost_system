package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("roles")
public class Role extends BaseEntity {
  @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("role_code")
    private String roleCode; // 角色编码：ADMIN/USER

    @TableField("role_name")
    private String roleName; // 角色名称

    private String description; // 角色描述

    @TableField("is_system")
    private Boolean isSystem; // 是否系统内置角色

}
