package com.campus.lostfound.notice;

import com.campus.lostfound.common.ApiResponse;
import com.campus.lostfound.demo.DemoDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final DemoDataService demoDataService;

    public NoticeController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(demoDataService.notices());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(demoDataService.notices().stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElse(demoDataService.notices().get(0)));
    }
}
