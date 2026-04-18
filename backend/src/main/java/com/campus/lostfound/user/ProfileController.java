package com.campus.lostfound.user;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(profileService.getCurrentProfile());
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> update(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("profile-updated", profileService.updateCurrentProfile(request));
    }
}
