package insty.domain.user.service;

import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.implement.UserWriter;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserWriter userWriter;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 이메일 회원가입 Facade 패턴
     */
    public UserCreateRes signup(UserCreateReq req) {
        // 비밀번호 암호화
        String encodedPassword = bCryptPasswordEncoder.encode(req.password());
        // 유저 저장
        User user = userWriter.save(req.email(), encodedPassword, req.nickname());

        return UserCreateRes.from(user.getId(), user.getEmail(), user.getNickname(), user.getUserType());
    }
}
