package com.campus.lostfound.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminDTO {

    public static class AuditItemReq {
        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotBlank(message = "审核结果不能为空")
        private String auditStatus;

        private String auditRemark;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
        public String getAuditRemark() { return auditRemark; }
        public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    }

    public static class AuditClaimReq {
        @NotNull(message = "认领申请ID不能为空")
        private Long claimId;

        @NotBlank(message = "审核结果不能为空")
        private String auditStatus;

        private String auditRemark;

        public Long getClaimId() { return claimId; }
        public void setClaimId(Long claimId) { this.claimId = claimId; }
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
        public String getAuditRemark() { return auditRemark; }
        public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    }

    public static class AnnouncementCreateReq {
        @NotBlank(message = "标题不能为空")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        private Boolean published;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getPublished() { return published; }
        public void setPublished(Boolean published) { this.published = published; }
    }

    public static class AnnouncementUpdateReq extends AnnouncementCreateReq {
        @NotNull(message = "公告ID不能为空")
        private Long id;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class CategoryUpsertReq {
        private Long id;

        @NotBlank(message = "分类名称不能为空")
        private String name;

        private Integer sortOrder;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class HandleReportReq {
        @NotNull(message = "举报ID不能为空")
        private Long reportId;

        @NotBlank(message = "处理状态不能为空")
        private String status;

        private String handleRemark;

        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getHandleRemark() { return handleRemark; }
        public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }
    }

    public record DashboardResp(long lostCount, long foundCount, long approvedClaims, long totalClaims,
                                double claimSuccessRate) {
    }
}
