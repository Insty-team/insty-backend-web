package insty.domain.auth.strategy;

import insty.domain.user.implement.UserValidator;
import insty.domain.user.repository.UserRepository;
import insty.generator.NicknameGenerator;
import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;
import insty.social.kakao.adapter.KakaoService;
import insty.social.kakao.dto.KakaoTokenRes;
import insty.social.kakao.dto.KakaoUserInfoRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoStrategy implements SocialStrategy {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    /**
     *  전략 사용 지원 여부
     */
    @Override
    public boolean supports(SocialType provider) {
        return provider == SocialType.KAKAO;
    }

    /**
     *  인증 URL 조회
     */
    @Override
    public String getAuthUrl(String state) {
        log.info("카카오 로그인 : 인가코드 받는 URL 요청");
        return kakaoService.getAuthUrl(state);
    }

    /**
     *  소셜로그인 진행 및 토큰 발급
     */
    @Override
    @Transactional
    public User loginBySocial(String code, UserType userType) {
        // 카카오 토큰 조회
        KakaoTokenRes token = kakaoService.getTokenFromKakao(code);
        log.info("카카오 로그인 : 토큰 조회 완료");

        // 사용자 정보 조회
        KakaoUserInfoRes userProfile = kakaoService.getUserProfile(token.accessToken());

        Long socialId = userProfile.id();       // 소셜 회원 ID
        String email = userProfile.kakaoAccount().email();      // 이메일
        String nickname = NicknameGenerator.generateNickname();

        userValidator.validateDuplicateEmail(email);

        log.info("카카오 로그인 : 사용자 정보 조회 완료 , 소셜 ID : {}", socialId);

        return userRepository.findBySocialIdAndSocialType(String.valueOf(socialId), SocialType.KAKAO)
                .orElseGet(() -> {                      // 존재 X → 회원가입
                    User newUser = User.createBySocial(String.valueOf(socialId), SocialType.KAKAO, email, nickname, userType);
                    return userRepository.save(newUser);
                });
    }
}