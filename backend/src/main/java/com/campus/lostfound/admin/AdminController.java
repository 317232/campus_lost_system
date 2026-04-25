package com.campus.lostfound.admin;

import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.admin.service.AdminService;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.common.api.ResultCode;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员后台控制器
 * 基础路径: /api/admin
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ════════════════════════════════════════════════════════════
    // 物品审核 / 认领审核
    // ════════════════════════════════════════════════════════════

    /** 待审核物品列表（分页） */
    @GetMapping("/items/review")
    public ApiResponse<PageResponse<Map<String, Object>>> listPendingItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String scene) {
        PageResponse<Map<String, Object>> result = adminService.listPendingItems(page, pageSize, scene);
        return ApiResponse.success(result);
    }

    /** 待审核认领列表（分页） */
    @GetMapping("/claims/review")
    public ApiResponse<PageResponse<Map<String, Object>>> listPendingClaims(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Map<String, Object>> result = adminService.listPendingClaims(page, pageSize);
        return ApiResponse.success(result);
    }

    /** 物品信息审核 */
    @PostMapping("/items/audit")
    public ResponseEntity<ApiResponse<Void>> auditItem(@Valid @RequestBody AdminDTO.AuditItemReq request) {
        try {
            adminService.auditItem(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    /** 认领申请审核 */
    @PostMapping("/claims/audit")
    public ResponseEntity<ApiResponse<Void>> auditClaim(@Valid @RequestBody AdminDTO.AuditClaimReq request) {
        try {
            adminService.auditClaim(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════
    // 公告管理 CRUD
    // ════════════════════════════════════════════════════════════

    /** 公告列表（分页） */
    @GetMapping("/notices")
    public ApiResponse<PageResponse<AdminDTO.NoticeResp>> listNotices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageResponse<AdminDTO.NoticeResp> result = adminService.listNotices(page, pageSize, keyword);
        return ApiResponse.success(result);
    }

    /** 新增公告 */
    @PostMapping("/notices")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createNotice(
            @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        Long id = adminService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    /** 修改公告 */
    @PutMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        // 组装 UpdateReq（含 id）
        AdminDTO.AnnouncementUpdateReq req = new AdminDTO.AnnouncementUpdateReq();
        req.setId(id);
        req.setTitle(request.getTitle());
        req.setContent(request.getContent());
        req.setPublished(request.getPublished());
        boolean updated = adminService.updateAnnouncement(req);
        if (updated) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    /** 删除公告 */
    @DeleteMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        boolean deleted = adminService.deleteAnnouncement(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    // 兼容旧路径 /announcements（保留，不影响新路径）
    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAnnouncement(
            @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        Long id = adminService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    @PutMapping("/announcements")
    public ResponseEntity<ApiResponse<Void>> updateAnnouncement(
            @Valid @RequestBody AdminDTO.AnnouncementUpdateReq request) {
        boolean updated = adminService.updateAnnouncement(request);
        if (updated) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        boolean deleted = adminService.deleteAnnouncement(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 物品分类 CRUD
    // ════════════════════════════════════════════════════════════

    /** 分类列表 */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<AdminDTO.CategoryResp>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.success(adminService.listCategories()));
    }

    /** 新增分类 */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(
            @Valid @RequestBody AdminDTO.CategoryUpsertReq request) {
        Long id = adminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    /** 修改分类 */
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.CategoryUpsertReq request) {
        try {
            request.setId(id);
            boolean updated = adminService.updateCategory(request);
            if (updated) return ResponseEntity.ok(ApiResponse.success(null));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "分类不存在"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    /** 删除分类 */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        boolean deleted = adminService.deleteCategory(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "分类不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 举报处理 CRUD
    // ════════════════════════════════════════════════════════════

    /** 举报列表（分页） */
    @GetMapping("/reports")
    public ApiResponse<PageResponse<AdminDTO.ReportResp>> listReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        PageResponse<AdminDTO.ReportResp> result = adminService.listReports(page, pageSize, status);
        return ApiResponse.success(result);
    }

    /** 处理举报 */
    @PutMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<Void>> handleReport(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.HandleReportReq request) {
        request.setReportId(id);
        boolean handled = adminService.handleReport(request);
        if (handled) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    /** 删除举报记录 */
    @DeleteMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        boolean deleted = adminService.deleteReport(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    // 兼容旧 PUT /reports
    @PutMapping("/reports")
    public ResponseEntity<ApiResponse<Void>> handleReportLegacy(
            @Valid @RequestBody AdminDTO.HandleReportReq request) {
        boolean handled = adminService.handleReport(request);
        if (handled) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 数据统计
    // ════════════════════════════════════════════════════════════

    /** 数据统计大盘 */
    @GetMapping("/stats/dashboard")
    public ApiResponse<AdminDTO.DashboardResp> dashboard() {
        return ApiResponse.success(adminService.dashboard());
    }

    /** 趋势统计 */
    @GetMapping("/statistics/trend")
    public ApiResponse<AdminDTO.StatisticsTrendResp> getStatisticsTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String type) {
        return ApiResponse.success(adminService.getStatisticsTrend(startDate, endDate, type));
    }

    // ════════════════════════════════════════════════════════════
    // 用户管理 CRUD
    // ════════════════════════════════════════════════════════════

    /** 用户列表（分页） */
    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminDTO.UserListResp>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageResponse<AdminDTO.UserListResp> result = adminService.listUsers(page, pageSize, keyword);
        return ApiResponse.success(result);
    }

    /** 新增用户 */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(
            @Valid @RequestBody AdminDTO.CreateUserReq request) {
        try {
            Long id = adminService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    /** 修改用户信息 */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.UpdateUserReq request) {
        boolean updated = adminService.updateUser(id, request);
        if (updated) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
    }

    /** 删除用户 */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        boolean deleted = adminService.deleteUser(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
    }

    /** 禁用 / 启用用户 */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.UpdateUserStatusReq request) {
        try {
            adminService.updateUserStatus(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }
}
