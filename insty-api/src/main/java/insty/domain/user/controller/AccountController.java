package insty.domain.user.controller;

import insty.domain.user.controller.docs.AccountControllerDocs;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.response.DuplicateCheckRes;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.service.AccountService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import insty.trackevent.TrackEvent;
import insty.trackevent.model.MixpanelEventType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AccountController implements AccountControllerDocs {

    private final AccountService accountService;

    @PostMapping
    @TrackEvent(
            successEventType = MixpanelEventType.AUTH_SIGNED_UP,
            failureEventType = MixpanelEventType.AUTH_SIGNUP_FAILED,
            distinctIdStrategy = TrackEvent.DistinctIdStrategy.ANONYMOUS
    )
    public SuccessRes<UserCreateRes> signup(@Valid @RequestBody UserCreateReq req) {
        return SuccessRes.of(accountService.signup(req));
    }

    @GetMapping("/email/check")
    public SuccessRes<DuplicateCheckRes> emailCheck(@Valid @ModelAttribute UserEmailCheckReq req) {
        return SuccessRes.of(accountService.existCheckByEmail(req));
    }

    @GetMapping("/nickname/check")
    public SuccessRes<DuplicateCheckRes> nicknameCheck(@Valid @ModelAttribute UserNicknameCheckReq req) {
        return SuccessRes.of(accountService.existCheckByNickname(req));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/profile/password")
    public SuccessRes<UserDetailRes> updatePassword(
        @CurrentUser Long userId,
        @RequestBody @Validated UserPasswordUpdateReq req
    ) {
        return SuccessRes.of(accountService.updatePassword(userId, req));
    }

    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public SuccessRes<Void> delete(@CurrentUser Long userId) {
        accountService.withdraw(userId);
        return SuccessRes.of();
    }
}
