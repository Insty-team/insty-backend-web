package insty.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.domain.user.dto.response.UserLoginRes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoginFailHandler implements AuthenticationFailureHandler {

    /**
     * 로그인 실패하면 실행하는 핸들러
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 응답 메시지 작성
        String jsonResponse = new ObjectMapper().writeValueAsString(new UserLoginRes(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(jsonResponse);
    }
}
