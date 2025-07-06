package insty.domain.user.implement;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserWriter {

    private final UserRepository userRepository;

    /**
     * 사용자 생성
     */
    public User save(String email, String password, String nickname) {
        User newUser = User.create(email, password, nickname);
        return userRepository.save(newUser);
    }

    /**
     * 사용자 정보 수정
     */
    public User updateUser(Long userId, String email, String nickname, String introduce) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.update(email, nickname, introduce);
        return userRepository.save(user);
    }

    /**
     *  사용자 비밀번호 변경
     */
    public User changePassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.changePassword(password);
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 수정 (유저타입)
     */
    public User updateUserByUserType(Long userId, UserType userType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.update(userType);
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 수정 (약관 동의 및 수신 동의)
     */
    public User updateUserByAgreement(Long userId, boolean isEmailAgreed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.update(isEmailAgreed);
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 수정 (마지막 로그인 시각)
     */
    public void updateLastLoginAt(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.updateLastLoginAt();
        userRepository.save(user);
    }
}
