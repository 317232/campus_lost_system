package com.campus.lostfound.user;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.domain.entity.UserRole;
import com.campus.lostfound.security.SecurityUserUtils;
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserDTO;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户个人资料控制器
 * 基础路径: /api/users
 */
@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;
    private final SecurityUserUtils securityUserUtils;

    public ProfileController(ProfileService profileService,
                             SecurityUserUtils securityUserUtils) {
        this.profileService = profileService;
        this.securityUserUtils = securityUserUtils;
    }

    /** 获取当前登录用户资料 */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO.UserResp>> me() {
        UserDTO.UserResp profile = profileService.getCurrentUserProfile();
        if (profile == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /** 更新当前登录用户资料 */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> update(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = profileService.updateCurrentProfile(request);
        if (profile == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /** 当前用户发布的失物列表 */
    @GetMapping("/me/lost-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> myLostItems() {
        Long uid = getCurrentUserId();
        List<UserDTO.UserItemResp> items = profileService.getUserLostItems(uid);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /** 当前用户发布的招领列表 */
    @GetMapping("/me/found-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> myFoundItems() {
        Long uid = getCurrentUserId();
        List<UserDTO.UserItemResp> items = profileService.getUserFoundItems(uid);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /** 全量用户统计（管理员调用） */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<UserDTO.UserStatisticsResp>>> adminStatistics() {
        List<UserDTO.UserStatisticsResp> statistics = profileService.getAdminStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /** 查看指定用户发布的失物列表 - 权限控制：仅本人或管理员可查看完整信息 */
    @GetMapping("/{userId}/lost-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> userLostItems(
            @PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(userId);
        boolean isAdmin = isCurrentUserAdmin();
        
        List<UserDTO.UserItemResp> items = profileService.getUserLostItems(userId);
        
        // 非本人且非管理员，只能看到脱敏后的信息（隐藏联系方式等敏感信息）
        if (!isOwner && !isAdmin) {
            items = items.stream().map(item -> {
                item.setContact(null); // 隐藏联系方式
                item.setDescription(item.getDescription() != null && item.getDescription().length() > 20 
                    ? item.getDescription().substring(0, 20) + "..." : item.getDescription());
                return item;
            }).toList();
        }
        
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /** 查看指定用户发布的招领列表 - 权限控制：仅本人或管理员可查看完整信息 */
    @GetMapping("/{userId}/found-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> userFoundItems(
            @PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(userId);
        boolean isAdmin = isCurrentUserAdmin();
        
        List<UserDTO.UserItemResp> items = profileService.getUserFoundItems(userId);
        
        // 非本人且非管理员，只能看到脱敏后的信息
        if (!isOwner && !isAdmin) {
            items = items.stream().map(item -> {
                item.setContact(null); // 隐藏联系方式
                item.setDescription(item.getDescription() != null && item.getDescription().length() > 20 
                    ? item.getDescription().substring(0, 20) + "..." : item.getDescription());
                return item;
            }).toList();
        }
        
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    /**
     * 判断当前用户是否为管理员
     */
    private boolean isCurrentUserAdmin() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    /**
     * 从 JWT Security 上下文获取当前用户 ID。
     * 未登录时 fallback 为 1L（便于开发期无 token 测试）。
     */
    private Long getCurrentUserId() {
        Long uid = securityUserUtils.getCurrentUserId();
        return uid != null ? uid : 1L;
    }
}