package insty.model.user;

import insty.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "shared", name = "users")
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

    // TODO 프로필 이미지 생성
//    @Column(nullable = false)
//    private Files profileImage

    @Column(nullable = false)
    private boolean isDeleted;      // 탈퇴 여부

    private Instant deletedAt;      // 탈퇴 시각

    private Instant lastLoginAt;     // 마지막 로그인 시각

    // TODO 소셜로그인

    // 회원가입 엔티티 객체 생성
    public static User create(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .isEmailAgreed(false) // 기본 false 설정
                .userType(UserType.LEARNER)  // 기본 사용자 타입 지정
                .isDeleted(false)      // 기본 false
                .build();
    }
}
