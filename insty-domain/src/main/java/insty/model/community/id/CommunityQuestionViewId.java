package insty.model.community.id;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityQuestionViewId implements Serializable {

    private Long communityQuestion;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityQuestionViewId)) return false;

        CommunityQuestionViewId that = (CommunityQuestionViewId) o;
        return Objects.equals(communityQuestion, that.communityQuestion) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(communityQuestion, userId);
    }

    public static CommunityQuestionViewId create(Long questionId, Long userId) {
        validateCreate(questionId, userId);
        return CommunityQuestionViewId.builder()
                .communityQuestion(questionId)
                .userId(userId)
                .build();
    }

    private static void validateCreate(Long questionId, Long userId) {
        if (questionId == null) {
            log.error("생성 오류 - questionId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (userId == null) {
            log.error("생성 오류 - userId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
