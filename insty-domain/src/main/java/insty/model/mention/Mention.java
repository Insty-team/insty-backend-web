package insty.model.mention;

import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.courseqna.CourseAnswer;
import insty.model.user.User;
import jakarta.persistence.Entity;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_answer_id", nullable = false)
    private CourseAnswer courseAnswer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentioner_user_id", nullable = false)
    private User mentionerUser;

    /**
     * 멘션 생성
     */
    public static Mention create(CourseAnswer courseAnswer, User mentionedUser, User mentionerUser) {
        validateCreate(courseAnswer, mentionedUser, mentionerUser);
        return Mention.builder()
                .courseAnswer(courseAnswer)
                .mentionedUser(mentionedUser)
                .mentionerUser(mentionerUser)
                .build();
    }

    private static void validateCreate(CourseAnswer courseAnswer, User mentionedUser, User mentionerUser) {
        if (courseAnswer == null) {
            log.error("멘션 생성 오류 - courseAnswer : null");
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
