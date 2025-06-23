package insty.model.course.id;

import insty.error.CourseErrorCode;
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
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseTagId implements Serializable {

    private Long courseId;
    private Long tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseTagId)) {
            return false;
        }
        CourseTagId that = (CourseTagId) o;
        return Objects.equals(courseId, that.courseId) &&
                Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, tagId);
    }

    public static CourseTagId create(Long courseId, Long tagId) {
        validateCreate(courseId, tagId);
        return CourseTagId.builder()
                .courseId(courseId)
                .tagId(tagId)
                .build();
    }

    private static void validateCreate(Long courseId, Long tagId) {
        if (courseId == null) {
            log.error("생성 오류 - courseId : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
        if (tagId == null) {
            log.error("생성 오류 - tagId : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
