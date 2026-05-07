package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("item_contacts")
public class ItemContact extends BaseEntity {

    @TableField("item_id")
    private Long itemId;

    @TableField("contact_type")
    private String contactType;

    @TableField("contact_value")
    private String contactValue;

    @TableField("masked_value")
    private String maskedValue;

    @TableField("is_primary")
    private Boolean isPrimary;
}
