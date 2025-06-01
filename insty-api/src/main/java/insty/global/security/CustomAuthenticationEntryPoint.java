package insty.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.error.CommonErrorCode;
import insty.error.ErrorCode;
import insty.global.response.ErrorInfo;
import insty.global.response.FailRes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException {

        log.warn("인증 실패: {} -> {}", request.getRequestURI(), e.getMessage());

        // 에러
        ErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;

        // 응답 상태값 작성
        response.setStatus(errorCode.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        // 응답 객체 생성
        FailRes<CommonErrorCode> failRes = FailRes.of(ErrorInfo.of(errorCode));

        // 응답
        response.getWriter().write(objectMapper.writeValueAsString(failRes));
    }
}

