package insty.domain.auth.strategy;

import insty.social.kakao.dto.KakaoTokenRes;
import insty.social.kakao.dto.KakaoUserInfoRes;
import insty.social.kakao.adapter.KakaoService;
import insty.domain.user.repository.UserRepository;
import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;
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
    public String getAuthUrl() {
        return kakaoService.getAuthUrl();
    }

    /**
     *  로그인 구현
     */
    @Override
    @Transactional
    public User loginBySocial(String code, String state, UserType userType) {
        // 카카오 토큰 조회
        KakaoTokenRes token = kakaoService.getTokenFromKakao(code);

        // 사용자 정보 조회
        KakaoUserInfoRes userProfile = kakaoService.getUserProfile(token.accessToken());

        Long socialId = userProfile.id();       // 소셜 회원 ID
        String email = userProfile.kakaoAccount().email();      // 이메일
        String nickname = userProfile.kakaoAccount().profile().nickname();      // 닉네임

        return userRepository.findBySocialIdAndSocialType(String.valueOf(socialId), SocialType.KAKAO)
                .orElseGet(() -> {                      // 존재 X → 회원가입
                    User newUser = User.createByKakao(String.valueOf(socialId), SocialType.KAKAO, email, nickname, userType);
                    return userRepository.save(newUser);
                });
    }
}