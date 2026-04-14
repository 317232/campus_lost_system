package com.campus.lostfound.domain.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

@Data
@TableName("role_permission")
@EqualsAndHashCode(callSuper = true)
public class RolePermission extends BaseEntity {
    private Long roleId;
    private Long permissionId;

}
