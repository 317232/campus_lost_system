package com.campus.lostfound.domain.entity;


import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;



@Data
@TableName("permission")
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {
    private String name;
    private String code;
    private String description;
    private Boolean enabled;
}
