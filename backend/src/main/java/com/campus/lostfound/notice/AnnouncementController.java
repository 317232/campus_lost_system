package com.campus.lostfound.notice;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.notice.dto.NoticeDTO;
import com.campus.lostfound.notice.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final NoticeService noticeService;

    public AnnouncementController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<Page<NoticeDTO.NoticeResp>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword) {
        return ApiResponse.success(noticeService.getNoticesPage(pageNum, pageSize, keyword, true));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeDTO.NoticeResp> detail(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getNoticeById(id));
    }
}
