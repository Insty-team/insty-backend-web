package insty.domain.user.implement;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public User updateUser(Long userId, String email, String password, String nickname, String introduce, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.update(email, password, nickname, introduce);
        return userRepository.save(user);
    }
}
