package com.campus.lostfound.user.service.impl;

import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Override
    public UserProfileResponse getCurrentProfile() {
        return baseProfile();
    }

    @Override
    public UserProfileResponse updateCurrentProfile(UpdateProfileRequest request) {
        UserProfileResponse current = baseProfile();
        return new UserProfileResponse(
            current.id(),
            current.username(),
            request.displayName() == null || request.displayName().isBlank() ? current.displayName() : request.displayName(),
            current.role(),
            request.phone() == null || request.phone().isBlank() ? current.phone() : request.phone(),
            request.email() == null || request.email().isBlank() ? current.email() : request.email(),
            request.avatarUrl() == null || request.avatarUrl().isBlank() ? current.avatarUrl() : request.avatarUrl(),
            request.bio() == null || request.bio().isBlank() ? current.bio() : request.bio()
        );
    }

    private UserProfileResponse baseProfile() {
        return new UserProfileResponse(
            1001L,
            "user",
            "Student User",
            "USER",
            "18811111111",
            "user@campus.local",
            null,
            "Personal profile skeleton response"
        );
    }
}
