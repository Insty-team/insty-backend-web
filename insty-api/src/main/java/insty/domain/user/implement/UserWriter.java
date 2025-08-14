package insty.domain.user.implement;

import insty.domain.course.implement.CourseCleaner;
import insty.domain.user.repository.UserRepository;
import insty.model.user.User;
import insty.model.user.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserWriter {

    private final CourseCleaner courseCleaner;
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
    public User updateUser(User user, String email, String nickname, String introduce) {
        user.update(email, nickname, introduce);
        return userRepository.save(user);
    }

    /**
     *  사용자 비밀번호 변경
     */
    public User changePassword(User user, String password) {
        user.changePassword(password);
        return userRepository.save(user);
    }

    /**
     *  사용자 닉네임 변경
     */
    public User changeNickname(User user, String nickname, String introduce) {
        user.update(user.getEmail(), nickname, introduce);
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 수정 (유저타입)
     */
    public User changeUserType(User user, UserType userType) {
        user.update(userType);
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 수정 (약관 동의 및 수신 동의)
     */
    public User changeEmailAgreementStatus(User user, boolean isEmailAgreed) {
        user.update(isEmailAgreed);
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 수정 (마지막 로그인 시각)
     */
    public void updateLastLoginAt(User user) {
        user.updateLastLoginAt();
        userRepository.save(user);
    }

    public void withdraw(Long userId) {
        userRepository.deleteById(userId);
        courseCleaner.cleanAllData(userId);
    }
}
