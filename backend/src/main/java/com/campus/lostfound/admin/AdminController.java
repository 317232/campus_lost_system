package com.campus.lostfound.admin;

import com.campus.lostfound.auth.AuthController;
import com.campus.lostfound.common.api.ApiResponse;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.lostfound.user.service.ProfileService;
import com.campus.lostfound.claim.service.ClaimService;


/**
 * 管理员控制器
 * 提供管理员操作的接口
 * 包括用户管理、物品审核、招领物品审核、物品管理、举报审核、公告管理
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthController authController;
    
    private final ProfileService profileService;

    public AdminController(ProfileService profileService, AuthController authController) {
        this.profileService = profileService;
        this.authController = authController;
    }

    @GetMapping("/users")
    public ApiResponse<?> users() {
        return ApiResponse.success(profileService.adminUsers());
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success("user-status-updated", Map.of("id", id, "payload", payload));
    }

    @GetMapping("/lost-items/review")
    public ApiResponse<?> lostReviewList() {
        // 获取待审核的物品列表
        return ApiResponse.success(profileService.reviewTasks());
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
        // 获取招领物品的待审核的物品列表
        return ApiResponse.success(profileService.reviewTasks());
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
        return ApiResponse.success(profileService.claims());
    }

    @GetMapping("/claims/{id}")
    public ApiResponse<?> claimDetail(@PathVariable Long id) {
        return ApiResponse.success(profileService.claims().stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElse(profileService.claims().get(0)));
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
