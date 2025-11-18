package insty.model.user;

import insty.model.BaseEntity;
import insty.model.file.File;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "web_service", name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;       // 이메일

    @Column(nullable = false)
    private String password;        // 비밀번호

    @Column(nullable = false, length = 30)
    private String nickname;        // 사용자 닉네임

    @Column(length = 4000)
    private String introduce;       // 사용자 소개

    @Column(nullable = false)
    private boolean isEmailAgreed;      // 사용자 이메일 수신 동의 여부

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private UserType userType;      // 사용자 타입

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profileimage_id")
    private File profileImage;

    @Column(nullable = false)
    private boolean isDeleted;      // 탈퇴 여부

    private Instant deletedAt;      // 탈퇴 시각

    private Instant lastLoginAt;     // 마지막 로그인 시각

    @Column(length = 150)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private SocialType socialType;

    // 이메일 회원가입 엔티티 객체 생성
    public static User create(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .introduce("")
                .isEmailAgreed(false)
                .userType(UserType.NONE)
                .isDeleted(false)
                .build();
    }

    // 소셜 로그인 회원가입 엔티티 객체 생성
    public static User createBySocial(String socialId, SocialType socialType, String email, String nickname, UserType userType) {
        return User.builder()
                .socialId(String.valueOf(socialId))
                .socialType(socialType)
                .email(email)
                .password("")
                .nickname(nickname)
                .introduce("")
                .isEmailAgreed(false)
                .userType(userType)
                .isDeleted(false)
                .build();
    }

    // 사용자 정보 수정 객체 생성
    public void update(String email,String nickname, String introduce) {
        this.email = email;
        this.nickname = nickname;
        this.introduce = introduce;
    }

    // 비밀번호 변경
    public void changePassword(String password) {
        this.password = password;
    }

    // 사용자 전환
    public void update(UserType userType) {
        this.userType = userType;
    }

    public boolean isSocialUser() {
        return this.socialId != null;
    }

    public void update(boolean isEmailAgreed) {
        this.isEmailAgreed = isEmailAgreed;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = Instant.now();
    }

    public void updateProfileImage(File profileImage) {
        this.profileImage = profileImage;
    }
}
