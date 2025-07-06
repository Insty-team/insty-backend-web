package insty.domain.user.service;

import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.implement.UserFileWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    // 사용자 모듈 서비스
    private final UserWriter userWriter;
    private final UserValidator userValidator;
    private final UserReader userReader;

    // 스프링 시큐리티
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 유저파일
    private final UserFileWriter userFileWriter;
    private final UserFileReader userFileReader;

    /**
     * 이메일 회원가입
     */
    public UserCreateRes signup(UserCreateReq req) {
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
     * 사용자 상세 정보 조회
     */
    public UserDetailRes getDetailUser(Long userId) {
        User findUser = userReader.getUser(userId);
        String profileImageUrl = userFileReader.getProfileImageUrl(findUser);
        return UserDetailRes.from(findUser, profileImageUrl);
    }

    /**
     * 사용자 정보 수정
     */
    public UserDetailRes updateUser(Long userId, UserUpdateReq req, MultipartFile profileImage) {
        // 내껏을 제회한 유효성 체크
        userValidator.validateDuplicateEmailExcludingSelf(userId, req.email());
        userValidator.validateDuplicateNicknameExcludingSelf(userId, req.nickname());

        User updatedUser = userWriter.updateUser(
                userId,
                req.email(),
                req.nickname(),
                req.introduce()
        );
        Optional<String> savedUrl = userFileWriter.saveProfileImageGetUrl(updatedUser, profileImage);
        String profileImageUrl = savedUrl.orElseGet(() -> userFileReader.getProfileImageUrl(updatedUser));

        return UserDetailRes.from(updatedUser, profileImageUrl);
    }

    /**
     * 사용자 타입 변경
     */
    public UserDetailRes updateUserType(Long userId, UserTypeUpdateReq req) {
        User updatedUser = userWriter.updateUserByUserType(userId, req.userType());
        String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);
        return UserDetailRes.from(updatedUser, profileImageUrl);
    }

    /**
     * 사용자 수신 및 약관 동의 여부 변경
     */
    public UserDetailRes updateAgreement(Long userId, UserAgreementUpdateReq req) {
        User updatedUser = userWriter.updateUserByAgreement(userId, req.isEmailAgree());
        String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);
        return UserDetailRes.from(updatedUser, profileImageUrl);
    }

    /**
     *  비밀번호 변경
     */
    public UserDetailRes updatePassword(Long userId, UserPasswordUpdateReq req) {
        User findUser = userReader.getUser(userId);
        userValidator.validateMatchesCurrentPassword(findUser.getPassword(), req.currentPassword(), req.newPassword());

        String encodedPassword = bCryptPasswordEncoder.encode(req.newPassword());

        User updatedUser = userWriter.changePassword(userId, encodedPassword);
        String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);

        return UserDetailRes.from(updatedUser, profileImageUrl);
    }
}
