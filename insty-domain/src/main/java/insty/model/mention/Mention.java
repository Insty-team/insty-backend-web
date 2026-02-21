package insty.model.mention;

import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "mentions", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Mention extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private MentionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentioner_user_id", nullable = false)
    private User mentionerUser;

    /**
     * 멘션 생성
     */
    public static Mention create(MentionTargetType targetType, Long targetId, User mentionedUser, User mentionerUser) {
        validateCreate(targetType, targetId, mentionedUser, mentionerUser);
        return Mention.builder()
                .targetType(targetType)
                .targetId(targetId)
                .mentionedUser(mentionedUser)
                .mentionerUser(mentionerUser)
                .build();
    }

    private static void validateCreate(MentionTargetType targetType, Long targetId, User mentionedUser, User mentionerUser) {
        if (targetType == null) {
            log.error("멘션 생성 오류 - targetType : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        if (targetId == null) {
            log.error("멘션 생성 오류 - targetId : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        if (mentionedUser == null) {
            log.error("멘션 생성 오류 - mentionedUser : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        if (mentionerUser == null) {
            log.error("멘션 생성 오류 - mentionerUser : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        Long mentionedId = mentionedUser.getId();
        Long mentionerId = mentionerUser.getId();
        if (mentionedId == null || mentionerId == null) {
            log.error("멘션 생성 오류 - user id : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        if (java.util.Objects.equals(mentionedId, mentionerId)) {
            log.error("멘션 생성 오류 - 자기 자신을 멘션할 수 없음");
            throw new CustomException(MentionErrorCode.MENTION_SELF_ERROR);
        }
    }
}
