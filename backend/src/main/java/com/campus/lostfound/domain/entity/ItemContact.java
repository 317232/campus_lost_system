package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("item_contact")
@EqualsAndHashCode(callSuper = true)
public class ItemContact extends BaseEntity {
  @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private Long itemId; // 关联的物品ID

    @TableField("contact_type")
    private String contactType; // 联系方式类型（如：phone, email, wechat等）

    @TableField("contact_value")
    private String contactValue; // 联系方式真实内容

    @TableField("masked_value")
    private String maskedValue; // 脱敏后的联系方式（如：138****1234）
}
