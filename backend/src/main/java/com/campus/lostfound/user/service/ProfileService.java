package com.campus.lostfound.user.service;

import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserDTO;
import com.campus.lostfound.user.dto.UserProfileResponse;

import java.util.List;

public interface ProfileService {

    UserDTO.UserResp getCurrentUserProfile();

    UserProfileResponse updateCurrentProfile(UpdateProfileRequest request);

    List<UserDTO.UserItemResp> getUserLostItems(Long userId);

    List<UserDTO.UserItemResp> getUserFoundItems(Long userId);

    List<UserDTO.UserStatisticsResp> getAdminStatistics();
}