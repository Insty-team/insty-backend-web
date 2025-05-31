package insty.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.domain.user.dto.response.LoginSuccessRes;
import insty.global.response.SuccessRes;
import insty.util.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtHelper;
    private final ObjectMapper objectMapper;

    /**
     * 로그인 성공하면 실행하는 핸들러
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        // 유저ID
        Long userId = user.getUserId();

        // 토큰 생성
        String accessToken = jwtHelper.generateAccessToken(String.valueOf(userId), user.getUserType().name());
        String refreshToken = jwtHelper.generateRefreshToken(String.valueOf(userId));

        // 만료 시간 추출
        long accessTokenExpiresAt = jwtHelper.extractExpiredAt(accessToken);
        long refreshTokenExpiresAt = jwtHelper.extractExpiredAt(refreshToken);

        // 응답 상태값 작성
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpServletResponse.SC_OK);

        // 응답 객체 생성
        LoginSuccessRes loginSuccessRes = LoginSuccessRes.create(
                user.getUserId(),
                user.getUsername(),
                user.getUserType(),
                accessToken,
                refreshToken,
                accessTokenExpiresAt,
                refreshTokenExpiresAt
        );
        SuccessRes<LoginSuccessRes> successResponse = SuccessRes.of(loginSuccessRes);

        // 응답
        response.getWriter().write(objectMapper.writeValueAsString(successResponse));
    }
}
