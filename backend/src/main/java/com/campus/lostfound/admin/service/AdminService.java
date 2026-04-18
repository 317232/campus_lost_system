package com.campus.lostfound.admin.service;

import com.campus.lostfound.admin.dto.AdminDTO;

public interface AdminService {

    void auditItem(AdminDTO.AuditItemReq request);

    void auditClaim(AdminDTO.AuditClaimReq request);

    Long createAnnouncement(AdminDTO.AnnouncementCreateReq request);

    boolean updateAnnouncement(AdminDTO.AnnouncementUpdateReq request);

    boolean deleteAnnouncement(Long announcementId);

    Long createCategory(AdminDTO.CategoryUpsertReq request);

    boolean updateCategory(AdminDTO.CategoryUpsertReq request);

    AdminDTO.DashboardResp dashboard();

    boolean handleReport(AdminDTO.HandleReportReq request);
}
