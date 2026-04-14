package com.campus.lostfound.auth;

import com.campus.lostfound.auth.AuthRequests.ForgotPasswordRequest;
import com.campus.lostfound.auth.AuthRequests.LoginRequest;
import com.campus.lostfound.auth.AuthRequests.RegisterRequest;
import com.campus.lostfound.auth.AuthRequests.ResetPasswordRequest;
import com.campus.lostfound.common.enums.UserRole;
import com.campus.lostfound.demo.DemoDtos.AuthToken;
import com.campus.lostfound.security.JwtTokenProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthDemoService {

    private static final String CAPTCHA_CODE = "1234";
    private static final Duration RESET_GRANT_TTL = Duration.ofMinutes(10);

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final int captchaThreshold;
    private final AtomicLong nextUserId = new AtomicLong(100L);
    private final Map<String, DemoAccount> accountsByIdentifier = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> resetGrants = new ConcurrentHashMap<>();

    public AuthDemoService(JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,
                           @Value("${app.security.login-failure-captcha-threshold:3}") int captchaThreshold) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.captchaThreshold = captchaThreshold;
        seedAccounts();
    }

    public Map<String, Object> register(RegisterRequest request) {
        String studentNo = normalizeIdentifier(request.studentNo());
        String phone = normalizeIdentifier(request.phone());
        String email = normalizeIdentifier(request.email());

        ensureIdentifierAvailable(studentNo, "学号已被注册");
        ensureIdentifierAvailable(phone, "手机号已被注册");
        ensureIdentifierAvailable(email, "邮箱已被注册");

        DemoAccount account = new DemoAccount(
            nextUserId.incrementAndGet(),
            request.name().trim(),
            studentNo,
            phone,
            email,
            passwordEncoder.encode(request.password()),
            UserRole.USER
        );
        indexAccount(account);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", account.userId());
        payload.put("name", account.name());
        payload.put("studentNo", account.studentNo());
        payload.put("phone", account.phone());
        payload.put("email", account.email());
        payload.put("role", account.role().name());
        payload.put("status", "NORMAL");
        return payload;
    }

    public AuthToken login(LoginRequest request) {
        String identifier = normalizeIdentifier(request.account());
        DemoAccount account = accountsByIdentifier.get(identifier);

        if (requiresCaptcha(identifier) && !CAPTCHA_CODE.equals(normalizeIdentifier(request.captcha()))) {
            throw new IllegalArgumentException("该账号已连续失败 3 次，请输入验证码 1234。");
        }

        if (account == null || !passwordEncoder.matches(request.password(), account.passwordHash())) {
            int failCount = failedAttempts.merge(identifier, 1, Integer::sum);
            if (failCount >= captchaThreshold) {
                throw new IllegalArgumentException("账号或密码错误，已达到 3 次失败，请输入验证码 1234。");
            }
            throw new IllegalArgumentException("账号或密码错误。");
        }

        clearAttempts(account);
        String token = jwtTokenProvider.createToken(account.userId(), account.studentNo(), account.role().name());
        return new AuthToken(token, account.role(), account.name());
    }

    public Map<String, Object> forgotPassword(ForgotPasswordRequest request) {
        String accountIdentifier = normalizeIdentifier(request.account());
        DemoAccount account = accountsByIdentifier.get(accountIdentifier);

        if (account == null) {
            throw new IllegalArgumentException("未找到对应账号。");
        }

        if (!account.studentNo().equals(normalizeIdentifier(request.studentNo()))) {
            throw new IllegalArgumentException("学号核验失败。");
        }

        String identityValue = normalizeIdentifier(request.identityValue());
        boolean identityMatched = identityValue.equals(account.phone()) || identityValue.equals(account.email());
        if (!identityMatched) {
            throw new IllegalArgumentException("绑定信息核验失败，请输入已绑定的手机号或邮箱。");
        }

        resetGrants.put(accountIdentifier, Instant.now().plus(RESET_GRANT_TTL));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("account", request.account().trim());
        payload.put("canReset", true);
        payload.put("expiresInMinutes", RESET_GRANT_TTL.toMinutes());
        return payload;
    }

    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        String accountIdentifier = normalizeIdentifier(request.account());
        DemoAccount account = accountsByIdentifier.get(accountIdentifier);
        Instant expiresAt = resetGrants.get(accountIdentifier);

        if (account == null) {
            throw new IllegalArgumentException("未找到对应账号。");
        }
        if (expiresAt == null || Instant.now().isAfter(expiresAt)) {
            resetGrants.remove(accountIdentifier);
            throw new IllegalArgumentException("请先完成身份核验，再重置密码。");
        }

        DemoAccount updatedAccount = account.withPasswordHash(passwordEncoder.encode(request.newPassword()));
        indexAccount(updatedAccount);
        resetGrants.remove(accountIdentifier);
        clearAttempts(updatedAccount);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("account", request.account().trim());
        payload.put("reset", true);
        payload.put("displayName", updatedAccount.name());
        return payload;
    }

    private void seedAccounts() {
        indexAccount(new DemoAccount(2L, "系统管理员", "admin001", "18800009999", "admin@example.com",
            passwordEncoder.encode("admin123"), UserRole.ADMIN));
        indexAccount(new DemoAccount(3L, "审核员小周", "auditor001", "18800001001", "auditor@example.com",
            passwordEncoder.encode("123456"), UserRole.USER));
        indexAccount(new DemoAccount(4L, "林知夏", "2023110421", "18800001111", "lin@example.com",
            passwordEncoder.encode("123456"), UserRole.USER));
        indexAccount(new DemoAccount(5L, "许同学", "2023110555", "18800006666", "xu@example.com",
            passwordEncoder.encode("123456"), UserRole.USER));
    }

    private void ensureIdentifierAvailable(String identifier, String message) {
        if (accountsByIdentifier.containsKey(identifier)) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean requiresCaptcha(String identifier) {
        return failedAttempts.getOrDefault(identifier, 0) >= captchaThreshold;
    }

    private void clearAttempts(DemoAccount account) {
        for (String identifier : account.identifiers()) {
            failedAttempts.remove(identifier);
        }
    }

    private void indexAccount(DemoAccount account) {
        for (String identifier : account.identifiers()) {
            accountsByIdentifier.put(identifier, account);
        }
    }

    private String normalizeIdentifier(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private record DemoAccount(Long userId, String name, String studentNo, String phone, String email,
                               String passwordHash, UserRole role) {

        DemoAccount {
            Objects.requireNonNull(userId, "userId");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(studentNo, "studentNo");
            Objects.requireNonNull(phone, "phone");
            Objects.requireNonNull(email, "email");
            Objects.requireNonNull(passwordHash, "passwordHash");
            Objects.requireNonNull(role, "role");
        }

        Set<String> identifiers() {
            return Set.of(studentNo, phone, email);
        }

        DemoAccount withPasswordHash(String updatedPasswordHash) {
            return new DemoAccount(userId, name, studentNo, phone, email, updatedPasswordHash, role);
        }
    }
}
