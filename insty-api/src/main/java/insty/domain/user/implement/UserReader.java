package insty.domain.user.implement;

import insty.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {

    private final UserRepository userRepository;

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
}

