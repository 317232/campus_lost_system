package com.campus.lostfound.demo;

import com.campus.lostfound.common.enums.AuditStatus;
import com.campus.lostfound.common.enums.ClaimStatus;
import com.campus.lostfound.common.enums.FoundItemStatus;
import com.campus.lostfound.common.enums.LostItemStatus;
import com.campus.lostfound.common.enums.ReportStatus;
import com.campus.lostfound.common.enums.UserRole;

public final class DemoDtos {

    private DemoDtos() {
    }

    public record UserProfile(Long id, String username, String realName, String studentNo,
                              String phone, String email, UserRole role) {
    }

    public record AuthToken(String token, UserRole role, String displayName) {
    }

    public record ItemCard(Long id, String title, String itemName, String category, String location,
                           String time, String description, String contact, String owner,
                           AuditStatus auditStatus, String stage) {
    }

    public record ClaimRecord(Long id, String itemTitle, String proof, ClaimStatus status, String updatedAt) {
    }

    public record NoticeRecord(Long id, String title, String content, String publishedAt) {
    }

    public record ReportRecord(Long id, String title, String source, ReportStatus status) {
    }

    public record ReviewTask(String code, String title, String submitter, String queue, AuditStatus status) {
    }

    public record OverviewMetric(String label, int value) {
    }

    public record TrendPoint(String day, int posts) {
    }

    public record LostDraft(Long id, String title, LostItemStatus status) {
    }

    public record FoundDraft(Long id, String title, FoundItemStatus status) {
    }
}
