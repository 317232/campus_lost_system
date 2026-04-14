package com.campus.lostfound.domain.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

@Data
@TableName("user_role")
@EqualsAndHashCode(callSuper = true)
public class UserRole extends BaseEntity {
    private Long userId;
    private Long roleId;
}
