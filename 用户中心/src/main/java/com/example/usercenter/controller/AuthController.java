package com.example.usercenter.controller;

import com.example.usercenter.service.SSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理登录、登出和刷新令牌等认证相关请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SSOService ssoService;

    /**
     * 用户登录
     * @param loginRequest 登录请求参数
     * @return 登录结果，包含访问令牌和刷新令牌
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
        return ssoService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }

    /**
     * 刷新访问令牌
     * @param refreshTokenRequest 刷新令牌请求参数
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String newAccessToken = ssoService.refreshToken(refreshTokenRequest.getRefreshToken());
        Map<String, String> response = new java.util.HashMap<>();
        response.put("access_token", newAccessToken);
        return response;
    }

    /**
     * 用户登出
     * @param token 访问令牌（从请求头中获取）
     */
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        ssoService.logout(token);
    }

    /**
     * 验证令牌
     * @param token 访问令牌（从请求头中获取）
     * @return 令牌是否有效
     */
    @GetMapping("/validate")
    public boolean validateToken(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ssoService.validateToken(token);
    }

    // 登录请求参数类
    public static class LoginRequest {
        private String username;
        private String password;

        // getter和setter方法
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // 刷新令牌请求参数类
    public static class RefreshTokenRequest {
        private String refreshToken;

        // getter和setter方法
        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}