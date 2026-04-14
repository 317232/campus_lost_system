package com.campus.lostfound.admin;

import com.campus.lostfound.common.ApiResponse;
import com.campus.lostfound.demo.DemoDataService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DemoDataService demoDataService;

    public AdminController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/users")
    public ApiResponse<?> users() {
        return ApiResponse.success(demoDataService.adminUsers());
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success("user-status-updated", Map.of("id", id, "payload", payload));
    }

    @GetMapping("/lost-items/review")
    public ApiResponse<?> lostReviewList() {
        return ApiResponse.success(demoDataService.reviewTasks());
    }

    @PostMapping("/lost-items/{id}/approve")
    public ApiResponse<?> approveLost(@PathVariable Long id) {
        return ApiResponse.success("lost-approved", Map.of("id", id));
    }

    @PostMapping("/lost-items/{id}/reject")
    public ApiResponse<?> rejectLost(@PathVariable Long id) {
        return ApiResponse.success("lost-rejected", Map.of("id", id));
    }

    @GetMapping("/found-items/review")
    public ApiResponse<?> foundReviewList() {
        return ApiResponse.success(demoDataService.reviewTasks());
    }

    @PostMapping("/found-items/{id}/approve")
    public ApiResponse<?> approveFound(@PathVariable Long id) {
        return ApiResponse.success("found-approved", Map.of("id", id));
    }

    @PostMapping("/found-items/{id}/reject")
    public ApiResponse<?> rejectFound(@PathVariable Long id) {
        return ApiResponse.success("found-rejected", Map.of("id", id));
    }

    @GetMapping("/claims")
    public ApiResponse<?> claims() {
        return ApiResponse.success(demoDataService.claims());
    }

    @GetMapping("/claims/{id}")
    public ApiResponse<?> claimDetail(@PathVariable Long id) {
        return ApiResponse.success(demoDataService.claims().stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElse(demoDataService.claims().get(0)));
    }

    @PostMapping("/claims/{id}/approve")
    public ApiResponse<?> approveClaim(@PathVariable Long id) {
        return ApiResponse.success("claim-approved", Map.of("id", id));
    }

    @PostMapping("/claims/{id}/reject")
    public ApiResponse<?> rejectClaim(@PathVariable Long id) {
        return ApiResponse.success("claim-rejected", Map.of("id", id));
    }

    @GetMapping("/reports")
    public ApiResponse<?> reports() {
        return ApiResponse.success(demoDataService.reports());
    }

    @PostMapping("/reports/{id}/resolve")
    public ApiResponse<?> resolveReport(@PathVariable Long id) {
        return ApiResponse.success("report-resolved", Map.of("id", id));
    }

    @GetMapping("/notices")
    public ApiResponse<?> notices() {
        return ApiResponse.success(demoDataService.notices());
    }

    @PostMapping("/notices")
    public ApiResponse<?> createNotice(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success("notice-created", payload);
    }

    @PutMapping("/notices/{id}")
    public ApiResponse<?> updateNotice(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success("notice-updated", Map.of("id", id, "payload", payload));
    }

    @PostMapping("/notices/{id}/delete")
    public ApiResponse<?> deleteNotice(@PathVariable Long id) {
        return ApiResponse.success("notice-deleted", Map.of("id", id));
    }

    @GetMapping("/statistics/overview")
    public ApiResponse<?> statisticsOverview() {
        return ApiResponse.success(demoDataService.statisticsOverview());
    }

    @GetMapping("/statistics/trend")
    public ApiResponse<?> statisticsTrend() {
        return ApiResponse.success(demoDataService.statisticsTrend());
    }
}
