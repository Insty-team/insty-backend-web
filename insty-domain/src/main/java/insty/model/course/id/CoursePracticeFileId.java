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
public class CoursePracticeFileId implements Serializable {

    private Long courseId;
    private Long fileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoursePracticeFileId)) {
            return false;
        }
        CoursePracticeFileId that = (CoursePracticeFileId) o;
        return Objects.equals(courseId, that.courseId) &&
                Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, fileId);
    }

    public static CoursePracticeFileId create(Long courseId, Long fileId) {
        validateCreate(courseId, fileId);
        return CoursePracticeFileId.builder()
                .courseId(courseId)
                .fileId(fileId)
                .build();
    }

    private static void validateCreate(Long courseId, Long fileId) {
        if (courseId == null) {
            log.error("CoursePracticeFileId 생성 오류 - courseId : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
        if (fileId == null) {
            log.error("CoursePracticeFileId 생성 오류 - fileId : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
