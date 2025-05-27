package insty.domain.user.implement;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    /**
     * 사용자 이메일 중복 체크
     */
    public void validateDuplicateEmail(String email) {
        userRepository.findByEmail(email).ifPresent((user) -> {
            throw new CustomException(UserErrorCode.USER_DUPLICATE_EMAIL);
        });
    }

    /**
     * 사용자 닉네임 중복 체크
     */
    public void validateDuplicateNickname(String nickname) {
        userRepository.findByNickname(nickname).ifPresent((user) -> {
            throw new CustomException(UserErrorCode.USER_DUPLICATE_NICKNAME);
        });
    }
}
