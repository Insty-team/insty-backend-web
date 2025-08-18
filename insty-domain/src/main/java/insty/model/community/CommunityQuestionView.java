package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.community.id.CommunityQuestionViewId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Entity
@Table(name = "community_question_views", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityQuestionView extends BaseEntity {

    @EmbeddedId
    private CommunityQuestionViewId communityQuestionViewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("communityQuestion")
    @JoinColumn(name = "question_id", nullable = false)
    private CommunityQuestion communityQuestion;

    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt;

    public static CommunityQuestionView create(CommunityQuestion communityQuestion, Long userId) {
        validateCreate(communityQuestion, userId);
        return CommunityQuestionView.builder()
                .communityQuestionViewId(CommunityQuestionViewId.create(communityQuestion.getId(), userId))
                .communityQuestion(communityQuestion)
                .lastViewedAt(Instant.now())
                .build();
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = Instant.now();
    }

    private static void validateCreate(CommunityQuestion communityQuestion, Long userId) {
        if (communityQuestion == null) {
            log.error("생성 오류 - communityQuestion : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (userId == null) {
            log.error("생성 오류 - userId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
