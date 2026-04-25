package com.campus.lostfound.admin.service;

import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.common.api.PageResponse;
import java.util.List;
import java.util.Map;

public interface AdminService {

    // ── 待审列表 ───────────────────────────────────────────────
    /** 待审核物品列表（分页） */
    PageResponse<Map<String, Object>> listPendingItems(Integer page, Integer pageSize, String scene);

    /** 待审核认领列表（分页） */
    PageResponse<Map<String, Object>> listPendingClaims(Integer page, Integer pageSize);

    // ── 审核操作 ───────────────────────────────────────────────
    void auditItem(AdminDTO.AuditItemReq request);

    void auditClaim(AdminDTO.AuditClaimReq request);

    // ── 公告 CRUD ──────────────────────────────────────────────
    /** 公告列表（分页） */
    PageResponse<AdminDTO.NoticeResp> listNotices(Integer page, Integer pageSize, String keyword);

    Long createAnnouncement(AdminDTO.AnnouncementCreateReq request);

    boolean updateAnnouncement(AdminDTO.AnnouncementUpdateReq request);

    boolean deleteAnnouncement(Long announcementId);

    // ── 分类 CRUD ──────────────────────────────────────────────
    /** 分类列表 */
    List<AdminDTO.CategoryResp> listCategories();

    Long createCategory(AdminDTO.CategoryUpsertReq request);

    boolean updateCategory(AdminDTO.CategoryUpsertReq request);

    /** 删除分类 */
    boolean deleteCategory(Long categoryId);

    // ── 数据大盘 ───────────────────────────────────────────────
    AdminDTO.DashboardResp dashboard();

    /** 趋势统计（按时间维度） */
    AdminDTO.StatisticsTrendResp getStatisticsTrend(String startDate, String endDate, String type);

    // ── 举报 CRUD ──────────────────────────────────────────────
    /** 举报列表（分页） */
    PageResponse<AdminDTO.ReportResp> listReports(Integer page, Integer pageSize, String status);

    boolean handleReport(AdminDTO.HandleReportReq request);

    /** 删除举报记录 */
    boolean deleteReport(Long reportId);

    // ── 用户管理 ───────────────────────────────────────────────
    /** 用户列表（分页） */
    PageResponse<AdminDTO.UserListResp> listUsers(Integer page, Integer pageSize, String keyword);

    /** 新增用户 */
    Long createUser(AdminDTO.CreateUserReq request);

    /** 修改用户信息 */
    boolean updateUser(Long userId, AdminDTO.UpdateUserReq request);

    /** 删除用户 */
    boolean deleteUser(Long userId);

    /** 更新用户状态：ACTIVE / INACTIVE */
    void updateUserStatus(Long userId, String status);
}
