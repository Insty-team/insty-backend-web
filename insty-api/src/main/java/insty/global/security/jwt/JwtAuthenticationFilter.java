package insty.global.security.jwt;

import insty.constants.JwtValidationType;
import insty.domain.user.implement.UserReader;
import insty.error.CommonErrorCode;
import insty.exception.CustomException;
import insty.global.security.CustomUserDetails;
import insty.model.user.User;
import insty.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserReader userReader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 사용자 토큰 조회
        final String accessToken = extractAccessToken(request);

        // 토큰이 없으면 넘기기
        if(accessToken.isEmpty()){
            log.debug("token이 header에 존재하지 않습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 검증
        JwtValidationType jwtValidationType = jwtUtils.validateToken(accessToken);
        switch (jwtValidationType) {        // TODO 필터 예외처리
            case VALID:
                log.info("정상 토큰입니다.");
                break;
            case EXPIRED:
                log.info("토큰이 만료되었습니다.");
                throw new AuthenticationServiceException(CommonErrorCode.UNAUTHORIZED.getMessage(), new CustomException(CommonErrorCode.UNAUTHORIZED)); // 401
            case INVALID_SIGNATURE:
            case MALFORMED:
            case UNSUPPORTED:
                log.warn("토큰이 위조되었거나 형식이 잘못되었습니다.");
//                throw new AuthenticationServiceException(CommonErrorCode.FORBIDDEN.getMessage(), new CustomException(CommonErrorCode.FORBIDDEN)); // 403
                throw new CustomException(CommonErrorCode.FORBIDDEN);
            case CLAIMS_INVALID:
                log.warn("토큰 클레임이 유효하지 않습니다.");
                throw new AuthenticationServiceException(CommonErrorCode.UNAUTHORIZED.getMessage(), new CustomException(CommonErrorCode.UNAUTHORIZED)); // 401
            default:
                log.error("토큰 검증 중 알 수 없는 오류가 발생했습니다.");
                throw new AuthenticationServiceException(CommonErrorCode.INTERNAL_ERROR.getMessage(), new CustomException(CommonErrorCode.INTERNAL_ERROR)); // 401
        }

        // 토큰에 있는 정보로 스프링 시큐리티 컨텍스트에 유저정보 저장
        setSecurityContextAuthentication(accessToken);

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }

    /**
     * 사용자 정보 조회
     */
    private User getUser(Long userId) {
        return userReader.getUser(userId);
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

        User user = getUser(userId);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}