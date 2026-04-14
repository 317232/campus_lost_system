package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("claim_draft")
@EqualsAndHashCode(callSuper = true)
public class ClaimDault extends BaseEntity {
@TableId(type = IdType.AUTO)
    private Long id;
    // 认领草稿ID
    @TableField("draft_token")
    private String draftToken; // 草稿令牌（唯一标识）

    @TableField("item_id")
    private Long itemId; // 物品ID

    @TableField("claimant_id")
    private Long claimantId; // 认领人ID

    @TableField("keyword_proof")
    private String keywordProof; // 关键词证明

    private String status; // 状态：ACTIVE(活跃)/USED(已使用)/EXPIRED(已过期)

    @TableField("expires_at")
    private LocalDateTime expiresAt; // 过期时间
}
