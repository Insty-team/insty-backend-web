package insty.global.security;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     *  사용자 정보 조회
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // TODO 이메일 인증 받으면 소셜로그인, 이메일 계정 통합계정으로 수정
        User user = userRepository.findByEmailAndSocialIdIsNull(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return new CustomUserDetails(user);
    }
}