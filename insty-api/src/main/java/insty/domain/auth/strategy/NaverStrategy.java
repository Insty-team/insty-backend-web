package insty.domain.auth.strategy;

import insty.domain.user.repository.UserRepository;
import insty.generator.NicknameGenerator;
import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;
import insty.social.kakao.adapter.NaverService;
import insty.social.kakao.dto.NaverTokenRes;
import insty.social.kakao.dto.NaverUserInfoRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverStrategy implements SocialStrategy {
    private final NaverService naverService;
    private final UserRepository userRepository;

    /**
     *  전략 사용 지원 여부
     */
    @Override
    public boolean supports(SocialType provider) {
        return provider == SocialType.NAVER;
    }

    /**
     *  인증 URL 조회
     */
    @Override
    public String getAuthUrl(String state) {
        log.info("네이버 로그인 : 인가코드 받는 URL 요청");
        return naverService.getAuthUrl(state);
    }

    /**
     *  소셜로그인 진행 및 토큰 발급
     */
    @Override
    public User loginBySocial(String code, UserType userType) {
        // 네이버 토큰 조회
        NaverTokenRes token = naverService.getTokenFromNaver(code);
        log.info("네이버 로그인 : 토큰 조회 완료");

        // 사용자 정보 조회
        NaverUserInfoRes userProfile = naverService.getUserProfile(token.accessToken());

        String socialId = userProfile.naverAccount().id();       // 소셜 회원 ID
        String email = userProfile.naverAccount().email();      // 이메일
        String nickname = NicknameGenerator.generateNickname();

        log.info("네이버 로그인 : 사용자 정보 조회 완료 , 소셜 ID : {}", socialId);

        return userRepository.findBySocialIdAndSocialType(socialId, SocialType.NAVER)
                .orElseGet(() -> {                      // 존재 X → 회원가입
                    User newUser = User.createBySocial(socialId, SocialType.NAVER, email, nickname, userType);
                    return userRepository.save(newUser);
                });
    }
}
