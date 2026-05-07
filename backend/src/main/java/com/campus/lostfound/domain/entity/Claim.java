package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("claims")
public class Claim extends BaseEntity {

    @TableField("biz_id")
    private String bizId;

    @TableField("item_id")
    private Long itemId;

    @TableField("claimant_id")
    private Long claimantId;

    @TableField("claim_statement")
    private String claimStatement;

    @TableField("feature_proof")
    private String featureProof;

    private String contact;

    private String status;

    @TableField("description_check_result")
    private String descriptionCheckResult;

    @TableField("description_checked_by")
    private Long descriptionCheckedBy;

    @TableField("description_checked_at")
    private LocalDateTime descriptionCheckedAt;

    @TableField("reviewed_by")
    private Long reviewedBy;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    @TableField("audit_remark")
    private String auditRemark;
}
