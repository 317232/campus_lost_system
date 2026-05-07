package com.campus.lostfound.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimDTO {

    /** 用户提交认领申请请求 */
    @Data
    public static class ClaimApplyReq {
        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotBlank(message = "认领描述不能为空")
        private String claimDescription;

        private String proofMaterial;

        @NotBlank(message = "联系方式不能为空")
        private String contact;
    }

    /** 用户查看我的认领申请响应 */
    @Data
    public static class MyClaimVO {
        private Long id;
        private String bizId;
        private Long itemId;
        private String itemName;
        private String itemLocation;
        private String claimDescription;
        private String proofMaterial;
        private String contact;
        private String status;
        private String createTime;
        private String updateTime;
    }

    /** 管理员审核请求 */
    @Data
    public static class ClaimAuditDTO {
        @NotNull(message = "认领申请ID不能为空")
        private Long claimId;

        @NotBlank(message = "审核结果不能为空")
        private String auditResult;

        private String auditRemark;
    }

    /** 管理员查看认领申请详情响应 */
    @Data
    public static class ClaimAuditVO {
        private Long id;
        private String bizId;
        private Long itemId;
        private String itemTitle;
        private String itemName;
        private Long claimantId;
        private String claimantName;
        private String claimantStudentNo;
        private String claimDescription;
        private String proofMaterial;
        private String contact;
        private String status;
        private String createTime;
    }

    /** 创建认领草稿请求 */
    @Data
    public static class CreateDraftReq {
        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotBlank(message = "关键词证明不能为空")
        private String keywordProof;
    }

    /** 认领草稿响应 */
    @Data
    public static class DraftResp {
        private String draftToken;
        private Long itemId;
        private LocalDateTime expiresAt;
        private String message;
    }

    @Data
    public static class CreateClaimReq {
        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotBlank(message = "认领证明不能为空")
        private String proofDescription;

        private String keywordProof;

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
}