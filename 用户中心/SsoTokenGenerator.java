import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.RedisTemplate;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * SSO服务器访问令牌生成工具类
 * 包含JWT（自包含令牌）和Redis存储（可吊销）两种方案
 */
public class SsoTokenGenerator {

    // ==================== JWT方案 ====================
    // JWT密钥（生产环境需使用更长的密钥，建议256位以上）
    private static final String JWT_SECRET = "sso-server-secret-key-1234567890abcdefghijklmnopqrstuvwxyz";
    // JWT过期时间（2小时）
    private static final long JWT_EXPIRATION = 2 * 60 * 60 * 1000;

    /**
     * 生成JWT访问令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param extraClaims 额外信息（如角色、客户端ID）
     * @return JWT令牌
     */
    public static String generateJwtToken(String userId, String username, Map<String, Object> extraClaims) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

        return Jwts.builder()
                .setId(UUID.randomUUID().toString()) // 令牌唯一ID
                .setSubject(userId) // 用户ID
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) // 过期时间
                .setIssuer("sso-server") // 签发者
                .claim("username", username) // 附加用户名
                .addClaims(extraClaims) // 附加其他信息
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    /**
     * 验证JWT令牌并解析信息
     * @param token JWT令牌
     * @return 令牌中的声明信息
     * @throws JwtException 令牌无效时抛出
     */
    public static Claims verifyJwtToken(String token) throws JwtException {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // ==================== Redis方案 ====================
    private final RedisTemplate<String, String> redisTemplate;
    // Redis令牌过期时间（2小时）
    private static final long REDIS_TOKEN_EXPIRE = 2;

    public SsoTokenGenerator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成Redis存储的令牌
     * @param userId 用户ID
     * @return 随机令牌字符串
     */
    public String generateRedisToken(String userId) {
        // 生成随机令牌（UUID去除横线）
        String token = "sso_" + UUID.randomUUID().toString().replace("-", "");

        // 存储令牌-用户ID映射（键：令牌，值：用户ID）
        String tokenKey = "sso:token:" + token;
        redisTemplate.opsForValue().set(tokenKey, userId, REDIS_TOKEN_EXPIRE, TimeUnit.HOURS);

        // 存储用户-令牌映射（用于登出时批量删除）
        String userTokenKey = "sso:user:tokens:" + userId;
        redisTemplate.opsForSet().add(userTokenKey, token);
        redisTemplate.expire(userTokenKey, REDIS_TOKEN_EXPIRE, TimeUnit.HOURS);

        return token;
    }

    /**
     * 验证Redis令牌
     * @param token 令牌
     * @return 对应的用户ID（null表示令牌无效）
     */
    public String verifyRedisToken(String token) {
        String tokenKey = "sso:token:" + token;
        return redisTemplate.opsForValue().get(tokenKey);
    }

    /**
     * 单点登出（删除用户所有令牌）
     * @param userId 用户ID
     */
    public void logout(String userId) {
        String userTokenKey = "sso:user:tokens:" + userId;
        // 获取用户所有令牌并删除
        redisTemplate.opsForSet().members(userTokenKey).forEach(token -> {
            redisTemplate.delete("sso:token:" + token);
        });
        // 删除用户-令牌映射
        redisTemplate.delete(userTokenKey);
    }


    // ==================== 示例用法 ====================
    public static void main(String[] args) {
        // 1. JWT方案示例
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "user");
        claims.put("clientId", "web-app");
        String jwtToken = generateJwtToken("1001", "张三", claims);
        System.out.println("JWT令牌: " + jwtToken);

        // 验证JWT
        try {
            Claims parsedClaims = verifyJwtToken(jwtToken);
            System.out.println("JWT解析用户ID: " + parsedClaims.getSubject());
            System.out.println("JWT解析用户名: " + parsedClaims.get("username"));
            System.out.println("JWT过期时间: " + parsedClaims.getExpiration());
        } catch (JwtException e) {
            System.out.println("JWT验证失败: " + e.getMessage());
        }

        // 2. Redis方案示例（需注入RedisTemplate实例）
        // RedisTemplate<String, String> redisTemplate = ...; // 实际项目中通过Spring注入
        // SsoTokenGenerator redisGenerator = new SsoTokenGenerator(redisTemplate);
        // String redisToken = redisGenerator.generateRedisToken("1001");
        // System.out.println("Redis令牌: " + redisToken);
        // String userId = redisGenerator.verifyRedisToken(redisToken);
        // System.out.println("Redis令牌验证用户ID: " + userId);
    }
}
    