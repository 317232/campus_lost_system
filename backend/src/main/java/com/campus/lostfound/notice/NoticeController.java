package com.campus.lostfound.notice;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.notice.dto.NoticeDTO;
import com.campus.lostfound.notice.service.AnnouncementService;
import com.campus.lostfound.security.SecurityUserUtils;
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
@RequestMapping("/api/notices")
public class NoticeController {

    private final AnnouncementService noticeService;
    private final SecurityUserUtils securityUserUtils;

    public NoticeController(AnnouncementService noticeService, SecurityUserUtils securityUserUtils) {
        this.noticeService = noticeService;
        this.securityUserUtils = securityUserUtils;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeDTO.NoticeResp>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean published) {
        Page<NoticeDTO.NoticeResp> page = noticeService.getNoticesPage(pageNum, pageSize, keyword, published);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDTO.NoticeResp>> detail(@PathVariable Long id) {
        NoticeDTO.NoticeResp notice = noticeService.getNoticeById(id);
        if (notice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "公告不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(notice));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody NoticeDTO.CreateNoticeReq request) {
        if (!securityUserUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ResultCode.FORBIDDEN, "需要管理员权限"));
        }
        try {
            noticeService.createNotice(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @Valid @RequestBody NoticeDTO.UpdateNoticeReq request) {
        if (!securityUserUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ResultCode.FORBIDDEN, "需要管理员权限"));
        }
        request.setId(id);
        boolean updated = noticeService.updateNotice(request);
        if (updated) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "更新失败，公告可能不存在"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!securityUserUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ResultCode.FORBIDDEN, "需要管理员权限"));
        }
        boolean deleted = noticeService.deleteNotice(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "删除失败，公告可能不存在"));
    }
}