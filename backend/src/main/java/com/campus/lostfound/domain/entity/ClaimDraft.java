package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("claim_drafts")
public class ClaimDraft extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
    
    @TableField("used_at")
    private LocalDateTime usedAt; // 使用时间
    
    @TableField("created_at")
    private LocalDateTime createdAt; // 创建时间
}
