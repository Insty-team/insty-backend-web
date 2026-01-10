package insty.domain.user.controller;

import insty.domain.user.controller.docs.UserControllerDocs;
import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.service.UserService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @GetMapping("/profile")
    public SuccessRes<UserDetailRes> getProfile(@CurrentUser Long userId) {
        return SuccessRes.of(userService.getDetailUser(userId));
    }

    @PutMapping(value = "/profile/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<UserDetailRes> updateProfile(
            @CurrentUser Long userId,
            @RequestPart("userUpdateReq") @Valid UserUpdateReq req,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return SuccessRes.of(userService.updateUser(userId, req, profileImage));
    }

    @PatchMapping("/profile/userType")
    public SuccessRes<UserDetailRes> updateUserType(
            @CurrentUser Long userId,
            @Valid @RequestBody UserTypeUpdateReq req
    ) {
        return SuccessRes.of(userService.updateUserType(userId, req));
    }

    @PatchMapping("/profile/email-agree")
    public SuccessRes<UserDetailRes> updateEmailAgreed(
            @CurrentUser Long userId,
            @RequestBody UserAgreementUpdateReq req
    ) {
        return SuccessRes.of(userService.updateAgreement(userId, req));
    }
}
