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
public class CourseQuestionViewId implements Serializable {

    private Long courseQuestion;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseQuestionViewId)) return false;

        CourseQuestionViewId that = (CourseQuestionViewId) o;
        return Objects.equals(courseQuestion, that.courseQuestion) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseQuestion, userId);
    }

    public static CourseQuestionViewId create(Long questionId, Long userId) {
        validateCreate(questionId, userId);
        return CourseQuestionViewId.builder()
                .courseQuestion(questionId)
                .userId(userId)
                .build();
    }

    private static void validateCreate(Long questionId, Long userId) {
        if (questionId == null) {
            log.error("생성 오류 - questionId : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_CREATE_ERROR);
        }
        if (userId == null) {
            log.error("생성 오류 - userId : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
