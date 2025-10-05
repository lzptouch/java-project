package com.example.usercenter.service.impl;

import com.example.usercenter.entity.SysUser;
import com.example.usercenter.repository.SysUserRepository;
import com.example.usercenter.service.SSOService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 单点登录服务实现类
 */
@Service
public class SSOServiceImpl implements SSOService {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public Map<String, String> login(String username, String password) {
        // 验证用户
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成访问令牌
        String accessToken = generateAccessToken(user.getId(), user.getUsername());

        // 生成刷新令牌
        String refreshToken = generateRefreshToken(user.getId());

        // 存储刷新令牌到Redis
        String refreshTokenKey = "refresh_token:" + refreshToken;
        redisTemplate.opsForValue().set(refreshTokenKey, user.getId(), refreshExpiration, TimeUnit.MILLISECONDS);

        // 存储用户的所有令牌（用于强制下线）
        String userTokensKey = "user_tokens:" + user.getId();
        redisTemplate.opsForSet().add(userTokensKey, accessToken);
        redisTemplate.expire(userTokensKey, jwtExpiration, TimeUnit.MILLISECONDS);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        tokens.put("token_type", "Bearer");
        tokens.put("expires_in", String.valueOf(jwtExpiration / 1000));

        return tokens;
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 验证刷新令牌
        String refreshTokenKey = "refresh_token:" + refreshToken;
        Long userId = (Long) redisTemplate.opsForValue().get(refreshTokenKey);

        if (userId == null) {
            throw new RuntimeException("无效的刷新令牌");
        }

        // 获取用户信息
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 生成新的访问令牌
        String newAccessToken = generateAccessToken(user.getId(), user.getUsername());

        // 更新Redis中的令牌信息
        String userTokensKey = "user_tokens:" + user.getId();
        redisTemplate.opsForSet().add(userTokensKey, newAccessToken);
        redisTemplate.expire(userTokensKey, jwtExpiration, TimeUnit.MILLISECONDS);

        return newAccessToken;
    }

    @Override
    public void logout(String token) {
        try {
            // 从令牌中获取用户ID
            Long userId = getUserIdFromToken(token);

            // 从Redis中移除令牌
            String userTokensKey = "user_tokens:" + userId;
            redisTemplate.opsForSet().remove(userTokensKey, token);

            // 将令牌加入黑名单
            String blacklistKey = "token_blacklist:" + token;
            redisTemplate.opsForValue().set(blacklistKey, "1", jwtExpiration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 如果令牌无效，忽略错误
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // 检查令牌是否在黑名单中
            String blacklistKey = "token_blacklist:" + token;
            if (redisTemplate.hasKey(blacklistKey)) {
                return false;
            }

            // 验证令牌
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 生成访问令牌
     */
    private String generateAccessToken(Long userId, String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .setIssuer("sso-server")
                .claim("username", username)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}