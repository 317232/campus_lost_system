package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("items")
public class Item extends BaseEntity {

    @TableField("biz_id")
    private String bizId;

    private String scene;

    private String status;

    @TableField("owner_id")
    private Long ownerId;

    private String title;

    @TableField("item_name")
    private String itemName;

    @TableField("category_id")
    private Long categoryId;

    private String location;

    @TableField("occurred_at")
    private LocalDateTime occurredAt;

    @TableField("verify_method")
    private String verifyMethod;

    private String description;

    @TableField("value_tag")
    private String valueTag;

    @TableField("contact_visibility")
    private String contactVisibility;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("closed_at")
    private LocalDateTime closedAt;
}
