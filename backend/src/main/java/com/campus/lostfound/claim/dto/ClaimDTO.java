package com.campus.lostfound.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimDTO {

    @Data
    public static class CreateClaimReq {
        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotBlank(message = "认领证明不能为空")
        private String proofDescription;

        @NotBlank(message = "联系方式不能为空")
        private String contact;
    }

    @Data
    public static class ClaimResp {
        private Long id;
        private String bizId;
        private Long itemId;
        private String itemName;
        private Long claimantId;
        private String claimantName;
        private String proofDescription;
        private String contact;
        private String status;
        private String auditRemark;
        private String createTime;
    }

    @Data
    public static class ClaimQueryReq {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String status;
        private Long itemId;
    }

    // ========== 认领草稿 DTO ==========

    /**
     * 创建认领草稿请求
     * 关键词验证 → 草稿令牌
     */
    @Data
    public static class CreateDraftReq {
        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotBlank(message = "关键词证明不能为空")
        private String keywordProof; // 用户提供的关键词，证明是失主
    }

    /**
     * 认领草稿响应
     * 返回草稿令牌，用于后续正式申请
     */
    @Data
    public static class DraftResp {
        private String draftToken; // 草稿令牌
        private Long itemId; // 物品ID
        private LocalDateTime expiresAt; // 过期时间
        private String message; // 提示信息
    }
}