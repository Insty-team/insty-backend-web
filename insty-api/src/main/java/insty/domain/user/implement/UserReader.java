package insty.domain.user.implement;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {

    private final UserRepository userRepository;

    /**
     * 사용자 정보 조회
     */
    public User getUser(Long userId) {
        log.debug("유저 번호로 사용자 조회 >> {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일 존재 여부 체크
     */
    public boolean existCheckByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 존재 여부 체크
     */
    public boolean existCheckByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public void existByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
    }
}

