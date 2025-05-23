package insty.domain.user.implement;

import insty.domain.user.repository.UserRepository;
import insty.model.user.User;
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
}
