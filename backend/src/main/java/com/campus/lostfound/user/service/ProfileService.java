package com.campus.lostfound.user.service;

import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserItemSummaryResponse;
import com.campus.lostfound.user.dto.UserProfileResponse;
import java.util.List;

public interface ProfileService {

    UserProfileResponse getCurrentProfile();

    UserProfileResponse updateCurrentProfile(UpdateProfileRequest request);

    List<UserItemSummaryResponse> listMyLostItems();

    List<UserItemSummaryResponse> listMyFoundItems();
}
