package com.campus.lostfound.auth;

import jakarta.validation.constraints.NotBlank;

public final class AuthRequests {

    private AuthRequests() {
    }

    public record RegisterRequest(@NotBlank String name, @NotBlank String studentNo,
                                  @NotBlank String phone, @NotBlank String email,
                                  @NotBlank String password) {
    }

    public record LoginRequest(@NotBlank String account, @NotBlank String password, String captcha) {
    }

    public record ForgotPasswordRequest(@NotBlank String account, @NotBlank String studentNo,
                                        @NotBlank String identityValue) {
    }

    public record ResetPasswordRequest(@NotBlank String account, @NotBlank String newPassword) {
    }
}
