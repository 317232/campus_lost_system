package com.campus.lostfound.user;

import com.campus.lostfound.common.api.ApiResponse;
<<<<<<< HEAD
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserDTO;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
=======
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import jakarta.validation.Valid;
>>>>>>> e6018af274081aaf01236708b32c16c95d1c734a
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

<<<<<<< HEAD
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO.UserResp>> me() {
        UserDTO.UserResp profile = profileService.getCurrentUserProfile();
        if (profile == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> update(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = profileService.updateCurrentProfile(request);
        if (profile == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/me/lost-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> myLostItems() {
        List<UserDTO.UserItemResp> items = profileService.getUserLostItems(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/me/found-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> myFoundItems() {
        List<UserDTO.UserItemResp> items = profileService.getUserFoundItems(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(items));
=======
    @GetMapping
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(profileService.getCurrentProfile());
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> update(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("profile-updated", profileService.updateCurrentProfile(request));
>>>>>>> e6018af274081aaf01236708b32c16c95d1c734a
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<UserDTO.UserStatisticsResp>>> adminStatistics() {
        List<UserDTO.UserStatisticsResp> statistics = profileService.getAdminStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/{userId}/lost-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> userLostItems(@PathVariable Long userId) {
        List<UserDTO.UserItemResp> items = profileService.getUserLostItems(userId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{userId}/found-items")
    public ResponseEntity<ApiResponse<List<UserDTO.UserItemResp>>> userFoundItems(@PathVariable Long userId) {
        List<UserDTO.UserItemResp> items = profileService.getUserFoundItems(userId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}