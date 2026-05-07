package com.campus.lostfound.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.common.exception.BusinessException;
import com.campus.lostfound.domain.entity.*;
import com.campus.lostfound.mapper.*;
import com.campus.lostfound.security.SecurityUserUtils;
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserDTO;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户个人资料 Service 实现
 *
 * 修复记录：
 *  - 移除混用 @Autowired + 显式构造器的问题，改为纯构造器注入
 *  - 修复 getCurrentUserId() 硬编码 1L → 使用 SecurityUserUtils
 *  - 补全缺失的 getUserLostItems() 实现
 *  - 补全 bio → major 字段的更新写入
 */
@lombok.extern.slf4j.Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final ItemMapper itemMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final SecurityUserUtils securityUserUtils;

    public ProfileServiceImpl(UserMapper userMapper,
                              RoleMapper roleMapper,
                              PermissionMapper permissionMapper,
                              ItemMapper itemMapper,
                              UserRoleMapper userRoleMapper,
                              RolePermissionMapper rolePermissionMapper,
                              SecurityUserUtils securityUserUtils) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.itemMapper = itemMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.securityUserUtils = securityUserUtils;
    }

    // ─── 接口方法：GET /api/users/me ──────────────────────────
    @Override
    public UserDTO.UserResp getCurrentUserProfile() {
        Long currentUserId = getCurrentUserId();
        log.info("DEBUG getCurrentUserProfile: currentUserId={}", currentUserId);
        if (currentUserId == null) {
            return null;
        }
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            return null;
        }
        return buildUserResp(user);
    }

    /**
     * getCurrentProfile() 是 ProfileController#update 之外的第二个接口方法，
     * 保持与 getCurrentUserProfile() 行为一致，返回 UserDTO.UserResp 以统一响应格式。
     * （接口声明已调整为 UserDTO.UserResp，详见 ProfileService.java）
     */
    @Override
    public UserDTO.UserResp getCurrentProfile() {
        return getCurrentUserProfile();
    }

    // ─── 接口方法：PUT /api/users/me ──────────────────────────
    @Override
    public UserProfileResponse updateCurrentProfile(UpdateProfileRequest request) {
        User user = resolveCurrentUser();

        if (StringUtils.hasText(request.displayName())) {
            user.setName(request.displayName().trim());
        }
        if (StringUtils.hasText(request.phone())) {
            user.setPhone(request.phone().trim());
        }
        if (StringUtils.hasText(request.email())) {
            user.setEmail(request.email().trim());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            user.setAvatarUrl(request.avatarUrl().trim());
        }
        // bio 字段对应 User.major
        if (StringUtils.hasText(request.bio())) {
            user.setMajor(request.bio().trim());
        }

        userMapper.updateById(user);
        return toResponse(user);
    }

    // ─── 接口方法：GET /api/users/me/lost-items ──────────────
    @Override
    public List<UserDTO.UserItemResp> getUserLostItems(Long userId) {
        LambdaQueryWrapper<Item> qw = new LambdaQueryWrapper<>();
        qw.eq(Item::getOwnerId, userId);
        qw.eq(Item::getScene, "LOST");
        qw.orderByDesc(Item::getCreateTime);
        return itemMapper.selectList(qw)
                .stream()
                .map(this::convertToUserItemResp)
                .collect(Collectors.toList());
    }

    // ─── 接口方法：GET /api/users/me/found-items ─────────────
    @Override
    public List<UserDTO.UserItemResp> getUserFoundItems(Long userId) {
        LambdaQueryWrapper<Item> qw = new LambdaQueryWrapper<>();
        qw.eq(Item::getOwnerId, userId);
        qw.eq(Item::getScene, "FOUND");
        qw.orderByDesc(Item::getCreateTime);
        return itemMapper.selectList(qw)
                .stream()
                .map(this::convertToUserItemResp)
                .collect(Collectors.toList());
    }

    // ─── 接口方法：GET /api/users/statistics ─────────────────
    @Override
    public List<UserDTO.UserStatisticsResp> getAdminStatistics() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        return users.stream().map(user -> {
            UserDTO.UserStatisticsResp stat = new UserDTO.UserStatisticsResp();
            stat.setUserId(user.getId());
            stat.setUserName(user.getName());

            List<Item> items = itemMapper.selectList(
                    new LambdaQueryWrapper<Item>().eq(Item::getOwnerId, user.getId()));

            stat.setTotalPosts((long) items.size());
            stat.setApprovedCount(items.stream().filter(i -> "PUBLISHED".equals(i.getStatus())).count());
            stat.setPendingCount(items.stream().filter(i -> "PENDING_REVIEW".equals(i.getStatus())).count());
            stat.setRejectedCount(items.stream().filter(i -> "REJECTED".equals(i.getStatus())).count());
            stat.setApprovalRate(stat.getTotalPosts() > 0
                    ? (double) stat.getApprovedCount() / stat.getTotalPosts() * 100
                    : 0.0);
            return stat;
        }).collect(Collectors.toList());
    }

    // ─── 私有辅助 ─────────────────────────────────────────────

    /** 获取当前登录用户 ID，优先从 JWT Security 上下文取 */
    private Long getCurrentUserId() {
        return securityUserUtils.getCurrentUserId();
    }

    /**
     * 解析当前用户实体
     * @throws BusinessException 如果用户未登录或不存在
     */
    private User resolveCurrentUser() {
        Long uid = getCurrentUserId();
        if (uid == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录，请先登录");
        }
        
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    /** 将 User 实体映射为 UserDTO.UserResp（含角色/权限） */
    private UserDTO.UserResp buildUserResp(User user) {
        UserDTO.UserResp resp = new UserDTO.UserResp();
        resp.setId(user.getId());
        resp.setStudentNo(user.getStudentNo());
        resp.setName(user.getName());
        resp.setMajor(user.getMajor());
        resp.setPhone(user.getPhone());
        resp.setEmail(user.getEmail());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        resp.setPermissions(getUserPermissions(user.getId()));

        Role role = getUserRole(user.getId());
        if (role != null) {
            resp.setRoleCode(role.getRoleCode());
            resp.setRoleName(role.getRoleName());
        }
        return resp;
    }

    /** 将 User 实体映射为 UserProfileResponse（简化版，用于 update 响应） */
    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getStudentNo(),
                user.getName(),
                "USER",
                user.getPhone(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getMajor()
        );
    }

    private List<String> getUserPermissions(Long userId) {
        List<String> permissions = new ArrayList<>();
        for (UserRole ur : getUserRoles(userId)) {
            for (RolePermission rp : getRolePermissions(ur.getRoleId())) {
                Permission perm = permissionMapper.selectById(rp.getPermissionId());
                if (perm != null) {
                    permissions.add(perm.getPermCode());
                }
            }
        }
        return permissions;
    }

    private Role getUserRole(Long userId) {
        List<UserRole> userRoles = getUserRoles(userId);
        return userRoles.isEmpty() ? null : roleMapper.selectById(userRoles.get(0).getRoleId());
    }

    private List<UserRole> getUserRoles(Long userId) {
        return userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    private List<RolePermission> getRolePermissions(Long roleId) {
        return rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
    }

    private UserDTO.UserItemResp convertToUserItemResp(Item item) {
        UserDTO.UserItemResp resp = new UserDTO.UserItemResp();
        resp.setId(item.getId());
        resp.setBizId(item.getBizId());
        resp.setTitle(item.getTitle());
        resp.setItemName(item.getItemName());
        resp.setCategory(null);
        resp.setLocation(item.getLocation());
        resp.setStatus(item.getStatus());
        resp.setCreateTime(item.getCreateTime());
        resp.setImageUrls(new ArrayList<>());
        return resp;
    }
}