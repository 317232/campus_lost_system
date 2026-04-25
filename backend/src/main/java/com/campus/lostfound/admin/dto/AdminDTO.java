package com.campus.lostfound.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

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

    /** 公告列表响应 */
    public static class NoticeResp {
        private Long id;
        private String title;
        private String content;
        private Boolean published;
        private String publishedAt;
        private String createTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getPublished() { return published; }
        public void setPublished(Boolean published) { this.published = published; }
        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    /** 分类列表响应 */
    public static class CategoryResp {
        private Long id;
        private String bizId;
        private String name;
        private Integer sortOrder;
        private String status;
        private String createTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBizId() { return bizId; }
        public void setBizId(String bizId) { this.bizId = bizId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    /** 举报列表响应 */
    public static class ReportResp {
        private Long id;
        private String bizId;
        private Long reporterId;
        private String targetType;
        private Long targetId;
        private String reason;
        private String detail;
        private String status;
        private String handleRemark;
        private String handledAt;
        private String createTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBizId() { return bizId; }
        public void setBizId(String bizId) { this.bizId = bizId; }
        public Long getReporterId() { return reporterId; }
        public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getHandleRemark() { return handleRemark; }
        public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }
        public String getHandledAt() { return handledAt; }
        public void setHandledAt(String handledAt) { this.handledAt = handledAt; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    /** 用户列表响应 */
    public static class UserListResp {
        private Long id;
        private String studentNo;
        private String name;
        private String phone;
        private String email;
        private String major;
        private String avatarUrl;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /** 更新用户状态请求 */
    public static class UpdateUserStatusReq {
        @NotBlank(message = "状态不能为空")
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /** 管理员新增用户请求 */
    public static class CreateUserReq {
        @NotBlank(message = "学号不能为空")
        @Size(max = 20, message = "学号不能超过20位")
        private String studentNo;

        @NotBlank(message = "姓名不能为空")
        @Size(max = 32, message = "姓名不能超过32位")
        private String name;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度6-32位")
        private String password;

        @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "手机号格式不正确")
        private String phone;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Size(max = 64, message = "专业不能超过64位")
        private String major;

        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
    }

    /** 管理员修改用户信息请求 */
    public static class UpdateUserReq {
        @Size(max = 32, message = "姓名不能超过32位")
        private String name;

        @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "手机号格式不正确")
        private String phone;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Size(max = 64, message = "专业不能超过64位")
        private String major;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
    }

    /** 趋势统计数据响应 */
    public static class StatisticsTrendResp {
        private String type; // daily/monthly
        private List<TrendDataPoint> data;

        public static class TrendDataPoint {
            private String date;
            private long lostCount;
            private long foundCount;
            private long claimCount;
            private long approvedCount;

            public TrendDataPoint() {}

            public TrendDataPoint(String date, long lostCount, long foundCount, long claimCount, long approvedCount) {
                this.date = date;
                this.lostCount = lostCount;
                this.foundCount = foundCount;
                this.claimCount = claimCount;
                this.approvedCount = approvedCount;
            }

            public String getDate() { return date; }
            public void setDate(String date) { this.date = date; }
            public long getLostCount() { return lostCount; }
            public void setLostCount(long lostCount) { this.lostCount = lostCount; }
            public long getFoundCount() { return foundCount; }
            public void setFoundCount(long foundCount) { this.foundCount = foundCount; }
            public long getClaimCount() { return claimCount; }
            public void setClaimCount(long claimCount) { this.claimCount = claimCount; }
            public long getApprovedCount() { return approvedCount; }
            public void setApprovedCount(long approvedCount) { this.approvedCount = approvedCount; }
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<TrendDataPoint> getData() { return data; }
        public void setData(List<TrendDataPoint> data) { this.data = data; }
    }
}
