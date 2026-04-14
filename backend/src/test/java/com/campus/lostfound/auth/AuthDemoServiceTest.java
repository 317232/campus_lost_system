package com.campus.lostfound.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.lostfound.auth.AuthRequests.ForgotPasswordRequest;
import com.campus.lostfound.auth.AuthRequests.LoginRequest;
import com.campus.lostfound.auth.AuthRequests.ResetPasswordRequest;
import com.campus.lostfound.config.JwtProperties;
import com.campus.lostfound.demo.DemoDtos.AuthToken;
import com.campus.lostfound.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthDemoServiceTest {

    private AuthDemoService authDemoService;

    @BeforeEach
    void setUp() {
        authDemoService = new AuthDemoService(
            new JwtTokenProvider(new JwtProperties()),
            new BCryptPasswordEncoder(),
            3
        );
    }

    @Test
    void loginRequiresCaptchaAfterThreeFailures() {
        assertThrows(IllegalArgumentException.class,
            () -> authDemoService.login(new LoginRequest("2023110421", "wrong-pass", null)));
        assertThrows(IllegalArgumentException.class,
            () -> authDemoService.login(new LoginRequest("2023110421", "wrong-pass", null)));

        IllegalArgumentException thirdFailure = assertThrows(IllegalArgumentException.class,
            () -> authDemoService.login(new LoginRequest("2023110421", "wrong-pass", null)));
        assertTrue(thirdFailure.getMessage().contains("验证码"));

        IllegalArgumentException missingCaptcha = assertThrows(IllegalArgumentException.class,
            () -> authDemoService.login(new LoginRequest("2023110421", "123456", null)));
        assertTrue(missingCaptcha.getMessage().contains("验证码"));

        AuthToken token = authDemoService.login(new LoginRequest("2023110421", "123456", "1234"));
        assertNotNull(token.token());
        assertFalse(token.token().isBlank());
    }

    @Test
    void forgotPasswordVerificationAllowsResetWithoutDatabaseTable() {
        authDemoService.forgotPassword(new ForgotPasswordRequest("18800001111", "2023110421", "lin@example.com"));
        authDemoService.resetPassword(new ResetPasswordRequest("18800001111", "new-password-123"));

        AuthToken token = authDemoService.login(new LoginRequest("lin@example.com", "new-password-123", null));
        assertNotNull(token.token());
        assertFalse(token.token().isBlank());
    }
}
