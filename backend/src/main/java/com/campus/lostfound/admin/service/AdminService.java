package com.campus.lostfound.admin.service;

import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.common.api.PageResponse;
import java.util.List;

public interface AdminService {

    // ── 分类 CRUD ──────────────────────────────────────────────
    /** 分类列表 */
    List<AdminDTO.CategoryResp> listCategories();

    Long createCategory(AdminDTO.CategoryUpsertReq request);

    boolean updateCategory(AdminDTO.CategoryUpsertReq request);

    /** 删除分类 */
    boolean deleteCategory(Long categoryId);

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

    /** 更新用户状态：ACTIVE / DISABLED */
    void updateUserStatus(Long userId, String status);
}
