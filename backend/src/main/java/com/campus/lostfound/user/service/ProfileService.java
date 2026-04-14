package com.campus.lostfound.user.service;

import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserProfileResponse;

public interface ProfileService {

    UserProfileResponse getCurrentProfile();

    UserProfileResponse updateCurrentProfile(UpdateProfileRequest request);
}
