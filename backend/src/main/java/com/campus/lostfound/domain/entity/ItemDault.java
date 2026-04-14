package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

public class ItemDault extends BaseEntity {
  @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private Long itemId; // 物品ID

    @TableField("audit_status")
    private String auditStatus; // 审核状态

    @TableField("audit_remark")
    private String auditRemark; // 审核备注（如驳回原因）

    @TableField("auditor_id")
    private Long auditorId; // 审核人（管理员）ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
