package com.campus.lostfound.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
}