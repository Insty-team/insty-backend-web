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

    /**
     * 사용자 이메일 중복 체크 (자신 것은 제외)
     */
    public void validateDuplicateEmailExcludingSelf(Long userId, String email) {
        userRepository.findByEmail(email).ifPresent((user) -> {
            if (!user.getId().equals(userId)) {
                throw new CustomException(UserErrorCode.USER_DUPLICATE_EMAIL);
            }
        });
    }

    /**
     * 사용자 닉네임 중복 체크 (자신 것은 제외)
     */
    public void validateDuplicateNicknameExcludingSelf(Long userId, String nickname) {
        userRepository.findByNickname(nickname).ifPresent((user) -> {
            if (!user.getId().equals(userId)) {
                throw new CustomException(UserErrorCode.USER_DUPLICATE_NICKNAME);
            }
        });
    }
}
