package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认领审核历史表
 * 只记录审核操作历史，状态由 claims.status 统一管理
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("claim_audits")
public class ClaimAudit extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("claim_id")
    private Long claimId; // 认领申请ID

    @TableField("audit_action")
    private String auditAction; // 审核操作：APPROVED/REJECTED

    @TableField("audit_remark")
    private String auditRemark; // 审核备注

    @TableField("auditor_id")
    private Long auditorId; // 审核人ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
