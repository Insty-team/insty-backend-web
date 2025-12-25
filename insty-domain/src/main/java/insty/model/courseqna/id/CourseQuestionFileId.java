package insty.model.courseqna.id;

import insty.error.CourseQnaErrorCode;
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
public class CourseQuestionFileId implements Serializable {

    private Long questionId;
    private Long fileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseQuestionFileId)) return false;

        CourseQuestionFileId that = (CourseQuestionFileId) o;
        return Objects.equals(questionId, that.questionId) &&
                Objects.equals(fileId, that.fileId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, fileId);
    }

    public static CourseQuestionFileId create(Long questionId, Long fileId) {
        validateCreate(questionId, fileId);
        return CourseQuestionFileId.builder()
                .questionId(questionId)
                .fileId(fileId)
                .build();
    }

    private static void validateCreate(Long questionId, Long fileId) {
        if (questionId == null) {
            log.error("생성 오류 - questionId : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_CREATE_ERROR);
        }
        if (fileId == null) {
            log.error("생성 오류 - fileId : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_CREATE_ERROR);
        }
    }
}

