package com.campus.lostfound.admin;

import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.admin.service.AdminService;
import com.campus.lostfound.admin.service.AnnouncementAdminService;
import com.campus.lostfound.admin.service.ClaimAuditService;
import com.campus.lostfound.admin.service.DashboardQueryService;
import com.campus.lostfound.admin.service.ItemAuditService;
import com.campus.lostfound.claim.dto.ClaimDTO;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.item.ItemService;
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
    private final ItemAuditService itemAuditService;
    private final ClaimAuditService claimAuditService;
    private final AnnouncementAdminService announcementAdminService;
    private final DashboardQueryService dashboardQueryService;
    private final ItemService itemService;

    public AdminController(AdminService adminService,
                          ItemAuditService itemAuditService,
                          ClaimAuditService claimAuditService,
                          AnnouncementAdminService announcementAdminService,
                          DashboardQueryService dashboardQueryService,
                          ItemService itemService) {
        this.adminService = adminService;
        this.itemAuditService = itemAuditService;
        this.claimAuditService = claimAuditService;
        this.announcementAdminService = announcementAdminService;
        this.dashboardQueryService = dashboardQueryService;
        this.itemService = itemService;
    }

    // ════════════════════════════════════════════════════════════
    // 物品审核 / 认领审核 → ItemAuditService / ClaimAuditService
    // ════════════════════════════════════════════════════════════

    @GetMapping("/items/review")
    public ApiResponse<PageResponse<Map<String, Object>>> listPendingItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String scene) {
        PageResponse<Map<String, Object>> result = itemAuditService.listPendingItems(page, pageSize, scene);
        return ApiResponse.success(result);
    }

    @GetMapping("/claims/review")
    public ApiResponse<PageResponse<Map<String, Object>>> listPendingClaims(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Map<String, Object>> result = claimAuditService.listPendingClaims(page, pageSize);
        return ApiResponse.success(result);
    }

    @PostMapping("/items/audit")
    public ResponseEntity<ApiResponse<Void>> auditItem(@Valid @RequestBody AdminDTO.AuditItemReq request) {
        try {
            itemAuditService.auditItem(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/claims/audit")
    public ResponseEntity<ApiResponse<Void>> auditClaim(@Valid @RequestBody AdminDTO.AuditClaimReq request) {
        try {
            claimAuditService.auditClaim(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/claims/{claimId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String auditRemark) {
        try {
            claimAuditService.approveClaim(claimId, auditRemark);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/claims/{claimId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String auditRemark) {
        try {
            claimAuditService.rejectClaim(claimId, auditRemark);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/claims/{claimId}")
    public ResponseEntity<ApiResponse<ClaimDTO.ClaimAuditVO>> getClaimAuditDetail(@PathVariable Long claimId) {
        try {
            ClaimDTO.ClaimAuditVO detail = claimAuditService.getClaimAuditDetail(claimId);
            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ResultCode.NOT_FOUND, "认领申请不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════
    // 管理员物品下线/删除 → ItemService
    // ════════════════════════════════════════════════════════════

    @PutMapping("/items/{id}/offline")
    public ResponseEntity<ApiResponse<Void>> setItemOffline(@PathVariable Long id) {
        try {
            itemService.setOffline(id);
            return ResponseEntity.ok(ApiResponse.success("物品已下线", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> adminDeleteItem(@PathVariable Long id) {
        try {
            itemService.adminDeleteItem(id);
            return ResponseEntity.ok(ApiResponse.success("物品已删除", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════════════
    // 公告管理 CRUD → AnnouncementAdminService
    // ════════════════════════════════════════════════════════════

    @GetMapping("/notices")
    public ApiResponse<PageResponse<AdminDTO.NoticeResp>> listNotices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageResponse<AdminDTO.NoticeResp> result = announcementAdminService.listNotices(page, pageSize, keyword);
        return ApiResponse.success(result);
    }

    @PostMapping("/notices")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createNotice(
            @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        Long id = announcementAdminService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    @PutMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        AdminDTO.AnnouncementUpdateReq req = new AdminDTO.AnnouncementUpdateReq();
        req.setId(id);
        req.setTitle(request.getTitle());
        req.setContent(request.getContent());
        req.setPublished(request.getPublished());
        boolean updated = announcementAdminService.updateAnnouncement(req);
        if (updated) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        boolean deleted = announcementAdminService.deleteAnnouncement(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    // 兼容旧路径 /announcements
    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAnnouncement(
            @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        Long id = announcementAdminService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    @PutMapping("/announcements")
    public ResponseEntity<ApiResponse<Void>> updateAnnouncement(
            @Valid @RequestBody AdminDTO.AnnouncementUpdateReq request) {
        boolean updated = announcementAdminService.updateAnnouncement(request);
        if (updated) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        boolean deleted = announcementAdminService.deleteAnnouncement(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 物品分类 CRUD → AdminService (保留)
    // ════════════════════════════════════════════════════════════

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<AdminDTO.CategoryResp>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.success(adminService.listCategories()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(
            @Valid @RequestBody AdminDTO.CategoryUpsertReq request) {
        Long id = adminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

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

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        boolean deleted = adminService.deleteCategory(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "分类不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 举报处理 CRUD → AdminService (保留)
    // ════════════════════════════════════════════════════════════

    @GetMapping("/reports")
    public ApiResponse<PageResponse<AdminDTO.ReportResp>> listReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        PageResponse<AdminDTO.ReportResp> result = adminService.listReports(page, pageSize, status);
        return ApiResponse.success(result);
    }

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

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        boolean deleted = adminService.deleteReport(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    @PutMapping("/reports")
    public ResponseEntity<ApiResponse<Void>> handleReportLegacy(
            @Valid @RequestBody AdminDTO.HandleReportReq request) {
        boolean handled = adminService.handleReport(request);
        if (handled) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    @PostMapping("/reports/handle")
    public ResponseEntity<ApiResponse<Void>> handleReportLegacyPost(
            @Valid @RequestBody AdminDTO.HandleReportReq request) {
        boolean handled = adminService.handleReport(request);
        if (handled) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 数据统计 → DashboardQueryService
    // ════════════════════════════════════════════════════════════

    @GetMapping("/stats/dashboard")
    public ApiResponse<AdminDTO.DashboardResp> dashboard() {
        return ApiResponse.success(dashboardQueryService.dashboard());
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDTO.DashboardResp> dashboardLegacy() {
        return dashboard();
    }

    @GetMapping("/statistics/trend")
    public ApiResponse<AdminDTO.StatisticsTrendResp> getStatisticsTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String type) {
        return ApiResponse.success(dashboardQueryService.getStatisticsTrend(startDate, endDate, type));
    }

    // ════════════════════════════════════════════════════════════
    // 用户管理 CRUD → AdminService (保留)
    // ════════════════════════════════════════════════════════════

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminDTO.UserListResp>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageResponse<AdminDTO.UserListResp> result = adminService.listUsers(page, pageSize, keyword);
        return ApiResponse.success(result);
    }

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

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminDTO.UpdateUserReq request) {
        boolean updated = adminService.updateUser(id, request);
        if (updated) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        boolean deleted = adminService.deleteUser(id);
        if (deleted) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
    }

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
