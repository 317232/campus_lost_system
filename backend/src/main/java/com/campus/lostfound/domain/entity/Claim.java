package com.campus.lostfound.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;


import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("claims")
public class Claim extends BaseEntity {

    @TableField("biz_id")
    private String bizId; // 业务编号

    @TableField("item_id")
    private Long itemId; // 物品ID

    @TableField("claimant_id")
    private Long claimantId; // 认领人ID

    @TableField("draft_id")
    private Long draftId; // 草稿ID

    @TableField("formal_proof")
    private String formalProof; // 正式证明材料

    private String contact; // 联系信息

    private String status; // 状态：REVIEW_PENDING/APPROVED/REJECTED/EXPIRED/CANCELLED

    @TableField("audit_remark")
    private String auditRemark; // 审核备注
}
