package com.campus.lostfound.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 32, message = "Display name must be at most 32 characters")
    String displayName,

    @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "Phone format is invalid")
    String phone,

    @Email(message = "Email format is invalid")
    String email,

    @Size(max = 255, message = "Avatar URL must be at most 255 characters")
    String avatarUrl,

    @Size(max = 255, message = "Bio must be at most 255 characters")
    String bio
) {
}
