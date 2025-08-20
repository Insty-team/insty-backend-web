package insty.model.mention;

import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.community.CommunityAnswer;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_answer_id", nullable = false)
    private CommunityAnswer communityAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioner_user_id", nullable = false)
    private User mentionerUser;

    /**
     * 멘션 생성
     */
    public static Mention create(CommunityAnswer communityAnswer, User mentionedUser, User mentionerUser) {
        validateCreate(communityAnswer, mentionedUser, mentionerUser);
        return Mention.builder()
                .communityAnswer(communityAnswer)
                .mentionedUser(mentionedUser)
                .mentionerUser(mentionerUser)
                .build();
    }

    /**
     * 멘션 생성 (userId 기반)
     */
    public static Mention create(CommunityAnswer communityAnswer, Long mentionedUserId, Long mentionerUserId) {
        if (mentionedUserId == null) {
            log.error("멘션 생성 오류 - mentionedUserId : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        if (mentionerUserId == null) {
            log.error("멘션 생성 오류 - mentionerUserId : null");
            throw new CustomException(MentionErrorCode.MENTION_CREATE_ERROR);
        }
        if (mentionedUserId.equals(mentionerUserId)) {
            log.error("멘션 생성 오류 - 자기 자신을 멘션할 수 없음");
            throw new CustomException(MentionErrorCode.MENTION_SELF_ERROR);
        }
        
        return Mention.builder()
                .communityAnswer(communityAnswer)
                .mentionedUser(User.builder().id(mentionedUserId).build())
                .mentionerUser(User.builder().id(mentionerUserId).build())
                .build();
    }

    /**
     * CommunityAnswer 설정
     */
    public void setCommunityAnswer(CommunityAnswer communityAnswer) {
        this.communityAnswer = communityAnswer;
    }

    private static void validateCreate(CommunityAnswer communityAnswer, User mentionedUser, User mentionerUser) {
        if (communityAnswer == null) {
            log.error("멘션 생성 오류 - communityAnswer : null");
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
        if (mentionedUser.getId().equals(mentionerUser.getId())) {
            log.error("멘션 생성 오류 - 자기 자신을 멘션할 수 없음");
            throw new CustomException(MentionErrorCode.MENTION_SELF_ERROR);
        }
    }
}
