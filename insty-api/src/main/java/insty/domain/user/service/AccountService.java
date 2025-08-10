package insty.domain.user.service;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final UserWriter userWriter;
    private final UserReader userReader;

    private final UserValidator userValidator;
    private final UserFileReader userFileReader;

    // 스프링 시큐리티
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailVerificationReader emailVerificationReader;
    /**
     * 이메일 회원가입
     */
    public UserCreateRes signup(UserCreateReq req) {
        /*if (!emailVerificationReader.existsByEmail(req.email())) {
            throw new CustomException(AuthErrorCode.REQUIRES_EMAIL_VERIFICATION_REQUEST);
        }*/
        // 중복 체크
        userValidator.validateDuplicateEmail(req.email());
        userValidator.validateDuplicateNickname(req.nickname());

        // 비밀번호 암호화
        String encodedPassword = bCryptPasswordEncoder.encode(req.password());
        // 유저 저장
        User user = userWriter.save(req.email(), encodedPassword, req.nickname());

        return UserCreateRes.from(user.getId(), user.getEmail(), user.getNickname(), user.getUserType());
    }

    /**
     * 이메일 존재여부 체크
     */
    public void existCheckByEmail(UserEmailCheckReq req) {
        userValidator.validateDuplicateEmail(req.email());
    }

    /**
     * 닉네임 존재여부 체크
     */
    public void existsCheckByNickname(UserNicknameCheckReq req) {
        userValidator.validateDuplicateNickname(req.nickname());
    }

    /**
     *  비밀번호 변경
     */
    public UserDetailRes updatePassword(Long userId, UserPasswordUpdateReq req) {
        User findUser = userReader.getUser(userId);
        userValidator.validateMatchesCurrentPassword(findUser.getPassword(), req.currentPassword(), req.newPassword());
        userValidator.validatePasswordChangeAvailable(findUser.getSocialId());

        String encodedPassword = bCryptPasswordEncoder.encode(req.newPassword());
        User updatedUser = userWriter.changePassword(findUser, encodedPassword);
        String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);

        return UserDetailRes.from(updatedUser, profileImageUrl);
    }

    public void withDraw(Long userId) {
        userReader.existByUserId(userId);
        userWriter.delete(userId);
    }
}
