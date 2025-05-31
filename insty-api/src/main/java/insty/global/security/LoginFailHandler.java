package insty.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.error.CommonErrorCode;
import insty.error.ErrorCode;
import insty.global.response.ErrorInfo;
import insty.global.response.FailRes;
import insty.global.security.exception.CustomAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    /**
     * 로그인 실패하면 실행하는 핸들러
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException {

        log.debug("=========== Security Login 로그인 실패 =========== ");
        // 에러 조회
        ErrorCode errorCode;
        String errorMessage = "";

        if (e instanceof CustomAuthenticationException customException) {
            errorCode = customException.getErrorCode();
            errorMessage = customException.getMessage();
        } else {
            // CustomAuthenticationException이 아닐 경우 기본 에러코드 지정
            errorCode = CommonErrorCode.UNAUTHORIZED;
            errorMessage = e.getMessage() != null ? e.getMessage() : errorCode.getMessage();
        }

        // 응답 상태값 작성
        response.setStatus(errorCode.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        // 응답 객체 생성
        FailRes<CommonErrorCode> failRes = FailRes.of(ErrorInfo.of(errorCode, errorMessage));

        // 응답
        response.getWriter().write(objectMapper.writeValueAsString(failRes));
    }

}
