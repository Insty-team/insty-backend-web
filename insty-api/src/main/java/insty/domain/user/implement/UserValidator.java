package insty.domain.user.implement;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.repository.UserRepository;
import insty.error.AuthErrorCode;
import insty.error.SocialErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailVerificationReader emailVerificationReader;

    /**
     * 사용자 이메일 중복 체크 return Exception
     */
    public void validateDuplicateEmail(String email) {
        if (existsEmail(email)) {
            throw new CustomException(UserErrorCode.USER_DUPLICATE_EMAIL);
        }
    }

    /**
     * 사용자 이메일 존재 여부 체크
     */
    public boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자 닉네임 중복 체크 return Exception
     */
    public void validateDuplicateNickname(String nickname) {
        if(existsNickname(nickname)) {
            throw new CustomException(UserErrorCode.USER_DUPLICATE_NICKNAME);
        };
    }

    /**
     * 닉네임 중복 존재 여부 체크
     */
    public boolean existsNickname(String nickname){
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 사용자 이메일 중복 체크 (자신 것은 제외)
     */
    public void validateDuplicateEmailExcludingSelf(Long userId, String email) {
        userRepository.findByEmailAndSocialIdIsNull(email).ifPresent((user) -> {
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

    /**
     *  비밀번호 상태에 따른 유효성
     */
    public void validateMatchesCurrentPassword(String userPassword, String currentPassword, String newPassword) {
        if (!bCryptPasswordEncoder.matches(currentPassword, userPassword)) {
            throw new CustomException(UserErrorCode.USER_CURRENT_PASSWORD_NOT_MATCHED);
        }

        if (bCryptPasswordEncoder.matches(newPassword, userPassword)) {
            throw new CustomException(UserErrorCode.USER_NEW_PASSWORD_SAME_AS_CURRENT);
        }
    }

    /**
     * 비밀번호 변경할 수 있는 사용자인가 유효성
     */
    public void validatePasswordChangeAvailable(String socialId) {
        if(socialId != null) throw new CustomException(SocialErrorCode.NOT_CHANGE_PASSWORD);
    }

    /**
     *  본인확인
     */
    public void validateIdentityByPassword(String userPassword, String currentPassword) {
        if (!bCryptPasswordEncoder.matches(currentPassword, userPassword)) {
            throw new CustomException(UserErrorCode.USER_CURRENT_PASSWORD_NOT_MATCHED);
        }
    }

    /**
     * 소셜로그인 회원은 일부 데이터 변경 거절
     */
    public void validateRestrictedUpdatesForSocialUser(User findUser, UserUpdateReq req) {
        if(!findUser.getEmail().equals(req.email())) {
            throw new CustomException(SocialErrorCode.NOT_CHANGE_EMAIL);
        }
    }

    public void validateEmailVerification(String email) {
        if (!emailVerificationReader.checkEmailVerified(email)) {
            throw new CustomException(AuthErrorCode.REQUIRES_EMAIL_VERIFICATION_REQUEST);
        }
    }
}
