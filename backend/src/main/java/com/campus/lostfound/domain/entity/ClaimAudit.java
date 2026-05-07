package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("claim_audits")
public class ClaimAudit extends BaseEntity {

    @TableField("claim_id")
    private Long claimId;

    @TableField("audit_action")
    private String auditAction;

    @TableField("from_status")
    private String fromStatus;

    @TableField("to_status")
    private String toStatus;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("auditor_id")
    private Long auditorId;
}
