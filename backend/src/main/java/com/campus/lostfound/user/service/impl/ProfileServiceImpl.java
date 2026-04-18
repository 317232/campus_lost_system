package com.campus.lostfound.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import com.campus.lostfound.user.dto.UpdateProfileRequest;
import com.campus.lostfound.user.dto.UserItemSummaryResponse;
import com.campus.lostfound.user.dto.UserProfileResponse;
import com.campus.lostfound.user.service.ProfileService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final SecurityUserUtils securityUserUtils;

    public ProfileServiceImpl(UserMapper userMapper,
                              ItemMapper itemMapper,
                              SecurityUserUtils securityUserUtils) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public UserProfileResponse getCurrentProfile() {
        User user = resolveCurrentUser();
        return toResponse(user);
    }

    @Override
    public UserProfileResponse updateCurrentProfile(UpdateProfileRequest request) {
        Long currentUserId = requireCurrentUserId();

        QueryWrapper<User> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("id", currentUserId);

        User user = new User();
        writeField(user, "id", currentUserId);

        if (StringUtils.hasText(request.displayName())) {
            writeField(user, "name", request.displayName().trim());
        }
        if (StringUtils.hasText(request.phone())) {
            writeField(user, "phone", request.phone().trim());
        }
        if (StringUtils.hasText(request.email())) {
            writeField(user, "email", request.email().trim());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            writeField(user, "avatarUrl", request.avatarUrl().trim());
        }

        userMapper.update(user, updateWrapper);
        return toResponse(resolveCurrentUser());
    }

    @Override
    public List<UserItemSummaryResponse> listMyLostItems() {
        return listMyItemsByScene("lost");
    }

    @Override
    public List<UserItemSummaryResponse> listMyFoundItems() {
        return listMyItemsByScene("found");
    }

    private List<UserItemSummaryResponse> listMyItemsByScene(String scene) {
        Long currentUserId = requireCurrentUserId();
        List<Map<String, Object>> rows = itemMapper.selectMaps(new QueryWrapper<Item>()
            .select("id", "biz_id", "item_name", "scene", "location", "status", "created_at")
            .eq("owner_id", currentUserId)
            .eq("scene", scene)
            .orderByDesc("id"));

        return rows.stream().map(this::toSummary).toList();
    }

    private User resolveCurrentUser() {
        Long currentUserId = requireCurrentUserId();
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        return user;
    }

    private Long requireCurrentUserId() {
        Long currentUserId = securityUserUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        return currentUserId;
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
            readLong(user, "id"),
            readString(user, "studentNo"),
            readString(user, "name"),
            "USER",
            readString(user, "phone"),
            readString(user, "email"),
            readString(user, "avatarUrl"),
            readString(user, "major")
        );
    }

    private UserItemSummaryResponse toSummary(Map<String, Object> row) {
        return new UserItemSummaryResponse(
            asLong(row.get("id")),
            asString(row.get("biz_id")),
            asString(row.get("item_name")),
            asString(row.get("scene")),
            asString(row.get("location")),
            asString(row.get("status")),
            asDateTime(row.get("created_at"))
        );
    }

    private void writeField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("字段写入失败: " + fieldName, e);
        }
    }

    private String readString(Object source, String fieldName) {
        Object value = readField(source, fieldName);
        return value == null ? null : value.toString();
    }

    private Long readLong(Object source, String fieldName) {
        Object value = readField(source, fieldName);
        return asLong(value);
    }

    private Object readField(Object source, String fieldName) {
        try {
            var field = source.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(source);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("字段读取失败: " + fieldName, e);
        }
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime asDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        return null;
    }
}
