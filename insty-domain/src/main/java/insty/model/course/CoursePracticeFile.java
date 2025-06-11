package insty.model.course;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.course.id.CoursePracticeFileId;
import insty.model.file.File;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "course_practice_files", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CoursePracticeFile extends BaseEntity {

    @EmbeddedId
    private CoursePracticeFileId coursePracticeFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File practiceFile;


    public static CoursePracticeFile create(Course course, File practiceFile) {
        validateCreate(course, practiceFile);
        return CoursePracticeFile.builder()
                .coursePracticeFileId(CoursePracticeFileId.create(course.getId(), practiceFile.getId()))
                .course(course)
                .practiceFile(practiceFile)
                .build();
    }

    private static void validateCreate(Course course, File practiceFile) {
        if (course == null) {
            log.error("CoursePracticeFile 생성 오류 - course : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
        if (practiceFile == null) {
            log.error("CoursePracticeFile 생성 오류 - practiceFile : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
