package insty.domain.auth.strategy;

import insty.domain.user.repository.UserRepository;
import insty.generator.NicknameGenerator;
import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;
import insty.social.kakao.adapter.GoogleService;
import insty.social.kakao.dto.GoogleTokenRes;
import insty.social.kakao.dto.GoogleUserInfoRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleStrategy implements SocialStrategy {

    private final GoogleService googleService;
    private final UserRepository userRepository;

    /**
     *  전략 사용 지원 여부 체크
     */
    @Override
    public boolean supports(SocialType provider) {
        return provider == SocialType.GOOGLE;
    }

    /**
     *  인증 URL 조회
     */
    @Override
    public String getAuthUrl(String state) {
        log.info("구글 로그인 : 인가코드 받는 URL 요청");
        return googleService.getAuthUrl(state);
    }

    /**
     *  소셜로그인 진행 및 토큰 발급
     */
    @Override
    public User loginBySocial(String code, UserType userType) {
        log.info("임시로 code 값 조회 {}", code);
        // 구글 토큰 조회
        GoogleTokenRes token = googleService.getTokenFromGoogle(code);
        log.info("구글 로그인 : 토큰 조회 완료 {}", token);

        // 사용자 정보 조회
        GoogleUserInfoRes userProfile = googleService.getUserProfile(token.accessToken());
        String socialId = userProfile.id();       // 소셜 회원 ID
        String email = userProfile.email();      // 이메일
        String nickname = NicknameGenerator.generateNickname();      // 닉네임

        log.info("구글 로그인 : 사용자 정보 조회 완료 , 소셜 ID : {}", socialId);

        return userRepository.findBySocialIdAndSocialType(String.valueOf(socialId), SocialType.GOOGLE)
                .orElseGet(() -> {                      // 존재 X → 회원가입
                    User newUser = User.createBySocial(socialId, SocialType.GOOGLE, email, nickname, userType);
                    return userRepository.save(newUser);
                });
    }
}
