package insty.model.courseqna.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CourseQuestionFileIdTest {

    @Test
    void create_정상() {
        // given
        Long fileId = 1L;
        Long questionId = 2L;

        // when
        CourseQuestionFileId courseQuestionFileId = CourseQuestionFileId.create(questionId, fileId);

        // then
        assertThat(courseQuestionFileId).isNotNull();
        assertThat(courseQuestionFileId.getFileId()).isEqualTo(fileId);
        assertThat(courseQuestionFileId.getQuestionId()).isEqualTo(questionId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long fileId = 1L;
        Long questionId = 2L;

        CourseQuestionFileId courseQuestionFileId1 = CourseQuestionFileId.create(questionId, fileId);
        CourseQuestionFileId courseQuestionFileId2 = CourseQuestionFileId.create(questionId, fileId);

        // when, then
        assertThat(courseQuestionFileId1).isEqualTo(courseQuestionFileId2);
        assertThat(courseQuestionFileId1.hashCode()).isEqualTo(courseQuestionFileId2.hashCode());
    }

    @Test
    void create_에러_questionId가Null_예외() {
        // given
        Long fileId = 1L;
        Long questionId = null;

        // when, then
        assertThatThrownBy(() -> CourseQuestionFileId.create(questionId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_파일ID가Null_예외() {
        // given
        Long fileId = null;
        Long questionId = 2L;

        // when, then
        assertThatThrownBy(() -> CourseQuestionFileId.create(fileId, questionId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_CREATE_ERROR);
    }
}
