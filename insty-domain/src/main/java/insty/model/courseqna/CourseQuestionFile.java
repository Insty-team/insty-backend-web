package insty.model.courseqna;

import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.courseqna.id.CourseQuestionFileId;
import insty.model.file.File;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "community_question_files", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseQuestionFile extends BaseEntity {

    @EmbeddedId
    private CourseQuestionFileId courseQuestionFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private CourseQuestion courseQuestion;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    public static CourseQuestionFile create(CourseQuestion courseQuestion, File file) {
        validateCreate(courseQuestion, file);
        return CourseQuestionFile.builder()
                .courseQuestionFileId(CourseQuestionFileId.create(courseQuestion.getId(), file.getId()))
                .courseQuestion(courseQuestion)
                .file(file)
                .build();
    }

    private static void validateCreate(CourseQuestion courseQuestion, File file) {
        if (courseQuestion == null) {
            log.error("생성 오류 - courseQuestion : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_CREATE_ERROR);
        }
        if (file == null) {
            log.error("생성 오류 - file : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
