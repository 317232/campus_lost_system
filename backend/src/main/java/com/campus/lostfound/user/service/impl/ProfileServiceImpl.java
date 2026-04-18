package com.campus.lostfound.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
<<<<<<< HEAD
import com.campus.lostfound.domain.entity.*;
import com.campus.lostfound.mapper.*;
=======
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
>>>>>>> e6018af274081aaf01236708b32c16c95d1c734a
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserDTO;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

<<<<<<< HEAD
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public UserDTO.UserResp getCurrentUserProfile() {
        Long currentUserId = getCurrentUserId();
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            return null;
        }

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

        List<String> permissions = getUserPermissions(currentUserId);
        resp.setPermissions(permissions);

        Role role = getUserRole(currentUserId);
        if (role != null) {
            resp.setRoleCode(role.getRoleCode());
            resp.setRoleName(role.getRoleName());
        }

        return resp;
=======
    private final UserMapper userMapper;
    private final SecurityUserUtils securityUserUtils;

    public ProfileServiceImpl(UserMapper userMapper, SecurityUserUtils securityUserUtils) {
        this.userMapper = userMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public UserProfileResponse getCurrentProfile() {
        User user = resolveCurrentUser();
        return toResponse(user);
>>>>>>> e6018af274081aaf01236708b32c16c95d1c734a
    }

    @Override
    public UserProfileResponse updateCurrentProfile(UpdateProfileRequest request) {
<<<<<<< HEAD
        Long currentUserId = getCurrentUserId();
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            return null;
        }

        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setName(request.displayName());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            user.setAvatarUrl(request.avatarUrl());
        }

        userMapper.updateById(user);

        return new UserProfileResponse(
            user.getId(),
            user.getStudentNo(),
            user.getName(),
            getUserRoleCode(currentUserId),
            user.getPhone(),
            user.getEmail(),
            user.getAvatarUrl(),
            request.bio()
        );
    }

    @Override
    public List<UserDTO.UserItemResp> getUserLostItems(Long userId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getOwnerId, userId);
        queryWrapper.eq(Item::getScene, "lost");
        queryWrapper.orderByDesc(Item::getCreateTime);

        List<Item> items = itemMapper.selectList(queryWrapper);
        return items.stream().map(this::convertToUserItemResp).collect(Collectors.toList());
=======
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

        userMapper.updateById(user);
        return toResponse(user);
    }

    private User resolveCurrentUser() {
        Long currentUserId = securityUserUtils.getCurrentUserId();
        User user = null;

        if (currentUserId != null) {
            user = userMapper.selectById(currentUserId);
        }

        if (user == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        }

        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        return user;
    }

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
>>>>>>> e6018af274081aaf01236708b32c16c95d1c734a
    }

    @Override
    public List<UserDTO.UserItemResp> getUserFoundItems(Long userId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getOwnerId, userId);
        queryWrapper.eq(Item::getScene, "found");
        queryWrapper.orderByDesc(Item::getCreateTime);

        List<Item> items = itemMapper.selectList(queryWrapper);
        return items.stream().map(this::convertToUserItemResp).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO.UserStatisticsResp> getAdminStatistics() {
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        List<User> users = userMapper.selectList(userQueryWrapper);

        return users.stream().map(user -> {
            UserDTO.UserStatisticsResp stat = new UserDTO.UserStatisticsResp();
            stat.setUserId(user.getId());
            stat.setUserName(user.getName());

            LambdaQueryWrapper<Item> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(Item::getOwnerId, user.getId());
            List<Item> items = itemMapper.selectList(itemQueryWrapper);

            stat.setTotalPosts((long) items.size());
            stat.setApprovedCount(items.stream().filter(i -> "APPROVED".equals(i.getAuditStatus())).count());
            stat.setPendingCount(items.stream().filter(i -> "PENDING".equals(i.getAuditStatus())).count());
            stat.setRejectedCount(items.stream().filter(i -> "REJECTED".equals(i.getAuditStatus())).count());

            if (stat.getTotalPosts() > 0) {
                stat.setApprovalRate((double) stat.getApprovedCount() / stat.getTotalPosts() * 100);
            } else {
                stat.setApprovalRate(0.0);
            }

            return stat;
        }).collect(Collectors.toList());
    }

    private List<String> getUserPermissions(Long userId) {
        LambdaQueryWrapper<UserRole> urQueryWrapper = new LambdaQueryWrapper<>();
        urQueryWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = getUserRoles(userId);

        List<String> permissions = new ArrayList<>();
        for (UserRole ur : userRoles) {
            LambdaQueryWrapper<RolePermission> rpQueryWrapper = new LambdaQueryWrapper<>();
            rpQueryWrapper.eq(RolePermission::getRoleId, ur.getRoleId());
            List<RolePermission> rolePermissions = getRolePermissions(ur.getRoleId());

            for (RolePermission rp : rolePermissions) {
                Permission permission = permissionMapper.selectById(rp.getPermissionId());
                if (permission != null && Boolean.TRUE.equals(permission.getEnabled())) {
                    permissions.add(permission.getCode());
                }
            }
        }
        return permissions;
    }

    private Role getUserRole(Long userId) {
        List<UserRole> userRoles = getUserRoles(userId);
        if (userRoles.isEmpty()) {
            return null;
        }
        return roleMapper.selectById(userRoles.get(0).getRoleId());
    }

    private String getUserRoleCode(Long userId) {
        Role role = getUserRole(userId);
        return role != null ? role.getRoleCode() : "USER";
    }

    private List<UserRole> getUserRoles(Long userId) {
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = new ArrayList<>();
        return userRoles;
    }

    private List<RolePermission> getRolePermissions(Long roleId) {
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissions = new ArrayList<>();
        return rolePermissions;
    }

    private Long getCurrentUserId() {
        return 1L;
    }

    private UserDTO.UserItemResp convertToUserItemResp(Item item) {
        UserDTO.UserItemResp resp = new UserDTO.UserItemResp();
        resp.setId(item.getId());
        resp.setBizId(item.getBizId());
        resp.setTitle(item.getTitle());
        resp.setItemName(item.getItemName());
        resp.setCategory(item.getCategory());
        resp.setLocation(item.getLocation());
        resp.setStatus(item.getStatus());
        resp.setAuditStatus(item.getAuditStatus());
        resp.setCreateTime(item.getCreateTime());
        resp.setImageUrls(new ArrayList<>());
        return resp;
    }
}