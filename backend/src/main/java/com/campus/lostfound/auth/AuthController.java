package com.campus.lostfound.auth;

import com.campus.lostfound.auth.dto.AuthDTO.ForgotPasswordReq;
import com.campus.lostfound.auth.dto.AuthDTO.LoginReq;
import com.campus.lostfound.auth.dto.AuthDTO.LoginResp;
import com.campus.lostfound.auth.dto.AuthDTO.RegisterReq;
import com.campus.lostfound.auth.dto.AuthDTO.ResetPasswordReq;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.lostfound.auth.dto.AuthDTO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterReq request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResp>> login(@Valid @RequestBody LoginReq request) {
        try {
            LoginResp loginResp = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(loginResp));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PostMapping("/send-email-code")
    public ResponseEntity<ApiResponse<Void>> sendEmailCode(@Valid @RequestBody AuthDTO.SendEmailCodeReq request) {
        try {
            authService.sendEmailCode(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException | com.campus.lostfound.common.exception.BusinessException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordReq request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordReq request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(jakarta.servlet.http.HttpServletRequest request) {
        String authorization = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization);
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}