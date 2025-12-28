package insty.model.courseqna;

import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.courseqna.id.CourseAnswerFileId;
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
@Table(name = "course_answers_files", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseAnswerFile extends BaseEntity {

    @EmbeddedId
    private CourseAnswerFileId courseAnswerFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("answerId")
    @JoinColumn(name = "answer_id", nullable = false)
    private CourseAnswer courseAnswer;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    public static CourseAnswerFile create(CourseAnswer courseAnswer, File file) {
        validateCreate(courseAnswer, file);
        return CourseAnswerFile.builder()
                .courseAnswerFileId(CourseAnswerFileId.create(courseAnswer.getId(), file.getId()))
                .courseAnswer(courseAnswer)
                .file(file)
                .build();
    }

    private static void validateCreate(CourseAnswer courseAnswer, File file) {
        if (courseAnswer == null) {
            log.error("생성 오류 - courseAnswer : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
        }
        if (file == null) {
            log.error("생성 오류 - file : null");
            throw new CustomException(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
        }
    }
}
