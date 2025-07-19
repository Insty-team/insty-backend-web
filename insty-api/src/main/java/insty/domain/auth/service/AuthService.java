package insty.domain.auth.service;

import insty.domain.auth.implement.AuthTokenRedisWriter;
import insty.domain.auth.implement.AuthTokenValidator;
import insty.domain.auth.strategy.SocialStrategy;
import insty.domain.user.dto.UserAuthTokenDto;
import insty.domain.user.dto.request.UserLoginReq;
import insty.domain.auth.dto.response.AuthUserRes;
import insty.domain.auth.implement.AuthTokenIssuer;
import insty.domain.user.dto.request.UserSocialLoginReq;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserWriter;
import insty.error.SocialErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.global.security.CustomUserDetails;
import insty.model.user.SocialType;
import insty.model.user.User;
import insty.util.JwtUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    // 사용자 모듈 서비스
    private final UserWriter userWriter;
    private final UserReader userReader;

    // 인증 및 토큰 모듈 서비스
    private final AuthTokenIssuer authTokenIssuer;
    private final AuthTokenValidator authTokenValidator;
    private final AuthTokenRedisWriter authTokenRedisWriter;

    // 스프링 시큐리티
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // 전략 패턴
    private final List<SocialStrategy> strategies;


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
        // 마지막 로그인 시간 변경 및 유저타입 변경
        userWriter.updateLastLoginAt(user.getUser());
        userWriter.changeUserType(user.getUser(), req.userType());

        // 토큰 발급
        UserAuthTokenDto token = authTokenIssuer.generateUserTokens(user.getUserId(), user.getUserType());
        authTokenRedisWriter.saveRefreshToken(user.getUserId(), token.refreshToken());  // redis에 저장

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
        authTokenValidator.validateRefreshToken(refreshToken);  // token 자체 검증

        Long userId = Long.parseLong(jwtUtils.extractSubject(refreshToken));    // userId 추출
        User user = userReader.getUser(userId);

        log.info("회원ID : {}  토큰 재발급", userId);

        // 토큰 발급
        UserAuthTokenDto token = authTokenIssuer.generateUserTokens(user.getId(), user.getUserType());
        authTokenRedisWriter.saveRefreshToken(user.getId(), token.refreshToken());  // redis에 저장

        // 응답 객체 생성
        return AuthUserRes.create(
                user.getId(),
                user.getNickname(),
                user.getUserType(),
                token
        );
    }

    /**
     * 로그아웃
     */
    public void logout(Long userId) {
        authTokenRedisWriter.deleteRefreshToken(userId);
    }

    /**
     *  소셜 로그인
     */
    public AuthUserRes loginBySocial(SocialType socialName, UserSocialLoginReq req) {
        // 전략 가져오기
        SocialStrategy socialLoginStrategy = strategies.stream()
                .filter(s -> s.supports(socialName))        // supports() 로 판별
                .findFirst()
                .orElseThrow(() -> new CustomException(SocialErrorCode.SOCIAL_UNSUPPORTED_TYPE));

        log.info("{} 로그인 전략 동작", socialName);

        // 각 전략에 맞춰 유저 정보 조회
        User findUser = socialLoginStrategy.loginBySocial(req.code(), req.userType());

        // 마지막 로그인 시간 변경 및 유저타입 변경
        userWriter.updateLastLoginAt(findUser);
        userWriter.changeUserType(findUser, req.userType());

        // 토큰 발급
        UserAuthTokenDto token = authTokenIssuer.generateUserTokens(findUser.getId(), findUser.getUserType());
        authTokenRedisWriter.saveRefreshToken(findUser.getId(), token.refreshToken());  // redis에 저장

        // 응답 객체 생성
        return AuthUserRes.create(
                findUser.getId(),
                findUser.getNickname(),
                findUser.getUserType(),
                token
        );
    }

    /**
     *  인가코드 받기
     */
    public String getAuthUrl(SocialType socialName, String state) {

        // 전략 가져오기
        SocialStrategy socialLoginStrategy = strategies.stream()
                .filter(s -> s.supports(socialName))        // supports() 로 판별
                .findFirst()
                .orElseThrow(() -> new CustomException(SocialErrorCode.SOCIAL_UNSUPPORTED_TYPE));

        // 각 전략에 맞춰 유저 정보 조회
        return socialLoginStrategy.getAuthUrl(state);
    }
}
