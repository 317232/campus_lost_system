package com.campus.lostfound.user.service;

import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserDTO;
import com.campus.lostfound.user.dto.UserProfileResponse;

import java.util.List;

/**
 * 用户个人资料 Service 接口
 *
 * GET  /api/users/me            → getCurrentUserProfile()
 * PUT  /api/users/me            → updateCurrentProfile()
 * GET  /api/users/me/lost-items  → getUserLostItems()
 * GET  /api/users/me/found-items → getUserFoundItems()
 * GET  /api/users/statistics     → getAdminStatistics()
 */
public interface ProfileService {

    /** 获取当前登录用户完整资料（含角色/权限） */
    UserDTO.UserResp getCurrentUserProfile();

    /** 同上，保持向后兼容 */
    UserDTO.UserResp getCurrentProfile();

    /** 更新当前用户个人资料，返回更新后的简化视图 */
    UserProfileResponse updateCurrentProfile(UpdateProfileRequest request);

    /** 查询指定用户发布的失物记录 */
    List<UserDTO.UserItemResp> getUserLostItems(Long userId);

    /** 查询指定用户发布的招领记录 */
    List<UserDTO.UserItemResp> getUserFoundItems(Long userId);

    /** 全量用户发布统计（管理员用） */
    List<UserDTO.UserStatisticsResp> getAdminStatistics();
}