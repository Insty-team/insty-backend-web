package insty.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.domain.user.dto.request.UserLoginReq;
import insty.error.CommonErrorCode;
import insty.global.security.exception.CustomAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@RequiredArgsConstructor
public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;


    /**
     * 시큐리티 이메일 로그인 시도
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // JSON 타입으로 받아야함
        if (request.getContentType() == null || !request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            throw new CustomAuthenticationException(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        try {
            log.debug("=========== Security Login 로그인 시도 =========== ");
            // 데이터 바인딩 (getInputStream은 1번 밖에 못 읽음)
            UserLoginReq loginRequest = objectMapper.readValue(request.getInputStream(), UserLoginReq.class);

            // 아직 인증되기전의 인증 객체 생성
            UsernamePasswordAuthenticationToken unauthenticated = new UsernamePasswordAuthenticationToken(loginRequest.email() , loginRequest.password());

            // token에 인증되지 않은 정보 검증 위해 AuthenticationManager로 전달
            return super.getAuthenticationManager().authenticate(unauthenticated);

        } catch (IOException e) {
            log.error("로그인 요청 중 파라미터 바인딩 실패 : ", e);
            throw new CustomAuthenticationException(CommonErrorCode.PARAMETER_VALIDATION_ERROR);
        }
    }

    /**
     * security 인증 성공 시 실행하는 메서드 (굳이 구현x) : LoginSucessHandler로 진행
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("=========== Security Login 인증 성공 =========== ");
        super.successfulAuthentication(request, response, chain, authResult);
    }

    /**
     * security 인증 실패 시 실행하는 메서드 (굳이 구현x) : LoginFailHandler로 진행
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.debug("=========== Security Login 인증 실패 =========== ");
        super.unsuccessfulAuthentication(request, response, failed);
    }
}
