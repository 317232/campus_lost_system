package com.campus.lostfound.admin;

import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.admin.service.AdminService;
import com.campus.lostfound.common.api.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** 信息审核-获取待审核列表 */
    @GetMapping("/items/pending")
    public ApiResponse<List<Map<String, Object>>> pendingItems() {
        return ApiResponse.success(adminService.listPendingItems());
    }

    /** 信息审核-提交审核（兼容 PUT/POST） */
    @PutMapping("/items/audit")
    @PostMapping("/items/audit")
    public ResponseEntity<ApiResponse<Void>> auditItem(@Valid @RequestBody AdminDTO.AuditItemReq request) {
        try {
            adminService.auditItem(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    /** 认领审核（兼容 PUT/POST） */
    @PutMapping("/claims/audit")
    @PostMapping("/claims/audit")
    public ResponseEntity<ApiResponse<Void>> auditClaim(@Valid @RequestBody AdminDTO.AuditClaimReq request) {
        try {
            adminService.auditClaim(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    /** 用户管理 */
    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        return ApiResponse.success(adminService.listUsers());
    }

    @PutMapping("/users/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = payload.get("userId") == null ? null : Long.parseLong(payload.get("userId").toString());
            String status = payload.get("status") == null ? null : payload.get("status").toString();
            if (userId == null || status == null) {
                return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, "userId/status 不能为空"));
            }
            adminService.updateUserStatus(userId, status);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    /** 公告管理-发布 */
    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAnnouncement(
        @Valid @RequestBody AdminDTO.AnnouncementCreateReq request) {
        Long id = adminService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    /** 公告管理-修改 */
    @PutMapping("/announcements")
    public ResponseEntity<ApiResponse<Void>> updateAnnouncement(
        @Valid @RequestBody AdminDTO.AnnouncementUpdateReq request) {
        boolean updated = adminService.updateAnnouncement(request);
        if (updated) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    /** 公告管理-下架/删除 */
    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        boolean deleted = adminService.deleteAnnouncement(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
    }

    /** 分类维护-新增 */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(
        @Valid @RequestBody AdminDTO.CategoryUpsertReq request) {
        Long id = adminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("id", id)));
    }

    /** 分类维护-修改 */
    @PutMapping("/categories")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
        @Valid @RequestBody AdminDTO.CategoryUpsertReq request) {
        try {
            boolean updated = adminService.updateCategory(request);
            if (updated) {
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "分类不存在"));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    /** 数据统计 */
    @RequestMapping(value = {"/statistics", "/stats/dashboard"}, method = RequestMethod.GET)
    public ApiResponse<AdminDTO.DashboardResp> dashboard() {
        return ApiResponse.success(adminService.dashboard());
    }

    /** 反馈处理 */
    @RequestMapping(value = {"/reports/handle", "/reports"}, method = RequestMethod.PUT)
    public ResponseEntity<ApiResponse<Void>> handleReport(@Valid @RequestBody AdminDTO.HandleReportReq request) {
        boolean handled = adminService.handleReport(request);
        if (handled) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
    }

    /** 操作日志 */
    @GetMapping("/logs")
    public ApiResponse<List<Map<String, Object>>> logs() {
        return ApiResponse.success(adminService.listOperationLogs());
    }
}
