package insty.domain.auth.strategy;

import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;

public interface SocialStrategy {
    boolean supports(SocialType provider);
    String getAuthUrl();
    User loginBySocial(String code, String state, UserType userType);
}
