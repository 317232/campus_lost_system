package com.campus.lostfound.report;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.report.dto.ReportDTO;
import com.campus.lostfound.report.service.ReportService;
import jakarta.validation.Valid;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportDTO.ReportResp>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {
        Page<ReportDTO.ReportResp> page = reportService.getReportsPage(pageNum, pageSize, status, targetType);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDTO.ReportResp>> detail(@PathVariable Long id) {
        ReportDTO.ReportResp report = reportService.getReportById(id);
        if (report == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "举报记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody ReportDTO.CreateReportReq request) {
        try {
            reportService.createReport(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @Valid @RequestBody ReportDTO.UpdateReportReq request) {
        request.setId(id);
        boolean updated = reportService.updateReport(request);
        if (updated) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "更新失败，记录可能不存在"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        boolean deleted = reportService.deleteReport(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "删除失败，记录可能不存在"));
    }

    // ════════════════════════════════════════════════════════════
    // 用户查看自己的举报记录 → /api/reports/me
    // ════════════════════════════════════════════════════════════

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<ReportDTO.ReportResp>>> myReports(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        try {
            return ResponseEntity.ok(ApiResponse.success(reportService.getMyReports(pageNum, pageSize, status)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }
}