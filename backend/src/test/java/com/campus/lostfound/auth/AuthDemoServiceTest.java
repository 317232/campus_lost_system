package com.campus.lostfound.auth;

import com.campus.lostfound.auth.dto.AuthDTO.LoginReq;
import com.campus.lostfound.auth.dto.AuthDTO.LoginResp;
import com.campus.lostfound.auth.dto.AuthDTO.RegisterReq;
import com.campus.lostfound.auth.dto.AuthDTO.ResetPasswordReq;
import com.campus.lostfound.auth.dto.AuthDTO.ForgotPasswordReq;
import com.campus.lostfound.auth.service.impl.AuthServiceImpl;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.common.exception.BusinessException;
import com.campus.lostfound.common.utils.VerifyCodeCache;
import com.campus.lostfound.common.utils.TokenBlacklistCache;
import com.campus.lostfound.domain.entity.Role;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.RoleMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.mapper.UserRoleMapper;
import com.campus.lostfound.common.utils.JwtUtils;
import com.campus.lostfound.common.utils.EmailUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthDemoServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailUtils emailUtils;

    @Mock
    private VerifyCodeCache verifyCodeCache;

    @Mock
    private TokenBlacklistCache tokenBlacklistCache;

    @Mock
    private com.campus.lostfound.auth.service.TurnstileService turnstileService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setStudentNo("2023110421");
        testUser.setName("测试用户");
        testUser.setPasswordHash(BCrypt.hashpw("password123", BCrypt.gensalt()));
        testUser.setEmail("test@example.com");
        testUser.setPhone("18800001111");
        testUser.setStatus("ACTIVE");
    }

    @Test
    void register_Success() {
        RegisterReq req = new RegisterReq();
        req.setStudentNo("2023110422");
        req.setName("新用户");
        req.setPassword("password123");
        req.setEmail("new@example.com");
        req.setEmailCode("123456");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleCode("USER");

        when(verifyCodeCache.get("new@example.com")).thenReturn("123456");
        when(userMapper.exists(any())).thenReturn(false);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(roleMapper.selectOne(any())).thenReturn(userRole);

        assertDoesNotThrow(() -> authService.register(req));

        verify(userMapper, times(2)).exists(any());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_StudentNoAlreadyExists_ThrowsException() {
        RegisterReq req = new RegisterReq();
        req.setStudentNo("2023110421");
        req.setName("新用户");
        req.setPassword("password123");
        req.setEmail("new@example.com");
        req.setEmailCode("123456");

        when(verifyCodeCache.get("new@example.com")).thenReturn("123456");
        when(userMapper.exists(any())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.register(req));

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertTrue(exception.getMessage().contains("该学号已被注册"));
    }

    @Test
    void login_Success() {
        LoginReq req = new LoginReq();
        req.setAccount("2023110421");
        req.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginResp resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("access-token", resp.getAccessToken());
        assertEquals("refresh-token", resp.getRefreshToken());
        assertEquals(1L, resp.getUserId());
        assertEquals("测试用户", resp.getName());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginReq req = new LoginReq();
        req.setAccount("nonexistent");
        req.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(req));

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertTrue(exception.getMessage().contains("账号不存在"));
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        LoginReq req = new LoginReq();
        req.setAccount("2023110421");
        req.setPassword("wrongpassword");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(req));

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertTrue(exception.getMessage().contains("密码错误"));
    }

    @Test
    void login_UserDisabled_ThrowsException() {
        testUser.setStatus("DISABLED");

        LoginReq req = new LoginReq();
        req.setAccount("2023110421");
        req.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(req));

        assertEquals(ResultCode.FORBIDDEN, exception.getResultCode());
        assertTrue(exception.getMessage().contains("账号已被禁用"));
    }

    @Test
    void forgotPassword_UserExists_SendsVerifyCode() {
        ForgotPasswordReq req = new ForgotPasswordReq();
        req.setAccount("test@example.com");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        doNothing().when(emailUtils).sendVerifyCode(any(), any());

        assertDoesNotThrow(() -> authService.forgotPassword(req));

        verify(emailUtils).sendVerifyCode(eq("test@example.com"), any());
    }

    @Test
    void forgotPassword_UserNotExists_ThrowsException() {
        ForgotPasswordReq req = new ForgotPasswordReq();
        req.setAccount("nonexistent@example.com");

        when(userMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.forgotPassword(req));

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertTrue(exception.getMessage().contains("该账号不存在"));
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordReq req = new ResetPasswordReq();
        req.setAccount("2023110421");
        req.setVerifyCode("123456");
        req.setNewPassword("newpassword123");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(verifyCodeCache.get("reset:test@example.com")).thenReturn("123456");
        when(userMapper.updateById(testUser)).thenReturn(1);

        assertDoesNotThrow(() -> authService.resetPassword(req));

        verify(userMapper).updateById(testUser);
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        ResetPasswordReq req = new ResetPasswordReq();
        req.setAccount("nonexistent");
        req.setVerifyCode("123456");
        req.setNewPassword("newpassword123");

        when(userMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.resetPassword(req));

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void refreshToken_Success() {
        String refreshToken = "valid-refresh-token";

        when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("new-refresh-token");

        LoginResp resp = authService.refreshToken(refreshToken);

        assertNotNull(resp);
        assertEquals("new-access-token", resp.getAccessToken());
        assertEquals("new-refresh-token", resp.getRefreshToken());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        String invalidToken = "invalid-token";

        when(jwtUtils.getUserIdFromToken(invalidToken))
            .thenThrow(new BusinessException(ResultCode.UNAUTHORIZED, "令牌无效"));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.refreshToken(invalidToken));

        assertEquals(ResultCode.UNAUTHORIZED, exception.getResultCode());
    }

    @Test
    void logout_Success() {
        String token = "some-token";

        assertDoesNotThrow(() -> authService.logout(token));
    }
}