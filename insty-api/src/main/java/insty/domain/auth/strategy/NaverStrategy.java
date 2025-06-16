package insty.domain.auth.strategy;

import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;
import insty.social.kakao.adapter.NaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverStrategy implements SocialStrategy {

    private final NaverService naverService;

    @Override
    public boolean supports(SocialType provider) {
        return provider == SocialType.NAVER;
    }

    @Override
    public String getAuthUrl() {
        return naverService.getAuthUrl();
    }

    @Override
    public User loginBySocial(String code, String state, UserType userType) {
        // 네이버 토큰 조회
        return null;
    }
}
