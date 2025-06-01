package insty.domain.user.service;

import insty.domain.user.dto.CurrentUserDto;
import insty.domain.user.dto.UserAuthTokenDto;
import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserLoginReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.dto.response.UserLoginRes;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserTokenIssuer;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.global.security.CustomUserDetails;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final UserTokenIssuer userTokenIssuer;

    // 스프링 시큐리티
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * 이메일 회원가입
     */
    public UserCreateRes signup(UserCreateReq req) {
        // 유효성 체크
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
    public UserDuplicateCheckRes existCheckByEmail(UserEmailCheckReq req) {
        boolean emailExists = userReader.existCheckByEmail(req.email());
        boolean isAvailable = !emailExists; // 존재하지 않으면 사용가능
        String reason = isAvailable ? "사용 가능한 이메일입니다." : UserErrorCode.USER_DUPLICATE_EMAIL.getMessage();
        return UserDuplicateCheckRes.from(isAvailable, reason);
    }

    /**
     * 닉네임 존재여부 체크
     */
    public UserDuplicateCheckRes existsCheckByNickname(UserNicknameCheckReq req) {
        boolean nicknameExists = userReader.existCheckByNickname(req.nickname());
        boolean isAvailable = !nicknameExists; // 존재하지 않으면 사용가능
        String reason = isAvailable ? "사용 가능한 닉네임입니다." : UserErrorCode.USER_DUPLICATE_NICKNAME.getMessage();
        return UserDuplicateCheckRes.from(isAvailable, reason);
    }

    /**
     * 이메일 로그인 스프링 시큐리티
     */
    public UserLoginRes loginByEmail(UserLoginReq req) {
        // 인증 전 객체 생성
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(req.email(), req.password());
        // 인증 시도
        Authentication authenticated = authenticationManager.authenticate(authenticationRequest);

        if(!authenticated.isAuthenticated()) throw new CustomException(UserErrorCode.UNAUTHORIZED);

        // 인증된 객체
        CustomUserDetails user = (CustomUserDetails) authenticated.getPrincipal();
        // 마지막 로그인 시간 변경
        userWriter.updateLastLoginAt(user.getUserId());

        // 토큰 발급
        UserAuthTokenDto token = userTokenIssuer.generateUserTokens(user);

        // 응답 객체 생성
        return UserLoginRes.create(
                user.getUserId(),
                user.getNickname(),
                user.getUserType(),
                token
        );
    }

    /**
     * 사용자 상세 정보 조회
     */
    public UserDetailRes getDetailUser(CurrentUserDto currentUser) {
        User findUser = userReader.getUser(currentUser.id());
        return UserDetailRes.from(findUser);
    }

    /**
     * 사용자 정보 수정
     */
    public UserDetailRes updateUser(Long userId, UserUpdateReq req, MultipartFile profileImage) {
        String encodedPassword = bCryptPasswordEncoder.encode(req.password());
        User updatedUser = userWriter.updateUser(
                userId,
                req.email(),
                encodedPassword,
                req.nickname(),
                req.introduce(),
                profileImage
        );
        return UserDetailRes.from(updatedUser);
    }

    /**
     * 사용자 타입 변경
     */
    public UserDetailRes updateUserType(Long userId, UserTypeUpdateReq req) {
        User updatedUser = userWriter.updateUserByUserType(userId, req.userType());
        return UserDetailRes.from(updatedUser);
    }

    /**
     * 사용자 수신 및 약관 동의 여부 변경
     */
    public UserDetailRes updateAgreement(Long userId, UserAgreementUpdateReq req) {
        User updatedUser = userWriter.updateUserByAgreement(userId, req.isEmailAgree());
        return UserDetailRes.from(updatedUser);
    }
}
