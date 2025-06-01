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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException {

        log.warn("인가 실패: {} -> {}", request.getRequestURI(), e.getMessage());

        // 에러 조회
        ErrorCode errorCode = CommonErrorCode.FORBIDDEN;

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

