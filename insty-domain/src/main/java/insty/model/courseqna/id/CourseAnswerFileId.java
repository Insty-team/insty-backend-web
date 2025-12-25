package insty.model.courseqna.id;

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
public class CourseAnswerFileId implements Serializable {

    private Long answerId;
    private Long fileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseAnswerFileId)) return false;

        CourseAnswerFileId that = (CourseAnswerFileId) o;
        return Objects.equals(answerId, that.answerId) &&
                Objects.equals(fileId, that.fileId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(answerId, fileId);
    }

    public static CourseAnswerFileId create(Long answerId, Long fileId) {
        validateCreate(answerId, fileId);
        return CourseAnswerFileId.builder()
                .answerId(answerId)
                .fileId(fileId)
                .build();
    }

    private static void validateCreate(Long answerId, Long fileId) {
        if (answerId == null) {
            log.error("생성 오류 - answerId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (fileId == null) {
            log.error("생성 오류 - fileId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}

