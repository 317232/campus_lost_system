package com.campus.lostfound.user;

import com.campus.lostfound.common.ApiResponse;
import com.campus.lostfound.demo.DemoDataService;
import com.campus.lostfound.demo.DemoDtos.FoundDraft;
import com.campus.lostfound.demo.DemoDtos.LostDraft;
import com.campus.lostfound.demo.DemoDtos.UserProfile;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

    private final DemoDataService demoDataService;

    public ProfileController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
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
        return ApiResponse.success(demoDataService.myLostDrafts());
    }

    @GetMapping("/found-items")
    public ApiResponse<List<FoundDraft>> myFoundItems() {
        return ApiResponse.success(demoDataService.myFoundDrafts());
    }
}
