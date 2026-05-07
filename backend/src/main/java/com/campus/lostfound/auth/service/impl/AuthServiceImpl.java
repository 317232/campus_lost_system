package com.campus.lostfound.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.auth.service.AuthService;

import java.time.LocalDateTime;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.common.exception.BusinessException;
import com.campus.lostfound.common.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campus.lostfound.auth.dto.AuthDTO;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.domain.entity.UserRole;
import com.campus.lostfound.domain.entity.Role;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.mapper.UserRoleMapper;
import com.campus.lostfound.mapper.RoleMapper;
import com.campus.lostfound.common.utils.EmailUtils;
import com.campus.lostfound.common.utils.VerifyCodeCache;
import com.campus.lostfound.common.utils.TokenBlacklistCache;
import io.jsonwebtoken.Claims;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    // 这里可以实现具体的认证逻辑，例如使用JWT、OAuth2等方式进行用户认证和授权
    // 例如，可以实现一个方法来验证用户的登录信息，并生成相应的令牌

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final JwtUtils jwtUtils;
    private final EmailUtils emailUtils;
    private final VerifyCodeCache verifyCodeCache;
    private final TokenBlacklistCache tokenBlacklistCache;
    private final com.campus.lostfound.auth.service.TurnstileService turnstileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(AuthDTO.RegisterReq req) {
        // 0. 验证邮箱验证码
        String cachedCode = verifyCodeCache.get(req.getEmail());
        if (cachedCode == null || !cachedCode.equals(req.getEmailCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        // 1. 检查学号是否已被注册
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, req.getStudentNo()));
        if (exists) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该学号已被注册");
        }

        // 1.1 检查邮箱是否已被注册
        boolean emailExists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, req.getEmail()));
        if (emailExists) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该邮箱已被注册");
        }

        // 2. 密码加密 (BCrypt)
        String hashedPassword = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());

        // 3. 构建并保存用户实体
        User user = new User();
        user.setStudentNo(req.getStudentNo());
        user.setName(req.getName());
        user.setPasswordHash(hashedPassword);
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setStatus("ACTIVE"); // 默认激活状态

        userMapper.insert(user);
        verifyCodeCache.remove(req.getEmail()); // 注册成功后移除验证码

        // 4. 为新用户分配默认 USER 角色
        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "USER"));
        if (defaultRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRoleMapper.insert(userRole);
            log.info("用户注册成功，学号: {}, 分配默认角色: USER", req.getStudentNo());
        } else {
            log.warn("用户注册成功但未找到默认 USER 角色，学号: {}", req.getStudentNo());
        }
    }

    @Override
    public void sendEmailCode(AuthDTO.SendEmailCodeReq req) {
        // 1. 检查邮箱是否已被注册
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, req.getEmail()));
        if (exists) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该邮箱已被注册");
        }

        // 2. 生成 6 位随机验证码
        String verifyCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 3. 存入缓存，设置 5 分钟过期时间
        verifyCodeCache.put(req.getEmail(), verifyCode, 5 * 60 * 1000);

        // 4. 调用邮件服务发送验证码
        emailUtils.sendVerifyCode(req.getEmail(), verifyCode);
    }

    @Override
    public AuthDTO.LoginResp login(AuthDTO.LoginReq req) {
        // 0. 验证验证码
        if (req.getCaptchaToken() != null && !req.getCaptchaToken().isBlank()) {
            // 本地滑动拼图验证码直接通过
            if (!"local-puzzle-verified".equals(req.getCaptchaToken())) {
                boolean captchaValid = turnstileService.verify(req.getCaptchaToken());
                if (!captchaValid) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "人机验证失败，请重试");
                }
            }
        }

        // 1. 根据账号查询用户（支持学号、邮箱或手机号登录）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, req.getAccount())
                .or().eq(User::getEmail, req.getAccount())
                .or().eq(User::getPhone, req.getAccount()));

        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账号不存在");
        }

        // 2. 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        // 3. 校验密码
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码错误");
        }

        // 4. 查询用户角色
        String roleCode = "USER";
        try {
            var userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
            if (!userRoles.isEmpty()) {
                Role role = roleMapper.selectById(userRoles.get(0).getRoleId());
                if (role != null) {
                    roleCode = role.getRoleCode();
                }
            }
        } catch (Exception e) {
            log.warn("查询用户角色失败，使用默认角色 USER: {}", e.getMessage());
        }

        // 5. 签发 JWT（含角色）
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getStudentNo(), roleCode);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // 5.1 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 6. 封装返回对象
        AuthDTO.LoginResp resp = new AuthDTO.LoginResp();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setUserId(user.getId());
        resp.setStudentNo(user.getStudentNo());
        resp.setName(user.getName());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setRole(roleCode);

        log.info("用户登录成功，学号: {}, 角色: {}", user.getStudentNo(), roleCode);
        return resp;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Claims claims = jwtUtils.parseToken(token);
            long expireTime = claims.getExpiration().getTime();
            tokenBlacklistCache.add(token, expireTime);
            log.info("用户请求登出，Token 已加入黑名单作废");
        } catch (Exception e) {
            log.warn("用户请求登出，但 Token 解析失败或已过期");
        }
    }

    @Override
    public AuthDTO.LoginResp refreshToken(String refreshToken) {
        try {
            // 1. 解析 Refresh Token 获取 userId
            Long userId = jwtUtils.getUserIdFromToken(refreshToken);

            // 2. 查询用户确认存在且状态正常
            User user = userMapper.selectById(userId);
            if (user == null || !"ACTIVE".equals(user.getStatus())) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "用户状态异常，请重新登录");
            }

            // 3. 查询用户角色
            String roleCode = "USER";
            try {
                var userRoles = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
                if (!userRoles.isEmpty()) {
                    Role role = roleMapper.selectById(userRoles.get(0).getRoleId());
                    if (role != null) {
                        roleCode = role.getRoleCode();
                    }
                }
            } catch (Exception e) {
                log.warn("refreshToken 查询用户角色失败: {}", e.getMessage());
            }

            // 4. 重新签发双 Token（含角色）
            String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getStudentNo(), roleCode);
            String newRefreshToken = jwtUtils.generateRefreshToken(user.getId());

            AuthDTO.LoginResp resp = new AuthDTO.LoginResp();
            resp.setAccessToken(newAccessToken);
            resp.setRefreshToken(newRefreshToken);
            resp.setUserId(user.getId());
            resp.setStudentNo(user.getStudentNo());
            resp.setName(user.getName());
            resp.setAvatarUrl(user.getAvatarUrl());
            resp.setRole(roleCode);

            return resp;
        } catch (Exception e) {
            // 解析失败（比如 Token 过期或被篡改）
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌无效或已过期，请重新登录");
        }
    }

    @Override
    public void forgotPassword(AuthDTO.ForgotPasswordReq req) {
        // 1. 验证用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, req.getAccount())
                .or().eq(User::getEmail, req.getAccount()));
                
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该账号不存在");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该账号未绑定有效邮箱，无法重置密码");
        }

        // 2. 生成 6 位随机验证码
        String verifyCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        
        // 3. 将 verifyCode 存入 内存缓存，设置 5 分钟过期时间
        verifyCodeCache.put("reset:" + user.getEmail(), verifyCode, 5 * 60 * 1000);
        
        // 4. 调用邮件服务发送验证码
        emailUtils.sendVerifyCode(user.getEmail(), verifyCode);
    }

    @Override
    public void resetPassword(AuthDTO.ResetPasswordReq req) {
        // 1. 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, req.getAccount())
                .or().eq(User::getEmail, req.getAccount()));

        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该账号未绑定有效邮箱，无法重置密码");
        }

        // 2. 从缓存获取对应的验证码并比对
        String cachedCode = verifyCodeCache.get("reset:" + user.getEmail());
        if (cachedCode == null || !cachedCode.equals(req.getVerifyCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        // 3. 修改密码并更新
        String hashedNewPassword = BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt());
        user.setPasswordHash(hashedNewPassword);
        userMapper.updateById(user);

        // 4. 删除缓存中的验证码
        verifyCodeCache.remove("reset:" + user.getEmail());
        
        log.info("账号 {} 密码重置成功", req.getAccount());
    }
}