package insty.global.security.jwt;

import insty.domain.user.implement.UserReader;
import insty.global.security.CustomUserDetails;
import insty.model.user.User;
import insty.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserReader userReader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = extractAccessToken(request);

        // 토큰이 없으면 넘겨~
        if(accessToken.isEmpty()){
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 검증
        if(!jwtUtils.validateToken(accessToken)) {
            // TODO 없거나 유효하지 않으면 401, 403 예외
            // Front 와 협의하여 어떻게 내려줄지 해야한다.
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("유효하지 않은 토큰입니다.");

            return;
        }

        // 토큰에서 정보 획득
        Long userId = Long.parseLong(jwtUtils.extractSubject(accessToken));

        User user = getUser(userId);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authToken);

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
}