package insty.util;

import static insty.constants.JwtConstants.ACCESS_TOKEN_VALIDITY;
import static insty.constants.JwtConstants.REFRESH_TOKEN_VALIDITY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    private final Logger log = LoggerFactory.getLogger(JwtUtils.class);     // common 모듈은 롬복을 의존x

    private final String secretKey;

    public JwtUtils(@Value("${spring.security.jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * AccessToken 생성
     */
    public String generateAccessToken(String subject, String role){
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(subject)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))           // 토큰 발급 시간
                .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_VALIDITY)))        // 토큰 만료시간
                .sign(Algorithm.HMAC512(secretKey.getBytes(StandardCharsets.UTF_8)));   // 명시적으로 UTF-8을 지정하면 어떤 환경에서도 동일한 결과가 보장
    }

    /**
     * RefreshToken 생성
     */
    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        UUID tokenId = UUID.randomUUID();       // 토큰 ID

        return JWT.create()
                .withSubject(subject)
                .withJWTId(String.valueOf(tokenId))
                .withIssuedAt(Date.from(now))           // 토큰 발급 시간
                .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_VALIDITY)))       // 토큰 만료시간
                .sign(Algorithm.HMAC512(secretKey.getBytes(StandardCharsets.UTF_8)));   // 명시적으로 UTF-8을 지정하면 어떤 환경에서도 동일한 결과가 보장
    }

    /**
     * 토큰 유효성 체크
     */
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey.getBytes(StandardCharsets.UTF_8))).build();
            verifier.verify(token);
            return true;
        } catch (TokenExpiredException e) {
            log.warn("토큰 만료: {} >>>> {}", token, e.getMessage());
            return false;
        } catch (JWTVerificationException e) {
            log.error("토큰 검증 실패: {} >>>> {}", token, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("토큰 검증 중 알 수 없는 에러 발생: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 주체 추출
     */
    public String extractSubject(String token){
        return JWT.decode(token)
                .getSubject();
    }

    /**
     * 토큰에서 유효시간 추출
     */
    public long extractExpiredAt(String token){
        return JWT.decode(token)
                .getExpiresAt()
                .getTime();
    }

    /**
     * 토큰에서 권한 및 역할 추출
     */
    public String extractRole(String token){
        return JWT.decode(token)
                .getClaim("role")
                .asString();
    }

}
