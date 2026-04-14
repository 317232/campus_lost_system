package com.campus.lostfound.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 认证模块相关 DTO
 */
public class AuthDTO {
    @Data
    public static class RegisterReq {
        @NotBlank(message = "学号不能为空")
        private String studentNo;
        @NotBlank(message = "姓名不能为空")
        private String name;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String phone;
        @Email(message = "邮箱格式不正确")
        private String email;
    }

    @Data
    public static class LoginReq {
        @NotBlank(message = "账号(学号/邮箱/手机号)不能为空")
        private String account;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class LoginResp {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String name;
        private String avatarUrl;
    }

    @Data
    public static class RefreshTokenReq {
        @NotBlank(message = "刷新令牌不能为空")
        private String refreshToken;
    }

    @Data
    public static class ForgotPasswordReq {
        @NotBlank(message = "学号/邮箱不能为空")
        private String account;
        // 在实际业务中，这里通常会触发发送验证码的操作
    }

    @Data
    public static class ResetPasswordReq {
        @NotBlank(message = "账号不能为空")
        private String account;
        @NotBlank(message = "验证码不能为空")
        private String verifyCode;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
