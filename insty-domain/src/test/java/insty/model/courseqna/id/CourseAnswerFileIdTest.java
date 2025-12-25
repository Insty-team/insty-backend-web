package insty.model.courseqna.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CourseAnswerFileIdTest {

    @Test
    void create_정상() {
        // given
        Long answerId = 1L;
        Long fileId = 2L;

        // when
        CourseAnswerFileId courseAnswerFileId = CourseAnswerFileId.create(answerId, fileId);

        // then
        assertThat(courseAnswerFileId).isNotNull();
        assertThat(courseAnswerFileId.getAnswerId()).isEqualTo(answerId);
        assertThat(courseAnswerFileId.getFileId()).isEqualTo(fileId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long answerId = 1L;
        Long fileId = 2L;

        CourseAnswerFileId courseAnswerFileId1 = CourseAnswerFileId.create(answerId, fileId);
        CourseAnswerFileId courseAnswerFileId2 = CourseAnswerFileId.create(answerId, fileId);

        // when, then
        assertThat(courseAnswerFileId1).isEqualTo(courseAnswerFileId2);
        assertThat(courseAnswerFileId1.hashCode()).isEqualTo(courseAnswerFileId2.hashCode());
    }

    @Test
    void equals_다른객체_false() {
        // given
        Long answerId1 = 1L;
        Long fileId1 = 2L;
        Long answerId2 = 3L;
        Long fileId2 = 4L;

        CourseAnswerFileId courseAnswerFileId1 = CourseAnswerFileId.create(answerId1, fileId1);
        CourseAnswerFileId courseAnswerFileId2 = CourseAnswerFileId.create(answerId2, fileId2);

        // when, then
        assertThat(courseAnswerFileId1).isNotEqualTo(courseAnswerFileId2);
        assertThat(courseAnswerFileId1.hashCode()).isNotEqualTo(courseAnswerFileId2.hashCode());
    }

    @Test
    void create_에러_answerId가Null_예외() {
        // given
        Long answerId = null;
        Long fileId = 2L;

        // when, then
        assertThatThrownBy(() -> CourseAnswerFileId.create(answerId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }

    @Test
    void create_에러_fileId가Null_예외() {
        // given
        Long answerId = 1L;
        Long fileId = null;

        // when, then
        assertThatThrownBy(() -> CourseAnswerFileId.create(answerId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }
}
