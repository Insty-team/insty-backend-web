package insty.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.constants.JwtValidationType;
import insty.constants.TokenType;
import insty.error.CommonErrorCode;
import insty.error.TokenErrorCode;
import insty.global.security.CustomAuthenticationEntryPoint;
import insty.global.security.exception.CustomAuthenticationException;
import insty.model.user.UserType;
import insty.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 사용자 토큰 조회
        final String token = extractAccessToken(request);

        // 토큰이 없으면 넘기기
        if(token.isEmpty()){
            log.debug("Token이 Header에 존재하지 않습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 검증
            JwtValidationType tokenValidStatus = jwtUtils.validateToken(token);
            validateTokenStatus(tokenValidStatus);  // 상태값에 따라 예외 던지기

            // 토큰에 있는 정보로 스프링 시큐리티 컨텍스트에 유저정보 저장
            setSecurityContextAuthentication(token);

            // 다음 필터 실행
            filterChain.doFilter(request, response);
        } catch (CustomAuthenticationException ex) {

            // 예외 발생 시 즉시 AuthenticationEntryPoint 호출
            SecurityContextHolder.clearContext(); // 컨텍스트 정리

            AuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint(objectMapper);
            entryPoint.commence(request, response, ex);  // 직접 호출
        }



    }

    /**
     * 토큰 상태에 따라 예외 발생
     */
    private void validateTokenStatus(JwtValidationType tokenValidStatus) {
        switch (tokenValidStatus) {
            case VALID -> {} // 정상
            case EXPIRED -> throw new CustomAuthenticationException(TokenErrorCode.ACCESS_TOKEN_EXPIRED); // 만료
            case CLAIMS_INVALID -> throw new CustomAuthenticationException(TokenErrorCode.TOKEN_CLAIMS_INVALID); // 내부 클레임 검증 실패
            case INVALID_SIGNATURE -> throw new CustomAuthenticationException(TokenErrorCode.ACCESS_TOKEN_SIGNATURE_INVALID); // 서명 검증 실패
            case MALFORMED -> throw new CustomAuthenticationException(TokenErrorCode.TOKEN_MALFORMED); // 토큰 형식이 올바르지 않음
            case UNSUPPORTED -> throw new CustomAuthenticationException(TokenErrorCode.TOKEN_UNSUPPORTED); // 지원하지 않음
            default -> throw new CustomAuthenticationException(CommonErrorCode.INTERNAL_ERROR); // 알 수 없는 오류
        }
    }

    /**
     * 인증 토큰 추출
     */
    private String extractAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 이후의 토큰만 추출
        }

        return "";
    }

    /**
     * 정상적으로 토큰에 있는 정보로 스프링 시큐리티 컨텍스트에 유저정보 저장
     */
    private void setSecurityContextAuthentication(String token) {
        // 토큰에서 정보 획득
        Long userId = Long.parseLong(jwtUtils.extractSubject(token));
        TokenType tokenType = extractTokenType(token);

        JwtAuthenticationToken authToken = createAuthenticationToken(tokenType, token, userId);

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     *  스프링 시큐리티 사용자 권한 넣기 위한 메서드
     */
    private GrantedAuthority toGrantedAuthority(UserType userType) {
        return new SimpleGrantedAuthority("ROLE_" + userType.name());
    }

    /**
     *  토큰 타입 추출
     */
    private TokenType extractTokenType(String token) {
        return TokenType.valueOf(jwtUtils.extractClaim(token, "tokenType"));
    }

    /**
     *  인증 객체 생성
     */
    private JwtAuthenticationToken createAuthenticationToken(TokenType tokenType, String token, Long userId) {
        if (tokenType == TokenType.ACCESS) {
            UserType userType = resolveUserTypeClaim(token);
            List<GrantedAuthority> authorities = userType == null ? List.of() : List.of(toGrantedAuthority(userType));
            return new JwtAuthenticationToken(userId, null, authorities);
        }

        return new JwtAuthenticationToken(userId, null, List.of()); // refreshToken은 권한 없음
    }

    private UserType resolveUserTypeClaim(String token) {
        try {
            String userTypeClaim = jwtUtils.extractUserType(token);
            if (userTypeClaim == null || userTypeClaim.isBlank()) {
                return UserType.NONE;
            }
            return UserType.valueOf(userTypeClaim);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid userType claim in token, defaulting to NONE");
            return UserType.NONE;
        }
    }
}
