package insty.domain.auth.service;

import insty.domain.auth.implement.AuthTokenValidator;
import insty.domain.user.dto.UserAuthTokenDto;
import insty.domain.user.dto.request.UserLoginReq;
import insty.domain.user.dto.response.AuthUserRes;
import insty.domain.auth.implement.AuthTokenIssuer;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserWriter;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.global.security.CustomUserDetails;
import insty.model.user.User;
import insty.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    // 사용자 모듈 서비스
    private final UserWriter userWriter;
    private final UserReader userReader;

    // 인증 및 토큰 모듈 서비스
    private final AuthTokenIssuer userTokenIssuer;
    private final AuthTokenValidator authTokenValidator;

    // 스프링 시큐리티
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * 이메일 로그인 스프링 시큐리티
     */
    public AuthUserRes loginByEmail(UserLoginReq req) {
        // 인증 전 객체 생성
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(req.email(), req.password());
        // 인증 시도
        Authentication authenticated = authenticationManager.authenticate(authenticationRequest);

        if(!authenticated.isAuthenticated()) throw new CustomException(UserErrorCode.UNAUTHORIZED);

        // 인증된 객체
        CustomUserDetails user = (CustomUserDetails) authenticated.getPrincipal();
        // 마지막 로그인 시간 변경
        userWriter.updateLastLoginAt(user.getUserId());

        // TODO Redis에 RefreshToken 넣고 관리

        // 토큰 발급
        UserAuthTokenDto token = userTokenIssuer.generateUserTokens(user.getUserId());

        // 응답 객체 생성
        return AuthUserRes.create(
                user.getUserId(),
                user.getNickname(),
                user.getUserType(),
                token
        );
    }

    /**
     * RefreshToken으로 토큰 재발급
     */
    public AuthUserRes reissueByRefreshToken(String refreshToken) {
        authTokenValidator.validateToken(refreshToken);

        Long userId = Long.parseLong(jwtUtils.extractSubject(refreshToken));
        User user = userReader.getUser(userId);

        // TODO Redis에 있는 값과 비교하여 일치하면 재발급

        // 토큰 발급
        UserAuthTokenDto token = userTokenIssuer.generateUserTokens(user.getId());

        // 응답 객체 생성
        return AuthUserRes.create(
                user.getId(),
                user.getNickname(),
                user.getUserType(),
                token
        );
    }
}
