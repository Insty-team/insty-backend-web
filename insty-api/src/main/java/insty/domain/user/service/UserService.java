package insty.domain.user.service;

import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.implement.UserFileWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
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
    private final UserReader userReader;

    private final UserValidator userValidator;

    // 유저파일
    private final UserFileWriter userFileWriter;
    private final UserFileReader userFileReader;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;



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
        User findUser = userReader.getUser(userId);
        if(findUser.isSocialUser()) {
            userValidator.validateDuplicateNicknameExcludingSelf(userId, req.nickname());
            userValidator.validateRestrictedUpdatesForSocialUser(findUser, req);

            User updatedUser = userWriter.changeNickname(findUser, req.nickname(), req.introduce());
            String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);

            return UserDetailRes.from(updatedUser, profileImageUrl);
        } else {
            userValidator.validateIdentityByPassword(findUser.getPassword(), req.currentPassword());
            userValidator.validateDuplicateEmailExcludingSelf(userId, req.email());
            userValidator.validateDuplicateNicknameExcludingSelf(userId, req.nickname());

            // TODO 회원 정보 수정 페이지 분리 되면 삭제 예정
            if(req.currentPassword() != null && req.newPassword() != null) {
                userValidator.validatePasswordChangeAvailable(findUser.getSocialId());
                String encodedPassword = bCryptPasswordEncoder.encode(req.newPassword());
                userValidator.validateMatchesCurrentPassword(findUser.getPassword(), req.currentPassword(), req.newPassword());
                userWriter.changePassword(findUser, encodedPassword);
            }


            User updatedUser = userWriter.updateUser(
                    findUser,
                    req.email(),
                    req.nickname(),
                    req.introduce()
            );
            Optional<String> savedUrl = userFileWriter.saveProfileImageGetUrl(updatedUser, profileImage);
            String profileImageUrl = savedUrl.orElseGet(() -> userFileReader.getProfileImageUrl(updatedUser));

            return UserDetailRes.from(updatedUser, profileImageUrl);
        }
    }

    /**
     * 사용자 타입 변경
     */
    public UserDetailRes updateUserType(Long userId, UserTypeUpdateReq req) {
        User findUser = userReader.getUser(userId);
        User updatedUser = userWriter.changeUserType(findUser, req.userType());
        String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);
        return UserDetailRes.from(updatedUser, profileImageUrl);
    }

    /**
     * 사용자 수신 및 약관 동의 여부 변경
     */
    public UserDetailRes updateAgreement(Long userId, UserAgreementUpdateReq req) {
        User findUser = userReader.getUser(userId);
        User updatedUser = userWriter.changeEmailAgreementStatus(findUser, req.isEmailAgree());
        String profileImageUrl = userFileReader.getProfileImageUrl(updatedUser);
        return UserDetailRes.from(updatedUser, profileImageUrl);
    }
}
