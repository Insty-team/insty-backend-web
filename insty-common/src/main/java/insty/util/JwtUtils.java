package insty.util;

import static insty.constants.JwtConstants.ACCESS_TOKEN_VALIDITY;
import static insty.constants.JwtConstants.REFRESH_TOKEN_VALIDITY;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import insty.constants.JwtValidationType;
import insty.constants.TokenType;
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
    public String generateAccessToken(String subject, String userType){
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(subject)
                .withClaim("userType", userType)
                .withClaim("tokenType", TokenType.ACCESS.name())
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
                .withClaim("tokenType", TokenType.REFRESH.name())
                .withIssuedAt(Date.from(now))           // 토큰 발급 시간
                .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_VALIDITY)))       // 토큰 만료시간
                .sign(Algorithm.HMAC512(secretKey.getBytes(StandardCharsets.UTF_8)));   // 명시적으로 UTF-8을 지정하면 어떤 환경에서도 동일한 결과가 보장
    }

    /**
     * JWT 유효성 검사 및 상태 값 조회
     */
    public JwtValidationType validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey.getBytes(StandardCharsets.UTF_8))).build();
            verifier.verify(token);
            // 정상
            return JwtValidationType.VALID;
        } catch (TokenExpiredException e) {
            // 토큰 만료
            return JwtValidationType.EXPIRED;
        } catch (SignatureVerificationException e) {
            log.error("서명 검증 실패 (토큰 변조 의심): {} >>>> {}", token, e.getMessage());
            return JwtValidationType.INVALID_SIGNATURE;
        } catch (AlgorithmMismatchException e) {
            log.error("지원하지 않는 알고리즘 또는 타입: {} >>>> {}", token, e.getMessage());
            return JwtValidationType.UNSUPPORTED;
        } catch (JWTDecodeException e) {
            log.error("토큰 형식이 올바르지 않음: {} >>>> {}", token, e.getMessage());
            return JwtValidationType.MALFORMED;
        } catch (JWTVerificationException e) {
            log.error("토큰 내부 클레임 검증 실패: {} >>>> {}", token, e.getMessage());
            return JwtValidationType.CLAIMS_INVALID;
        } catch (Exception e) {
            log.error("토큰 검증 중 알 수 없는 에러 발생: {}", e.getMessage());
            return JwtValidationType.UNKNOWN_ERROR;
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
    public Instant extractExpiredAt(String token){
        return JWT.decode(token)
                .getExpiresAt()
                .toInstant(); // 정확하게 Instant 타입으로 변환
    }

    /**
     * 토큰에서 권한 추출
     */
    public String extractUserType(String token){
        return JWT.decode(token)
                .getClaim("userType")
                .asString();
    }

    /**
     * JWT 토큰에서 JWT ID (jti) 추출
     */
    public UUID extractTokenId(String refreshToken) {
        String tokenId = JWT.decode(refreshToken).getId();
        return UUID.fromString(tokenId);
    }

    /**
     *  동적으로 claimKey로 JWT 토큰에 있는 값 추출
     */
    public String extractClaim(String token, String claimKey) {
        return JWT.decode(token)
                .getClaim(claimKey)
                .asString();
    }

}
