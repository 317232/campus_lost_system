
package com.campus.lostfound.auth.service;

import com.campus.lostfound.auth.dto.AuthDTO;

public interface AuthService {
    /**
     * 处理用户注册逻辑
     * 需对密码进行 Hash 加密，并检查学号是否已存在
     *
     * @param req 注册请求参数
     */
    void register(AuthDTO.RegisterReq req);

    /**
     * 发送邮箱验证码
     *
     * @param req 发送验证码请求
     */
    void sendEmailCode(AuthDTO.SendEmailCodeReq req);

    /**
     * 处理用户登录逻辑
     * 需校验账号密码，成功后生成并返回 JWT (Access Token & Refresh Token)
     *
     * @param req 登录请求参数
     * @return 包含 Token 和用户基础信息的响应对象
     */
    AuthDTO.LoginResp login(AuthDTO.LoginReq req);

    /**
     * 处理用户登出逻辑
     * （当前为无状态 JWT，实际项目中可配合 Redis 将 Token 加入黑名单）
     *
     * @param token 用户的 Access Token
     */
    void logout(String token);

    /**
     * 刷新访问令牌
     * 验证 Refresh Token 是否有效，有效则签发全新的一对 Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的 Token 响应对象
     */
    AuthDTO.LoginResp refreshToken(String refreshToken);

    /**
     * 忘记密码：发送验证码逻辑
     * 校验账号存在后，生成验证码（通常存入 Redis）并调用邮件/短信服务发送
     *
     * @param req 忘记密码请求参数
     */
    void forgotPassword(AuthDTO.ForgotPasswordReq req);

    /**
     * 重置密码
     * 校验前端传来的验证码，通过后对新密码进行 Hash 加密并更新数据库
     *
     * @param req 重置密码请求参数
     */
    void resetPassword(AuthDTO.ResetPasswordReq req);
}