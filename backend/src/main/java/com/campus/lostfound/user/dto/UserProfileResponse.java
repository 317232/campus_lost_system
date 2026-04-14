package com.campus.lostfound.user.dto;

public record UserProfileResponse(
    Long id,
    String username,
    String displayName,
    String role,
    String phone,
    String email,
    String avatarUrl,
    String bio
) {
}
