package com.campus.lostfound.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProfileServiceImpl implements ProfileService {

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
    }

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
    }
}
