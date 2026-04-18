package com.campus.lostfound.admin.service;

import com.campus.lostfound.admin.dto.AdminDTO;
import java.util.List;
import java.util.Map;

public interface AdminService {

    List<Map<String, Object>> listPendingItems();

    void auditItem(AdminDTO.AuditItemReq request);

    void auditClaim(AdminDTO.AuditClaimReq request);

    List<Map<String, Object>> listUsers();

    void updateUserStatus(Long userId, String status);

    Long createAnnouncement(AdminDTO.AnnouncementCreateReq request);

    boolean updateAnnouncement(AdminDTO.AnnouncementUpdateReq request);

    boolean deleteAnnouncement(Long announcementId);

    Long createCategory(AdminDTO.CategoryUpsertReq request);

    boolean updateCategory(AdminDTO.CategoryUpsertReq request);

    AdminDTO.DashboardResp dashboard();

    boolean handleReport(AdminDTO.HandleReportReq request);

    List<Map<String, Object>> listOperationLogs();
}
