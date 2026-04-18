package com.campus.lostfound.user;


import com.campus.lostfound.user.service.ProfileService;
import com.campus.lostfound.common.api.ApiResponse;

import java.util.List;
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
    public ApiResponse<UserProfile> me() {
        return ApiResponse.success(demoDataService.currentUser());
    }

    @PutMapping
    public ApiResponse<UserProfile> update(@RequestBody UserProfile profile) {
        return ApiResponse.success("profile-updated", profile);
    }

    @GetMapping("/lost-items")
    public ApiResponse<List<LostDraft>> myLostItems() {
        return ApiResponse.success(profileService.myLostDrafts());
    }

    @GetMapping("/found-items")
    public ApiResponse<List<FoundDraft>> myFoundItems() {
        return ApiResponse.success(profileService.myFoundDrafts());
    }
}
