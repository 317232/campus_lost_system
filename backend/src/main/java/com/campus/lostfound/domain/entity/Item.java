package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("items")
public class Item extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("biz_id")
    private String bizId; // 业务编号

    private String scene; // 场景：lost(失物)/found(招领)

    private String stage; // 阶段：draft/active/closed

    private String status; // 状态：DRAFT/PENDING_REVIEW/PUBLISHED/REJECTED/CLAIMED/FOUND_BACK/OFFLINE

    @TableField("owner_id")
    private Long ownerId; // 发布人ID

    private String title; // 标题

    @TableField("item_name")
    private String itemName; // 物品名称

    private String category; // 分类

    private String zone; // 区域

    private String location; // 地点

    @TableField("time_label")
    private String timeLabel; // 时间描述

    @TableField("verify_method")
    private String verifyMethod; // 认领验证方式

    private String description; // 物品描述

    @TableField("value_tag")
    private String valueTag; // 价值标签

    @TableField("contact_visibility")
    private String contactVisibility; // 联系方式可见性：MASKED/UNMASKED

    @TableLogic // MyBatis-Plus 逻辑删除注解：0=未删除，1=已删除
    @TableField("deleted")
    private Integer deleted;

}
