package com.example.usercenter.service;

import java.util.Map;

/**
 * 单点登录服务接口
 */
public interface SSOService {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含访问令牌和刷新令牌
     */
    Map<String, String> login(String username, String password);

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);

    /**
     * 用户登出
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 验证令牌
     * @param token 访问令牌
     * @return 令牌是否有效
     */
    boolean validateToken(String token);

    /**
     * 从令牌中获取用户ID
     * @param token 访问令牌
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);
}